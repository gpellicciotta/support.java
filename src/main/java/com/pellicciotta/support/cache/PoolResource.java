package com.pellicciotta.support.cache;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import com.pellicciotta.support.logging.Log;
import com.pellicciotta.support.logging.LogService;
import com.pellicciotta.support.utils.Contract;
import com.pellicciotta.support.utils.DateTimeUtils;

/**
 *  A pooled resource needs to be closeable and capable of indicating its health.
 */
public interface PoolResource extends AutoCloseable
{
  /**
   *  @return Whether this is resource is still healthy and can be re-used.
   */
  boolean isHealthy();
}
