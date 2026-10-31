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
public abstract class LogBackEndBase extends LogBackEnd
{
  // <editor-fold desc="Construction">

  protected LogBackEndBase() throws ConfigurationException {
    this(new LoggingConfig());
  }

  protected LogBackEndBase(LoggingConfig config) throws ConfigurationException {
    this.configure(config);
  }

  private boolean closed = false;
  private LoggingConfig config = null;
  private final ConcurrentMap<String, LogBase> logs = new ConcurrentHashMap<>();

  // </editor-fold>
  // <editor-fold desc="LogBackEnd Implementation">

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
    if (name == null) { name = ""; }
    int minLogLevel = this.config == null ? LogLevel.ALL : this.config.getMinLevel();
    return this.logs.computeIfAbsent(name, n -> this.createNewLog(n, minLogLevel));
  }

  public abstract void onLogEvent(LogEvent logEvent);

  protected static final ThreadLocal<Integer> spanLevel = ThreadLocal.withInitial(() -> { return 0; } );

  public synchronized void onSpanStarted(Span span) {
    LogEvent evt = new LogEvent(span.getLog().getName(), LogLevel.INFO, "Operation '%s' started", new Object[] { span.getName() }, null, null);
    this.onLogEvent(evt);
    spanLevel.set(spanLevel.get() + 1);
  }

  public synchronized void onSpanClosed(Span span) {
    spanLevel.set(Math.max(0, spanLevel.get() - 1));
    LogEvent evt = new LogEvent(span.getLog().getName(), LogLevel.INFO, "Operation '%s' ended in %s", new Object[] { span.getName(), DateTimeUtils.formatTimespan(span.getElapsedTime()) }, null, null);
    this.onLogEvent(evt);
  }

  // </editor-fold>
}
