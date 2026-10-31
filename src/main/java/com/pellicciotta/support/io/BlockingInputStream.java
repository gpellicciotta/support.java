// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.io;

import java.io.IOException;
import java.io.InputStream;

import com.pellicciotta.support.utils.Contract;

/**
 * An {@link InputStream} with read methods which will block until the number of requested bytes are actually read.
 */
public class BlockingInputStream extends InputStream {
  private InputStream decoratedStream;

  /**
   * Constructs a BlockingInputStream, which acts as a decorator on the {@code decoratedStream} parameter.
   *
   * @param decoratedStream the input stream to decorate
   * @throws IllegalArgumentException if decoratedStream is null
   */
  public BlockingInputStream(InputStream decoratedStream) throws IllegalArgumentException {
    Contract.isNotNull("decoratedStream", decoratedStream);
    this.decoratedStream = decoratedStream;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int available() throws IOException {
    return this.decoratedStream.available();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int read() throws IOException {
    return this.decoratedStream.read();
  }

  /**
   * Reads a number bytes from the input stream and stores them into the buffer array b.
   * <p>
   * This method blocks until the specified buffer is full, end of file is detected, or an exception is thrown.</p>
   *
   * @param b a buffer array
   * @return The number of bytes actually read.
   *
   * @throws IOException If reading fails due to an IO error.
   */
  @Override
  public int read(byte[] b) throws IOException {
    return this.read(b, 0, b.length);
  }

  /**
   * Reads len bytes of data from the input stream into an array of bytes. The number of bytes actually read is returned as an
   * integer.
   * <p>
   * This method blocks until len bytes are read, end of file is detected, or an exception is thrown.</p>
   *
   * @param b a buffer array.
   * @param off offset in the input stream.
   * @param len the number of bytes to read.
   * @return The number of bytes actually read.
   *
   * @throws IOException If reading fails due to an IO error.
   */
  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    int totalBytesRead = 0;
    while (totalBytesRead < len) {
      int bytesRead = this.decoratedStream.read(b, totalBytesRead, len - totalBytesRead);
      if (bytesRead == -1) {
        return -1;
      }
      else {
        totalBytesRead += bytesRead;
      }
    }
    return totalBytesRead;
  }
}
