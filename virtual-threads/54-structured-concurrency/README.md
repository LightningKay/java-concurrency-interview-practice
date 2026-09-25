# Problem 54 — Structured Concurrency

## 🔵 Difficulty: Virtual Threads (Java 21+, preview)

## 📖 Background

Structured Concurrency (JEP 453, preview Java 21; finalised Java 25 via JEP 505) treats a group of concurrent subtasks as a single unit of work. When the scope exits, all subtasks are guaranteed complete or cancelled — no orphaned threads.

Two built-in scope policies:
- `ShutdownOnFailure` — if any subtask fails, cancel all others; re-throw the exception
- `ShutdownOnSuccess<T>` — as soon as one subtask succeeds, cancel all others; return its result

Why this matters vs `CompletableFuture.allOf()`:
- `allOf` does not cancel remaining tasks when one fails
- Exceptions in CompletableFuture can be lost silently
- Structured concurrency guarantees cleanup — no resource leaks

**Requires `--enable-preview`** (already configured in the parent pom.xml).

## 🎯 Task

Implement `StructuredConcurrencyTasks`:
1. `fetchUserDashboard(fetchUser, fetchOrders)` — run both concurrently via `ShutdownOnFailure`; if either fails, cancel both and propagate the exception
2. `fetchFirstAvailable(sources)` — run all sources concurrently via `ShutdownOnSuccess`; return the first result, cancel the rest
3. `withTimeout(task, duration)` — run task with a deadline; throw if exceeded

## 🧠 Interview Talking Points

- What problem does structured concurrency solve that `CompletableFuture` does not?
- What is the difference between `ShutdownOnFailure` and `ShutdownOnSuccess`?
- How does `scope.joinUntil(Instant)` implement a timeout?
- Why can't a subtask's lifetime exceed the scope's lifetime? Why is this a useful guarantee?
- What Java version finalised `StructuredTaskScope`? (Java 25, JEP 505)
