// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 *  A server-agnostic representation of an outgoing HTTP response. {@link HttpExchangeResponse} is the only
 *  backing implementation today (on top of {@code com.sun.net.httpserver.HttpExchange}), but nothing in this
 *  interface is tied to it.
 *  <p>
 *  Headers, status and content-length (if known) must be set <em>before</em> the first call to
 *  {@link #getOutputStream()}/{@link #getWriter()} — once the body starts being written, they can no longer
 *  be changed.</p>
 */
public interface Response {
  /** Set the HTTP status code to send, e.g. <code>200</code>. Defaults to <code>200</code> if never called. */
  void setStatus(int statusCode);

  /** Set (replacing any previous value) a response header. */
  void setHeader(String name, String value);

  /** Convenience for {@code setHeader("Content-Type", contentType)}. */
  default void setContentType(String contentType) {
    setHeader("Content-Type", contentType);
  }

  /**
   *  Declare the exact number of bytes that will be written to the body. If never called, the body is sent
   *  with chunked transfer-encoding instead, which works for any length but is marginally less efficient.
   */
  void setContentLength(int contentLength);

  /** @return A stream to write the response body to, as bytes. */
  OutputStream getOutputStream() throws IOException;

  /** @return A writer to write the response body to, as UTF-8 encoded text. */
  default Writer getWriter() throws IOException {
    return new OutputStreamWriter(getOutputStream(), StandardCharsets.UTF_8);
  }

  /** Add a {@code Set-Cookie} response header for the given cookie. Can be called more than once. */
  void addCookie(Cookie cookie);

  /**
   *  Set the status code and send a short, plain-text error message as the entire response body.
   *  Convenience for the common "this request failed, here's a one-line explanation" case.
   */
  void sendError(int statusCode, String message) throws IOException;
}
