// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.sql;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.function.Function;

import com.pellicciotta.support.exceptions.FormatException;
import com.pellicciotta.support.exceptions.SqlRuntimeException;
import com.pellicciotta.support.text.SyntaxToken;
import com.pellicciotta.support.utils.CollectionUtils;
import com.pellicciotta.support.utils.Contract;

/**
 *  Class enabling page-forward and page-backward operations, while also providing access to the current page.
 *  <p>
 *  The implementation assumes that bookmarks are a serialized form of <c>ORDER BY</c> column values and that
 *  queries can be built where the concrete column values can be filled.</p>
 */
class ColumnValuesBasedSqlPager<T> implements SqlPager<T> {
  // <editor-fold desc="Construction">

  /**
   *  Create a {@link ColumnValuesBasedSqlPager}.
   *
   *  @param sql The {@link SqlExecutor} this pager belongs to.
   *  @param mapper How to map {@link SqlRow}s to elements of type <c>T</c>.
   *  @param baseSqlQuery Must have an <c>ORDER BY</c> and a <c>SELECT</c> selecting all <c>ORDER BY</c> columns.
   *                      Must not have a <c>LIMIT</c> clause.
   *                      Must not have a <c>WITH</c> clause.
   *  @param namedParameters Any named parameters to be set before executing the statement.
   *  @param positionalParameters Any positional parameters to be set before executing the statement. The first array element will be associated with position 1.
   */
  protected ColumnValuesBasedSqlPager(SqlExecutor sql, Function<SqlRow, T> mapper, String baseSqlQuery, Map<String, Object> namedParameters, Object[] positionalParameters) throws SQLException {
    Contract.isNotNull("sql", sql);
    Contract.isNotNull("mapper", mapper);
    Contract.isNotNull("baseSqlQuery", baseSqlQuery);
    this.sql = sql;
    this.mapper = mapper;
    this.namedParams = (namedParameters == null) ? new HashMap<>() : namedParameters;
    this.posParams = (positionalParameters == null) ? new Object[0] : positionalParameters;
    SqlTokenizer tokenizer = new SqlTokenizer();
    List<SyntaxToken> tokens = CollectionUtils.listFromIterator(tokenizer.tokenize(baseSqlQuery));
    SqlGenerator gen = new SqlGenerator();
    this.baseSqlQuery = gen.generate(tokens);
    SqlParser par = new SqlParser(tokens);
    SqlParser.FromClause from = par.findFromClause();
    if (from == null) {
      throw new FormatException(baseSqlQuery, "Missing FROM clause");
    }
    SqlParser.OrderByClause orderBy = par.findOrderByClause();
    if (orderBy == null) {
      throw new FormatException(baseSqlQuery, "Missing ORDER BY clause");
    }
    // Determine column info
    StringBuilder sb = new StringBuilder();
    sb.append("SELECT ");
    for (int i = 0; i < orderBy.orderBySpecs.length; i++) {
      SqlParser.OrderBySpec obs = orderBy.orderBySpecs[i];
      if (i > 0) {
        sb.append(", ");
      }
      sb.append(obs.getIdentifier());
    }
    sb.append(" ");
    sb.append(from.toString());
    sb.append(" ");
    sb.append(orderBy.toString());
    sb.append(" LIMIT 1");
    ParameterizedSqlStatement determineColumnInfoStmt = sql.createStatement(sb.toString());
    SqlRow row = determineColumnInfoStmt.executeQuery();
    ResultSetMetaData metaData = row.getMetaData();
    int colCount = metaData.getColumnCount();
    this.colNames = new String[colCount];
    this.colTypes = new int[colCount];
    this.colSizes = new int[colCount];
    for (int i = 0; i < colCount; i++) {
      String name = metaData.getColumnName(i + 1);
      int type = metaData.getColumnType(i + 1);
      switch (type) {
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.INTEGER:
        case Types.BIGINT:
        case Types.NUMERIC:
        case Types.TIMESTAMP:
          break;
        default:
          throw new IllegalArgumentException("The column " + name + " has a type '" + type + "' (" + metaData.getColumnTypeName(i + 1) + ") that currently is not supported");
      }
      colNames[i] = name;
      colTypes[i] = type;
      colSizes[i] = metaData.getPrecision(i + 1);
    }
    /*
    SqlParser.LimitClause limit = par.findLimitClause();
    if (limit != null) {
      throw new FormatException(baseSqlQuery, "The base SQL must not already contain a LIMIT clause");
    }
    SqlParser.WithClause with = par.findWithClause();
    if (with != null) {
      throw new FormatException(baseSqlQuery, "The base SQL must not already contain a WITH clause");
    }
    */
    String firstPageStmtText = SqlSyntaxUtils.normalizeText(baseSqlQuery) + " LIMIT :page-size";
    this.firstPageStmt = sql.createStatement(firstPageStmtText);

    String nextPageStmtText = SqlSyntaxUtils.createColumnValuesBasedPagingStatement(firstPageStmtText, false);
    this.nextPageStmt = sql.createStatement(nextPageStmtText);

    String lastPageStmtText = SqlSyntaxUtils.reverseOrderBy(baseSqlQuery) + " LIMIT :page-size";
    this.lastPageStmt = sql.createStatement(lastPageStmtText);

    String prevPageStmtText = SqlSyntaxUtils.createColumnValuesBasedPagingStatement(lastPageStmtText, false);
    this.prevPageStmt = sql.createStatement(prevPageStmtText);
  }

  private final String baseSqlQuery;
  private final SqlExecutor sql;
  private final Function<SqlRow, T> mapper;
  private final String[] colNames;
  private final int[] colTypes;
  private final int[] colSizes;
  private final Map<String, Object> namedParams;
  private final Object[] posParams;
  private ParameterizedSqlStatement firstPageStmt; // With named parameter :page-size
  private ParameterizedSqlStatement nextPageStmt;  // With named parameters :bookmark-val-{num} and :page-size
  private ParameterizedSqlStatement prevPageStmt;  // With named parameters :bookmark-val-{num} and :page-size
  private ParameterizedSqlStatement lastPageStmt;  // With named parameter :page-size
  private SqlPage<T> currentPage = null;
  private int pageSize = 25;

  // </editor-fold>
  // <editor-fold desc="Paging API">

  @Override
  public void setPageSize(int newSize) {
    Contract.isPositive("newSize", newSize);
    this.pageSize = newSize;
  }

  @Override
  public int getPageSize() {
    return this.pageSize;
  }

  @Override
  public SqlPage<T> getCurrentPage() {
    return this.currentPage;
  }

  @Override
  public boolean isPositioned() {
    return (this.currentPage != null);
  }

  private void setParams(ParameterizedSqlStatement stmt) {
    for (int i = 0; i < this.posParams.length; i++) {
      stmt.setObject((i + 1), this.posParams[i]);
    }
    for (String name : this.namedParams.keySet()) {
      stmt.setObject(name, this.namedParams.get(name));
    }
  }

  private void applyBookmark(ParameterizedSqlStatement stmt, String bookmark) {
    String[] parts = bookmark.split("[:]");
    if (parts.length != this.colNames.length) {
      throw new IllegalArgumentException("The bookmark '" + bookmark + "' is invalid as it refers to " + parts.length + " columns while the paging query orders by " + this.colNames.length + " columns");
    }
    for (int i = 0; i < this.colNames.length; i++) {
      String part = parts[i];
      int colType = this.colTypes[i];
      String colName = this.colNames[i];
      String paramName = "bookmark-val" + (i + 1);
      switch (colType) {
        case Types.INTEGER:
        case Types.BIGINT:
        case Types.NUMERIC:
          stmt.setBigDecimal(paramName, new BigDecimal(part));
          break;
        case Types.CHAR:
        case Types.VARCHAR:
          stmt.setString(paramName, part);
          break;
        case Types.TIMESTAMP:
          stmt.setTimestamp(paramName, new Timestamp(new BigDecimal(part).longValue()));
          break;
        default:
          throw new SqlRuntimeException("Column '%s' has an unsupported type '%d'", colName, colType);
      }
    }
  }

  private String createBookmark(SqlRow row) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < this.colNames.length; i++) {
      if (i > 0) {
        sb.append(':');
      }
      String part = columnToString(row, this.colNames[i], this.colTypes[i], this.colSizes[i]);
      sb.append(part);
    }
    return sb.toString();
  }

  private String columnToString(SqlRow row, String colName, int colType, int colMaxSize) {
    switch (colType) {
      case Types.INTEGER:
      case Types.BIGINT:
      case Types.NUMERIC:
        return row.getBigDecimal(colName).toPlainString();
      case Types.CHAR:
      case Types.VARCHAR:
        return row.getString(colName);
      case Types.TIMESTAMP:
        return BigDecimal.valueOf(row.getTimestamp(colName).getTime()).toPlainString();
      default:
        throw new SqlRuntimeException("Column '%s' has an unsupported type '%d'", colName, colType);
    }
  }

  @Override
  public boolean moveToFirstPage() {
    // Set params
    this.setParams(this.firstPageStmt);
    this.firstPageStmt.setInt("page-size", this.pageSize + 1);
    this.firstPageStmt.setFetchSize(this.pageSize + 1);

    // Execute
    List<T> results = new ArrayList<>();
    List<String> bookmarks = new ArrayList<>();
    try (SqlRow row = this.firstPageStmt.executeQuery()) {
      for (int i = 0; (i < this.pageSize + 1) && row.next(); i++) {
        T element = this.mapper.apply(row);
        results.add(element);
        String bookmark = this.createBookmark(row);
        bookmarks.add(bookmark);
      }
    }
    if (results.isEmpty()) {
      this.currentPage = null;
      return false;
    }
    String nextBookmark = null;
    if (results.size() == this.pageSize + 1) {
      results = results.subList(0, this.pageSize); // Get rid of +1 result, that was just for checking whether we're at the end of the results
      nextBookmark = bookmarks.get(this.pageSize - 1);
    }
    this.currentPage = new SqlPage<T>(results, this.pageSize, null, nextBookmark);
    return true;
  }

  @Override
  public boolean hasNextPage() {
    return (this.currentPage != null) && (this.currentPage.getEndBookmark() != null);
  }

  @Override
  public boolean moveToNextPage() {
    if (this.currentPage == null) {
      return this.moveToFirstPage();
    }
    if (this.currentPage.getEndBookmark() != null) {
      return this.moveToPageAfter(this.currentPage.getEndBookmark());
    }
    return false;
  }

  @Override
  public boolean moveToPageAfter(String prevEndBookmark) {
    if (prevEndBookmark == null) {
      return this.moveToFirstPage();
    }
    // Set params
    this.setParams(this.nextPageStmt);
    this.applyBookmark(this.nextPageStmt, prevEndBookmark);
    this.nextPageStmt.setInt("page-size", this.pageSize + 1);
    this.nextPageStmt.setFetchSize(this.pageSize + 1);
    // Execute
    List<T> results = new ArrayList<>();
    List<String> bookmarks = new ArrayList<>();
    try (SqlRow row = this.nextPageStmt.executeQuery()) {
      for (int i = 0; (i < this.pageSize + 1) && row.next(); i++) {
        T element = this.mapper.apply(row);
        results.add(element);
        String bookmark = this.createBookmark(row);
        bookmarks.add(bookmark);
      }
    }
    if (results.isEmpty()) {
      this.currentPage = null;
      return false;
    }
    String prevBookmark = null;
    String nextBookmark = null;
    if (results.size() < this.pageSize + 1) {
      prevBookmark = bookmarks.get(0);
      nextBookmark = null;
    }
    else {
      results = results.subList(0, this.pageSize); // Get rid of +1 result, that was just for checking whether we're at the end of the results
      prevBookmark = bookmarks.get(0);
      nextBookmark = bookmarks.get(this.pageSize - 1);
    }
    this.currentPage = new SqlPage<T>(results, this.pageSize, prevBookmark, nextBookmark);
    return true;
  }

  @Override
  public boolean hasPreviousPage() {
    return (this.currentPage != null) && (this.currentPage.getStartBookmark() != null);
  }

  @Override
  public boolean moveToPreviousPage() {
    if (this.currentPage == null) {
      return this.moveToLastPage();
    }
    if (this.currentPage.getStartBookmark() != null) {
      return this.moveToPageBefore(this.currentPage.getStartBookmark());
    }
    return false;
  }

  @Override
  public boolean moveToPageBefore(String prevStartBookmark) {
    if (prevStartBookmark == null) {
      return this.moveToLastPage();
    }
    // Set params
    this.setParams(this.prevPageStmt);
    this.applyBookmark(this.prevPageStmt, prevStartBookmark);
    this.prevPageStmt.setInt("page-size", this.pageSize + 1);
    this.prevPageStmt.setFetchSize(this.pageSize + 1);
    // Execute
    List<T> results = new ArrayList<>();
    List<String> bookmarks = new ArrayList<>();
    try (SqlRow row = this.prevPageStmt.executeQuery()) {
      for (int i = 0; (i < this.pageSize + 1) && row.next(); i++) {
        T element = this.mapper.apply(row);
        results.add(element);
        String bookmark = this.createBookmark(row);
        bookmarks.add(bookmark);
      }
    }
    if (results.isEmpty()) {
      this.currentPage = null;
      return false;
    }
    String prevBookmark = null;
    String nextBookmark = null;
    if (results.size() < this.pageSize + 1) {
      prevBookmark = null;
      nextBookmark = bookmarks.get(0);
    }
    else {
      results = results.subList(0, this.pageSize); // Get rid of +1 result, that was just for checking whether we're at the end of the results
      prevBookmark = bookmarks.get(this.pageSize - 1);
      nextBookmark = bookmarks.get(0);
    }
    this.currentPage = new SqlPage<T>(results, this.pageSize, prevBookmark, nextBookmark);
    return true;
  }

  @Override
  public boolean moveToLastPage() {
    // Set params
    this.setParams(this.lastPageStmt);
    this.lastPageStmt.setInt("page-size", this.pageSize + 1);
    this.lastPageStmt.setFetchSize(this.pageSize + 1);

    // Execute
    List<T> results = new ArrayList<>();
    List<String> bookmarks = new ArrayList<>();
    try (SqlRow row = this.lastPageStmt.executeQuery()) {
      for (int i = 0; (i < this.pageSize + 1) && row.next(); i++) {
        T element = this.mapper.apply(row);
        results.add(element);
        String bookmark = this.createBookmark(row);
        bookmarks.add(bookmark);
      }
    }
    if (results.isEmpty()) {
      this.currentPage = null;
      return false;
    }
    String prevBookmark = null;
    if (results.size() == this.pageSize + 1) {
      results = results.subList(0, this.pageSize); // Get rid of +1 result, that was just for checking whether we're at the end of the results
      prevBookmark = bookmarks.get(this.pageSize - 1);
    }
    this.currentPage = new SqlPage<T>(results, this.pageSize, prevBookmark, null);
    return true;
  }

  // </editor-fold>
}
