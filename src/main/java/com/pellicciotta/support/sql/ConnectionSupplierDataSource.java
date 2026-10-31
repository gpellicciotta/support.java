// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.sql;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.*;
import java.util.function.Supplier;
import java.util.logging.Logger;

import com.pellicciotta.support.utils.Contract;

/**
 *  A {@link DataSource} implementation based on an {@link Supplier <Connection>}.
 */
public class ConnectionSupplierDataSource implements DataSource {
  /**
   *  Create a new {@link DataSource}.
   *
   *  @param supplier The underlying supplier of {@link Connection}s.
   */
  public ConnectionSupplierDataSource(Supplier<Connection> supplier) {
    Contract.isNotNull("supplier", supplier);
    this.supplier = supplier;
  }

  private final Supplier<Connection> supplier;

  /**
   *  Attempts to establish a connection with the data source that this {@code DataSource} object represents.
   *
   *  @return A connection to the data source.
   *
   *  @throws SQLException        If a database access error occurs.
   *  @throws SQLTimeoutException When the driver has determined that the
   *                              timeout value specified by the {@code setLoginTimeout} method
   *                              has been exceeded and has at least tried to cancel the
   *                              current database connection attempt.
   */
  @Override
  public Connection getConnection() {
    return this.supplier.get();
  }

  /**
   *  Attempts to establish a connection with the data source that this {@code DataSource} object represents.
   *
   *  @param username The database user on whose behalf the connection is being made.
   *  @param password The user's password.
   *
   *  @return A connection to the data source.
   *
   *  @throws SQLException        If a database access error occurs.
   *  @throws SQLTimeoutException When the driver has determined that the
   *                              timeout value specified by the {@code setLoginTimeout} method
   *                              has been exceeded and has at least tried to cancel the
   *                              current database connection attempt.
   */
  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    if (this.supplier instanceof DriverManagerConnectionSupplier) {
      return ((DriverManagerConnectionSupplier)supplier).get(username, password);
    }
    else if (this.supplier instanceof DataSourceConnectionSupplier) {
      return ((DataSourceConnectionSupplier)supplier).get(username, password);
    }
    throw new SQLFeatureNotSupportedException("getConnection(userName, password)");
  }

  /** @throws SQLFeatureNotSupportedException Always. */
  @Override
  public PrintWriter getLogWriter() throws SQLFeatureNotSupportedException {
    throw new SQLFeatureNotSupportedException("getLogWriter()");
  }

  /** @throws SQLFeatureNotSupportedException Always. */
  @Override
  public void setLogWriter(PrintWriter out) throws SQLFeatureNotSupportedException {
    throw new SQLFeatureNotSupportedException("setLogWriter(PrintWriter)");
  }

  /** @throws SQLFeatureNotSupportedException Always. */
  @Override
  public void setLoginTimeout(int seconds) throws SQLFeatureNotSupportedException {
    throw new SQLFeatureNotSupportedException("setLogWriter(PrintWriter)");
  }

  /** @return Always zero. */
  @Override
  public int getLoginTimeout() throws SQLException {
    return 0;
  }

  /** @throws SQLFeatureNotSupportedException Always. */
  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    throw new SQLFeatureNotSupportedException("getParentLogger()");
  }

  /** @throws SQLFeatureNotSupportedException Always. */
  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new SQLFeatureNotSupportedException("unwrap(Class<T>)");
  }

  /** @return Always return false. */
  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return false;
  }
}
