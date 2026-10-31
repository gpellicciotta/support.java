// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging.jul;

import java.util.ArrayDeque;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import com.pellicciotta.support.utils.Contract;

/**
 *  An {@link Handler} implementation that will keep track of the n most recent {@link LogRecord}s.
 */
public class RotatingBufferHandler extends Handler
{
  // <editor-fold desc="Construction">

  /**
   *  Create a handler.
   *
   *  @param capacity The max. number of {@link LogRecord}s to keep track of.
   *  @param minLogLevel The min. log level for {@link LogRecord}s to keep track of.
   */
  public RotatingBufferHandler(int capacity, int minLogLevel) {
    Contract.isInRange("capacity", capacity, 1, Integer.MAX_VALUE);
    this.capacity = capacity;
    this.minLogLevel = minLogLevel;
    this.logEvents = new ArrayDeque<>(capacity);
  }

  private final int capacity;
  private final int minLogLevel;
  private final ArrayDeque<LogRecord> logEvents;

  /**
   *  @return The currently buffered (and hence last) {@link LogRecord}s. The first one returned will be the most recent one.
   */
  public synchronized Iterable<LogRecord> getLogRecords() {
    return this.logEvents.clone();
  }

  // </editor-fold>
  // <editor-fold desc="Handler Interface">

  /** {@inheritDoc} */
  @Override
  public synchronized void publish(LogRecord record) {
    if (record.getLevel().intValue() < this.minLogLevel) {
      return ;
    }
    while (this.logEvents.size() >= this.capacity) {
      this.logEvents.removeLast();
    }
    this.logEvents.addFirst(record);
  }

  /** {@inheritDoc} */
  @Override
  public void flush() {
    // Do nothing
  }

  /** {@inheritDoc} */
  @Override
  public synchronized void close() throws SecurityException {
    this.logEvents.clear();
  }

  // </editor-fold>
}
