// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;


/**
 * A generic tuple class allowing the storage of a tuple of two loosely related items.
 */
public class Tuple2<T, S> implements Comparable<Tuple2<T, S>> {

  // <editor-fold desc="Construction">

  /**
   * Create a new {@link Tuple2}.
   *
   * @param first First value.
   * @param second Second value.
   */
  public Tuple2(T first, S second) {
    this.first = first;
    this.second = second;
  }

  /**
   * Create a new {@link Tuple2} which is really a copy of the given one.
   *
   * @param templateTuple Tuple to be copied. Must not be <c>null</c>.
   */
  public Tuple2(Tuple2<T, S> templateTuple) {
    Contract.isNotNull("templateTuple", templateTuple);
    this.first = templateTuple.first;
    this.second = templateTuple.second;
  }

  /**
   * Generic method that makes it easier to create {@link Tuple2}s.
   *
   * Example:<pre> Tuple&lt;String, Integer&gt; p = Tuple.of("foo", 132)</pre>
   *
   * @param <A> Type of first element of the tuple.
   * @param <B> Type of the second element of the tuple.
   * @param a First element value.
   * @param b Second element value.
   *
   * @return A tuple of the first and second value provided.
   */
  public static <A, B> Tuple2<A, B> of(A a, B b) {
    return new Tuple2<>(a, b);
  }

  // </editor-fold>
  // <editor-fold desc="Properties">

  private final T first;
  private final S second;

  /**
   * @return First part of tuple.
   */
  public T getFirst() {
    return this.first;
  }

  /**
   * @return Second part of tuple.
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
    if (!(other instanceof Tuple2)) {
      return false;
    }
    Tuple2<?, ?> otherTuple = (Tuple2<?, ?>) other;
    return Checks.areEqual(this.first, otherTuple.first) && Checks.areEqual(this.second, otherTuple.second);
  }

  /**
   * Whether the tuple is alphabetically before, after or equal to the provided tuple. Comparison will be based string representation
   * of the tuple.
   *
   * @param o Tuple to compare against.
   *
   * @return A negative number if this object's tuple is before the to be compared tuple, 0 if both versions are equal and a positive
   *         number if this object's tuple is after the to be compared tuple
   */
  @Override
  public int compareTo(Tuple2<T, S> o) {
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
