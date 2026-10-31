// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http.messages;

/**
 *  A {@link ResultMessage} with {@code status} fixed to {@code "session-id"}.
 */
public class SessionIdMessage extends ResultMessage
{
  public SessionIdMessage(String id) {
    super("session-id", id);
  }
}
