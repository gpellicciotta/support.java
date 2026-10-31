// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging.spi;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 *  All the data that fits together and needs to be send to the log destination(s).
 */
public class LogEvent {
  private static final AtomicLong globalSequenceNumber = new AtomicLong(0);

  /**
   * Create an event with an explicit thread and time; it gets the next global sequence number.
   *
   * @param threadId The ID of the originating thread.
   * @param logTime The time of the event.
   * @param logName The name of the originating log.
   * @param logLevel The event level.
   * @param message The message format.
   * @param msgParams The message format parameters; may be <code>null</code>.
   * @param ctx The context data; may be <code>null</code>.
   * @param cause The associated throwable; may be <code>null</code>.
   */
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

  /**
   * Create an event stamped with the current thread and the current time; it gets the next global sequence number.
   *
   * @param logName The name of the originating log.
   * @param logLevel The event level.
   * @param message The message format.
   * @param msgParams The message format parameters; may be <code>null</code>.
   * @param ctx The context data; may be <code>null</code>.
   * @param cause The associated throwable; may be <code>null</code>.
   */
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

  /**
   * @return The event's sequence number, unique and increasing within this JVM.
   */
  public long getId() {
    return id;
  }

  private final Instant logTime;

  /**
   * @return The time at which the event occurred.
   */
  public Instant getLogTime() {
    return logTime;
  }

  private final long threadId;

  /**
   * @return The ID of the thread that originated the event.
   */
  public long getThreadId() {
    return threadId;
  }

  private final String logName;

  /**
   * @return The name of the log that produced the event.
   */
  public String getLogName() {
    return this.logName;
  }

  private final int logLevel;

  /**
   * @return The event level, as a {@link com.pellicciotta.support.logging.LogLevel} value.
   */
  public int getLogLevel() {
    return this.logLevel;
  }

  private final String message;

  /**
   * @return The message format, before parameter substitution.
   */
  public String getMessage() {
    return this.message;
  }

  private final Object[] params;

  /**
   * @return The message format parameters; may be <code>null</code>.
   */
  public Object[] getParams() {
    return this.params;
  }

  private final ContextData contextData;

  /**
   * @return The context data attached to the event; may be <code>null</code>.
   */
  public ContextData getContextData() {
    return this.contextData;
  }

  private final Throwable cause;

  /**
   * @return The throwable associated with the event; may be <code>null</code>.
   */
  public Throwable getCause() {
    return this.cause;
  }
}
