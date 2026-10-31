// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;


/**
 * A generic pair class allowing the storage of a tuple of two loosely related items.
 */
public class Pair<T, S> implements Comparable<Pair<T, S>> {

  // <editor-fold desc="Construction">

  /**
   * Create a new {@link Pair}.
   *
   * @param first First value.
   * @param second Second value.
   */
  public Pair(T first, S second) {
    this.first = first;
    this.second = second;
  }

  /**
   * Create a new {@link Pair} which is really a copy of the given one.
   *
   * @param templatePair Pair to be copied. Must not be <c>null</c>.
   */
  public Pair(Pair<T, S> templatePair) {
    Contract.isNotNull("templatePair", templatePair);
    this.first = templatePair.first;
    this.second = templatePair.second;
  }

  /**
   * Generic method that makes it easier to create {@link Pair}s.
   *
   * Example:<pre> Pair&lt;String, Integer&gt; p = Pair.of("foo", 132)</pre>
   *
   * @param <A> Type of first element of the pair.
   * @param <B> Type of the second element of the pair.
   * @param a First element value.
   * @param b Second element value.
   *
   * @return A pair of the first and second value provided.
   */
  public static <A, B> Pair<A, B> of(A a, B b) {
    return new Pair<>(a, b);
  }

  // </editor-fold>
  // <editor-fold desc="Properties">

  private final T first;
  private final S second;

  /**
   * @return First part of pair.
   */
  public T getFirst() {
    return this.first;
  }

  /**
   * @return Second part of pair.
   */
  public S getSecond() {
    return this.second;
  }

  // </editor-fold>
  // <editor-fold desc="Comparison">

  @Override
  public int hashCode() {
    int hash = 7;
    hash = (37 * hash) + (this.first != null ? this.first.hashCode() : 0);
    hash = (37 * hash) + (this.second != null ? this.second.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (!(other instanceof Pair)) {
      return false;
    }
    Pair<?, ?> otherPair = (Pair<?, ?>) other;
    return Checks.areEqual(this.first, otherPair.first) && Checks.areEqual(this.second, otherPair.second);
  }

  /**
   * Whether the pair is alphabetically before, after or equal to the provided pair. Comparison will be based string representation
   * of the pair.
   *
   * @param o Pair to compare against.
   *
   * @return A negative number if this object's pair is before the to be compared pair, 0 if both versions are equal and a positive
   *         number if this object's pair is after the to be compared pair
   */
  @Override
  public int compareTo(Pair<T, S> o) {
    return this.toString().compareTo(o.toString());
  }

  // </editor-fold>
  // <editor-fold desc="Debugging">

  /**
   * @return A human-readable representation.
   */
  @Override
  public String toString() {
    return "(" + this.first + ", " + this.second + ")";
  }

  // </editor-fold>
}
