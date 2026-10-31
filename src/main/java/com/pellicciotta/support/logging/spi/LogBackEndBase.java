// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging.spi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.pellicciotta.support.exceptions.ConfigurationException;
import com.pellicciotta.support.logging.*;
import com.pellicciotta.support.utils.DateTimeUtils;

/**
 *  Base class for implementing a {@link LogBackEnd}.
 *  <p>
 *  Implementing classes must ensure to remain thread-safe.</p>
 */
public abstract class LogBackEndBase extends LogBackEnd {
  // <editor-fold desc="Construction">

  /**
   * Create a back-end with a default {@link LoggingConfig}.
   *
   * @throws ConfigurationException If the default configuration cannot be applied.
   */
  protected LogBackEndBase() throws ConfigurationException {
    this(new LoggingConfig());
  }

  /**
   * Create a back-end and apply the given configuration.
   *
   * @param config The configuration to apply; may be <code>null</code> (no named loggers get created).
   * @throws ConfigurationException If the configuration cannot be applied.
   */
  protected LogBackEndBase(LoggingConfig config) throws ConfigurationException {
    this.configure(config);
  }

  private boolean closed = false;
  private LoggingConfig config = null;
  private final ConcurrentMap<String, LogBase> logs = new ConcurrentHashMap<>();

  // </editor-fold>
  // <editor-fold desc="LogBackEnd Implementation">

  /**
   * Discard all logs created so far; called by {@link #configure(LoggingConfig)} before applying a new configuration.
   */
  protected void resetConfiguration() {
    this.logs.clear();
  }

  /**
   *  (re)Configure the back-end.
   *
   *  @param config The configuration to be applied. Any previous configuration will be undone.
   */
  public synchronized void configure(LoggingConfig config) throws ConfigurationException {
    this.resetConfiguration();
    this.config = config;
    if (this.config == null) {
      return ;
    }
    // Re-configure
    for (LoggerConfig lc : this.config.getLoggerConfigs().values()) {
      int minLevel = lc.getMinLogLevel() != null ? lc.getMinLogLevel().intValue() : config.getMinLevel();
      this.logs.putIfAbsent(lc.getName(), this.createNewLog(lc.getName(), minLevel));
    }
  }

  @Override
  public boolean isClosed() {
    return this.closed;
  }

  /**
   *  Close this back-end, which means flushing any data that got not logged yet
   *  and invalidating any {@link Log} instances that got created by this back-end.
   */
  public synchronized void close() {
    this.closed = true; // Close, so no new activity can happen
    for (Log log : this.logs.values()) {
      log.close();
    }
    this.logs.clear();
  }

  /**
   * Factory hook for the {@link LogBase} instances handed out by this back-end; subclasses may override it.
   *
   * @param name The log name.
   * @param minLevel The minimum {@link LogLevel} that the new log will emit.
   * @return A new log bound to this back-end.
   */
  protected LogBase createNewLog(String name, int minLevel) {
    return new LogBase(name, minLevel, this);
  }

  /**
   *  Create a log with the given name.
   *
   *  @param name The name of the log, which in a sense is the <em>source name</em> of the log record/event.
   */
  public synchronized LogBase getLog(String name) {
    if (this.closed) {
      throw new IllegalStateException("This log back-end has been closed");
    }
    if (name == null) {
      name = "";
    }
    int minLogLevel = this.config == null ? LogLevel.ALL : this.config.getMinLevel();
    return this.logs.computeIfAbsent(name, n -> this.createNewLog(n, minLogLevel));
  }

  /**
   * Deliver a log event to the actual log destination.
   *
   * @param logEvent The event to be written.
   */
  public abstract void onLogEvent(LogEvent logEvent);

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
