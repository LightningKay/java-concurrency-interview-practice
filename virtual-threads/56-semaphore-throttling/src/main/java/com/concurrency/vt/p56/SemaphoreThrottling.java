package com.concurrency.vt.p56;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * Problem 56 — Semaphore Throttling with Virtual Threads
 *
 * Virtual threads eliminate the thread-pool bottleneck, but resource pools
 * (database connections, HTTP connections, file handles) are still finite.
 *
 * Problem: with newVirtualThreadPerTaskExecutor() and no throttling,
 *   1,000 tasks may all try to acquire a DB connection simultaneously.
 *   If the pool cap is 20, 980 tasks are rejected or queue inside the pool
 *   with no back-pressure — leading to errors, connection timeouts, or OOM.
 *
 * Solution: use a Semaphore to cap concurrency at the resource pool size.
 *   Virtual threads waiting on Semaphore.acquire() are parked cheaply —
 *   they do NOT pin carrier threads (ReentrantLock internals, no synchronized).
 *
 * This is the correct use of Semaphore in a virtual-thread world:
 *   NOT for limiting threads (unnecessary) — but for limiting resources (essential).
 */
public class SemaphoreThrottling {

    /**
     * Simulates a database connection pool with a hard capacity limit.
     * Attempts to acquire beyond capacity throw {@link PoolExhaustedException}.
     */
    public static class SimulatedDbPool {
        private final int capacity;
        private final AtomicInteger inUse   = new AtomicInteger(0);
        private final AtomicInteger total   = new AtomicInteger(0);
        private final AtomicInteger rejected = new AtomicInteger(0);

        public SimulatedDbPool(int capacity) { this.capacity = capacity; }

        /** Acquire a connection. Throws if pool is exhausted (no waiting). */
        public int acquire() {
            int current = inUse.incrementAndGet();
            if (current > capacity) {
                inUse.decrementAndGet();
                rejected.incrementAndGet();
                throw new PoolExhaustedException("Pool exhausted at capacity " + capacity);
            }
            return total.incrementAndGet();
        }

        /** Release a connection back to the pool. */
        public void release() { inUse.decrementAndGet(); }

        public int getRejectedCount()  { return rejected.get(); }
        public int getPeakInUse()      { return inUse.get(); }
    }

    public static class PoolExhaustedException extends RuntimeException {
        public PoolExhaustedException(String msg) { super(msg); }
    }

    // ── Your implementations ────────────────────────────────────────────────

    /**
     * Runs {@code taskCount} virtual thread tasks against the pool with NO throttling.
     * All tasks attempt to acquire a connection concurrently.
     *
     * @return result containing success count, failure count, and elapsed ms
     *
     * TODO:
     *   - Create newVirtualThreadPerTaskExecutor()
     *   - Submit taskCount tasks, each: pool.acquire() → Thread.sleep(workMs) → pool.release()
     *   - Catch PoolExhaustedException → increment failures
     *   - Wait for all tasks, return Result(successes, failures, elapsedMs)
     */
    public Result runUnthrottled(SimulatedDbPool pool, int taskCount, long workMs)
            throws InterruptedException {
        throw new UnsupportedOperationException("Implement runUnthrottled()");
    }

    /**
     * Runs {@code taskCount} virtual thread tasks against the pool, throttled
     * so at most {@code maxConcurrent} tasks hold a connection simultaneously.
     *
     * Virtual threads waiting on the Semaphore are parked cheaply —
     * they do not consume carrier threads while waiting.
     *
     * @return result containing success count, failure count (should be 0), and elapsed ms
     *
     * TODO:
     *   - Semaphore semaphore = new Semaphore(maxConcurrent)
     *   - Each task: semaphore.acquire() → pool.acquire() → Thread.sleep(workMs)
     *                → pool.release() → semaphore.release()  (in finally)
     *   - No PoolExhaustedException should occur because the semaphore caps concurrency
     *   - Return Result(successes, failures, elapsedMs)
     */
    public Result runThrottled(SimulatedDbPool pool, int taskCount,
                               int maxConcurrent, long workMs)
            throws InterruptedException {
        throw new UnsupportedOperationException("Implement runThrottled()");
    }

    public record Result(int successes, int failures, long elapsedMs) {}
}
