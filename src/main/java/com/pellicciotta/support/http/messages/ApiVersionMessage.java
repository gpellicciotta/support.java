// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http.messages;

import com.pellicciotta.support.utils.Contract;

/**
 *  Reports the running API's version.
 */
public class ApiVersionMessage {
  public ApiVersionMessage(String version) {
    Contract.isNotNull(version);
    this.version = version;
  }

  public final String version;
}
