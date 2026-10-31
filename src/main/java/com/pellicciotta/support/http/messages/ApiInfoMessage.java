// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http.messages;

import com.pellicciotta.support.utils.Contract;

import java.util.List;

/**
 *  Reports the running API's version plus its full set of routes.
 */
public class ApiInfoMessage extends ApiVersionMessage
{
  public ApiInfoMessage(String version, List<ApiRouteMessage> webRoutes) {
    super(version);
    Contract.isNotNull(webRoutes);
    this.apiEndPoints = webRoutes;
  }

  public final List<ApiRouteMessage> apiEndPoints;
}
