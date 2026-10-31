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
  public ApiRouteMessage(HttpRoute route) {
    Contract.isNotNull(route);
    this.method = route.getMethod();
    this.uriPattern = route.getPath();
    this.description = route.getDescription();
    this.realms = route.getRealms();
  }

  public ApiRouteMessage(HttpMethod method, String uriPattern, String description, Set<String> realms) {
    Contract.isNotNull(method);
    Contract.isNotNull(uriPattern);
    Contract.isNotNull(description);
    this.method = method;
    this.uriPattern = uriPattern;
    this.description = description;
    this.realms = realms;
  }

  public final HttpMethod method;
  public final String uriPattern;
  public final String description;
  public final Set<String> realms;
}
