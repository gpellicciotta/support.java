// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.config.v1;

import com.pellicciotta.support.annotations.ConfigSetting;
import com.pellicciotta.support.config.*;

public class WebConfig
{
  @ConfigSetting(name="http-port", description="Port at which the webserver will be listening for HTTP requests")
  private int httpPort = 80;
  @ConfigSetting(name="https-port", description="Port at which the webserver will be listening for HTTPS requests")
  private int httpsPort = 443;
  @ConfigSetting(name="host", description="Host name of the webserver")
  private String host = "localhost";

  public int getHttpPort() {
    return this.httpPort;
  }

  public void setHttpPort(int port) {
    this.httpPort = port;
  }

  public int getHttpsPort() {
    return this.httpsPort;
  }

  public void setHttpsPort(int port) {
    this.httpsPort = port;
  }

  public String getHost() {
    return this.host;
  }

  public void setHost(String host) {
    this.host = host;
  }
}
