package com.concurrency.vt.p58;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Timeout;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MigratedOrderService.
 *
 * The first four tests mirror LegacyOrderServiceTest exactly — the migrated
 * service must be a drop-in replacement.
 *
 * The fifth test is the differentiator: 1,000 concurrent orders must complete
 * within 5 seconds. LegacyOrderService with poolSize=10 would take ~100 seconds
 * (1000 orders × 10ms each, serialised through the pool) and fail this test.
 * MigratedOrderService with virtual threads completes it in ~10ms + overhead.
 */
@Timeout(value = 15, unit = TimeUnit.SECONDS)
class MigratedOrderServiceTest {

    private MigratedOrderService svc;

    @BeforeEach void setUp()    { svc = new MigratedOrderService(); }
    @AfterEach  void tearDown() throws InterruptedException { svc.shutdown(); }

    // ── Compatibility tests (mirror legacy) ──────────────────────────────────

    @Test
    void singleOrderProcessedSuccessfully() throws Exception {
        Future<OrderResult> future = svc.submitOrder("o1", "alice", 100.0);
        OrderResult result = future.get(5, TimeUnit.SECONDS);
        assertTrue(result.success());
        assertEquals("o1", result.orderId());
        assertEquals("alice", result.customerId());
    }

    @Test
    void processedCountIncrements() throws Exception {
        List<Future<OrderResult>> futures = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            futures.add(svc.submitOrder("o" + i, "user" + i, i * 10.0));
        }
        for (Future<OrderResult> f : futures) f.get(5, TimeUnit.SECONDS);
        assertEquals(20, svc.getProcessedCount());
    }

    @Test
    void auditLogContainsAllOrders() throws Exception {
        svc.submitOrder("ord-99", "bob", 250.0).get(5, TimeUnit.SECONDS);
        assertTrue(svc.getAuditLog().stream().anyMatch(e -> e.contains("ord-99")));
        assertTrue(svc.getAuditLog().stream().anyMatch(e -> e.contains("bob")));
    }

    @Test
    void concurrentOrdersAllSucceed() throws Exception {
        int count = 30;
        List<Future<OrderResult>> futures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            futures.add(svc.submitOrder("ord-" + i, "customer-" + i, 50.0));
        }
        int successes = 0;
        for (Future<OrderResult> f : futures) {
            if (f.get(10, TimeUnit.SECONDS).success()) successes++;
        }
        assertEquals(count, successes);
    }

    // ── The differentiator: high-concurrency throughput ──────────────────────

    @Test
    @DisplayName("THROUGHPUT: 1,000 concurrent orders complete in < 5s (fails with legacy pool)")
    @Timeout(value = 12, unit = TimeUnit.SECONDS)
    void oneThousandConcurrentOrdersCompleteQuickly() throws Exception {
        int orderCount = 1_000;
        List<Future<OrderResult>> futures = new ArrayList<>(orderCount);

        long start = System.currentTimeMillis();
        for (int i = 0; i < orderCount; i++) {
            futures.add(svc.submitOrder("ord-" + i, "customer-" + i, (i + 1) * 10.0));
        }

        int successes = 0;
        for (Future<OrderResult> f : futures) {
            if (f.get().success()) successes++;
        }
        long elapsed = System.currentTimeMillis() - start;

        assertEquals(orderCount, successes,
            "All 1,000 orders must succeed");
        assertEquals(orderCount, svc.getProcessedCount(),
            "Processed count must equal order count");
        assertEquals(orderCount, svc.getAuditLog().size(),
            "Audit log must contain one entry per order");

        // LegacyOrderService (pool=10, 10ms/order) would take ~10s and hit the @Timeout.
        // MigratedOrderService (virtual threads) completes in << 5s.
        assertTrue(elapsed < 5_000,
            "1,000 orders (10ms simulated DB each) must complete in < 5s with virtual threads. " +
            "Took: " + elapsed + "ms. If this fails, you may still be using a fixed thread pool.");
    }

    @Test
    @DisplayName("Audit log has correct format: PROCESSED:orderId:customerId")
    void auditLogFormatIsCorrect() throws Exception {
        svc.submitOrder("x1", "charlie", 99.0).get(5, TimeUnit.SECONDS);
        assertTrue(svc.getAuditLog().stream().anyMatch(e -> e.equals("PROCESSED:x1:charlie")),
            "Audit entry must be 'PROCESSED:orderId:customerId'");
    }
}
