# Problem 42 — Async Event Bus

## 🔴 Difficulty: Expert

## 📖 Background

An **event bus** decouples producers from consumers: producers publish typed events without knowing which consumers exist; consumers subscribe to specific event types and are notified asynchronously. This is the publish-subscribe (pub-sub) pattern at the JVM level.

Design dimensions:
- **Synchronous vs asynchronous delivery**: sync buses call subscribers inline on the publisher's thread (simple, low-latency, risk of slow subscriber blocking publisher); async buses dispatch to a thread pool
- **Type-based routing**: `Class<T>` as the routing key; a subscriber registered for `AnimalEvent` should also receive `DogEvent extends AnimalEvent` (type hierarchy routing)
- **Error isolation**: one subscriber throwing must not prevent other subscribers from receiving the event
- **Ordering guarantees**: within a single subscriber, events must be delivered in publish order; across subscribers, no ordering guarantee
- **Unsubscribe**: subscriptions must be cancellable; no memory leaks from zombie subscribers
- **Dead events**: events with no subscribers are typically re-published as a `DeadEvent` wrapper

Guava's `EventBus` / `AsyncEventBus` is the canonical reference implementation.

**References:** Guava `EventBus` source and javadoc; POSA Vol 1 (Event-based implicit invocation); JCIP §7.1 (task cancellation).

## 🎯 Task

Implement in `com.concurrency.advanced.p42`:

1. **`AsyncEventBus`** — `publish(Object event)` dispatches to all registered subscribers of that event type on a shared `ExecutorService`; `subscribe(Class<T>, Consumer<T>)` registers a subscriber and returns a handle; `unsubscribe(handle)` removes it
2. **Type hierarchy routing** — a subscriber for `Animal.class` receives `Dog` events (use `Class.isAssignableFrom`)
3. **Error isolation** — catch and log subscriber exceptions; continue delivery to remaining subscribers
4. **Dead event** — if no subscriber handles an event type, publish a `DeadEvent(originalEvent)` (prevent infinite recursion if `DeadEvent` itself has no subscriber)
5. **Graceful shutdown** — `shutdown()` stops accepting new events and awaits in-flight delivery

## 📋 Skeleton

See `src/main/java/com/concurrency/advanced/p42/`

## 💡 Hints

- Use `ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<Subscriber>>` — `CopyOnWriteArrayList` allows iteration during concurrent subscribe/unsubscribe without locking
- For type hierarchy: walk `event.getClass()` and all its superclasses/interfaces to find matching subscribers
- Subscription handle: return an opaque token that `unsubscribe` can look up to remove the `Consumer` from the list
- Executor: `Executors.newCachedThreadPool()` or a bounded pool; wrap each subscriber call in try-catch

## 🧠 Interview Talking Points

- What is the difference between pub-sub and observer pattern?
- How do you prevent a slow subscriber from blocking the event bus?
- What are the ordering guarantees of your implementation?
- How does Guava `EventBus` handle `@Subscribe` annotation scanning?
- What is a dead event and why is it useful?
