// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.exceptions;

import com.pellicciotta.support.utils.CollectionUtils;

/**
 * Base class for all exceptions thrown when a value or file data doesn't have the expected format.
 */
public class FormatException extends RuntimeExceptionBase
{
  // <editor-fold desc="Construction">

  /**
   *  Constructs a new runtime exception with the specified detail message.
   *  The cause is not initialized, and may subsequently be initialized by a call to {@link #initCause}.
   *
   *  @param message The error message ({@link String#format}-style format.
   */
  public FormatException(String message) {
    super(message);
    this.value = null;
  }

  /**
   *  Constructs a new runtime exception with the specified detail message.
   *  The cause is not initialized, and may subsequently be initialized by a call to {@link #initCause}.
   *
   *  @param messageFormat The error message ({@link String#format}-style format. The detail message, after applying any parameters, is saved for later retrieval by the {@link #getMessage()} method.
   *  @param messageParams Message format params.
   */
  public FormatException(String messageFormat, String messageParam, Object... messageParams) {
    super(messageFormat, CollectionUtils.asArray(messageParam, messageParams));
    this.value = null;
  }

  /**
   *  Constructs a new runtime exception with the specified detail message.
   *  The cause is not initialized, and may subsequently be initialized by a call to {@link #initCause}.
   *
   *  @param messageFormat The error message ({@link String#format}-style format. The detail message, after applying any parameters, is saved for later retrieval by the {@link #getMessage()} method.
   *  @param messageParams Message format params.
   */
  public FormatException(String messageFormat, Object... messageParams) {
    super(messageFormat, messageParams);
    this.value = null;
  }

  /**
   *  Constructs a new runtime exception with the specified detail message.
   *  The cause is not initialized, and may subsequently be initialized by a call to {@link #initCause}.
   *
   *  @param value Value that didn't conform to the format.
   *  @param messageFormat The error message ({@link String#format}-style format. The detail message, after applying any parameters, is saved for later retrieval by the {@link #getMessage()} method.
   *  @param messageParams Message format params.
   */
  public FormatException(Object value, String messageFormat, Object... messageParams) {
    super(((messageParams != null) && (messageParams.length > 0)) ? String.format(messageFormat, messageParams) : messageFormat);
    this.value = value;
  }

  /**
   *  Constructs a new runtime exception with the specified detail message and underlying cause.
   *
   *  @param value Value that didn't conform to the format.
   *  @param cause The underlying cause (which is saved for later retrieval by the {@link #getCause()} method).
   *  @param messageFormat The error message ({@link String#format}-style format. The detail message, after applying any parameters, is saved for later retrieval by the {@link #getMessage()} method.
   *  @param messageParams Message format params.
   */
  public FormatException(Object value, Throwable cause, String messageFormat, Object... messageParams) {
    super(cause, ((messageParams != null) && (messageParams.length > 0)) ? String.format(messageFormat, messageParams) : messageFormat);
    this.value = value;
  }

  /**
   *  Constructs a new runtime exception with the specified cause and a detail message of {@code (cause==null ? null : cause.toString())} (which typically contains the class and detail message of {@code cause}).
   *  This constructor is useful for runtime exceptions that are little more than wrappers for other throwables.
   *
   *  @param value Value that didn't conform to the format.
   *  @param cause The underlying cause (which is saved for later retrieval by the {@link #getCause()} method).
   */
  public FormatException(Object value, Throwable cause) {
    super(cause);
    this.value = value;
  }

  // </editor-fold>
  // <editor-fold desc="Properties">

  protected Object value;

  /**
   * @return The value that didn't comply to the format.
   */
  public Object getValue() {
    return this.value;
  }

  // </editor-fold>
}
