// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import java.util.Iterator;
import java.util.List;

/**
 *  A generic sequence of elements within some larger collection or sequence.
 *
 *  @param <T> The sequence or collection of elements this span refers to.
 *  @param <E> The type of elements in the source collection and hence also in the span.
 */
public abstract class Slice<T, E> implements Iterable<E>
{
  // <editor-fold desc="Construction">

  /**
   *  Create a new span.
   *
   *  @param source The element source. Can be <c>null</c> for pseudo-tokens.
   *  @param startIndex Index of the span's first element.
   *  @param endIndex Index one beyond the span's last element.
   */
  public Slice(T source, int startIndex, int endIndex) {
    Contract.isNotNull("source", source);
    Contract.isTrue("startIndex >= 0", startIndex >= 0);
    Contract.isTrue("endIndex >= startIndex", endIndex >= startIndex);
    this.source = source;
    this.startIndex = startIndex;
    this.endIndex = endIndex;
  }

  // </editor-fold>
  // <editor-fold desc="Properties">

  protected final T source;
  protected final int startIndex; // Inclusive
  protected final int endIndex; // Exclusive

  /**
   *  @return The collection of sequence of elements this span refers to.
   */
  public T getSource() {
    return this.source;
  }

  /**
   * @return The index of the first index covered by this span (unless the span has length zero).
   */
  public int getStartIndex() {
    return this.startIndex;
  }

  /**
   * @return One beyond the last element index covered by this span. If equal to the start index, there are effectively zero elements covered by this span.
   */
  public int getEndIndex() { return this.endIndex; }

  /**
   * @return The number of elements covered by this span.
   */
  public int getLength() { return this.endIndex - this.startIndex; };

  // </editor-fold>
  // <editor-fold desc="Element Access">

  /**
   *  Get a specific element.
   *
   *  @param index The element index, which must be between {@link #getStartIndex()} (inclusively) and {@link #getEndIndex()} (exclusively).
   *
   *  @return The element with the given index.
   *
   *  @throws IndexOutOfBoundsException If index is invalid.
   */
  public abstract E elementAt(int index);

  /**
   *  @return The elements covered by this span.
   */
  public List<E> elements() {
    return CollectionUtils.listFromIterator(this.iterator());
  }

  /**
   *  @return An iterator over the elements covered by this span.
   */
  public Iterator<E> iterator() {
    int[] currentIndex = new int[] { this.startIndex };
    return new Iterator<E>() {
      @Override
      public boolean hasNext() {
        return currentIndex[0] < Slice.this.endIndex;
      }

      @Override
      public E next() {
        int idx = currentIndex[0];
        if (idx >= Slice.this.endIndex) {
          return null;
        }
        currentIndex[0] = idx + 1;
        return Slice.this.elementAt(idx);
      }
    };
  }

  // </editor-fold>
  // <editor-fold desc="Comparison">

  @Override
  public int hashCode() {
    int hash = 7;
    hash = (37 * hash) + (this.source != null ? this.source.hashCode() : 0);
    hash = (37 * hash) + this.startIndex;
    hash = (37 * hash) + this.endIndex;
    return hash;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (!(other instanceof Slice)) {
      return false;
    }
    Slice<?,?> otherSlice = (Slice<?,?>) other;
    return (this.source == otherSlice.source) && Checks.areEqual(this.startIndex, otherSlice.endIndex) && Checks.areEqual(this.endIndex, otherSlice.endIndex);
  }

  // </editor-fold>
  // <editor-fold desc="Debugging">

  /**
   * @return A human-readable representation.
   */
  @Override
  public String toString() {
    return String.format("span: { size: %d, start: %d, end: %d }", this.getLength(), this.getStartIndex(), this.getEndIndex());
  }

  // </editor-fold>
}
