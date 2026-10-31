// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import com.pellicciotta.support.app.Version;

/**
 * A generic pair representing a start and end point in a sequence.
 */
public class Range<T extends Comparable<T>> implements Comparable<Range<T>> {
  // <editor-fold desc="Construction">

  /**
   * Create a new {@link Range}.
   *
   * @param first First item in range (inclusive). Can be <c>null</c> to indicate there is no first item.
   * @param last Last item in range (inclusive). Can be <c>null</c> to indicate there is no last item.
   */
  public Range(T first, T last) {
    Contract.isTrue("first <= last", (first == null) || (last == null) || (first.compareTo(last) <= 0));
    this.first = first;
    this.last = last;
  }

  /**
   * Create a new {@link Range} which is really a copy of the given one.
   *
   * @param templateRange Range to be copied. Must not be <c>null</c>.
   */
  public Range(Range<T> templateRange) {
    Contract.isNotNull("templateRange", templateRange);
    this.first = templateRange.first;
    this.last = templateRange.last;
  }

  /**
   * Generic method that makes it easier to create @{link Range}s.
   *
   * Example:<pre> Range&lt;String&gt; p = Range.of("aaa", "zzz")</pre>
   *
   * @param <T> Type of elements. These must be comparable.
   * @param f First element value. Can be <c>null</c> to indicate there is no first item.
   * @param l Last element value. Can be <c>null</c> to indicate there is no last item but otherwise must come after <c>f</c>.
   *
   * @return A pair of the first and second value provided.
   */
  public static <T extends Comparable<T>> Range<T> of(T f, T l) {
    return new Range<>(f, l);
  }

  // </editor-fold>
  // <editor-fold desc="Properties">

  private final T first;
  private final T last;

  /**
   * @return First element of this range. Can be <c>null</c> to indicate there is no first item.
   */
  public T getFirst() {
    return this.first;
  }

  /**
   * @return Last element of this range. Can be <c>null</c> to indicate there is no last item.
   */
  public T getLast() {
    return this.last;
  }

  // </editor-fold>
  // <editor-fold desc="Comparison">

  @Override
  public int hashCode() {
    int hash = 7;
    hash = (37 * hash) + (this.first != null ? this.first.hashCode() : 0);
    hash = (37 * hash) + (this.last != null ? this.last.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (!(other instanceof Range)) {
      return false;
    }
    Range<?> otherRange = (Range<?>) other;
    return Checks.areEqual(this.first, otherRange.first) && Checks.areEqual(this.last, otherRange.last);
  }

  /**
   *  Check whether this range's {@link #getLast()} comes strictly before the given element.
   *
   *  @param element The element to check.
   *  @return <c>true</c> if <c>element &gt; this.getLast()</c>. Always <c>false</c> if <c>this.getLast() == null</c>.
   */
  public boolean strictlyBefore(T element) {
    Contract.isNotNull("element", element);
    if (this.last == null) {
      return false;
    }
    return this.last.compareTo(element) < 0;
  }

  /**
   *  Check whether this range's {@link #getFirst()} comes strictly after the given element.
   *
   *  @param element The element to check.
   *  @return <c>true</c> if <c>element &lt; this.getFirst()</c>. Always <c>false</c> if <c>this.getFirst() == null</c>.
   */
  public boolean strictlyAfter(T element) {
    Contract.isNotNull("element", element);
    if (this.first == null) {
      return false;
    }
    return this.first.compareTo(element) > 0;
  }

  /**
   *  Check whether an element falls in the range.
   *
   *  @param element The element to check.
   *  @return <c>true</c> if <c>element</c> falls within the current range.
   */
  public boolean contains(T element) {
    Contract.isNotNull("element", element);
    if (this.first == null) {
      if (this.last == null) {
        return true;
      }
      return (element.compareTo(this.last) <= 0);
    }
    else if (this.last == null) {
      return (element.compareTo(this.first) >= 0);
    }
    return (element.compareTo(this.first) >= 0) && (element.compareTo(this.last) <= 0);
  }

  /**
   * Whether the range comes before, after or equal to the provided range.
   *
   * @param o Range to compare against.
   *
   * @return -1 if the current range's {@link #getFirst()} comes before {@code o#getFirst())} or if both ranges have the same first elements but
   *            {@link #getLast()} comes before {@code o#getLast()}, 0 if both ranges are exactly equal and +1 otherwise.
   */
  @Override
  public int compareTo(Range<T> o) {
    if (this.first == null) {
      if (o.first != null) {
        return -1;
      }
    }
    else if (o.first == null) {
      return +1;
    }
    else {
      int cmp = this.first.compareTo(o.first);
      if (cmp != 0) {
        return cmp;
      }
    }
    if (this.last == null) {
      if (o.last == null) {
        return 0;
      }
      return +1;
    }
    else if (o.last == null) {
      return -1;
    }
    return this.last.compareTo(o.last); // -1 if this.last comes first, +1 if o.last comes first, 0 otherwise
  }

  // </editor-fold>
  // <editor-fold desc="Debugging">

  /**
   * @return A human-readable representation.
   */
  @Override
  public String toString() {
    if (this.first == null) {
      if (this.last == null) {
        return "range [<unbounded> - <unbounded>]";
      }
      return String.format("range [<unbounded> - %s]", this.last);
    }
    else if (this.last == null) {
      return String.format("range [%s - <unbounded>]", this.first);
    }
    return String.format("[%s - %s]", this.first, this.last);
  }

  // </editor-fold>
}
