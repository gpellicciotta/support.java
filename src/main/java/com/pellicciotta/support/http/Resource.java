// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

import java.io.IOException;
import java.io.InputStream;

interface Resource
{
  /**
   * @return The mime-type for the resource.
   */
  String getMimeType();

  /**
   * @return The last-modified time of the resource, in millis since epoch. Will be <code>-1</code> if unknown.
   */
  long getLastModified();

  /**
   * @return The content-length in bytes, or <code>-1</code> if unknown.
   */
  int getContentLength();

  /**
   * @return The stream of bytes with the content of the resource.
   */
  InputStream getContentStream() throws IOException;
}
