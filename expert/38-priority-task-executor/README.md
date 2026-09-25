# Problem 38 — Priority Task Executor

## 🔴 Difficulty: Expert

## 📖 Background

`ThreadPoolExecutor` accepts a `BlockingQueue<Runnable>` at construction time. By substituting a **`PriorityBlockingQueue`** you get priority-ordered task execution essentially for free — the queue's `poll()` always returns the highest-priority item.

But several subtleties arise:

- **`Runnable` is not `Comparable`** — you must wrap tasks in a `Comparable` carrier (`PriorityTask`) that holds the actual `Runnable` plus its priority value
- **Starvation**: low-priority tasks may wait indefinitely if high-priority tasks arrive continuously. Mitigate with *aging* — gradually boost priority of waiting tasks
- **`PriorityBlockingQueue` is unbounded** — you cannot use `CallerRunsPolicy` based on queue capacity; instead cap the number of submitted tasks explicitly
- **Cancellation**: a submitted `Future` must remove its task from the queue, not just set a cancelled flag

`ScheduledThreadPoolExecutor` uses a similar heap-based `DelayedWorkQueue` for time-ordered scheduling; the techniques transfer directly.

**References:** `java.util.concurrent.PriorityBlockingQueue`; `ThreadPoolExecutor` source; JCIP §8.3.

## 🎯 Task

Implement in `com.concurrency.advanced.p38`:

1. **`PriorityTask`** — implements `Runnable` and `Comparable<PriorityTask>`; lower integer = higher priority; stores submit timestamp for aging support
2. **`PriorityTaskExecutor`** — wraps a `ThreadPoolExecutor` backed by `PriorityBlockingQueue`; exposes `submit(Runnable, int priority)` returning a `Future<?>`; exposes `getQueueSize()` and `shutdown()`
3. **Aging support** — a background thread periodically boosts the effective priority of tasks that have waited longer than a configurable threshold
4. **Cancellation** — `Future.cancel(true)` must remove the task from the queue and prevent execution

## 📋 Skeleton

See `src/main/java/com/concurrency/advanced/p38/`

## 💡 Hints

- `PriorityBlockingQueue` requires its elements to implement `Comparable`; wrap `Runnable` in `PriorityTask` with `compareTo` based on priority then submit time
- For cancellation: override `newTaskFor` in a custom `ExecutorService` subclass so returned `Future` objects hold a reference to the `PriorityTask` for removal
- For aging: a `ScheduledExecutorService` calling `queue.forEach(t -> t.boostPriority())` every N seconds is sufficient; the queue doesn't automatically re-sort, so you may need to drain and re-add
- Document that aging is best-effort — exact ordering during the re-add window is not guaranteed

## 🧠 Interview Talking Points

- How does substituting `PriorityBlockingQueue` change `ThreadPoolExecutor` behavior?
- Why can task starvation occur and how does aging address it?
- What are the thread-safety guarantees of `PriorityBlockingQueue`?
- How would you implement a multi-level priority queue (e.g., HIGH/MEDIUM/LOW bands)?
- What is the time complexity of enqueue and dequeue in a binary heap?
