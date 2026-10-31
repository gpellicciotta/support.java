// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WebServerConfigTest
{
  @Test
  public void testDefaults() {
    WebServerConfig config = new WebServerConfig();

    Assertions.assertEquals(80, config.getPort());
    Assertions.assertEquals("127.0.0.1", config.getHost());
    Assertions.assertEquals("http://127.0.0.1", config.getBaseUrl());
  }

  @Test
  public void testCalculatedBaseUrlForHttpsPort() {
    WebServerConfig config = new WebServerConfig();
    config.setHost("example.com");
    config.setPort(443);

    Assertions.assertEquals("https://example.com", config.getBaseUrl());
  }

  @Test
  public void testCalculatedBaseUrlForNonStandardPort() {
    WebServerConfig config = new WebServerConfig();
    config.setHost("example.com");
    config.setPort(8080);

    Assertions.assertEquals("http://example.com:8080", config.getBaseUrl());
  }

  @Test
  public void testExplicitBaseUrlOverridesCalculation() {
    WebServerConfig config = new WebServerConfig();
    config.setHost("example.com");
    config.setPort(8080);
    config.setBaseUrl("https://api.example.com/v1");

    Assertions.assertEquals("https://api.example.com/v1", config.getBaseUrl());
  }
}
