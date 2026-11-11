// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.sun.net.httpserver.HttpExchange;

/**
 *  A {@link Response} backed by a {@link HttpExchange} from the JDK's built-in {@code com.sun.net.httpserver.HttpServer}.
 *  <p>
 *  {@code HttpExchange} requires headers to be sent (via {@code sendResponseHeaders}) before the response body
 *  can be written, and the exact body length must be decided at that point — this class defers that call to the
 *  first byte written to {@link #getOutputStream()}, using chunked transfer-encoding unless
 *  {@link #setContentLength} was called first. A handled request that writes nothing — even if it declared a
 *  content length or opened the stream — gets a body-less header response from {@link OutputStream#close()} or
 *  {@link #finish()} instead. Once headers are sent, {@link #sendError} throws {@link IOException} instead of
 *  appending error text to the body.</p>
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
  private boolean noContentSent = false;
  private OutputStream bodyStream;

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
    if (!this.headersSent && this.hasNoBody()) {
      this.sendHeaders(-1);
    }
    // The JDK closes the body of a no-body answer, so writes go nowhere instead of failing
    if (this.noBody) {
      return OutputStream.nullOutputStream();
    }
    if (this.bodyStream == null) {
      this.bodyStream = new BodyOutputStream();
    }
    return this.bodyStream;
  }

  @Override
  public void addCookie(Cookie cookie) {
    this.exchange.getResponseHeaders().add("Set-Cookie", cookie.toSetCookieHeaderValue());
  }

  @Override
  public void sendError(int statusCode, String message) throws IOException {
    if (this.headersSent) {
      throw new IOException("Response headers already sent");
    }
    this.statusCode = statusCode;
    byte[] bytes = (message != null ? message : "").getBytes(StandardCharsets.UTF_8);
    this.setContentType("text/plain; charset=UTF-8");
    this.setContentLength(bytes.length);
    try (OutputStream os = this.getOutputStream()) {
      os.write(bytes);
    }
  }

  /** @return {@code true} once the status code and response headers have been sent to the underlying exchange. */
  boolean headersSent() {
    return this.headersSent;
  }

  /** Sends the headers if no body was written, so an answer that wrote nothing still reaches the client. */
  void finish() throws IOException {
    if (!this.headersSent) {
      this.sendHeaders(-1);
    }
  }

  /**
   *  {@code sendResponseHeaders(status, responseLength)}: a positive length declares exactly that many bytes
   *  will be written; {@code 0} means chunked transfer-encoding (any amount, unknown up front) — used here
   *  whenever {@link #setContentLength} was not set to a positive value.
   */
  private void ensureHeadersSent() throws IOException {
    if (!this.headersSent) {
      long lengthToSend = (this.contentLength >= 0) ? this.contentLength : 0;
      this.sendHeaders(lengthToSend);
    }
  }

  private void sendHeaders(long lengthToSend) throws IOException {
    this.headersSent = true;
    // -1 tells the JDK there is no body; any other length on a no-body answer makes it warn and close the stream
    this.noBody = this.hasNoBody();
    long effectiveLength = this.noBody ? -1 : lengthToSend;
    this.noContentSent = (effectiveLength == -1);
    this.exchange.sendResponseHeaders(this.statusCode, effectiveLength);
  }

  /** Tells whether this answer carries no body: one to a {@code HEAD} request, or a {@code 1xx}, {@code 204}, or {@code 304}. */
  private boolean hasNoBody() {
    return "HEAD".equalsIgnoreCase(this.exchange.getRequestMethod()) || (this.statusCode < 200) || (this.statusCode == 204) || (this.statusCode == 304);
  }

  private final class BodyOutputStream extends OutputStream {
    private boolean closed = false;

    @Override
    public void write(int b) throws IOException {
      this.prepareWrite();
      if (!HttpExchangeResponse.this.noBody) {
        HttpExchangeResponse.this.exchange.getResponseBody().write(b);
      }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      Objects.checkFromIndexSize(off, len, b.length);
      if (len == 0) {
        return;
      }
      this.prepareWrite();
      if (!HttpExchangeResponse.this.noBody) {
        HttpExchangeResponse.this.exchange.getResponseBody().write(b, off, len);
      }
    }

    @Override
    public void flush() throws IOException {
      if (this.closed) {
        throw new IOException("Stream closed");
      }
      if (HttpExchangeResponse.this.headersSent && !HttpExchangeResponse.this.noContentSent) {
        HttpExchangeResponse.this.exchange.getResponseBody().flush();
      }
    }

    @Override
    public void close() throws IOException {
      if (this.closed) {
        return;
      }
      this.closed = true;
      if (!HttpExchangeResponse.this.headersSent) {
        HttpExchangeResponse.this.sendHeaders(-1);
        return;
      }
      if (!HttpExchangeResponse.this.noContentSent) {
        HttpExchangeResponse.this.exchange.getResponseBody().close();
      }
    }

    private void prepareWrite() throws IOException {
      if (this.closed || (!HttpExchangeResponse.this.noBody && HttpExchangeResponse.this.noContentSent)) {
        throw new IOException("Stream closed");
      }
      HttpExchangeResponse.this.ensureHeadersSent();
    }
  }
}
