// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.sql;

import com.pellicciotta.support.text.*;

/**
 *  Class capable of dividing a series of characters into a series of SQL tokens.
 */
class SqlTokenizer extends Tokenizer {
  private static final TokenMatcher[] SQL_TOKEN_MATCHERS = new TokenMatcher[] {
    // Order is important!
    TokenMatchers.forPattern("\\s+", SqlSyntaxType.WHITESPACE),
    TokenMatchers.forLineComment("--", SqlSyntaxType.LINE_COMMENT),
    TokenMatchers.forBlockComment("/*", "*/", SqlSyntaxType.BLOCK_COMMENT),
    // The ASC? is important as doing AS|ASC will match AS and not ASC
    TokenMatchers.forPattern("(SELECT|FROM|ASC?|COUNT|DISTINCT|WHERE|ORDER +BY|GROUP +BY|HAVING|LIMIT|DESC|IS|NOT|NULL|LIKE)", SqlSyntaxType.KEYWORD),
    TokenMatchers.forPattern("([=]|[>]|[<]|[*]|[/]|[%]|[+]|[-]|AND|OR|NOT|>=|<=|<>|!=)", SqlSyntaxType.OPERATOR),
    TokenMatchers.forPattern("[:][a-zA-Z][a-zA-Z0-9]*([._-][a-zA-Z0-9]+)*", SqlSyntaxType.NAMED_PARAMETER),
    TokenMatchers.forString("?", SqlSyntaxType.POSITIONAL_PARAMETER),
    TokenMatchers.forString(",", SqlSyntaxType.COMMA),
    TokenMatchers.forString("(", SqlSyntaxType.LEFT_PAREN),
    TokenMatchers.forString(")", SqlSyntaxType.RIGHT_PAREN),
    TokenMatchers.forPattern("[nN]?([']([^']|[\\\\][']|[']['])*['])", SqlSyntaxType.STRING_LITERAL),
    TokenMatchers.forPattern("[+-]?[0-9]+([.][0-9]+)?([eE][+-]?[0-9]+)?", SqlSyntaxType.NUMBER_LITERAL),
    TokenMatchers.forPattern("([a-zA-Z][a-zA-Z0-9]*([._-][a-zA-Z0-9]+)*)|(\\[[a-zA-Z][a-zA-Z0-9]*([._-][a-zA-Z0-9]+)*\\])|([`][a-zA-Z][a-zA-Z0-9]*([._-][a-zA-Z0-9]+)*[`])|([\"][a-zA-Z][a-zA-Z0-9]*([._-][a-zA-Z0-9]+)*[\"])", SqlSyntaxType.IDENTIFIER)
  };

  /**
   *  Create a SQL tokenizer.
   */
  public SqlTokenizer() {
    super(SQL_TOKEN_MATCHERS);
  }
}
