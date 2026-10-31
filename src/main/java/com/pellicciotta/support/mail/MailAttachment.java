// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.mail;

import java.io.File;

import com.pellicciotta.support.http.HttpUtils;
import com.pellicciotta.support.utils.*;

/**
 *  A mail attachment: a file to be sent with the mail message.
**/
public class MailAttachment
{
  // <editor-fold desc="Construction">

  public MailAttachment(File file) {
    this(null, file, null);
  }

  public MailAttachment(String name, File file) {
    this(name, file, null);
  }

  public MailAttachment(String name, File file, String mimeType) {
    Contract.isNotNull("file", file);
    Contract.isTrue("file.exists() && file.isFile()", file.exists() && file.isFile());
    this.name = name;
    this.file = file;
    this.mimeType = mimeType != null ? mimeType : HttpUtils.getMimeType(this.file);
  }

  // </editor-fold>
  // <editor-fold desc="Properties">

  private final String name;
  private final File file;
  private final String mimeType;

  public String getName() {
    if (this.name != null) {
      return this.name;
    }
    return this.file.getName();
  }

  public File getFile()
  {
    return this.file;
  }

  public String getMimeType()
  {
    return this.mimeType;
  }

  // </editor-fold>
  // <editor-fold desc="Textual Representation">

  /**
   *  @return <pre>[attachment '${file-name}' (mime-type: ${mime-type})]</pre>
  **/
  @Override
  public String toString() {
    return String.format("[attachment '%s' (mime-type: %s)]", this.name, this.mimeType);
  }

  // </editor-fold>
}
