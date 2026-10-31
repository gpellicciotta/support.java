// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.sql;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.function.Function;

import com.pellicciotta.support.exceptions.FormatException;
import com.pellicciotta.support.exceptions.SqlRuntimeException;
import com.pellicciotta.support.text.SyntaxToken;
import com.pellicciotta.support.utils.*;

/**
 *  Class enabling page-forward and page-backward operations, while also providing access to the current page.
 *  <p>
 *  The implementation assumes that bookmarks are a serialized form of <c>ID</c> column value.</p>
 */
class IdColumnBasedSqlPager<T> implements SqlPager<T>
{
  // <editor-fold desc="Construction">

  /**
   *  Create a {@link IdColumnBasedSqlPager}.
   *
   *  @param sql The {@link SqlExecutor} this pager belongs to.
   *  @param mapper How to map {@link SqlRow}s to elements of type <c>T</c>.
   *  @param idColName Column name for each row's unique ID.
   *  @param idColType The type of the ID column, expressed as {@link java.sql.Types} constant. Currently restricted to <c>CHAR, VARCHAR, INTEGER, BIGINT, NUMERIC</c>
   *  @param baseSqlQuery Must have an <c>ORDER BY</c> and a <c>SELECT</c> selecting all <c>ORDER BY</c> columns.
   *                      Must not have a <c>LIMIT</c> clause.
   *                      Must not have a <c>WITH</c> clause.
   */
  protected IdColumnBasedSqlPager(SqlExecutor sql, Function<SqlRow, T> mapper, String idColName, int idColType, String baseSqlQuery) {
    Contract.isNotNull("sql", sql);
    Contract.isNotNull("mapper", mapper);
    Contract.isNotNull("idColName", idColName);
    Contract.isNotNull("baseSqlQuery", baseSqlQuery);
    this.sql = sql;
    this.mapper = mapper;
    this.idColName = idColName;
    this.idColType = idColType;
    switch (this.idColType) {
      case Types.CHAR:
      case Types.VARCHAR:
      case Types.INTEGER:
      case Types.BIGINT:
      case Types.NUMERIC:
      case Types.TIMESTAMP:
        break;
      default:
        throw new IllegalArgumentException("The idColType '" + idColType + "' is currently not supported");
    }
    SqlTokenizer tokenizer = new SqlTokenizer();
    List<SyntaxToken> tokens = CollectionUtils.listFromIterator(tokenizer.tokenize(baseSqlQuery));
    SqlGenerator gen = new SqlGenerator();
    this.baseSqlQuery = gen.generate(tokens);
    SqlParser par = new SqlParser(tokens);
    SqlParser.OrderByClause orderBy = par.findOrderByClause();
    if (orderBy == null) {
      throw new FormatException(baseSqlQuery, "Missing ORDER BY clause");
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

    String nextPageStmtText = SqlSyntaxUtils.createIdColumnBasedPagingStatement(firstPageStmtText, idColName, false);
    this.nextPageStmt = sql.createStatement(nextPageStmtText);

    String lastPageStmtText = SqlSyntaxUtils.reverseOrderBy(baseSqlQuery) + " LIMIT :page-size";
    this.lastPageStmt = sql.createStatement(lastPageStmtText);

    String prevPageStmtText = SqlSyntaxUtils.createIdColumnBasedPagingStatement(lastPageStmtText, idColName, false);
    this.prevPageStmt = sql.createStatement(prevPageStmtText);
  }

  private final String baseSqlQuery;
  private final SqlExecutor sql;
  private final Function<SqlRow, T> mapper;
  private final String idColName;
  private final int idColType;
  private ParameterizedSqlStatement firstPageStmt; // With named parameter :page-size
  private ParameterizedSqlStatement nextPageStmt;  // With named parameters :bookmark-id-val and :page-size
  private ParameterizedSqlStatement prevPageStmt;  // With named parameters :bookmark-id-val and :page-size
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

  private void applyBookmark(ParameterizedSqlStatement stmt, String bookmark) {
    switch (this.idColType) {
      case Types.INTEGER:
      case Types.BIGINT:
      case Types.NUMERIC:
        stmt.setBigDecimal("bookmark-id-val", new BigDecimal(bookmark));
        break;
      case Types.CHAR:
      case Types.VARCHAR:
        stmt.setString("bookmark-id-val", bookmark);
        break;
      case Types.TIMESTAMP:
        stmt.setTimestamp("bookmark-id-val", new Timestamp(new BigDecimal(bookmark).longValue()));
        break;
      default:
        throw new SqlRuntimeException("ID column has unsupported type '%d'", this.idColType);
    }
  }

  private String createBookmark(SqlRow row) {
    switch (this.idColType) {
      case Types.INTEGER:
      case Types.BIGINT:
      case Types.NUMERIC:
        return row.getBigDecimal(this.idColName).toPlainString();
      case Types.CHAR:
      case Types.VARCHAR:
        return row.getString(this.idColName);
      case Types.TIMESTAMP:
        return BigDecimal.valueOf(row.getTimestamp(this.idColName).getTime()).toPlainString();
      default:
        throw new SqlRuntimeException("ID column has unsupported type '%d'", this.idColType);
    }
  }

  @Override
  public boolean moveToFirstPage() {
    // Set params
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
    // Set params
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
    // Set params
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
