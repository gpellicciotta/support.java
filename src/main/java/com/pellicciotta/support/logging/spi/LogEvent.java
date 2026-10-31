// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging.spi;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 *  All the data that fits together and needs to be send to the log destination(s).
 */
public class LogEvent
{
  private static final AtomicLong globalSequenceNumber = new AtomicLong(0);

  public LogEvent(long threadId, Instant logTime, String logName, int logLevel, String message, Object[] msgParams, ContextData ctx, Throwable cause) {
    this.logName = logName;
    this.logLevel = logLevel;
    this.id = globalSequenceNumber.getAndIncrement();
    this.threadId = threadId;
    this.logTime = logTime;
    this.message = message;
    this.params = msgParams;
    this.contextData = ctx;
    this.cause = cause;
  }

  public LogEvent(String logName, int logLevel, String message, Object[] msgParams, ContextData ctx, Throwable cause) {
    this.logName = logName;
    this.logLevel = logLevel;
    this.id = globalSequenceNumber.getAndIncrement();
    this.threadId = Thread.currentThread().getId();
    this.logTime = Instant.now();
    this.message = message;
    this.params = msgParams;
    this.contextData = ctx;
    this.cause = cause;
  }

  private final long id;

  public long getId() {
    return id;
  }

  private final Instant logTime;

  public Instant getLogTime() {
    return logTime;
  }

  private final long threadId;

  public long getThreadId() {
    return threadId;
  }

  private final String logName;

  public String getLogName() {
    return this.logName;
  }

  private final int logLevel;

  public int getLogLevel() {
    return this.logLevel;
  }

  private final String message;

  public String getMessage() { return this.message; }

  private final Object[] params;

  public Object[] getParams() { return this.params; }

  private final ContextData contextData;

  public ContextData getContextData() {
    return this.contextData;
  }

  private final Throwable cause;

  public Throwable getCause() {
    return this.cause;
  }
}
