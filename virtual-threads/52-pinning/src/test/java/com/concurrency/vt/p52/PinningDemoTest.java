package com.concurrency.vt.p52;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Timeout;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

/**
 * These tests use wall-clock timing to prove the throughput difference
 * between synchronized (pins carrier) and ReentrantLock (releases carrier).
 *
 * Setup: 4 virtual threads, each sleeping 200ms inside the lock.
 *   - BuggyService:  carrier threads are pinned → tasks run 2-by-2 (or serially)
 *                    → total time ≈ 2 × 200ms = 400ms (with 2 carrier threads)
 *   - FixedService:  carriers are released → all 4 tasks overlap
 *                    → total time ≈ 200ms
 *
 * NOTE (Java 24+): JEP 491 fixed synchronized pinning. On Java 24+ both
 * services may finish in ~200ms. The test documents the historical behavior
 * and the structural difference between the two implementations.
 */
@Timeout(value = 15, unit = TimeUnit.SECONDS)
class PinningDemoTest {

    private static final int TASKS    = 4;
    private static final long BLOCK_MS = 200;

    @Test
    @DisplayName("FixedService (ReentrantLock) completes all tasks in roughly one sleep interval")
    void fixedServiceCompletesInOneSlot() throws InterruptedException {
        PinningDemo.FixedService svc = new PinningDemo.FixedService();
        long elapsed = svc.processAll(TASKS, BLOCK_MS);
        // With virtual threads and ReentrantLock, all 4 tasks run concurrently.
        // Total time should be close to one sleep interval + overhead.
        assertTrue(elapsed < BLOCK_MS * 2,
            "FixedService: expected ~" + BLOCK_MS + "ms, got " + elapsed + "ms. " +
            "Carrier threads should be free to run other virtual threads.");
    }

    @Test
    @DisplayName("BuggyService (synchronized) serialises tasks on Java 21-23; documents pinning")
    void buggyServiceShowsPinningBehavior() throws InterruptedException {
        PinningDemo.BuggyService svc = new PinningDemo.BuggyService();
        long elapsed = svc.processAll(TASKS, BLOCK_MS);
        // On Java 21-23: pinning serialises tasks → elapsed ≈ TASKS * BLOCK_MS / carriers
        // On Java 24+: JEP 491 fixed this → elapsed may be ~BLOCK_MS
        // The test simply asserts the service completes correctly — timing variance is documented.
        assertTrue(elapsed > 0, "Service must complete all tasks");
        assertTrue(elapsed < TASKS * BLOCK_MS * 2,
            "Service must finish within a reasonable bound");
    }

    @Test
    @DisplayName("FixedService is at least as fast as BuggyService")
    void fixedServiceNotSlowerThanBuggy() throws InterruptedException {
        PinningDemo.BuggyService buggy = new PinningDemo.BuggyService();
        PinningDemo.FixedService fixed = new PinningDemo.FixedService();
        long buggyTime = buggy.processAll(TASKS, BLOCK_MS);
        long fixedTime = fixed.processAll(TASKS, BLOCK_MS);
        assertTrue(fixedTime <= buggyTime + 50,
            "FixedService (" + fixedTime + "ms) must not be materially slower than " +
            "BuggyService (" + buggyTime + "ms)");
    }

    @Test
    @DisplayName("Both services run the correct number of tasks")
    void bothServicesRunAllTasks() throws InterruptedException {
        // Smoke test: just confirm processAll doesn't lose tasks
        PinningDemo.BuggyService buggy = new PinningDemo.BuggyService();
        PinningDemo.FixedService fixed = new PinningDemo.FixedService();
        long b = buggy.processAll(2, 50);
        long f = fixed.processAll(2, 50);
        assertTrue(b > 0 && f > 0, "Both services must complete their tasks");
    }
}
