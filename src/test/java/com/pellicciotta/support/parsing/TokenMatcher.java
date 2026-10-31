// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.parsing;

import com.pellicciotta.support.text.SyntaxToken;

/**
 *  Functional interface to be implemented by types that can participate in a matching a {@link SyntaxToken}.
 */
@FunctionalInterface
public interface TokenMatcher {
  /**
   */
  SyntaxToken match(TokenMatchContext ctx);
}
