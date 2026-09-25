package com.concurrency.vt.p58;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Timeout;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

/** Baseline tests for LegacyOrderService — these must still pass as-is. */
@Timeout(value = 15, unit = TimeUnit.SECONDS)
class LegacyOrderServiceTest {

    private LegacyOrderService svc;

    @BeforeEach void setUp()    { svc = new LegacyOrderService(10); }
    @AfterEach  void tearDown() throws InterruptedException { svc.shutdown(); }

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
        Future<OrderResult> f = svc.submitOrder("ord-99", "bob", 250.0);
        f.get(5, TimeUnit.SECONDS);
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
}
