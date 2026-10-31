// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.text;

/**
 *  Functional interface to be implemented by types that can participate in a tokenization process.
 */
@FunctionalInterface
public
interface TokenMatcher {
  // IDEA: or have a separate MatchContext with members 'source', 'currentOffset' but also '[get|set]CurrentState' and 'addToken()' ?

  /**
   *  Try to match a sequence of characters starting at <c>startOffset</c>.
   *
   *  @param source The input source to match against.
   *  @param startOffset The first character to match against.
   *  @param length The max. number of characters to match.
   *
   *  @return Either <c>null</c> if no match succeeded or else a {@link SyntaxToken} representing a contiguous sequence of
   *          input characters in <c>source</c>, with {@code Token#getStartPosition == startOffset}.
   */
  SyntaxToken match(CharSequence source, int startOffset, int length);
}
