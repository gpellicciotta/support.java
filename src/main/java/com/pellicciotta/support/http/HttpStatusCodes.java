// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

/**
 *  Common HTTP status-code constants, named after the {@code javax.servlet.http.HttpServletResponse.SC_*}
 *  convention. Covers the subset actually used across this library's known consumers; extend as needed.
 */
public final class HttpStatusCodes {
  private HttpStatusCodes() { }

  public static final int SC_OK = 200;
  public static final int SC_CREATED = 201;
  public static final int SC_ACCEPTED = 202;
  public static final int SC_NO_CONTENT = 204;
  public static final int SC_BAD_REQUEST = 400;
  public static final int SC_UNAUTHORIZED = 401;
  public static final int SC_FORBIDDEN = 403;
  public static final int SC_CONFLICT = 409;
  public static final int SC_TOO_MANY_REQUESTS = 429;
  public static final int SC_INTERNAL_SERVER_ERROR = 500;
  public static final int SC_SERVICE_UNAVAILABLE = 503;
}
