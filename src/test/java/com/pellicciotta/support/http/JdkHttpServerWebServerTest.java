// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class JdkHttpServerWebServerTest
{
  private JdkHttpServerWebServer server;
  private int port;
  private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

  @BeforeEach
  public void findFreePort() throws IOException {
    try (ServerSocket socket = new ServerSocket(0)) {
      this.port = socket.getLocalPort();
    }
  }

  @AfterEach
  public void stopServer() {
    if ((this.server != null) && this.server.isRunning()) {
      this.server.shutDown();
    }
  }

  private HttpResponse<String> get(String path) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + this.port + path))
        .timeout(Duration.ofSeconds(5))
        .GET()
        .build();
    return this.client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  @TempDir
  Path files;

  // The JDK server's logger, held here so it stays alive, and the no-body warnings it logged during a test
  private static final Logger JDK_SERVER_LOG = Logger.getLogger("com.sun.net.httpserver");
  private final List<String> noBodyWarnings = Collections.synchronizedList(new ArrayList<>());
  private final Handler warningListener = new Handler() {
    @Override
    public void publish(LogRecord logRecord) {
      if ((logRecord.getLevel().intValue() >= Level.WARNING.intValue()) && String.valueOf(logRecord.getMessage()).contains("sendResponseHeaders")) {
        JdkHttpServerWebServerTest.this.noBodyWarnings.add(logRecord.getMessage());
      }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }
  };

  @BeforeEach
  public void listenForNoBodyWarnings() {
    JDK_SERVER_LOG.addHandler(this.warningListener);
  }

  @AfterEach
  public void stopListening() {
    JDK_SERVER_LOG.removeHandler(this.warningListener);
  }

  private HttpResponse<String> send(String method, String path) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + this.port + path))
        .timeout(Duration.ofSeconds(5))
        .method(method, HttpRequest.BodyPublishers.noBody())
        .build();
    return this.client.send(request, HttpResponse.BodyHandlers.ofString());
  }

  // Registers a handler that answers with the given status, a declared length, and a body, recording a failed write;
  // the latch opens when it finishes, since a client of a body-less answer gets it before the handler writes
  private void addWritingHandler(int status, AtomicReference<IOException> failure, CountDownLatch done) {
    this.server.add((req, resp) -> {
      try {
        byte[] body = "hello".getBytes(StandardCharsets.UTF_8);
        resp.setStatus(status);
        resp.setContentType("text/plain");
        resp.setContentLength(body.length);
        try (OutputStream os = resp.getOutputStream()) {
          os.write(body);
        }
        catch (IOException e) {
          failure.set(e);
        }
        return true;
      }
      finally {
        done.countDown();
      }
    });
  }

  @Test
  public void testHeadRequestGetsNoBodyAndTheHandlersWriteDoesNotFail() throws Exception {
    AtomicReference<IOException> failure = new AtomicReference<>();
    CountDownLatch done = new CountDownLatch(2);
    this.server = new JdkHttpServerWebServer(this.port);
    this.addWritingHandler(200, failure, done);
    this.server.start();

    HttpResponse<String> head = this.send("HEAD", "/page");
    HttpResponse<String> get = this.get("/page");
    Assertions.assertTrue(done.await(5, TimeUnit.SECONDS));

    Assertions.assertEquals(200, head.statusCode());
    Assertions.assertEquals("", head.body());
    Assertions.assertNull(failure.get(), "Writing the body of a HEAD response must not fail");
    Assertions.assertEquals(List.of(), this.noBodyWarnings, "The JDK server is told the HEAD answer has no body");
    Assertions.assertEquals("hello", get.body());
  }

  @Test
  public void testNotModifiedAndNoContentAnswersDropTheBody() throws Exception {
    for (int status : new int[] {304, 204}) {
      AtomicReference<IOException> failure = new AtomicReference<>();
      CountDownLatch done = new CountDownLatch(1);
      this.server = new JdkHttpServerWebServer(this.port);
      this.addWritingHandler(status, failure, done);
      this.server.start();

      HttpResponse<String> response = this.get("/page");
      Assertions.assertTrue(done.await(5, TimeUnit.SECONDS));

      Assertions.assertEquals(status, response.statusCode());
      Assertions.assertEquals("", response.body());
      Assertions.assertNull(failure.get(), "Writing the body of a " + status + " response must not fail");
      Assertions.assertEquals(List.of(), this.noBodyWarnings, "The JDK server is told the " + status + " answer has no body");
      this.server.shutDown();
      this.findFreePort();
    }
  }

  @Test
  public void testAStatusChangedAfterANoBodyAnswerWentOutStillGetsNoBodyStream() throws Exception {
    AtomicReference<IOException> failure = new AtomicReference<>();
    CountDownLatch done = new CountDownLatch(1);
    this.server = new JdkHttpServerWebServer(this.port);
    this.server.add((req, resp) -> {
      try {
        resp.setStatus(304);
        resp.getOutputStream().close();
        // The headers went out as a 304, so a later error message must not reach for the closed JDK stream
        resp.setStatus(500);
        resp.getOutputStream().write("too late".getBytes(StandardCharsets.UTF_8));
      }
      catch (IOException e) {
        failure.set(e);
      }
      finally {
        done.countDown();
      }
      return true;
    });
    this.server.start();

    HttpResponse<String> response = this.get("/page");
    Assertions.assertTrue(done.await(5, TimeUnit.SECONDS));

    Assertions.assertEquals(304, response.statusCode());
    Assertions.assertNull(failure.get(), "A write after a no-body answer went out must not fail");
  }

  @Test
  public void testHeadRequestForAStaticFileDoesNotFail() throws Exception {
    Files.writeString(this.files.resolve("index.html"), "<p>Hello</p>", StandardCharsets.UTF_8);
    AtomicReference<Exception> failure = new AtomicReference<>();
    CountDownLatch done = new CountDownLatch(1);
    StaticFileHandler files = new StaticFileHandler(this.files.toString());
    this.server = new JdkHttpServerWebServer(this.port);
    this.server.add((req, resp) -> {
      try {
        return files.handle(req, resp);
      }
      catch (IOException e) {
        failure.set(e);
        throw e;
      }
      finally {
        done.countDown();
      }
    });
    this.server.start();

    HttpResponse<String> head = this.send("HEAD", "/index.html");
    Assertions.assertTrue(done.await(5, TimeUnit.SECONDS));

    Assertions.assertEquals(200, head.statusCode());
    Assertions.assertEquals("", head.body());
    Assertions.assertNull(failure.get(), "Serving a file for a HEAD request must not fail");
  }

  @Test
  public void testHandlerRespondsToRealRequest() throws Exception {
    this.server = new JdkHttpServerWebServer(this.port);
    this.server.add((req, resp) -> {
      resp.setStatus(200);
      resp.setContentType("text/plain");
      Writer writer = resp.getWriter();
      writer.write("hello from " + req.getRequestURI());
      writer.flush();
      return true;
    });
    this.server.start();

    HttpResponse<String> response = this.get("/greet");

    Assertions.assertEquals(200, response.statusCode());
    Assertions.assertEquals("hello from /greet", response.body());
  }

  @Test
  public void testNoHandlerMatchesReturns404() throws Exception {
    this.server = new JdkHttpServerWebServer(this.port);
    this.server.add((req, resp) -> false);
    this.server.start();

    HttpResponse<String> response = this.get("/nowhere");

    Assertions.assertEquals(404, response.statusCode());
  }

  @Test
  public void testHandlerThrowingReturns500() throws Exception {
    this.server = new JdkHttpServerWebServer(this.port);
    this.server.add((req, resp) -> {
      throw new RuntimeException("boom");
    });
    this.server.start();

    HttpResponse<String> response = this.get("/explode");

    Assertions.assertEquals(500, response.statusCode());
  }

  @Test
  public void testFirstMatchingHandlerWins() throws Exception {
    this.server = new JdkHttpServerWebServer(this.port);
    this.server.add((req, resp) -> false);
    this.server.add((req, resp) -> {
      resp.setStatus(200);
      Writer writer = resp.getWriter();
      writer.write("second handler");
      writer.flush();
      return true;
    });
    this.server.add((req, resp) -> {
      throw new AssertionError("Should never be reached");
    });
    this.server.start();

    HttpResponse<String> response = this.get("/anything");

    Assertions.assertEquals(200, response.statusCode());
    Assertions.assertEquals("second handler", response.body());
  }

  @Test
  public void testAddAfterStartThrows() {
    this.server = new JdkHttpServerWebServer(this.port);
    this.server.start();

    Assertions.assertThrows(IllegalStateException.class, () -> this.server.add((req, resp) -> true));
  }
}
