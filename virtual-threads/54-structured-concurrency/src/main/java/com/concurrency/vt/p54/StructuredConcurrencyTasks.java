package com.concurrency.vt.p54;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * Problem 54 — Structured Concurrency
 *
 * Structured Concurrency (JEP 453, preview in Java 21; finalised in Java 25 via JEP 505)
 * treats a group of related concurrent tasks as a single unit of work. When the scope
 * closes, all subtasks are guaranteed to be either complete or cancelled — no orphaned
 * threads are possible.
 *
 * Key types (java.util.concurrent):
 *   StructuredTaskScope<T>            — base scope
 *   StructuredTaskScope.ShutdownOnFailure — cancel all if any subtask fails
 *   StructuredTaskScope.ShutdownOnSuccess<T> — cancel all as soon as one succeeds
 *
 * NOTE: Requires --enable-preview on Java 21–24. Stable on Java 25+.
 * The parent pom.xml already passes --enable-preview to the compiler and surefire.
 *
 * IMPORTANT — API shape on Java 21 (preview):
 *   try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
 *       StructuredTaskScope.Subtask<A> a = scope.fork(callableA);
 *       StructuredTaskScope.Subtask<B> b = scope.fork(callableB);
 *       scope.join().throwIfFailed();
 *       return new Result(a.get(), b.get());
 *   }
 */
public class StructuredConcurrencyTasks {

    /**
     * Fetch user profile and orders concurrently.
     * If either fetch fails, both are cancelled and the exception propagates.
     *
     * @param fetchUser   supplier that returns the user's display name
     * @param fetchOrders supplier that returns the user's order list
     * @return UserDashboard combining both results
     * @throws Exception if either supplier throws
     *
     * TODO: Use StructuredTaskScope.ShutdownOnFailure:
     *   scope.fork(fetchUser::get) — fork user fetch
     *   scope.fork(fetchOrders::get) — fork order fetch
     *   scope.join().throwIfFailed()
     *   return new UserDashboard(userSubtask.get(), ordersSubtask.get())
     */
    public UserDashboard fetchUserDashboard(
            Supplier<String> fetchUser,
            Supplier<List<String>> fetchOrders) throws Exception {
        throw new UnsupportedOperationException("Implement fetchUserDashboard()");
    }

    /**
     * Try multiple data sources concurrently; return the first successful result.
     * All other in-flight fetches are cancelled as soon as one succeeds.
     *
     * @param sources list of suppliers; at least one must succeed
     * @return result from whichever supplier completes first
     * @throws Exception if all sources fail
     *
     * TODO: Use StructuredTaskScope.ShutdownOnSuccess<String>:
     *   fork each source
     *   scope.join()
     *   return scope.result() — throws if all failed
     */
    public String fetchFirstAvailable(List<Supplier<String>> sources) throws Exception {
        throw new UnsupportedOperationException("Implement fetchFirstAvailable()");
    }

    /**
     * Run {@code task} inside a structured scope with a deadline.
     * If the task does not complete within {@code timeout}, it is cancelled
     * and a TimeoutException is thrown.
     *
     * TODO: Use StructuredTaskScope.ShutdownOnFailure:
     *   scope.fork(task)
     *   scope.joinUntil(Instant.now().plus(timeout))  → throws TimeoutException if exceeded
     *   scope.throwIfFailed()
     *   return subtask.get()
     */
    public <T> T withTimeout(Callable<T> task, Duration timeout) throws Exception {
        throw new UnsupportedOperationException("Implement withTimeout()");
    }

    // ── Supporting record ────────────────────────────────────────────────────
    public record UserDashboard(String userName, List<String> orders) {}
}
