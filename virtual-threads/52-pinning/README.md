# Problem 52 — Virtual Thread Pinning: Diagnosis and Fix

## 🔵 Difficulty: Virtual Threads (Java 21+)

## 📖 Background

**Pinning** occurs when a virtual thread cannot be unmounted from its carrier thread during a blocking operation. The carrier is stuck waiting with the virtual thread instead of being freed for other work. Under high concurrency, all carrier threads can be pinned simultaneously, freezing the application.

Primary cause on Java 21–23:
- A virtual thread enters a `synchronized` block and then performs a blocking operation (I/O, `Thread.sleep`, etc.)

**Real-world example**: Netflix's "Java 21 Virtual Threads — Dude, Where's My Lock?" (July 2024) described this exact failure: thousands of sockets stuck in CLOSE_WAIT, JVM alive but serving no traffic.

**Fix**: replace `synchronized` with `ReentrantLock`. The virtual thread can unmount while holding a `ReentrantLock` because the lock is managed by the JVM, not the OS monitor.

**Java 24+ note**: JEP 491 resolved `synchronized` pinning. On Java 24+, both implementations may perform similarly. The problem teaches *why* the fix was needed and how to detect pinning.

## 🎯 Task

Implement `PinningDemo.BuggyService`:
- `synchronized(lock) { Thread.sleep(blockMs); }` inside `simulateWork()`
- `processAll(tasks, blockMs)` — launch tasks virtual threads, return elapsed ms

Implement `PinningDemo.FixedService`:
- `lock.lock(); try { Thread.sleep(blockMs); } finally { lock.unlock(); }` — ReentrantLock
- Same `processAll` pattern

## 🧠 Interview Talking Points

- What is carrier thread pinning and what causes it on Java 21–23?
- Why does `ReentrantLock` fix pinning but `synchronized` causes it?
- How do you detect pinning in production? (`-Djdk.tracePinnedThreads=short`, JFR `jdk.VirtualThreadPinned` event)
- What did JEP 491 (Java 24) change?
- Is `synchronized` always bad with virtual threads? (No — only when blocking I/O is performed inside the synchronized block)
