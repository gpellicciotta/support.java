package com.pellicciotta.support.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

class StaticResource implements Resource {
  public StaticResource(byte[] content, long lastModifiedTime, String mimeType) throws IOException {
    assert content != null;
    assert mimeType != null;
    this.lastModified = lastModifiedTime;
    this.mimeType = mimeType;
    this.content = content;
  }

  private final long lastModified;
  private final String mimeType;
  private final byte[] content;

  public String getMimeType() {
    return this.mimeType;
  }

  public long getLastModified() {
    return this.lastModified;
  }

  public int getContentLength() {
    return this.content.length;
  }

  public InputStream getContentStream() throws IOException {
    return new ByteArrayInputStream(this.content);
  }
}
