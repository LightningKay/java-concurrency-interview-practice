# Problem 58 — Migration Kata: Platform Threads → Virtual Threads (Capstone)

## 🔵 Difficulty: Virtual Threads (Java 21+)

## 📖 Background

This is the capstone problem of the virtual-threads section. It replicates the actual migration task engineers face in production: taking a working legacy service and migrating it to virtual threads correctly, without breaking existing behaviour.

`LegacyOrderService` has three classic pre-Java-21 patterns:
1. **Fixed thread pool** — naturally limits concurrency; becomes the bottleneck
2. **ThreadLocal context** — works fine with pooled threads; leaks with virtual threads
3. **synchronized + blocking I/O** — pins carrier threads on Java 21–23

Your migration in `MigratedOrderService` must:
1. Replace the fixed pool with `newVirtualThreadPerTaskExecutor()`
2. Eliminate `ThreadLocal` — pass context as method parameters
3. Replace `synchronized` with `ReentrantLock` (Java 21 best practice; not required on Java 24+)
4. Pass the 1,000-concurrent-order throughput test that the legacy service cannot pass within the timeout

## 🎯 Task

Implement `MigratedOrderService` to be a drop-in replacement for `LegacyOrderService`:
- Same public API: `submitOrder(orderId, customerId, amount)`, `shutdown()`, `getProcessedCount()`, `getFailedCount()`, `getAuditLog()`
- Same audit log format: `"PROCESSED:orderId:customerId"`
- Zero `ThreadLocal` usage in your implementation
- All `LegacyOrderServiceTest` tests pass (run them against `MigratedOrderService`)
- The 1,000-concurrent-order throughput test passes (< 5 seconds)

## 🧠 Interview Talking Points

- Walk through each change you made and why
- What would break if you used `newVirtualThreadPerTaskExecutor()` without removing `ThreadLocal`?
- Why is `ReentrantLock` preferred over `synchronized` in virtual thread code on Java 21–23?
- What is the theoretical throughput improvement from a 10-thread pool to virtual threads for this workload?
- If this service used an ORM (Hibernate) that has `ThreadLocal` session management internally, how would you handle that?
