// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.sql;

import javax.sql.DataSource;
import java.sql.*;
import java.util.function.Supplier;

import com.pellicciotta.support.exceptions.SqlRuntimeException;
import com.pellicciotta.support.utils.Contract;

/**
 *  Provides connections based on an underlying {@link DriverManager} and JDBC URL template.
 */
public class DriverManagerConnectionSupplier implements Supplier<Connection> {
  /**
   *  Create a supplier that will create new connections every time {@link #get()} is requested.
   *  Connections will be created via the {@link DriverManager#getConnection(String)} method.
   *
   *  @param jdbcUrl The JDBC URL based on which a connection will be created.
   */
  public DriverManagerConnectionSupplier(String jdbcUrl) {
    Contract.isNotNull("jdbcUrl", jdbcUrl);
    this.jdbcUrl = jdbcUrl;
  }

  private final String jdbcUrl;

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
      return DriverManager.getConnection(this.jdbcUrl);
    }
    catch (Exception e) {
      throw new SqlRuntimeException(e, "Failed to create connection to '%s': %s", this.jdbcUrl, e);
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
      return DriverManager.getConnection(this.jdbcUrl, userName, password);
    }
    catch (Exception e) {
      throw new SqlRuntimeException(e, "Failed to create connection to '%s': %s", this.jdbcUrl, e);
    }
  }
}
