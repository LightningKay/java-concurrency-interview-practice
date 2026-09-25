# Hints — Problem 55: Scoped Values

## Level 1 — Nudge
`ScopedValue.newInstance()` creates a *key* — like a `ThreadLocal` field but immutable. Bind it with `ScopedValue.where(KEY, value).run(task)` or `.call(task)`. Inside the scope, `KEY.get()` returns the value. Outside the scope, `KEY.isBound()` is false.

---

## Level 2 — Direction

**Declarations**:
```java
public static final ScopedValue<String> USER_ID  = ScopedValue.newInstance();
public static final ScopedValue<String> TRACE_ID = ScopedValue.newInstance();
```

**`runWithContext`**:
```java
return ScopedValue.where(USER_ID, userId)
                  .where(TRACE_ID, traceId)
                  .call(task);
```

**`getCurrentUserId`**:
```java
return USER_ID.isBound() ? USER_ID.get() : "anonymous";
```

**`readUserIdFromChildThread`**:
```java
String[] result = new String[1];
Thread child = Thread.ofVirtual().start(() -> result[0] = USER_ID.get());
child.join();
return result[0];
```
Child virtual threads automatically inherit the parent scope's ScopedValue bindings.

---

## Level 3 — Almost there

| Symptom | Likely cause |
|---|---|
| Compilation error on `ScopedValue` | Missing `--enable-preview` — check parent pom.xml |
| `getCurrentUserId` throws `NoSuchElementException` | Calling `USER_ID.get()` without checking `isBound()` first |
| Child thread sees `null` | Not using `Thread.ofVirtual()` — platform threads created via `new Thread()` may not inherit scoped values |
| Concurrent scopes interfere | Using a `static` field mutable from outside the scope — scoped values are per-binding, not global |
