package com.pellicciotta.support.sql;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.sql.*;
import java.util.*;

import com.pellicciotta.support.exceptions.SqlRuntimeException;
import com.pellicciotta.support.utils.Contract;

/**
 *  Class implementing and wrapping an underlying {@link PreparedStatement} and
 *  allowing the setting of named parameters, in addition to positional parameters.
 *  <p>
 *  All methods can throw the unchecked {@link SqlRuntimeException}, most likely encapsulating
 *  an underlying {@link SQLException} cause.</p>
 *  <p>
 *  All {@link ResultSet} returning methods also return {@link SqlRow} instead, which also is a
 *  simple wrapper that ensures throwning unchecked {@link SqlRuntimeException} instead of the
 *  checked {@link SQLException}.</p>
 */
public class ParameterizedSqlStatement extends SqlStatementBase {
  // <editor-fold desc="Construction">

  /**
   *  Create a SQL statement that represents the given SQL text, which was already turned into a {@link PreparedStatement}
   *  and with the given named parameters mapping to the given (1-based) positions.
   *
   *  @param wrapped The already created {@link PreparedStatement} that represents <c>sqlText</c>.
   *  @param sqlText The original SQL text used to create the {@link PreparedStatement}. This SQL text may still contain named parameter references (i.e. <c>:param-name</c>) instead
   *                 of positional ones (i.e. <c>?</c>) that are actually used by JDBC.
   *  @param namedParameterPositions A mapping of each parameter name to the (1-based) positions in the {@link PreparedStatement}.
   *                                 This mapping will be used by all <c>setXxx(String paramName, Xxx val)</c> methods to actually
   *                                 invoke <c>setXxx(int paramIdx, Xxx val)</c> one or multiple time.
   */
  protected ParameterizedSqlStatement(PreparedStatement wrapped, String sqlText, Map<String, int[]> namedParameterPositions) {
    super(wrapped);
    Contract.isNotNull("sqlText", sqlText);
    Contract.isNotNull("namedParameterPositions", namedParameterPositions);
    this.sqlText = sqlText;
    this.namedParameterPositions = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    this.namedParameterPositions.putAll(namedParameterPositions);
  }

  /** The original SQL text this statement was created from. */
  protected final String sqlText;
  /** Positions of each named parameter, keyed case-insensitively by parameter name. */
  protected final Map<String, int[]> namedParameterPositions;

  /**
   *  Get the original SQL used to create this statement.
   *
   *  @return The SQL text for this statement.
   */
  public String getSql() {
    return this.sqlText;
  }

  /**
   *  Get the named parameters used in this statement.
   *
   *  @return The set of parameter names used in this statement. May be empty but never <c>null</c>.
   */
  public Set<String> getParameterNames() {
    return this.namedParameterPositions.keySet();
  }

  /**
   *  Get the positions a parameter appears in.
   *
   *  @param parameterName The name of the parameter. Parameter names are matched without considering case.
   *  @return The 1-based positions this parameter appears in.
   *
   *  @throws IllegalArgumentException If the parameter has no associated positions.
   */
  public int[] getParameterPositions(String parameterName) {
    int[] pos = this.namedParameterPositions.get(parameterName);
    if ((pos == null) || (pos.length == 0)) {
      throw new IllegalArgumentException("No positions are know for parameter '" + parameterName + "'");
    }
    return pos;
  }

  // </editor-fold>
  // <editor-fold desc="PreparedStatement Interface">

  /**
   *  Delegate to {@link #setNull(int, int)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param sqlType SQL type of the column being set to <c>NULL</c>.
   */
  public void setNull(String parameterName, int sqlType) throws SqlRuntimeException {
    try {
      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setNull(parameterIndex, sqlType);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setNull(int, int, String)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param sqlType SQL type of the column being set to <c>NULL</c>.
   *  @param typeName The fully-qualified name of an SQL user-defined type; ignored if the parameter is not a user-defined type or REF.
   */
  public void setNull(String parameterName, int sqlType, String typeName) throws SqlRuntimeException {
    try {
      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setNull(parameterIndex, sqlType, typeName);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setBoolean(int, boolean)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The boolean value to set.
   */
  public void setBoolean(String parameterName, boolean x) throws SqlRuntimeException {
    try {
      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setBoolean(parameterIndex, x);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setByte(int, byte)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The byte value to set.
   */
  public void setByte(String parameterName, byte x) throws SqlRuntimeException {
    try {
      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setByte(parameterIndex, x);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setShort(int, short)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The short value to set.
   */
  public void setShort(String parameterName, short x) throws SqlRuntimeException {
    try {
      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setShort(parameterIndex, x);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setInt(int, int)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The int value to set.
   */
  public void setInt(String parameterName, int x) throws SqlRuntimeException {
    try {
      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setInt(parameterIndex, x);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setLong(int, long)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The long value to set.
   */
  public void setLong(String parameterName, long x) throws SqlRuntimeException {
    try {
      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setLong(parameterIndex, x);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setFloat(int, float)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The float value to set.
   */
  public void setFloat(String parameterName, float x) throws SqlRuntimeException {
    try {

      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setFloat(parameterIndex, x);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setDouble(int, double)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The double value to set.
   */
  public void setDouble(String parameterName, double x) throws SqlRuntimeException {
    try {

      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setDouble(parameterIndex, x);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setBigDecimal(int, BigDecimal)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The BigDecimal value to set.
   */
  public void setBigDecimal(String parameterName, BigDecimal x) throws SqlRuntimeException {
    try {

      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setBigDecimal(parameterIndex, x);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setString(int, String)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The String value to set.
   */
  public void setString(String parameterName, String x) throws SqlRuntimeException {
    try {
      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setString(parameterIndex, x);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setNString(int, String)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The String value to set.
   */
  public void setNString(String parameterName, String x) throws SqlRuntimeException {
    try {

      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setNString(parameterIndex, x);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setBytes(int, byte[])} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The byte[] value to set.
   */
  public void setBytes(String parameterName, byte[] x) throws SqlRuntimeException {
    try {

      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setBytes(parameterIndex, x);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setDate(int, Date)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The Date value to set.
   */
  public void setDate(String parameterName, Date x) throws SqlRuntimeException {
    try {

      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setDate(parameterIndex, x);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setDate(int, Date)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The Date value to set.
   *  @param cal The Calendar object the driver will use to construct the date.
   */
  public void setDate(String parameterName, Date x, Calendar cal) throws SqlRuntimeException {
    try {

      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setDate(parameterIndex, x, cal);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setTime(int, Time)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The Time value to set.
   */
  public void setTime(String parameterName, Time x) throws SqlRuntimeException {
    try {

      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setTime(parameterIndex, x);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setTime(int, Time)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The Time value to set.
   *  @param cal The Calendar object the driver will use to construct the time.
   */
  public void setTime(String parameterName, Time x, Calendar cal) throws SqlRuntimeException {
    try {

      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setTime(parameterIndex, x, cal);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setTimestamp(int, Timestamp)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The Timestamp value to set.
   */
  public void setTimestamp(String parameterName, Timestamp x) throws SqlRuntimeException {
    try {

      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setTimestamp(parameterIndex, x);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setTimestamp(int, Timestamp, Calendar)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The Date value to set.
   *  @param cal The Calendar object the driver will use to construct the date.
   */
  public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SqlRuntimeException {
    try {

      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setTimestamp(parameterIndex, x, cal);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setAsciiStream(int, InputStream)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The input stream that contains the ASCII parameter value.
   */
  public void setAsciiStream(String parameterName, InputStream x) throws SqlRuntimeException {
    try {

      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setAsciiStream(parameterIndex, x);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setAsciiStream(int, InputStream, int)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The input stream that contains the ASCII parameter value.
   *  @param length The number of bytes in the stream.
   */
  public void setAsciiStream(String parameterName, InputStream x, int length) throws SqlRuntimeException {
    try {

      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setAsciiStream(parameterIndex, x, length);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setAsciiStream(int, InputStream, long)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The input stream that contains the ASCII parameter value.
   *  @param length The number of bytes in the stream.
   */
  public void setAsciiStream(String parameterName, InputStream x, long length) throws SqlRuntimeException {
    try {

      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setAsciiStream(parameterIndex, x, length);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setBinaryStream(int, InputStream)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The input stream that contains the binary parameter value.
   */
  public void setBinaryStream(String parameterName, InputStream x) throws SqlRuntimeException {
    try {

      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setBinaryStream(parameterIndex, x);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setBinaryStream(int, InputStream, int)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The input stream that contains the binary parameter value.
   *  @param length The number of bytes in the stream.
   */
  public void setBinaryStream(String parameterName, InputStream x, int length) throws SqlRuntimeException {
    try {

      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setBinaryStream(parameterIndex, x, length);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setBinaryStream(int, InputStream, long)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The input stream that contains the binary parameter value.
   *  @param length The number of bytes in the stream.
   */
  public void setBinaryStream(String parameterName, InputStream x, long length) throws SqlRuntimeException {
    try {

      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setBinaryStream(parameterIndex, x, length);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setCharacterStream(int, Reader)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param reader The {@link Reader} object that contains the Unicode data.
   */
  public void setCharacterStream(String parameterName, Reader reader) throws SqlRuntimeException {
    try {

      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setCharacterStream(parameterIndex, reader);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setCharacterStream(int, Reader, int)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param reader The {@link Reader} object that contains the Unicode data.
   *  @param length The number of characters in the stream.
   */
  public void setCharacterStream(String parameterName, Reader reader, int length) throws SqlRuntimeException {
    try {

      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setCharacterStream(parameterIndex, reader, length);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setCharacterStream(int, Reader, long)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param reader The {@link Reader} object that contains the Unicode data.
   *  @param length The number of characters in the stream.
   */
  public void setCharacterStream(String parameterName, Reader reader, long length) throws SqlRuntimeException {
    try {

      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setCharacterStream(parameterIndex, reader, length);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setNCharacterStream(int, Reader)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param reader The {@link Reader} object that contains the Unicode data.
   */
  public void setNCharacterStream(String parameterName, Reader reader) throws SqlRuntimeException {
    try {

      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setNCharacterStream(parameterIndex, reader);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setNCharacterStream(int, Reader, long)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param reader The {@link Reader} object that contains the Unicode data.
   *  @param length The number of characters in the stream.
   */
  public void setNCharacterStream(String parameterName, Reader reader, long length) throws SqlRuntimeException {
    try {

      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setNCharacterStream(parameterIndex, reader, length);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setUnicodeStream(int, InputStream, int)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The input stream that contains the binary parameter value.
   *  @param length The number of bytes in the stream.
   *
   *  @deprecated Will always throw SqlRuntimeException since no longer implemented.
   */
  @Deprecated()
  public void setUnicodeStream(String parameterName, InputStream x, int length) throws SqlRuntimeException {
    throw new SqlRuntimeException("Not implemented");
  }

  /**
   *  Delegate to {@link #setRef(int, Ref)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The Ref parameter value.
   */
  public void setRef(String parameterName, Ref x) throws SqlRuntimeException {
    try {
      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setRef(parameterIndex, x);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setArray(int, Array)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The Array parameter value.
   */
  public void setArray(String parameterName, Array x) throws SqlRuntimeException {
    try {
      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setArray(parameterIndex, x);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setURL(int, URL)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The URL parameter value.
   */
  public void setURL(String parameterName, URL x) throws SqlRuntimeException {
    try {
      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setURL(parameterIndex, x);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setRowId(int, RowId)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The RowId parameter value.
   */
  public void setRowId(String parameterName, RowId x) throws SqlRuntimeException {
    try {
      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setRowId(parameterIndex, x);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setClob(int, Clob)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The {@link Clob} parameter value.
   */
  public void setClob(String parameterName, Clob x) throws SqlRuntimeException {
    try {
      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setClob(parameterIndex, x);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setClob(int, Reader)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param reader The {@link Reader} object that contains the CLOB data.
   */
  public void setClob(String parameterName, Reader reader) throws SqlRuntimeException {
    try {
      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setClob(parameterIndex, reader);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setClob(int, Reader, long)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param reader The {@link Reader} object that contains the CLOB data.
   *  @param length The number of characters in the stream.
   */
  public void setClob(String parameterName, Reader reader, long length) throws SqlRuntimeException {
    try {
      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setClob(parameterIndex, reader, length);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setNClob(int, NClob)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The {@link NClob} parameter value.
   */
  public void setNClob(String parameterName, NClob x) throws SqlRuntimeException {
    try {
      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setNClob(parameterIndex, x);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setNClob(int, Reader)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param reader The {@link Reader} object that contains the NCLOB data.
   */
  public void setNClob(String parameterName, Reader reader) throws SqlRuntimeException {
    try {
      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setNClob(parameterIndex, reader);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setNClob(int, Reader, long)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param reader The {@link Reader} object that contains the NCLOB data.
   *  @param length The number of characters in the stream.
   */
  public void setNClob(String parameterName, Reader reader, long length) throws SqlRuntimeException {
    try {
      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setNClob(parameterIndex, reader, length);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setBlob(int, Blob)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The {@link Blob} parameter value.
   */
  public void setBlob(String parameterName, Blob x) throws SqlRuntimeException {
    try {
      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setBlob(parameterIndex, x);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setBlob(int, InputStream)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param inputStream The {@link InputStream} object that contains the BLOB data.
   */
  public void setBlob(String parameterName, InputStream inputStream) throws SqlRuntimeException {
    try {
      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setBlob(parameterIndex, inputStream);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setBlob(int, InputStream, long)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param inputStream The {@link InputStream} object that contains the BLOB data.
   *  @param length The number of bytes in the stream.
   */
  public void setBlob(String parameterName, InputStream inputStream, long length) throws SqlRuntimeException {
    try {
      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setBlob(parameterIndex, inputStream, length);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setSQLXML(int, SQLXML)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The {@link SQLXML} parameter value.
   */
  public void setSQLXML(String parameterName, SQLXML x) throws SqlRuntimeException {
    try {
      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setSQLXML(parameterIndex, x);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setObject(int, Object)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The parameter value.
   */
  public void setObject(String parameterName, Object x) throws SqlRuntimeException {
    try {
      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setObject(parameterIndex, x);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setObject(int, Object, int)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The parameter value.
   *  @param targetSqlType The SQL type (as defined in java.sql.Types) to be sent to the database.
   */
  public void setObject(String parameterName, Object x, int targetSqlType) throws SqlRuntimeException {
    try {
      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setObject(parameterIndex, x, targetSqlType);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setObject(int, Object, int, int)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The parameter value.
   *  @param targetSqlType The SQL type (as defined in java.sql.Types) to be sent to the database.
   *  @param scaleOrLength For {@link Types#DECIMAL} or {@link Types#NUMERIC} types, this is the number of digits after the decimal point.
   *                       For types {@link InputStream} and {@link Reader}, this is the length of the data in the stream or reader.
   *                       For all other types, this value will be ignored.
   */
  public void setObject(String parameterName, Object x, int targetSqlType, int scaleOrLength) throws SqlRuntimeException {
    try {
      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setObject(int, Object, SQLType)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The parameter value.
   *  @param targetSqlType The SQL type to be sent to the database.
   */
  public void setObject(String parameterName, Object x, SQLType targetSqlType) throws SqlRuntimeException {
    try {
      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setObject(parameterIndex, x, targetSqlType);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  /**
   *  Delegate to {@link #setObject(int, Object, SQLType, int)} for all positions of the name parameter.
   *
   *  @param parameterName The name of the parameter. Must map to at least one position.
   *  @param x The parameter value.
   *  @param targetSqlType The SQL type to be sent to the database.
   *  @param scaleOrLength For {@link Types#DECIMAL} or {@link Types#NUMERIC} types, this is the number of digits after the decimal point.
   *                       For types {@link InputStream} and {@link Reader}, this is the length of the data in the stream or reader.
   *                       For all other types, this value will be ignored.
   */
  public void setObject(String parameterName, Object x, SQLType targetSqlType, int scaleOrLength) throws SqlRuntimeException {
    try {
      int[] positions = this.getParameterPositions(parameterName);
      for (int parameterIndex : positions) {
        this.wrapped.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  // </editor-fold>
}
