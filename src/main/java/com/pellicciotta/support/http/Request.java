// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import com.pellicciotta.support.utils.Pair;

/**
 *  A server-agnostic representation of an incoming HTTP request. {@link HttpExchangeRequest} is the only
 *  backing implementation today (on top of {@code com.sun.net.httpserver.HttpExchange}), but nothing in this
 *  interface is tied to it.
 */
public interface Request
{
  /** @return The HTTP method, e.g. <code>"GET"</code>. */
  String getMethod();

  /** @return The request path, not including any query string. */
  String getRequestURI();

  /** @return The raw query string (the part after <code>?</code>), or <code>null</code> if there is none. */
  String getQueryString();

  /** @return The value of the named header, or <code>null</code> if not present. If the header occurs more than once, the first value. */
  String getHeader(String name);

  /** @return The names of all headers present on this request. */
  Set<String> getHeaderNames();

  /** @return The request body as a byte stream. */
  InputStream getInputStream() throws IOException;

  /** @return The request body as a character stream, decoded as UTF-8. */
  default Reader getReader() throws IOException {
    return new InputStreamReader(getInputStream(), StandardCharsets.UTF_8);
  }

  /** @return The value of a request-scoped attribute previously set via {@link #setAttribute}, or <code>null</code>. */
  Object getAttribute(String name);

  /** Associate a value with this request, e.g. for handlers further down the chain to pick up. */
  void setAttribute(String name, Object value);

  /** @return All cookies sent with this request. Never <code>null</code>, but can be empty. */
  List<Cookie> getCookies();

  /** @return The cookie with the given name, or <code>null</code> if not present. */
  default Cookie getCookie(String name) {
    for (Cookie c : getCookies()) {
      if (name.equals(c.getName())) {
        return c;
      }
    }
    return null;
  }

  /**
   *  @return The value after <code>Bearer </code> in the <code>Authorization</code> header, if any, otherwise <code>null</code>.
   */
  default String getBearerAuthorization() {
    String authHeader = getHeader("Authorization");
    if ((authHeader != null) && authHeader.startsWith("Bearer ")) {
      return authHeader.substring("Bearer ".length()).trim();
    }
    return null;
  }

  /**
   *  @return The value after <code>Basic </code> in the <code>Authorization</code> header, if any, otherwise <code>null</code>.
   */
  default String getBasicAuthorization() {
    String authHeader = getHeader("Authorization");
    if ((authHeader != null) && authHeader.startsWith("Basic ")) {
      return authHeader.substring("Basic ".length()).trim();
    }
    return null;
  }

  /**
   *  The <code>Basic</code> authorization header contains the base64-encoded value of a string
   *  that contains a user name, a colon and a password.
   *
   *  @return A pair consisting of the decoded user and password fields, or <code>null</code> if no basic authorization header was found.
   *
   *  @throws com.pellicciotta.support.exceptions.FormatException If a basic authorization header was found but its value has an invalid structure.
   */
  default Pair<String, String> getBasicAuthorizationParts() {
    String basicAuth = getBasicAuthorization();
    if (basicAuth == null) {
      return null;
    }
    return HttpUtils.decodeBasicAuthorizationParts(basicAuth);
  }
}
