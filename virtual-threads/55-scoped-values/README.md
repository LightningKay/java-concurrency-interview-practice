# Problem 55 — Scoped Values

## 🔵 Difficulty: Virtual Threads (Java 21+, preview)

## 📖 Background

`ScopedValue` (JEP 446 preview Java 21; finalised Java 25 via JEP 506) is the intended replacement for `ThreadLocal` in virtual thread applications.

Key differences from `ThreadLocal`:

| | ThreadLocal | ScopedValue |
|---|---|---|
| Mutability | Mutable (set/get/remove) | Immutable within a scope |
| Lifetime | Until removed or thread dies | Automatic — ends when scope exits |
| Child threads | Requires InheritableThreadLocal | Inherited automatically |
| Memory risk | Leaks in pooled/virtual threads | No risk — scope-bound |
| Cost | One instance per thread | Shared across child scopes |

API pattern:
```java
static final ScopedValue<String> USER = ScopedValue.newInstance();
ScopedValue.where(USER, "alice").run(() -> {
    USER.get();  // "alice" — visible here and in child threads
});
// USER.isBound() == false here
```

**Requires `--enable-preview`** (already configured in the parent pom.xml).

## 🎯 Task

Implement `ScopedValueContext`:
1. Declare `USER_ID`, `TRACE_ID`, `TENANT_ID` as `ScopedValue<String>` constants
2. `runWithContext(userId, traceId, task)` — bind both values, run task, return result
3. `getCurrentUserId()` — return bound value or "anonymous"
4. `getCurrentTraceId()` — return bound value or "no-trace"
5. `readUserIdFromChildThread()` — spawn a virtual child thread, return the USER_ID it sees

## 🧠 Interview Talking Points

- Why is `ScopedValue` immutable? What bug does mutability in `ThreadLocal` cause?
- How does `ScopedValue` propagate to child threads? Why is this different from `InheritableThreadLocal`?
- What happens to the `ScopedValue` binding after the scope exits?
- When would you still use `ThreadLocal` vs `ScopedValue`?
