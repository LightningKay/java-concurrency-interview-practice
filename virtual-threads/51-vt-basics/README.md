# Problem 51 — Virtual Thread Basics

## 🔵 Difficulty: Virtual Threads (Java 21+)

## 📖 Background

Java 21 introduced virtual threads (JEP 444) as a production-ready feature. Virtual threads are lightweight threads managed by the JVM — not the OS. They are mounted onto a small pool of **carrier threads** (platform threads, typically one per CPU core) and unmounted when they block, freeing the carrier for other virtual threads.

Key API:
- `Thread.ofVirtual().start(task)` — create and start a virtual thread
- `Thread.ofVirtual().name("name").start(task)` — named virtual thread
- `Executors.newVirtualThreadPerTaskExecutor()` — one virtual thread per submitted task
- `thread.isVirtual()` — check if a thread is virtual

## 🎯 Task

Implement `VirtualThreadBasics`:
1. `createAndStartVirtualThread(Runnable)` — start a virtual thread, return it
2. `createNamedVirtualThread(String, Runnable)` — named virtual thread
3. `isVirtualThread(Thread)` — introspect thread type
4. `runAll(List<Callable<T>>)` — run all tasks on virtual threads, return results in order
5. `runConcurrentCounters(int)` — launch N virtual threads incrementing a shared counter

Implement `VirtualThreadScalability`:
6. `launchAndWait(int count, long sleepMs)` — launch N sleeping virtual threads, return how many completed

## 🧠 Interview Talking Points

- What is a carrier thread? What is the carrier-to-virtual-thread ratio?
- Why can you create 100,000 virtual threads but not 100,000 platform threads?
- What does "mounting" and "unmounting" mean in the context of virtual threads?
- Why should you NOT pool virtual threads (i.e., why is `newCachedThreadPool` wrong for virtual threads)?
- When does the JVM unmount a virtual thread from its carrier?
