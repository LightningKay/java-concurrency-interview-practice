# Problem 36 — Reactive Streams Publisher/Subscriber

## 🔴 Difficulty: Expert

## 📖 Background

The **Java 9 Flow API** (`java.util.concurrent.Flow`) brings the Reactive Streams specification into the JDK. It models asynchronous data pipelines through four interfaces:

- **`Flow.Publisher<T>`** — produces items on demand; calls `Subscriber.onNext()` up to the requested count
- **`Flow.Subscriber<T>`** — consumes items; signals demand via `Subscription.request(n)`
- **`Flow.Subscription`** — the link between publisher and subscriber; controls demand and cancellation
- **`Flow.Processor<T,R>`** — both Publisher and Subscriber; transforms the stream

The critical invariant is **backpressure**: the subscriber controls flow rate by requesting exactly as many items as it can handle. Publishers must never deliver more than requested. This prevents fast producers from overwhelming slow consumers — the root cause of most streaming system failures.

Key rules from the spec:
- `request(n)` with `n ≤ 0` must signal `onError` with `IllegalArgumentException`
- `onNext` must not be called after `onError` or `onComplete`
- All signals to a given subscriber must be serialized (no concurrent `onNext` calls)
- `cancel()` must be idempotent

**References:** JDK `java.util.concurrent.Flow` Javadoc; Reactive Streams specification (reactive-streams.org); JEP 266.

## 🎯 Task

Implement the following in `com.concurrency.advanced.p36`:

1. **`SimplePublisher<T>`** — publishes a fixed list of items; supports multiple concurrent subscribers each getting their own independent subscription; respects `request(n)` demand signals; calls `onComplete` when all items are delivered
2. **`BufferingSubscriber<T>`** — collects received items in a thread-safe list; requests items in configurable batch sizes; provides `getReceivedItems()` and `isDone()` accessors
3. **`TransformProcessor<T,R>`** — applies a `Function<T,R>` transform; acts as a subscriber upstream and a publisher downstream; propagates backpressure correctly

## 📋 Skeleton

See `src/main/java/com/concurrency/advanced/p36/`

## 💡 Hints

- Use `AtomicLong` to track remaining demand per subscription; `getAndUpdate` to safely decrement
- Serialize `onNext` delivery — use a single-threaded executor or `synchronized` per subscription
- For `TransformProcessor`: wire up a subscription to the upstream publisher on `onSubscribe`, then forward demand signals upstream from your own downstream subscriber
- `cancel()` must stop delivery immediately — check a `volatile boolean cancelled` before each `onNext`

## 🧠 Interview Talking Points

- What is backpressure and why does it matter in reactive systems?
- How does `Flow.Publisher` differ from `Observable` in RxJava?
- What invariants does the Reactive Streams spec enforce on `onNext`?
- How would you implement a bounded buffer between a fast publisher and a slow subscriber?
- What happens if a subscriber never calls `request(n)`?
