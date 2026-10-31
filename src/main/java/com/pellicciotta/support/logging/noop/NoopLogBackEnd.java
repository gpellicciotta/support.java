// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging.noop;

import com.pellicciotta.support.logging.Span;
import com.pellicciotta.support.logging.spi.*;

/**
 *  A {@link LogBackEnd} that simply discards all log messages.
 */
public class NoopLogBackEnd extends LogBackEndBase {
  /** Create a back-end that discards everything it receives. */
  public NoopLogBackEnd() { }

  @Override
  public void onLogEvent(LogEvent logEvent) {
    // Do nothing
  }

  @Override
  public void onSpanStarted(Span span) {
    // Do nothing
  }

  @Override
  public void onSpanClosed(Span span) {
    // Do nothing
  }
}
