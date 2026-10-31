// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.mail;

import com.pellicciotta.support.config.*;
import com.pellicciotta.support.logging.spi.LogBackEnd;
import com.pellicciotta.support.logging.stdout.StdoutBasedLogBackEnd;

/**
 *  Mail configuration {@link SettingsGroup}.
 */
public class MailConfig extends SettingsGroup {
  /**
   * Create the settings group with its defaults: dummy back-end, host {@code localhost}, port 465, SSL enabled,
   * and no user or password.
   */
  public MailConfig() {
    super("mail", "Mail back-end settings");
    SettingsFactory create = new SettingsFactory();
    this.backEndType = create.standaloneClassSetting(MailBackEnd.class, "back-end-type", "The type of mail back-end to be used", DummyBackEnd.class);
    this.host = create.standaloneStringSetting("host", "Host name or IP address at which the mail server will be listening", "localhost");
    this.port = create.standaloneIntegerSetting("port", "Port at which the mail server will be listening for SMTP(S) requests", 465);
    this.ssl = create.standaloneBooleanSetting("ssl", "Whether to use SSL for mail server communication", true);
    this.user = create.standaloneStringSetting("user", "The user name for authentication with the mail server");
    this.password = create.standalonePasswordSetting("password", "The password for authentication with the mail server");
    this.addChildren(this.backEndType, this.host, this.port, this.user, this.password);
  }

  private Setting<Class> backEndType;
  private Setting<Integer> port;
  private Setting<String> host;
  private Setting<Boolean> ssl;
  private Setting<String> user;
  private Setting<String> password;

  /**
   * @return The configured back-end class, or {@link DummyBackEnd} if none was set.
   */
  public Class<? extends MailBackEnd> getBackEndType() {
    final Class valueOrDefault = this.backEndType.getValueOrDefault();
    return (Class<? extends MailBackEnd>)valueOrDefault;
  }

  /**
   * @param type The back-end class to use.
   */
  public void setBackEndType(Class<? extends MailBackEnd> type) {
    this.backEndType.setValue(type);
  }

  /**
   * @return Host name or IP address of the mail server; {@code localhost} by default.
   */
  public String getHost() {
    return this.host.getValueOrDefault();
  }

  /**
   * @param host Host name or IP address of the mail server.
   */
  public void setHost(String host) {
    this.host.setValue(host);
  }

  /**
   * @return Port of the mail server; 465 by default.
   */
  public int getPort() {
    return this.port.getValueOrDefault();
  }

  /**
   * @param port Port of the mail server.
   */
  public void setPort(int port) {
    this.port.setValue(port);
  }

  /**
   * @return Whether SSL is used for mail server communication; true by default.
   */
  public boolean isSSL() {
    return this.ssl.getValueOrDefault();
  }

  /**
   * @param useSSL Whether to use SSL for mail server communication.
   */
  public void setSSL(boolean useSSL) {
    this.ssl.setValue(useSSL);
  }

  /**
   * @return The user name for authentication with the mail server; null if not set.
   */
  public String getUser() {
    return this.user.getValueOrDefault();
  }

  /**
   * @param user The user name for authentication with the mail server.
   */
  public void setUser(String user) {
    this.user.setValue(user);
  }

  /**
   * @return The password for authentication with the mail server; null if not set.
   */
  public String getPassword() {
    return this.password.getValueOrDefault();
  }

  /**
   * @param pwd The password for authentication with the mail server.
   */
  public void setPassword(String pwd) {
    this.password.setValue(pwd);
  }
}
