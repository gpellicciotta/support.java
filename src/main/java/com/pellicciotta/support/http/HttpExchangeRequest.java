// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.net.httpserver.HttpExchange;

/**
 *  A {@link Request} backed by a {@link HttpExchange} from the JDK's built-in {@code com.sun.net.httpserver.HttpServer}.
 */
final class HttpExchangeRequest implements Request
{
  HttpExchangeRequest(HttpExchange exchange) {
    assert exchange != null;
    this.exchange = exchange;
  }

  private final HttpExchange exchange;
  private final Map<String, Object> attributes = new HashMap<>();
  private List<Cookie> cookies;

  @Override
  public String getMethod() {
    return this.exchange.getRequestMethod();
  }

  @Override
  public String getRequestURI() {
    return this.exchange.getRequestURI().getPath();
  }

  @Override
  public String getQueryString() {
    return this.exchange.getRequestURI().getRawQuery();
  }

  @Override
  public String getHeader(String name) {
    return this.exchange.getRequestHeaders().getFirst(name);
  }

  @Override
  public Set<String> getHeaderNames() {
    return this.exchange.getRequestHeaders().keySet();
  }

  @Override
  public InputStream getInputStream() {
    return this.exchange.getRequestBody();
  }

  @Override
  public Object getAttribute(String name) {
    return this.attributes.get(name);
  }

  @Override
  public void setAttribute(String name, Object value) {
    this.attributes.put(name, value);
  }

  @Override
  public List<Cookie> getCookies() {
    if (this.cookies == null) {
      this.cookies = Cookie.parseCookieHeader(this.exchange.getRequestHeaders().getFirst("Cookie"));
    }
    return this.cookies;
  }
}
