// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http.messages;

/**
 *  A {@link ResultMessage} with {@code status} fixed to {@code "session-id"}.
 */
public class SessionIdMessage extends ResultMessage {
  /**
   *  Creates a session-id message.
   *  @param id the session id, sent as the message text; must not be null
   */
  public SessionIdMessage(String id) {
    super("session-id", id);
  }
}
