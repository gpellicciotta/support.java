// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

/**
 *  Interface to be implemented by components that are capable of handling a {@link Request}
 *  and resulting in a {@link Response}.
 *  <p>
 *  This interface is different from {@link HttpRequestHandler} in that the latter can indicate whether further
 *  processing is needed, while this interface merely represent the processing itself.</p>
 */
@FunctionalInterface
public interface HttpRequestProcessor
{
  /**
   *  Process the request and fill the response.
   *
   *  @param request The HTTP request.
   *  @param response The HTTP response.
   */
  void process(Request request, Response response) throws Exception;
}
