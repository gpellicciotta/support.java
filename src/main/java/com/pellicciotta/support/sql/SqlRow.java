// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.sql;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;

import com.pellicciotta.support.exceptions.SqlRuntimeException;
import com.pellicciotta.support.utils.Contract;

/**
 *  Class implementing and wrapping an underlying {@link ResultSet} while ensuring that all
 *  methods throw the unchecked {@link SqlRuntimeException} instead of the checked {@link SQLException}.
 */
public class SqlRow implements ResultSet {
  // <editor-fold desc="Construction">

  /**
   *  Wraps the given result set.
   *
   *  @param wrapped The result set to delegate to; must not be null.
   */
  protected SqlRow(ResultSet wrapped) {
    Contract.isNotNull("wrapped", wrapped);
    this.wrapped = wrapped;
  }

  /** The underlying result set all calls are delegated to. */
  protected final ResultSet wrapped;

  // </editor-fold>
  // <editor-fold desc="PreparedStatement Interface">

  @Override
  public boolean next() throws SqlRuntimeException {
    try {
      return this.wrapped.next();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void close() throws SqlRuntimeException {
    try {
      this.wrapped.close();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public boolean wasNull() throws SqlRuntimeException {
    try {
      return this.wrapped.wasNull();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public String getString(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getString(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public boolean getBoolean(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getBoolean(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public byte getByte(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getByte(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public short getShort(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getShort(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public int getInt(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getInt(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public long getLong(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getLong(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public float getFloat(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getFloat(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public double getDouble(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getDouble(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  @Deprecated(since = "1.2")
  public BigDecimal getBigDecimal(int columnIndex, int scale) throws SqlRuntimeException {
    try {
      return this.wrapped.getBigDecimal(columnIndex, scale);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public byte[] getBytes(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getBytes(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Date getDate(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getDate(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Time getTime(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getTime(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Timestamp getTimestamp(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getTimestamp(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public InputStream getAsciiStream(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getAsciiStream(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  @Deprecated(since = "1.2")
  public InputStream getUnicodeStream(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getUnicodeStream(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public InputStream getBinaryStream(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getBinaryStream(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public String getString(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getString(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public boolean getBoolean(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getBoolean(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public byte getByte(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getByte(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public short getShort(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getShort(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public int getInt(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getInt(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public long getLong(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getLong(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public float getFloat(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getFloat(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public double getDouble(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getDouble(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  @Deprecated(since = "1.2")
  public BigDecimal getBigDecimal(String columnLabel, int scale) throws SqlRuntimeException {
    try {
      return this.wrapped.getBigDecimal(columnLabel, scale);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public byte[] getBytes(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getBytes(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Date getDate(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getDate(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Time getTime(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getTime(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Timestamp getTimestamp(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getTimestamp(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public InputStream getAsciiStream(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getAsciiStream(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  @Deprecated(since = "1.2")
  public InputStream getUnicodeStream(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getUnicodeStream(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public InputStream getBinaryStream(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getBinaryStream(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public SQLWarning getWarnings() throws SqlRuntimeException {
    try {
      return this.wrapped.getWarnings();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void clearWarnings() throws SqlRuntimeException {
    try {
      this.wrapped.clearWarnings();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public String getCursorName() throws SqlRuntimeException {
    try {
      return this.wrapped.getCursorName();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public ResultSetMetaData getMetaData() throws SqlRuntimeException {
    try {
      return this.wrapped.getMetaData();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Object getObject(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getObject(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Object getObject(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getObject(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public int findColumn(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.findColumn(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Reader getCharacterStream(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getCharacterStream(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Reader getCharacterStream(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getCharacterStream(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public BigDecimal getBigDecimal(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getBigDecimal(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public BigDecimal getBigDecimal(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getBigDecimal(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public boolean isBeforeFirst() throws SqlRuntimeException {
    try {
      return this.wrapped.isBeforeFirst();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public boolean isAfterLast() throws SqlRuntimeException {
    try {
      return this.wrapped.isAfterLast();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public boolean isFirst() throws SqlRuntimeException {
    try {
      return this.wrapped.isFirst();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public boolean isLast() throws SqlRuntimeException {
    try {
      return this.wrapped.isLast();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void beforeFirst() throws SqlRuntimeException {
    try {
      this.wrapped.beforeFirst();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void afterLast() throws SqlRuntimeException {
    try {
      this.wrapped.afterLast();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public boolean first() throws SqlRuntimeException {
    try {
      return this.wrapped.first();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public boolean last() throws SqlRuntimeException {
    try {
      return this.wrapped.last();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public int getRow() throws SqlRuntimeException {
    try {
      return this.wrapped.getRow();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public boolean absolute(int row) throws SqlRuntimeException {
    try {
      return this.wrapped.absolute(row);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public boolean relative(int rows) throws SqlRuntimeException {
    try {
      return this.wrapped.relative(rows);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public boolean previous() throws SqlRuntimeException {
    try {
      return this.wrapped.previous();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void setFetchDirection(int direction) throws SqlRuntimeException {
    try {
      this.wrapped.setFetchDirection(direction);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public int getFetchDirection() throws SqlRuntimeException {
    try {
      return this.wrapped.getFetchDirection();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void setFetchSize(int rows) throws SqlRuntimeException {
    try {
      this.wrapped.setFetchSize(rows);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public int getFetchSize() throws SqlRuntimeException {
    try {
      return this.wrapped.getFetchSize();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public int getType() throws SqlRuntimeException {
    try {
      return this.wrapped.getType();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public int getConcurrency() throws SqlRuntimeException {
    try {
      return this.wrapped.getConcurrency();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public boolean rowUpdated() throws SqlRuntimeException {
    try {
      return this.wrapped.rowUpdated();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public boolean rowInserted() throws SqlRuntimeException {
    try {
      return this.wrapped.rowInserted();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public boolean rowDeleted() throws SqlRuntimeException {
    try {
      return this.wrapped.rowDeleted();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateNull(int columnIndex) throws SqlRuntimeException {
    try {
      this.wrapped.updateNull(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateBoolean(int columnIndex, boolean x) throws SqlRuntimeException {
    try {
      this.wrapped.updateBoolean(columnIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateByte(int columnIndex, byte x) throws SqlRuntimeException {
    try {
      this.wrapped.updateByte(columnIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateShort(int columnIndex, short x) throws SqlRuntimeException {
    try {
      this.wrapped.updateShort(columnIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateInt(int columnIndex, int x) throws SqlRuntimeException {
    try {
      this.wrapped.updateInt(columnIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateLong(int columnIndex, long x) throws SqlRuntimeException {
    try {
      this.wrapped.updateLong(columnIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateFloat(int columnIndex, float x) throws SqlRuntimeException {
    try {
      this.wrapped.updateFloat(columnIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateDouble(int columnIndex, double x) throws SqlRuntimeException {
    try {
      this.wrapped.updateDouble(columnIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateBigDecimal(int columnIndex, BigDecimal x) throws SqlRuntimeException {
    try {
      this.wrapped.updateBigDecimal(columnIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateString(int columnIndex, String x) throws SqlRuntimeException {
    try {
      this.wrapped.updateString(columnIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateBytes(int columnIndex, byte[] x) throws SqlRuntimeException {
    try {
      this.wrapped.updateBytes(columnIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateDate(int columnIndex, Date x) throws SqlRuntimeException {
    try {
      this.wrapped.updateDate(columnIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateTime(int columnIndex, Time x) throws SqlRuntimeException {
    try {
      this.wrapped.updateTime(columnIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateTimestamp(int columnIndex, Timestamp x) throws SqlRuntimeException {
    try {
      this.wrapped.updateTimestamp(columnIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SqlRuntimeException {
    try {
      this.wrapped.updateAsciiStream(columnIndex, x, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SqlRuntimeException {
    try {
      this.wrapped.updateBinaryStream(columnIndex, x, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateCharacterStream(int columnIndex, Reader x, int length) throws SqlRuntimeException {
    try {
      this.wrapped.updateCharacterStream(columnIndex, x, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SqlRuntimeException {
    try {
      this.wrapped.updateObject(columnIndex, x, scaleOrLength);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateObject(int columnIndex, Object x) throws SqlRuntimeException {
    try {
      this.wrapped.updateObject(columnIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateNull(String columnLabel) throws SqlRuntimeException {
    try {
      this.wrapped.updateNull(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateBoolean(String columnLabel, boolean x) throws SqlRuntimeException {
    try {
      this.wrapped.updateBoolean(columnLabel, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateByte(String columnLabel, byte x) throws SqlRuntimeException {
    try {
      this.wrapped.updateByte(columnLabel, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateShort(String columnLabel, short x) throws SqlRuntimeException {
    try {
      this.wrapped.updateShort(columnLabel, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateInt(String columnLabel, int x) throws SqlRuntimeException {
    try {
      this.wrapped.updateInt(columnLabel, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateLong(String columnLabel, long x) throws SqlRuntimeException {
    try {
      this.wrapped.updateLong(columnLabel, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateFloat(String columnLabel, float x) throws SqlRuntimeException {
    try {
      this.wrapped.updateFloat(columnLabel, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateDouble(String columnLabel, double x) throws SqlRuntimeException {
    try {
      this.wrapped.updateDouble(columnLabel, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateBigDecimal(String columnLabel, BigDecimal x) throws SqlRuntimeException {
    try {
      this.wrapped.updateBigDecimal(columnLabel, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateString(String columnLabel, String x) throws SqlRuntimeException {
    try {
      this.wrapped.updateString(columnLabel, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateBytes(String columnLabel, byte[] x) throws SqlRuntimeException {
    try {
      this.wrapped.updateBytes(columnLabel, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateDate(String columnLabel, Date x) throws SqlRuntimeException {
    try {
      this.wrapped.updateDate(columnLabel, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateTime(String columnLabel, Time x) throws SqlRuntimeException {
    try {
      this.wrapped.updateTime(columnLabel, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateTimestamp(String columnLabel, Timestamp x) throws SqlRuntimeException {
    try {
      this.wrapped.updateTimestamp(columnLabel, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SqlRuntimeException {
    try {
      this.wrapped.updateAsciiStream(columnLabel, x, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SqlRuntimeException {
    try {
      this.wrapped.updateBinaryStream(columnLabel, x, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SqlRuntimeException {
    try {
      this.wrapped.updateCharacterStream(columnLabel, reader, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SqlRuntimeException {
    try {
      this.wrapped.updateObject(columnLabel, x, scaleOrLength);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateObject(String columnLabel, Object x) throws SqlRuntimeException {
    try {
      this.wrapped.updateObject(columnLabel, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void insertRow() throws SqlRuntimeException {
    try {
      this.wrapped.insertRow();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateRow() throws SqlRuntimeException {
    try {
      this.wrapped.updateRow();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void deleteRow() throws SqlRuntimeException {
    try {
      this.wrapped.deleteRow();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void refreshRow() throws SqlRuntimeException {
    try {
      this.wrapped.refreshRow();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void cancelRowUpdates() throws SqlRuntimeException {
    try {
      this.wrapped.cancelRowUpdates();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void moveToInsertRow() throws SqlRuntimeException {
    try {
      this.wrapped.moveToInsertRow();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void moveToCurrentRow() throws SqlRuntimeException {
    try {
      this.wrapped.moveToCurrentRow();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Statement getStatement() throws SqlRuntimeException {
    try {
      return this.wrapped.getStatement();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SqlRuntimeException {
    try {
      return this.wrapped.getObject(columnIndex, map);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Ref getRef(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getRef(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Blob getBlob(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getBlob(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Clob getClob(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getClob(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Array getArray(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getArray(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SqlRuntimeException {
    try {
      return this.wrapped.getObject(columnLabel, map);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Ref getRef(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getRef(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Blob getBlob(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getBlob(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Clob getClob(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getClob(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Array getArray(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getArray(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Date getDate(int columnIndex, Calendar cal) throws SqlRuntimeException {
    try {
      return this.wrapped.getDate(columnIndex, cal);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Date getDate(String columnLabel, Calendar cal) throws SqlRuntimeException {
    try {
      return this.wrapped.getDate(columnLabel, cal);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Time getTime(int columnIndex, Calendar cal) throws SqlRuntimeException {
    try {
      return this.wrapped.getTime(columnIndex, cal);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Time getTime(String columnLabel, Calendar cal) throws SqlRuntimeException {
    try {
      return this.wrapped.getTime(columnLabel, cal);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SqlRuntimeException {
    try {
      return this.wrapped.getTimestamp(columnIndex, cal);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SqlRuntimeException {
    try {
      return this.wrapped.getTimestamp(columnLabel, cal);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public URL getURL(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getURL(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public URL getURL(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getURL(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateRef(int columnIndex, Ref x) throws SqlRuntimeException {
    try {
      this.wrapped.updateRef(columnIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateRef(String columnLabel, Ref x) throws SqlRuntimeException {
    try {
      this.wrapped.updateRef(columnLabel, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateBlob(int columnIndex, Blob x) throws SqlRuntimeException {
    try {
      this.wrapped.updateBlob(columnIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateBlob(String columnLabel, Blob x) throws SqlRuntimeException {
    try {
      this.wrapped.updateBlob(columnLabel, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateClob(int columnIndex, Clob x) throws SqlRuntimeException {
    try {
      this.wrapped.updateClob(columnIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateClob(String columnLabel, Clob x) throws SqlRuntimeException {
    try {
      this.wrapped.updateClob(columnLabel, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateArray(int columnIndex, Array x) throws SqlRuntimeException {
    try {
      this.wrapped.updateArray(columnIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateArray(String columnLabel, Array x) throws SqlRuntimeException {
    try {
      this.wrapped.updateArray(columnLabel, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public RowId getRowId(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getRowId(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public RowId getRowId(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getRowId(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateRowId(int columnIndex, RowId x) throws SqlRuntimeException {
    try {
      this.wrapped.updateRowId(columnIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateRowId(String columnLabel, RowId x) throws SqlRuntimeException {
    try {
      this.wrapped.updateRowId(columnLabel, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public int getHoldability() throws SqlRuntimeException {
    try {
      return this.wrapped.getHoldability();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public boolean isClosed() throws SqlRuntimeException {
    try {
      return this.wrapped.isClosed();
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateNString(int columnIndex, String nString) throws SqlRuntimeException {
    try {
      this.wrapped.updateNString(columnIndex, nString);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateNString(String columnLabel, String nString) throws SqlRuntimeException {
    try {
      this.wrapped.updateNString(columnLabel, nString);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateNClob(int columnIndex, NClob nClob) throws SqlRuntimeException {
    try {
      this.wrapped.updateNClob(columnIndex, nClob);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateNClob(String columnLabel, NClob nClob) throws SqlRuntimeException {
    try {
      this.wrapped.updateNClob(columnLabel, nClob);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public NClob getNClob(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getNClob(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public NClob getNClob(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getNClob(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public SQLXML getSQLXML(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getSQLXML(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public SQLXML getSQLXML(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getSQLXML(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SqlRuntimeException {
    try {
      this.wrapped.updateSQLXML(columnIndex, xmlObject);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SqlRuntimeException {
    try {
      this.wrapped.updateSQLXML(columnLabel, xmlObject);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public String getNString(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getNString(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public String getNString(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getNString(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Reader getNCharacterStream(int columnIndex) throws SqlRuntimeException {
    try {
      return this.wrapped.getNCharacterStream(columnIndex);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public Reader getNCharacterStream(String columnLabel) throws SqlRuntimeException {
    try {
      return this.wrapped.getNCharacterStream(columnLabel);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SqlRuntimeException {
    try {
      this.wrapped.updateNCharacterStream(columnIndex, x, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SqlRuntimeException {
    try {
      this.wrapped.updateNCharacterStream(columnLabel, reader, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SqlRuntimeException {
    try {
      this.wrapped.updateAsciiStream(columnIndex, x, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SqlRuntimeException {
    try {
      this.wrapped.updateBinaryStream(columnIndex, x, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateCharacterStream(int columnIndex, Reader x, long length) throws SqlRuntimeException {
    try {
      this.wrapped.updateCharacterStream(columnIndex, x, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SqlRuntimeException {
    try {
      this.wrapped.updateAsciiStream(columnLabel, x, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SqlRuntimeException {
    try {
      this.wrapped.updateBinaryStream(columnLabel, x, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SqlRuntimeException {
    try {
      this.wrapped.updateCharacterStream(columnLabel, reader, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SqlRuntimeException {
    try {
      this.wrapped.updateBlob(columnIndex, inputStream, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SqlRuntimeException {
    try {
      this.wrapped.updateBlob(columnLabel, inputStream, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateClob(int columnIndex, Reader reader, long length) throws SqlRuntimeException {
    try {
      this.wrapped.updateClob(columnIndex, reader, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateClob(String columnLabel, Reader reader, long length) throws SqlRuntimeException {
    try {
      this.wrapped.updateClob(columnLabel, reader, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateNClob(int columnIndex, Reader reader, long length) throws SqlRuntimeException {
    try {
      this.wrapped.updateNClob(columnIndex, reader, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateNClob(String columnLabel, Reader reader, long length) throws SqlRuntimeException {
    try {
      this.wrapped.updateNClob(columnLabel, reader, length);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateNCharacterStream(int columnIndex, Reader x) throws SqlRuntimeException {
    try {
      this.wrapped.updateNCharacterStream(columnIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateNCharacterStream(String columnLabel, Reader reader) throws SqlRuntimeException {
    try {
      this.wrapped.updateNCharacterStream(columnLabel, reader);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateAsciiStream(int columnIndex, InputStream x) throws SqlRuntimeException {
    try {
      this.wrapped.updateAsciiStream(columnIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateBinaryStream(int columnIndex, InputStream x) throws SqlRuntimeException {
    try {
      this.wrapped.updateBinaryStream(columnIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateCharacterStream(int columnIndex, Reader x) throws SqlRuntimeException {
    try {
      this.wrapped.updateCharacterStream(columnIndex, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateAsciiStream(String columnLabel, InputStream x) throws SqlRuntimeException {
    try {
      this.wrapped.updateAsciiStream(columnLabel, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateBinaryStream(String columnLabel, InputStream x) throws SqlRuntimeException {
    try {
      this.wrapped.updateBinaryStream(columnLabel, x);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateCharacterStream(String columnLabel, Reader reader) throws SqlRuntimeException {
    try {
      this.wrapped.updateCharacterStream(columnLabel, reader);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateBlob(int columnIndex, InputStream inputStream) throws SqlRuntimeException {
    try {
      this.wrapped.updateBlob(columnIndex, inputStream);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateBlob(String columnLabel, InputStream inputStream) throws SqlRuntimeException {
    try {
      this.wrapped.updateBlob(columnLabel, inputStream);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateClob(int columnIndex, Reader reader) throws SqlRuntimeException {
    try {
      this.wrapped.updateClob(columnIndex, reader);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateClob(String columnLabel, Reader reader) throws SqlRuntimeException {
    try {
      this.wrapped.updateClob(columnLabel, reader);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateNClob(int columnIndex, Reader reader) throws SqlRuntimeException {
    try {
      this.wrapped.updateNClob(columnIndex, reader);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateNClob(String columnLabel, Reader reader) throws SqlRuntimeException {
    try {
      this.wrapped.updateNClob(columnLabel, reader);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public <T> T getObject(int columnIndex, Class<T> type) throws SqlRuntimeException {
    try {
      return this.wrapped.getObject(columnIndex, type);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public <T> T getObject(String columnLabel, Class<T> type) throws SqlRuntimeException {
    try {
      return this.wrapped.getObject(columnLabel, type);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateObject(int columnIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SqlRuntimeException {
    try {
      this.wrapped.updateObject(columnIndex, x, targetSqlType, scaleOrLength);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateObject(String columnLabel, Object x, SQLType targetSqlType, int scaleOrLength) throws SqlRuntimeException {
    try {
      this.wrapped.updateObject(columnLabel, x, targetSqlType, scaleOrLength);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateObject(int columnIndex, Object x, SQLType targetSqlType) throws SqlRuntimeException {
    try {
      this.wrapped.updateObject(columnIndex, x, targetSqlType);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void updateObject(String columnLabel, Object x, SQLType targetSqlType) throws SqlRuntimeException {
    try {
      this.wrapped.updateObject(columnLabel, x, targetSqlType);
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SqlRuntimeException {
    try {
      if (iface.isAssignableFrom(this.wrapped.getClass())) {
        return (T) this.wrapped;
      }
      else {
        return this.wrapped.unwrap(iface);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

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
