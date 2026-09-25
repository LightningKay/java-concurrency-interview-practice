# Hints — Problem 53: ThreadLocal Memory Leak

## Level 1 — Nudge
`ThreadLocal.withInitial(supplier)` runs the supplier *once per thread* that calls `get()`. The key insight: with a fixed platform thread pool, "once per thread" means once per pool member (bounded). With virtual threads, "once per thread" means once per task (unbounded).

---

## Level 2 — Direction

**`LeakyRequestHandler`**:
```java
private static final AtomicInteger instanceCount = new AtomicInteger(0);
private static final ThreadLocal<byte[]> buffer = ThreadLocal.withInitial(() -> {
    instanceCount.incrementAndGet();
    return new byte[10 * 1024];
});

public void handleRequest(String requestId) {
    byte[] buf = buffer.get();  // accesses (and potentially initialises) the ThreadLocal
    // simulate minimal work with buf
}
public int getBufferInstanceCount() { return instanceCount.get(); }
public void resetCount() { instanceCount.set(0); }
```

**`FixedRequestHandler`**:
```java
public void handleRequest(String requestId) {
    byte[] buffer = new byte[10 * 1024];  // local — GC'd when method returns
    requestsHandled.incrementAndGet();
}
```

**`runTasks` pattern**:
```java
CountDownLatch latch = new CountDownLatch(taskCount);
for (int i = 0; i < taskCount; i++) {
    int idx = i;
    executor.submit(() -> {
        try { handler.handleRequest("req-" + idx); }
        finally { latch.countDown(); }
    });
}
latch.await();
```

---

## Level 3 — Almost there

| Symptom | Likely cause |
|---|---|
| `instanceCount` never exceeds 1 with virtual threads | `buffer` is instance field, not `static` — each handler has its own ThreadLocal; tests share one handler instance |
| Platform pool count > poolSize | ThreadLocal not `static` — each submit creates a new ThreadLocal bound to main thread |
| `resetCount` test fails | `instanceCount` is not `static` — resetting the wrong field |
