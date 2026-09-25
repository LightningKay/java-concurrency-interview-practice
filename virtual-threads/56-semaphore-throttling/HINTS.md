# Hints — Problem 56: Semaphore Throttling

## Level 1 — Nudge
`Semaphore(n)` permits n concurrent acquisitions. `acquire()` blocks if at the limit; `release()` in finally always returns the permit. The `runUnthrottled` method submits all tasks directly to the pool. The `runThrottled` method wraps each task with semaphore acquire/release around the pool access.

---

## Level 2 — Direction

**`runUnthrottled`**:
```java
long start = System.currentTimeMillis();
AtomicInteger successes = new AtomicInteger(0), failures = new AtomicInteger(0);
CountDownLatch latch = new CountDownLatch(taskCount);
try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < taskCount; i++) {
        exec.submit(() -> {
            try {
                int conn = pool.acquire();
                Thread.sleep(workMs);
                pool.release();
                successes.incrementAndGet();
            } catch (PoolExhaustedException e) {
                failures.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally { latch.countDown(); }
        });
    }
    latch.await();
}
return new Result(successes.get(), failures.get(), System.currentTimeMillis() - start);
```

**`runThrottled`** — add semaphore:
```java
Semaphore semaphore = new Semaphore(maxConcurrent);
// Inside each task:
semaphore.acquire();
try {
    int conn = pool.acquire();
    try { Thread.sleep(workMs); } finally { pool.release(); }
    successes.incrementAndGet();
} finally { semaphore.release(); }
```

---

## Level 3 — Almost there

| Symptom | Likely cause |
|---|---|
| `runThrottled` still produces failures | `semaphore.acquire()` called after `pool.acquire()` — wrong order; semaphore must gate entry to pool |
| `runThrottled` deadlocks | `semaphore.release()` inside `pool.release()` block — exception from pool causes semaphore to never release |
| Elapsed time wrong | Measuring time before submitting tasks, or after submitting but before `latch.await()` |
