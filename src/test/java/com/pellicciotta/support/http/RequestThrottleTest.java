// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

import org.junit.jupiter.api.*;

import java.util.concurrent.atomic.AtomicLong;

/**
 *  Tests for {@link RequestThrottle}, the per-IP token-bucket limiter. Uses the injectable millis-clock
 *  constructor instead of a real sleep, so the window-elapses-and-refills case runs instantly and
 *  deterministically.
 */
public class RequestThrottleTest
{
  @Test
  public void allowsRequestsUpToTheLimit() {
    AtomicLong clock = new AtomicLong(0L);
    RequestThrottle throttle = new RequestThrottle(5, clock::get);
    for (int i = 0; i < 5; i++) {
      Assertions.assertTrue(throttle.allow("1.2.3.4"), "request " + i + " should be allowed (within the limit of 5)");
    }
  }

  @Test
  public void deniesRequestsBeyondTheLimitWithinTheSameWindow() {
    AtomicLong clock = new AtomicLong(0L);
    RequestThrottle throttle = new RequestThrottle(5, clock::get);
    for (int i = 0; i < 5; i++) {
      Assertions.assertTrue(throttle.allow("1.2.3.4"));
    }
    Assertions.assertFalse(throttle.allow("1.2.3.4"), "a 6th request within the same second should be throttled");
    Assertions.assertFalse(throttle.allow("1.2.3.4"), "still throttled without the clock advancing");
  }

  @Test
  public void tracksEachIpIndependently() {
    AtomicLong clock = new AtomicLong(0L);
    RequestThrottle throttle = new RequestThrottle(1, clock::get);
    Assertions.assertTrue(throttle.allow("1.1.1.1"));
    Assertions.assertFalse(throttle.allow("1.1.1.1"), "a second request from the same IP within the window should be throttled");
    Assertions.assertTrue(throttle.allow("2.2.2.2"), "a different IP has its own, independent budget");
  }

  @Test
  public void allowsRequestsAgainAfterTheWindowElapses() {
    AtomicLong clock = new AtomicLong(0L);
    RequestThrottle throttle = new RequestThrottle(3, clock::get);
    for (int i = 0; i < 3; i++) {
      Assertions.assertTrue(throttle.allow("1.2.3.4"));
    }
    Assertions.assertFalse(throttle.allow("1.2.3.4"));
    clock.addAndGet(1000L); // Advance the fake clock by a full second - no real sleep needed
    Assertions.assertTrue(throttle.allow("1.2.3.4"), "the bucket should have fully refilled after a full second");
  }

  @Test
  public void refillsGraduallyRatherThanAllOrNothing() {
    AtomicLong clock = new AtomicLong(0L);
    RequestThrottle throttle = new RequestThrottle(10, clock::get);
    for (int i = 0; i < 10; i++) {
      Assertions.assertTrue(throttle.allow("1.2.3.4"));
    }
    Assertions.assertFalse(throttle.allow("1.2.3.4"));
    clock.addAndGet(500L); // Half a second - should refill about half the bucket (5 tokens), not the full 10
    for (int i = 0; i < 5; i++) {
      Assertions.assertTrue(throttle.allow("1.2.3.4"), "token " + i + " of the ~5 refilled after half a second should be allowed");
    }
    Assertions.assertFalse(throttle.allow("1.2.3.4"), "only ~5 tokens should have refilled after half a second, not 10");
  }

  @Test
  public void defaultConstructorUsesTheRealSystemClock() {
    RequestThrottle throttle = new RequestThrottle(1);
    Assertions.assertEquals(1, throttle.getMaxRequestsPerSecondPerIp());
    Assertions.assertTrue(throttle.allow("3.3.3.3"));
  }
}
