// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.text;

import java.util.Objects;

import com.pellicciotta.support.utils.Contract;

/**
 *  The common interface for all elements that can be categorized according to a {@link SyntaxType}.
 */
public interface SyntaxElement
{
  /**
   *  Type of syntax represented by the current element.
   */
  SyntaxType getSyntaxType();
}
