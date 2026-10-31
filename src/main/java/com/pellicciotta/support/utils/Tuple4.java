// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;


/**
 * A generic tuple class allowing the storage of a tuple of four loosely related items.
 */
public class Tuple4<T, S, U, V> implements Comparable<Tuple4<T, S, U, V>> {

  // <editor-fold desc="Construction">

  /**
   * Create a new {@link Tuple4}.
   *
   * @param first First value.
   * @param second Second value.
   * @param third Third value.
   * @param fourth Fourth value.
   */
  public Tuple4(T first, S second, U third, V fourth) {
    this.first = first;
    this.second = second;
    this.third = third;
    this.fourth = fourth;
  }

  /**
   * Create a new {@link Tuple4} which is really a copy of the given one.
   *
   * @param templateTuple Tuple to be copied. Must not be <c>null</c>.
   */
  public Tuple4(Tuple4<T, S, U, V> templateTuple) {
    Contract.isNotNull("templateTuple", templateTuple);
    this.first = templateTuple.first;
    this.second = templateTuple.second;
    this.third = templateTuple.third;
    this.fourth = templateTuple.fourth;
  }

  /**
   * Generic method that makes it easier to create {@link Tuple4}s.
   *
   * Example:<pre> Tuple&lt;String, Integer, byte[]&gt; p = Tuple3.of("foo", 132, new byte[] { 0xff })</pre>
   *
   * @param <A> Type of first element of the tuple.
   * @param <B> Type of the second element of the tuple.
   * @param <C> Type of the third element of the tuple.
   * @param <D> Type of the fourth element of the tuple.
   *
   * @param a First element value.
   * @param b Second element value.
   * @param c Third element value.
   * @param d Fourth element value.
   *
   * @return A tuple of the values provided.
   */
  public static <A, B, C, D> Tuple4<A, B, C, D> of(A a, B b, C c, D d) {
    return new Tuple4<>(a, b, c, d);
  }

  // </editor-fold>
  // <editor-fold desc="Properties">

  private final T first;
  private final S second;
  private final U third;
  private final V fourth;

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

  /**
   * @return Third part of tuple.
   */
  public U getThird() {
    return this.third;
  }

  /**
   * @return Fourth part of tuple.
   */
  public V getFourth() {
    return this.fourth;
  }

  // </editor-fold>
  // <editor-fold desc="Comparison">

  @Override
  public int hashCode() {
    int hash = 7;
    hash = (37 * hash) + (this.first != null ? this.first.hashCode() : 0);
    hash = (37 * hash) + (this.second != null ? this.second.hashCode() : 0);
    hash = (37 * hash) + (this.third != null ? this.third.hashCode() : 0);
    hash = (37 * hash) + (this.fourth != null ? this.fourth.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (!(other instanceof Tuple4)) {
      return false;
    }
    Tuple4<?, ?, ?, ?> otherTuple = (Tuple4<?, ?, ?, ?>) other;
    return Checks.areEqual(this.first, otherTuple.first)
        && Checks.areEqual(this.second, otherTuple.second)
        && Checks.areEqual(this.third, otherTuple.third)
        && Checks.areEqual(this.fourth, otherTuple.fourth);
  }

  /**
   * Whether the tuple is alphabetically before, after or equal to the provided tuple.
   * Comparison will be based string representation of the tuple.
   *
   * @param o Pair to compare against.
   *
   * @return A negative number if this object's tuple is before the to be compared tuple, 0 if both versions are equal and a positive
   *         number if this object's tuple is after the to be compared tuple
   */
  @Override
  public int compareTo(Tuple4<T, S, U, V> o) {
    return this.toString().compareTo(o.toString());
  }

  // </editor-fold>
  // <editor-fold desc="Debugging">

  /**
   * @return A human-readable representation.
   */
  @Override
  public String toString() {
    return "(" + this.first + ", " + this.second + ", " + this.third + ", " + this.fourth + ")";
  }

  // </editor-fold>
}
