// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.diff;

import java.util.Objects;

/**
 *  A pair of indexes indicating a matching item in 2 sequences.
 */
public class MatchPair
{
  public MatchPair(int indexA, int indexB) {
    this.indexA = indexA;
    this.indexB = indexB;
  }

  final int indexA;

  /**
   *  @return Index in the first stream or sequence.
   */
  public int getIndexA() {
    return this.indexA;
  }

  final int indexB;

  /**
   *  @return Index in the second stream or sequence.
   */
  public int getIndexB() {
    return this.indexB;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MatchPair)) return false;
    MatchPair matchPair = (MatchPair) o;
    return this.indexA == matchPair.indexA && this.indexB == matchPair.indexB;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.indexA, this.indexB);
  }

  @Override
  public String toString() {
    return String.format("A[%d] = B[%d]", this.indexA, this.indexB);
  }
}
