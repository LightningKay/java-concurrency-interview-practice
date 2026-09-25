# Hints — Problem 51: Virtual Thread Basics

## Level 1 — Nudge
Java 21 added `Thread.ofVirtual()` as a builder. It works identically to `Thread.ofPlatform()` — just swap the word. For an executor that gives each task its own virtual thread, check `Executors` for a method added in Java 21.

---

## Level 2 — Direction

**`createAndStartVirtualThread`**: `return Thread.ofVirtual().start(task);`

**`createNamedVirtualThread`**: `return Thread.ofVirtual().name(name).start(task);`

**`isVirtualThread`**: `return thread.isVirtual();`

**`runAll`**:
```java
try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
    List<Future<T>> futures = tasks.stream().map(exec::submit).collect(toList());
    List<T> results = new ArrayList<>();
    for (Future<T> f : futures) results.add(f.get());
    return results;
}
```
Use index-based iteration (not stream on futures) to preserve submission order.

**`runConcurrentCounters`**:
```java
AtomicLong counter = new AtomicLong(0);
List<Thread> threads = new ArrayList<>();
for (int i = 0; i < count; i++)
    threads.add(Thread.ofVirtual().start(counter::incrementAndGet));
for (Thread t : threads) t.join();
return counter.get();
```

**`launchAndWait`**: Use `Executors.newVirtualThreadPerTaskExecutor()` and a `CountDownLatch(count)`. Each task: `Thread.sleep(sleepMs); completed.incrementAndGet(); latch.countDown()`.

---

## Level 3 — Almost there

| Symptom | Likely cause |
|---|---|
| `runAll` returns out of order | Collecting futures with `stream().map(f -> f.get())` — short-circuits on first; iterate by index instead |
| `runConcurrentCounters` returns wrong count | Using non-atomic `long` instead of `AtomicLong` |
| `launchAndWait` test times out | Not submitting to an executor — creating threads manually one-at-a-time sequentially |
| `isVirtualThread` always returns false | Calling `Thread.currentThread().isVirtual()` instead of `thread.isVirtual()` |
