// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http.messages;

/**
 *  A {@link ResultMessage} with {@code status} fixed to {@code "ok"}.
 */
public class OkMessage extends ResultMessage {
  /**
   *  Creates an ok message.
   *  @param message the message text; must not be null
   */
  public OkMessage(String message) {
    super("ok", message);
  }
}
