// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** A minimal, in-memory {@link Request} test double, used instead of standing up a real {@code HttpExchange}. */
class FakeRequest implements Request {
  FakeRequest(String method, String requestURI) {
    this.method = method;
    this.requestURI = requestURI;
  }

  private final String method;
  private final String requestURI;
  private final Map<String, String> headers = new HashMap<>();
  private final Map<String, Object> attributes = new HashMap<>();
  private final List<Cookie> cookies = new ArrayList<>();
  private byte[] body = new byte[0];

  FakeRequest withHeader(String name, String value) {
    this.headers.put(name, value);
    return this;
  }

  FakeRequest withBody(byte[] body) {
    this.body = body;
    return this;
  }

  @Override
  public String getMethod() {
    return this.method;
  }

  @Override
  public String getRequestURI() {
    return this.requestURI;
  }

  @Override
  public String getQueryString() {
    return null;
  }

  @Override
  public String getHeader(String name) {
    return this.headers.get(name);
  }

  @Override
  public Set<String> getHeaderNames() {
    return Collections.unmodifiableSet(this.headers.keySet());
  }

  @Override
  public InputStream getInputStream() {
    return new ByteArrayInputStream(this.body);
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
    return this.cookies;
  }
}
