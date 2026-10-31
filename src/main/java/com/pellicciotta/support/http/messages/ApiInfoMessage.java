// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http.messages;

import com.pellicciotta.support.utils.Contract;

import java.util.List;

/**
 *  Reports the running API's version plus its full set of routes.
 */
public class ApiInfoMessage extends ApiVersionMessage {
  /**
   *  Creates the message.
   *  @param version the API version; must not be null
   *  @param webRoutes the routes to report; must not be null
   */
  public ApiInfoMessage(String version, List<ApiRouteMessage> webRoutes) {
    super(version);
    Contract.isNotNull(webRoutes);
    this.apiEndPoints = webRoutes;
  }

  /** The routes of the API. */
  public final List<ApiRouteMessage> apiEndPoints;
}
