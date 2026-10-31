package com.pellicciotta.support.cache;

import com.pellicciotta.support.exceptions.RuntimeExceptionBase;

/**
 *  Exception thrown when a pool operation fails.
 */
public class PoolException extends RuntimeExceptionBase
{
  // <editor-fold desc="Construction">

  /**
   *  Constructs a new runtime exception with the specified detail message.
   *  The cause is not initialized, and may subsequently be initialized by a call to {@link #initCause}.
   *
   *  @param messageFormat The error message ({@link String#format}-style format. The detail message, after applying any parameters, is saved for later retrieval by the {@link #getMessage()} method.
   *  @param messageParams Message format params.
   */
  public PoolException(String messageFormat, Object... messageParams) {
    super(messageFormat, messageParams);
  }

  /**
   *  Constructs a new runtime exception with the specified detail message and underlying cause
   *
   *  @param cause The underlying cause (which is saved for later retrieval by the {@link #getCause()} method).
   *  @param messageFormat The error message ({@link String#format}-style format. The detail message, after applying any parameters, is saved for later retrieval by the {@link #getMessage()} method.
   *  @param messageParams Message format params.
   */
  public PoolException(Throwable cause, String messageFormat, Object... messageParams) {
    super(cause, messageFormat, messageParams);
  }

  /**
   *  Constructs a new runtime exception with the specified cause and a  detail message of {@code (cause==null ? null : cause.toString())}
   *  (which typically contains the class and detail message of {@code cause}).
   *  This constructor is useful for runtime exceptions that are little more than wrappers for other throwables.
   *
   *  @param cause The underlying cause (which is saved for later retrieval by the {@link #getCause()} method).
   */
  public PoolException(Throwable cause) {
    super(cause);
  }

  // </editor-fold>
}
