package com.concurrency.beginner.p01;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(value = 5, unit = TimeUnit.SECONDS)
class PrintingThreadTest {

    @Test
    void threadPrintsMessageExactNumberOfTimes() throws InterruptedException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        try {
            PrintingThread t = new PrintingThread("test-printer", "hello", 5);
            t.start();
            t.join();

            String[] lines = out.toString().trim().split(System.lineSeparator());
            assertEquals(5, lines.length, "Should print exactly 5 lines");
            for (String line : lines) {
                assertEquals("hello", line.trim(), "Each line should contain the message");
            }
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void threadRunsOnSeparateThread() throws InterruptedException {
        PrintingThread t = new PrintingThread("my-thread", "x", 1);
        t.start();
        t.join();

        assertNotNull(t.executingThreadName, "executingThreadName should be set after joining");
        assertEquals("my-thread", t.executingThreadName,
                "Task should run on the named thread, not the main thread");
        assertNotEquals(Thread.currentThread().getName(), t.executingThreadName,
                "Task must NOT run on the calling (main) thread");
    }

    @Test
    void threadNameIsSetCorrectly() {
        PrintingThread t = new PrintingThread("worker-1", "msg", 1);
        assertEquals("worker-1", t.getName(), "Thread name should match constructor argument");
    }

    @Test
    void threadPrintsZeroTimesWhenRepeatCountIsZero() throws InterruptedException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(out));

        try {
            PrintingThread t = new PrintingThread("zero-thread", "should-not-print", 0);
            t.start();
            t.join();
            assertEquals("", out.toString().trim(), "Nothing should be printed for repeatCount=0");
            assertNotNull(t.executingThreadName,
                    "run() must execute and record the thread name even when repeatCount=0");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    void multipleThreadsRunConcurrently() throws InterruptedException {
        // This test ensures start() is used (not run()), which would block
        PrintingThread t1 = new PrintingThread("t1", "A", 100);
        PrintingThread t2 = new PrintingThread("t2", "B", 100);

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertTrue(t1.getState() == Thread.State.TERMINATED);
        assertTrue(t2.getState() == Thread.State.TERMINATED);
        assertNotNull(t1.executingThreadName, "t1 must run() successfully and record its thread name");
        assertNotNull(t2.executingThreadName, "t2 must run() successfully and record its thread name");
    }
}
