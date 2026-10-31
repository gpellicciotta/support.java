// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.text;

import java.util.Objects;

import com.pellicciotta.support.utils.Contract;
import com.pellicciotta.support.utils.Slice;

/**
 *  A token represents a contiguous region of characters in an input character sequence that has a syntactic purpose.
 *  <p>
 *  Tokens can have zero-length, in which case they don't actually represent input characters but some implied entity at a given offset (e.g. <c>INDENT</c> token in python).</p>
 */
public class SyntaxToken extends Slice<CharSequence, Character> implements SyntaxElement, Comparable<SyntaxToken> {
  /**
   *  Create a token covering a region of the source; its text is that region.
   *
   *  @param tokenType The token type.
   *  @param source The character sequence the token is a slice of.
   *  @param offset The index of the first covered character.
   *  @param length The number of covered characters.
   */
  public SyntaxToken(SyntaxType tokenType, CharSequence source, int offset, int length) {
    super(source, offset, offset + length);
    this.tokenType = tokenType;
    this.text = this.source.subSequence(offset, offset + length).toString();
  }

  /**
   *  Create a zero-length pseudo-token at an offset, with explicit text.
   *
   *  @param tokenType The token type.
   *  @param source The character sequence the token is positioned in.
   *  @param offset The position of the token.
   *  @param text The token text; must not be null.
   */
  public SyntaxToken(SyntaxType tokenType, CharSequence source, int offset, String text) {
    super(source, offset, offset);
    Contract.isNotNull("text", text);
    this.tokenType = tokenType;
    this.text = text;
  }

  /** The type of this token. */
  protected final SyntaxType tokenType;
  /** The token text. */
  protected final String text;

  @Override
  public SyntaxType getSyntaxType() {
    return this.tokenType;
  }

  /**
   *  Get a specific input source character within this token.
   *
   *  {@inheritDoc}
   */
  public Character elementAt(int index) {
    if ((index < this.startIndex) || (index >= this.endIndex)) {
      throw new IndexOutOfBoundsException(index);
    }
    return this.source.charAt(index);
  }

  /**
   *  @return The token text value, which can be literally a sub-sequence of {@link #getSource()} or some other value for <em>pseudo-tokens</em>.
   */
  public String getText() {
    return this.text;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if ((o == null) || (this.getClass() != o.getClass())) {
      return false;
    }
    SyntaxToken token = (SyntaxToken) o;
    return (this.startIndex == token.startIndex) &&
           (this.endIndex == token.endIndex) &&
           this.tokenType.equals(token.tokenType) &&
           this.source.equals(token.source) &&
           ((this.startIndex == this.endIndex) || Objects.equals(this.text, token.text)) /* Only compare text when length is zero: for pseudo-tokens */;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.tokenType, this.source, this.startIndex, this.endIndex);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int compareTo(SyntaxToken o) {
    if (this.source != o.source) {
      throw new IllegalArgumentException("Can only compare tokens that have the same source");
    }
    int cmp = Integer.compare(this.startIndex, o.startIndex);
    if (cmp != 0) {
      return cmp;
    }
    return Integer.compare(this.endIndex, o.endIndex);
  }

  @Override
  public String toString() {
    return String.format("token: { type: %s, offset: %d, length: %d, text: \"%s\" }", this.tokenType, this.startIndex, this.getLength(), this.text);
  }
}
