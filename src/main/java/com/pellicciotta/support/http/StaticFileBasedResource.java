package com.pellicciotta.support.http;

import java.io.*;

class StaticFileBasedResource implements Resource {
  public StaticFileBasedResource(File file, String mimeType) {
    assert file != null;
    assert mimeType != null;
    this.file = file;
    this.mimeType = mimeType;
  }

  /** The backing file. */
  protected final File file;
  /** The MIME type reported for the file. */
  protected final String mimeType;

  public String getMimeType() {
    return this.mimeType;
  }

  public long getLastModified() {
    return this.file.lastModified();
  }

  public int getContentLength() {
    return (int)this.file.length();
  }

  public InputStream getContentStream() throws IOException {
    return new BufferedInputStream(new FileInputStream(this.file));
  }
}
