# Hints — Problem 52: Virtual Thread Pinning

## Level 1 — Nudge
The difference between `BuggyService` and `FixedService` is one import and one lock type. `BuggyService` uses the `synchronized` keyword. `FixedService` uses `ReentrantLock`. The `processAll` method pattern is identical in both — launch N virtual threads, each calling `simulateWork`, join all, return elapsed time.

---

## Level 2 — Direction

**`BuggyService.simulateWork`**:
```java
synchronized (lock) {
    Thread.sleep(blockMs);
}
```

**`FixedService.simulateWork`**:
```java
lock.lock();
try { Thread.sleep(blockMs); }
finally { lock.unlock(); }
```

**`processAll` pattern (same for both)**:
```java
long start = System.currentTimeMillis();
List<Thread> threads = new ArrayList<>();
for (int i = 0; i < tasks; i++)
    threads.add(Thread.ofVirtual().start(() -> {
        try { simulateWork(blockMs); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }));
for (Thread t : threads) t.join();
return System.currentTimeMillis() - start;
```

---

## Level 3 — Almost there

| Symptom | Likely cause |
|---|---|
| Both services have the same timing | You used `ReentrantLock` in BuggyService too (or `synchronized` in FixedService) |
| `processAll` returns 0 | Measuring elapsed before joining threads |
| Test timeout | Tasks not joining — exception swallowed inside the lambda, thread silently exits |
| On Java 24+: both timings are equal | Expected — JEP 491 fixed synchronized pinning. The structural difference is still correct. |

**How to detect pinning on Java 21–23**:
Add `-Djdk.tracePinnedThreads=short` to the JVM args. The JVM prints a stack trace to stderr whenever a virtual thread is pinned. JFR also exposes `jdk.VirtualThreadPinned` events.
