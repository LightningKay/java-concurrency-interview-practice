# Problem 46 — Concurrent Trie

## 🔴 Difficulty: Expert

## 📖 Background

A **trie** (prefix tree) stores strings by their character-by-character prefixes. Each node represents a prefix; edges are labeled by characters; nodes marked as terminal represent complete strings. Tries support O(L) insert, lookup, and delete (where L is the string length) and efficient prefix queries — "all strings starting with `foo`".

Making a trie concurrent requires careful thought about **structural modifications**: inserting a new word may create multiple new nodes along a path, and a concurrent delete may remove nodes that another thread's insert is still traversing.

**Approaches:**
- **Coarse-grained lock**: `synchronized` on the trie root. Simple, correct, poor concurrency.
- **Lock per node** (lock striping): acquire locks top-down; prevents most conflicts but risks deadlock if not ordered correctly.
- **Lock-free with `AtomicReference`**: each node's children map is an `AtomicReference<ChildrenMap>`; structural changes use CAS. This is the basis of the CTrie (Concurrent Hash Array Mapped Trie) from Bagwell & Rompf (2012).
- **`ConcurrentHashMap` children**: store children in a `ConcurrentHashMap<Character, TrieNode>` per node. Simpler than full lock-free; concurrent inserts into different subtrees don't conflict.

**References:** "Concurrent Tries with Efficient Non-Blocking Snapshots" (Prokopec et al., 2012); JDK `ConcurrentSkipListMap` Javadoc (uses similar key ordering ideas); `java.util.concurrent.ConcurrentHashMap` source.

## 🎯 Task

Implement in `com.concurrency.advanced.p46`:

1. **`ConcurrentTrie`** — `insert(String word)`; `search(String word)` returns `true` if the exact word was inserted; `startsWith(String prefix)` returns `true` if any inserted word starts with that prefix; `delete(String word)` removes a word (and cleans up now-unused nodes)
2. Thread-safe for concurrent inserts, searches, and deletes from multiple threads
3. **`getWordsWithPrefix(String prefix)`** — returns all inserted words with that prefix (snapshot semantics acceptable)
4. Support case-sensitive ASCII strings of arbitrary length

## 📋 Skeleton

See `src/main/java/com/concurrency/advanced/p46/`

## 💡 Hints

- Each `TrieNode` holds `ConcurrentHashMap<Character, TrieNode> children` and `volatile boolean isEndOfWord`
- `insert`: for each character, `children.computeIfAbsent(c, k -> new TrieNode())` — atomically creates missing nodes; mark last node's `isEndOfWord = true`
- `delete`: tricky — mark `isEndOfWord = false` first, then clean up leaf nodes bottom-up; use a stack of visited nodes during the descent
- `getWordsWithPrefix`: traverse to the prefix node, then DFS from there collecting all terminal nodes — snapshot the children map at each level

## 🧠 Interview Talking Points

- How does a trie compare to a `HashMap<String, V>` for prefix queries?
- Why is `computeIfAbsent` the right primitive for concurrent trie insertion?
- What race condition occurs during concurrent delete if you remove nodes top-down?
- How does the CTrie achieve lock-free snapshots?
- What is the memory overhead of a trie compared to a sorted array of strings?
