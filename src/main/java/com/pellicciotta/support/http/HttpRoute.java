// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  A specific HTTP route with associated processor method as implementation of the {@link HttpRequestHandler} interface.
 */
public final class HttpRoute implements HttpRequestHandler, Comparable<HttpRoute>
{
  private static final Pattern GROUP_NAMES_PATTERN = Pattern.compile("[(]?[<]([a-zA-Z0-9]+)[>]");

  HttpRoute(HttpMethod method, String path, HttpRequestProcessor processor, Set<String> realms, String description) {
    assert method != null;
    assert path != null;
    assert processor != null;

    this.method = method;
    this.path = path;
    this.description = description;
    this.processor = processor;
    this.urlRegex = Pattern.compile(path);
    this.groupNames = new HashSet<>();
    Matcher groupMatcher = GROUP_NAMES_PATTERN.matcher(this.path);
    while (groupMatcher.find()) {
      this.groupNames.add(groupMatcher.group(1));
    }
    this.realms = new HashSet<>(realms);
  }

  private final HttpMethod method;

  public HttpMethod getMethod() {
    return this.method;
  }

  private final String path;

  public String getPath() {
    return this.path;
  }

  private final Set<String> realms;

  public boolean isRestricted() {
    return !this.realms.isEmpty();
  }

  public Set<String> getRealms() {
    return this.realms;
  }

  private final String description;

  public String getDescription() {
    return this.description;
  }

  private transient final Pattern urlRegex;

  private transient final Set<String> groupNames;

  public Set<String> getPathGroupNames() {
    return this.groupNames;
  }

  private transient final HttpRequestProcessor processor;

  @Override
  public boolean handle(Request req, Response resp) throws Exception {
    // Check HTTP method
    if (!req.getMethod().equalsIgnoreCase(this.method.name())) {
      return false;
    }
    // Check HTTP URI
    Matcher m = this.urlRegex.matcher(req.getRequestURI());
    if (!m.matches()) {
      return false;
    }
    // Add named group as request attributes
    for (String groupName : this.groupNames) {
      String v = m.group(groupName);
      if (v != null) {
        req.setAttribute(groupName, v);
      }
    }
    // Set route too
    req.setAttribute(HttpRoute.class.getName(), this);
    // Process
    this.processor.process(req, resp);
    return true;
  }

  @Override
  public int compareTo(HttpRoute o) {
    // Paths:
    int cmp = this.path.compareTo(o.path);
    if (cmp != 0) {
      return cmp;
    }
    // Less path group names first
    cmp = Integer.compare(this.groupNames.size(), o.groupNames.size());
    if (cmp != 0) {
      return cmp;
    }
    // Methods:
    cmp = this.method.compareTo(o.method);
    if (cmp != 0) {
      return cmp;
    }
    // Description last:
    return this.description.compareTo(o.description);
  }

  @Override
  public String toString() {
    return String.format("%s %s%n%s", this.method, this.path, this.description);
  }
}
