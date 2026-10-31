// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.config.v0;

import com.pellicciotta.support.config.*;

public class WebConfig extends SettingsGroup {
  public WebConfig() {
    super("web", "Web server configuration");
    SettingsFactory create = new SettingsFactory();
    this.httpPort = create.standaloneIntegerSetting("http-port", "Port at which the webserver will be listening for HTTP requests", 80);
    this.httpsPort = create.standaloneIntegerSetting("https-port", "Port at which the webserver will be listening for HTTPS requests", 443);
    this.host = create.standaloneStringSetting("host", "Host name of the webserver", "localhost");
    this.addChildren(this.httpPort, this.httpsPort, this.host);
  }

  Setting<Integer> httpPort;
  Setting<Integer> httpsPort;
  Setting<String> host;

  public int getHttpPort() {
    return this.httpPort.getValueOrDefault();
  }

  public void setHttpPort(int port) {
    this.httpPort.setValue(port);
  }

  public int getHttpsPort() {
    return this.httpsPort.getValueOrDefault();
  }

  public void setHttpsPort(int port) {
    this.httpsPort.setValue(port);
  }

  public String getHost() {
    return this.host.getValueOrDefault();
  }

  public void setHost(String host) {
    this.host.setValue(host);
  }
}
