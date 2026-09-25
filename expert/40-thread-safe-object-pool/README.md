# Problem 40 — Thread-Safe Object Pool

## 🔴 Difficulty: Expert

## 📖 Background

An **object pool** recycles expensive-to-create objects (DB connections, SSL sessions, parsers, byte buffers) rather than creating and destroying them per-use. The pool maintains a collection of idle objects and loans them out to callers who return them when done.

Key design concerns:

- **Borrow / return semantics**: `borrow()` either returns an idle object or creates a new one (up to max pool size); `return` places the object back in the idle collection
- **Max pool size enforcement**: if all objects are borrowed and the pool is at capacity, callers block or receive an exception depending on the configured timeout
- **Object validation**: objects may become invalid (stale DB connections, closed sockets); the pool must validate before lending and discard invalid objects
- **`AutoCloseable` integration**: wrapping borrowed objects in a `PooledResource<T>` that implements `AutoCloseable` enables try-with-resources, guaranteeing return even on exceptions
- **Resource leak detection**: track which threads hold which objects; log or alert on objects not returned within a threshold

JDBC connection pools (HikariCP, c3p0) and Netty's `ByteBufPool` are real-world examples.

**References:** JCIP §12.1 (resource management); HikariCP source; Apache Commons Pool.

## 🎯 Task

Implement in `com.concurrency.advanced.p40`:

1. **`ObjectPool<T>`** — configurable min/max pool size; `borrow(long timeout, TimeUnit)` blocks up to timeout; `returnObject(T)` returns to idle queue; validates objects before lending (via a `Predicate<T>` validator)
2. **`PooledResource<T>`** — `AutoCloseable` wrapper; `get()` returns the underlying object; `close()` calls `pool.returnObject()`; calling `close()` twice must be idempotent
3. Pool lifecycle: `initialize()` pre-warms to min size; `shutdown()` closes all idle and borrowed objects (via a `Consumer<T>` destroyer)
4. Metrics: `idleCount()`, `borrowedCount()`, `totalCreated()`

## 📋 Skeleton

See `src/main/java/com/concurrency/advanced/p40/`

## 💡 Hints

- Use `LinkedBlockingDeque<T>` as the idle queue — `pollFirst(timeout, unit)` for borrow, `offerFirst` for return
- Track borrowed objects in a `ConcurrentHashMap<T, Thread>` (for leak detection)
- Validate on borrow: `if (!validator.test(obj)) { destroy(obj); try again; }`
- `PooledResource.close()` should use `AtomicBoolean returned` to guard against double-return

## 🧠 Interview Talking Points

- Why use a pool instead of always creating new objects?
- What happens if a caller never returns a borrowed object?
- How would you implement a pool that grows and shrinks dynamically?
- What is the difference between a blocking pool and a non-blocking pool?
- How do connection pools like HikariCP handle connection validation?
