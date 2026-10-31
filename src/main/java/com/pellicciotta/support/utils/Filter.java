// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

/**
 * Interface to be implemented for filtering objects of type T. It is the specific method accepting a Filter that will define the
 * semantics of the accept method.
 */
public interface Filter<T> {
  /**
   * @param item The item to be inspected for being viable or not.
   *
   * @return Whether the provided item should be accepted or not.
   */
  boolean accept(T item);
}
