# Problem 47 — Two-Phase Commit Transaction Manager

## 🔴 Difficulty: Expert

## 📖 Background

**Two-Phase Commit (2PC)** is the canonical distributed protocol for achieving atomic commits across multiple participants:

- **Phase 1 (Prepare)**: the coordinator sends `PREPARE` to all participants; each participant writes its changes to a local write-ahead log (WAL) and replies `YES` (ready to commit) or `NO` (abort)
- **Phase 2 (Commit/Abort)**: if all replied `YES`, coordinator sends `COMMIT` to all; if any replied `NO` or timed out, coordinator sends `ABORT` to all

The **coordinator failure** between phases is the classic problem: participants that voted `YES` are blocked (they've locked resources) until the coordinator recovers. This is 2PC's fundamental limitation.

In a JVM context, simulating 2PC teaches **resource locking protocols**, **WAL-based durability**, and **failure handling patterns** (timeout, retry, compensation). The JTA (Java Transaction API) and XA protocol implement this at the JDBC level.

Relevant JDK APIs: `javax.transaction` (JTA), `javax.transaction.xa.XAResource`, `Executors.newScheduledThreadPool` for timeout enforcement.

**References:** "Transaction Processing: Concepts and Techniques" (Gray & Reuter); JTA specification; "Designing Data-Intensive Applications" §9 (Kleppmann).

## 🎯 Task

Implement in `com.concurrency.advanced.p47`:

1. **`TransactionCoordinator`** — manages a set of `Participant` resources; `beginTransaction()` returns a transaction ID; `prepare(txId)` sends prepare to all participants concurrently and collects votes; `commit(txId)` / `rollback(txId)` sends the decision to all
2. **`Participant`** (interface + in-memory implementation) — `prepare(txId)` locks resources and returns `Vote.YES` or `Vote.NO`; `commit(txId)` applies changes; `rollback(txId)` releases locks and discards changes
3. **Timeout handling** — if any participant doesn't respond to `prepare` within a configured timeout, coordinator decides `ABORT`
4. **Concurrent transactions** — multiple transactions may run concurrently; different transactions touching different participants must not block each other

## 📋 Skeleton

See `src/main/java/com/concurrency/advanced/p47/`

## 💡 Hints

- Send `prepare` calls concurrently using `CompletableFuture.allOf` or `ExecutorService.invokeAll`; collect votes with a timeout (`invokeAll(tasks, timeout, unit)`)
- Each `Participant` maintains a `ConcurrentHashMap<String, PendingTx>` to store prepared-but-not-yet-committed state
- Transaction ID: `UUID.randomUUID().toString()` is sufficient for simulation
- Phase 2 must be sent to all participants regardless of individual failures — commit/rollback delivery is idempotent; use retry with backoff for failed deliveries

## 🧠 Interview Talking Points

- What is the blocking problem in 2PC and how does 3PC attempt to solve it?
- What happens if the coordinator crashes between Phase 1 and Phase 2?
- How does XA differ from 2PC conceptually?
- What is a write-ahead log and why is it necessary for 2PC participants?
- How would you implement compensation (SAGA pattern) as an alternative to 2PC?
