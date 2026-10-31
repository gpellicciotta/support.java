// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http.messages;

import com.pellicciotta.support.http.HttpMethod;
import com.pellicciotta.support.http.HttpRoute;
import com.pellicciotta.support.utils.Contract;

import java.util.Set;

/**
 *  Describes a single {@link HttpRoute} for inclusion in an {@link ApiInfoMessage}.
 */
public class ApiRouteMessage {
  /**
   *  Creates the message from a route, copying its method, path, description, and realms.
   *  @param route the route to describe; must not be null
   */
  public ApiRouteMessage(HttpRoute route) {
    Contract.isNotNull(route);
    this.method = route.getMethod();
    this.uriPattern = route.getPath();
    this.description = route.getDescription();
    this.realms = route.getRealms();
  }

  /**
   *  Creates the message from explicit values.
   *  @param method the HTTP method; must not be null
   *  @param uriPattern the URI pattern; must not be null
   *  @param description the description; must not be null
   *  @param realms the allowed realms
   */
  public ApiRouteMessage(HttpMethod method, String uriPattern, String description, Set<String> realms) {
    Contract.isNotNull(method);
    Contract.isNotNull(uriPattern);
    Contract.isNotNull(description);
    this.method = method;
    this.uriPattern = uriPattern;
    this.description = description;
    this.realms = realms;
  }

  /** The HTTP method of the route. */
  public final HttpMethod method;
  /** The URI pattern of the route. */
  public final String uriPattern;
  /** The description of the route. */
  public final String description;
  /** The realms allowed to access the route. */
  public final Set<String> realms;
}
