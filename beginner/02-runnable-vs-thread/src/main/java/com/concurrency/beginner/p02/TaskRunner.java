package com.concurrency.beginner.p02;

import java.util.ArrayList;
import java.util.List;

/**
 * Problem 02 - Runnable vs Thread
 *
 * Implement methods that use Runnable (not Thread subclasses) to execute tasks.
 */
public class TaskRunner {

    /**
     * Runs the given task on a new thread with the specified name.
     * Blocks until the thread completes before returning.
     *
     * @param task       the task to execute
     * @param threadName name for the new thread
     */
    public void runTask(Runnable task, String threadName) throws InterruptedException {
        // TODO: create a thread with the given name, start it, then join it
        Thread newThread = new Thread(task, threadName);
        newThread.start();
        newThread.join();
    }

    /**
     * Runs all tasks in parallel, each on its own thread named "worker-0", "worker-1", etc.
     * Blocks until ALL tasks have completed before returning.
     *
     * @param tasks list of tasks to run concurrently
     */
    public void runTasksParallel(List<Runnable> tasks) throws InterruptedException {
        // TODO: start all threads, then join all threads
        List<Thread> threads = new ArrayList<>();
        for(int i = 0; i < tasks.size(); i++){
            Thread newThread = new Thread(tasks.get(i), "worker-"+ i);
            threads.add(newThread);
        }

        for(int i = 0; i < threads.size(); i++){
            threads.get(i).start();
        }

        for(int i = 0; i < threads.size(); i++){
            threads.get(i).join();
        }
    }

    /**
     * Returns a Runnable that, when run, adds `value` to `collector` exactly `times` times.
     *
     * @param collector a thread-safe list to add values into
     * @param value     the integer to add
     * @param times     how many times to add it
     * @return a Runnable implementing the described behavior
     */
    public Runnable buildCountingRunnable(List<Integer> collector, int value, int times) {
        // TODO: return a lambda or anonymous class
        System.out.println(collector.toString());
        Runnable runnable = () -> {
            for(int j = 0 ; j < times; j++) {
               collector.add(value);
            }
        };
        return runnable;
    }
}
