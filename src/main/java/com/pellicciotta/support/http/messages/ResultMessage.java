// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http.messages;

import com.pellicciotta.support.utils.Contract;

/**
 *  Generic {@code status}/{@code message} REST response envelope.
 */
public class ResultMessage {
  /**
   *  Creates the message.
   *  @param status the status value; must not be null
   *  @param message the message text; must not be null
   */
  public ResultMessage(String status, String message) {
    Contract.isNotNull(status);
    Contract.isNotNull(message);
    this.status = status;
    this.message = message;
  }

  /** The status value. */
  public final String status;
  /** The message text. */
  public final String message;
}
