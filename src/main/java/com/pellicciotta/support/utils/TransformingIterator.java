// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 *  An iterator that transforms the elements provided by an underlying {@link Iterator}.
 */
public class TransformingIterator<S, T> implements Iterator<T> {
  // <editor-fold desc="Construction">

  /**
   * Creates a new iterator using the provided one as element source.
   *
   * @param source The underlying element iterator, whose elements should get transformed.
   * @param transform Function to apply to each element before returning it.
   */
  public TransformingIterator(Iterator<S> source, Function<S, T> transform) {
    Contract.isNotNull(source);
    Contract.isNotNull(source);
    this.source = source;
    this.transform = transform;
  }

  private final Iterator<S> source;
  private Function<S, T> transform;

  // </editor-fold>
  // <editor-fold desc="Iterator Interface">

  /**
   * Returns whether another element is to be expected
   *
   * @return True if {@link #next()} will return another element.
   */
  @Override
  public boolean hasNext() {
    return this.source.hasNext();
  }

  /**
   * Fetches the next element, if any, from the underlying {@link Iterator}
   * and transforms it.
   *
   * @return The next, transformed, element.
   *
   * @throws NoSuchElementException if there is no next element.
   */
  @Override
  public T next() {
    return this.transform.apply(this.source.next());
  }

  // </editor-fold>
}
