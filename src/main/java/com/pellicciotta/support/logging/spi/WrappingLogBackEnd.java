// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging.spi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.pellicciotta.support.logging.*;
import com.pellicciotta.support.utils.Contract;
import com.pellicciotta.support.utils.DateTimeUtils;

/**
 *  Class for implementing a {@link LogBackEnd} that also wraps an existing {@link LogBackEnd}.
 *  <p>
 *  Implementing classes must ensure to remain thread-safe.</p>
 */
public abstract class WrappingLogBackEnd extends LogBackEnd {
  // <editor-fold desc="Construction">

  /**
   * Create a back-end that delegates to another one.
   *
   * @param actual The back-end to wrap; must not be <code>null</code>.
   */
  protected WrappingLogBackEnd(LogBackEnd actual) {
    Contract.isNotNull("actual", actual);
    this.wrapped = actual;
  }

  private final LogBackEnd wrapped;

  // </editor-fold>
  // <editor-fold desc="LogBackEnd Implementation">

  /**
   *  Configure the back-end.
   *
   *  @param config The configuration to be applied.
   */
  public synchronized void configure(LoggingConfig config) {
    this.wrapped.configure(config);
  }

  /** @return Whether this back-end is closed and hence can no longer be used. */
  public synchronized boolean isClosed() {
    return this.wrapped.isClosed();
  }

  /**
   *  Close this back-end, which means flushing any data that got not logged yet
   *  and invalidating any {@link Log} instances that got created by this back-end.
   */
  public synchronized void close() {
    this.wrapped.close();
  }

  /**
   *  Create a log with the given name.
   *
   *  @param name The name of the log, which in a sense is the <em>source name</em> of the log record/event.
   */
  public synchronized Log getLog(String name) {
    Log actualLog = this.wrapped.getLog(name);
    int minLevel = LogLevel.ALL;
    if (actualLog.isDebugEnabled()) {
      minLevel = LogLevel.DEBUG;
    }
    else if (actualLog.isInfoEnabled()) {
      minLevel = LogLevel.INFO;
    }
    else if (actualLog.isWarningEnabled()) {
      minLevel = LogLevel.WARNING;
    }
    else if (actualLog.isErrorEnabled()) {
      minLevel = LogLevel.ERROR;
    }
    else {
      minLevel = LogLevel.NONE;
    }
    return new LogBase(actualLog.getName(), minLevel, this);
  }

  /**
   * Pass the event on to the wrapped back-end.
   *
   * @param logEvent The event to be written.
   */
  public void onLogEvent(LogEvent logEvent) {
    this.wrapped.onLogEvent(logEvent);
  }

  /** Per-thread nesting depth of currently open spans. */
  protected static final ThreadLocal<Integer> spanLevel = ThreadLocal.withInitial(() -> 0);

  /**
   * Log an INFO event announcing the span and increase the calling thread's span nesting level.
   *
   * @param span The span that was started.
   */
  public synchronized void onSpanStarted(Span span) {
    LogEvent evt = new LogEvent(span.getLog().getName(), LogLevel.INFO, "Operation '%s' started", new Object[] { span.getName() }, null, null);
    this.onLogEvent(evt);
    spanLevel.set(spanLevel.get() + 1);
  }

  /**
   * Decrease the calling thread's span nesting level (never below zero) and log an INFO event with the span's
   * elapsed time.
   *
   * @param span The span that was closed.
   */
  public synchronized void onSpanClosed(Span span) {
    spanLevel.set(Math.max(0, spanLevel.get() - 1));
    LogEvent evt = new LogEvent(span.getLog().getName(), LogLevel.INFO, "Operation '%s' ended in %s", new Object[] { span.getName(), DateTimeUtils.formatTimespan(span.getElapsedTime()) }, null, null);
    this.onLogEvent(evt);
  }

  // </editor-fold>
}
