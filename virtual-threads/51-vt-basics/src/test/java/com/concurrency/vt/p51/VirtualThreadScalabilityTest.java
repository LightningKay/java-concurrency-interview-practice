package com.concurrency.vt.p51;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Timeout;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(value = 30, unit = TimeUnit.SECONDS)
class VirtualThreadScalabilityTest {

    @Test void oneHundredThousandVirtualThreadsComplete() throws InterruptedException {
        VirtualThreadScalability vts = new VirtualThreadScalability();
        int completed = vts.launchAndWait(100_000, 100);
        assertEquals(100_000, completed, "All 100,000 virtual threads must complete");
    }

    @Test void tenThousandVirtualThreadsCompleteQuickly() throws InterruptedException {
        VirtualThreadScalability vts = new VirtualThreadScalability();
        long start = System.currentTimeMillis();
        int completed = vts.launchAndWait(10_000, 50);
        long elapsed = System.currentTimeMillis() - start;
        assertEquals(10_000, completed);
        // 10,000 × 50ms sequentially = 500 seconds; virtual threads do it in < 5s
        assertTrue(elapsed < 5_000,
            "10,000 virtual threads (50ms sleep each) must finish in < 5s, took " + elapsed + "ms");
    }
}
