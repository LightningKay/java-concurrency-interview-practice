package com.concurrency.vt.p53;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Timeout;
import java.util.concurrent.*;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(value = 30, unit = TimeUnit.SECONDS)
class ThreadLocalLeakTest {

    @Test
    @DisplayName("LeakyHandler: platform thread pool reuses buffers — instance count = pool size")
    void leakyHandlerWithPlatformPoolReusesBuffers() throws InterruptedException {
        int poolSize = 10;
        int tasks    = 100;
        ThreadLocalLeak.LeakyRequestHandler handler = new ThreadLocalLeak.LeakyRequestHandler();
        handler.resetCount();

        try (ExecutorService pool = Executors.newFixedThreadPool(poolSize)) {
            ThreadLocalLeak.runTasks(pool, handler, tasks);
        }
        // With a pool of 10 threads, the ThreadLocal initializer runs at most 10 times
        int count = handler.getBufferInstanceCount();
        assertTrue(count <= poolSize,
            "Platform thread pool of " + poolSize + " should reuse ThreadLocals. " +
            "Got " + count + " instances (expected ≤ " + poolSize + ")");
    }

    @Test
    @DisplayName("LeakyHandler: virtual threads create one buffer per task — instance count = task count")
    void leakyHandlerWithVirtualThreadsCreatesOneBufferPerTask() throws InterruptedException {
        int tasks = 200;
        ThreadLocalLeak.LeakyRequestHandler handler = new ThreadLocalLeak.LeakyRequestHandler();
        handler.resetCount();

        try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            ThreadLocalLeak.runTasks(exec, handler, tasks);
        }
        // Virtual threads are never reused. The ThreadLocal initializer runs once per virtual thread.
        int count = handler.getBufferInstanceCount();
        assertEquals(tasks, count,
            "Virtual threads are never pooled: expected " + tasks +
            " buffer instances, got " + count);
    }

    @Test
    @DisplayName("FixedHandler: all tasks complete correctly regardless of executor")
    void fixedHandlerHandlesAllRequests() throws InterruptedException {
        int tasks = 500;
        ThreadLocalLeak.FixedRequestHandler handler = new ThreadLocalLeak.FixedRequestHandler();

        try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            ThreadLocalLeak.runTasks(exec, handler, tasks);
        }
        assertEquals(tasks, handler.getRequestsHandled(),
            "FixedHandler must process every request");
    }

    @Test
    @DisplayName("Instance count contrast: virtual vs platform thread pool")
    void contrastInstanceCountsBetweenExecutors() throws InterruptedException {
        int poolSize = 5;
        int tasks    = 50;

        ThreadLocalLeak.LeakyRequestHandler platformHandler = new ThreadLocalLeak.LeakyRequestHandler();
        platformHandler.resetCount();
        try (ExecutorService platform = Executors.newFixedThreadPool(poolSize)) {
            ThreadLocalLeak.runTasks(platform, platformHandler, tasks);
        }
        int platformInstances = platformHandler.getBufferInstanceCount();

        ThreadLocalLeak.LeakyRequestHandler virtualHandler = new ThreadLocalLeak.LeakyRequestHandler();
        virtualHandler.resetCount();
        try (ExecutorService virtual = Executors.newVirtualThreadPerTaskExecutor()) {
            ThreadLocalLeak.runTasks(virtual, virtualHandler, tasks);
        }
        int virtualInstances = virtualHandler.getBufferInstanceCount();

        assertTrue(virtualInstances > platformInstances,
            "Virtual threads should create more ThreadLocal instances (" + virtualInstances +
            ") than a platform pool of " + poolSize + " (" + platformInstances + ")");
    }
}
