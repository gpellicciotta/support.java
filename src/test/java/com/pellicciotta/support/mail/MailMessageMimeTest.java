// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.mail;

import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MailMessageMimeTest {
  private static MailMessage message() {
    try (MailMessageBuilder builder = new MailMessageBuilder()) {
      return builder.from("sender@example.com").to("receiver@example.com").subject("Hello").plainTextBody("Body").build();
    }
  }

  @Test
  public void xMailerHeaderCarriesTheValueGivenByTheCaller() throws Exception {
    Session session = Session.getInstance(new Properties());
    MimeMessage mime = message().toMimeMessage(session, "my-tool 1.2");
    assertArrayEquals(new String[] {"my-tool 1.2"}, mime.getHeader("X-Mailer"));
  }

  @Test
  public void xMailerHeaderIsAbsentWhenNoValueIsGiven() throws Exception {
    Session session = Session.getInstance(new Properties());
    MimeMessage mime = message().toMimeMessage(session);
    assertNull(mime.getHeader("X-Mailer"));
  }
}
