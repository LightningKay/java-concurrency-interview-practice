package com.concurrency.vt.p55;

import java.util.concurrent.Callable;

/**
 * Problem 55 — Scoped Values
 *
 * ScopedValue (JEP 446 preview Java 21; finalised Java 25 via JEP 506) is the
 * recommended replacement for ThreadLocal when working with virtual threads.
 *
 * Key differences vs ThreadLocal:
 *   - Immutable within a scope — cannot be changed after binding
 *   - Scope-bound — value is automatically unbound when the scope exits
 *   - Inherited by child scopes (StructuredTaskScope.fork) without copying
 *   - No memory leak risk — no pooled threads, no forgotten remove()
 *
 * API pattern:
 *   static final ScopedValue<String> USER = ScopedValue.newInstance();
 *
 *   ScopedValue.where(USER, "alice").run(() -> {
 *       System.out.println(USER.get()); // "alice"
 *   });
 *   // USER.get() here throws NoSuchElementException — out of scope
 *
 * NOTE: Requires --enable-preview on Java 21–24. Stable on Java 25+.
 */
public class ScopedValueContext {

    /**
     * Scoped values for per-request context.
     * Declare these as public static final — they are keys, not values.
     *
     * TODO:
     *   public static final ScopedValue<String> USER_ID    = ScopedValue.newInstance();
     *   public static final ScopedValue<String> TRACE_ID   = ScopedValue.newInstance();
     *   public static final ScopedValue<String> TENANT_ID  = ScopedValue.newInstance();
     */

    /**
     * Run {@code task} with USER_ID and TRACE_ID bound to the given values.
     * The task can call ScopedValueContext.USER_ID.get() and .TRACE_ID.get().
     *
     * TODO:
     *   ScopedValue.where(USER_ID, userId)
     *              .where(TRACE_ID, traceId)
     *              .call(task);
     */
    public <T> T runWithContext(String userId, String traceId, Callable<T> task) throws Exception {
        throw new UnsupportedOperationException("Implement runWithContext()");
    }

    /**
     * Return the current USER_ID scoped value, or "anonymous" if not bound.
     *
     * TODO: ScopedValue.isBound() to check, then .get()
     */
    public String getCurrentUserId() {
        throw new UnsupportedOperationException("Implement getCurrentUserId()");
    }

    /**
     * Return the current TRACE_ID scoped value, or "no-trace" if not bound.
     */
    public String getCurrentTraceId() {
        throw new UnsupportedOperationException("Implement getCurrentTraceId()");
    }

    /**
     * Demonstrate that scoped values are inherited by child virtual threads.
     *
     * Inside a runWithContext() call, spawn a virtual thread and verify it can
     * read the same USER_ID and TRACE_ID without being explicitly passed them.
     *
     * @return the USER_ID as seen by the child virtual thread
     *
     * TODO:
     *   String[] childSaw = new String[1];
     *   Thread child = Thread.ofVirtual().start(() -> childSaw[0] = USER_ID.get());
     *   child.join();
     *   return childSaw[0];
     */
    public String readUserIdFromChildThread() throws InterruptedException {
        throw new UnsupportedOperationException("Implement readUserIdFromChildThread()");
    }
}
