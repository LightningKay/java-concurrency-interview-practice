# Hints — Problem 58: Migration Kata (Capstone)

## Level 1 — Nudge
Three changes, each independent. Make them one at a time and run the tests after each:
1. Swap the executor (one line)
2. Remove ThreadLocal (pass customerId as parameter)
3. Swap synchronized for ReentrantLock (four lines)

---

## Level 2 — Direction

**Change 1 — executor**:
```java
// Before: private final ExecutorService pool = Executors.newFixedThreadPool(poolSize);
private final ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();
```
Remove the `poolSize` constructor parameter — it is no longer meaningful.

**Change 2 — ThreadLocal removal**:
```java
// Remove: static final ThreadLocal<String> REQUEST_CONTEXT = new ThreadLocal<>();
// Change processOrder signature: processOrder(String orderId, String customerId, double amount)
// In submitOrder: pool.submit(() -> processOrder(orderId, customerId, amount))
// In processOrder body: use `customerId` parameter directly instead of REQUEST_CONTEXT.get()
// Remove: REQUEST_CONTEXT.set(...) and REQUEST_CONTEXT.remove()
```

**Change 3 — ReentrantLock**:
```java
// Before:
private final Object orderLock = new Object();
// ... synchronized (orderLock) { Thread.sleep(10); ... }

// After:
private final ReentrantLock orderLock = new ReentrantLock();
// ...
orderLock.lock();
try { Thread.sleep(10); auditLog.add(...); processedCount.incrementAndGet(); }
catch (InterruptedException e) { Thread.currentThread().interrupt(); failedCount.incrementAndGet(); }
finally { orderLock.unlock(); }
```

**`shutdown`**:
```java
pool.close();  // Java 19+ — shuts down and awaits termination
// or: pool.shutdown(); pool.awaitTermination(30, TimeUnit.SECONDS);
```

---

## Level 3 — Almost there

| Symptom | Likely cause |
|---|---|
| Throughput test still fails (timeout) | Executor not changed — still a fixed thread pool |
| Audit log contains null instead of customerId | ThreadLocal removed but parameter not plumbed through to the audit line |
| All orders fail after migration | Exception in `processOrder` swallowed — ReentrantLock unlock not in finally |
| `getAuditLog()` count wrong | `synchronized` on `auditLog.add()` removed but list is `ArrayList` — keep `Collections.synchronizedList` wrapper |
| `processedCount` wrong | Counter increment moved outside the lock — two threads can both enter and both increment for same order |
