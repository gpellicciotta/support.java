// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging.spi;

import com.pellicciotta.support.logging.ContextDataItem;
import com.pellicciotta.support.logging.jul.JULBasedLog;

public class ContextDataItemBase implements ContextDataItem {
  public ContextDataItemBase(String name, Object value, LogBackEnd backEnd) {
    assert name != null;
    assert backEnd != null;
    this.backEnd = backEnd;
    this.name = name;
    this.value = value;
    this.closed = false;
  }

  private final LogBackEnd backEnd;
  private boolean closed;
  private final String name;
  private final Object value;

  @Override
  public String getName() {
    if (this.closed) {
      throw new IllegalStateException("The data item has been disassociated");
    }
    return this.name;
  }

  @Override
  public boolean isClosed() {
    return this.closed;
  }

  @Override
  public Object getValue() {
    if (this.closed) {
      throw new IllegalStateException("The data item has been disassociated");
    }
    return this.value;
  }

  @Override
  public void close() {
    if (this.closed) {
      throw new IllegalStateException("The data item has been disassociated");
    }
    this.closed = true;
  }
}
