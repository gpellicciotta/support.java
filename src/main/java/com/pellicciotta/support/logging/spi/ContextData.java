// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging.spi;

import java.util.*;

import com.pellicciotta.support.logging.ContextDataItem;

/**
 *  A read-only collection of {@code ContextDataItem}s.
 */
public class ContextData implements Iterable<ContextDataItem> {
  /**
   *  Construct a new, empty and read-only collection of context data items.
   */
  public ContextData() {
    this.data = Collections.emptyMap();
  }

  /**
   *  Construct a new read-only collection of context data items.
   *
   *  @param items The collection of items to hold.
   */
  public ContextData(Collection<ContextDataItem> items) {
    this.data = new TreeMap<>(String::compareToIgnoreCase);
    for (ContextDataItem cdi : items) {
      this.data.put(cdi.getName(), cdi);
    }
  }

  private final Map<String, ContextDataItem> data;

  /**
   *  @return The number of items contained in this collection.
   */
  public int size() {
    return this.data.size();
  }

  /**
   *  @return Whether this collection contains any items at all.
   */
  public boolean isEmpty() {
    return this.data.isEmpty();
  }

  /**
   *  @return The set of names for which values are present in this object.
   */
  public Set<String> nameSet() {
    return this.data.keySet();
  }

  /**
   *  Check whether a context data item with the given name exists.
   *
   *  @param name The context data item name.
   *
   *  @return True if a value with this name exists, false otherwise.
   */
  public boolean contains(String name) {
    return this.data.containsKey(name);
  }

  /**
   *  Get the name context data value.
   *
   *  @param name The context data item name.
   *
   *  @return The context data value with the given name, or null if no such item exists.
   */
  public Object get(String name) {
    return this.data.get(name).getValue();
  }

  @Override
  public Iterator<ContextDataItem> iterator() {
    return this.data.values().iterator();
  }
}
