// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.parsing;

import java.util.Iterator;
import java.util.List;

import com.pellicciotta.support.utils.Contract;


/**
 *  A parser base-class designed to work together with a {@link Tokenizer}.
 */
public class Parser
{
  /**
   *  Create a parser based on the given list of matchers.
   *
   *  @param matchers The ordered list of matchers.
   */
  public Parser(List<Matcher> matchers) {
    Contract.isNotEmpty("matchers", matchers);
    this.matchers = matchers.toArray(new Matcher[0]);
  }

  /**
   *  Create a parser based on the given list of matchers.
   *
   *  @param matchers The ordered list of matchers.
   */
  public Parser(Matcher... matchers) {
    Contract.isNotEmpty("matchers", matchers);
    this.matchers = matchers;
  }

  private final Matcher[] matchers;


}
