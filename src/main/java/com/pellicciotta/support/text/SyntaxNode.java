// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.text;

import java.util.List;
import java.util.Objects;

import com.pellicciotta.support.utils.Slice;

/**
 *  A syntax node represents a syntactic construct that is represented within a certain slice of {@link SyntaxToken}s.
 */
public class SyntaxNode extends Slice<List<SyntaxToken>, SyntaxToken> implements SyntaxElement {
  public SyntaxNode(SyntaxType nodeType, List<SyntaxToken> source, int startIdx, int endIdx) {
    super(source, startIdx, endIdx);
    this.nodeType = nodeType;
  }

  protected final SyntaxType nodeType;

  @Override
  public SyntaxType getSyntaxType() {
    return this.nodeType;
  }

  /**
   *  Get a specific input token in the span of tokens covered by this syntax.
   *
   *  {@inheritDoc}
   */
  public SyntaxToken elementAt(int index) {
    if ((index < this.startIndex) || (index >= this.endIndex)) {
      throw new IndexOutOfBoundsException(index);
    }
    return this.source.get(index);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if ((o == null) || (this.getClass() != o.getClass())) {
      return false;
    }
    SyntaxNode token = (SyntaxNode) o;
    return (this.startIndex == token.startIndex) &&
           (this.endIndex == token.endIndex) &&
           this.nodeType.equals(token.nodeType) &&
           this.source.equals(token.source);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.nodeType, this.source, this.startIndex, this.endIndex);
  }

  @Override
  public String toString() {
    return String.format("syntax-node: { type: %s, offset: %d, length: %d }", this.nodeType, this.startIndex, this.getLength());
  }
}
