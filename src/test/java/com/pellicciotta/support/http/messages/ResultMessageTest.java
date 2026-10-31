// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http.messages;

import org.junit.jupiter.api.*;

public class ResultMessageTest {
  @Test
  public void testFields() {
    ResultMessage msg = new ResultMessage("status", "message");
    Assertions.assertEquals("status", msg.status);
    Assertions.assertEquals("message", msg.message);
  }

  @Test
  public void testRejectsNulls() {
    Assertions.assertThrows(RuntimeException.class, () -> new ResultMessage(null, "message"));
    Assertions.assertThrows(RuntimeException.class, () -> new ResultMessage("status", null));
  }

  @Test
  public void testErrorMessage() {
    ErrorMessage msg = new ErrorMessage("oops");
    Assertions.assertEquals("error", msg.status);
    Assertions.assertEquals("oops", msg.message);
  }

  @Test
  public void testOkMessage() {
    OkMessage msg = new OkMessage("done");
    Assertions.assertEquals("ok", msg.status);
    Assertions.assertEquals("done", msg.message);
  }

  @Test
  public void testSessionIdMessage() {
    SessionIdMessage msg = new SessionIdMessage("abc-123");
    Assertions.assertEquals("session-id", msg.status);
    Assertions.assertEquals("abc-123", msg.message);
  }
}
