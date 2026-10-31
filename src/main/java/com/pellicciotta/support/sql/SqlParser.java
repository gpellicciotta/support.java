// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.pellicciotta.support.exceptions.FormatException;
import com.pellicciotta.support.text.SyntaxToken;
import com.pellicciotta.support.text.SyntaxType;
import com.pellicciotta.support.utils.Order;

/**
 *  Class capable of dividing a series of characters into a series of SQL tokens and further into a higher-level structure.
 */
class SqlParser {
  public SqlParser(List<SyntaxToken> tokens) {
    this.tokens = tokens.stream().filter((tok) -> (tok.getSyntaxType() != SqlSyntaxType.WHITESPACE) && (tok.getSyntaxType() != SqlSyntaxType.LINE_COMMENT) && (tok.getSyntaxType() != SqlSyntaxType.BLOCK_COMMENT)).collect(Collectors.toList());
  }

  private final List<SyntaxToken> tokens;
  private final static SqlGenerator sqlGen = new SqlGenerator();

  public static class SqlPart {
    public SqlPart(List<SyntaxToken> tokens, int startIdx, int endIdx) {
      this.tokenSource = tokens;
      this.firstTokenIndex = startIdx;
      this.lastTokenIndex = endIdx;
    }

    /** The token list this part refers to. */
    protected final List<SyntaxToken> tokenSource;
    /** Index in {@code tokenSource} of the first token of this part. */
    protected final int firstTokenIndex;
    /** Index in {@code tokenSource} of the last token of this part (inclusive). */
    protected final int lastTokenIndex;

    @Override
    public String toString() {
      return sqlGen.generate(this.tokenSource, this.firstTokenIndex, this.lastTokenIndex - this.firstTokenIndex + 1);
    }
  }

  public static class SelectClause extends SqlPart {
    public SelectClause(List<SyntaxToken> tokens, int keywordIdx, int endIdx) {
      super(tokens, keywordIdx, endIdx);
    }
  }

  public static class FromClause extends SqlPart {
    public FromClause(List<SyntaxToken> tokens, int keywordIdx, int endIdx) {
      super(tokens, keywordIdx, endIdx);
    }
  }

  public static class WhereClause extends SqlPart {
    public WhereClause(List<SyntaxToken> tokens, int keywordIdx, int endIdx) {
      super(tokens, keywordIdx, endIdx);
    }
  }

  public static class GroupByClause extends SqlPart {
    public GroupByClause(List<SyntaxToken> tokens, int keywordIdx, int endIdx) {
      super(tokens, keywordIdx, endIdx);
    }
  }

  public static class HavingClause extends SqlPart {
    public HavingClause(List<SyntaxToken> tokens, int keywordIdx, int endIdx) {
      super(tokens, keywordIdx, endIdx);
    }
  }

  public SelectClause findSelectClause() {
    return this.findSelectClause(0);
  }

  public SelectClause findSelectClause(int startOffset) {
    for (int i = startOffset; i < this.tokens.size(); i++) {
      SyntaxToken tok = this.tokens.get(i);
      if ((tok.getSyntaxType() == SqlSyntaxType.KEYWORD) && ("SELECT".equalsIgnoreCase(tok.getText()))) {
        SelectClause select = this.matchSelectClause(i);
        if (select != null) {
          return select;
        }
      }
    }
    return null;
  }

  public FromClause findFromClause() {
    return this.findFromClause(0);
  }

  public FromClause findFromClause(int startOffset) {
    for (int i = startOffset; i < this.tokens.size(); i++) {
      SyntaxToken tok = this.tokens.get(i);
      if ((tok.getSyntaxType() == SqlSyntaxType.KEYWORD) && ("FROM".equalsIgnoreCase(tok.getText()))) {
        FromClause from = this.matchFromClause(i);
        if (from != null) {
          return from;
        }
      }
    }
    return null;
  }

  public WhereClause findWhereClause() {
    return this.findWhereClause(0);
  }

  public WhereClause findWhereClause(int startOffset) {
    for (int i = startOffset; i < this.tokens.size(); i++) {
      SyntaxToken tok = this.tokens.get(i);
      if ((tok.getSyntaxType() == SqlSyntaxType.KEYWORD) && ("WHERE".equalsIgnoreCase(tok.getText()))) {
        WhereClause where = this.matchWhereClause(i);
        if (where != null) {
          return where;
        }
      }
    }
    return null;
  }

  public GroupByClause findGroupByClause() {
    return this.findGroupByClause(0);
  }

  public GroupByClause findGroupByClause(int startOffset) {
    for (int i = startOffset; i < this.tokens.size(); i++) {
      SyntaxToken tok = this.tokens.get(i);
      if ((tok.getSyntaxType() == SqlSyntaxType.KEYWORD) && ("GROUP BY".equalsIgnoreCase(tok.getText()))) {
        GroupByClause groupBy = this.matchGroupByClause(i);
        if (groupBy != null) {
          return groupBy;
        }
      }
    }
    return null;
  }

  public HavingClause findHavingClause() {
    return this.findHavingClause(0);
  }

  public HavingClause findHavingClause(int startOffset) {
    for (int i = startOffset; i < this.tokens.size(); i++) {
      SyntaxToken tok = this.tokens.get(i);
      if ((tok.getSyntaxType() == SqlSyntaxType.KEYWORD) && ("HAVING".equalsIgnoreCase(tok.getText()))) {
        HavingClause having = this.matchHavingClause(i);
        if (having != null) {
          return having;
        }
      }
    }
    return null;
  }

  public static class OrderBySpec extends SqlPart {
    public OrderBySpec(List<SyntaxToken> tokens, int identifierIndex, int orderKeywordIndex) {
      super(tokens, identifierIndex, orderKeywordIndex);
      this.identifierIndex = identifierIndex;
      this.orderKeywordIndex = orderKeywordIndex;
    }

    /** Index of the token holding the sort column identifier. */
    protected final int identifierIndex;
    /** Index of the token holding the order keyword ({@code ASC} or {@code DESC}). */
    protected final int orderKeywordIndex;

    public String getIdentifier() {
      return this.tokenSource.get(this.identifierIndex).getText();
    }

    public Order getOrder() {
      if (this.tokenSource.get(this.orderKeywordIndex).getText().equalsIgnoreCase("DESC")) {
        return Order.DESC;
      }
      else {
        return Order.ASC;
      }
    }
  }

  public static class OrderByClause extends SqlPart {
    public OrderByClause(List<SyntaxToken> tokens, int keywordIdx, OrderBySpec[] orderBySpecs) {
      super(tokens, keywordIdx, orderBySpecs[orderBySpecs.length - 1].lastTokenIndex);
      this.orderByKeywordIndex = keywordIdx;
      this.orderBySpecs = orderBySpecs;
    }

    /** Index of the {@code ORDER BY} keyword token. */
    protected final int orderByKeywordIndex;
    /** The individual sort specifications of this clause. */
    protected final OrderBySpec[] orderBySpecs;
  }

  public OrderByClause findOrderByClause() {
    return this.findOrderByClause(0);
  }

  public OrderByClause findOrderByClause(int startOffset) {
    for (int i = startOffset; i < this.tokens.size(); i++) {
      SyntaxToken tok = this.tokens.get(i);
      if ((tok.getSyntaxType() == SqlSyntaxType.KEYWORD) && ("ORDER BY".equalsIgnoreCase(tok.getText()))) {
        OrderByClause orderBy = this.matchOrderByClause(i);
        if (orderBy != null) {
          return orderBy;
        }
      }
    }
    return null;
  }

  public SelectClause matchSelectClause(int offset) {
    int len = this.matchClause(offset, "SELECT", "FROM", "WHERE", "GROUP BY", "ORDER BY", "LIMIT");
    if (len == 0) {
      return null;
    }
    return new SelectClause(this.tokens, offset, offset + len - 1);
  }

  public FromClause matchFromClause(int offset) {
    int len = this.matchClause(offset, "FROM", "WHERE", "GROUP BY", "ORDER BY", "LIMIT");
    if (len == 0) {
      return null;
    }
    return new FromClause(this.tokens, offset, offset + len - 1);
  }

  public WhereClause matchWhereClause(int offset) {
    int len = this.matchClause(offset, "WHERE", "GROUP BY", "ORDER BY", "LIMIT");
    if (len == 0) {
      return null;
    }
    return new WhereClause(this.tokens, offset, offset + len - 1);
  }

  public GroupByClause matchGroupByClause(int offset) {
    int len = this.matchClause(offset, "GROUP BY", "HAVING", "ORDER BY", "LIMIT");
    if (len == 0) {
      return null;
    }
    return new GroupByClause(this.tokens, offset, offset + len - 1);
  }

  public HavingClause matchHavingClause(int offset) {
    int len = this.matchClause(offset, "HAVING", "ORDER BY", "LIMIT");
    if (len == 0) {
      return null;
    }
    return new HavingClause(this.tokens, offset, offset + len - 1);
  }

  public int matchClause(int offset, String keyword, String... endKeywords) {
    int startOffset = offset;
    while (this.matchWhitespaceOrComments(offset)) {
      offset += 1;
    }
    if (!this.matchKeyword(offset,keyword)) {
      return 0;
    }
    int keywordIdx = offset;
    offset += 1;
    while (offset < this.tokens.size()) {
      SyntaxToken tok = this.tokens.get(offset);
      SyntaxType tt = tok.getSyntaxType();
      if ((endKeywords.length > 0) && tt.equals(SqlSyntaxType.KEYWORD)) {
        for (String endKeyword : endKeywords) {
          if (endKeyword.equalsIgnoreCase(tok.getText())) {
            return (offset - startOffset);
          }
        }
      }
      offset += 1;
    }
    return (offset - startOffset);
  }

  public OrderByClause matchOrderByClause(int offset) {
    int startOffset = offset;
    while (this.matchWhitespaceOrComments(offset)) {
      offset += 1;
    }
    if (!this.matchKeyword(offset,"ORDER BY")) {
      return null;
    }
    int keywordIdx = offset;
    offset += 1;
    while (this.matchWhitespaceOrComments(offset)) {
      offset += 1;
    }
    List<OrderBySpec> orderBys = new ArrayList<>();
    OrderBySpec orderBy = this.matchOrderBySpec(offset);
    if (orderBy == null) {
      throw new FormatException(this.tokens, "Failed to parse ORDER BY: found token '%s' at index %d while an IDENTIFIER was expected", this.tokens.get(offset), offset);
    }
    orderBys.add(orderBy);
    offset = orderBy.lastTokenIndex + 1;
    while (this.matchWhitespaceOrComments(offset)) {
      offset += 1;
    }
    while (this.matchComma(offset)) {
      offset += 1;
      while (this.matchWhitespaceOrComments(offset)) {
        offset += 1;
      }
      orderBy = this.matchOrderBySpec(offset);
      if (orderBy == null) {
        throw new FormatException(this.tokens, "Failed to parse ORDER BY: found token '%s' at index %d while an IDENTIFIER was expected", this.tokens.get(offset), offset);
      }
      orderBys.add(orderBy);
      offset = orderBy.lastTokenIndex + 1;
    }
    return new OrderByClause(this.tokens, keywordIdx, orderBys.toArray(new OrderBySpec[0]));
  }

  public OrderBySpec matchOrderBySpec(int offset) {
    int startOffset = offset;
    // Whitespace:
    while (this.matchWhitespaceOrComments(offset)) {
      offset += 1;
    }
    // Keyword:
    if (!this.matchTokenType(offset, SqlSyntaxType.IDENTIFIER)) {
      return null;
    }
    int idTokenIdx = offset;
    offset += 1;
    // Whitespace
    while (this.matchWhitespaceOrComments(offset)) {
      offset += 1;
    }
    // ASC or DESC
    if (!this.matchKeyword(offset, "ASC") && !this.matchKeyword(offset, "DESC")) {
      return null;
    }
    int keywordId = offset;
    return new OrderBySpec(this.tokens, idTokenIdx, keywordId);
  }

  public boolean matchOperator(int offset, String operator) {
    if (offset >= this.tokens.size()) {
      return false;
    }
    SyntaxToken tok = this.tokens.get(offset);
    SyntaxType tt = tok.getSyntaxType();
    return tt.equals(SqlSyntaxType.KEYWORD) && operator.equalsIgnoreCase(tok.getText());
  }

  public boolean matchKeyword(int offset, String keyword) {
    if (offset >= this.tokens.size()) {
      return false;
    }
    SyntaxToken tok = this.tokens.get(offset);
    SyntaxType tt = tok.getSyntaxType();
    return tt.equals(SqlSyntaxType.KEYWORD) && keyword.equalsIgnoreCase(tok.getText());
  }

  public boolean matchTokenType(int offset, SyntaxType expectedType) {
    if (offset >= this.tokens.size()) {
      return false;
    }
    SyntaxType tt = this.tokens.get(offset).getSyntaxType();
    return tt.equals(expectedType);
  }

  public boolean matchComma(int offset) {
    if (offset >= this.tokens.size()) {
      return false;
    }
    SyntaxType tt = this.tokens.get(offset).getSyntaxType();
    return tt.equals(SqlSyntaxType.COMMA);
  }

  public boolean matchWhitespaceOrComments(int offset) {
    if (offset >= this.tokens.size()) {
      return false;
    }
    SyntaxType tt = this.tokens.get(offset).getSyntaxType();
    return (tt.equals(SqlSyntaxType.WHITESPACE) || tt.equals(SqlSyntaxType.LINE_COMMENT) || tt.equals(SqlSyntaxType.BLOCK_COMMENT));
  }
}
