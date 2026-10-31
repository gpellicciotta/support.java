// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.mail;

import com.pellicciotta.support.logging.Log;
import com.pellicciotta.support.logging.LogService;

/**
 *  Dummy no-op implementation
**/
public class DummyBackEnd extends MailBackEndBase
{
  private static final Log log = LogService.fromCurrentClass();

  @Override
  protected final void onMailSent(MailMessage mailMessage) { }

  @Override
  protected final void onMailError(MailMessage mailMessage, Exception cause) { }

  /**
   *  Send a mail message.
   *
   *  @param msg The message to be sent, including all meta-data.
   */
  protected final void doSend(MailMessage msg) {
    log.warning("Following mail message could not be sent as no real mail back-end has been configured:%n%s", msg);
  }
}
