// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http.messages;

/**
 *  A {@link ResultMessage} with {@code status} fixed to {@code "error"}.
 */
public class ErrorMessage extends ResultMessage
{
  public ErrorMessage(String message) {
    super("error", message);
  }
}
