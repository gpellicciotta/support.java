// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.sql;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import com.pellicciotta.support.app.Version;
import com.pellicciotta.support.exceptions.SqlRuntimeException;
import com.pellicciotta.support.logging.Log;
import com.pellicciotta.support.logging.LogService;
import com.pellicciotta.support.utils.*;

/**
 *  Class providing support for the building and execution of SQL statements more conveniently.
 */
public class SqlExecutor implements AutoCloseable
{
  private static final Log log = LogService.fromCurrentClass();

  /**
   *  Create an executor using the given connection and assuming standard SQL.
   *
   *  @param connection The connection to use.
   */
  public SqlExecutor(Connection connection) {
    this(new SqlDialect() { }, connection);
  }

  /**
   *  Create an executor using the given connection and assuming the provided dialect.
   *
   *  @param dialect The dialect specifics to use.
   *  @param connection The connection to use.
   */
  public SqlExecutor(SqlDialect dialect, Connection connection) {
    Contract.isNotNull("dialect", dialect);
    Contract.isNotNull("connection", connection);
    this.dialect = dialect;
    this.connection = connection;
  }

  @Override
  public void close() {
    try {
      this.connection.close();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  private final SqlDialect dialect;
  private final Connection connection;

  /**
   *  Execute a <c>ROLLBACK</c>.
   */
  public void rollback() {
    try {
      this.connection.rollback();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Execute a <c>COMMIT</c>.
   */
  public void commit() {
    try {
      this.connection.commit();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Create a {@link java.sql.PreparedStatement}, possibly with named parameters and with the given statement options.
   *
   *  @param sqlSyntax Valid SQL statement syntax.
   *  @param options Statement options to be used.
   *
   *  @return The {@link ParameterizedSqlStatement} that can be executed, possibly after setting parameter values.
   */
  public ParameterizedSqlStatement createStatement(String sqlSyntax, int options) {
    Contract.isNotEmpty("sqlSyntax", sqlSyntax);
    try {
      SqlStatementSyntax syntax = new SqlStatementSyntax(sqlSyntax);
      PreparedStatement stmt = this.connection.prepareStatement(syntax.normalizedSyntax, options);
      return new ParameterizedSqlStatement(stmt, sqlSyntax, syntax.namedParameters);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Create a {@link java.sql.PreparedStatement}, possibly with named parameters and always returning generated keys.
   *
   *  @param sqlSyntaxParts Valid SQL statement syntax, possibly spread over multiple parts, which will be concatenated with one intervening space in between.
   *                        At least one part must be provided.
   *
   *  @return The {@link ParameterizedSqlStatement} that can be executed, possibly after setting parameter values.
   */
  public ParameterizedSqlStatement createStatement(String... sqlSyntaxParts) {
    Contract.isNotEmpty("sqlSyntaxParts", sqlSyntaxParts);
    try {
      String sqlSyntax = StringUtils.join(" ", sqlSyntaxParts);
      SqlStatementSyntax syntax = new SqlStatementSyntax(sqlSyntax);
      PreparedStatement stmt = this.connection.prepareStatement(syntax.normalizedSyntax, Statement.RETURN_GENERATED_KEYS);
      return new ParameterizedSqlStatement(stmt, sqlSyntax, syntax.namedParameters);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Create a {@link Statement} representing an update (<c>UPDATE</c>, <c>INSERT</c> or <c>DELETE</c>) operation,
   *  execute it and return the rows affected.
   *
   *  @param sqlSyntaxParts Valid SQL statement syntax, possibly spread over multiple parts, which will be concatenated with one intervening space in between.
   *                        At least one part must be provided.
   *
   *  @return The number of rows affected.
   */
  public int executeUpdate(String... sqlSyntaxParts) {
    Contract.isNotEmpty("sqlSyntaxParts", sqlSyntaxParts);
    try {
      String sqlSyntax = StringUtils.join(" ", sqlSyntaxParts);
      try (Statement stmt = this.connection.createStatement()) {
        return stmt.executeUpdate(sqlSyntax);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Create a {@link Statement} representing a <c>SELECT</c> operation, execute it and return the resulting rows.
   *
   *  @param sqlSyntaxParts Valid SQL statement syntax, possibly spread over multiple parts, which will be concatenated with one intervening space in between.
   *                        At least one part must be provided.
   *
   *  @return The resulting rows, just positioned before the first.
   */
  public SqlRow executeQuery(String... sqlSyntaxParts) {
    Contract.isNotEmpty("sqlSyntaxParts", sqlSyntaxParts);
    try {
      String sqlSyntax = StringUtils.join(" ", sqlSyntaxParts);
      Statement stmt = this.connection.createStatement();
      return new SqlRow(stmt.executeQuery(sqlSyntax));
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Set the parameters on the provided {@link ParameterizedSqlStatement}, execute it and return the resulting rows.
   *
   *  @param stmt A prepared statement.
   *  @param namedParameters Any named parameters to be set before executing the statement.
   *
   *  @return The resulting rows, just positioned before the first.
   */
  public SqlRow executeQuery(ParameterizedSqlStatement stmt, Map<String, Object> namedParameters) {
    // Set params
    for (String paramName : namedParameters.keySet()) {
      Object paramVal = namedParameters.get(paramName);
      stmt.setObject(paramName, paramVal);
    }
    // Execute
    return stmt.executeQuery();
  }

  /**
   *  Set the parameters on the provided {@link ParameterizedSqlStatement}, execute it and return the resulting rows.
   *
   *  @param stmt A prepared statement.
   *  @param positionalParameters Any positional parameters to be set before executing the statement. The first array element will be associated with position 1.
   *
   *  @return The resulting rows, just positioned before the first.
   */
  public SqlRow executeQuery(ParameterizedSqlStatement stmt, Object... positionalParameters) {
    // Set params
    if (positionalParameters != null) {
      for (int i = 0; i < positionalParameters.length; i++) {
        stmt.setObject((i+1), positionalParameters[i]);
      }
    }
    // Execute
    return stmt.executeQuery();
  }

  /**
   *  Execute (and commit) all SQL statements found in the given file, possibly restricted to those statements preceded by a valid version in the desired version range.
   *
   *  @param sqlFileName Name of the SQL file to find and execute statements from. For error reporting.
   *
   *  @throws SqlRuntimeException If either the file cannot be found, cannot be parsed or a statement fails to execute. Note that some statements may have already been committed.
   *
   *  @return The number of SQL statements that were successfully executed.
   */
  public int executeFromFile(String sqlFileName) {
    return this.executeFromFile(sqlFileName, null, false);
  }

  /**
   *  Execute (and commit) all SQL statements found in the given file, possibly restricted to those statements preceded by a valid version in the desired version range.
   *
   *  @param sqlFileName Name of the SQL file to find and execute statements from. For error reporting.
   *  @param versionRange The range of versions for SQL script parts that should be executed. If <c>null</c>, all parts will get executed.
   *
   *  @throws SqlRuntimeException If either the file cannot be found, cannot be parsed or a statement fails to execute. Note that some statements may have already been committed.
   *
   *  @return The number of SQL statements that were successfully executed.
   */
  public int executeFromFile(String sqlFileName, Range<Version> versionRange) {
    return this.executeFromFile(sqlFileName, versionRange, false);
  }

  /**
   *  Execute (and commit) all SQL statements found in the given file, possibly restricted to those statements preceded by a valid version in the desired version range.
   *
   *  @param sqlFileName Name of the SQL file to find and execute statements from. For error reporting.
   *  @param versionRange The range of versions for SQL script parts that should be executed. If <c>null</c>, all parts will get executed.
   *  @param allowDropFailures Continue processing SQL statements if a statement fails, but only if the statement is a DROP-statement
   *
   *  @throws SqlRuntimeException If either the script cannot be found, it contains statements with a wrong version indication or a non-DROP
   *                              statement fails to execute (or a DROP statement fails to execute if allowDropErrors is false)
   *
   *  @return The number of SQL statements that were successfully executed.
   */
  public int executeFromFile(String sqlFileName, Range<Version> versionRange, boolean allowDropFailures) {
    Contract.isNotNull("sqlFileName", sqlFileName);
    List<VersionedSqlStatement> scriptParts;
    try {
      SqlScriptFileReader r = new SqlScriptFileReader();
      scriptParts = r.readFile(sqlFileName);
    }
    catch (IOException ioe) {
      throw new SqlRuntimeException(ioe, "Cannot open SQL file '%s'", sqlFileName);
    }
    return this.executeVersionedStatements(scriptParts, versionRange, allowDropFailures);
  }

  /**
   *  Execute (and commit) all SQL statements found in the given script file, possibly restricted to those statements preceded by a valid version in the desired version range.
   *
   *  @param sqlResourceName Name of the SQL resource file to find and execute statements from.
   *
   *  @throws SqlRuntimeException If either the resource cannot be found, cannot be parsed or a statement fails to execute. Note that some statements may have already been committed.
   *
   *  @return The number of SQL statements that were successfully executed.
   */
  public int executeFromResourceFile(String sqlResourceName) {
    return this.executeFromResourceFile(sqlResourceName, null, false);
  }

  /**
   *  Execute (and commit) all SQL statements found in the given script file, possibly restricted to those statements preceded by a valid version in the desired version range.
   *
   *  @param sqlResourceName Name of the SQL resource file to find and execute statements from.
   *  @param versionRange The range of versions for SQL script parts that should be executed. If <c>null</c>, all parts will get executed.
   *
   *  @throws SqlRuntimeException If either the resource cannot be found, cannot be parsed or a statement fails to execute. Note that some statements may have already been committed.
   *
   *  @return The number of SQL statements that were successfully executed.
   */
  public int executeFromResourceFile(String sqlResourceName, Range<Version> versionRange) {
    return this.executeFromResourceFile(sqlResourceName, versionRange, false);
  }

  /**
   *  Execute (and commit) all SQL statements found in the given script file, possibly restricted to those statements preceded by a valid version in the desired version range.
   *
   *  @param sqlResourceName Name of the SQL resource file to find and execute statements from.
   *  @param versionRange The range of versions for SQL script parts that should be executed. If <c>null</c>, all parts will get executed.
   *  @param allowDropFailures Continue processing SQL statements if a statement fails, but only if the statement is a DROP-statement
   *
   *  @throws SqlRuntimeException If either the resource cannot be found, it contains statements with a wrong version indication or a non-DROP
   *                              statement fails to execute (or a DROP statement fails to execute if allowDropErrors is false)
   *
   *  @return The number of SQL statements that were successfully executed.
   */
  public int executeFromResourceFile(String sqlResourceName, Range<Version> versionRange, boolean allowDropFailures) {
    Contract.isNotNull("sqlResourceName", sqlResourceName);
    List<VersionedSqlStatement> scriptParts;
    try {
      SqlScriptFileReader r = new SqlScriptFileReader();
      scriptParts = r.readResource(sqlResourceName);
    }
    catch (IOException ioe) {
      throw new SqlRuntimeException(ioe, "Cannot open SQL resource file '%s'", sqlResourceName);
    }
    return this.executeVersionedStatements(scriptParts, versionRange, allowDropFailures);
  }

  /**
   *  Execute (and commit) all versioned SQL statements that fall within a desired version range.
   *
   *  @param versionedStatements The statements to be optionally executed.
   *  @param versionRange The range of versions for SQL script parts that should be executed. If <c>null</c>, all parts will get executed.
   *  @param allowDropFailures Continue processing SQL statements if a statement fails, but only if the statement is a DROP-statement
   *
   *  @throws SqlRuntimeException If a non-DROP statement fails to execute (or a DROP statement fails to execute if allowDropErrors is false)
   *
   *  @return The number of SQL statements that were successfully executed.
   */
  public int executeVersionedStatements(List<VersionedSqlStatement> versionedStatements, Range<Version> versionRange, boolean allowDropFailures) {
    Contract.isNotNull("versionedStatements", versionedStatements);
    int statementsExecuted = 0;
    // Go through all parts and expect to execute the ones in the correct version range
    for (VersionedSqlStatement part : versionedStatements) {
      Version partVersion = part.getVersion();
      if ((versionRange != null) && (partVersion != null) && !versionRange.contains(partVersion)) {
        continue; // Skip SQL statements before the first valid version or after the last
      }
      String comment = part.getComment();
      String sqlStmt = part.getSqlStatement();
      if (comment.isEmpty()) {
        comment = "Executing SQL statement '" + sqlStmt + "'";
      }
      log.info(comment);
      try {
        statementsExecuted++;
        this.executeUpdate(sqlStmt);
        this.connection.commit();
      }
      catch (Exception e) {
        if (allowDropFailures && sqlStmt.toLowerCase().startsWith("drop ")) {
          log.warning("'%s' failed to execute, but this is being ignored: %s", sqlStmt, e);
        } else {
          throw new SqlRuntimeException(e);
        }
      }
    }
    return statementsExecuted;
  }

  /**
   *  Fetch a single result that gets automatically mapped from a {@link SqlRow} to the desired type {T}.
   *
   *  @param mapper The function to map from {@link SqlRow} to {T}.
   *  @param sqlSyntaxParts Valid SQL statement syntax, possibly spread over multiple parts, which will be concatenated with one intervening space in between.
   *                        At least one part must be provided.
   *
   *  @return The single result, based on the single row being fetched.
   *          Will return <c>null</c> if zero rows were returned.
   *
   *  @throws SqlRuntimeException If more than 1 rows were returned.
   */
  public <T> T fetchSingle(Function<SqlRow, T> mapper, String... sqlSyntaxParts) {
    Contract.isNotNull("mapper", mapper);
    Contract.isNotEmpty("sqlSyntaxParts", sqlSyntaxParts);
    try {
      String sqlSyntax = StringUtils.join(" ", sqlSyntaxParts);
      try (Statement stmt = this.connection.createStatement()) {
        try (SqlRow row = new SqlRow(stmt.executeQuery(sqlSyntax))) {
          if (!row.next()) {
            return null;
          }
          T result = mapper.apply(row);
          if (!row.isClosed() && row.next()) {
            throw new SqlRuntimeException("Expected a single result but resultset has more than one row");
          }
          return result;
        }
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Fetch a single result that gets automatically mapped from a {@link SqlRow} to the desired type {T}.
   *
   *  @param mapper The function to map from {@link SqlRow} to {T}.
   *  @param sqlSyntax The SQL syntax that will be turned into a prepared statement first.
   *  @param namedParameters Any named parameters to be set before executing the statement.
   *
   *  @return The single result, based on the single row being fetched.
   *          Will return <c>null</c> if zero rows were returned.
   *
   *  @throws SqlRuntimeException If more than 1 rows were returned.
   */
  public <T> T fetchSingleWithParams(Function<SqlRow, T> mapper, String sqlSyntax, Map<String, Object> namedParameters) {
    try (ParameterizedSqlStatement stmt = this.createStatement(sqlSyntax)) {
      return this.fetchSingleWithParams(mapper, stmt, namedParameters);
    }
  }

  /**
   *  Fetch a single result that gets automatically mapped from a {@link SqlRow} to the desired type {T}.
   *
   *  @param mapper The function to map from {@link SqlRow} to {T}.
   *  @param stmt A prepared statement.
   *  @param namedParameters Any named parameters to be set before executing the statement.
   *
   *  @return The single result, based on the single row being fetched.
   *          Will return <c>null</c> if zero rows were returned.
   *
   *  @throws SqlRuntimeException If more than 1 rows were returned.
   */
  public <T> T fetchSingleWithParams(Function<SqlRow, T> mapper, ParameterizedSqlStatement stmt, Map<String, Object> namedParameters) {
    Contract.isNotNull("mapper", mapper);
    Contract.isNotNull("stmt", stmt);
    Contract.isNotNull("namedParameters", namedParameters);
    // Set params
    for (String paramName : namedParameters.keySet()) {
      Object paramVal = namedParameters.get(paramName);
      stmt.setObject(paramName, paramVal);
    }
    // Execute
    try (SqlRow row = new SqlRow(stmt.executeQuery())) {
      if (!row.next()) {
        return null;
      }
      T result = mapper.apply(row);
      if (!row.isClosed() && row.next()) {
        throw new SqlRuntimeException("Expected a single result but the result set has more than one row");
      }
      return result;
    }
  }

  /**
   *  Fetch a single result that gets automatically mapped from a {@link SqlRow} to the desired type {T}.
   *
   *  @param mapper The function to map from {@link SqlRow} to {T}.
   *  @param sqlSyntax The SQL syntax that will be turned into a prepared statement first.
   *  @param positionalParameters Any positional parameters to be set before executing the statement. The first array element will be associated with position 1.
   *
   *  @return The single result, based on the single row being fetched.
   *          Will return <c>null</c> if zero rows were returned.
   *
   *  @throws SqlRuntimeException If more than 1 rows were returned.
   */
  public <T> T fetchSingleWithParams(Function<SqlRow, T> mapper, String sqlSyntax, Object... positionalParameters) {
    try (ParameterizedSqlStatement stmt = this.createStatement(sqlSyntax)) {
      return this.fetchSingleWithParams(mapper, stmt, positionalParameters);
    }
  }

  /**
   *  Fetch a single result that gets automatically mapped from a {@link SqlRow} to the desired type {T}.
   *
   *  @param mapper The function to map from {@link SqlRow} to {T}.
   *  @param stmt A prepared statement.
   *  @param positionalParameters Any positional parameters to be set before executing the statement. The first array element will be associated with position 1.
   *
   *  @return The single result, based on the single row being fetched.
   *          Will return <c>null</c> if zero rows were returned.
   *
   *  @throws SqlRuntimeException If more than 1 rows were returned.
   */
  public <T> T fetchSingleWithParams(Function<SqlRow, T> mapper, ParameterizedSqlStatement stmt, Object... positionalParameters) {
    Contract.isNotNull("mapper", mapper);
    Contract.isNotNull("stmt", stmt);
    // Set params
    if (positionalParameters != null) {
      for (int i = 0; i < positionalParameters.length; i++) {
        stmt.setObject((i+1), positionalParameters[i]);
      }
    }
    // Execute
    try (SqlRow rs = stmt.executeQuery()) {
      if (!rs.next()) {
        return null;
      }
      T result = mapper.apply(rs);
      if (!rs.isClosed() && rs.next()) {
        throw new SqlRuntimeException("Expected a single result but resultset has more than one row");
      }
      return result;
    }
  }

  /**
   *  Fetch the first result that gets automatically mapped from a {@link SqlRow} to the desired type {T}.
   *
   *  @param mapper The function to map from {@link SqlRow} to {T}.
   *  @param sqlSyntaxParts Valid SQL statement syntax, possibly spread over multiple parts, which will be concatenated with one intervening space in between.
   *                        At least one part must be provided.
   *
   *  @return The first result or <c>null</c> if zero rows were returned.
   */
  public <T> T fetchFirst(Function<SqlRow, T> mapper, String... sqlSyntaxParts) {
    Contract.isNotNull("mapper", mapper);
    Contract.isNotEmpty("sqlSyntaxParts", sqlSyntaxParts);
    try {
      String sqlSyntax = StringUtils.join(" ", sqlSyntaxParts);
      try (Statement stmt = this.connection.createStatement()) {
        try (SqlRow rs = new SqlRow(stmt.executeQuery(sqlSyntax))) {
          if (!rs.next()) {
            return null;
          }
          T result = mapper.apply(rs);
          return result;
        }
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Fetch the first result that gets automatically mapped from a {@link SqlRow} to the desired type {T}.
   *
   *  @param mapper The function to map from {@link SqlRow} to {T}.
   *  @param sqlSyntax The SQL syntax that will be turned into a prepared statement first.
   *  @param namedParameters Any named parameters to be set before executing the statement.
   *
   *  @return The first result or <c>null</c> if zero rows were returned.
   */
  public <T> T fetchFirstWithParams(Function<SqlRow, T> mapper, String sqlSyntax, Map<String, Object> namedParameters) {
    try (ParameterizedSqlStatement stmt = this.createStatement(sqlSyntax)) {
      return this.fetchFirstWithParams(mapper, stmt, namedParameters);
    }
  }

  /**
   *  Fetch the first result that gets automatically mapped from a {@link SqlRow} to the desired type {T}.
   *
   *  @param mapper The function to map from {@link SqlRow} to {T}.
   *  @param stmt A prepared statement.
   *  @param namedParameters Any named parameters to be set before executing the statement.
   *
   *  @return The first result or <c>null</c> if zero rows were returned.
   */
  public <T> T fetchFirstWithParams(Function<SqlRow, T> mapper, ParameterizedSqlStatement stmt, Map<String, Object> namedParameters) {
    Contract.isNotNull("mapper", mapper);
    Contract.isNotNull("stmt", stmt);
    Contract.isNotNull("namedParameters", namedParameters);
    // Set params
    for (String paramName : namedParameters.keySet()) {
      Object paramVal = namedParameters.get(paramName);
      stmt.setObject(paramName, paramVal);
    }
    // Execute
    try (SqlRow rs = stmt.executeQuery()) {
      if (!rs.next()) {
        return null;
      }
      T result = mapper.apply(rs);
      return result;
    }
  }

  /**
   *  Fetch the first result that gets automatically mapped from a {@link SqlRow} to the desired type {T}.
   *
   *  @param mapper The function to map from {@link SqlRow} to {T}.
   *  @param sqlSyntax The SQL syntax that will be turned into a prepared statement first.
   *  @param positionalParameters Any positional parameters to be set before executing the statement. The first array element will be associated with position 1.
   *
   *  @return The first result or <c>null</c> if zero rows were returned.
   */
  public <T> T fetchFirstWithParams(Function<SqlRow, T> mapper, String sqlSyntax, Object... positionalParameters) {
    try (ParameterizedSqlStatement stmt = this.createStatement(sqlSyntax)) {
      return this.fetchFirstWithParams(mapper, stmt, positionalParameters);
    }
  }

  /**
   *  Fetch the first result that gets automatically mapped from a {@link SqlRow} to the desired type {T}.
   *
   *  @param mapper The function to map from {@link SqlRow} to {T}.
   *  @param stmt A prepared statement.
   *  @param positionalParameters Any positional parameters to be set before executing the statement. The first array element will be associated with position 1.
   *
   *  @return The first result or <c>null</c> if zero rows were returned.
   */
  public <T> T fetchFirstWithParams(Function<SqlRow, T> mapper, ParameterizedSqlStatement stmt, Object... positionalParameters) {
    Contract.isNotNull("mapper", mapper);
    Contract.isNotNull("stmt", stmt);
    // Set params
    if (positionalParameters != null) {
      for (int i = 0; i < positionalParameters.length; i++) {
        stmt.setObject((i+1), positionalParameters[i]);
      }
    }
    // Execute
    try (SqlRow rs = stmt.executeQuery()) {
      if (!rs.next()) {
        return null;
      }
      T result = mapper.apply(rs);
      return result;
    }
  }

  /**
   *  Fetch all results that get automatically mapped from a {@link SqlRow} to the desired type {T}.
   *  <p>
   *  Don't use this method if there are too many (e.g. more than hundredths) rows.</p>
   *
   *  @param mapper The function to map from {@link SqlRow} to {T}.
   *  @param sqlSyntaxParts Valid SQL statement syntax, possibly spread over multiple parts, which will be concatenated with one intervening space in between.
   *                        At least one part must be provided.
   *
   *  @return A possibly empty list of all results.
   */
  public <T> List<T> fetchAll(Function<SqlRow, T> mapper, String... sqlSyntaxParts) {
    Contract.isNotNull("mapper", mapper);
    Contract.isNotEmpty("sqlSyntaxParts", sqlSyntaxParts);
    try {
      String sqlSyntax = StringUtils.join(" ", sqlSyntaxParts);
      List<T> results = new ArrayList<>();
      try (Statement stmt = this.connection.createStatement()) {
        try (SqlRow row = new SqlRow(stmt.executeQuery(sqlSyntax))) {
          while (!row.isClosed() && row.next()) {
            T result = mapper.apply(row);
            results.add(result);
          }
        }
      }
      return results;
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Fetch all results that get automatically mapped from a {@link SqlRow} to the desired type {T}.
   *  <p>
   *  Don't use this method if there are too many (e.g. more than hundredths) rows.</p>
   *
   *  @param mapper The function to map from {@link SqlRow} to {T}.
   *  @param sqlSyntax The SQL syntax that will be turned into a prepared statement first.
   *  @param namedParameters Any named parameters to be set before executing the statement.
   *
   *  @return A possibly empty list of all results.
   */
  public <T> List<T> fetchAllWithParams(Function<SqlRow, T> mapper, String sqlSyntax, Map<String, Object> namedParameters) {
    try (ParameterizedSqlStatement stmt = this.createStatement(sqlSyntax)) {
      return this.fetchAllWithParams(mapper, stmt, namedParameters);
    }
  }

  /**
   *  Fetch all results that get automatically mapped from a {@link SqlRow} to the desired type {T}.
   *  <p>
   *  Don't use this method if there are too many (e.g. more than hundredths) rows.</p>
   *
   *  @param mapper The function to map from {@link SqlRow} to {T}.
   *  @param sqlSyntax The SQL syntax that will be turned into a prepared statement first.
   *  @param namedParameterProvider Function that can provide zero or more named parameters.
   *
   *  @return A possibly empty list of all results.
   */
  public <T> List<T> fetchAllWithParams(Function<SqlRow, T> mapper, String sqlSyntax, Consumer<Map<String, Object>> namedParameterProvider) {
    Contract.isNotNull("mapper", mapper);
    Contract.isNotNull("sqlSyntax", sqlSyntax);
    Contract.isNotNull("namedParameterProvider", namedParameterProvider);
    Map<String, Object> namedParams = new HashMap<>();
    if (namedParameterProvider != null) {
      namedParameterProvider.accept(namedParams);
    }
    try (ParameterizedSqlStatement stmt = this.createStatement(sqlSyntax)) {
      return this.fetchAllWithParams(mapper, stmt, namedParams);
    }
  }

  /**
   *  Fetch all results that get automatically mapped from a {@link SqlRow} to the desired type {T}.
   *  <p>
   *  Don't use this method if there are too many (e.g. more than hundredths) rows.</p>
   *
   *  @param mapper The function to map from {@link SqlRow} to {T}.
   *  @param stmt A prepared statement.
   *  @param namedParameters Any named parameters to be set before executing the statement.
   *
   *  @return A possibly empty list of all results.
   */
  public <T> List<T> fetchAllWithParams(Function<SqlRow, T> mapper, ParameterizedSqlStatement stmt, Map<String, Object> namedParameters) {
    Contract.isNotNull("mapper", mapper);
    Contract.isNotNull("stmt", stmt);
    Contract.isNotNull("namedParameters", namedParameters);
    // Set params
    for (String paramName : namedParameters.keySet()) {
      Object paramVal = namedParameters.get(paramName);
      stmt.setObject(paramName, paramVal);
    }
    // Execute
    List<T> results = new ArrayList<>();
    try (SqlRow row = new SqlRow(stmt.executeQuery())) {
      while (!row.isClosed() && row.next()) {
        T result = mapper.apply(row);
        results.add(result);
      }
    }
    return results;
  }

  /**
   *  Fetch all results that get automatically mapped from a {@link SqlRow} to the desired type {T}.
   *  <p>
   *  Don't use this method if there are too many (e.g. more than hundredths) rows.</p>
   *
   *  @param mapper The function to map from {@link SqlRow} to {T}.
   *  @param sqlSyntax The SQL syntax that will be turned into a prepared statement first.
   *  @param positionalParameters Any positional parameters to be set before executing the statement. The first array element will be associated with position 1.
   *
   *  @return A possibly empty list of all results.
   */
  public <T> List<T> fetchAllWithParams(Function<SqlRow, T> mapper, String sqlSyntax, Object... positionalParameters) {
    try (ParameterizedSqlStatement stmt = this.createStatement(sqlSyntax)) {
      return this.fetchAllWithParams(mapper, stmt, positionalParameters);
    }
  }

  /**
   *  Fetch all results that get automatically mapped from a {@link SqlRow} to the desired type {T}.
   *  <p>
   *  Don't use this method if there are too many (e.g. more than hundredths) rows.</p>
   *
   *  @param mapper The function to map from {@link SqlRow} to {T}.
   *  @param stmt A prepared statement.
   *  @param positionalParameters Any positional parameters to be set before executing the statement. The first array element will be associated with position 1.
   *
   *  @return A possibly empty list of all results.
   */
  public <T> List<T> fetchAllWithParams(Function<SqlRow, T> mapper, ParameterizedSqlStatement stmt, Object... positionalParameters) {
    Contract.isNotNull("mapper", mapper);
    Contract.isNotNull("stmt", stmt);
    // Set params
    if (positionalParameters != null) {
      for (int i = 0; i < positionalParameters.length; i++) {
        stmt.setObject((i+1), positionalParameters[i]);
      }
    }
    // Execute
    List<T> results = new ArrayList<>();
    try (SqlRow rs = stmt.executeQuery()) {
      while (!rs.isClosed() && rs.next()) {
        T result = mapper.apply(rs);
        results.add(result);
      }
    }
    return results;
  }

  /**
   *  Fetch all results that get automatically mapped from a {@link SqlRow} to the desired type {T}.
   *  <p>
   *  Don't use this method if there are too many (e.g. more than hundredths) rows.</p>
   *
   *  @param mapper The function to map from {@link SqlRow} to {T}.
   *  @param sqlSyntaxParts Valid SQL statement syntax, possibly spread over multiple parts, which will be concatenated with one intervening space in between.
   *                        At least one part must be provided.
   *
   *  @return A possibly empty list of all results.
   */
  public <T> Iterator<T> iterate(Function<SqlRow, T> mapper, String... sqlSyntaxParts) {
    Contract.isNotNull("mapper", mapper);
    Contract.isNotEmpty("sqlSyntaxParts", sqlSyntaxParts);
    try {
      String sqlSyntax = StringUtils.join(" ", sqlSyntaxParts);
      Statement stmt = this.connection.createStatement();
      SqlRow row = new SqlRow(stmt.executeQuery(sqlSyntax));
      return new TransformingResultSetIterator<>(row, mapper);
    }
    catch (Exception e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Fetch all results that get automatically mapped from a {@link SqlRow} to the desired type {T}.
   *  <p>
   *  Don't use this method if there are too many (e.g. more than hundredths) rows.</p>
   *
   *  @param mapper The function to map from {@link SqlRow} to {T}.
   *  @param sqlSyntax The SQL syntax that will be turned into a prepared statement first.
   *  @param namedParameters Any named parameters to be set before executing the statement.
   *
   *  @return A possibly empty list of all results.
   */
  public <T> Iterator<T> iterateWithParams(Function<SqlRow, T> mapper, String sqlSyntax, Map<String, Object> namedParameters) {
    ParameterizedSqlStatement stmt = this.createStatement(sqlSyntax);
    return this.iterateWithParams(mapper, stmt, namedParameters);
  }

  /**
   *  Fetch all results that get automatically mapped from a {@link SqlRow} to the desired type {T}.
   *  <p>
   *  Don't use this method if there are too many (e.g. more than hundredths) rows.</p>
   *
   *  @param mapper The function to map from {@link SqlRow} to {T}.
   *  @param sqlSyntax The SQL syntax that will be turned into a prepared statement first.
   *  @param namedParameterProvider Function that can provide zero or more named parameters.
   *
   *  @return A possibly empty list of all results.
   */
  public <T> Iterator<T> iterateWithParams(Function<SqlRow, T> mapper, String sqlSyntax, Consumer<Map<String, Object>> namedParameterProvider) {
    Contract.isNotNull("mapper", mapper);
    Contract.isNotNull("sqlSyntax", sqlSyntax);
    Contract.isNotNull("namedParameterProvider", namedParameterProvider);
    Map<String, Object> namedParams = new HashMap<>();
    if (namedParameterProvider != null) {
      namedParameterProvider.accept(namedParams);
    }
    ParameterizedSqlStatement stmt = this.createStatement(sqlSyntax);
    return this.iterateWithParams(mapper, stmt, namedParams);
  }

  /**
   *  Fetch all results that get automatically mapped from a {@link SqlRow} to the desired type {T}.
   *  <p>
   *  Don't use this method if there are too many (e.g. more than hundredths) rows.</p>
   *
   *  @param mapper The function to map from {@link SqlRow} to {T}.
   *  @param stmt A prepared statement.
   *  @param namedParameters Any named parameters to be set before executing the statement.
   *
   *  @return A possibly empty list of all results.
   */
  public <T> Iterator<T> iterateWithParams(Function<SqlRow, T> mapper, ParameterizedSqlStatement stmt, Map<String, Object> namedParameters) {
    Contract.isNotNull("mapper", mapper);
    Contract.isNotNull("stmt", stmt);
    Contract.isNotNull("namedParameters", namedParameters);
    // Set params
    for (String paramName : namedParameters.keySet()) {
      Object paramVal = namedParameters.get(paramName);
      stmt.setObject(paramName, paramVal);
    }
    // Execute
    return new TransformingResultSetIterator<>(stmt.executeQuery(), mapper);
  }

  /**
   *  Fetch all results that get automatically mapped from a {@link SqlRow} to the desired type {T}.
   *  <p>
   *  Don't use this method if there are too many (e.g. more than hundredths) rows.</p>
   *
   *  @param mapper The function to map from {@link SqlRow} to {T}.
   *  @param sqlSyntax The SQL syntax that will be turned into a prepared statement first.
   *  @param positionalParameters Any positional parameters to be set before executing the statement. The first array element will be associated with position 1.
   *
   *  @return A possibly empty list of all results.
   */
  public <T> Iterator<T> iterateWithParams(Function<SqlRow, T> mapper, String sqlSyntax, Object... positionalParameters) {
    ParameterizedSqlStatement stmt = this.createStatement(sqlSyntax);
    return this.iterateWithParams(mapper, stmt, positionalParameters);
  }

  /**
   *  Fetch all results that get automatically mapped from a {@link SqlRow} to the desired type {T}.
   *  <p>
   *  Don't use this method if there are too many (e.g. more than hundredths) rows.</p>
   *
   *  @param mapper The function to map from {@link SqlRow} to {T}.
   *  @param stmt A prepared statement.
   *  @param positionalParameters Any positional parameters to be set before executing the statement. The first array element will be associated with position 1.
   *
   *  @return A possibly empty list of all results.
   */
  public <T> Iterator<T> iterateWithParams(Function<SqlRow, T> mapper, ParameterizedSqlStatement stmt, Object... positionalParameters) {
    Contract.isNotNull("mapper", mapper);
    Contract.isNotNull("stmt", stmt);
    // Set params
    if (positionalParameters != null) {
      for (int i = 0; i < positionalParameters.length; i++) {
        stmt.setObject((i+1), positionalParameters[i]);
      }
    }
    // Execute
    return new TransformingResultSetIterator<>(stmt.executeQuery(), mapper);
  }

  /**
   *  Create a {@link SqlPager} capable of moving through an ordered result set on a page-by-page basis
   *  while also allowing to position after or before a previously returned page by enabling the retrieval
   *  and use of bookmarks.
   *  <p>
   *  Bookmarks should be considered opaque strings that are tied to the <c>baseQuerySql</c>.
   *  Any change in the SQL's <c>ORDER BY</c>, clause will likely make existing bookmarks defunct.</p>
   *  <p>
   *  The implementation currently relies on the underlying database supporting:<ol>
   *    <li>The <c>LIMIT</c>-clause for restricting the number of returned results</li>
   *  </ol>
   *  </p>
   *
   *  @param mapper The function to map from {@link SqlRow} to {T}.
   *  @param baseQuerySql The basic SQL to use for building queries capable of paging.
   *                      This basic SQL must:<ol>
   *                        <li>have an <c>ORDER BY</c> that guarantees a unique result order</li>,
   *                        <li>ensure that all <c>ORDER BY</c> columns are retrievable by name from the result</li>,
   *                        <li>ensure that each row has a unique ID that can also be retrieved from the result and that has either a numeric or string type</li>,
   *                        <li>not have a <c>WITH</c>-clause yet</li>
   *                      </ol>
   *  @param positionalParameters Any positional parameters to be set before executing the statement. The first array element will be associated with position 1.
   *
   *  @return A {@link SqlPager} that will not be positioned yet (i.e. not have retrieved a results page yet) but be ready to do so.
   */
  public <T> SqlPager<T> createPagerWithParams(Function<SqlRow, T> mapper, String baseQuerySql, Object... positionalParameters) {
    try {
      return new ColumnValuesBasedSqlPager<>(this, mapper, baseQuerySql, null, positionalParameters);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Create a {@link SqlPager} capable of moving through an ordered result set on a page-by-page basis
   *  while also allowing to position after or before a previously returned page by enabling the retrieval
   *  and use of bookmarks.
   *  <p>
   *  Bookmarks should be considered opaque strings that are tied to the <c>baseQuerySql</c>.
   *  Any change in the SQL's <c>ORDER BY</c>, clause will likely make existing bookmarks defunct.</p>
   *  <p>
   *  The implementation currently relies on the underlying database supporting:<ol>
   *    <li>The <c>LIMIT</c>-clause for restricting the number of returned results</li>
   *  </ol>
   *  </p>
   *
   *  @param mapper The function to map from {@link SqlRow} to {T}.
   *  @param baseQuerySql The basic SQL to use for building queries capable of paging.
   *                      This basic SQL must:<ol>
   *                        <li>have an <c>ORDER BY</c> that guarantees a unique result order</li>,
   *                        <li>ensure that all <c>ORDER BY</c> columns are retrievable by name from the result</li>,
   *                        <li>ensure that each row has a unique ID that can also be retrieved from the result and that has either a numeric or string type</li>,
   *                        <li>not have a <c>WITH</c>-clause yet</li>
   *                      </ol>
   *  @param namedParameters Any named parameters to be set before executing the statement.
   *
   *  @return A {@link SqlPager} that will not be positioned yet (i.e. not have retrieved a results page yet) but be ready to do so.
   */
  public <T> SqlPager<T> createPagerWithParams(Function<SqlRow, T> mapper, String baseQuerySql, Map<String, Object> namedParameters) {
    try {
      return new ColumnValuesBasedSqlPager<>(this, mapper, baseQuerySql, namedParameters, new Object[0]);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Create a {@link SqlPager} capable of moving through an ordered result set on a page-by-page basis
   *  while also allowing to position after or before a previously returned page by enabling the retrieval
   *  and use of bookmarks.
   *  <p>
   *  Bookmarks should be considered opaque strings that are tied to the <c>baseQuerySql</c>.
   *  Any change in the SQL's <c>ORDER BY</c>, clause will likely make existing bookmarks defunct.</p>
   *  <p>
   *  The implementation currently relies on the underlying database supporting:<ol>
   *    <li>The <c>LIMIT</c>-clause for restricting the number of returned results</li>
   *  </ol>
   *  </p>
   *
   *  @param mapper The function to map from {@link SqlRow} to {T}.
   *  @param baseQuerySql The basic SQL to use for building queries capable of paging.
   *                      This basic SQL must:<ol>
   *                        <li>have an <c>ORDER BY</c> that guarantees a unique result order</li>,
   *                        <li>ensure that all <c>ORDER BY</c> columns are retrievable by name from the result</li>,
   *                        <li>ensure that each row has a unique ID that can also be retrieved from the result and that has either a numeric or string type</li>,
   *                        <li>not have a <c>WITH</c>-clause yet</li>
   *                      </ol>
   *  @param namedParameters Any named parameters to be set before executing the statement.
   *  @param positionalParameters Any positional parameters to be set before executing the statement. The first array element will be associated with position 1.
   *
   *  @return A {@link SqlPager} that will not be positioned yet (i.e. not have retrieved a results page yet) but be ready to do so.
   */
  public <T> SqlPager<T> createPagerWithParams(Function<SqlRow, T> mapper, String baseQuerySql, Map<String, Object> namedParameters, Object[] positionalParameters) {
    try {
      return new ColumnValuesBasedSqlPager<>(this, mapper, baseQuerySql, namedParameters, positionalParameters);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Create a {@link SqlPager} capable of moving through an ordered result set on a page-by-page basis
   *  while also allowing to position after or before a previously returned page by enabling the retrieval
   *  and use of bookmarks.
   *  <p>
   *  Bookmarks should be considered opaque strings that are tied to the <c>baseQuerySql</c>.
   *  Any change in the SQL's <c>ORDER BY</c>, clause will likely make existing bookmarks defunct.</p>
   *  <p>
   *  The implementation currently relies on the underlying database supporting:<ol>
   *    <li>The <c>WITH</c>-clause for selecting common table expressions</li>
   *    <li>The <c>LIMIT</c>-clause for restricting the number of returned results</li>
   *  </ol>
   *  </p>
   *
   *  @param mapper The function to map from {@link SqlRow} to {T}.
   *  @param idColName Name of the unique ID column.
   *  @param idColType The type of the ID column, expressed as {@link java.sql.Types} constant. Currently restricted to <c>CHAR, VARCHAR, INTEGER, BIGINT, NUMERIC</c>
   *  @param baseQuerySql The basic SQL to use for building queries capable of paging.
   *                      This basic SQL must:<ol>
   *                        <li>have an <c>ORDER BY</c> that guarantees a unique result order</li>,
   *                        <li>ensure that all <c>ORDER BY</c> columns are retrievable by name from the result</li>,
   *                        <li>ensure that each row has a unique ID that can also be retrieved from the result and that has either a numeric or string type</li>,
   *                        <li>not have a <c>WITH</c>-clause yet</li>
   *                      </ol>
   *
   *  @return A {@link SqlPager} that will not be positioned yet (i.e. not have retrieved a results page yet) but be ready to do so.
   */
  public <T> SqlPager<T> createPager(Function<SqlRow, T> mapper, String idColName, int idColType, String baseQuerySql) {
    return new IdColumnBasedSqlPager<T>(this, mapper, idColName, idColType, baseQuerySql);
  }
}
