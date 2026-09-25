# Hints — Problem 57: Virtual vs Platform Threads

## Level 1 — Nudge
The four benchmark methods all follow the same pattern: create executor, submit tasks, wait for all, measure elapsed time. The only difference is the executor type and the task body. `burnCpu` is a static utility provided — call it from CPU-bound tasks.

---

## Level 2 — Direction

**I/O-bound pattern** (same for both, different executor):
```java
long start = System.currentTimeMillis();
CountDownLatch latch = new CountDownLatch(taskCount);
try (ExecutorService exec = /* virtual or platform */) {
    for (int i = 0; i < taskCount; i++) {
        exec.submit(() -> {
            try { Thread.sleep(ioDelayMs); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            finally { latch.countDown(); }
        });
    }
    latch.await();
}
return System.currentTimeMillis() - start;
```

**`runCpuBoundPlatform`**: use `Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())`. Do NOT use virtual threads for CPU work.

**`virtualProducerPlatformConsumer`**:
```java
LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>(queueCapacity);
AtomicInteger consumed = new AtomicInteger(0);
// Consumer: platform thread
Thread consumer = Thread.ofPlatform().start(() -> {
    while (true) {
        try {
            String item = queue.take();
            if (item == null) break;  // poison pill
            consumed.incrementAndGet();
        } catch (InterruptedException e) { break; }
    }
});
// Producers: virtual threads
try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < itemCount; i++) {
        int idx = i;
        exec.submit(() -> { try { queue.put("item-" + idx); } catch (InterruptedException e) {} });
    }
}  // all producers done when exec closes
queue.put(null);  // poison pill
consumer.join();
return consumed.get();
```

---

## Level 3 — Almost there

| Symptom | Likely cause |
|---|---|
| CPU-bound platform not faster than virtual | Pool sized too large (using `newCachedThreadPool`) instead of `availableProcessors()` |
| Producer-consumer deadlocks | Poison pill sent before all producers finish — send it after the executor closes |
| I/O virtual not faster than platform pool | Platform pool not undersized enough — use 10 threads for 200 tasks to see the difference |
