// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.text;

import java.util.Iterator;
import java.util.List;

import com.pellicciotta.support.utils.Contract;

/**
 *  A tokenizer implementation based on an ordered list of {@link TokenMatcher}s.
 */
public class Tokenizer {
  /**
   *  Create a tokenizer based on the given list of matchers.
   *
   *  @param matchers The ordered list of matchers.
   */
  public Tokenizer(List<TokenMatcher> matchers) {
    Contract.isNotEmpty("matchers", matchers);
    this.matchers = matchers.toArray(new TokenMatcher[0]);
  }

  /**
   *  Create a tokenizer based on the given list of matchers.
   *
   *  @param matchers The ordered list of matchers.
   */
  public Tokenizer(TokenMatcher... matchers) {
    Contract.isNotEmpty("matchers", matchers);
    this.matchers = matchers;
  }

  private final TokenMatcher[] matchers;

  /**
   *  Turn the input source into a sequence of tokens.
   *
   *  @param source The source to be tokenized.
   *
   *  @return The stream of tokens. Some tokens may have type {@link SyntaxType#ERROR} if nothing matched.
   */
  public Iterator<SyntaxToken> tokenize(CharSequence source) {
    return this.tokenize(source, 0, source.length());
  }

  /**
   *  Turn the input source into a sequence of tokens.
   *
   *  @param source The source to be tokenized.
   *  @param start The index of the first character to match.
   *  @param length The number of characters to maximally turn into tokens.
   *
   *  @return The stream of tokens. Some tokens may have type {@link SyntaxType#ERROR} if nothing matched.
   */
  public Iterator<SyntaxToken> tokenize(CharSequence source, int start, final int length) {
    final int[] offset = new int[] { start }; // Using array so we can change from within the lambda
    final CharSequence src = source;

    return new Iterator<SyntaxToken>() {
      @Override
      public boolean hasNext() {
        return (offset[0] < length);
      }

      @Override
      public SyntaxToken next() {
        if (offset[0] >= length) {
          return null;
        }
        for (TokenMatcher m : Tokenizer.this.matchers) {
          SyntaxToken t = m.match(source, offset[0], length);
          if (t != null) {
            offset[0] += t.getLength();
            return t;
          }
        }
        // Nothing matched, so consume one character as ERROR
        // TODO: check also upcoming characters and merge all non-matching ones into a single ERROR token
        offset[0] += 1;
        return new SyntaxToken(SyntaxType.ERROR, source, offset[0] - 1, 1);
      }
    };
  }
}
