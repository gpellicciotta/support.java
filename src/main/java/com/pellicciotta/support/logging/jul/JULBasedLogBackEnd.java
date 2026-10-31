// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging.jul;

import java.util.logging.*;

import com.pellicciotta.support.logging.*;
import com.pellicciotta.support.logging.spi.*;

/**
 *  A {@link LogBackEnd} that really uses JUL.
 */
public class JULBasedLogBackEnd extends LogBackEndBase {
  public JULBasedLogBackEnd() { }

  @Override
  public void onLogEvent(LogEvent logEvent) {
    Logger julLog = Logger.getLogger(logEvent.getLogName());
    LogRecord julEvent = new LogRecord(JULUtils.toNearestLevel(logEvent.getLogLevel()), logEvent.getMessage());
    julEvent.setInstant(logEvent.getLogTime());
    julEvent.setParameters(logEvent.getParams());
    julEvent.setThrown(logEvent.getCause());
    julEvent.setSourceClassName((String)logEvent.getContextData().get(LogBase.ORIGIN_CLASS));
    julEvent.setSourceMethodName((String)logEvent.getContextData().get(LogBase.ORIGIN_METHOD));

    julLog.log(julEvent);
  }

  @Override
  public void onSpanStarted(Span span) {
    Logger julLog = Logger.getLogger(span.getLog().getName());
    julLog.fine("Starting span '" + span.getName() + "'");
  }

  @Override
  public void onSpanClosed(Span span) {
    Logger julLog = Logger.getLogger(span.getLog().getName());
    julLog.fine("Ending span '" + span.getName() + "' in " + span.getElapsedTime() + " ms");
  }
}
