// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.sql;

import com.pellicciotta.support.config.Setting;
import com.pellicciotta.support.config.SettingsFactory;
import com.pellicciotta.support.config.SettingsGroup;
import com.pellicciotta.support.utils.Contract;

/**
 *  DB connectivity configuration {@link SettingsGroup}.
 */
public class DbConfig extends SettingsGroup {
  /**
   *  Create a default DB settings group.
   *
   *  @param defaultDbName The database name to use in the default JDBC URL (e.g. "auth", "counters").
   *      Must not be <code>null</code>.
   */
  public DbConfig(String defaultDbName) {
    super("db", "Database connectivity settings");
    Contract.isNotNull("defaultDbName", defaultDbName);

    SettingsFactory create = new SettingsFactory();
    this.jdbcUrl = create.standaloneStringSetting("jdbc-url", "The JDBC URL to use to make a database connection", "jdbc:mariadb://localhost:3306/" + defaultDbName);
    this.user = create.standaloneStringSetting("user", "The user name for authentication with the database server", "pellicciotta");
    this.password = create.standalonePasswordSetting("password", "The password for authentication with the database server");
    this.addChildren(this.jdbcUrl, this.user, this.password);
  }

  private final Setting<String> user;
  private final Setting<String> password;
  private final Setting<String> jdbcUrl;

  /**
   *  @return The configured JDBC URL.
   */
  public String getJdbcUrl() {
    return this.jdbcUrl.getValueOrDefault();
  }

  /**
   *  Set the JDBC URL to be used.
   *
   *  @param url The valid JDBC URL to be used. Must not be <code>null</code>.
   */
  public void setJdbcUrl(String url) {
    assert url != null;

    this.jdbcUrl.setValue(url);
  }

  /**
   *  @return The DB user account to setup connections with.
   */
  public String getUser() {
    return this.user.getValueOrDefault();
  }

  /**
   *  Set the user account to setup connections with.
   *
   *  @param user The user account to be used. May be <code>null</code>.
   */
  public void setUser(String user) {
    this.user.setValue(user);
  }

  /**
   *  @return The DB user account password to setup connections with.
   */
  public String getPassword() {
    return this.password.getValueOrDefault();
  }

  /**
   *  Set the user account password to setup connections with.
   *
   *  @param pwd The user account passsword to be used. May be <code>null</code>.
   */
  public void setPassword(String pwd) {
    this.password.setValue(pwd);
  }
}
