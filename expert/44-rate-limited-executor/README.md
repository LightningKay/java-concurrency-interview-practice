# Problem 44 — Token Bucket Rate Limiter

## 🔴 Difficulty: Expert

## 📖 Background

A **token bucket** rate limiter allows bursts up to a configured capacity while enforcing a long-term average rate. Tokens accumulate at a fixed `rate` (tokens/second) up to a `capacity`; each request consumes one token. If no tokens are available, the caller either blocks, is rejected, or waits a computed delay.

Contrast with **leaky bucket** (output is constant regardless of input burstsiness) and **sliding window counter** (tracks request counts within a rolling window — no burst allowance).

The key implementation challenge is **token refill without a background thread**: refill lazily on each `tryAcquire` call by computing `elapsed * rate` tokens since the last refill time. This avoids scheduling overhead and is the approach used by Guava `RateLimiter`.

**Guava `RateLimiter`** is the JVM reference implementation. It uses `synchronized` methods, a `storedPermits` double, and `nextFreeTicketMicros` to model future permit availability — supporting `acquire()` that blocks until a permit is available by sleeping the computed wait time.

**References:** Guava `RateLimiter` source; "Rate Limiting" chapter in _Designing Distributed Systems_ (Burns, 2018); nginx rate limiting documentation.

## 🎯 Task

Implement in `com.concurrency.advanced.p44`:

1. **`TokenBucketRateLimiter`** — constructor takes `capacity` (max tokens) and `ratePerSecond`; `tryAcquire()` returns `true` if a token is available (non-blocking); `acquire()` blocks until a token is available; `acquire(long timeout, TimeUnit)` timed variant
2. Thread-safe under concurrent callers — multiple threads acquiring simultaneously must each get a distinct token
3. **Lazy refill** — compute tokens earned since last acquire on each call; no background refill thread required
4. **`RateLimitedExecutor`** — wraps an `ExecutorService`; `submit(Runnable)` acquires a token before dispatching the task; rejects (throws) if the limiter's timeout expires

## 📋 Skeleton

See `src/main/java/com/concurrency/advanced/p44/`

## 💡 Hints

- Store `tokens` as a `double` (fractional tokens allowed); `lastRefillNanos` as `volatile long`
- In `synchronized tryAcquire()`: `elapsed = now - lastRefillNanos; tokens = min(capacity, tokens + elapsed * rate / 1e9); if (tokens >= 1) { tokens--; return true; }`
- For `acquire()` with blocking: compute `waitNanos = (1 - tokens) / rate * 1e9`; call `LockSupport.parkNanos(waitNanos)` or `Thread.sleep`
- Use `System.nanoTime()` (monotonic) not `System.currentTimeMillis()` (wall clock, can jump)

## 🧠 Interview Talking Points

- What is the difference between token bucket and leaky bucket?
- How does Guava `RateLimiter` handle warm-up periods?
- Why use `System.nanoTime()` instead of `currentTimeMillis()` for rate limiting?
- How would you distribute a rate limiter across multiple JVMs (e.g., using Redis)?
- What happens if the system clock jumps backward?
