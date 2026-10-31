// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.mail;

import com.pellicciotta.support.logging.spi.LoggingConfig;

import javax.mail.*;
import java.io.IOException;

/**
 *  Class for sending email via a mail transfer system.
 */
public interface MailBackEnd extends AutoCloseable {
  /**
   *  Configure the back-end.
   *
   *  @param config The configuration to be applied.
   */
  void configure(MailConfig config);


  /** @return Whether this back-end is closed and hence can no longer be used. */
  boolean isClosed();

  /**
   *  Close this back-end, which also means closing any open connections.
   */
  void close();

  /**
   *  Send a mail message.
   *
   *  @param msg The message to be sent, including all meta-data.
   *
   *  @throws Exception If sending the mail has certainly failed.
   */
  void send(MailMessage msg) throws Exception;
}
