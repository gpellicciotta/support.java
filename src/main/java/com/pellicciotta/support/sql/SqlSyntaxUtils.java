// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.pellicciotta.support.exceptions.FormatException;
import com.pellicciotta.support.text.SyntaxToken;
import com.pellicciotta.support.utils.*;
import static com.pellicciotta.support.sql.SqlSyntaxType.*;
import static com.pellicciotta.support.sql.SqlSyntaxType.OPERATOR;

/**
 *  Utilities for working with and altering SQL syntax.
 */
public final class SqlSyntaxUtils {
  private SqlSyntaxUtils() {  }

  /**
   *  Normalize the SQL text: remove all comments and whitespace, uppercase all keywords and have a single space between tokens.
   *
   *  @param sqlStmt The SQL statement syntax to normalize.
   *
   *  @return The normalized version of <c>sqlStmt</c>.
   */
  public static String normalizeText(String sqlStmt) {
    Contract.isNotNull("sqlStmt", sqlStmt);

    SqlTokenizer tokenizer = new SqlTokenizer();
    List<SyntaxToken> tokens = CollectionUtils.listFromIterator(tokenizer.tokenize(sqlStmt)).stream().filter((tok) -> (tok.getSyntaxType() != SqlSyntaxType.WHITESPACE) && (tok.getSyntaxType() != SqlSyntaxType.LINE_COMMENT) && (tok.getSyntaxType() != SqlSyntaxType.BLOCK_COMMENT)).collect(Collectors.toList());
    SqlGenerator gen = new SqlGenerator();
    return gen.generate(tokens);
  }

  /**
   *  Add a <c>FROM</c>-expression an existing SQL statement.
   *
   *  @param baseSqlStmt The SQL statement syntax to add a <c>FROM</c>-expression to.
   *  @param extraFromExpression The <c>FROM</c>-expression to add (via <c>, </c>) to any existing <c>FROM</c>-expressions.
   *
   *  @return The normalized version of updated <c>baseSqlStmt</c>.
   */
  public static String addFromExpression(String baseSqlStmt, String extraFromExpression) {
    Contract.isNotNull("baseSqlStmt", baseSqlStmt);
    Contract.isNotNull("extraFromExpression", extraFromExpression);

    SqlTokenizer tokenizer = new SqlTokenizer();
    List<SyntaxToken> stmtTokens = CollectionUtils.listFromIterator(tokenizer.tokenize(baseSqlStmt)).stream().filter((tok) -> (tok.getSyntaxType() != SqlSyntaxType.WHITESPACE) && (tok.getSyntaxType() != SqlSyntaxType.LINE_COMMENT) && (tok.getSyntaxType() != SqlSyntaxType.BLOCK_COMMENT)).collect(Collectors.toList());
    SqlParser stmtParser = new SqlParser(stmtTokens);

    List<SyntaxToken> fromTokens = CollectionUtils.listFromIterator(tokenizer.tokenize(extraFromExpression)).stream().filter((tok) -> (tok.getSyntaxType() != SqlSyntaxType.WHITESPACE) && (tok.getSyntaxType() != SqlSyntaxType.LINE_COMMENT) && (tok.getSyntaxType() != SqlSyntaxType.BLOCK_COMMENT)).collect(Collectors.toList());

    SqlParser.FromClause from = stmtParser.findFromClause();
    if (from == null) {
      throw new FormatException(baseSqlStmt, "No FROM clause found");
    }
    SyntaxToken lt = from.tokenSource.get(from.lastTokenIndex);
    stmtTokens.add(from.lastTokenIndex + 1, new SyntaxToken(COMMA, lt.getSource(), lt.getEndIndex(), ","));
    stmtTokens.addAll(from.lastTokenIndex + 1 + 1, fromTokens);

    SqlGenerator gen = new SqlGenerator();
    return gen.generate(stmtTokens);
  }

  /**
   *  Add a <c>WHERE</c>-clause or <c>WHERE</c>-clause condition to an existing SQL statement.
   *
   *  @param baseSqlStmt The SQL statement syntax to add a <c>WHERE</c>-condition to.
   *  @param extraWhereCond The <c>WHERE</c>-condition to add (via <c>AND</c>) to any existing <c>WHERE</c>-condition or to use as <c>WHERE</c> condition if there is no <c>WHERE</c> clause currently.
   *
   *  @return The normalized version of updated <c>baseSqlStmt</c>.
   */
  public static String addWhereCondition(String baseSqlStmt, String extraWhereCond) {
    Contract.isNotNull("baseSqlStmt", baseSqlStmt);
    Contract.isNotNull("extraWhereCond", extraWhereCond);

    SqlTokenizer tokenizer = new SqlTokenizer();
    List<SyntaxToken> stmtTokens = CollectionUtils.listFromIterator(tokenizer.tokenize(baseSqlStmt)).stream().filter((tok) -> (tok.getSyntaxType() != SqlSyntaxType.WHITESPACE) && (tok.getSyntaxType() != SqlSyntaxType.LINE_COMMENT) && (tok.getSyntaxType() != SqlSyntaxType.BLOCK_COMMENT)).collect(Collectors.toList());
    SqlParser stmtParser = new SqlParser(stmtTokens);

    List<SyntaxToken> condTokens = CollectionUtils.listFromIterator(tokenizer.tokenize(extraWhereCond)).stream().filter((tok) -> (tok.getSyntaxType() != SqlSyntaxType.WHITESPACE) && (tok.getSyntaxType() != SqlSyntaxType.LINE_COMMENT) && (tok.getSyntaxType() != SqlSyntaxType.BLOCK_COMMENT)).collect(Collectors.toList());

    SqlParser.WhereClause where = stmtParser.findWhereClause();
    if (where == null) {
      int afterIdx = -1;
      int beforeIdx = -1;
      SqlParser.GroupByClause groupBy = stmtParser.findGroupByClause();
      if (groupBy != null) {
        beforeIdx = groupBy.firstTokenIndex;
      }
      else {
        SqlParser.OrderByClause orderBy = stmtParser.findOrderByClause();
        if (orderBy != null) {
          beforeIdx = orderBy.firstTokenIndex;
        }
      }
      if (beforeIdx == -1) {
        SqlParser.FromClause from = stmtParser.findFromClause();
        if (from != null) {
          afterIdx = from.lastTokenIndex + 1;
        }
      }
      if (beforeIdx != -1) {
        SyntaxToken fromToken = stmtTokens.get(beforeIdx);
        condTokens.add(0, new SyntaxToken(KEYWORD, fromToken.getSource(), fromToken.getEndIndex(), "WHERE"));
        stmtTokens.addAll(beforeIdx, condTokens);
      }
      else if (afterIdx != -1) {
        SyntaxToken fromToken = stmtTokens.get(afterIdx - 1);
        condTokens.add(0, new SyntaxToken(KEYWORD, fromToken.getSource(), fromToken.getEndIndex(), "WHERE"));
        stmtTokens.addAll(afterIdx, condTokens);
      }
      else { // No WHERE, no FROM: just add at end
        SyntaxToken fromToken = stmtTokens.get(stmtTokens.size() - 1);
        condTokens.add(0, new SyntaxToken(KEYWORD, fromToken.getSource(), fromToken.getEndIndex(), "WHERE"));
        stmtTokens.addAll(condTokens);
      }
    }
    else {
      SyntaxToken ft = where.tokenSource.get(where.firstTokenIndex);
      stmtTokens.add(where.firstTokenIndex + 1, new SyntaxToken(LEFT_PAREN, ft.getSource(), ft.getEndIndex(), "("));
      SyntaxToken lt = where.tokenSource.get(where.lastTokenIndex);
      condTokens.add(0, new SyntaxToken(RIGHT_PAREN, lt.getSource(), lt.getEndIndex(), ")"));
      condTokens.add(1, new SyntaxToken(OPERATOR, lt.getSource(), lt.getEndIndex(), "AND"));
      stmtTokens.addAll(where.lastTokenIndex + 1 + 1, condTokens);
    }

    SqlGenerator gen = new SqlGenerator();
    return gen.generate(stmtTokens);
  }

  /**
   *  Reverse the <c>ORDER BY</c>-clause in a SQL statement by changing all <c>ASC</c> by <c>DESC</c>, and vice versa.
   *
   *  @param sqlWithOrderByClause The SQL statement syntax that contains an <c>ORDER BY</c>-clause.
   *
   *  @return The updated <c>sqlWithOrderByClause</c> with reverse <c>ORDER BY</c> orders.
   *
   *  @throws FormatException If no <c>ORDER BY</c>-clause could be found.
   */
  public static String reverseOrderBy(String sqlWithOrderByClause) {
    Contract.isNotNull("sqlWithOrderByClause", sqlWithOrderByClause);
    SqlTokenizer tokenizer = new SqlTokenizer();
    List<SyntaxToken> stmtTokens = CollectionUtils.listFromIterator(tokenizer.tokenize(sqlWithOrderByClause)).stream().filter((tok) -> (tok.getSyntaxType() != SqlSyntaxType.WHITESPACE) && (tok.getSyntaxType() != SqlSyntaxType.LINE_COMMENT) && (tok.getSyntaxType() != SqlSyntaxType.BLOCK_COMMENT)).collect(Collectors.toList());
    SqlParser stmtParser = new SqlParser(stmtTokens);
    SqlParser.OrderByClause orderBy = stmtParser.findOrderByClause();
    if (orderBy == null) {
      throw new FormatException(sqlWithOrderByClause, "No ORDER BY-clause could be found");
    }
    for (SqlParser.OrderBySpec obs : orderBy.orderBySpecs) {
      int orderIdx = obs.orderKeywordIndex;
      SyntaxToken orderToken = stmtTokens.get(orderIdx);
      SyntaxToken changedToken;
      if (orderToken.getText().equalsIgnoreCase("ASC")) {
        changedToken = new SyntaxToken(orderToken.getSyntaxType(), orderToken.getSource(), orderToken.getStartIndex(), "DESC");
      }
      else {
        changedToken = new SyntaxToken(orderToken.getSyntaxType(), orderToken.getSource(), orderToken.getStartIndex(), "ASC");
      }
      stmtTokens.set(orderIdx, changedToken);
    }
    SqlGenerator gen = new SqlGenerator();
    return gen.generate(stmtTokens);
  }

  /**
   *  Create an altered SQL statement that will page forward or backward from a given bookmarked row.
   *
   *  @param sqlSelectStmt The basic <c>SELECT</c>-statement with <c>FROM</c>-clause and <c>ORDER BY</c>-clause to be used a guidance.
   *  @param reverseOrder Whether to create a paging condition in the reverse direction of the <c>ORDER BY</c>.
   *
   *  @return An updated <c>SELECT</c> statement with:<ul>
   *          <li>Updated <c>WHERE</c>-clause that filters based on the bookmarked rows values and taking the <c>ORDER BY</c> columns and order into account:
   *          <pre>
   *            WHERE ( {original-where-condition} ) AND
   *                  ( ( {order-by-col1} &gt; bookmark-val1 )
   *                    OR ( ({order-by-col1} = bookmark-val1) AND ( {order-by-col2} &gt; bookmark-val2) )
   *                    OR ... )
   *          </pre></li>
   *          </ul>
   *
   *  @throws FormatException If the <c>sqlSelectStmt</c> isn't a <c>SELECT</c> statement or doesn't have a <c>FROM</c> or <c>ORDER BY</c> clause.
   */
  public static String createColumnValuesBasedPagingStatement(String sqlSelectStmt, boolean reverseOrder) {
    Contract.isNotNull("sqlSelectStmt", sqlSelectStmt);

    SqlTokenizer tokenizer = new SqlTokenizer();
    List<SyntaxToken> stmtTokens = CollectionUtils.listFromIterator(tokenizer.tokenize(sqlSelectStmt)).stream().filter((tok) -> (tok.getSyntaxType() != SqlSyntaxType.WHITESPACE) && (tok.getSyntaxType() != SqlSyntaxType.LINE_COMMENT) && (tok.getSyntaxType() != SqlSyntaxType.BLOCK_COMMENT)).collect(Collectors.toList());
    SqlParser stmtParser = new SqlParser(stmtTokens);

    SqlParser.OrderByClause orderBy = stmtParser.findOrderByClause();
    if (orderBy == null) {
      throw new FormatException(sqlSelectStmt, "No ORDER BY-clause could be found");
    }
    SqlParser.GroupByClause groupBy = stmtParser.findGroupByClause();
    SqlParser.WhereClause where = stmtParser.findWhereClause();
    SqlParser.FromClause from = stmtParser.findFromClause();
    if (from == null) {
      throw new FormatException(sqlSelectStmt, "No FROM-clause could be found");
    }
    // Update WHERE clause:
    String whereCond = createPagingWhereCondition(orderBy, reverseOrder, ":bookmark-val");
    List<SyntaxToken> whereClauseTokens = CollectionUtils.listFromIterator(tokenizer.tokenize(whereCond));
    if (where != null) {
      SyntaxToken ft = where.tokenSource.get(where.firstTokenIndex);
      SyntaxToken lt = where.tokenSource.get(where.lastTokenIndex);
      List<SyntaxToken> whereStart = new ArrayList<>();
      whereStart.add(ft);
      whereStart.add(new SyntaxToken(LEFT_PAREN, ft.getSource(), ft.getEndIndex(), "("));
      whereStart.addAll(where.tokenSource.subList(where.firstTokenIndex + 1, where.lastTokenIndex + 1));
      whereStart.add(new SyntaxToken(RIGHT_PAREN, lt.getSource(), lt.getEndIndex(), ")"));
      whereStart.add(new SyntaxToken(OPERATOR, lt.getSource(), lt.getEndIndex(), "AND"));
      whereStart.add(new SyntaxToken(LEFT_PAREN, lt.getSource(), lt.getEndIndex(), "("));
      whereClauseTokens.addAll(0, whereStart);
      whereClauseTokens.add(new SyntaxToken(RIGHT_PAREN, lt.getSource(), lt.getEndIndex(), ")"));
    }
    else {
      SyntaxToken ft = from.tokenSource.get(from.lastTokenIndex);
      whereClauseTokens.add(0, new SyntaxToken(KEYWORD, ft.getSource(), ft.getEndIndex(), "WHERE"));
    }

    List<SyntaxToken> updatedStmtTokens = new ArrayList<>();
    // Add all until WHERE (if there was a WHERE) or until GROUP BY or ORDER BY, whichever comes first
    int untilTokIdx = orderBy.firstTokenIndex; // Assuming no WHERE and no GROUP BY
    int fromTokIdx = orderBy.firstTokenIndex;
    if (where != null) { // There is a WHERE that will be changed: copy everything before and after
      untilTokIdx = where.firstTokenIndex;
      fromTokIdx = where.lastTokenIndex + 1;
    }
    else if (groupBy != null) { // There is a GROUP BY but no where: copy everything before, add WHERE and then copy everything including
      untilTokIdx = groupBy.firstTokenIndex;
      fromTokIdx = groupBy.firstTokenIndex;
    }
    updatedStmtTokens.addAll(stmtTokens.subList(0, untilTokIdx));
    updatedStmtTokens.addAll(whereClauseTokens);
    updatedStmtTokens.addAll(stmtTokens.subList(fromTokIdx, stmtTokens.size()));

    SqlGenerator gen = new SqlGenerator();
    return gen.generate(updatedStmtTokens);
  }

  /**
   *  Create an altered SQL statement that will page forward or backward from a given bookmarked row.
   *
   *  @param sqlSelectStmt The basic <c>SELECT</c>-statement with <c>FROM</c>-clause and <c>ORDER BY</c>-clause to be used a guidance.
   *  @param idColName The name of the unique ID column whose value will act as bookmark.
   *  @param reverseOrder Whether to create a paging condition in the reverse direction or the <c>ORDER BY</c>.
   *
   *  @return An updated <c>SELECT</c> statement with:<ul>
   *          <li>Additional <c>WITH</c>-clause to select the bookmark row's values:
   *          <pre>
   *            WITH bookmark (val1, val2, ...) AS
   *                ( SELECT {order-col-1}, {order-col-2}, ...
   *                  {from-clause}
   *                  WHERE {id-col-name} = :bookmark-id-val )
   *          </pre></li>
   *          <li>Updated <c>FROM</c>-clause to make these bookmark values available:
   *          <pre>
   *            FROM {original-from-expressions}, bookmark
   *          </pre></li>
   *          <li>Updated <c>WHERE</c>-clause that filters based on these values and taking the <c>ORDER BY</c> columns and order into account:
   *          <pre>
   *            WHERE ( {original-where-condition} ) AND
   *                  ( ( {order-by-col1} &gt; {bookmarkValPrefix}1 )
   *                    OR ( ({order-by-col1} = {bookmarkValPrefix}1) AND ( {order-by-col2} &gt; {bookmarkValPrefix}2) )
   *                    OR ... )
   *          </pre></li>
   *          </ul>
   *
   *  @throws FormatException If the <c>sqlSelectStmt</c> isn't a <c>SELECT</c> statement or doesn't have a <c>FROM</c> or <c>ORDER BY</c> clause.
   */
  public static String createIdColumnBasedPagingStatement(String sqlSelectStmt, String idColName, boolean reverseOrder) {
    Contract.isNotNull("sqlSelectStmt", sqlSelectStmt);
    Contract.isNotNull("idColName", idColName);

    SqlTokenizer tokenizer = new SqlTokenizer();
    List<SyntaxToken> stmtTokens = CollectionUtils.listFromIterator(tokenizer.tokenize(sqlSelectStmt)).stream().filter((tok) -> (tok.getSyntaxType() != SqlSyntaxType.WHITESPACE) && (tok.getSyntaxType() != SqlSyntaxType.LINE_COMMENT) && (tok.getSyntaxType() != SqlSyntaxType.BLOCK_COMMENT)).collect(Collectors.toList());
    SqlParser stmtParser = new SqlParser(stmtTokens);

    SqlParser.OrderByClause orderBy = stmtParser.findOrderByClause();
    if (orderBy == null) {
      throw new FormatException(sqlSelectStmt, "No ORDER BY-clause could be found");
    }
    SqlParser.WhereClause where = stmtParser.findWhereClause();
    SqlParser.FromClause from = stmtParser.findFromClause();
    if (from == null) {
      throw new FormatException(sqlSelectStmt, "No FROM-clause could be found");
    }

    // Create WITH clause:
    StringBuilder sb = new StringBuilder();
    sb.append("WITH bookmark (");
    for (int i = 0; i < orderBy.orderBySpecs.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append("val");
      sb.append(i + 1);
    }
    sb.append(") AS ( SELECT ");
    for (int i = 0; i < orderBy.orderBySpecs.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(orderBy.orderBySpecs[i].getIdentifier());
    }
    sb.append(" ");
    sb.append(from.toString());
    sb.append(" WHERE ");
    sb.append(idColName);
    sb.append(" = :bookmark-id-val");
    sb.append(")");
    String withClause = sb.toString();
    List<SyntaxToken> withClauseTokens = CollectionUtils.listFromIterator(tokenizer.tokenize(withClause));

    // Update FROM clause:
    String updatedFrom = from.toString() + ", bookmark";
    List<SyntaxToken> fromClauseTokens = CollectionUtils.listFromIterator(tokenizer.tokenize(updatedFrom));

    // Update WHERE clause:
    String whereCond = createPagingWhereCondition(orderBy, reverseOrder, "bookmark.val");
    List<SyntaxToken> whereClauseTokens = CollectionUtils.listFromIterator(tokenizer.tokenize(whereCond));
    if (where != null) {
      SyntaxToken ft = where.tokenSource.get(where.firstTokenIndex);
      SyntaxToken lt = where.tokenSource.get(where.lastTokenIndex);
      List<SyntaxToken> whereStart = new ArrayList<>();
      whereStart.add(ft);
      whereStart.add(new SyntaxToken(LEFT_PAREN, ft.getSource(), ft.getEndIndex(), "("));
      whereStart.addAll(where.tokenSource.subList(where.firstTokenIndex + 1, where.lastTokenIndex + 1));
      whereStart.add(new SyntaxToken(RIGHT_PAREN, lt.getSource(), lt.getEndIndex(), ")"));
      whereStart.add(new SyntaxToken(OPERATOR, lt.getSource(), lt.getEndIndex(), "AND"));
      whereStart.add(new SyntaxToken(LEFT_PAREN, lt.getSource(), lt.getEndIndex(), "("));
      whereClauseTokens.addAll(0, whereStart);
      whereClauseTokens.add(new SyntaxToken(RIGHT_PAREN, lt.getSource(), lt.getEndIndex(), ")"));
    }
    else {
      SyntaxToken ft = from.tokenSource.get(from.lastTokenIndex);
      whereClauseTokens.add(0, new SyntaxToken(KEYWORD, ft.getSource(), ft.getEndIndex(), "WHERE"));
    }

    List<SyntaxToken> updatedStmtTokens = new ArrayList<>();
    updatedStmtTokens.addAll(withClauseTokens);
    updatedStmtTokens.addAll(stmtTokens.subList(0, from.firstTokenIndex)); // All until FROM keyword
    updatedStmtTokens.addAll(fromClauseTokens);
    updatedStmtTokens.addAll(whereClauseTokens);
    if (where != null) { // There was a WHERE clause: include everything after WHERE clause
      updatedStmtTokens.addAll(stmtTokens.subList(where.lastTokenIndex + 1, stmtTokens.size()));
    }
    else { // There was no WHERE clause: include everything after FROM clause
      updatedStmtTokens.addAll(stmtTokens.subList(from.lastTokenIndex + 1, stmtTokens.size()));
    }

    SqlGenerator gen = new SqlGenerator();
    return gen.generate(updatedStmtTokens);
  }

  /**
   *  Create a paging <c>WHERE</c>-condition based on an existing <c>ORDER BY</c>-clause.
   *
   *  @param sqlWithOrderByClause The <c>ORDER BY</c>-clause to be used a guidance. Can be standalone or part of a larger SQL statement.
   *  @param reverseOrder Whether to create a paging condition in the reverse direction or the <c>ORDER BY</c>.
   *  @param bookmarkValPrefix The bookmark value prefix. The 1-based <c>ORDER BY</c> column sequence number will get appended automatically.
   *
   *  @return A <c>WHERE</c> condition that assumes that the series of bookmark values named <c>bookmarkValPrefix{num}</c> is available.
   *
   *  @throws FormatException If no <c>ORDER BY</c>-clause could be found.
   */
  public static String createPagingWhereCondition(String sqlWithOrderByClause, boolean reverseOrder, String bookmarkValPrefix) {
    Contract.isNotNull("sqlWithOrderByClause", sqlWithOrderByClause);
    SqlTokenizer tokenizer = new SqlTokenizer();
    List<SyntaxToken> stmtTokens = CollectionUtils.listFromIterator(tokenizer.tokenize(sqlWithOrderByClause)).stream().filter((tok) -> (tok.getSyntaxType() != SqlSyntaxType.WHITESPACE) && (tok.getSyntaxType() != SqlSyntaxType.LINE_COMMENT) && (tok.getSyntaxType() != SqlSyntaxType.BLOCK_COMMENT)).collect(Collectors.toList());
    SqlParser stmtParser = new SqlParser(stmtTokens);
    SqlParser.OrderByClause orderBy = stmtParser.findOrderByClause();
    if (orderBy == null) {
      throw new FormatException(sqlWithOrderByClause, "No ORDER BY-clause could be found");
    }
    return createPagingWhereCondition(orderBy, reverseOrder, bookmarkValPrefix);
  }

  /**
   *  Create a paging <c>WHERE</c>-condition based on an existing <c>ORDER BY</c>-clause.
   *
   *  @param orderBy The <c>ORDER BY</c>-clause to be used a guidance.
   *  @param reverseOrder Whether to create a paging condition in the reverse direction or the <c>ORDER BY</c>.
   *  @param bookmarkValPrefix The bookmark value prefix. The 1-based <c>ORDER BY</c> column sequence number will get appended automatically.
   *
   *  @return A <c>WHERE</c> condition that assumes that the series of bookmark values named <c>bookmarkValPrefix{num}</c> is available.
   */
  public static String createPagingWhereCondition(SqlParser.OrderByClause orderBy, boolean reverseOrder, String bookmarkValPrefix) {
    /*  Given: A ASC, B DESC, C ASC, D DESC
        Resulting WHERE condition:      ( A > {bookmarkValPrefix}1 )
                                   OR ( ( A = {bookmarkValPrefix}1 ) AND ( B < {bookmarkValPrefix}2 ) )
                                   OR ( ( A = {bookmarkValPrefix}1 ) AND ( B = {bookmarkValPrefix}2 ) AND ( C > {bookmarkValPrefix}3 ) )
                                   OR ( ( A = {bookmarkValPrefix}1 ) AND ( B = {bookmarkValPrefix}2 ) AND ( C = {bookmarkValPrefix}3 ) AND ( D < {bookmarkValPrefix}4 ) )
        So as many OR-branches as there are ORDER BY specs and always the last spec comparing with an inequality ( > or < ) that
        is corresponding to the <c>ORDER</c>ing.

        Note that with tuples, this could be written also simply as: (A, B, C, D) >= ({bookmarkValPrefix}1, {bookmarkValPrefix}2, {bookmarkValPrefix}3, {bookmarkValPrefix}4)
     */
    Contract.isNotNull("orderBy", orderBy);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < orderBy.orderBySpecs.length; i++) {
      if (i > 0) {
        sb.append(" OR ( ");
      }
      for (int j = 0; j < i; j++) {
        sb.append("( ");
        sb.append(orderBy.orderBySpecs[j].getIdentifier());
        sb.append(" = ");
        sb.append(bookmarkValPrefix);
        sb.append(j + 1);
        sb.append(" ) AND ");
      }
      sb.append("( ");
      sb.append(orderBy.orderBySpecs[i].getIdentifier());
      Order o = orderBy.orderBySpecs[i].getOrder();
      if ((!reverseOrder && (o == Order.ASC)) || (reverseOrder && (o == Order.DESC))) {
        sb.append(" > ");
      }
      else {
        sb.append(" < ");
      }
      sb.append(bookmarkValPrefix);
      sb.append(i + 1);
      sb.append(" )");
      if (i > 0) {
        sb.append(" )");
      }
    }
    return sb.toString();
  }
}
