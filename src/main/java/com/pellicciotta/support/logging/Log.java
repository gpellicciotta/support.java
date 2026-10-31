// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging;

/**
 *  Main logging/tracing API.
 *  <p>
 *  This interface is meant for consumption only.</p>
 */
public interface Log extends AutoCloseable
{
  // <editor-fold desc="Properties">

  /** @return The name of this log. */
  String getName();

  /** @return Whether this log is closed and hence can no longer be used. */
  boolean isClosed();

  /** @return Whether error-messages are enable for this log. */
  boolean isErrorEnabled();

  /** @return Whether warning-messages are enable for this log. */
  boolean isWarningEnabled();

  /** @return Whether info-messages are enable for this log. */
  boolean isInfoEnabled();

  /** @return Whether debug-messages are enable for this log. */
  boolean isDebugEnabled();

  // </editor-fold>
  // <editor-fold desc="Log Methods">

  /**
   *  Log an error message, including any {@link ContextDataItem}s associated with this log object directly
   *  or via any active {@link Span}s.
   *
   *  @param msgFormat The message, possibly with format parameters.
   *  @param formatParams The format parameters.
   */
  void error(String msgFormat, Object... formatParams);

  /**
   *  Log a warning message, including any {@link ContextDataItem}s associated with this log object directly
   *  or via any active {@link Span}s.
   *
   *  @param msgFormat The message, possibly with format parameters.
   *  @param formatParams The format parameters.
   */
  void warning(String msgFormat, Object... formatParams);

  /**
   *  Log an info message, including any {@link ContextDataItem}s associated with this log object directly
   *  or via any active {@link Span}s.
   *
   *  @param msgFormat The message, possibly with format parameters.
   *  @param formatParams The format parameters.
   */
  void info(String msgFormat, Object... formatParams);

  /**
   *  Log a debug message, including any {@link ContextDataItem}s associated with this log object directly
   *  or via any active {@link Span}s.
   *
   *  @param msgFormat The message, possibly with format parameters.
   *  @param formatParams The format parameters.
   */
  void debug(String msgFormat, Object... formatParams);

  // </editor-fold>
  // <editor-fold desc="Associating Additional Data">

  /**
   *  Create a {@link ContextDataItem} that will remain associated with the current {@link Log} object until closed.
   *
   *  @param itemName The name of the piece of data to be stored (and provided with all subsequent log messages).
   *  @param itemValue The value of the data to be stored (and provided with all subsequent log messages).
   */
  ContextDataItem add(String itemName, Object itemValue);

  /**
   *  Start a span, which typically represents everything that happens during a certain timespan that is needed
   *  to complete some logical operation (E.g. handle an HTTP request).
   *  <p>
   *  A span can add context-data to all log messages that are emitted while it is effective.
   *  A span also has an associated timer, so that when the span gets closed, the total elapsed time is known.</p>
   *
   *  @param spanName The name to give to this span. This should typically be the name of an operation.
   *
   *  @return A {@link Span} object that can be used to associated context-data with all subsequent log messages,
   *          and with the span itself.
   */
  Span start(String spanName);

  /** Close this log, and also all still open spans and remove all still available context-data items. */
  void close();

  // </editor-fold>
}
