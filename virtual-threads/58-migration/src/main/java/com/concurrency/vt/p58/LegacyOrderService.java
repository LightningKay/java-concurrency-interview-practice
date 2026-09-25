package com.concurrency.vt.p58;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * Problem 58 — Migration Kata: Platform Threads → Virtual Threads
 *
 * This is the capstone of the virtual-threads section.
 *
 * You are given LegacyOrderService — a typical pre-Java-21 service that:
 *   1. Uses a fixed platform thread pool (Executors.newFixedThreadPool)
 *   2. Stores per-request context in a ThreadLocal
 *   3. Uses synchronized for order state protection
 *   4. Cannot handle more than ~poolSize concurrent orders without queuing
 *
 * Your task: implement MigratedOrderService using virtual threads while:
 *   - Keeping the same public API and all existing tests passing
 *   - Correctly replacing ThreadLocal with constructor/method parameters
 *   - Replacing synchronized + blocking I/O with ReentrantLock (Java 21 best practice)
 *   - Passing the NEW high-concurrency throughput test that LegacyOrderService fails
 *
 * DO NOT modify LegacyOrderService or its tests.
 * Implement MigratedOrderService below it.
 */
public class LegacyOrderService {

    // ── Legacy implementation (DO NOT MODIFY) ───────────────────────────────

    private static final ThreadLocal<String> REQUEST_CONTEXT = new ThreadLocal<>();

    private final ExecutorService pool;
    private final int poolSize;
    private final AtomicInteger processedCount = new AtomicInteger(0);
    private final AtomicInteger failedCount    = new AtomicInteger(0);
    private final List<String> auditLog = Collections.synchronizedList(new ArrayList<>());

    /** Simulates database latency inside the lock — the pinning risk on Java 21–23. */
    private final Object orderLock = new Object();

    public LegacyOrderService(int poolSize) {
        this.poolSize = poolSize;
        this.pool = Executors.newFixedThreadPool(poolSize);
    }

    public Future<OrderResult> submitOrder(String orderId, String customerId, double amount) {
        return pool.submit(() -> processOrder(orderId, customerId, amount));
    }

    private OrderResult processOrder(String orderId, String customerId, double amount) {
        REQUEST_CONTEXT.set(customerId);
        try {
            synchronized (orderLock) {
                // Simulates DB write latency — pins carrier on Java 21–23 virtual threads
                Thread.sleep(10);
                String ctx = REQUEST_CONTEXT.get();
                auditLog.add("PROCESSED:" + orderId + ":" + ctx);
                processedCount.incrementAndGet();
                return new OrderResult(orderId, customerId, amount, true);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            failedCount.incrementAndGet();
            return new OrderResult(orderId, customerId, amount, false);
        } finally {
            REQUEST_CONTEXT.remove();
        }
    }

    public void shutdown() throws InterruptedException {
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
    }

    public int getProcessedCount() { return processedCount.get(); }
    public int getFailedCount()    { return failedCount.get(); }
    public List<String> getAuditLog() { return Collections.unmodifiableList(auditLog); }
    public int getPoolSize() { return poolSize; }
}
