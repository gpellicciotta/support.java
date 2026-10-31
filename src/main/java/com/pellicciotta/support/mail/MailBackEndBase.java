// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.mail;

import com.pellicciotta.support.logging.Log;
import com.pellicciotta.support.logging.LogLevel;
import com.pellicciotta.support.logging.Span;
import com.pellicciotta.support.logging.spi.*;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *  Base class for implementing a {@link MailBackEnd}.
 *  <p>
 *  Implementing classes must ensure to remain thread-safe.</p>
 */
public abstract class MailBackEndBase implements MailBackEnd {
  // <editor-fold desc="Construction">

  /**
   * Create a back-end with a default {@link MailConfig}.
   */
  protected MailBackEndBase() {
    this(new MailConfig());
  }

  /**
   * Create a back-end with the provided configuration.
   *
   * @param config The configuration to be applied.
   */
  protected MailBackEndBase(MailConfig config) {
    this.configure(config);
  }

  /** Whether {@link #close()} has been called. */
  protected boolean closed = false;
  /** The active configuration, as last set by {@link #configure(MailConfig)}. */
  protected MailConfig config = null;

  // </editor-fold>
  // <editor-fold desc="Properties">

  @Override
  public boolean isClosed() {
    return this.closed;
  }

  // </editor-fold>
  // <editor-fold desc="Overridable Methods">

  /**
   * Called after a message was sent successfully.
   *
   * @param mailMessage The message that was sent.
   */
  protected abstract void onMailSent(MailMessage mailMessage);
  /**
   * Called when sending a message failed, before the exception is rethrown by {@link #send(MailMessage)}.
   *
   * @param mailMessage The message that could not be sent.
   * @param cause The failure.
   */
  protected abstract void onMailError(MailMessage mailMessage, Exception cause);

  // </editor-fold>
  // <editor-fold desc="LogBackEnd Interface">

  /**
   *  Configure the back-end.
   *
   *  @param config The configuration to be applied.
   */
  public synchronized void configure(MailConfig config) {
    this.config = config;
  }

  /**
   *  Send a mail message.
   *
   *  @param msg The message to be sent, including all meta-data.
   */
  public final void send(MailMessage msg) throws Exception {
    try {
      this.doSend(msg);
      this.onMailSent(msg);
    }
    catch (Exception e) {
      this.onMailError(msg, e);
      throw e;
    }
  }

  /**
   * Performs the actual delivery of a message.
   *
   * @param msg The message to be sent.
   * @throws MessagingException If the mail system rejects or fails to send the message.
   * @throws IOException If reading the message content or attachments fails.
   */
  protected abstract void doSend(MailMessage msg) throws MessagingException, IOException;

  /**
   *  Close this back-end, which closing any connections.
   */
  public synchronized void close() {
    this.closed = true; // Close, so no new activity can happen
  }

  // </editor-fold>
}
