// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

/**
 *  Classes implementing this interface can participate in the handling of an HTTP request.
 *  <p>
 *  This interface is different from {@link HttpRequestProcessor} in that implementations of the current
 *  interface can indicate whether further processing is needed or not.</p>
 */
@FunctionalInterface
public interface HttpRequestHandler
{
  /**
   *  Inspect the request and handle it completely or partially.
   *  <p>
   *  A component can choose to change the request (and response) and hence act as middleware.</p>
   *
   *  @param request The request to be handled.
   *  @param response The response to be sent ultimately.
   *
   *  @return Whether the response should be considered complete and sent back to the client.
   *          If <code>false</code>, both the request and response objects will be passed on
   *          to the next registered {@link HttpRequestHandler}s or lead to an error (and a corresponding
   *          HTTP 500 code) if there are no further handlers.
   */
  boolean handle(Request request, Response response) throws Exception;
}
