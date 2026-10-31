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

  /** Token type for whitespace. */
  public static final SqlSyntaxType WHITESPACE = new SqlSyntaxType("whitespace");
  /** Token type for a SQL keyword. */
  public static final SqlSyntaxType KEYWORD = new SqlSyntaxType("keyword");
  /** Token type for a SQL identifier. */
  public static final SqlSyntaxType IDENTIFIER = new SqlSyntaxType("identifier");
  /** Token type for a SQL named parameter. */
  public static final SqlSyntaxType NAMED_PARAMETER = new SqlSyntaxType("named-parameter");
  /** Token type for a SQL positional parameter ({@code ?}). */
  public static final SqlSyntaxType POSITIONAL_PARAMETER = new SqlSyntaxType("positional-parameter");
  /** Token type for a SQL operator. */
  public static final SqlSyntaxType OPERATOR = new SqlSyntaxType("operator");
  /** Token type for a SQL comma. */
  public static final SqlSyntaxType COMMA = new SqlSyntaxType("comma");
  /** Token type for a SQL left parenthesis. */
  public static final SqlSyntaxType LEFT_PAREN = new SqlSyntaxType("left-paren");
  /** Token type for a SQL right parenthesis. */
  public static final SqlSyntaxType RIGHT_PAREN = new SqlSyntaxType("right-paren");
  /** Token type for a SQL string literal. */
  public static final SqlSyntaxType STRING_LITERAL = new SqlSyntaxType("string-literal");
  /** Token type for a SQL numeric literal. */
  public static final SqlSyntaxType NUMBER_LITERAL = new SqlSyntaxType("number-literal");
  /** Token type for a SQL line comment. */
  public static final SqlSyntaxType LINE_COMMENT = new SqlSyntaxType("line-comment");
  /** Token type for a SQL block comment. */
  public static final SqlSyntaxType BLOCK_COMMENT = new SqlSyntaxType("block-comment");

  // </editor-fold>
  // <editor-fold desc="Node Constants">

  /** Node type for a SQL SELECT clause. */
  public static final SqlSyntaxType SELECT_CLAUSE = new SqlSyntaxType("select-clause");
  /** Node type for a SQL FROM clause. */
  public static final SqlSyntaxType FROM_CLAUSE = new SqlSyntaxType("from-clause");
  /** Node type for a SQL WHERE clause. */
  public static final SqlSyntaxType WHERE_CLAUSE = new SqlSyntaxType("where-clause");
  /** Node type for a SQL GROUP BY clause. */
  public static final SqlSyntaxType GROUP_BY_CLAUSE = new SqlSyntaxType("group-by-clause");
  /** Node type for a SQL HAVING clause. */
  public static final SqlSyntaxType HAVING_CLAUSE = new SqlSyntaxType("having-clause");
  /** Node type for a SQL single ORDER BY sort specification. */
  public static final SqlSyntaxType ORDER_BY_SPEC = new SqlSyntaxType("order-by-spec");
  /** Node type for a SQL ORDER BY clause. */
  public static final SqlSyntaxType ORDER_BY_CLAUSE = new SqlSyntaxType("order-by-clause");
  /** Node type for a SQL LIMIT clause. */
  public static final SqlSyntaxType LIMIT_CLAUSE = new SqlSyntaxType("limit-clause");

  // </editor-fold>
}
