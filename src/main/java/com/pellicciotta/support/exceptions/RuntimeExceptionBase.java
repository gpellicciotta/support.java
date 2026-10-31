// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.exceptions;

/**
 *  Base class providing the generally needed constructors that allow for {@link String#format}-style messages with parameters.
 */
public abstract class RuntimeExceptionBase extends RuntimeException {
  /**
   *  Constructs a new runtime exception with the specified detail message.
   *  The cause is not initialized, and may subsequently be initialized by a call to {@link #initCause}.
   *
   *  @param messageFormat The error message ({@link String#format}-style format. The detail message, after applying any parameters, is saved for later retrieval by the {@link #getMessage()} method.
   *  @param messageParams Message format params.
   */
  public RuntimeExceptionBase(String messageFormat, Object... messageParams) {
    super(((messageParams != null) && (messageParams.length > 0)) ? String.format(messageFormat, messageParams) : messageFormat);
  }

  /**
   *  Constructs a new runtime exception with the specified detail message and underlying cause.
   *
   *  @param cause The underlying cause (which is saved for later retrieval by the {@link #getCause()} method).
   *  @param messageFormat The error message ({@link String#format}-style format. The detail message, after applying any parameters, is saved for later retrieval by the {@link #getMessage()} method.
   *  @param messageParams Message format params.
   */
  public RuntimeExceptionBase(Throwable cause, String messageFormat, Object... messageParams) {
    super(((messageParams != null) && (messageParams.length > 0)) ? String.format(messageFormat, messageParams) : messageFormat, cause);
  }

  /**
   *  Constructs a new runtime exception with the specified cause and a detail message of {@code (cause==null ? null : cause.toString())} (which typically contains the class and detail message of {@code cause}).
   *  This constructor is useful for runtime exceptions that are little more than wrappers for other throwables.
   *
   *  @param cause The underlying cause (which is saved for later retrieval by the {@link #getCause()} method).
   */
  public RuntimeExceptionBase(Throwable cause) {
    super(cause);
  }
}
