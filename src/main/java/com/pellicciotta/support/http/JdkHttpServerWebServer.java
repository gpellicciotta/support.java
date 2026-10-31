// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.pellicciotta.support.logging.Log;
import com.pellicciotta.support.logging.LogService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 *  A {@link WebServer} implementation on top of the JDK's built-in {@code com.sun.net.httpserver.HttpServer} —
 *  no servlet container, no external HTTP server dependency.
 *  <p>
 *  Registered {@link HttpRequestHandler}s are tried in registration order for every request, exactly like the
 *  {@link WebServer} contract describes; the first one that returns <code>true</code> from
 *  {@link HttpRequestHandler#handle} wins. This already covers what a servlet container's URL-pattern-based
 *  dispatch (a {@code web.xml}) would otherwise be needed for — a handler simply returns <code>false</code>
 *  for paths it doesn't own, e.g. via {@link HttpRoute}'s own method/path matching. A request nothing handles
 *  gets a plain 404.</p>
 */
public final class JdkHttpServerWebServer implements WebServer
{
  /**
   *  @param port The TCP port to listen on.
   */
  public JdkHttpServerWebServer(int port) {
    this(port, null);
  }

  /**
   *  @param port The TCP port to listen on.
   *  @param executor The executor to run request handling on. If <code>null</code>, a small cached thread pool is used.
   *                   Threading is deliberately the caller's choice, not hardcoded — e.g. pass
   *                   {@code Executors.newVirtualThreadPerTaskExecutor()} on a JDK that supports it.
   */
  public JdkHttpServerWebServer(int port, Executor executor) {
    this.port = port;
    this.executor = executor != null ? executor : Executors.newCachedThreadPool();
  }

  private final Log log = LogService.fromCurrentClass();
  private final int port;
  private final Executor executor;
  private final List<HttpRequestHandler> handlers = new CopyOnWriteArrayList<>();
  private volatile HttpServer server;
  private volatile boolean shuttingDown;

  @Override
  public void add(HttpRequestHandler middleware) {
    if (this.isRunning()) {
      throw new IllegalStateException("Cannot add a handler while the server is running");
    }
    this.handlers.add(middleware);
  }

  @Override
  public void remove(HttpRequestHandler middleware) {
    if (this.isRunning()) {
      throw new IllegalStateException("Cannot remove a handler while the server is running");
    }
    this.handlers.remove(middleware);
  }

  @Override
  public boolean isRunning() {
    return this.server != null;
  }

  @Override
  public boolean isShuttingDown() {
    return this.shuttingDown;
  }

  @Override
  public void start() {
    if (this.isRunning()) {
      throw new IllegalStateException("Already running");
    }
    try {
      HttpServer newServer = HttpServer.create(new InetSocketAddress(this.port), 0);
      newServer.setExecutor(this.executor);
      newServer.createContext("/", this::dispatch);
      newServer.start();
      this.server = newServer;
      this.log.info("WebServer started on port %d with %d handler(s)", newServer.getAddress().getPort(), this.handlers.size());
    }
    catch (IOException e) {
      throw new RuntimeException("Failed to start WebServer on port " + this.port, e);
    }
  }

  @Override
  public void shutDown() {
    if (!this.isRunning()) {
      throw new IllegalStateException("Not running");
    }
    if (this.shuttingDown) {
      return;
    }
    this.shuttingDown = true;
    try {
      this.server.stop(0);
      this.log.info("WebServer on port %d stopped", this.port);
    }
    finally {
      this.server = null;
      this.shuttingDown = false;
    }
  }

  private void dispatch(HttpExchange exchange) {
    long startTime = System.currentTimeMillis();
    Request request = new HttpExchangeRequest(exchange);
    Response response = new HttpExchangeResponse(exchange);
    try {
      boolean handled = false;
      for (HttpRequestHandler handler : this.handlers) {
        if (handler.handle(request, response)) {
          handled = true;
          break;
        }
      }
      if (!handled) {
        response.sendError(404, "No handler found for " + request.getMethod() + " " + request.getRequestURI());
      }
    }
    catch (Exception e) {
      this.log.error("Failed to handle %s %s: %s", request.getMethod(), request.getRequestURI(), e);
      try {
        response.sendError(500, "Internal server error");
      }
      catch (IOException ignored) {
        // Best effort only - the connection may already be broken
      }
    }
    finally {
      exchange.close();
      this.log.info("%s %s -> %d (%dms)", request.getMethod(), request.getRequestURI(), exchange.getResponseCode(), System.currentTimeMillis() - startTime);
    }
  }
}
