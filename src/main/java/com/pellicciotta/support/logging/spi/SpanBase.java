// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging.spi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.pellicciotta.support.logging.*;

/** Default {@link Span}: tracks timing and context data, and notifies its {@link LogBase} when closed. */
public class SpanBase implements Span {
  // <editor-fold desc="Construction">

  /**
   * Create an open span, started now.
   *
   * @param name The span name; must not be <code>null</code> (asserted).
   * @param parentSpan The enclosing span; may be <code>null</code>.
   * @param log The owning log; must not be <code>null</code> (asserted).
   */
  protected SpanBase(String name, SpanBase parentSpan, LogBase log) {
    assert name != null;
    assert log != null;
    this.name = name;
    this.parentSpan = parentSpan;
    this.log = log;
    this.closed = false;
    this.contextData = new ConcurrentHashMap<>();
    this.startTime = System.currentTimeMillis();
    this.stopTime = 0L;
  }

  private final String name;
  final SpanBase parentSpan;
  private final LogBase log;
  private boolean closed;
  final ConcurrentMap<String, ContextDataItem> contextData;
  private long startTime;
  private long stopTime;

  // </editor-fold>
  // <editor-fold desc="Properties">

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public Log getLog() {
    return this.log;
  }

  @Override
  public boolean isClosed() {
    return this.closed;
  }

  @Override
  public long getStartTime() {
    return this.startTime;
  }

  @Override
  public long getCloseTime() {
    return this.stopTime;
  }

  @Override
  public long getElapsedTime() {
    if (this.closed) {
      return (this.stopTime - this.startTime);
    }
    return (System.currentTimeMillis() - this.startTime);
  }

  // </editor-fold>

  @Override
  public ContextDataItem add(String itemName, Object itemValue) {
    ContextDataItemBase cdi = new ContextDataItemBase(itemName, itemValue, this.log.backEnd);
    this.contextData.put(itemName, cdi);
    return cdi;
  }

  @Override
  public void close() {
    this.stopTime = System.currentTimeMillis();
    this.closed = true;
    this.log.onSpanClosed(this);
  }
}
