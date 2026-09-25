# Problem 53 — ThreadLocal Memory Leak with Virtual Threads

## 🔵 Difficulty: Virtual Threads (Java 21+)

## 📖 Background

`ThreadLocal` was designed for pooled platform threads: a pool of N threads means N instances of the ThreadLocal value, created once and reused for the lifetime of the pool.

Virtual threads are **never pooled**. `newVirtualThreadPerTaskExecutor()` creates a new virtual thread for every task. If a ThreadLocal is accessed, its initializer runs once per virtual thread. With 10,000 tasks:
- Platform pool (100 threads): 100 ThreadLocal instances, reused 100× each
- Virtual threads: 10,000 ThreadLocal instances, each used once, then eligible for GC

This is heap pressure proportional to concurrency — the more virtual threads you use, the worse it gets. Libraries are the hidden danger: Spring Security, Logback MDC, and Hibernate all used ThreadLocal internally.

**Fix**: pass context as method parameters, or use `ScopedValue` (Problem 55).

## 🎯 Task

Implement `LeakyRequestHandler`:
- `ThreadLocal<byte[]>` with 10KB buffer, instance counter in the initializer
- `handleRequest(String)` accesses the ThreadLocal
- `getBufferInstanceCount()` returns the counter

Implement `FixedRequestHandler`:
- Allocate buffer as a local variable inside `handleRequest()` — no ThreadLocal
- `getRequestsHandled()` returns request count

Implement both `runTasks()` overloads: submit N tasks to the given executor, wait for all.

## 🧠 Interview Talking Points

- Why does ThreadLocal cause a memory leak with virtual threads specifically?
- What production libraries use ThreadLocal internally that you need to watch for?
- What is `ScopedValue` and how does it address this problem?
- If you cannot change a library that uses ThreadLocal internally, what are your options?
