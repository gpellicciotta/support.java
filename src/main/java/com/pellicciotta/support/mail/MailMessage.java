// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.mail;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.IOException;
import java.util.*;

import com.pellicciotta.support.text.TextBuilder;
import com.pellicciotta.support.utils.CollectionUtils;
import static javax.mail.Message.RecipientType.*;

/**
 *  A mail message.
 */
public class MailMessage {
  // <editor-fold desc="Construction">

  MailMessage(MailAddress from, MailAddress replyTo, List<MailAddress> to, List<MailAddress> cc, List<MailAddress> bcc, String subject, String body, String bodyMimeType, List<MailAttachment> attachments) {
    assert from != null;
    assert to != null;
    assert to.size() > 0;
    assert cc != null;
    assert bcc != null;
    assert attachments != null;

    this.from = from;
    this.replyTo = replyTo;
    this.to = new ArrayList<>(to);
    this.cc = new ArrayList<>(cc);
    this.bcc = new ArrayList<>(bcc);
    this.subject = subject;
    this.body = body;
    this.bodyMimeType = bodyMimeType;
    this.attachments = new ArrayList<>(attachments);
  }

  // </editor-fold>
  // <editor-fold desc="Properties">

  private final MailAddress from;

  /**
   * @return The sender address.
   */
  public MailAddress getFrom() {
    return this.from;
  }

  private final MailAddress replyTo;

  /**
   * @return The reply-to address; may be null.
   */
  public MailAddress getReplyTo() {
    return this.replyTo;
  }

  private final List<MailAddress> to;

  /**
   * @return The primary recipients (never empty when built via {@link MailMessageBuilder}).
   */
  public List<MailAddress> getTo() {
    return this.to;
  }

  private final List<MailAddress> cc;

  /**
   * @return The carbon-copy recipients; may be empty.
   */
  public List<MailAddress> getCc() {
    return this.cc;
  }

  private final List<MailAddress> bcc;

  /**
   * @return The blind-carbon-copy recipients; may be empty.
   */
  public List<MailAddress> getBcc() {
    return this.bcc;
  }

  private final String subject;

  /**
   * @return The subject line; may be null.
   */
  public String getSubject() {
    return this.subject;
  }

  private final String body;
  private final String bodyMimeType;

  /**
   * @return The message body text.
   */
  public String getBody() {
    return this.body;
  }

  /**
   * @return The MIME type of the body, such as {@code text/plain} or {@code text/html}.
   */
  public String getBodyMimeType() {
    return this.bodyMimeType;
  }

  private final List<MailAttachment> attachments;

  /**
   * @return The attachments; may be empty.
   */
  public List<MailAttachment> getAttachments() {
    return this.attachments;
  }

  // </editor-fold>
  // <editor-fold desc="Conversion Operators">

  /**
   * Convert to a JavaMail message using the current time as sent date and no X-Mailer header.
   *
   * @param session The mail session to create the message in.
   * @return The MIME message.
   * @throws MessagingException If setting any header, address, or content fails.
   * @throws IOException If reading an attachment file fails.
   */
  public MimeMessage toMimeMessage(Session session) throws MessagingException, IOException {
    return this.toMimeMessage(session, null, new Date());
  }

  /**
   * Convert to a JavaMail message using the current time as sent date.
   *
   * @param session The mail session to create the message in.
   * @param xMailerHeader If non-null, an {@code X-Mailer} header is set (to a fixed value, not this argument).
   * @return The MIME message.
   * @throws MessagingException If setting any header, address, or content fails.
   * @throws IOException If reading an attachment file fails.
   */
  public MimeMessage toMimeMessage(Session session, String xMailerHeader) throws MessagingException, IOException {
    return this.toMimeMessage(session, xMailerHeader, new Date());
  }

  /**
   * Convert to a JavaMail message. A multipart message is produced if there are attachments or the body is not
   * {@code text/plain}; otherwise a single-part UTF-8 text message.
   *
   * @param session The mail session to create the message in.
   * @param xMailerHeader If non-null, an {@code X-Mailer} header is set (to a fixed value, not this argument).
   * @param sentDate The sent date; if null, the current time is used.
   * @return The MIME message.
   * @throws MessagingException If setting any header, address, or content fails.
   * @throws IOException If reading an attachment file fails.
   */
  public MimeMessage toMimeMessage(Session session, String xMailerHeader, Date sentDate) throws MessagingException, IOException {
    MimeMessage mimeMsg = new MimeMessage(session);
    if (xMailerHeader != null) {
      mimeMsg.setHeader("X-Mailer", "SendMailTool");
    }
    mimeMsg.setSentDate(sentDate != null ? sentDate : new Date());
    // Set From: header field of the header.
    if (this.getFrom() != null) {
      mimeMsg.setFrom(this.getFrom().toInternetAddress());
    }
    else {
      mimeMsg.setFrom();
    }
    if (this.getReplyTo() != null) {
      mimeMsg.setReplyTo(new InternetAddress[] { this.getReplyTo().toInternetAddress() });
    }
    for (MailAddress to : this.getTo()) {
      mimeMsg.addRecipient(TO, to.toInternetAddress());
    }
    for (MailAddress cc : this.getCc()) {
      mimeMsg.addRecipient(CC, cc.toInternetAddress());
    }
    for (MailAddress bcc : this.getBcc()) {
      mimeMsg.addRecipient(BCC, bcc.toInternetAddress());
    }
    mimeMsg.setSubject(this.getSubject());
    if (!this.getAttachments().isEmpty() || !this.getBodyMimeType().equals("text/plain")) {
      Multipart multipart = new MimeMultipart();
      // First part is body text
      MimeBodyPart messagePart = new MimeBodyPart();
      messagePart.setContent(this.getBody(), this.getBodyMimeType());
      multipart.addBodyPart(messagePart);
      // Next parts are the actual attachments
      for (MailAttachment attachment : this.getAttachments()) {
        // Attach the specified file.
        messagePart = new MimeBodyPart();
        messagePart.attachFile(attachment.getFile());
        messagePart.setFileName(attachment.getName());
        // Alternative to .attachFile
        //  FileDataSource fds = new FileDataSource(attachment.getFile()) {
        //    public String getContentType() {
        //      return attachment.getMimeType();
        //    }
        //  };
        //messagePart.setDataHandler(new DataHandler(fds));
        multipart.addBodyPart(messagePart);
      }
      mimeMsg.setContent(multipart);
    }
    else {
      mimeMsg.setText(this.getBody(), "UTF-8", this.getBodyMimeType().substring(this.getBodyMimeType().indexOf("/") + 1));
    }
    return mimeMsg;
  }

  // </editor-fold>
  // <editor-fold desc="Textual Representation">

  /**
   *  @return A multi-line text formatted as:
   *          <pre>
   *            From: ${alias} ${from-address}
   *            Subject: ${subject}
   *            To: ${to-address}
   *            Cc: ${cc-address}
   *            Bcc: ${bcc-address}
   *          </pre>
  **/
  @Override
  public String toString() {
    TextBuilder tb = new TextBuilder();
    tb.append("From: %s", this.from);
    if (this.subject != null) {
      tb.appendLine();
      tb.append("Subject: %s", this.subject);
    }
    if (!this.to.isEmpty()) {
      tb.appendLine();
      tb.append("To: %s", CollectionUtils.toString(this.to, "", ", ", ""));
    }
    if (!this.cc.isEmpty()) {
      tb.appendLine();
      tb.append("Cc: %s", CollectionUtils.toString(this.cc, "", ", ", ""));
    }
    if (!this.bcc.isEmpty()) {
      tb.appendLine();
      tb.append("Bcc: %s", CollectionUtils.toString(this.bcc, "", ", ", ""));
    }
    return tb.toString();
  }

  // </editor-fold>
}
