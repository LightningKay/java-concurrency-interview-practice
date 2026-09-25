package com.concurrency.vt.p56;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Timeout;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(value = 30, unit = TimeUnit.SECONDS)
class SemaphoreThrottlingTest {

    private static final int POOL_CAP      = 20;
    private static final int TASKS         = 200;
    private static final long WORK_MS      = 50;

    @Test
    @DisplayName("Unthrottled: connections exhausted — many failures expected")
    void unthrottledCausesRejections() throws InterruptedException {
        SemaphoreThrottling.SimulatedDbPool pool = new SemaphoreThrottling.SimulatedDbPool(POOL_CAP);
        SemaphoreThrottling st = new SemaphoreThrottling();

        SemaphoreThrottling.Result result = st.runUnthrottled(pool, TASKS, WORK_MS);

        // Without throttling, many tasks will try to exceed pool capacity
        assertTrue(result.failures() > 0,
            "Unthrottled run must produce failures when tasks > pool capacity. " +
            "Got successes=" + result.successes() + " failures=" + result.failures());
        assertEquals(TASKS, result.successes() + result.failures(),
            "successes + failures must equal total tasks");
    }

    @Test
    @DisplayName("Throttled: zero connection rejections")
    void throttledProducesZeroFailures() throws InterruptedException {
        SemaphoreThrottling.SimulatedDbPool pool = new SemaphoreThrottling.SimulatedDbPool(POOL_CAP);
        SemaphoreThrottling st = new SemaphoreThrottling();

        SemaphoreThrottling.Result result = st.runThrottled(pool, TASKS, POOL_CAP, WORK_MS);

        assertEquals(0, result.failures(),
            "Throttled run must have zero failures — semaphore caps at pool capacity");
        assertEquals(TASKS, result.successes(),
            "All " + TASKS + " tasks must succeed");
    }

    @Test
    @DisplayName("Throttled: all tasks complete even with many more tasks than pool cap")
    void throttledCompletesAllTasks() throws InterruptedException {
        int bigTaskCount = 500;
        SemaphoreThrottling.SimulatedDbPool pool = new SemaphoreThrottling.SimulatedDbPool(POOL_CAP);
        SemaphoreThrottling st = new SemaphoreThrottling();

        SemaphoreThrottling.Result result = st.runThrottled(pool, bigTaskCount, POOL_CAP, 20);

        assertEquals(bigTaskCount, result.successes(),
            "All " + bigTaskCount + " tasks must succeed when throttled correctly");
        assertEquals(0, result.failures());
    }

    @Test
    @DisplayName("Throttled run completes in reasonable time — virtual threads wait cheaply")
    void throttledCompletesInReasonableTime() throws InterruptedException {
        SemaphoreThrottling.SimulatedDbPool pool = new SemaphoreThrottling.SimulatedDbPool(10);
        SemaphoreThrottling st = new SemaphoreThrottling();

        // 100 tasks, pool=10, work=50ms
        // Min theoretical time: ceil(100/10) * 50ms = 500ms
        // Allow 3x overhead = 1500ms
        long start = System.currentTimeMillis();
        SemaphoreThrottling.Result result = st.runThrottled(pool, 100, 10, 50);
        long elapsed = System.currentTimeMillis() - start;

        assertEquals(0, result.failures());
        assertTrue(elapsed < 1_500,
            "100 tasks throttled to 10 concurrent (50ms work each) should finish < 1500ms, took " + elapsed + "ms");
    }

    @Test
    @DisplayName("Semaphore limit is respected — never more than maxConcurrent hold pool at once")
    void semaphoreCapIsNeverExceeded() throws InterruptedException {
        // Pool cap = 5; semaphore = 5; this should never cause a rejection
        SemaphoreThrottling.SimulatedDbPool pool = new SemaphoreThrottling.SimulatedDbPool(5);
        SemaphoreThrottling st = new SemaphoreThrottling();

        SemaphoreThrottling.Result result = st.runThrottled(pool, 50, 5, 30);

        assertEquals(0, result.failures(),
            "Semaphore must prevent any pool exhaustion");
    }
}
