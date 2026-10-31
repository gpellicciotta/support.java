// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging;

/**
 *  API to associate context-data with a {@link Log} during the timespan in which this {@link Span} object is active.
 */
public interface Span extends AutoCloseable {
  // <editor-fold desc="Properties">

  /**
   *  @return This span's name.
   */
  String getName();

  /**
   *  @return The {@link Log} this span was created for.
   */
  Log getLog();

  /** @return Whether this span is closed and hence can no longer be used. */
  boolean isClosed();

  /**
   *  @return The timestamp (in millis since epoch) at which this span got started.
   */
  long getStartTime();

  /**
   *  @return The timestamp (in millis since epoch) at which this span got closed. Will be zero if not closed yet.
   */
  long getCloseTime();

  /**
   *  @return The time this span has been active (until now or until it got closed), in millis.
   */
  long getElapsedTime();

  // </editor-fold>
  // <editor-fold desc="Associating Additional Data">

  /**
   *  Create a {@link ContextDataItem} that will remain associated with the current {@link Span} object until closed.
   *
   *  @param itemName The name of the piece of data to be stored (and provided with all subsequent log messages).
   *  @param itemValue The value of the data to be stored (and provided with all subsequent log messages).
   */
  ContextDataItem add(String itemName, Object itemValue);

  /**
   *  Close this span, hereby making it inactive, and also removing any context-data that was associated with it.
   *  At the same time, the active {@link com.pellicciotta.support.logging.spi.LogBackEnd} will be notified of this's span's closing.
   */
  void close();

  // </editor-fold>
}
