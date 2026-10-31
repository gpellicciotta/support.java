// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http.messages;

import com.pellicciotta.support.utils.Contract;

/**
 *  Generic {@code status}/{@code message} REST response envelope.
 */
public class ResultMessage {
  public ResultMessage(String status, String message) {
    Contract.isNotNull(status);
    Contract.isNotNull(message);
    this.status = status;
    this.message = message;
  }

  public final String status;
  public final String message;
}
