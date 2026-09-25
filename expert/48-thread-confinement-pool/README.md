# Problem 48 — Thread Confinement Pool

## 🔴 Difficulty: Expert

## 📖 Background

**Thread confinement** ensures that a mutable object is accessed by only one thread at a time — not through locks, but by design: the object never leaves the thread that owns it. This eliminates synchronization overhead entirely.

`ThreadLocal<T>` is Java's built-in confinement mechanism: each thread has its own copy of the value, initialized lazily on first access. It is used pervasively for:
- `SimpleDateFormat` (not thread-safe; one per thread via `ThreadLocal`)
- Database connections in frameworks that assign one connection per request thread
- Request-scoped context in web frameworks (Spring `RequestContextHolder`)

The **thread confinement pool** pattern extends this: instead of each thread creating its own resource independently, a pool pre-creates resources and assigns one to each thread on first use — giving deterministic resource count and enabling centralized lifecycle management.

The key challenge: `ThreadLocal` values survive thread reuse in thread pools. If a thread is returned to the pool with a "dirty" `ThreadLocal`, the next task on that thread sees stale data. Always clean up `ThreadLocal` values in a `finally` block.

**References:** JCIP §3.3 (thread confinement); JCIP §3.3.2 (ThreadLocal); Spring `NamedThreadLocal`; `java.lang.ThreadLocal` Javadoc.

## 🎯 Task

Implement in `com.concurrency.advanced.p48`:

1. **`ConfinedResourcePool<T>`** — manages a fixed set of resources; `borrow()` assigns one resource to the current thread (same thread always gets the same resource); `release()` marks the current thread's resource as available for other threads; thread-safe pool management
2. **`DateFormatterPool`** — concrete implementation confining `SimpleDateFormat` instances; `format(Date)` and `parse(String)` use the thread-confined formatter without any locking
3. Demonstrate that two threads using `DateFormatterPool` concurrently never corrupt each other's results — even though `SimpleDateFormat` itself is not thread-safe
4. Correct cleanup on thread pool reuse — test that a task following another task on the same thread does not see the previous task's confined state

## 📋 Skeleton

See `src/main/java/com/concurrency/advanced/p48/`

## 💡 Hints

- `ThreadLocal<T> local = ThreadLocal.withInitial(() -> pool.checkOut())` — but this doesn't let you return resources; use a manual `set`/`remove` pattern instead
- `borrow()`: if the thread already has a resource (`local.get() != null`), return it; otherwise `checkOut()` from the pool and `local.set(resource)` 
- `release()`: `local.remove()` + return resource to pool — always in `finally`
- For the pool itself: `ArrayBlockingQueue<T>` of available resources; `checkOut()` = `poll()` (non-blocking, pool is pre-sized)

## 🧠 Interview Talking Points

- What is thread confinement and how does it differ from synchronization?
- What happens if you forget to call `ThreadLocal.remove()` in a thread pool?
- Why is `SimpleDateFormat` not thread-safe and what are the three ways to fix it?
- How does Spring use `ThreadLocal` for request-scoped beans?
- What is the memory leak risk of `ThreadLocal` in long-lived threads?
