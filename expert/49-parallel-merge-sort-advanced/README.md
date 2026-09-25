# Problem 49 — Parallel Merge Sort (Advanced)

## 🔴 Difficulty: Expert

## 📖 Background

Merge sort is an ideal candidate for parallelism because its divide-and-conquer structure produces independent subproblems. The naive parallel version — fork a new thread for every recursive split — creates O(N) threads and collapses under scheduling overhead. The right approach: recurse sequentially below a **threshold** (typically 1,000–10,000 elements), and fork only above it.

**`ForkJoinPool`** and **`RecursiveAction`** / `RecursiveTask<T>` are the JDK primitives designed for this. The **work-stealing** scheduler keeps all cores busy: idle threads steal tasks from the deques of busy threads, minimising idle time without programmer-managed thread pools.

Key subtleties:
- **Merge step cannot be parallelised naively** — merging two sorted halves is sequential O(N). For large N, a **parallel merge** (splitting both halves at a common median) achieves O(N/P) merge time with P cores.
- **In-place vs auxiliary buffer** — standard merge sort uses O(N) extra space; in-place merge sort is O(1) space but O(N log²N) time.
- **Adaptive threshold** — the optimal cutoff depends on array size and available parallelism; `Runtime.getRuntime().availableProcessors()` informs the decision.

This is the same algorithm used by `Arrays.parallelSort()` in Java 8+, which applies parallel merge sort with a sequential threshold of 8,192 elements.

**References:** "Introduction to Algorithms" §27 (parallel merge sort); `java.util.Arrays.parallelSort` source; Lea's ForkJoin paper (2000); JCIP §8.

## 🎯 Task

Implement in `com.concurrency.advanced.p49`:

1. **`ParallelMergeSortAdvanced`** — `sort(int[] array)` using `ForkJoinPool`; configurable `threshold` below which sequential sort is used; configurable parallelism level
2. **`RecursiveSortTask`** — `RecursiveAction` that splits the array, forks left and right halves, joins, then merges
3. **Parallel merge** (stretch goal) — implement a parallel merge step that splits at the median element and recurses; reduces merge time from O(N) to O(N/P)
4. Demonstrate speedup on arrays of 10M+ integers compared to sequential merge sort using `System.nanoTime()` timing in the test

## 📋 Skeleton

See `src/main/java/com/concurrency/advanced/p49/`

## 💡 Hints

- Extend `RecursiveAction`; override `compute()`: if `array.length <= threshold`, sort sequentially (`Arrays.sort`); else split at midpoint, `fork` left half, `compute` right half (in-thread), `join` left half, then merge
- Call `compute()` (not `fork()`) on one half to save a thread — the current thread does one half while the fork handles the other
- For the merge step: use a temporary auxiliary array; `System.arraycopy` is faster than element-by-element copy
- Submit the root task via `ForkJoinPool.commonPool().invoke(task)` or a dedicated pool with `availableProcessors()` parallelism

## 🧠 Interview Talking Points

- Why does `ForkJoinPool` outperform `ThreadPoolExecutor` for recursive divide-and-conquer?
- What is work-stealing and how does it reduce idle time?
- Why should you call `compute()` on one subtask and `fork()/join()` on the other?
- What is the optimal threshold for switching from parallel to sequential sort?
- How does `Arrays.parallelSort` choose its algorithm for primitive vs object arrays?
