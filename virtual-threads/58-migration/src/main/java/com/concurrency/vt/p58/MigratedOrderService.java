package com.concurrency.vt.p58;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Problem 58 — MigratedOrderService
 *
 * Replace the platform thread pool with virtual threads.
 * Eliminate ThreadLocal by passing context as method parameters.
 * Replace synchronized + blocking I/O with ReentrantLock.
 *
 * The migrated service must:
 *   ✓ Pass all legacy compatibility tests (same API, same behaviour)
 *   ✓ Pass the 1,000-concurrent-order throughput test that the legacy service times out on
 *   ✓ Have zero ThreadLocal usage
 *   ✓ Use ReentrantLock (not synchronized) around the simulated DB write
 *
 * TODO — step by step:
 *
 * 1. Replace ExecutorService pool:
 *      pool = Executors.newVirtualThreadPerTaskExecutor();
 *    (keep it in a field; close it in shutdown())
 *
 * 2. Remove ThreadLocal entirely.
 *    Pass customerId directly to processOrder() as a parameter — it already
 *    receives orderId and amount, just add customerId.
 *
 * 3. Replace synchronized(orderLock) with:
 *      private final ReentrantLock orderLock = new ReentrantLock();
 *      orderLock.lock();
 *      try { Thread.sleep(10); ... }
 *      finally { orderLock.unlock(); }
 *
 * 4. Keep processedCount, failedCount, auditLog, and their getter methods
 *    identical to LegacyOrderService so tests are interchangeable.
 */
public class MigratedOrderService {

    // TODO: replace fixed thread pool with virtual thread executor
    private ExecutorService pool;

    // TODO: replace synchronized lock with ReentrantLock
    // private final ReentrantLock orderLock = new ReentrantLock();

    private final AtomicInteger processedCount = new AtomicInteger(0);
    private final AtomicInteger failedCount    = new AtomicInteger(0);
    private final List<String> auditLog = Collections.synchronizedList(new ArrayList<>());

    public MigratedOrderService() {
        // TODO: pool = Executors.newVirtualThreadPerTaskExecutor();
        throw new UnsupportedOperationException("Implement MigratedOrderService constructor");
    }

    public Future<OrderResult> submitOrder(String orderId, String customerId, double amount) {
        // TODO: pool.submit(() -> processOrder(orderId, customerId, amount))
        throw new UnsupportedOperationException("Implement submitOrder()");
    }

    private OrderResult processOrder(String orderId, String customerId, double amount) {
        // TODO:
        //   orderLock.lock();
        //   try {
        //       Thread.sleep(10);   // simulated DB write
        //       auditLog.add("PROCESSED:" + orderId + ":" + customerId);
        //       processedCount.incrementAndGet();
        //       return new OrderResult(orderId, customerId, amount, true);
        //   } catch (InterruptedException e) {
        //       Thread.currentThread().interrupt();
        //       failedCount.incrementAndGet();
        //       return new OrderResult(orderId, customerId, amount, false);
        //   } finally {
        //       orderLock.unlock();
        //   }
        throw new UnsupportedOperationException("Implement processOrder()");
    }

    public void shutdown() throws InterruptedException {
        // TODO: pool.close() or pool.shutdown() + awaitTermination
        throw new UnsupportedOperationException("Implement shutdown()");
    }

    public int getProcessedCount() { return processedCount.get(); }
    public int getFailedCount()    { return failedCount.get(); }
    public List<String> getAuditLog() { return Collections.unmodifiableList(auditLog); }
}
