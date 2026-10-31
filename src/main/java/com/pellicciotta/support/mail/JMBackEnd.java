// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.mail;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.IOException;
import java.util.Properties;

import com.pellicciotta.support.logging.Log;
import com.pellicciotta.support.logging.LogService;
import com.pellicciotta.support.utils.CollectionUtils;
import com.pellicciotta.support.utils.Contract;

/**
 *  Class for sending email via a mail transfer system offered via the Java mail APIs.
**/
public class JMBackEnd extends MailBackEndBase {
  private static final Log log = LogService.fromCurrentClass();

  // <editor-fold desc="Construction">

  /**
   *  Create a mail back-end based on the default configuration.
   */
  public JMBackEnd() {
    this(new MailConfig());
  }


  /**
   *  Create a mail back-end based on the provided configuration.
   *
   *  @param config The mail-specific configuration.
   **/
  public JMBackEnd(MailConfig config) {
    super(config);
  }

  @Override
  protected void onMailSent(MailMessage mailMessage) {
    log.debug("Mail to '%s' with subject '%s' was sent successfully",
        CollectionUtils.toString(mailMessage.getTo(), "", ", ", ""),
        mailMessage.getSubject());
  }

  @Override
  protected void onMailError(MailMessage mailMessage, Exception cause) {
    log.debug("Mail to '%s' with subject '%s' was not sent successfully: %s",
        CollectionUtils.toString(mailMessage.getTo(), "", ", ", ""),
        mailMessage.getSubject(),
        cause);
  }

  // </editor-fold>
  // <editor-fold desc="Operations">

  private Transport connection;
  private Session session;

  /**
   * Tells whether the underlying transport currently exists and is connected.
   *
   * @return {@code true} if connected, {@code false} if never connected or already disconnected.
   */
  public boolean isConnected() {
    if (this.connection != null) {
      return this.connection.isConnected();
    }
    return false;
  }

  /**
   * Connects to the configured mail server using the user and password from the {@link MailConfig}.
   *
   * @throws MessagingException If the transport cannot be obtained or the connection fails.
   */
  public void connect() throws MessagingException {
    this.connect(this.config.getUser(), this.config.getPassword());
  }

  /**
   * Connects to the configured mail server with explicit credentials. Uses the {@code smtps} protocol when
   * {@code password} is non-null and {@code smtp} otherwise, and sets the matching {@code mail.*} JVM system
   * properties as a side effect. Replaces any existing session and transport.
   *
   * @param user User name for authentication.
   * @param password Password for authentication.
   * @throws MessagingException If the transport cannot be obtained or the connection fails.
   */
  public void connect(String user, String password) throws MessagingException {
    log.debug("Trying to connect with user '%s' and password '%s'...", user, password);
    String mailProtocol = password != null ? "smtps" : "smtp";
    Properties mailProps = System.getProperties();
    if (log.isDebugEnabled()) {
      mailProps.setProperty("mail.debug", "true");
    }
    else {
      mailProps.setProperty("mail.debug", "false");
    }
    mailProps.setProperty("mail." + mailProtocol + ".starttls.enable", "true");
    mailProps.setProperty("mail." + mailProtocol + ".auth", "true");
    mailProps.setProperty("mail." + mailProtocol + ".host", this.config.getHost());
    mailProps.setProperty("mail." + mailProtocol + ".port", "" + this.config.getPort());
    mailProps.setProperty("mail." + mailProtocol + ".user", user);
    mailProps.setProperty("mail." + mailProtocol + ".password", password);

    this.session = Session.getInstance(mailProps, null);
    this.connection = this.session.getTransport(mailProtocol);
    if ((user == null) || (password == null)) {
      this.connection.connect();
    }
    else {
      this.connection.connect(this.config.getHost(), this.config.getPort(), user, password);
    }
  }

  @Override
  public void close() {
    this.disconnect();
  }

  /**
   * Closes the transport if connected and discards the session and transport. Failures while closing are logged,
   * not thrown.
   */
  public void disconnect() {
    log.debug("Disconnecting...");
    try {
      if ((this.connection != null) && this.connection.isConnected()) {
        this.connection.close();
      }
    }
    catch (Exception e) {
      log.error("Disconnecting failed: %s", e);
    }
    finally {
      this.connection = null;
      this.session = null;
    }
  }

  // </editor-fold>
  // <editor-fold desc="MailBackEnd Interface">

  @Override
  protected void doSend(MailMessage msg) throws MessagingException, IOException {
    log.debug("Sending message...");
    MimeMessage message = msg.toMimeMessage(this.session);
    if (!this.isConnected()) {
      this.connect();
    }
    this.connection.sendMessage(message, message.getAllRecipients());
  }

  // </editor-fold>
  // <editor-fold desc="Textual Representation">

  @Override
  public String toString() {
    return String.format("smtp%s to %s:%d", this.config.getPassword() != null ? "s" : "", this.config.getHost(), this.config.getPort());
  }

  // </editor-fold>
}
