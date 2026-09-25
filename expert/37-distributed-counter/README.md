# Problem 37 — Distributed Counter (Striped Counter)

## 🔴 Difficulty: Expert

## 📖 Background

A naive shared `AtomicLong` counter becomes a hot spot under high write contention: every increment does a CAS on the same cache line, causing thundering-herd cache invalidations across CPU cores.

**`LongAdder`** (Java 8) solves this with *striped cells* — each thread tends to update its own `Cell` (a padded `long`), and the true sum is computed lazily by adding all cells together. This trades `sum()` accuracy for dramatically lower write latency and throughput.

The same pattern generalises: a **Striped Counter** shards state across `N` stripes (typically a power of two ≥ number of cores). Each stripe is cache-line padded to avoid *false sharing* — two counters on the same 64-byte cache line would bounce between cores even though they're logically independent.

`@Contended` (JDK internal) or manual 64-byte padding achieves this:

```java
// Manual padding: 7 longs of padding on each side of the value
long p1,p2,p3,p4,p5,p6,p7;
volatile long value;
long q1,q2,q3,q4,q5,q6,q7;
```

**References:** `java.util.concurrent.atomic.LongAdder` source; Cliff Click's "Non-blocking hashtable" (SPAA 2007); JEP 189 (Fences).

## 🎯 Task

Implement in `com.concurrency.advanced.p37`:

1. **`StripedCounter`** — shards across `N` padded stripes; `increment(long delta)` selects a stripe via `Thread.currentThread().getId() % N`; `sum()` returns the sum across all stripes; `reset()` zeroes all stripes atomically (best-effort is acceptable)
2. **`MetricsCollector`** — wraps multiple `StripedCounter` instances (requests, errors, bytes); exposes `recordRequest()`, `recordError()`, `recordBytes(long)`, and `getSnapshot()` returning an immutable metrics view
3. Demonstrate that `StripedCounter` outperforms `AtomicLong` under 16+ concurrent writers in the provided benchmark test

## 📋 Skeleton

See `src/main/java/com/concurrency/advanced/p37/`

## 💡 Hints

- Number of stripes should be a power of two so you can use `& (N-1)` instead of `%` for stripe selection
- Pad each stripe to 64 bytes (8 longs = 64 bytes); put the `AtomicLong` value in the middle
- `sum()` iterates all stripes — this is not strongly consistent; document that callers may see a slightly stale value
- Use `VarHandle` or `Unsafe` for acquire/release semantics if you need stronger guarantees than `volatile`

## 🧠 Interview Talking Points

- What is false sharing and how does padding prevent it?
- Why does `LongAdder.sum()` not return a strongly consistent value?
- When would you prefer `LongAdder` over `AtomicLong`?
- How does stripe count affect the trade-off between write throughput and `sum()` cost?
- How would you implement a striped `LongAccumulator` for operations other than addition?
