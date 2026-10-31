// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.mail;

import com.pellicciotta.support.config.*;
import com.pellicciotta.support.logging.spi.LogBackEnd;
import com.pellicciotta.support.logging.stdout.StdoutBasedLogBackEnd;

/**
 *  Mail configuration {@link SettingsGroup}.
 */
public class MailConfig extends SettingsGroup {
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

  public Class<? extends MailBackEnd> getBackEndType() {
    final Class valueOrDefault = this.backEndType.getValueOrDefault();
    return (Class<? extends MailBackEnd>)valueOrDefault;
  }

  public void setBackEndType(Class<? extends MailBackEnd> type) {
    this.backEndType.setValue(type);
  }

  public String getHost() {
    return this.host.getValueOrDefault();
  }

  public void setHost(String host) {
    this.host.setValue(host);
  }

  public int getPort() {
    return this.port.getValueOrDefault();
  }

  public void setPort(int port) {
    this.port.setValue(port);
  }

  public boolean isSSL() {
    return this.ssl.getValueOrDefault();
  }

  public void setSSL(boolean useSSL) {
    this.ssl.setValue(useSSL);
  }

  public String getUser() {
    return this.user.getValueOrDefault();
  }

  public void setUser(String user) {
    this.user.setValue(user);
  }

  public String getPassword() {
    return this.password.getValueOrDefault();
  }

  public void setPassword(String pwd) {
    this.password.setValue(pwd);
  }
}
