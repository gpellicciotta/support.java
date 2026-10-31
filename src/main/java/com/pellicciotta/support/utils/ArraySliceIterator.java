// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *  An iterator that iterates over a slice of an underlying array.
 */
public final class ArraySliceIterator<T> implements Iterator<T> {
  // <editor-fold desc="Construction">

  /**
   * Creates a new iterator using the provided source array as element source.
   *
   * @param source The array over whose elements to iterate.
   * @param startIndex Index of the first element in array to return.
   */
  public ArraySliceIterator(T[] source, int startIndex) {
    Contract.isNotNull(source);
    Contract.isInRange(startIndex, 0, source.length);

    this.source = source;
    this.currentIndex = startIndex;
    this.leftToReturn = Math.max(0, source.length - startIndex);
  }

  /**
   * Creates a new iterator using the provided source array as element source.
   *
   * @param source The array over whose elements to iterate.
   * @param startIndex Index of the first element in array to return.
   * @param count The max. number of elements to return.
   *              Note that fewer elements may be returned in the source array doesn't have elements up to index startIndex + count - 1.
   */
  public ArraySliceIterator(T[] source, int startIndex, int count) {
    Contract.isNotNull(source);
    Contract.isInRange(startIndex, 0, source.length);
    Contract.isInRange(count, 0, Integer.MAX_VALUE);

    this.source = source;
    this.currentIndex = startIndex;
    this.leftToReturn = Math.min(count, source.length - startIndex);
  }

  private T[] source;
  private int currentIndex;
  private int leftToReturn;

  // </editor-fold>

  // <editor-fold desc="Iterator Interface">

  /**
   * Returns whether another element is to be expected
   *
   * @return True if {@link #next()} will return another element.
   */
  @Override
  public boolean hasNext() {
    return (this.leftToReturn > 0);
  }

  /**
   * Fetches the next record; automatically switching the source to obtain
   * linear concatenation
   *
   * @return The next element.
   *
   * @throws NoSuchElementException if there is no next element.
   */
  @Override
  public T next() {
    if (this.leftToReturn <= 0) {
      throw new NoSuchElementException();
    }
    T element = this.source[this.currentIndex];
    this.currentIndex += 1;
    this.leftToReturn -= 1;
    return element;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  // </editor-fold>
}
