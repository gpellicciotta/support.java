// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

import com.pellicciotta.support.config.Setting;
import com.pellicciotta.support.config.SettingsFactory;
import com.pellicciotta.support.config.SettingsGroup;
import com.pellicciotta.support.logging.Log;
import com.pellicciotta.support.logging.LogService;

/**
 *  Web-server configuration {@link SettingsGroup}.
 */
public class WebServerConfig extends SettingsGroup
{
  /**
   *  Create a default web server settings group.
   */
  public WebServerConfig() {
    super("web", "Web-server settings");
    this.log = LogService.fromCurrentClass();
    SettingsFactory create = new SettingsFactory();
    this.port = create.standaloneIntegerSetting("port", "Port at which the web server will be listening for HTTP(s) requests", 80);
    this.host = create.standaloneStringSetting("host", "Host name or IP address at which the web server will be listening for HTTP(s) requests", "127.0.0.1");
    this.baseUrl = create.standaloneStringSetting("base-url", "Base URL for all web API communication", "http://localhost");
    this.addChildren(this.port, this.host, this.baseUrl);
  }

  private final Log log;
  private final Setting<String> host;
  private final Setting<Integer> port;
  private final Setting<String> baseUrl;

  /**
   *  @return The configured HTTP server port.
   */
  public int getPort() {
    return this.port.getValueOrDefault();
  }

  /**
   *  Configure an HTTP server port.
   *
   *  @param port The HTTP server port to listen for connections.
   */
  public void setPort(int port) {
    this.port.setValue(port);
  }

  /**
   *  @return The configured HTTP server host name or IP address.
   */
  public String getHost() {
    return this.host.getValueOrDefault();
  }

  /**
   *  Configure an HTTP server host.
   *
   *  @param host The host name or IP address at which to listen for connections. Must not be <code>null</code>.
   */
  public void setHost(String host) {
    assert host != null;

    this.host.setValue(host);
  }

  /**
   *  @return The base URL for all API communication. Never ends with a <kbd>/</kbd>. If not explicitly set, will be determined based on the host and port values.
   */
  public String getBaseUrl() {
    if (!this.baseUrl.isValueSet()) {
      String calculatedBaseUrl;
      if (this.getPort() == 443) {
        calculatedBaseUrl = "https://" + this.getHost();
      }
      else if (this.getPort() == 80) {
        calculatedBaseUrl = "http://" + this.getHost();
      }
      else {
        calculatedBaseUrl = "http://" + this.getHost() + ":" + this.getPort();
      }
      this.log.warning("Calculated base URL based on host and port: %s", calculatedBaseUrl);
      return calculatedBaseUrl;
    }
    return this.baseUrl.getValue();
  }

  /**
   *  Configure the base URL for all REST API communication.
   *
   *  @param baseUrl The base URL, which should be of the form: <kbd>{{scheme}}://{{host}}:{{port}}/{{rest of base URL}}</kbd>
   */
  public void setBaseUrl(String baseUrl) {
    assert baseUrl != null;

    this.baseUrl.setValue(baseUrl);
  }
}
