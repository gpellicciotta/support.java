// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging.spi;

import com.pellicciotta.support.logging.Log;
import com.pellicciotta.support.logging.Span;

/**
 *  The back-end that needs to be able to create and manage {@link Log} and {@link Span} objects,
 *  while routing messages to their final destinations.
 */
public abstract class LogBackEnd implements AutoCloseable
{
  /**
   *  Configure the back-end.
   *
   *  @param config The configuration to be applied.
   */
  public abstract void configure(LoggingConfig config);

  /** @return Whether this back-end is closed and hence can no longer be used. */
  public abstract boolean isClosed();

  /**
   *  Close this back-end, which means flushing any data that got not logged yet
   *  and invalidating any {@link Log} instances that got created by this back-end.
   */
  public void close() { }

  /**
   *  Get or create a log with the given name, complying with the current configuration.
   *
   *  @param name The name of the log, which in a sense is the <em>source name</em> of the log record/event.
   */
  public abstract Log getLog(String name);

  /**
   *  A log event has been created: handle it.
   *
   *  @param logEvent The log even to be handled in a back-end specific way.
   */
  public abstract void onLogEvent(LogEvent logEvent);

  /**
   *  A span has started.
   *
   *  @param span The span that was started.
   */
  public abstract void onSpanStarted(Span span);

  /**
   *  A span has ended.
   *
   *  @param span The span that has ended.
   */
  public abstract void onSpanClosed(Span span);
}
