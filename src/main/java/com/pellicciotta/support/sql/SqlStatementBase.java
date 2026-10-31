// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.sql;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;

import com.pellicciotta.support.exceptions.SqlRuntimeException;
import com.pellicciotta.support.utils.Contract;
import com.pellicciotta.support.utils.StringConversionUtils;

/**
 *  Class implementing and wrapping an underlying {@link PreparedStatement} while ensuring that all
 *  methods throw the unchecked {@link SqlRuntimeException} instead of the checked {@link SQLException}.
 *  <p>
 *  All {@link ResultSet} returning methods also return {@link SqlRow} instead, which also is a
 *  simple wrapper that ensures throwning unchecked {@link SqlRuntimeException} instead of the
 *  checked {@link SQLException}.</p>
 */
public class SqlStatementBase implements PreparedStatement {
  // <editor-fold desc="Construction">

  protected SqlStatementBase(PreparedStatement wrapped) {
    Contract.isNotNull("wrapped", wrapped);
    this.wrapped = wrapped;
  }

  protected final PreparedStatement wrapped;

  // </editor-fold>
  // <editor-fold desc="PreparedStatement Interface">

  /** {@inheritDoc} */
  @Override
  public ResultSetMetaData getMetaData() throws SqlRuntimeException {
    try {
      return this.wrapped.getMetaData();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public ParameterMetaData getParameterMetaData() throws SqlRuntimeException {
    try {
      return this.wrapped.getParameterMetaData();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void clearParameters() throws SqlRuntimeException {
    try {
      this.wrapped.clearParameters();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setNull(int parameterIndex, int sqlType) throws SqlRuntimeException {
    try {
      this.wrapped.setNull(parameterIndex, sqlType);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setNull(int parameterIndex, int sqlType, String typeName) throws SqlRuntimeException {
    try {
      this.wrapped.setNull(parameterIndex, sqlType, typeName);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setBoolean(int parameterIndex, boolean x) throws SqlRuntimeException {
    try {
      this.wrapped.setBoolean(parameterIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setByte(int parameterIndex, byte x) throws SqlRuntimeException {
    try {
      this.wrapped.setByte(parameterIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setShort(int parameterIndex, short x) throws SqlRuntimeException {
    try {
      this.wrapped.setShort(parameterIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setInt(int parameterIndex, int x) throws SqlRuntimeException {
    try {
      this.wrapped.setInt(parameterIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setLong(int parameterIndex, long x) throws SqlRuntimeException {
    try {
      this.wrapped.setLong(parameterIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setFloat(int parameterIndex, float x) throws SqlRuntimeException {
    try {
      this.wrapped.setFloat(parameterIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setDouble(int parameterIndex, double x) throws SqlRuntimeException {
    try {
      this.wrapped.setDouble(parameterIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setBigDecimal(int parameterIndex, BigDecimal x) throws SqlRuntimeException {
    try {
      this.wrapped.setBigDecimal(parameterIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setString(int parameterIndex, String x) throws SqlRuntimeException {
    try {
      this.wrapped.setString(parameterIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setNString(int parameterIndex, String value) throws SqlRuntimeException {
    try {
      this.wrapped.setNString(parameterIndex, value);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setBytes(int parameterIndex, byte[] x) throws SqlRuntimeException {
    try {
      this.wrapped.setBytes(parameterIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setDate(int parameterIndex, Date x) throws SqlRuntimeException {
    try {
      this.wrapped.setDate(parameterIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setDate(int parameterIndex, Date x, Calendar cal) throws SqlRuntimeException {
    try {
      this.wrapped.setDate(parameterIndex, x, cal);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setTime(int parameterIndex, Time x) throws SqlRuntimeException {
    try {
      this.wrapped.setTime(parameterIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setTime(int parameterIndex, Time x, Calendar cal) throws SqlRuntimeException {
    try {
      this.wrapped.setTime(parameterIndex, x, cal);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setTimestamp(int parameterIndex, Timestamp x) throws SqlRuntimeException {
    try {
      this.wrapped.setTimestamp(parameterIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SqlRuntimeException {
    try {
      this.wrapped.setTimestamp(parameterIndex, x, cal);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setAsciiStream(int parameterIndex, InputStream x) throws SqlRuntimeException {
    try {
      this.wrapped.setAsciiStream(parameterIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SqlRuntimeException {
    try {
      this.wrapped.setAsciiStream(parameterIndex, x, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SqlRuntimeException {
    try {
      this.wrapped.setAsciiStream(parameterIndex, x, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setBinaryStream(int parameterIndex, InputStream x) throws SqlRuntimeException {
    try {
      this.wrapped.setBinaryStream(parameterIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SqlRuntimeException {
    try {
      this.wrapped.setBinaryStream(parameterIndex, x, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SqlRuntimeException {
    try {
      this.wrapped.setBinaryStream(parameterIndex, x, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setCharacterStream(int parameterIndex, Reader reader) throws SqlRuntimeException {
    try {
      this.wrapped.setCharacterStream(parameterIndex, reader);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SqlRuntimeException {
    try {
      this.wrapped.setCharacterStream(parameterIndex, reader, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SqlRuntimeException {
    try {
      this.wrapped.setCharacterStream(parameterIndex, reader, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setNCharacterStream(int parameterIndex, Reader value) throws SqlRuntimeException {
    try {
      this.wrapped.setNCharacterStream(parameterIndex, value);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SqlRuntimeException {
    try {
      this.wrapped.setNCharacterStream(parameterIndex, value, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  @Deprecated(since = "1.2")
  public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SqlRuntimeException {
    try {
      this.wrapped.setUnicodeStream(parameterIndex, x, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setRef(int parameterIndex, Ref x) throws SqlRuntimeException {
    try {
      this.wrapped.setRef(parameterIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setArray(int parameterIndex, Array x) throws SqlRuntimeException {
    try {
      this.wrapped.setArray(parameterIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setURL(int parameterIndex, URL x) throws SqlRuntimeException {
    try {
      this.wrapped.setURL(parameterIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setRowId(int parameterIndex, RowId x) throws SqlRuntimeException {
    try {
      this.wrapped.setRowId(parameterIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setClob(int parameterIndex, Clob x) throws SqlRuntimeException {
    try {
      this.wrapped.setClob(parameterIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setClob(int parameterIndex, Reader reader) throws SqlRuntimeException {
    try {
      this.wrapped.setClob(parameterIndex, reader);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setClob(int parameterIndex, Reader reader, long length) throws SqlRuntimeException {
    try {
      this.wrapped.setClob(parameterIndex, reader, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setNClob(int parameterIndex, NClob value) throws SqlRuntimeException {
    try {
      this.wrapped.setNClob(parameterIndex, value);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setNClob(int parameterIndex, Reader reader) throws SqlRuntimeException {
    try {
      this.wrapped.setNClob(parameterIndex, reader);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setNClob(int parameterIndex, Reader reader, long length) throws SqlRuntimeException {
    try {
      this.wrapped.setNClob(parameterIndex, reader, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setBlob(int parameterIndex, Blob x) throws SqlRuntimeException {
    try {
      this.wrapped.setBlob(parameterIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setBlob(int parameterIndex, InputStream inputStream) throws SqlRuntimeException {
    try {
      this.wrapped.setBlob(parameterIndex, inputStream);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SqlRuntimeException {
    try {
      this.wrapped.setBlob(parameterIndex, inputStream, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SqlRuntimeException {
    try {
      this.wrapped.setSQLXML(parameterIndex, xmlObject);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setObject(int parameterIndex, Object x) throws SqlRuntimeException {
    try {
      if (x instanceof Enum) {
        this.wrapped.setString(parameterIndex, StringConversionUtils.fromEnum((Enum)x));
      }
      else {
        this.wrapped.setObject(parameterIndex, x);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setObject(int parameterIndex, Object x, int targetSqlType) throws SqlRuntimeException {
    try {
      this.wrapped.setObject(parameterIndex, x, targetSqlType);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SqlRuntimeException {
    try {
      this.wrapped.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setObject(int parameterIndex, Object x, SQLType targetSqlType) throws SqlRuntimeException {
    try {
      this.wrapped.setObject(parameterIndex, x, targetSqlType);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setObject(int parameterIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SqlRuntimeException {
    try {
      this.wrapped.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public SqlRow executeQuery() throws SqlRuntimeException {
    try {
      return new SqlRow(this.wrapped.executeQuery());
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public int executeUpdate() throws SqlRuntimeException {
    try {
      return this.wrapped.executeUpdate();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public long executeLargeUpdate() throws SqlRuntimeException {
    try {
      return this.wrapped.executeLargeUpdate();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public SqlRow executeQuery(String sql) throws SqlRuntimeException {
    try {
      return new SqlRow(this.wrapped.executeQuery(sql));
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public int executeUpdate(String sql) throws SqlRuntimeException {
    try {
      return this.wrapped.executeUpdate(sql);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void close() throws SqlRuntimeException {
    try {
      this.wrapped.close();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public int getMaxFieldSize() throws SqlRuntimeException {
    try {
      return this.wrapped.getMaxFieldSize();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setMaxFieldSize(int max) throws SqlRuntimeException {
    try {
      this.wrapped.setMaxFieldSize(max);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public int getMaxRows() throws SqlRuntimeException {
    try {
      return this.wrapped.getMaxRows();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setMaxRows(int max) throws SqlRuntimeException {
    try {
      this.wrapped.setMaxRows(max);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setEscapeProcessing(boolean enable) throws SqlRuntimeException {
    try {
      this.wrapped.setEscapeProcessing(enable);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public int getQueryTimeout() throws SqlRuntimeException {
    try {
      return this.wrapped.getQueryTimeout();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setQueryTimeout(int seconds) throws SqlRuntimeException {
    try {
      this.wrapped.setQueryTimeout(seconds);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void cancel() throws SqlRuntimeException {
    try {
      this.wrapped.cancel();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public SQLWarning getWarnings() throws SqlRuntimeException {
    try {
      return this.wrapped.getWarnings();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void clearWarnings() throws SqlRuntimeException {
    try {
      this.wrapped.clearWarnings();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setCursorName(String name) throws SqlRuntimeException {
    try {
      this.wrapped.setCursorName(name);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean execute(String sql) throws SqlRuntimeException {
    try {
      return this.wrapped.execute(sql);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public SqlRow getResultSet() throws SqlRuntimeException {
    try {
      return new SqlRow(this.wrapped.getResultSet());
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public int getUpdateCount() throws SqlRuntimeException {
    try {
      return this.wrapped.getUpdateCount();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean getMoreResults() throws SqlRuntimeException {
    try {
      return this.wrapped.getMoreResults();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setFetchDirection(int direction) throws SqlRuntimeException {
    try {
      this.wrapped.setFetchDirection(direction);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public int getFetchDirection() throws SqlRuntimeException {
    try {
      return this.wrapped.getFetchDirection();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setFetchSize(int rows) throws SqlRuntimeException {
    try {
      this.wrapped.setFetchSize(rows);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public int getFetchSize() throws SqlRuntimeException {
    try {
      return this.wrapped.getFetchSize();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public int getResultSetConcurrency() throws SqlRuntimeException {
    try {
      return this.wrapped.getResultSetConcurrency();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public int getResultSetType() throws SqlRuntimeException {
    try {
      return this.wrapped.getResultSetType();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void addBatch() throws SqlRuntimeException {
    try {
      this.wrapped.addBatch();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void addBatch(String sql) throws SqlRuntimeException {
    try {
      this.wrapped.addBatch(sql);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void clearBatch() throws SqlRuntimeException {
    try {
      this.wrapped.clearBatch();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean execute() throws SqlRuntimeException {
    try {
      return this.wrapped.execute();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public int[] executeBatch() throws SqlRuntimeException {
    try {
      return this.wrapped.executeBatch();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public Connection getConnection() throws SqlRuntimeException {
    try {
      return this.wrapped.getConnection();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean getMoreResults(int current) throws SqlRuntimeException {
    try {
      return this.wrapped.getMoreResults(current);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public SqlRow getGeneratedKeys() throws SqlRuntimeException {
    try {
      return new SqlRow(this.wrapped.getGeneratedKeys());
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public int executeUpdate(String sql, int autoGeneratedKeys) throws SqlRuntimeException {
    try {
      return this.wrapped.executeUpdate(sql, autoGeneratedKeys);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public int executeUpdate(String sql, int[] columnIndexes) throws SqlRuntimeException {
    try {
      return this.wrapped.executeUpdate(sql, columnIndexes);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public int executeUpdate(String sql, String[] columnNames) throws SqlRuntimeException {
    try {
      return this.wrapped.executeUpdate(sql, columnNames);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean execute(String sql, int autoGeneratedKeys) throws SqlRuntimeException {
    try {
      return this.wrapped.execute(sql, autoGeneratedKeys);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean execute(String sql, int[] columnIndexes) throws SqlRuntimeException {
    try {
      return this.wrapped.execute(sql, columnIndexes);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean execute(String sql, String[] columnNames) throws SqlRuntimeException {
    try {
      return this.wrapped.execute(sql, columnNames);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public int getResultSetHoldability() throws SqlRuntimeException {
    try {
      return this.wrapped.getResultSetHoldability();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean isClosed() throws SqlRuntimeException {
    try {
      return this.wrapped.isClosed();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setPoolable(boolean poolable) throws SqlRuntimeException {
    try {
      this.wrapped.setPoolable(poolable);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean isPoolable() throws SqlRuntimeException {
    try {
      return this.wrapped.isPoolable();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void closeOnCompletion() throws SqlRuntimeException {
    try {
      this.wrapped.closeOnCompletion();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean isCloseOnCompletion() throws SqlRuntimeException {
    try {
      return this.wrapped.isCloseOnCompletion();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public long getLargeUpdateCount() throws SqlRuntimeException {
    try {
      return this.wrapped.getLargeUpdateCount();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void setLargeMaxRows(long max) throws SqlRuntimeException {
    try {
      this.wrapped.setLargeMaxRows(max);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public long getLargeMaxRows() throws SqlRuntimeException {
    try {
      return this.wrapped.getLargeMaxRows();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public long[] executeLargeBatch() throws SqlRuntimeException {
    try {
      return this.wrapped.executeLargeBatch();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public long executeLargeUpdate(String sql) throws SqlRuntimeException {
    try {
      return this.wrapped.executeLargeUpdate(sql);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SqlRuntimeException {
    try {
      return this.wrapped.executeLargeUpdate(sql, autoGeneratedKeys);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public long executeLargeUpdate(String sql, int[] columnIndexes) throws SqlRuntimeException {
    try {
      return this.wrapped.executeLargeUpdate(sql, columnIndexes);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public long executeLargeUpdate(String sql, String[] columnNames) throws SqlRuntimeException {
    try {
      return this.wrapped.executeLargeUpdate(sql, columnNames);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public String enquoteLiteral(String val) throws SqlRuntimeException {
    try {
      return this.wrapped.enquoteLiteral(val);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public String enquoteIdentifier(String identifier, boolean alwaysQuote) throws SqlRuntimeException {
    try {
      return this.wrapped.enquoteIdentifier(identifier, alwaysQuote);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean isSimpleIdentifier(String identifier) throws SqlRuntimeException {
    try {
      return this.wrapped.isSimpleIdentifier(identifier);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public String enquoteNCharLiteral(String val) throws SqlRuntimeException {
    try {
      return this.wrapped.enquoteNCharLiteral(val);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public <T> T unwrap(Class<T> iface) throws SqlRuntimeException {
    try {
      if (iface.isAssignableFrom(this.wrapped.getClass())) {
        return (T)this.wrapped;
      }
      else {
        return this.wrapped.unwrap(iface);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean isWrapperFor(Class<?> iface) throws SqlRuntimeException {
    try {
      return iface.isAssignableFrom(this.wrapped.getClass()) || this.wrapped.isWrapperFor(iface);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  // </editor-fold>
}
