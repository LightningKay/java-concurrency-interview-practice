# Problem 45 — Lock-Free Ring Buffer

## 🔴 Difficulty: Expert

## 📖 Background

A **ring buffer** (circular buffer) is a fixed-size FIFO where the write index wraps around. It is the data structure of choice for high-performance inter-thread communication: no allocation after initialization, cache-friendly sequential access, and — with the right atomic operations — no locks.

**SPSC (Single-Producer Single-Consumer)** is the simplest lock-free case: one thread writes, one reads. The producer advances the write index; the consumer advances the read index. Because only one thread writes and one reads, `volatile` on each index is sufficient — no CAS needed.

**MPSC (Multi-Producer Single-Consumer)** is harder: multiple producers race to claim a slot. Each producer CAS-advances the write index to claim a slot, then writes its item. The consumer must wait until all producers before it have finished writing (the slot's written flag must be set).

**LMAX Disruptor** — the canonical production ring buffer — uses sequence numbers, memory barriers, and careful cache-line padding to achieve ~100M ops/sec on a single ring. The "mechanical sympathy" principle: design data structures around how CPU caches work.

The tricky correctness point: the consumer must never overtake the producer, and the producer must never overwrite unconsumed data. These are the two *ring buffer invariants*.

**References:** LMAX Disruptor technical paper (Thompson et al., 2011); Martin Thompson's "Mechanical Sympathy" blog; `java.util.concurrent.ArrayBlockingQueue` source for the locked reference.

## 🎯 Task

Implement in `com.concurrency.advanced.p45`:

1. **`LockFreeRingBuffer<T>`** — SPSC ring buffer; power-of-two capacity; `offer(T)` returns `false` if full; `poll()` returns `null` if empty; no locks, only `volatile` and `VarHandle`/`AtomicLong`
2. **`MultiProducerRingBuffer<T>`** — MPSC ring buffer; multiple threads may call `offer` concurrently; single consumer calls `poll`; each slot has a `volatile` written flag
3. Both implementations must be free of ABA problems and safe under Java Memory Model
4. Demonstrate correctness with a multi-threaded producer/consumer test that verifies all items are received exactly once

## 📋 Skeleton

See `src/main/java/com/concurrency/advanced/p45/`

## 💡 Hints

- SPSC: `head` and `tail` as `volatile long`; mask with `(capacity - 1)` for index wrap; producer writes item then `tail++`; consumer reads item then `head++`
- Pad `head` and `tail` to separate cache lines to prevent false sharing — 64-byte padding (7 longs each side)
- MPSC: CAS on `tail` to claim slot index; then store item; set `written[slot]` to `true`; consumer spins until `written[readHead]` is `true`
- Use `VarHandle` with `setRelease` / `getAcquire` semantics for proper JMM ordering without full `volatile` overhead

## 🧠 Interview Talking Points

- What are the two invariants of a ring buffer?
- Why is SPSC easier to implement lock-free than MPMC?
- What is false sharing and how does padding prevent it?
- How does the LMAX Disruptor achieve its throughput numbers?
- What is the ABA problem and does it apply to ring buffers?
