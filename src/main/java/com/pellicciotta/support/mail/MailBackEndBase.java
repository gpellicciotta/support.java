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

  protected MailBackEndBase() {
    this(new MailConfig());
  }

  protected MailBackEndBase(MailConfig config) {
    this.configure(config);
  }

  protected boolean closed = false;
  protected MailConfig config = null;

  // </editor-fold>
  // <editor-fold desc="Properties">

  @Override
  public boolean isClosed() {
    return this.closed;
  }

  // </editor-fold>
  // <editor-fold desc="Overridable Methods">

  protected abstract void onMailSent(MailMessage mailMessage);
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

  protected abstract void doSend(MailMessage msg) throws MessagingException, IOException;

  /**
   *  Close this back-end, which closing any connections.
   */
  public synchronized void close() {
    this.closed = true; // Close, so no new activity can happen
  }

  // </editor-fold>
}
