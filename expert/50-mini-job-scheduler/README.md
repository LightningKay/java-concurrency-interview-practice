# Problem 50 — Mini Job Scheduler (Capstone)

## 🔴 Difficulty: Expert ⭐ Capstone

## 📖 Background

This capstone problem synthesises the techniques from the entire expert tier into a single, cohesive system: a **mini job scheduler** that accepts jobs, schedules them with cron-like timing, respects priorities and dependencies, enforces concurrency limits, and reports status.

Real-world job schedulers (Quartz, Spring Batch, Airflow) solve exactly these problems at scale. Understanding their internals — especially how they handle distributed state, failure recovery, and exactly-once execution — is essential for senior engineering roles.

**Core concepts revisited:**
- **`ScheduledThreadPoolExecutor`** — delay queue + thread pool; powers all time-based scheduling in the JDK
- **`PriorityBlockingQueue`** — priority ordering within a ready queue (Problem 38)
- **`ConcurrentHashMap`** — job registry and status tracking (Problems 39, 46)
- **`CountDownLatch` / dependency graph** — job A blocks until jobs B and C complete (Problems 21, 43)
- **`Semaphore`** — limit concurrent job executions (Problem 22)
- **`CompletableFuture`** — chaining dependent jobs asynchronously (Problem 32)
- **Rate limiting** — prevent job submission storms (Problem 44)

The scheduler must be **fault-tolerant**: a job that throws must not crash the scheduler; the exception is captured and exposed via the job's status. This mirrors the `Future.get()` / `ExecutionException` pattern.

**References:** Quartz Scheduler documentation; Spring `@Scheduled` source; JCIP §6.3 (ScheduledExecutorService); "Java Concurrency in Practice" Chapter 6–8 (task execution).

## 🎯 Task

Implement in `com.concurrency.advanced.p50`:

1. **`JobDefinition`** — immutable descriptor: job ID, name, `Callable<Object>` body, priority (int), max retry count, optional delay, optional dependencies (list of job IDs)
2. **`JobStatus`** — enum: `PENDING`, `WAITING_DEPS`, `RUNNING`, `COMPLETED`, `FAILED`, `CANCELLED`
3. **`JobResult`** — holds `Object value` on success, `Throwable exception` on failure, final `JobStatus`, and execution duration
4. **`MiniJobScheduler`** — `submit(JobDefinition)` returns a `Future<JobResult>`; `cancel(String jobId)`; `getStatus(String jobId)`; `getResult(String jobId)`; `shutdown()`
5. **Dependency resolution** — a job in `WAITING_DEPS` state becomes `PENDING` only when all declared dependencies have `COMPLETED`
6. **Priority execution** — among ready jobs, highest-priority runs first
7. **Retry on failure** — a failed job is resubmitted up to `maxRetry` times with exponential backoff
8. **Concurrency limit** — configurable max concurrent running jobs via `Semaphore`

## 📋 Skeleton

See `src/main/java/com/concurrency/advanced/p50/`

## 💡 Hints

- Internal structure: `ConcurrentHashMap<String, JobContext>` registry; `PriorityBlockingQueue<JobContext>` ready queue; `ScheduledExecutorService` for delay and retry scheduling; `ExecutorService` worker pool
- `JobContext` holds the `JobDefinition`, current `JobStatus` (volatile), result, and a `CompletableFuture<JobResult>` returned to the caller
- Dependency tracking: when a job completes, iterate all waiting jobs and check if their dependencies are now all `COMPLETED`; move to ready queue if so
- Retry: on failure, schedule a re-run via `scheduledExecutor.schedule(() -> readyQueue.offer(ctx), backoffMs, MILLISECONDS)` where `backoffMs = baseMs * 2^attemptNumber`
- `shutdown()`: `scheduledExecutor.shutdown()` + `workerPool.shutdown()` + `awaitTermination`

## 🧠 Interview Talking Points

- How does `ScheduledThreadPoolExecutor` differ from a plain `ThreadPoolExecutor`?
- How would you implement exactly-once job execution in a distributed system?
- What is the difference between job scheduling and job orchestration?
- How would you persist job state so the scheduler survives a JVM restart?
- What happens to running jobs when `shutdown()` is called — how do you decide between `shutdown()` and `shutdownNow()`?
- How would you add observability (metrics, distributed tracing) to this scheduler?
