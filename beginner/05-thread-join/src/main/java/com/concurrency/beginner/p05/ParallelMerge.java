package com.concurrency.beginner.p05;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Problem 05 - Thread Join
 *
 * Use Thread.join() to collect results from parallel computations.
 */
public class ParallelMerge {

    /**
     * Computes the sum of all elements in `array` by:
     *  - Splitting the array in half
     *  - Summing each half on a separate thread
     *  - Joining both threads
     *  - Returning the combined total
     *
     * For arrays of length 0 or 1, handle the edge case directly.
     *
     * @param array the input array (non-null)
     * @return the total sum of all elements
     */
    public long sumArray(int[] array) throws InterruptedException {
        // TODO: implement parallel sum using two threads + join
        int mid = array.length / 2;
        AtomicInteger leftSum = new AtomicInteger(0);
        AtomicInteger rightSum = new AtomicInteger(0);

        Thread threadLeft = new Thread(() -> {
            leftSum.set((IntStream.range(0, mid).map(i -> array[i]).sum()));
        });

        Thread threadRight = new Thread(() -> {
           rightSum.set((IntStream.range(mid, array.length).map(i -> array[i]).sum()));
        });

        threadLeft.start();
        threadRight.start();

        threadLeft.join();
        threadRight.join();
        return leftSum.get() + rightSum.get();
    }

    /**
     * Finds the maximum element in `array` by:
     *  - Splitting the array in half
     *  - Finding the max of each half on a separate thread
     *  - Joining both threads
     *  - Returning the overall max
     *
     * @param array non-null, non-empty array
     * @return the maximum value in the array
     */
    public int findMax(int[] array) throws InterruptedException {
        // TODO: implement parallel max using two threads + join
        int mid = array.length / 2;

        AtomicInteger leftMax = new AtomicInteger();
        AtomicInteger rightMax = new AtomicInteger();

        Thread threadLeft = new Thread(() -> {
            leftMax.set(IntStream.range(0, mid).map(i -> array[i]).max().getAsInt());
        });

        Thread threadRight = new Thread(() -> {
            rightMax.set(IntStream.range(mid, array.length).map(i -> array[i]).max().getAsInt());
        });

        threadLeft.start();
        threadRight.start();

        threadLeft.join();
        threadRight.join();

       return Math.max(leftMax.get(), rightMax.get());
    }

    /**
     * Runs `task` on a new thread and waits up to `timeoutMillis` for it to complete.
     *
     * @param task          the task to run
     * @param timeoutMillis maximum wait time in milliseconds
     * @return true if the task completed within the timeout; false if it timed out
     */
    public boolean runWithTimeout(Runnable task, long timeoutMillis) throws InterruptedException {
        // TODO: start thread, join with timeout, check isAlive()
        Thread newThread = new Thread(task);
        newThread.start();
        Thread.sleep(timeoutMillis);
        return newThread.getState().equals(Thread.State.TERMINATED);
    }
}
