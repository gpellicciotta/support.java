// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging.spi;

import javax.naming.Context;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import com.pellicciotta.support.logging.*;
import com.pellicciotta.support.logging.jul.JULBasedLog;

/**
 *  Base class for implementing a {@link Log}.
 */
public class LogBase implements Log {
  // <editor-fold desc="Construction">

  protected LogBase(String name, int minLevel, LogBackEnd backEnd) {
    assert name != null;
    assert backEnd != null;
    this.name = name;
    this.backEnd = backEnd;
    this.closed = false;
    this.minLevel = minLevel;
    this.contextData = new ConcurrentHashMap<>();
    this.activeSpan = null;
  }

  final String name;
  final int minLevel;
  final LogBackEnd backEnd;
  private boolean closed;
  private final ConcurrentMap<String, ContextDataItem> contextData;
  private SpanBase activeSpan;

  // </editor-fold>
  // <editor-fold desc="Implementation">

  protected synchronized void onSpanClosed(SpanBase span) {
    if (span == this.activeSpan) {
      this.activeSpan = span.parentSpan;
    }
    this.backEnd.onSpanClosed(span);
  }

  // </editor-fold>
  // <editor-fold desc="Properties">

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public boolean isClosed() {
    return this.closed;
  }

  @Override
  public boolean isErrorEnabled() {
    return (this.minLevel <= LogLevel.ERROR);
  }

  @Override
  public boolean isWarningEnabled() {
    return (this.minLevel <= LogLevel.WARNING);
  }

  @Override
  public boolean isInfoEnabled() {
    return (this.minLevel <= LogLevel.INFO);
  }

  @Override
  public boolean isDebugEnabled() {
    return (this.minLevel <= LogLevel.DEBUG);
  }

  // </editor-fold>
  // <editor-fold desc="Log Methods">

  protected synchronized ContextData getContextData() {
    Map<String, ContextDataItem> ctx = new HashMap<>();
    List<ContextDataItem> logData = new ArrayList<>(); // Safety copy
    // First span-based items, from most specific (= active) span to least:
    for (SpanBase span = this.activeSpan; span != null; span = span.parentSpan) {
      for (ContextDataItem cdi : span.contextData.values()) {
        ctx.putIfAbsent(cdi.getName(), cdi);
      }
    }
    // Add items associated directly with the log:
    for (ContextDataItem cdi : this.contextData.values()) {
      if (!cdi.isClosed()) {
        ctx.putIfAbsent(cdi.getName(), cdi);
      }
    }
    StackTraceElement callerLocation = getCallerStackTrace();
    if (callerLocation != null) {
      ContextDataItem cdi = new ContextDataItemBase(ORIGIN_CLASS, callerLocation.getClassName(), this.backEnd);
      ctx.put(ORIGIN_CLASS, cdi);
      cdi = new ContextDataItemBase(ORIGIN_METHOD, callerLocation.getMethodName(), this.backEnd);
      ctx.put(ORIGIN_METHOD, cdi);
      String source = callerLocation.getFileName();
      if (source != null) {
        cdi = new ContextDataItemBase(ORIGIN_FILE, source, this.backEnd);
        ctx.put(ORIGIN_FILE, cdi);
      }
      int line = callerLocation.getLineNumber();
      if (line >= 0) {
        cdi = new ContextDataItemBase(ORIGIN_LINE, line, this.backEnd);
        ctx.put(ORIGIN_LINE, cdi);
      }
    }
    return new ContextData(ctx.values());
  }

  public static final String ORIGIN_CLASS = "origin-class";
  public static final String ORIGIN_METHOD = "origin-method";
  public static final String ORIGIN_FILE = "origin-file";
  public static final String ORIGIN_LINE = "origin-line";

  private static StackTraceElement getCallerStackTrace() {
    Throwable t = new Throwable();
    t.fillInStackTrace();
    StackTraceElement[] stackTrace = t.getStackTrace();
    for (int i = 0; i < stackTrace.length; i++) {
      String className = stackTrace[i].getClassName();
      if (className.startsWith("com.pellicciotta.support.logging")) {
        continue;
      }
      return stackTrace[i];
    }
    return stackTrace[0];
  }

  protected synchronized void log(int level, String msgFormat, Object[] formatParams) {
    ContextData ctxData = this.getContextData();
    Throwable cause = null;
    if (formatParams != null) {
      for (int i = 0; i < formatParams.length; i++) {
        if (formatParams[i] instanceof Throwable) {
          cause = (Throwable)formatParams[i];
          break;
        }
      }
    }
    LogEvent event = new LogEvent(this.name, level, msgFormat, formatParams, ctxData, cause);
    this.backEnd.onLogEvent(event);
  }

  @Override
  public void error(String msgFormat, Object... formatParams) {
    if (this.minLevel <= LogLevel.ERROR) {
      this.log(LogLevel.ERROR, msgFormat, formatParams);
    }
  }

  @Override
  public void warning(String msgFormat, Object... formatParams) {
    if (this.minLevel <= LogLevel.WARNING) {
      this.log(LogLevel.WARNING, msgFormat, formatParams);
    }
  }

  @Override
  public void info(String msgFormat, Object... formatParams) {
    if (this.minLevel <= LogLevel.INFO) {
      this.log(LogLevel.INFO, msgFormat, formatParams);
    }
  }

  @Override
  public void debug(String msgFormat, Object... formatParams) {
    if (this.minLevel <= LogLevel.DEBUG) {
      this.log(LogLevel.DEBUG, msgFormat, formatParams);
    }
  }

  // </editor-fold>
  // <editor-fold desc="Associating Additional Data">

  @Override
  public synchronized ContextDataItem add(String itemName, Object itemValue) {
    ContextDataItemBase cdi = new ContextDataItemBase(itemName, itemValue, this.backEnd);
    this.contextData.put(itemName, cdi);
    return cdi;
  }

  @Override
  public synchronized Span start(String spanName) {
    SpanBase span = new SpanBase(spanName, this.activeSpan, this);
    this.activeSpan = span;
    this.backEnd.onSpanStarted(span);
    return span;
  }

  @Override
  public synchronized void close() {
    this.closed = true;
  }

  // </editor-fold>
}
