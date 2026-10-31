// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http.messages;

/**
 *  A {@link ResultMessage} with {@code status} fixed to {@code "ok"}.
 */
public class OkMessage extends ResultMessage
{
  public OkMessage(String message) {
    super("ok", message);
  }
}
