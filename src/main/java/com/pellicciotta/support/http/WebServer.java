// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

/**
 *  The high-level API for working with an HTTP web server.
 */
public interface WebServer
{
  /**
   *  Register middleware at the end of the current list.
   *  <p>
   *  Since each middleware can decide to 'complete' the current request handling, it is important to
   *  invoke this method in the correct order.</p>
   *  <p>
   *  Can only be invoked on a stopped server.</p>
   *
   *  @param middleware The handler to be add. This middleware should be thread-safe.
   *
   *  @throws IllegalStateException If the server is currently started.
   */
  void add(HttpRequestHandler middleware);

  /**
   *  Unregister middleware.
   *  <p>
   *  Can only be invoked on a stopped server.</p>
   *
   *  @param middleware The handler to be removed (if it was as previously added, otherwise this is a no-op.
   *
   *  @throws IllegalStateException If the server is currently started.
   */
  void remove(HttpRequestHandler middleware);

  /**
   *  @return Whether the server is started and hence ready to handle HTTP(S) requests. Will return <code>true</code> also when already shutting down.
   */
  boolean isRunning();

  /**
   * @return Whether shutdown has been requested via {@link #shutDown} but the server is currently still running.
   */
  boolean isShuttingDown();

  /**
   *  Start (or re-start) the current server, which means it will start listening on the configured port.
   *  <p>
   *  This method will return as soon as a listening port is established.
   *  </p>
   *
   *  @throws RuntimeException If starting fails.
   *  @throws UnsupportedOperationException If this server doesn't support requesting to start.
   */
  default void start() {
    throw new UnsupportedOperationException("Starting will happen automatically and can not be requested programmatically.");
  }

  /**
   *  Request shutdown of the currently running server.
   *  <p>
   *  Can only be invoked on a {@link #isRunning()} server.</p>
   *  <p>
   *  When the server is already in the {@link #isShuttingDown()} state, this is a no-op.</p>
   *  <p>
   *  Shutdown will always happen asynchronously and can take considerable time if requests are being handled still.</p>
   *
   *  @throws RuntimeException If requesting shut down fails.
   *  @throws UnsupportedOperationException If this server doesn't support requesting shutdown.
   */
  default void shutDown() {
    throw new UnsupportedOperationException("Shutting down can not be requested programmatically.");
  }
}
