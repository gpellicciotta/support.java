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
public class MailMessage
{
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

  public MailAddress getFrom() {
    return this.from;
  }

  private final MailAddress replyTo;

  public MailAddress getReplyTo() {
    return this.replyTo;
  }

  private final List<MailAddress> to;

  public List<MailAddress> getTo() {
    return this.to;
  }

  private final List<MailAddress> cc;

  public List<MailAddress> getCc() {
    return this.cc;
  }

  private final List<MailAddress> bcc;

  public List<MailAddress> getBcc() {
    return this.bcc;
  }

  private final String subject;

  public String getSubject() {
    return this.subject;
  }

  private final String body;
  private final String bodyMimeType;

  public String getBody() {
    return this.body;
  }

  public String getBodyMimeType() {
    return this.bodyMimeType;
  }

  private final List<MailAttachment> attachments;

  public List<MailAttachment> getAttachments() {
    return this.attachments;
  }

  // </editor-fold>
  // <editor-fold desc="Conversion Operators">

  public MimeMessage toMimeMessage(Session session) throws MessagingException, IOException {
    return this.toMimeMessage(session, null, new Date());
  }

  public MimeMessage toMimeMessage(Session session, String xMailerHeader) throws MessagingException, IOException {
    return this.toMimeMessage(session, xMailerHeader, new Date());
  }

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
