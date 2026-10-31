// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.mail;

import javax.mail.MessagingException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JMBackEndConnectTest {
  private static final String SECRET = "s3cret-pw";

  private static JMBackEnd backEndForUnreachableServer() {
    MailConfig config = new MailConfig();
    config.setHost("127.0.0.1");
    config.setPort(1);
    return new JMBackEnd(config);
  }

  @Test
  public void connectAttemptDescriptionNeverContainsThePassword() {
    String description = JMBackEnd.describeConnectAttempt("alice", SECRET);
    assertFalse(description.contains(SECRET), description);
    assertTrue(description.contains("alice"), description);
  }

  @Test
  public void connectDoesNotPublishThePasswordInJvmSystemProperties() {
    try (JMBackEnd backEnd = backEndForUnreachableServer()) {
      assertThrows(MessagingException.class, () -> backEnd.connect("alice", SECRET));
    }
    assertNull(System.getProperty("mail.smtps.password"));
    assertNull(System.getProperty("mail.smtp.password"));
  }

  @Test
  public void connectWithoutPasswordFailsWithMessagingExceptionRatherThanNullPointerException() {
    try (JMBackEnd backEnd = backEndForUnreachableServer()) {
      assertThrows(MessagingException.class, () -> backEnd.connect("alice", null));
    }
  }
}
