// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

class StaticUrlBasedResource implements Resource
{
  public StaticUrlBasedResource(URL url, long lastModified, String mimeType, int contentLength) {
    assert url != null;
    this.url = url;
    this.lastModified = lastModified;
    this.mimeType = mimeType;
    this.contentLength = contentLength;
  }

  private final URL url;
  private final long lastModified;
  private final String mimeType;
  private final int contentLength;

  @Override
  public String getMimeType() {
    return this.mimeType;
  }

  @Override
  public long getLastModified() {
    return this.lastModified;
  }

  @Override
  public int getContentLength() {
    return this.contentLength;
  }

  @Override
  public InputStream getContentStream() throws IOException {
    return this.url.openStream();
  }
}
