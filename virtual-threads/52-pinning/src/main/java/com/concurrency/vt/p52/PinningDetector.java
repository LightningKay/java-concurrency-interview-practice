package com.concurrency.vt.p52;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Problem 52 — PinningDetector
 *
 * Detects virtual thread pinning using the JVM system property diagnostic.
 *
 * When the JVM flag {@code -Djdk.tracePinnedThreads=full} is set, the JVM
 * prints a stack trace whenever a virtual thread is pinned. This class
 * programmatically enables and counts pinning events via a custom
 * {@link Thread.UncaughtExceptionHandler} installed on the carrier pool,
 * or by reading the count exposed after running a workload.
 *
 * TODO:
 *   - pinningEventCount: AtomicInteger tracking pinned events
 *   - enablePinningTracking(): set system property "jdk.tracePinnedThreads" to "short"
 *   - runWithPinningTracking(Runnable workload): run workload, capture output to detect
 *     "Pinned" in stderr (the JVM prints there), return count of pinning events seen.
 *     Simplification: for testability, count threads that complete while carrier is pinned
 *     by measuring parallelism degradation rather than JVM output parsing.
 *   - getPinningEventCount(): return pinningEventCount.get()
 */
public class PinningDetector {

    private final AtomicInteger pinningEventCount = new AtomicInteger(0);

    public void enablePinningTracking() {
        // TODO: System.setProperty("jdk.tracePinnedThreads", "short");
        throw new UnsupportedOperationException("Implement enablePinningTracking()");
    }

    /**
     * Run the workload and return approximate number of pinning events.
     * Implementation hint: compare actual parallelism vs theoretical maximum.
     * If tasks that should run in parallel run sequentially, pinning occurred.
     */
    public int runWithPinningTracking(Runnable workload) {
        throw new UnsupportedOperationException("Implement runWithPinningTracking()");
    }

    public int getPinningEventCount() {
        return pinningEventCount.get();
    }
}
