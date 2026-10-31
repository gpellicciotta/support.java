// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import com.pellicciotta.support.logging.Log;
import com.pellicciotta.support.logging.LogService;

import java.io.*;

/**
 *  A collection of stream-related utility methods.
 */
public final class StreamUtils {
  /** Prevent instantiation */
  private StreamUtils() { }

  // <editor-fold desc="Constants">

  private static final Log log = LogService.fromCurrentClass();

  // The default buffer size to use.
  private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

  // </editor-fold>
  // <editor-fold desc="Operations">

  /**
   * Reads buffer.length bytes from the input stream into the byte array.
   * This method reads repeatedly from the stream until the requested number of bytes are read.
   * This method blocks until the requested number of bytes are read, the end of the stream is detected, or an exception is thrown.
   *
   * @param input The input stream to read from.
   * @param buffer The buffer into which the data is read.
   * @return The number of bytes actually read. This should always be buffer.length except if the end of stream was reached.
   *
   * @exception IOException If an I/O error occurs.
   */
  public static int readFully(InputStream input, byte[] buffer) throws IOException {
    return readFully(input, buffer, 0, buffer.length);
  }

  /**
   * Reads length bytes from the input stream into the buffer.
   * This method reads repeatedly from the stream until the requested number of bytes are read.
   * This method blocks until the requested number of bytes are read, the end of the stream is detected, or an exception is thrown.
   *
   * @param input The input stream to read from.
   * @param buffer The buffer into which the data is read.
   * @param offset The offset in buffer into which to read the first byte from input.
   * @param length The number of bytes from input to read into the buffer.
   *
   * @return The number of bytes actually read. This should always be length except if the end of stream was reached.
   *
   * @exception IOException If an I/O error occurs.
   */
  public static int readFully(InputStream input, byte[] buffer, int offset, int length) throws IOException {
    Contract.isNotNull("input", input);
    Contract.isNotNull("buffer", buffer);
    Contract.isInRange("offset", offset, 0, buffer.length - 1);
    Contract.isInRange("length", length, 0, buffer.length - offset);

    if (buffer.length == 0) {
      return 0;
    }
    int read = 0;
    do {
      int count = input.read(buffer, offset + read, length - read);
      if (count < 0) {
        return read;
      }
      read += count;
    } while (read < length);
    return read;
  }

  /**
   *  Copy an entire input stream to an output stream.
   *  Leave the streams open.
   *
   *  @param input the input stream to be copied.
   *  @param output the output stream to be written to.
   *
   *  @return the number of bytes that have been copied.
   *
   *  @throws IllegalArgumentException if input or output is null.
   *  @throws IOException when any IO error occurs.
   */
  public static long copy(InputStream input, OutputStream output) throws IOException, IllegalArgumentException {
    return copy(input, output, false);
  }

  /**
   *  Copy an entire input stream to an output stream.
   *
   *  @param input the input stream to be copied.
   *  @param output the output stream to be written to.
   *  @param closeStreams Whether to close both input and output streams after all bytes have been copied
   *                      or also when an exception gets thrown.
   *
   *  @return the number of bytes that have been copied.
   *
   *  @throws IllegalArgumentException if input or output is null.
   *  @throws IOException when any IO error occurs.
   */
  public static long copy(InputStream input, OutputStream output, boolean closeStreams) throws IOException, IllegalArgumentException {
    Contract.isNotNull("input", input);
    Contract.isNotNull("output", output);
    try {
      byte[] buffer = new byte[StreamUtils.DEFAULT_BUFFER_SIZE];
      long count = 0;
      for (int n = input.read(buffer); n != -1; n = input.read(buffer)) {
        output.write(buffer, 0, n);
        count += n;
      }
      return count;
    }
    finally {
      closeSilently(input);
      closeSilently(output);
    }
  }

  /**
   *  Read all bytes from an input stream and returns them as an array.
   *
   *  @param in The input stream.
   *  @return The array of bytes just read.
   *
   *  @throws IOException when an IOException occurs while reading from the stream.
   *  @throws IllegalArgumentException If in is null.
   */
  public static byte[] readBinaryStream(InputStream in) throws IOException, IllegalArgumentException {
    Contract.isNotNull("in", in);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    if (in != null) {
      byte[] buf = new byte[1024];
      int amount = 0;
      while ((amount = in.read(buf)) != -1) {
        bos.write(buf, 0, amount);
      }
    }
    return bos.toByteArray();
  }

  /**
   *  Write all bytes from a byte array to an output stream.
   *
   *  @param os The output stream to write to.
   *  @param bytes The bytes to be written out.
   *
   *  @return The number of bytes that have been written.
   *
   *  @throws IOException When an IOException occurs while writing to the stream.
   *  @throws IllegalArgumentException If os is null.
   */
  public static long writeBinaryStream(OutputStream os, byte[] bytes) throws IOException, IllegalArgumentException {
    Contract.isNotNull("os", os);
    ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
    return StreamUtils.copy(bis, os);
  }

  /**
   *  Read everything there is to read from the reader.
   *
   *  @param reader The reader to read from until it has nothing more to give.
   *  @return A single string representing everything that was left to read. Might be empty but never null.
   *
   *  @throws IOException When an IOException occurs while writing to the stream.
   *  @throws IllegalArgumentException If reader is null.
   */
  public static String readAll(Reader reader) throws IOException {
    Contract.isNotNull("reader", reader);
    StringBuilder sb = new StringBuilder(1024);
    readAllInto(reader, sb);
    return sb.toString();
  }

  /**
   *  Read everything there is to read from the reader.
   *
   *  @param reader The reader to read from until it has nothing more to give.
   *  @param sb The buffer to read into.
   *
   *  @throws IOException When an IOException occurs while writing to the stream.
   *  @throws IllegalArgumentException If reader or sb is null.
   */
  public static void readAllInto(Reader reader, StringBuilder sb) throws IOException {
    Contract.isNotNull("reader", reader);
    Contract.isNotNull("sb", sb);
    char[] chars = new char[1024];
    int numRead = 0;
    while ((numRead = reader.read(chars)) > -1) {
      sb.append(String.valueOf(chars, 0, numRead));
    }
    reader.close();
  }

  /**
   *  Closes an {@code InputStream}, ignoring any errors.
   *
   *  @param is the InputStream, which may be null.
   */
  public static void closeSilently(InputStream is) {
    try {
      if (is != null) {
        is.close();
      }
    }
    catch (IOException ex) { // silently ignore
      log.warning("Closing input stream failed silently: %s", ex);
    }
  }

  /**
   *  Close an {@code OutputStream}, ignoring any errors.
   *
   *  @param os the OutputStream, which may be null.
   */
  public static void closeSilently(OutputStream os) {
    try {
      if (os != null) {
        os.close();
      }
    }
    catch (IOException ex) { // silently ignore
      log.warning("Closing output stream failed silently: %s", ex);
    }
  }

  /**
   *  Close a {@code Reader}, ignoring any errors.
   *
   *  @param reader the Reader, which may be null.
   */
  public static void closeSilently(Reader reader) {
    try {
      if (reader != null) {
        reader.close();
      }
    }
    catch (IOException ex) { // silently ignore
      log.warning("Closing reader failed silently: %s", ex);
    }
  }

  /**
   *  Close a {@code Writer}, ignoring any errors.
   *
   *  @param writer the Writer, which may be null
   */
  public static void closeSilently(Writer writer) {
    try {
      if (writer != null) {
        writer.close();
      }
    }
    catch (IOException ex) { // silently ignore
      log.warning("Closing writer failed silently: %s", ex);
    }
  }

  // </editor-fold>
}
