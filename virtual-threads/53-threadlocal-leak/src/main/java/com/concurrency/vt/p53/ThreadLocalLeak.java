package com.concurrency.vt.p53;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Problem 53 — ThreadLocal Memory Leak with Virtual Threads
 *
 * Traditional ThreadLocal was designed for pooled platform threads:
 *   - Pool of 200 threads → 200 ThreadLocal instances total, reused indefinitely.
 *
 * Virtual threads are NEVER pooled. One virtual thread per task means:
 *   - 10,000 tasks → 10,000 ThreadLocal instances, each GC'd only when the
 *     virtual thread itself is collected.
 *
 * This is the second major production failure mode after pinning.
 * Libraries that use ThreadLocal internally (Spring Security, MDC, Hibernate)
 * can cause significant heap pressure under high virtual-thread concurrency.
 */
public class ThreadLocalLeak {

    /**
     * Leaky request handler — uses ThreadLocal to hold a per-thread buffer.
     *
     * With a platform thread pool of size N, this creates N buffer instances.
     * With virtual threads (one per task), this creates one buffer per task —
     * bufferInstanceCount grows unboundedly with task count.
     *
     * TODO:
     *   - Add a static ThreadLocal<byte[]> holding a 10KB buffer
     *   - instanceCount: AtomicInteger incremented each time the ThreadLocal
     *     initializer runs (i.e., each time a new buffer is allocated)
     *   - handleRequest(): call threadLocal.get() to access the buffer
     *   - getBufferInstanceCount(): return instanceCount.get()
     */
    public static class LeakyRequestHandler {

        // TODO: static AtomicInteger instanceCount = new AtomicInteger(0);
        // TODO: static ThreadLocal<byte[]> buffer = ThreadLocal.withInitial(() -> {
        //           instanceCount.incrementAndGet();
        //           return new byte[10 * 1024]; // 10KB per instance
        //       });

        public void handleRequest(String requestId) {
            // TODO: access buffer.get() to simulate using the per-thread buffer
            throw new UnsupportedOperationException("Implement handleRequest()");
        }

        public int getBufferInstanceCount() {
            throw new UnsupportedOperationException("Implement getBufferInstanceCount()");
        }

        public void resetCount() {
            throw new UnsupportedOperationException("Implement resetCount()");
        }
    }

    /**
     * Fixed request handler — passes context as a method parameter instead
     * of storing it in a ThreadLocal. One buffer allocated per request,
     * GC'd when the request completes.
     *
     * TODO:
     *   - handleRequest(String requestId): allocate a local byte[] buffer
     *     (do NOT use ThreadLocal), simulate work, increment requestsHandled
     *   - getRequestsHandled(): return requestsHandled.get()
     *   - getBufferInstanceCount(): returns requestsHandled (one buffer per request,
     *     but each is immediately eligible for GC after the method returns)
     */
    public static class FixedRequestHandler {

        private final AtomicInteger requestsHandled = new AtomicInteger(0);

        public void handleRequest(String requestId) {
            // TODO: byte[] buffer = new byte[10 * 1024]; (local, not ThreadLocal)
            //       do minimal work, requestsHandled.incrementAndGet()
            throw new UnsupportedOperationException("Implement handleRequest()");
        }

        public int getRequestsHandled() {
            throw new UnsupportedOperationException("Implement getRequestsHandled()");
        }
    }

    /**
     * Run {@code taskCount} tasks using the given executor.
     * Each task calls {@code handler.handleRequest("req-" + i)}.
     * Waits for all tasks to complete before returning.
     */
    public static void runTasks(ExecutorService executor,
                                 LeakyRequestHandler handler,
                                 int taskCount) throws InterruptedException {
        // TODO: submit taskCount tasks, each calling handler.handleRequest(...)
        //       use a CountDownLatch to wait for all
        throw new UnsupportedOperationException("Implement runTasks() for LeakyRequestHandler");
    }

    public static void runTasks(ExecutorService executor,
                                 FixedRequestHandler handler,
                                 int taskCount) throws InterruptedException {
        // TODO: same as above but for FixedRequestHandler
        throw new UnsupportedOperationException("Implement runTasks() for FixedRequestHandler");
    }
}
