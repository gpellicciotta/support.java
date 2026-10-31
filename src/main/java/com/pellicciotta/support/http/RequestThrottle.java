// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

/**
 *  A simple in-memory, per-source-IP token-bucket rate limiter, meant to be plugged in as one of the first
 *  {@link HttpRequestHandler}s in a request-dispatch chain to bound how many requests/second a single client IP
 *  may make, protecting a server against a flood of requests from one source. Not a general-purpose
 *  rate-limiting framework — just enough to cover that one need.
 *  <p>
 *  Each IP gets its own bucket with capacity {@link #getMaxRequestsPerSecondPerIp()}, refilled continuously at
 *  that same rate (tokens/second), rather than reset in discrete per-second windows — so a client can't get two
 *  back-to-back bursts of the full limit merely by timing requests around a window boundary. Buckets live in a
 *  {@link ConcurrentHashMap} (no single global lock across different IPs — concurrent requests from different
 *  IPs never contend with each other), and each bucket's own state is guarded by a per-bucket {@code
 *  synchronized} block.
 *  <p>
 *  The requests/second limit is a plain constructor parameter — callers who want it configurable (e.g. via an
 *  app-specific env var) read that value themselves and pass it in.
 *  <p>
 *  <b>Memory bound:</b> a bucket for an IP that hasn't made a request in {@link #IDLE_EVICTION_MILLIS} is
 *  dropped the next time any request triggers a sweep (at most once every {@link #SWEEP_INTERVAL_MILLIS} — an
 *  opportunistic, request-triggered sweep rather than a dedicated background thread), so the map doesn't grow
 *  forever under real traffic from many distinct IPs.
 */
public final class RequestThrottle {
  private static final long IDLE_EVICTION_MILLIS = 60_000L;
  private static final long SWEEP_INTERVAL_MILLIS = 30_000L;

  public RequestThrottle(int maxRequestsPerSecondPerIp) {
    this(maxRequestsPerSecondPerIp, System::currentTimeMillis);
  }

  /** For tests: an explicit limit and an injectable millis-clock, so window-elapsing behavior can be verified without a real sleep. */
  public RequestThrottle(int maxRequestsPerSecondPerIp, LongSupplier clockMillis) {
    assert maxRequestsPerSecondPerIp > 0;
    assert clockMillis != null;
    this.maxRequestsPerSecondPerIp = maxRequestsPerSecondPerIp;
    this.clockMillis = clockMillis;
    this.lastSweepMillis = new AtomicLong(clockMillis.getAsLong());
  }

  private final int maxRequestsPerSecondPerIp;
  private final LongSupplier clockMillis;
  private final ConcurrentHashMap<String, Bucket> bucketsByIp = new ConcurrentHashMap<>();
  private final AtomicLong lastSweepMillis;

  public int getMaxRequestsPerSecondPerIp() {
    return this.maxRequestsPerSecondPerIp;
  }

  /** @return {@code true} if a request from {@code clientIp} may proceed now, {@code false} if it should be throttled (HTTP 429). */
  public boolean allow(String clientIp) {
    long now = this.clockMillis.getAsLong();
    this.maybeSweepIdleBuckets(now);
    Bucket bucket = this.bucketsByIp.computeIfAbsent(clientIp, ip -> new Bucket(this.maxRequestsPerSecondPerIp, now));
    return bucket.tryConsume(now, this.maxRequestsPerSecondPerIp);
  }

  /** Bounds {@link #bucketsByIp}'s size under traffic from many distinct IPs, without paying an O(map size) scan on every single request. */
  private void maybeSweepIdleBuckets(long now) {
    long last = this.lastSweepMillis.get();
    if ((now - last < SWEEP_INTERVAL_MILLIS) || !this.lastSweepMillis.compareAndSet(last, now)) {
      return; // Not due yet, or another thread just claimed this sweep
    }
    this.bucketsByIp.entrySet().removeIf(e -> (now - e.getValue().lastAccessMillis) > IDLE_EVICTION_MILLIS);
  }

  /** One IP's token bucket. All state changes go through the {@code synchronized} {@link #tryConsume}, so it's safe under concurrent requests from the same IP. */
  private static final class Bucket {
    Bucket(int initialTokens, long now) {
      this.tokens = initialTokens;
      this.lastRefillMillis = now;
      this.lastAccessMillis = now;
    }

    private double tokens;
    private long lastRefillMillis;
    volatile long lastAccessMillis; // Read without synchronization by RequestThrottle.maybeSweepIdleBuckets's eviction scan - a slightly stale read there only risks a bucket surviving one extra sweep, never premature eviction of an active one.

    synchronized boolean tryConsume(long now, int capacity) {
      long elapsedMillis = now - this.lastRefillMillis;
      if (elapsedMillis > 0) {
        double refill = (elapsedMillis / 1000.0) * capacity;
        this.tokens = Math.min(capacity, this.tokens + refill);
        this.lastRefillMillis = now;
      }
      this.lastAccessMillis = now;
      if (this.tokens >= 1.0) {
        this.tokens -= 1.0;
        return true;
      }
      return false;
    }
  }
}
