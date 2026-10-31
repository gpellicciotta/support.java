// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.sql;

import com.pellicciotta.support.text.SyntaxType;

/**
 *  A token as produced by the {@link SqlTokenizer}.
 */
final class SqlSyntaxType extends SyntaxType {
  // <editor-fold desc="Construction">

  private static int idGenerator = 1000;

  private SqlSyntaxType(String name) {
    super(idGenerator++, name);
  }

  // </editor-fold>
  // <editor-fold desc="Token Constants">

  public static final SqlSyntaxType WHITESPACE = new SqlSyntaxType("whitespace");
  public static final SqlSyntaxType KEYWORD = new SqlSyntaxType("keyword");
  public static final SqlSyntaxType IDENTIFIER = new SqlSyntaxType("identifier");
  public static final SqlSyntaxType NAMED_PARAMETER = new SqlSyntaxType("named-parameter");
  public static final SqlSyntaxType POSITIONAL_PARAMETER = new SqlSyntaxType("positional-parameter");
  public static final SqlSyntaxType OPERATOR = new SqlSyntaxType("operator");
  public static final SqlSyntaxType COMMA = new SqlSyntaxType("comma");
  public static final SqlSyntaxType LEFT_PAREN = new SqlSyntaxType("left-paren");
  public static final SqlSyntaxType RIGHT_PAREN = new SqlSyntaxType("right-paren");
  public static final SqlSyntaxType STRING_LITERAL = new SqlSyntaxType("string-literal");
  public static final SqlSyntaxType NUMBER_LITERAL = new SqlSyntaxType("number-literal");
  public static final SqlSyntaxType LINE_COMMENT = new SqlSyntaxType("line-comment");
  public static final SqlSyntaxType BLOCK_COMMENT = new SqlSyntaxType("block-comment");

  // </editor-fold>
  // <editor-fold desc="Node Constants">

  public static final SqlSyntaxType SELECT_CLAUSE = new SqlSyntaxType("select-clause");
  public static final SqlSyntaxType FROM_CLAUSE = new SqlSyntaxType("from-clause");
  public static final SqlSyntaxType WHERE_CLAUSE = new SqlSyntaxType("where-clause");
  public static final SqlSyntaxType GROUP_BY_CLAUSE = new SqlSyntaxType("group-by-clause");
  public static final SqlSyntaxType HAVING_CLAUSE = new SqlSyntaxType("having-clause");
  public static final SqlSyntaxType ORDER_BY_SPEC = new SqlSyntaxType("order-by-spec");
  public static final SqlSyntaxType ORDER_BY_CLAUSE = new SqlSyntaxType("order-by-clause");
  public static final SqlSyntaxType LIMIT_CLAUSE = new SqlSyntaxType("limit-clause");

  // </editor-fold>
}
