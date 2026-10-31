// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.mail;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import com.pellicciotta.support.exceptions.ConfigurationException;
import com.pellicciotta.support.logging.Log;
import com.pellicciotta.support.logging.LogLevel;
import com.pellicciotta.support.logging.LogService;
import com.pellicciotta.support.logging.spi.LoggerConfig;
import com.pellicciotta.support.logging.spi.LoggingConfig;
import com.pellicciotta.support.utils.DateTimeUtils;
import com.pellicciotta.support.utils.EnvironmentUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.mail.*;

public class MailBackEndTest {
  private static final Log log = LogService.fromCurrentClass();

  private static File getFileFromURL(String resourcePath) {
    ClassLoader clsLdr = MailBackEndTest.class.getClassLoader();
    URL url = clsLdr.getResource(resourcePath);
    File file = null;
    try {
      file = new File(url.toURI());
    }
    catch (URISyntaxException e) {
      file = new File(url.getPath());
    }
    finally {
      return file;
    }
  }

  private static MailMessage createMessage(String msgText) {
    MailAddress from = new MailAddress("noreply", "pellicciotta.com");

    MailAddress to = new MailAddress("giovanni.pellicciotta", "pellicciotta.com");
    MailAddress cc1 = new MailAddress("luca.pellicciotta", "gmail.com");
    MailAddress cc2 = new MailAddress("noah.pellicciotta", "gmail.com");

    MailAttachment a1 = new MailAttachment(getFileFromURL("test-attachment.txt"));
    MailAttachment a2 = new MailAttachment("Test Attachment HTML", getFileFromURL("test-attachment.html"));

    MailMessage msg = new MailMessageBuilder().to(to)
                                              .subject(msgText)
                                              .from(from)
                                              .cc(cc1)
                                              .cc(cc2)
                                              .attachment(a1)
                                              .attachment(a2)
                                              .htmlTextBody("<html><head><title>Just a sample HTML message</title></head><body><h1>Just a Test 3</h1><p>Really, just a test mail for testing my mail sending software</body></html>")
                                              .build();

    return msg;
  }

  @Test
  public void createAttachments() {
    MailAttachment a1 = new MailAttachment(getFileFromURL("test-attachment.txt"));
    MailAttachment a2 = new MailAttachment("Test Attachment HTML", getFileFromURL("test-attachment.html"));
  }

  @Disabled // Need password
  @Test
  public void sendWithGmailTest() throws Exception {
    MailConfig config = new MailConfig();
    config.setUser("pellicciotta@gmail.com"); // This account allows unsafe apps and therefore login via user/password
    config.setPassword(System.getProperty("pellicciotta-gmail-password"));
    config.setHost("smtp.gmail.com");
    config.setSSL(true);

    String msgText = "A sample sendWithGmailTest message sent on " + DateTimeUtils.formatDateTime(new Date());
    MailMessage msg = createMessage(msgText);

    try (JMBackEnd c = new JMBackEnd(config)) {
      c.send(msg);
    }
  }

  @Disabled // Need password
  @Test
  public void sendWithGmailTest2() throws Exception {
    LoggingConfig logConfig = new LoggingConfig();
    LoggerConfig loggerConfig = new LoggerConfig(JMBackEnd.class.getName(), LogLevel.DEBUG);
    logConfig.addLoggerConfig(loggerConfig);
    LogService.configure(logConfig);

    MailConfig config = new MailConfig();
    config.setHost("smtp.gmail.com");
    config.setUser("pellicciotta@gmail.com");
    config.setPassword(EnvironmentUtils.getStringProperty("pellicciotta-gmail-password"));

    String msgText = "A sample sendWithGmailTest2 email message sent on " + DateTimeUtils.formatDateTime(new Date());
    MailMessage msg = createMessage(msgText);

    try (JMBackEnd c = new JMBackEnd(config)) {
      c.send(msg);
    }
  }

  @Disabled // Need password
  @Test
  public void sendWithSendGridTest() throws Exception {
    LoggingConfig logConfig = new LoggingConfig();
    LoggerConfig loggerConfig = new LoggerConfig(JMBackEnd.class.getName(), LogLevel.INFO);
    logConfig.addLoggerConfig(loggerConfig);
    LogService.configure(logConfig);

    MailConfig config = new MailConfig();
    config.setHost("smtp.sendgrid.net");
    config.setUser("apikey");
    config.setPassword(EnvironmentUtils.getStringProperty("pellicciotta-sendgrid-apikey"));

    String msgText = "SendGrid sample email message sent on " + DateTimeUtils.formatDateTime(new Date());
    MailMessage msg = createMessage(msgText);

    try (JMBackEnd c = new JMBackEnd(config)) {
      c.send(msg);
    }
  }

  @Test
  public void sendWithDummyBackEndTest() {
    String msgText = "A sample email message #10";
    MailMessage msg = createMessage(msgText);

    try {
      MailService.send(msg);
    }
    catch (Exception e) {
      log.error("Sending failed: %s", e);
    }
  }
}
