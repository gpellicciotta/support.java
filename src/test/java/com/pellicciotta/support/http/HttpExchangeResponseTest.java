// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HttpExchangeResponseTest {
  @Test
  public void testFinishAfterPositiveContentLengthWithNoStreamAccessSendsNoBodyLength() throws Exception {
    StubHttpExchange exchange = new StubHttpExchange("GET");
    newResponse(exchange, 200, 5).finish();

    assertSent(exchange, 200, -1L, "");
  }

  @Test
  public void testClosingOutputStreamWithoutWritingAfterPositiveContentLengthSendsNoBodyLength() throws Exception {
    StubHttpExchange exchange = new StubHttpExchange("GET");
    HttpExchangeResponse response = newResponse(exchange, 200, 5);
    try (OutputStream os = response.getOutputStream()) {
      os.write(new byte[0]);
    }
    response.finish();

    assertSent(exchange, 200, -1L, "");
  }

  @Test
  public void testFlushingEmptyWriterAfterPositiveContentLengthSendsNoBodyLength() throws Exception {
    StubHttpExchange exchange = new StubHttpExchange("GET");
    HttpExchangeResponse response = newResponse(exchange, 200, 5);
    Writer writer = response.getWriter();
    writer.write("");
    writer.flush();
    response.finish();

    assertSent(exchange, 200, -1L, "");
  }

  @Test
  public void testZeroContentLengthFinalizesEmptyOrStreamsChunkedWhenWritten() throws Exception {
    StubHttpExchange emptyExchange = new StubHttpExchange("GET");
    HttpExchangeResponse emptyResponse = newResponse(emptyExchange, 200, 0);
    try (OutputStream os = emptyResponse.getOutputStream()) {
      os.write(new byte[0]);
    }
    emptyResponse.finish();
    assertSent(emptyExchange, 200, -1L, "");

    StubHttpExchange writeExchange = new StubHttpExchange("GET");
    HttpExchangeResponse writeResponse = newResponse(writeExchange, 200, 0);
    try (OutputStream os = writeResponse.getOutputStream()) {
      os.write('x');
    }
    writeResponse.finish();
    assertSent(writeExchange, 200, 0L, "x");
  }

  @Test
  public void testWritingBodyAfterPositiveContentLengthSendsDeclaredLength() throws Exception {
    StubHttpExchange exchange = new StubHttpExchange("GET");
    byte[] body = "hello".getBytes(StandardCharsets.UTF_8);
    HttpExchangeResponse response = newResponse(exchange, 200, body.length);
    try (OutputStream os = response.getOutputStream()) {
      os.write(body);
    }
    response.finish();

    assertSent(exchange, 200, 5L, "hello");
  }

  @Test
  public void testWritingBodyWithoutContentLengthUsesChunkedEncoding() throws Exception {
    StubHttpExchange exchange = new StubHttpExchange("GET");
    HttpExchangeResponse response = new HttpExchangeResponse(exchange);
    response.setStatus(200);
    try (Writer writer = response.getWriter()) {
      writer.write("hello");
    }
    response.finish();

    assertSent(exchange, 200, 0L, "hello");
  }

  @Test
  public void testSendErrorAfterFlushedChunkedWriterThrowsAndLeavesStartedBodyUntouched() throws Exception {
    StubHttpExchange exchange = new StubHttpExchange("GET");
    HttpExchangeResponse response = new HttpExchangeResponse(exchange);
    response.setStatus(200);
    response.setContentType("application/json");
    Writer writer = response.getWriter();
    writer.write("{\"ok\":");
    writer.flush();

    Assertions.assertThrows(IOException.class, () -> response.sendError(500, "Internal server error"));

    assertSent(exchange, 200, 0L, "{\"ok\":");
    Assertions.assertEquals("application/json", exchange.getResponseHeaders().getFirst("Content-Type"));
  }

  @Test
  public void testSendErrorAfterUnflushedChunkedStreamWriteThrowsAndLeavesStartedBodyUntouched() throws Exception {
    StubHttpExchange exchange = new StubHttpExchange("GET");
    HttpExchangeResponse response = new HttpExchangeResponse(exchange);
    response.setStatus(200);
    OutputStream os = response.getOutputStream();
    os.write("partial".getBytes(StandardCharsets.UTF_8));

    Assertions.assertThrows(IOException.class, () -> response.sendError(500, "Internal server error"));

    assertSent(exchange, 200, 0L, "partial");
  }

  @Test
  public void testSendErrorAfterPartialFixedLengthWriteThrowsAndLeavesStartedBodyUntouched() throws Exception {
    StubHttpExchange exchange = new StubHttpExchange("GET");
    HttpExchangeResponse response = newResponse(exchange, 200, 100);
    response.setContentType("text/plain");
    OutputStream os = response.getOutputStream();
    os.write("partial".getBytes(StandardCharsets.UTF_8));

    Assertions.assertThrows(IOException.class, () -> response.sendError(500, "Internal server error"));

    assertSent(exchange, 200, 100L, "partial");
    Assertions.assertEquals("text/plain", exchange.getResponseHeaders().getFirst("Content-Type"));
  }

  @Test
  public void testSendErrorBeforeBytesWrittenReplacesStatusHeadersAndSendsErrorBody() throws Exception {
    StubHttpExchange exchange = new StubHttpExchange("GET");
    HttpExchangeResponse response = newResponse(exchange, 200, 100);
    response.setContentType("application/json");
    OutputStream os = response.getOutputStream();
    os.write(new byte[0]);

    response.sendError(500, "Internal server error");

    assertSent(exchange, 500, 21L, "Internal server error");
    Assertions.assertEquals("text/plain; charset=UTF-8", exchange.getResponseHeaders().getFirst("Content-Type"));
  }

  private static HttpExchangeResponse newResponse(StubHttpExchange exchange, int status, int contentLength) {
    HttpExchangeResponse response = new HttpExchangeResponse(exchange);
    response.setStatus(status);
    response.setContentLength(contentLength);
    return response;
  }

  private static void assertSent(StubHttpExchange exchange, int status, long length, String body) {
    Assertions.assertEquals(1, exchange.sendResponseHeadersCalls);
    Assertions.assertEquals(status, exchange.statusCode);
    Assertions.assertEquals(length, exchange.responseLength);
    Assertions.assertEquals(body, exchange.responseBody.toString(StandardCharsets.UTF_8));
  }

  private static final class StubHttpExchange extends HttpExchange {
    private final String method;
    private final Headers responseHeaders = new Headers();
    private final ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
    private int statusCode = -1;
    private long responseLength = Long.MIN_VALUE;
    private int sendResponseHeadersCalls = 0;

    private StubHttpExchange(String method) {
      this.method = method;
    }

    @Override
    public Headers getRequestHeaders() {
      return new Headers();
    }

    @Override
    public Headers getResponseHeaders() {
      return this.responseHeaders;
    }

    @Override
    public URI getRequestURI() {
      return URI.create("/test");
    }

    @Override
    public String getRequestMethod() {
      return this.method;
    }

    @Override
    public HttpContext getHttpContext() {
      return null;
    }

    @Override
    public void close() {
    }

    @Override
    public InputStream getRequestBody() {
      return InputStream.nullInputStream();
    }

    @Override
    public OutputStream getResponseBody() {
      return this.responseBody;
    }

    @Override
    public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
      if (this.sendResponseHeadersCalls > 0) {
        throw new IOException("headers already sent");
      }
      this.sendResponseHeadersCalls++;
      this.statusCode = rCode;
      this.responseLength = responseLength;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
      return null;
    }

    @Override
    public int getResponseCode() {
      return this.statusCode;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
      return null;
    }

    @Override
    public String getProtocol() {
      return "HTTP/1.1";
    }

    @Override
    public Object getAttribute(String name) {
      return null;
    }

    @Override
    public void setAttribute(String name, Object value) {
    }

    @Override
    public void setStreams(InputStream i, OutputStream o) {
    }

    @Override
    public HttpPrincipal getPrincipal() {
      return null;
    }
  }
}
