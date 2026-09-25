package com.concurrency.vt.p51;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Timeout;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(value = 10, unit = TimeUnit.SECONDS)
class VirtualThreadBasicsTest {
    private final VirtualThreadBasics vtb = new VirtualThreadBasics();

    @Test void createAndStartVirtualThreadRunsTask() throws InterruptedException {
        AtomicReference<String> result = new AtomicReference<>();
        Thread t = vtb.createAndStartVirtualThread(() -> result.set("ran"));
        t.join();
        assertEquals("ran", result.get());
    }

    @Test void createdThreadIsVirtual() throws InterruptedException {
        Thread t = vtb.createAndStartVirtualThread(() -> {});
        t.join();
        assertTrue(t.isVirtual(), "Thread must be virtual");
    }

    @Test void namedVirtualThreadHasCorrectName() throws InterruptedException {
        Thread t = vtb.createNamedVirtualThread("my-vt", () -> {});
        assertEquals("my-vt", t.getName());
        t.join();
    }

    @Test void isVirtualReturnsTrueForVirtualThread() {
        Thread vt = Thread.ofVirtual().unstarted(() -> {});
        assertTrue(vtb.isVirtualThread(vt));
    }

    @Test void isVirtualReturnsFalseForPlatformThread() {
        Thread pt = Thread.ofPlatform().unstarted(() -> {});
        assertFalse(vtb.isVirtualThread(pt));
    }

    @Test void runAllReturnsResultsInOrder() throws Exception {
        List<Callable<Integer>> tasks = List.of(() -> 1, () -> 2, () -> 3, () -> 4, () -> 5);
        List<Integer> results = vtb.runAll(tasks);
        assertEquals(List.of(1, 2, 3, 4, 5), results, "Must be in submission order");
    }

    @Test void runConcurrentCountersIsAccurate() throws InterruptedException {
        long result = vtb.runConcurrentCounters(10_000);
        assertEquals(10_000L, result, "All increments must be counted");
    }

    @Test void getExecutingThreadNameReturnsVirtualThreadName() throws InterruptedException {
        String name = vtb.getExecutingThreadName("vt-worker", () -> {});
        assertEquals("vt-worker", name);
    }
}
