// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

import java.util.ArrayList;
import java.util.List;

import com.pellicciotta.support.utils.Contract;

/**
 *  A plain, container-agnostic representation of an HTTP cookie: parsed from an incoming <code>Cookie</code>
 *  request header, or built here and serialized into an outgoing <code>Set-Cookie</code> response header.
 */
public final class Cookie {
  /**
   *  Creates a cookie with no attributes set (session lifetime, no path or domain, not secure, not HTTP-only).
   *  @param name the cookie name; must not be null
   *  @param value the cookie value; may be null
   */
  public Cookie(String name, String value) {
    Contract.isNotNull("name", name);
    this.name = name;
    this.value = value;
  }

  private final String name;
  private String value;
  private int maxAge = -1;
  private String path;
  private String domain;
  private boolean secure;
  private boolean httpOnly;

  /** @return The cookie name. */
  public String getName() {
    return this.name;
  }

  /** @return The cookie value, possibly null. */
  public String getValue() {
    return this.value;
  }
  /**
   *  Sets the cookie value.
   *  @param value the new value; null is serialized as an empty value
   *  @return This cookie, for chaining.
   */
  public Cookie setValue(String value) {
    this.value = value; return this;
  }

  /** @return The max age in seconds, {@code -1} meaning "session cookie" (no explicit expiry) and {@code 0} meaning "expire immediately". */
  public int getMaxAge() {
    return this.maxAge;
  }
  /**
   *  Sets the max age; see {@link #getMaxAge()} for the meaning of special values.
   *  @param maxAgeInSeconds the max age in seconds
   *  @return This cookie, for chaining.
   */
  public Cookie setMaxAge(int maxAgeInSeconds) {
    this.maxAge = maxAgeInSeconds; return this;
  }

  /** @return The cookie path, or null if none is set. */
  public String getPath() {
    return this.path;
  }
  /**
   *  Sets the cookie path.
   *  @param path the path, or null for none
   *  @return This cookie, for chaining.
   */
  public Cookie setPath(String path) {
    this.path = path; return this;
  }

  /** @return The cookie domain, or null if none is set. */
  public String getDomain() {
    return this.domain;
  }
  /**
   *  Sets the cookie domain.
   *  @param domain the domain, or null for none
   *  @return This cookie, for chaining.
   */
  public Cookie setDomain(String domain) {
    this.domain = domain; return this;
  }

  /** @return {@code true} if the {@code Secure} attribute is set. */
  public boolean isSecure() {
    return this.secure;
  }
  /**
   *  Sets the {@code Secure} attribute.
   *  @param secure whether the cookie is only sent over HTTPS
   *  @return This cookie, for chaining.
   */
  public Cookie setSecure(boolean secure) {
    this.secure = secure; return this;
  }

  /** @return {@code true} if the {@code HttpOnly} attribute is set. */
  public boolean isHttpOnly() {
    return this.httpOnly;
  }
  /**
   *  Sets the {@code HttpOnly} attribute.
   *  @param httpOnly whether the cookie is hidden from client-side scripts
   *  @return This cookie, for chaining.
   */
  public Cookie setHttpOnly(boolean httpOnly) {
    this.httpOnly = httpOnly; return this;
  }

  // <editor-fold desc="Factory Methods">

  // Omitting the domain because otherwise it won't work on localhost: https://stackoverflow.com/questions/1134290/cookies-on-localhost-with-explicit-domain

  /**
   *  Creates a session cookie (no max age) with path {@code /}, HTTP-only, and no domain.
   *  @param name the cookie name
   *  @param value the cookie value
   *  @return The new cookie.
   */
  public static Cookie createSessionCookie(String name, String value) {
    return new Cookie(name, value).setPath("/").setHttpOnly(true);
  }

  /**
   *  Creates a persistent cookie with path {@code /}, HTTP-only, secure, and the given domain.
   *  @param name the cookie name
   *  @param value the cookie value
   *  @param maxAgeInSeconds the lifetime in seconds
   *  @param domain the cookie domain
   *  @return The new cookie.
   */
  public static Cookie createSecurePersistentCookie(String name, String value, int maxAgeInSeconds, String domain) {
    return new Cookie(name, value).setDomain(domain).setMaxAge(maxAgeInSeconds).setPath("/").setHttpOnly(true).setSecure(true);
  }

  /**
   *  Creates a persistent cookie with path {@code /} and HTTP-only, with no domain and not secure.
   *  @param name the cookie name
   *  @param value the cookie value
   *  @param maxAgeInSeconds the lifetime in seconds
   *  @return The new cookie.
   */
  public static Cookie createPersistentCookie(String name, String value, int maxAgeInSeconds) {
    return new Cookie(name, value).setMaxAge(maxAgeInSeconds).setPath("/").setHttpOnly(true);
  }

  /**
   *  Creates a persistent cookie with path {@code /} and HTTP-only, with the given domain and not secure.
   *  @param name the cookie name
   *  @param value the cookie value
   *  @param maxAgeInSeconds the lifetime in seconds
   *  @param domain the cookie domain
   *  @return The new cookie.
   */
  public static Cookie createPersistentCookie(String name, String value, int maxAgeInSeconds, String domain) {
    return new Cookie(name, value).setDomain(domain).setMaxAge(maxAgeInSeconds).setPath("/").setHttpOnly(true);
  }

  /**
   *  Creates a cookie that makes the browser delete {@code original}: same name and domain, null value,
   *  max age 0, path {@code /}.
   *  @param original the cookie to undo; must not be null
   *  @return The new cookie.
   */
  public static Cookie createUndoCookie(Cookie original) {
    Contract.isNotNull("original", original);
    return new Cookie(original.name, null).setDomain(original.domain).setMaxAge(0).setPath("/");
  }

  /**
   *  Creates a cookie that makes the browser delete the named cookie (null value, max age 0, path {@code /},
   *  no domain).
   *  @param name the cookie name
   *  @return The new cookie.
   */
  public static Cookie createUndoCookie(String name) {
    return new Cookie(name, null).setMaxAge(0).setPath("/");
  }

  /**
   *  Creates a cookie that makes the browser delete the named cookie for a domain (null value, max age 0,
   *  path {@code /}).
   *  @param name the cookie name
   *  @param domain the cookie domain
   *  @return The new cookie.
   */
  public static Cookie createUndoCookie(String name, String domain) {
    return new Cookie(name, null).setDomain(domain).setMaxAge(0).setPath("/");
  }

  /**
   *  Like {@link #createUndoCookie(String, String)}, but also HTTP-only and secure, matching
   *  {@link #createSecurePersistentCookie}.
   *  @param name the cookie name
   *  @param domain the cookie domain
   *  @return The new cookie.
   */
  public static Cookie createSecureUndoCookie(String name, String domain) {
    return new Cookie(name, null).setDomain(domain).setMaxAge(0).setPath("/").setHttpOnly(true).setSecure(true);
  }

  // </editor-fold>
  // <editor-fold desc="Header parsing/serialization">

  /**
   *  Parse the value of an incoming <code>Cookie</code> request header (a semicolon-separated list of
   *  <code>name=value</code> pairs) into individual cookies. Only name and value are ever present on the
   *  request side; the other attributes (path/domain/secure/...) are response-side only.
   */
  static List<Cookie> parseCookieHeader(String headerValue) {
    List<Cookie> result = new ArrayList<>();
    if (headerValue == null) {
      return result;
    }
    for (String part : headerValue.split(";")) {
      String pair = part.trim();
      if (pair.isEmpty()) {
        continue;
      }
      int eq = pair.indexOf('=');
      if (eq < 0) {
        continue;
      }
      String name = pair.substring(0, eq).trim();
      String value = pair.substring(eq + 1).trim();
      result.add(new Cookie(name, value));
    }
    return result;
  }

  /**
   *  @return This cookie's value as it should appear in an outgoing <code>Set-Cookie</code> response header.
   */
  String toSetCookieHeaderValue() {
    StringBuilder sb = new StringBuilder();
    sb.append(this.name).append('=').append(this.value != null ? this.value : "");
    if (this.maxAge >= 0) {
      sb.append("; Max-Age=").append(this.maxAge);
    }
    if (this.path != null) {
      sb.append("; Path=").append(this.path);
    }
    if (this.domain != null) {
      sb.append("; Domain=").append(this.domain);
    }
    if (this.secure) {
      sb.append("; Secure");
    }
    if (this.httpOnly) {
      sb.append("; HttpOnly");
    }
    return sb.toString();
  }

  // </editor-fold>

  @Override
  public String toString() {
    return String.format("HTTP Cookie '%s': %s (max-age: %d, path: '%s', domain: '%s', secure: %s, http-only: %s)",
        this.name, this.value, this.maxAge, this.path, this.domain, this.secure, this.httpOnly);
  }
}
