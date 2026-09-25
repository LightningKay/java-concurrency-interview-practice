# Problem 57 — Virtual Threads vs Platform Threads: When to Use Which

## 🔵 Difficulty: Virtual Threads (Java 21+)

## 📖 Background

Virtual threads are not a universal replacement for platform threads. The distinction is fundamental:

**I/O-bound work** (network, disk, DB queries, `Thread.sleep`):
- Thread spends most time waiting — not using the CPU
- Virtual threads shine: carrier is freed while thread waits, so N virtual threads use far fewer carriers than N platform threads
- Result: throughput scales with I/O concurrency, not thread count

**CPU-bound work** (sorting, hashing, compression, ML inference):
- Thread actively uses the CPU every cycle
- Virtual threads offer no benefit: carrier is still occupied the whole time
- A fixed pool of N_CPU platform threads is optimal — adding more threads causes context-switch overhead
- Using virtual threads for CPU-bound work can be *slower* than a tuned platform thread pool

**Rule of thumb**: if the task blocks, use virtual threads. If the task computes, use a fixed platform thread pool sized to `availableProcessors()`.

## 🎯 Task

Implement `VirtualPlatformInteraction`:
1. `runIoBoundVirtual(taskCount, ioDelayMs)` — virtual threads sleeping (I/O simulation)
2. `runIoBoundPlatform(taskCount, poolSize, ioDelayMs)` — fixed platform pool, same work
3. `runCpuBoundVirtual(taskCount, iterations)` — virtual threads doing computation
4. `runCpuBoundPlatform(taskCount, iterations)` — platform pool sized to CPU count
5. `virtualProducerPlatformConsumer(itemCount, capacity)` — virtual producers feeding a platform consumer via `LinkedBlockingQueue`

## 🧠 Interview Talking Points

- For a service that handles 10,000 concurrent HTTP requests (each doing one DB query), should you use virtual threads or a platform thread pool? Why?
- For a service that processes video transcoding jobs, which executor type is appropriate?
- What is the optimal size for a platform thread pool doing CPU-bound work? Why?
- If you mix I/O and CPU work in the same task, how do you decide?
