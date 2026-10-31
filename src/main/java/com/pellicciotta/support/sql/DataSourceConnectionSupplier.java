// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.sql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.function.Supplier;

import com.pellicciotta.support.exceptions.SqlRuntimeException;
import com.pellicciotta.support.utils.Contract;

/**
 *  Provides connections based on an underlying {@link javax.sql.DataSource}.
 */
public class DataSourceConnectionSupplier implements Supplier<Connection>
{
  /**
   *  Create a supplier that will create new connections every time {@link #get()} is requested.
   *  Connections will be created via the {@link DataSource#getConnection()} method.
   *
   *  @param dataSource The underlying data source object that will get used. Must not be <c>null</c>.
   */
  public DataSourceConnectionSupplier(DataSource dataSource) {
    Contract.isNotNull("dataSource", dataSource);
    this.dataSource = dataSource;
  }

  private final DataSource dataSource;

  /**
   *  Get a new (or re-used) connection.
   *
   *  @return The connection, ready for use.
   *
   *  @throws SqlRuntimeException If obtaining a connection fails.
   */
  @Override
  public Connection get() {
    try {
      return this.dataSource.getConnection();
    }
    catch (Exception e) {
      throw new SqlRuntimeException(e, "Failed to obtain a new connection");
    }
  }

  /**
   *  Get a new connection, while using the provided credentials.
   *
   *  @param userName The user name to create the connection for.
   *  @param password The accompanying password.
   *  @return The connection, ready for use.
   *
   *  @throws SqlRuntimeException If obtaining a connection fails.
   */
  public Connection get(String userName, String password) {
    try {
      return this.dataSource.getConnection(userName, password);
    }
    catch (Exception e) {
      throw new SqlRuntimeException(e, "Failed to obtain a new connection");
    }
  }
}
