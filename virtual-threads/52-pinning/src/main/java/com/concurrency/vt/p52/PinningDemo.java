package com.concurrency.vt.p52;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Problem 52 — Virtual Thread Pinning: Diagnosis and Fix
 *
 * When a virtual thread enters a {@code synchronized} block and then performs
 * a blocking operation, it is "pinned" to its carrier thread. The carrier
 * cannot be reused for other virtual threads, degrading throughput.
 *
 * This class has two implementations of the same operation:
 *   - BuggyService  — uses synchronized + blocking I/O (pins the carrier)
 *   - FixedService  — uses ReentrantLock + blocking I/O (carrier is released)
 *
 * Java version note:
 *   JEP 491 (Java 24) resolved synchronized pinning for most cases.
 *   On Java 21–23, the timing difference between the two implementations
 *   is measurable. On Java 24+ both may perform similarly — the tests
 *   document *why* the fix was needed and what to look for.
 */
public class PinningDemo {

    /**
     * Simulates work that blocks inside a synchronized block.
     *
     * With N carrier threads and N+N virtual threads, the synchronized
     * variant forces sequential execution because each carrier is pinned
     * while its virtual thread sleeps.
     *
     * TODO:
     *   - Declare a plain Object lock field
     *   - simulateWork(): synchronized(lock) { Thread.sleep(blockMs); }
     *   - processAll(int tasks, long blockMs):
     *       launch `tasks` virtual threads each calling simulateWork(blockMs),
     *       wait for all to finish, return elapsed millis
     */
    public static class BuggyService {
        // TODO: declare Object lock = new Object();

        public void simulateWork(long blockMs) throws InterruptedException {
            throw new UnsupportedOperationException("Implement simulateWork()");
        }

        public long processAll(int tasks, long blockMs) throws InterruptedException {
            throw new UnsupportedOperationException("Implement processAll()");
        }
    }

    /**
     * Simulates the same work but uses ReentrantLock instead of synchronized.
     * The virtual thread can unmount from the carrier while holding the lock
     * and sleeping, allowing the carrier to run other virtual threads.
     *
     * TODO:
     *   - Declare ReentrantLock lock = new ReentrantLock()
     *   - simulateWork(): lock.lock(); try { Thread.sleep(blockMs); } finally { lock.unlock(); }
     *   - processAll(int tasks, long blockMs): same launch pattern as BuggyService
     */
    public static class FixedService {
        // TODO: declare ReentrantLock lock = new ReentrantLock();

        public void simulateWork(long blockMs) throws InterruptedException {
            throw new UnsupportedOperationException("Implement simulateWork()");
        }

        public long processAll(int tasks, long blockMs) throws InterruptedException {
            throw new UnsupportedOperationException("Implement processAll()");
        }
    }
}
