// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.parsing;

/**
 *  Functional interface to be implemented by types that can participate in a matching process.
 *
 *  @param <T> The type of input elements.
 */
@FunctionalInterface
public interface Matcher<T> {
  /**
   *  Try to match one or more items from the input stream.
   *
   *  @param ctx The match context providing access to the input stream, and allowing the recording of a match or failure to match.
   *  @return Whether a match was found.
   */
  boolean match(MatchContext ctx);
}
