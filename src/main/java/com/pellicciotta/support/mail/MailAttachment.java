// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.mail;

import java.io.File;

import com.pellicciotta.support.http.HttpUtils;
import com.pellicciotta.support.utils.*;

/**
 *  A mail attachment: a file to be sent with the mail message.
**/
public class MailAttachment {
  // <editor-fold desc="Construction">

  /**
   * Create an attachment named after the file, with the MIME type derived from the file.
   *
   * @param file An existing regular file; must not be null.
   */
  public MailAttachment(File file) {
    this(null, file, null);
  }

  /**
   * Create an attachment with an explicit name and the MIME type derived from the file.
   *
   * @param name Name shown to the recipient; if null, the file name is used.
   * @param file An existing regular file; must not be null.
   */
  public MailAttachment(String name, File file) {
    this(name, file, null);
  }

  /**
   * Create an attachment with an explicit name and MIME type.
   *
   * @param name Name shown to the recipient; if null, the file name is used.
   * @param file An existing regular file; must not be null.
   * @param mimeType MIME type of the content; if null, it is derived from the file.
   */
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

  /**
   * @return The attachment name, which is the file name if no explicit name was given.
   */
  public String getName() {
    if (this.name != null) {
      return this.name;
    }
    return this.file.getName();
  }

  /**
   * @return The file whose content is attached.
   */
  public File getFile() {
    return this.file;
  }

  /**
   * @return The MIME type of the attachment, either as given or derived from the file.
   */
  public String getMimeType() {
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
