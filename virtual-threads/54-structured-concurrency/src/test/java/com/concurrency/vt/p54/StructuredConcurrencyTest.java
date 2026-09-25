package com.concurrency.vt.p54;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Timeout;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(value = 10, unit = TimeUnit.SECONDS)
class StructuredConcurrencyTest {

    private final StructuredConcurrencyTasks sct = new StructuredConcurrencyTasks();

    // ── fetchUserDashboard ───────────────────────────────────────────────────

    @Test
    @DisplayName("fetchUserDashboard combines both results correctly")
    void dashboardCombinesBothResults() throws Exception {
        var dashboard = sct.fetchUserDashboard(
            () -> "alice",
            () -> List.of("order-1", "order-2")
        );
        assertEquals("alice", dashboard.userName());
        assertEquals(List.of("order-1", "order-2"), dashboard.orders());
    }

    @Test
    @DisplayName("fetchUserDashboard propagates exception when user fetch fails")
    void dashboardPropagatesUserFetchFailure() {
        assertThrows(Exception.class, () -> sct.fetchUserDashboard(
            () -> { throw new RuntimeException("user service down"); },
            () -> List.of("order-1")
        ));
    }

    @Test
    @DisplayName("fetchUserDashboard cancels orders fetch when user fetch fails")
    void dashboardCancelsSiblingOnFailure() throws Exception {
        AtomicBoolean ordersFetchStarted   = new AtomicBoolean(false);
        AtomicBoolean ordersFetchCompleted = new AtomicBoolean(false);
        CountDownLatch ordersFetchBegun    = new CountDownLatch(1);

        assertThrows(Exception.class, () -> sct.fetchUserDashboard(
            () -> {
                // user fetch fails immediately
                throw new RuntimeException("fast failure");
            },
            () -> {
                ordersFetchStarted.set(true);
                ordersFetchBegun.countDown();
                try { Thread.sleep(5_000); } catch (InterruptedException e) { /* cancelled */ }
                ordersFetchCompleted.set(true);
                return List.of();
            }
        ));

        // Orders fetch may or may not have started, but must NOT complete
        assertFalse(ordersFetchCompleted.get(),
            "Orders fetch should be cancelled before completing");
    }

    // ── fetchFirstAvailable ──────────────────────────────────────────────────

    @Test
    @DisplayName("fetchFirstAvailable returns result from fastest source")
    void firstAvailableReturnsFastest() throws Exception {
        List<java.util.function.Supplier<String>> sources = List.of(
            () -> { try { Thread.sleep(500); } catch (InterruptedException e) {} return "slow"; },
            () -> "fast",
            () -> { try { Thread.sleep(300); } catch (InterruptedException e) {} return "medium"; }
        );
        String result = sct.fetchFirstAvailable(sources);
        assertEquals("fast", result, "Should return the fastest source");
    }

    @Test
    @DisplayName("fetchFirstAvailable finishes in time of fastest source")
    void firstAvailableFinishesQuickly() throws Exception {
        List<java.util.function.Supplier<String>> sources = List.of(
            () -> { try { Thread.sleep(2_000); } catch (InterruptedException e) {} return "slow"; },
            () -> "instant"
        );
        long start   = System.currentTimeMillis();
        String result = sct.fetchFirstAvailable(sources);
        long elapsed = System.currentTimeMillis() - start;
        assertEquals("instant", result);
        assertTrue(elapsed < 500, "Should complete near-instantly, took " + elapsed + "ms");
    }

    // ── withTimeout ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("withTimeout returns result when task completes in time")
    void timeoutSucceeds() throws Exception {
        String result = sct.withTimeout(() -> "done", Duration.ofSeconds(5));
        assertEquals("done", result);
    }

    @Test
    @DisplayName("withTimeout throws when task exceeds deadline")
    void timeoutThrowsOnExceedingDeadline() {
        assertThrows(Exception.class, () -> sct.withTimeout(
            () -> { Thread.sleep(5_000); return "never"; },
            Duration.ofMillis(100)
        ));
    }
}
