// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.parsing;

/**
 *  The context for matching a substring of a sequence according to some rules.
 *  The context keeps track of the original sequence and the current position within the sequence.
 *  It also provides methods for easily checking next (and prior) positions, updating the current position
 *  and for recording a match.
 *
 *  @param <T> The type of input elements.
 *  @param <R> The type of result elements.
 */
public interface MatchContext<T, R> {
  // The original input items:
  int getStartIndex();
  int getLength();

  // Advancing (and retracting) through the input list:
  int getOffset();
  void setOffset(int nextIndex);
  T getNext();

  default T peekNext() {
    return this.peekNext(0);
  }

  T peekNext(int extraOffset);

  // Matching tokens:
  void recordMatch(R node);
}
