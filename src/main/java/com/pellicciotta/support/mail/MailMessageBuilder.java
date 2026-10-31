// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.mail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.pellicciotta.support.exceptions.ConfigurationException;
import com.pellicciotta.support.http.HttpUtils;
import com.pellicciotta.support.utils.FileUtils;

/**
 *  A builder of {@link MailMessage}s that are ready for sending.
 */
public class MailMessageBuilder implements AutoCloseable {
  // <editor-fold desc="Construction">

  /**
   * Create an empty builder.
   */
  public MailMessageBuilder() { }

  @Override
  public void close() {
    this.from = null;
    this.replyTo = null;
    this.to.clear();
    this.cc.clear();
    this.bcc.clear();
    this.subject = null;
    this.body = null;
    this.attachments.clear();
  }

  // </editor-fold>
  // <editor-fold desc="Properties">

  private MailAddress from;
  private MailAddress replyTo;
  private final List<MailAddress> to = new ArrayList<>();
  private final List<MailAddress> cc = new ArrayList<>();
  private final List<MailAddress> bcc = new ArrayList<>();
  private String subject;
  private String body;
  private String bodyMimeType;
  private List<MailAttachment> attachments = new ArrayList<>();;

  /**
   * Set the sender from an address string.
   *
   * @param from Address text.
   * @return This builder.
   * @throws IllegalArgumentException If the address is invalid.
   */
  public MailMessageBuilder from(String from) {
    this.from = new MailAddress(from);
    return this;
  }

  /**
   * Set the sender.
   *
   * @param from The sender address.
   * @return This builder.
   */
  public MailMessageBuilder from(MailAddress from) {
    this.from = from;
    return this;
  }

  /**
   * Set the reply-to address from an address string.
   *
   * @param replyTo Address text.
   * @return This builder.
   * @throws IllegalArgumentException If the address is invalid.
   */
  public MailMessageBuilder replyTo(String replyTo) {
    this.replyTo = new MailAddress(replyTo);
    return this;
  }

  /**
   * Set the reply-to address.
   *
   * @param replyTo The reply-to address.
   * @return This builder.
   */
  public MailMessageBuilder replyTo(MailAddress replyTo) {
    this.replyTo = replyTo;
    return this;
  }

  /**
   * Add a primary recipient from an address string.
   *
   * @param to Address text.
   * @return This builder.
   * @throws IllegalArgumentException If the address is invalid.
   */
  public MailMessageBuilder to(String to) {
    this.to.add(new MailAddress(to));
    return this;
  }

  /**
   * Add a primary recipient.
   *
   * @param to The recipient address.
   * @return This builder.
   */
  public MailMessageBuilder to(MailAddress to) {
    this.to.add(to);
    return this;
  }

  /**
   * Add a carbon-copy recipient from an address string.
   *
   * @param cc Address text.
   * @return This builder.
   * @throws IllegalArgumentException If the address is invalid.
   */
  public MailMessageBuilder cc(String cc) {
    this.cc.add(new MailAddress(cc));
    return this;
  }

  /**
   * Add a carbon-copy recipient.
   *
   * @param cc The recipient address.
   * @return This builder.
   */
  public MailMessageBuilder cc(MailAddress cc) {
    this.cc.add(cc);
    return this;
  }

  /**
   * Add a blind-carbon-copy recipient from an address string.
   *
   * @param bcc Address text.
   * @return This builder.
   * @throws IllegalArgumentException If the address is invalid.
   */
  public MailMessageBuilder bcc(String bcc) {
    this.bcc.add(new MailAddress(bcc));
    return this;
  }

  /**
   * Add a blind-carbon-copy recipient.
   *
   * @param bcc The recipient address.
   * @return This builder.
   */
  public MailMessageBuilder bcc(MailAddress bcc) {
    this.bcc.add(bcc);
    return this;
  }

  /**
   * Set the subject line.
   *
   * @param subject The subject.
   * @return This builder.
   */
  public MailMessageBuilder subject(String subject) {
    this.subject = subject;
    return this;
  }

  /**
   * Set the body to the content of a text file, with MIME type {@code text/plain}.
   *
   * @param bodyFile File to read.
   * @return This builder.
   * @throws IOException If the file cannot be read.
   */
  public MailMessageBuilder plainTextBody(File bodyFile) throws IOException {
    this.body = FileUtils.readTextFile(bodyFile);
    this.bodyMimeType = "text/plain";
    return this;
  }

  /**
   * Set the body to the given text, with MIME type {@code text/plain}.
   *
   * @param body Body text.
   * @return This builder.
   */
  public MailMessageBuilder plainTextBody(String body) {
    this.body = body;
    this.bodyMimeType = "text/plain";
    return this;
  }

  /**
   * Set the body to the content of a text file, with MIME type {@code text/html}.
   *
   * @param bodyFile File to read.
   * @return This builder.
   * @throws IOException If the file cannot be read.
   */
  public MailMessageBuilder htmlTextBody(File bodyFile) throws IOException {
    this.body = FileUtils.readTextFile(bodyFile);
    this.bodyMimeType = "text/html";
    return this;
  }

  /**
   * Set the body to the given text, with MIME type {@code text/html}.
   *
   * @param body Body text.
   * @return This builder.
   */
  public MailMessageBuilder htmlTextBody(String body) {
    this.body = body;
    this.bodyMimeType = "text/html";
    return this;
  }

  /**
   * Set the body to the given text with an explicit MIME type.
   *
   * @param mimeType MIME type of the body.
   * @param body Body text.
   * @return This builder.
   */
  public MailMessageBuilder body(String mimeType, String body) {
    this.body = body;
    this.bodyMimeType = mimeType;
    return this;
  }

  /**
   * Set the body to the content of a text file with an explicit MIME type.
   *
   * @param mimeType MIME type of the body.
   * @param bodyFile File to read.
   * @return This builder.
   * @throws IOException If the file cannot be read.
   */
  public MailMessageBuilder body(String mimeType, File bodyFile) throws IOException {
    this.body = FileUtils.readTextFile(bodyFile);
    this.bodyMimeType = mimeType;
    return this;
  }

  /**
   * Set the body to the content of a text file, with the MIME type derived from the file.
   *
   * @param bodyFile File to read.
   * @return This builder.
   * @throws IOException If the file cannot be read.
   */
  public MailMessageBuilder body(File bodyFile) throws IOException {
    this.body = FileUtils.readTextFile(bodyFile);
    this.bodyMimeType = HttpUtils.getMimeType(bodyFile);
    return this;
  }

  /**
   * Add an attachment.
   *
   * @param attachment The attachment.
   * @return This builder.
   */
  public MailMessageBuilder attachment(MailAttachment attachment) {
    this.attachments.add(attachment);
    return this;
  }

  // </editor-fold>
  // <editor-fold desc="Build Operations">

  /**
   * Build the message from the current state of this builder.
   *
   * @return The new message.
   * @throws ConfigurationException If no {@code to} address was provided.
   */
  public MailMessage build() throws ConfigurationException {
    if (this.to.isEmpty()) {
      throw new ConfigurationException("to", this.to, "At least one 'to' address must be provided");
    }
    MailMessage msg = new MailMessage(this.from, this.replyTo, this.to, this.cc, this.bcc, this.subject, this.body, this.bodyMimeType, this.attachments);
    return msg;
  }

  // </editor-fold>
}
