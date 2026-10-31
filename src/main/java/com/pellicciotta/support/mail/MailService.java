// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.mail;

import com.pellicciotta.support.logging.spi.LogBackEnd;
import com.pellicciotta.support.logging.spi.LoggingConfig;

import javax.mail.MessagingException;
import java.io.IOException;
import java.lang.reflect.Constructor;

/**
 *  A service capable of sending {@link MailMessage}s.
 *  <p>
 *  This {@link MailService} should be configured once at the very start of an application, and henceforth only used
 *  to send {@link MailMessage} objects.</p>
 */
public final class MailService {
  // <editor-fold desc="Configuration">

  /** Prevent instantiation */
  private MailService() { }

  private static final MailBackEnd builtinDefaultMailBackEnd = new DummyBackEnd();

  private static MailBackEnd mailBackEnd = builtinDefaultMailBackEnd;

  /**
   *  Configure a new {@link MailBackEnd}.
   *  <p>
   *  In general, this method should only be used once at the start of a process and never be invoked
   *  from 'normal' code.</p>
   *
   *  @param backEnd The code that will be responsible from now one to send email messages.
   *                 If <code>null</code> the default configuration will be restored.
   */
  public static synchronized void setBackEnd(MailBackEnd backEnd) {
    if (backEnd != null) {
      mailBackEnd = backEnd;
    }
    else {
      mailBackEnd = builtinDefaultMailBackEnd;
    }
  }

  /**
   *  Configure a back-end and then invoke {@link #setBackEnd(MailBackEnd)} with the newly constructed
   *  and configured {@link MailBackEnd}.
   *
   *  @param config The configuration to be used and applied.
   */
  public static synchronized void configure(MailConfig config) throws Exception {
    Class<? extends MailBackEnd> backEndClass = config.getBackEndType();
    Constructor<? extends MailBackEnd> backEndCtor = backEndClass.getConstructor();
    MailBackEnd backEnd = backEndCtor.newInstance();
    backEnd.configure(config);
    setBackEnd(backEnd);
  }

  // </editor-fold>
  // <editor-fold desc="Mail Service Functions">

  /**
   * Send a simple text mail message to a single receiver.
   *
   * @param from The email address of the message sender.
   * @param to The email address of the message receiver.
   * @param subject The email subject.
   * @param textBody The email body as pure text.
   */
  public static synchronized void send(String from, String to, String subject, String textBody) throws Exception {
    MailMessageBuilder msgBuilder = new MailMessageBuilder();
    MailMessage msg = msgBuilder.from(from).to(to).subject(subject).plainTextBody(textBody).build();
    mailBackEnd.send(msg);
  }

  /**
   * Send a simple text mail message to a single receiver.
   *
   * @param from The email address of the message sender.
   * @param to The email address of the message receiver.
   * @param subject The email subject.
   * @param textBody The email body as pure text.
   */
  public static synchronized void send(MailAddress from, MailAddress to, String subject, String textBody) throws Exception {
    MailMessageBuilder msgBuilder = new MailMessageBuilder();
    MailMessage msg = msgBuilder.from(from).to(to).subject(subject).plainTextBody(textBody).build();
    mailBackEnd.send(msg);
  }

  /**
   * Send a mail message.
   *
   * @param mail The message to be sent.
   */
  public static synchronized void send(MailMessage mail) throws Exception {
    mailBackEnd.send(mail);
  }

  // </editor-fold>
}
