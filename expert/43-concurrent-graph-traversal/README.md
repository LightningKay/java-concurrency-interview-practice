# Problem 43 — Concurrent Graph Traversal

## 🔴 Difficulty: Expert

## 📖 Background

Parallel graph traversal applies BFS or DFS across multiple threads simultaneously to reduce wall-clock time on large graphs. The core challenge is **visited-set correctness**: if two threads simultaneously discover the same unvisited node, both may process it — producing duplicates or inconsistent results.

**Parallel BFS** is the natural fit: process all nodes at the current BFS level in parallel (using a thread pool), collect their unvisited neighbours, deduplicate, and proceed to the next level. This level-synchronised approach uses a barrier between levels.

**Concurrent DFS** is harder — paths branch but the visited set is global. Work-stealing (ForkJoin) maps well: each thread pops a node, processes it, and submits its unvisited neighbours as new tasks.

Key data structures:
- `ConcurrentHashMap.newKeySet()` — thread-safe visited set; `add()` returns `false` if already present, which is exactly the "already visited" check
- `ConcurrentLinkedQueue` — work queue for parallel BFS frontier
- `ForkJoinPool` with `RecursiveAction` — natural for DFS subtree parallelism

Termination detection is the hard part: workers must know when all nodes are processed with no more work pending. A `Phaser` or `CountDownLatch` per level (for BFS) or `ForkJoinPool.invoke()` (for DFS) handle this.

**References:** "Parallel Graph Algorithms" survey (Lumsdaine et al., 2007); `ForkJoinPool` Javadoc; Galois system paper.

## 🎯 Task

Implement in `com.concurrency.advanced.p43`:

1. **`Graph`** — adjacency list representation; `addEdge(int from, int to)`; supports both directed and undirected
2. **`ParallelBFS`** — level-synchronised BFS using a `ForkJoinPool` or `ExecutorService`; returns BFS level assignments for all reachable nodes; correct for disconnected graphs
3. **`ParallelDFS`** — post-order traversal using `ForkJoinPool`; collects visited nodes in order; handles cycles without revisiting
4. **`ConcurrentShortestPath`** — finds the shortest path between two nodes using parallel BFS; returns the path as a node list

## 📋 Skeleton

See `src/main/java/com/concurrency/advanced/p43/`

## 💡 Hints

- Visited set: `Set<Integer> visited = ConcurrentHashMap.newKeySet()`; `if (visited.add(node))` is your atomic "not yet visited" check
- Parallel BFS: `currentLevel.parallelStream().forEach(node -> ...)` or submit each node as a task; collect next-level nodes in a `ConcurrentLinkedQueue`; await all tasks before moving to next level
- For shortest path: record each node's parent using `ConcurrentHashMap<Integer, Integer>`; backtrace from target to source to reconstruct path
- Use `ForkJoinPool.commonPool()` for simplicity, or construct a custom pool to control parallelism

## 🧠 Interview Talking Points

- Why is DFS harder to parallelise than BFS?
- How do you detect termination in a parallel graph traversal?
- What is the parallel time complexity of BFS on a graph with `V` nodes and `E` edges?
- How would you scale this to a graph that doesn't fit in memory on one machine?
- What is work-stealing and why does it suit graph traversal?
