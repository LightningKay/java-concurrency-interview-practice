package com.concurrency.beginner.p03;

/**
 * Problem 03 - Synchronized Counter (Thread-Safe version)
 *
 * Implement all methods so they are safe to call from multiple threads simultaneously.
 * Use the `synchronized` keyword (do NOT use AtomicInteger for this exercise).
 */
public class ThreadSafeCounter {

    private int count = 0;

    /** Increments the counter by 1. */
    public synchronized void increment() {
        // TODO: synchronize and increment
        count += 1;
    }

    /** Decrements the counter by 1. */
    public synchronized void decrement() {
        // TODO: synchronize and decrement
        count -= 1;
    }

    /** Returns the current count. Must reflect all completed increments/decrements. */
    public synchronized int getCount() {
        // TODO: synchronize and return count
        return count;
    }

    /** Resets the counter to 0. */
    public synchronized void reset() {
        // TODO: synchronize and reset
        count = 0;
    }

    /**
     * Increments the counter by `delta` as a single atomic operation.
     * @param delta the amount to add (can be negative)
     */
    public synchronized void incrementBy(int delta) {
        // TODO: synchronize and add delta
        count += delta;
    }
}
