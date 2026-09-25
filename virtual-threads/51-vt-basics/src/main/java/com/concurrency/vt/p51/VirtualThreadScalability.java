package com.concurrency.vt.p51;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Problem 51 — Virtual Thread Scalability Demo
 *
 * Demonstrates that virtual threads can be created in quantities impossible
 * with platform threads. The key insight: virtual threads are cheap enough
 * to create one-per-task rather than pooling.
 */
public class VirtualThreadScalability {

    /**
     * Launch {@code count} virtual threads, each sleeping for {@code sleepMs}
     * milliseconds. Wait for all to finish and return the number that completed.
     *
     * With count=100_000 and sleepMs=100 this must complete in well under
     * 30 seconds — something impossible with a fixed platform thread pool
     * of the same size.
     */
    public int launchAndWait(int count, long sleepMs) throws InterruptedException {
        // TODO:
        //   AtomicInteger completed = new AtomicInteger(0);
        //   CountDownLatch latch = new CountDownLatch(count);
        //   Use Executors.newVirtualThreadPerTaskExecutor() — submit count tasks
        //   Each task: Thread.sleep(sleepMs), completed.incrementAndGet(), latch.countDown()
        //   latch.await(), return completed.get()
        throw new UnsupportedOperationException("Implement launchAndWait()");
    }
}
