// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 *  An iterator that iterates over another, while applying a {@link Predicate}..
 */
public class FilteringIterator<E> implements Iterator<E> {
  /**
   *  Create a new iterator that will apply the given filter to the source iterator.
   *
   *  @param source The original iterator to retrieve elements from.
   *  @param filter The predicate to test elements against. Only those passing the test, will be returned from this iterator.
   */
  public FilteringIterator(Iterator<? extends E> source, Predicate<E> filter) {
    Contract.isNotNull(source);
    Contract.isNotNull(filter);
    this.source = source;
    this.filter = filter;
    this.prepareNext();
  }

  private final Iterator<? extends E> source;
  private final Predicate<E> filter;
  private E nextElement;
  private boolean nextElementIsValid;

  /** {@inheritDoc} */
  @Override
  public boolean hasNext() {
    return this.nextElementIsValid;
  }

  /** {@inheritDoc} */
  @Override
  public E next() {
    if (!this.nextElementIsValid) {
      throw new NoSuchElementException();
    }
    E n = this.nextElement;
    this.prepareNext();
    return n;
  }

  private void prepareNext() {
    this.nextElement = null;
    this.nextElementIsValid = false;
    while (this.source.hasNext()) {
      final E n = this.source.next();
      if (this.filter.test(n)) {
        this.nextElement = n;
        this.nextElementIsValid = true;
        return;
      }
    }
  }
}
