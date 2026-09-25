package com.concurrency.vt.p55;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Timeout;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(value = 10, unit = TimeUnit.SECONDS)
class ScopedValueContextTest {

    private final ScopedValueContext ctx = new ScopedValueContext();

    @Test
    @DisplayName("getCurrentUserId returns bound value inside runWithContext")
    void boundUserIdIsVisible() throws Exception {
        String seen = ctx.runWithContext("alice", "trace-123", () -> ctx.getCurrentUserId());
        assertEquals("alice", seen);
    }

    @Test
    @DisplayName("getCurrentTraceId returns bound value inside runWithContext")
    void boundTraceIdIsVisible() throws Exception {
        String seen = ctx.runWithContext("alice", "trace-123", () -> ctx.getCurrentTraceId());
        assertEquals("trace-123", seen);
    }

    @Test
    @DisplayName("getCurrentUserId returns 'anonymous' outside any scope")
    void defaultUserIdOutsideScope() {
        assertEquals("anonymous", ctx.getCurrentUserId());
    }

    @Test
    @DisplayName("getCurrentTraceId returns 'no-trace' outside any scope")
    void defaultTraceIdOutsideScope() {
        assertEquals("no-trace", ctx.getCurrentTraceId());
    }

    @Test
    @DisplayName("ScopedValue is not visible after the scope exits")
    void scopedValueUnboundAfterScope() throws Exception {
        ctx.runWithContext("alice", "t1", () -> null);
        // After scope exits, the value must no longer be bound
        assertEquals("anonymous", ctx.getCurrentUserId(),
            "ScopedValue must be unbound after scope exits");
    }

    @Test
    @DisplayName("Child virtual thread inherits parent's scoped values")
    void childThreadInheritsScopedValues() throws Exception {
        String[] childSaw = new String[1];
        ctx.runWithContext("bob", "trace-456", () -> {
            childSaw[0] = ctx.readUserIdFromChildThread();
            return null;
        });
        assertEquals("bob", childSaw[0],
            "Child virtual thread must see the parent scope's USER_ID");
    }

    @Test
    @DisplayName("Concurrent scopes have independent values")
    void concurrentScopesAreIsolated() throws Exception {
        AtomicReference<String> aliceResult = new AtomicReference<>();
        AtomicReference<String> bobResult   = new AtomicReference<>();

        Thread tAlice = Thread.ofVirtual().start(() -> {
            try {
                aliceResult.set(ctx.runWithContext("alice", "ta", () -> {
                    Thread.sleep(50);
                    return ctx.getCurrentUserId();
                }));
            } catch (Exception e) { throw new RuntimeException(e); }
        });

        Thread tBob = Thread.ofVirtual().start(() -> {
            try {
                bobResult.set(ctx.runWithContext("bob", "tb", () -> {
                    Thread.sleep(50);
                    return ctx.getCurrentUserId();
                }));
            } catch (Exception e) { throw new RuntimeException(e); }
        });

        tAlice.join();
        tBob.join();

        assertEquals("alice", aliceResult.get(), "Alice's thread must see 'alice'");
        assertEquals("bob",   bobResult.get(),   "Bob's thread must see 'bob'");
    }

    @Test
    @DisplayName("runWithContext returns the task's return value")
    void runWithContextReturnsTaskResult() throws Exception {
        Integer result = ctx.runWithContext("u", "t", () -> 42);
        assertEquals(42, result);
    }
}
