package com.concurrency.vt.p57;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Timeout;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(value = 30, unit = TimeUnit.SECONDS)
class VirtualPlatformInteractionTest {

    private final VirtualPlatformInteraction vpi = new VirtualPlatformInteraction();
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

    // ── I/O-bound comparison ─────────────────────────────────────────────────

    @Test
    @DisplayName("I/O-bound: virtual threads dramatically outperform small platform pool")
    void ioBoundVirtualFasterThanSmallPool() throws InterruptedException {
        int tasks     = 200;
        long delayMs  = 50;
        int smallPool = 10;   // deliberately undersized

        long vtTime = vpi.runIoBoundVirtual(tasks, delayMs);
        long ptTime = vpi.runIoBoundPlatform(tasks, smallPool, delayMs);

        // Virtual: all 200 tasks run concurrently → ~50ms
        // Platform pool of 10: 200/10 = 20 batches × 50ms = ~1000ms
        assertTrue(vtTime < ptTime,
            "Virtual threads (" + vtTime + "ms) must be faster than " +
            "platform pool of " + smallPool + " (" + ptTime + "ms) for I/O-bound work");
    }

    @Test
    @DisplayName("I/O-bound: virtual threads finish near one sleep interval")
    void ioBoundVirtualCompletesInOneSlot() throws InterruptedException {
        int tasks    = 1_000;
        long delayMs = 100;

        long elapsed = vpi.runIoBoundVirtual(tasks, delayMs);

        // 1,000 tasks each sleeping 100ms; virtual threads should do this in ~100ms
        assertTrue(elapsed < delayMs * 5,
            "1,000 virtual threads (100ms I/O each) must finish in < 500ms, took " + elapsed + "ms");
    }

    // ── CPU-bound comparison ─────────────────────────────────────────────────

    @Test
    @DisplayName("CPU-bound: platform thread pool not slower than virtual threads")
    void cpuBoundPlatformNotSlowerThanVirtual() throws InterruptedException {
        int tasks      = CPU_COUNT * 4;
        long iterations = 5_000_000L;

        long vtTime = vpi.runCpuBoundVirtual(tasks, iterations);
        long ptTime = vpi.runCpuBoundPlatform(tasks, iterations);

        // Platform thread pool sized to CPU count should be >= virtual thread performance.
        // Virtual threads context-switch on top of the same carriers — no speedup for CPU work.
        // Allow 30% tolerance for scheduling variance.
        assertTrue(ptTime <= vtTime * 1.3,
            "For CPU-bound work, platform pool (" + ptTime + "ms) should match or beat " +
            "virtual threads (" + vtTime + "ms)");
    }

    // ── Virtual producer → platform consumer ────────────────────────────────

    @Test
    @DisplayName("Virtual producer → platform consumer handoff delivers all items")
    void producerConsumerDeliversAllItems() throws InterruptedException {
        int items    = 500;
        int capacity = 50;

        int consumed = vpi.virtualProducerPlatformConsumer(items, capacity);

        assertEquals(items, consumed,
            "All " + items + " items must be delivered to the platform consumer");
    }

    @Test
    @DisplayName("Virtual producer → platform consumer completes without deadlock")
    void producerConsumerCompletesCleanly() throws InterruptedException {
        int consumed = vpi.virtualProducerPlatformConsumer(100, 10);
        assertEquals(100, consumed);
    }
}
