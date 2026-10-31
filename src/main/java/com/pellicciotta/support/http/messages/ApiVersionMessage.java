// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http.messages;

import com.pellicciotta.support.utils.Contract;

/**
 *  Reports the running API's version.
 */
public class ApiVersionMessage {
  /**
   *  Creates the message.
   *  @param version the API version; must not be null
   */
  public ApiVersionMessage(String version) {
    Contract.isNotNull(version);
    this.version = version;
  }

  /** The API version. */
  public final String version;
}
