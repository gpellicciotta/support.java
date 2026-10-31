// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;

/**
 *  A {@link Response} backed by a {@link HttpExchange} from the JDK's built-in {@code com.sun.net.httpserver.HttpServer}.
 *  <p>
 *  {@code HttpExchange} requires headers to be sent (via {@code sendResponseHeaders}) before the response body
 *  can be written, and the exact body length must be decided at that point — this class defers that call to the
 *  first access of the body ({@link #getOutputStream()}), using chunked transfer-encoding unless
 *  {@link #setContentLength} was called first.</p>
 *  <p>
 *  Answers to a {@code HEAD} request, and {@code 1xx}, {@code 204}, and {@code 304} answers, carry no body: their headers go out
 *  with the JDK's no-body length, and whatever a handler writes is dropped, so handlers need no special case.</p>
 */
final class HttpExchangeResponse implements Response {
  HttpExchangeResponse(HttpExchange exchange) {
    assert exchange != null;
    this.exchange = exchange;
  }

  private final HttpExchange exchange;
  private int statusCode = 200;
  private long contentLength = -1;
  private boolean headersSent = false;
  // Decided when the headers go out, so a status changed afterwards cannot hand out the closed JDK stream
  private boolean noBody = false;

  @Override
  public void setStatus(int statusCode) {
    this.statusCode = statusCode;
  }

  @Override
  public void setHeader(String name, String value) {
    this.exchange.getResponseHeaders().set(name, value);
  }

  @Override
  public void setContentLength(int contentLength) {
    this.contentLength = contentLength;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    this.ensureHeadersSent();
    // The JDK closes the body of a no-body answer, so writes go nowhere instead of failing
    return this.noBody ? OutputStream.nullOutputStream() : this.exchange.getResponseBody();
  }

  @Override
  public void addCookie(Cookie cookie) {
    this.exchange.getResponseHeaders().add("Set-Cookie", cookie.toSetCookieHeaderValue());
  }

  @Override
  public void sendError(int statusCode, String message) throws IOException {
    this.statusCode = statusCode;
    byte[] bytes = (message != null ? message : "").getBytes(StandardCharsets.UTF_8);
    this.setContentType("text/plain; charset=UTF-8");
    this.setContentLength(bytes.length);
    try (OutputStream os = this.getOutputStream()) {
      os.write(bytes);
    }
  }

  /**
   *  {@code sendResponseHeaders(status, responseLength)}: a positive length declares exactly that many bytes
   *  will be written; {@code 0} means chunked transfer-encoding (any amount, unknown up front) — used here as
   *  the default whenever {@link #setContentLength} was never called.
   */
  private void ensureHeadersSent() throws IOException {
    if (!this.headersSent) {
      this.headersSent = true;
      // -1 tells the JDK there is no body; any other length on a no-body answer makes it warn and close the stream
      this.noBody = this.hasNoBody();
      long lengthToSend = this.noBody ? -1 : (this.contentLength >= 0) ? this.contentLength : 0;
      this.exchange.sendResponseHeaders(this.statusCode, lengthToSend);
    }
  }

  /** Tells whether this answer carries no body: one to a {@code HEAD} request, or a {@code 1xx}, {@code 204}, or {@code 304}. */
  private boolean hasNoBody() {
    return "HEAD".equalsIgnoreCase(this.exchange.getRequestMethod()) || (this.statusCode < 200) || (this.statusCode == 204) || (this.statusCode == 304);
  }
}
