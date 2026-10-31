// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** A minimal, in-memory {@link Response} test double, used instead of standing up a real {@code HttpExchange}. */
class FakeResponse implements Response {
  private int statusCode = 200;
  private int contentLength = -1;
  private final Map<String, String> headers = new HashMap<>();
  private final List<Cookie> cookies = new ArrayList<>();
  private final ByteArrayOutputStream body = new ByteArrayOutputStream();

  int getStatusCode() {
    return this.statusCode;
  }

  int getContentLength() {
    return this.contentLength;
  }

  String getHeader(String name) {
    return this.headers.get(name);
  }

  List<Cookie> getCookies() {
    return this.cookies;
  }

  String getBodyAsString() {
    return this.body.toString(StandardCharsets.UTF_8);
  }

  @Override
  public void setStatus(int statusCode) {
    this.statusCode = statusCode;
  }

  @Override
  public void setHeader(String name, String value) {
    this.headers.put(name, value);
  }

  @Override
  public void setContentLength(int contentLength) {
    this.contentLength = contentLength;
  }

  @Override
  public OutputStream getOutputStream() {
    return this.body;
  }

  @Override
  public void addCookie(Cookie cookie) {
    this.cookies.add(cookie);
  }

  @Override
  public void sendError(int statusCode, String message) {
    this.statusCode = statusCode;
    this.body.reset();
    byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
    this.body.write(bytes, 0, bytes.length);
  }
}
