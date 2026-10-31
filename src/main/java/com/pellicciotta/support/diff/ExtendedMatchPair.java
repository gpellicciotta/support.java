// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.diff;

final class ExtendedMatchPair extends MatchPair {
  public ExtendedMatchPair(int indexA, int indexB, Object val) {
    super(indexA, indexB);
    this.val = val;
  }

  final Object val;

  @Override
  public String toString() {
    return String.format("A[%d] = B[%d] = '%s'", this.indexA, this.indexB, this.val);
  }
}
