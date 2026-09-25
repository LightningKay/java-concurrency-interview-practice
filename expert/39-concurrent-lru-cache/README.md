# Problem 39 — Concurrent LRU Cache

## 🔴 Difficulty: Expert

## 📖 Background

An **LRU (Least Recently Used)** cache evicts the entry that was accessed least recently when capacity is exceeded. The canonical single-threaded implementation uses a `LinkedHashMap` with `accessOrder=true` and overrides `removeEldestEntry`. Making this thread-safe is non-trivial:

**Approach 1 — `ConcurrentHashMap` + doubly-linked list:** Store entries in a `ConcurrentHashMap` for O(1) lookup; maintain a doubly-linked list of entries ordered by access time. Every `get` and `put` must atomically move the accessed node to the list head. Requires a lock around list mutations, making this effectively a coarse lock over the ordering structure.

**Approach 2 — Segmented LRU:** Partition the key space into `N` segments, each an independent LRU. Reduces lock contention at the cost of slightly imprecise global LRU ordering.

**Approach 3 — `LinkedHashMap` with `ReadWriteLock`:** Wrap `LinkedHashMap(capacity, 0.75, true)` with a `ReentrantReadWriteLock`; write-lock on both `get` (because access order changes) and `put`. Simple but every access takes an exclusive lock.

Production caches (Caffeine, Guava) use **Window TinyLFU** + **lock-free frequency sketches** — far beyond this problem's scope, but worth knowing.

**References:** JCIP §11; Caffeine source (github.com/ben-manes/caffeine); "TinyLFU: A Highly Efficient Cache Admission Policy" (Einziger et al., 2017).

## 🎯 Task

Implement in `com.concurrency.advanced.p39`:

1. **`ConcurrentLRUCache<K,V>`** — fixed capacity; `get(K)` returns the value and promotes to MRU; `put(K,V)` inserts or updates and evicts LRU if over capacity; `size()` and `containsKey(K)` accessors
2. Thread-safe under concurrent reads and writes from multiple threads
3. Correct LRU eviction order — the entry accessed least recently is evicted first
4. `getHitCount()` and `getMissCount()` metrics

## 📋 Skeleton

See `src/main/java/com/concurrency/advanced/p39/`

## 💡 Hints

- Use `LinkedHashMap(capacity, 0.75f, true)` wrapped with `synchronized` or a `ReentrantReadWriteLock`; note that `get` with `accessOrder=true` modifies the map, so it requires a write lock
- Alternatively: `ConcurrentHashMap` for the data plus a separate `synchronized` doubly-linked list for LRU order — benchmark whether the added complexity buys you anything over the simple approach
- `removeEldestEntry` override: `return size() > capacity` — but this only works in a subclass where the map itself is locked
- For a lock-free stretch goal: look at `ConcurrentLinkedDeque` for the order list

## 🧠 Interview Talking Points

- Why can't you use `ConcurrentHashMap` alone to implement LRU?
- What is the time complexity of `get` and `put` in your implementation?
- How does `LinkedHashMap`'s `accessOrder` mode work internally?
- What trade-offs does segmented LRU make compared to a global LRU?
- How does Caffeine's W-TinyLFU differ from LRU in eviction policy?
