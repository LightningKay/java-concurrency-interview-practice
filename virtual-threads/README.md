# Virtual Threads (Problems 51–58)

## 🔵 Java 21+ Required · Problems 51–58

This tier covers Project Loom's virtual threads — the most significant change to Java concurrency since `java.util.concurrent` in Java 5.

**Prerequisites**: complete the beginner and intermediate tiers first. Virtual threads build on threads, executors, locks, and blocking queues — not replace them.

**Java version**: Java 21 minimum. Problems 54 (Structured Concurrency) and 55 (Scoped Values) require `--enable-preview` on Java 21–24; they are stable on Java 25+. The module pom.xml passes `--enable-preview` automatically.

---

## Why This Tier Exists

No existing practice repo covers virtual threads at implementation depth. Most resources explain *what* virtual threads are. This tier makes you encounter the failure modes:

- **Problem 52** reproduces the Netflix production incident (carrier thread pinning)
- **Problem 53** measures the ThreadLocal memory pressure multiplier
- **Problem 56** demonstrates database connection exhaustion without throttling
- **Problem 58** is the migration kata: take a broken legacy service and fix it

---

## Problems

| # | Problem | Core concept |
|---|---|---|
| 51 | VT Basics | `Thread.ofVirtual()`, `newVirtualThreadPerTaskExecutor()`, scalability proof |
| 52 | Pinning | `synchronized` pins carrier; `ReentrantLock` does not; JFR detection |
| 53 | ThreadLocal Leak | Per-task allocation vs per-thread reuse; memory pressure under concurrency |
| 54 | Structured Concurrency | `ShutdownOnFailure`, `ShutdownOnSuccess`, timeout via `joinUntil` |
| 55 | Scoped Values | `ScopedValue` vs `ThreadLocal`; immutability; child thread inheritance |
| 56 | Semaphore Throttling | Resource limiting (not thread limiting) with virtual threads |
| 57 | VT vs Platform | I/O-bound vs CPU-bound; when virtual threads help and when they don't |
| 58 | Migration Kata | Full legacy service migration — the capstone |

---

## Quick Start

```bash
# Run a single problem
cd virtual-threads/51-vt-basics
mvn test

# Run all virtual-threads problems
cd virtual-threads
mvn test

# Run from the repo root
mvn test -pl virtual-threads/51-vt-basics
```

---

## Key References

- [JEP 444 — Virtual Threads (Java 21)](https://openjdk.org/jeps/444)
- [JEP 491 — Synchronize Virtual Threads without Pinning (Java 24)](https://openjdk.org/jeps/491)
- [JEP 505 — Structured Concurrency (Java 25 preview)](https://openjdk.org/jeps/505)
- [JEP 506 — Scoped Values (Java 25 finalised)](https://openjdk.org/jeps/506)
- Netflix — *Java 21 Virtual Threads: Dude, Where's My Lock?* (July 2024)
