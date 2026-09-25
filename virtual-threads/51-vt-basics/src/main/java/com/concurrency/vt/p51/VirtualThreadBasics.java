package com.concurrency.vt.p51;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * Problem 51 — Virtual Thread Basics
 *
 * Explore the Java 21 virtual thread API: creation, executor, introspection,
 * and the carrier-thread model.
 *
 * Key API:
 *   Thread.ofVirtual().start(Runnable)
 *   Thread.ofVirtual().name(String).unstarted(Runnable)
 *   Thread.ofPlatform().start(Runnable)
 *   Executors.newVirtualThreadPerTaskExecutor()
 *   Thread.isVirtual()
 */
public class VirtualThreadBasics {

    /**
     * Create, start, and return a virtual thread running {@code task}.
     * The thread must be started before this method returns.
     */
    public Thread createAndStartVirtualThread(Runnable task) {
        // TODO: use Thread.ofVirtual().start(task)
        throw new UnsupportedOperationException("Implement createAndStartVirtualThread()");
    }

    /**
     * Create, start, and return a named virtual thread running {@code task}.
     */
    public Thread createNamedVirtualThread(String name, Runnable task) {
        // TODO: use Thread.ofVirtual().name(name).start(task)
        throw new UnsupportedOperationException("Implement createNamedVirtualThread()");
    }

    /**
     * Return true if {@code thread} is a virtual thread, false otherwise.
     */
    public boolean isVirtualThread(Thread thread) {
        // TODO: thread.isVirtual()
        throw new UnsupportedOperationException("Implement isVirtualThread()");
    }

    /**
     * Submit all callables to a virtual-thread-per-task executor, wait for
     * all to complete, and return the list of results in submission order.
     *
     * @param tasks list of Callable<T>
     * @param <T>   result type
     */
    public <T> List<T> runAll(List<Callable<T>> tasks) throws Exception {
        // TODO: use Executors.newVirtualThreadPerTaskExecutor() in try-with-resources
        //       submit all tasks, collect futures, join all, return results
        throw new UnsupportedOperationException("Implement runAll()");
    }

    /**
     * Create {@code count} virtual threads, each incrementing a shared counter,
     * wait for all to finish, and return the final counter value.
     *
     * Tip: the counter needs to be thread-safe.
     */
    public long runConcurrentCounters(int count) throws InterruptedException {
        // TODO: create `count` virtual threads via Thread.ofVirtual().start(...)
        //       each thread increments an AtomicLong
        //       join all threads, return counter value
        throw new UnsupportedOperationException("Implement runConcurrentCounters()");
    }

    /**
     * Return the name of the thread that executed {@code task}.
     * The task is run on a virtual thread whose name is {@code threadName}.
     */
    public String getExecutingThreadName(String threadName, Runnable task)
            throws InterruptedException {
        // TODO: start named virtual thread, join it, return its name
        throw new UnsupportedOperationException("Implement getExecutingThreadName()");
    }
}
