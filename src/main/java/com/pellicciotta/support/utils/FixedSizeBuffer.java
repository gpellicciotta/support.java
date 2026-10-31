// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 *  A fixed-size buffer: while new elements are added, older ones get removed if the buffer has reached its max. capacity.
 *  <p>
 *  This buffer is safe to use from multiple threads.</p>
 */
public class FixedSizeBuffer<T> implements Iterable<T> {
  /**
   * Creates an empty buffer.
   *
   * @param capacity The max. number of elements held before the oldest ones are discarded.
   */
  public FixedSizeBuffer(int capacity) {
    this.capacity = capacity;
    this.buffer = new ConcurrentLinkedDeque<>();
  }

  private final int capacity;
  private final Deque<T> buffer;

  /**
   * @return The max. number of elements that can be stored in this buffer.
   */
  public int capacity() {
    return this.capacity;
  }

  /**
   * @return The number of elements stored in this buffer. Always &lt;= {@link #capacity()}.
   */
  public int size() {
    return this.buffer.size();
  }

  /**
   *  Add a new element in this buffer's first position.
   *  If by adding this element, the buffer's capacity is exceeded, the last buffer element will be removed.
   *
   *  @param element The element to add.
   */
  public void add(T element) {
    while (this.buffer.size() >= this.capacity) {
      this.buffer.removeLast();
    }
    this.buffer.addFirst(element);
  }

  /**
   *  Iterate over this buffer's elements, from first (i.e. newest or last added) to last (i.e. oldest still in buffer).
   *
   *  @return The, potentially empty, iterator.
   */
  public Iterator<T> iterator() {
    return this.buffer.iterator();
  }

  /**
   *  @return A short description that can aid during debugging
   */
  @Override
  public String toString() {
    return String.format("fixed-buffer { capacity: %d, size: %d }", this.capacity, this.buffer.size());
  }
}
