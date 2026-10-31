// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.parsing;

/**
 *  Functional interface to be implemented by types that can participate in a matching part of a {@link CharSequence}.
 */
@FunctionalInterface
public interface CharSequenceMatcher {
  /**
   *  Try to match a substring starting at startIndex.
   *
   *  @param input The full sequence of characters.
   *  @param startIndex Index of the first character to be considered for matching.
   *  @param length The max. number of characters to be matched.
   *  @return The number of matched characters, starting at startIndex. Maybe be zero to indicate that nothing can be matched.
   */
  int match(CharSequence input, int startIndex, int length);
}
