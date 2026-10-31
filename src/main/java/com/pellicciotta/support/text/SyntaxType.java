// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.text;

import java.util.Objects;

import com.pellicciotta.support.utils.Contract;

/**
 *  A glorified integer value.
 *  <p>
 *  Syntax types are considered equal if their ordinal value is equal.</p>
 */
public class SyntaxType
{
  /** Token for source character(s) that don't match anything. */
  public final static SyntaxType ERROR = new SyntaxType(0, "error");

  /**
   *  Create a token type with given ID and name.
   *
   *  @param ordinal The unique ID of this token type. This should be unique among all {@link SyntaxType}s in the whole JVM.
   *                 ID zero is reserved as a general error token.
   *  @param name The descriptive name of this token type.
   */
  public SyntaxType(int ordinal, String name) {
    Contract.isNotNull("name", name);
    this.ordinal = ordinal;
    this.name = name;
  }

  protected final int ordinal;
  protected final String name;

  public String getName() { return this.name; }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if ((o == null) || (this.getClass() != o.getClass())) {
      return false;
    }
    SyntaxType tokenType = (SyntaxType) o;
    return this.ordinal == tokenType.ordinal;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.ordinal);
  }

  @Override
  public String toString() {
    return "" + this.ordinal + ":" + this.name;
  }
}
