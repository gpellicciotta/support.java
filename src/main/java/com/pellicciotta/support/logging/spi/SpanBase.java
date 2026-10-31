// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging.spi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.pellicciotta.support.logging.*;

public class SpanBase implements Span
{
  // <editor-fold desc="Construction">

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
