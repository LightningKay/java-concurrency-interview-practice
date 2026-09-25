# Problem 41 — Custom Barrier Synchronizer

## 🔴 Difficulty: Expert

## 📖 Background

A **barrier** causes a group of threads to wait at a rendezvous point until all participants have arrived, then releases them together — often called a *barrier synchronization*. The JDK provides `CyclicBarrier` (reusable) and `CountDownLatch` (one-shot), but building one from scratch deepens understanding of the underlying `Lock`/`Condition` mechanism.

The key challenge is the **generation** problem: after a barrier trips, a new generation begins. Threads arriving at the new generation must not be confused with threads still in the previous generation (i.e., slow threads waking up after `signalAll`).

`AbstractQueuedSynchronizer` (AQS) is the backbone of all JDK synchronizers. Understanding how it manages a CLH queue of waiting threads, and how `acquire`/`release` map to `tryAcquire`/`tryRelease`, is essential for expert-level Java concurrency interviews.

**The broken double-checked locking analogy**: a barrier implemented with `synchronized` but checked without it exhibits the same class of race condition — the generation check and the count decrement must be atomic.

**References:** JCIP §14 (building custom synchronizers); AQS paper "The java.util.concurrent Synchronizer Framework" (Lea, 2004); JDK source for `CyclicBarrier`.

## 🎯 Task

Implement in `com.concurrency.advanced.p41`:

1. **`CustomBarrier`** — constructor takes `parties` (number of threads) and an optional `Runnable` barrier action run by the last arriving thread
2. `await()` — blocks until all parties arrive; throws `BrokenBarrierException` if the barrier is broken; returns the arrival index (0 for last thread, parties-1 for first)
3. `await(long timeout, TimeUnit)` — timed version; throws `TimeoutException` and breaks the barrier if timeout expires
4. `reset()` — resets to initial state; breaks the current generation if threads are waiting
5. `isBroken()` and `getNumberWaiting()` accessors
6. **Cyclic**: after tripping, the barrier resets automatically for the next group of `parties` threads

## 📋 Skeleton

See `src/main/java/com/concurrency/advanced/p41/`

## 💡 Hints

- Use a `ReentrantLock` + `Condition` (`lock.newCondition()`); the lock guards `count` and `generation`
- `generation` is an inner class or a simple boolean flag — when the barrier trips, flip generation so laggards from the old generation don't re-enter
- Last arriving thread: runs the barrier action (if any), then calls `condition.signalAll()` and starts a new generation
- `reset()` must break the current generation and create a new one — set `broken = true`, `signalAll`, then reinitialise

## 🧠 Interview Talking Points

- How does `CyclicBarrier` differ from `CountDownLatch`?
- What is a "broken barrier" and what causes it?
- Why must the count decrement and the generation check be done under the same lock?
- How would you implement a barrier using `Phaser`?
- What does AQS provide that makes it easier to build synchronizers?
