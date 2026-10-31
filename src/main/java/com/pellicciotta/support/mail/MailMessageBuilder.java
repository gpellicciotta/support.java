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

  public MailMessageBuilder from(String from) {
    this.from = new MailAddress(from);
    return this;
  }

  public MailMessageBuilder from(MailAddress from) {
    this.from = from;
    return this;
  }

  public MailMessageBuilder replyTo(String replyTo) {
    this.replyTo = new MailAddress(replyTo);
    return this;
  }

  public MailMessageBuilder replyTo(MailAddress replyTo) {
    this.replyTo = replyTo;
    return this;
  }

  public MailMessageBuilder to(String to) {
    this.to.add(new MailAddress(to));
    return this;
  }

  public MailMessageBuilder to(MailAddress to) {
    this.to.add(to);
    return this;
  }

  public MailMessageBuilder cc(String cc) {
    this.cc.add(new MailAddress(cc));
    return this;
  }

  public MailMessageBuilder cc(MailAddress cc) {
    this.cc.add(cc);
    return this;
  }

  public MailMessageBuilder bcc(String bcc) {
    this.bcc.add(new MailAddress(bcc));
    return this;
  }

  public MailMessageBuilder bcc(MailAddress bcc) {
    this.bcc.add(bcc);
    return this;
  }

  public MailMessageBuilder subject(String subject) {
    this.subject = subject;
    return this;
  }

  public MailMessageBuilder plainTextBody(File bodyFile) throws IOException {
    this.body = FileUtils.readTextFile(bodyFile);
    this.bodyMimeType = "text/plain";
    return this;
  }

  public MailMessageBuilder plainTextBody(String body) {
    this.body = body;
    this.bodyMimeType = "text/plain";
    return this;
  }

  public MailMessageBuilder htmlTextBody(File bodyFile) throws IOException {
    this.body = FileUtils.readTextFile(bodyFile);
    this.bodyMimeType = "text/html";
    return this;
  }

  public MailMessageBuilder htmlTextBody(String body) {
    this.body = body;
    this.bodyMimeType = "text/html";
    return this;
  }

  public MailMessageBuilder body(String mimeType, String body) {
    this.body = body;
    this.bodyMimeType = mimeType;
    return this;
  }

  public MailMessageBuilder body(String mimeType, File bodyFile) throws IOException {
    this.body = FileUtils.readTextFile(bodyFile);
    this.bodyMimeType = mimeType;
    return this;
  }

  public MailMessageBuilder body(File bodyFile) throws IOException {
    this.body = FileUtils.readTextFile(bodyFile);
    this.bodyMimeType = HttpUtils.getMimeType(bodyFile);
    return this;
  }

  public MailMessageBuilder attachment(MailAttachment attachment) {
    this.attachments.add(attachment);
    return this;
  }

  // </editor-fold>
  // <editor-fold desc="Build Operations">

  public MailMessage build() throws ConfigurationException {
    if (this.to.isEmpty()) {
      throw new ConfigurationException("to", this.to, "At least one 'to' address must be provided");
    }
    MailMessage msg = new MailMessage(this.from, this.replyTo, this.to, this.cc, this.bcc, this.subject, this.body, this.bodyMimeType, this.attachments);
    return msg;
  }

  // </editor-fold>
}
