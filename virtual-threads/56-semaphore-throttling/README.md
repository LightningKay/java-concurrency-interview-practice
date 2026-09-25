# Problem 56 — Semaphore Throttling with Virtual Threads

## 🔵 Difficulty: Virtual Threads (Java 21+)

## 📖 Background

Virtual threads remove the thread-pool bottleneck — you can run 100,000 concurrent tasks. But downstream resources (database connections, HTTP connections, file handles) are still finite.

**The trap**: a service that was naturally rate-limited by its thread pool of 200 threads (max 200 concurrent DB queries) suddenly has no such limit. 10,000 virtual threads will all try to acquire a DB connection simultaneously, exhausting the pool and causing failures or cascading timeouts.

**The fix**: use a `Semaphore` to cap concurrency at the resource pool size.

```java
Semaphore semaphore = new Semaphore(maxConcurrent);
// In each task:
semaphore.acquire();       // virtual thread parks here cheaply if at limit
try {
    pool.acquire();
    doWork();
    pool.release();
} finally {
    semaphore.release();
}
```

Virtual threads waiting on `Semaphore.acquire()` are parked without holding a carrier thread — they cost almost nothing while waiting. This is the correct use of `Semaphore` in a virtual-thread world: **not for limiting threads** (unnecessary) but for **limiting resource access** (essential).

## 🎯 Task

Implement `SemaphoreThrottling`:
1. `runUnthrottled(pool, taskCount, workMs)` — all tasks hit the pool with no throttling; returns successes/failures/elapsed
2. `runThrottled(pool, taskCount, maxConcurrent, workMs)` — semaphore caps concurrent pool access; zero failures expected

## 🧠 Interview Talking Points

- Why does removing a thread pool size limit with virtual threads require adding a semaphore?
- How is `Semaphore.acquire()` different from `synchronized` in terms of virtual thread behaviour?
- What is the relationship between `maxConcurrent` (semaphore) and the DB connection pool size?
- What happens if the semaphore limit is higher than the DB pool capacity?
