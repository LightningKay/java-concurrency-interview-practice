package com.concurrency.vt.p57;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Problem 57 — Virtual Threads vs Platform Threads: When to Use Which
 *
 * Virtual threads are NOT a universal replacement for platform threads.
 * They excel at I/O-bound work where threads spend most time waiting.
 * They offer NO benefit — and add slight overhead — for CPU-bound work.
 *
 * This problem forces you to implement both types of workloads and compare
 * throughput, making the distinction concrete rather than theoretical.
 *
 * Rules:
 *   I/O-bound simulation  → Thread.sleep(ms) — thread blocks waiting for I/O
 *   CPU-bound simulation  → tight computation loop consuming actual CPU cycles
 */
public class VirtualPlatformInteraction {

    /**
     * Run {@code taskCount} I/O-bound tasks (each sleeping {@code ioDelayMs})
     * using a virtual-thread-per-task executor.
     *
     * @return elapsed milliseconds
     *
     * TODO: Executors.newVirtualThreadPerTaskExecutor(), submit taskCount tasks
     *       each doing Thread.sleep(ioDelayMs), wait for all, return elapsed
     */
    public long runIoBoundVirtual(int taskCount, long ioDelayMs)
            throws InterruptedException {
        throw new UnsupportedOperationException("Implement runIoBoundVirtual()");
    }

    /**
     * Run {@code taskCount} I/O-bound tasks using a fixed platform thread pool
     * of size {@code poolSize}.
     *
     * @return elapsed milliseconds
     *
     * TODO: Executors.newFixedThreadPool(poolSize), same task pattern
     */
    public long runIoBoundPlatform(int taskCount, int poolSize, long ioDelayMs)
            throws InterruptedException {
        throw new UnsupportedOperationException("Implement runIoBoundPlatform()");
    }

    /**
     * Run {@code taskCount} CPU-bound tasks using a virtual-thread-per-task executor.
     * Each task performs {@code iterations} iterations of pure computation.
     *
     * @return elapsed milliseconds
     *
     * TODO: same executor as runIoBoundVirtual but tasks call burnCpu(iterations)
     */
    public long runCpuBoundVirtual(int taskCount, long iterations)
            throws InterruptedException {
        throw new UnsupportedOperationException("Implement runCpuBoundVirtual()");
    }

    /**
     * Run {@code taskCount} CPU-bound tasks using a fixed thread pool sized to
     * the number of available CPU cores.
     *
     * @return elapsed milliseconds
     *
     * TODO: Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
     */
    public long runCpuBoundPlatform(int taskCount, long iterations)
            throws InterruptedException {
        throw new UnsupportedOperationException("Implement runCpuBoundPlatform()");
    }

    /**
     * Hand-off from virtual producer threads to a single platform consumer thread
     * via a LinkedBlockingQueue.
     *
     * Produces {@code itemCount} items from virtual threads into the queue.
     * The consumer (platform thread) drains the queue until it receives a
     * poison pill (null), counting each real item received.
     *
     * @return number of real items consumed by the platform thread
     *
     * TODO:
     *   LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>(capacity)
     *   AtomicInteger consumed = new AtomicInteger(0)
     *   Start a platform consumer thread: while(true) { item = queue.take(); if null break; consumed++ }
     *   Submit itemCount virtual thread producers, each queue.put("item-" + i)
     *   After all producers finish, queue.put(null) — poison pill
     *   consumer.join()
     *   return consumed.get()
     */
    public int virtualProducerPlatformConsumer(int itemCount, int queueCapacity)
            throws InterruptedException {
        throw new UnsupportedOperationException("Implement virtualProducerPlatformConsumer()");
    }

    // ── Utility ─────────────────────────────────────────────────────────────

    /** Simulate CPU-bound work: compute sum of 1..iterations. */
    static long burnCpu(long iterations) {
        long sum = 0;
        for (long i = 0; i < iterations; i++) sum += i;
        return sum;
    }
}
