# Hints — Problem 54: Structured Concurrency

## Level 1 — Nudge
`StructuredTaskScope` is the base type. Two subclasses handle the common policies. The scope must be in a `try-with-resources` block. Inside, call `scope.fork(callable)` for each task — this returns a `Subtask<T>` handle. After forking, call `scope.join()` (blocks until all complete or scope shuts down), then read results via `subtask.get()`.

---

## Level 2 — Direction

**`fetchUserDashboard`** — ShutdownOnFailure:
```java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    var userTask   = scope.fork(fetchUser::get);
    var ordersTask = scope.fork(fetchOrders::get);
    scope.join().throwIfFailed();
    return new UserDashboard(userTask.get(), ordersTask.get());
}
```

**`fetchFirstAvailable`** — ShutdownOnSuccess:
```java
try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
    sources.forEach(s -> scope.fork(s::get));
    scope.join();
    return scope.result();  // throws if all failed
}
```

**`withTimeout`**:
```java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    var sub = scope.fork(task);
    scope.joinUntil(Instant.now().plus(timeout));  // throws TimeoutException if exceeded
    scope.throwIfFailed();
    return sub.get();
}
```

---

## Level 3 — Almost there

| Symptom | Likely cause |
|---|---|
| Compilation error on `StructuredTaskScope` | Missing `--enable-preview` flag — check parent pom.xml is applied |
| `fetchFirstAvailable` waits for all sources | Using `ShutdownOnFailure` instead of `ShutdownOnSuccess` |
| `withTimeout` never throws `TimeoutException` | Using `scope.join()` (waits forever) instead of `scope.joinUntil(Instant)` |
| Sibling not cancelled when one fails | Using `CompletableFuture` instead of `StructuredTaskScope` — CF does not cancel siblings |
