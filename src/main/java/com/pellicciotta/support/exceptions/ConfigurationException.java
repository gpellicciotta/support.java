// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.exceptions;

/**
 * An unchecked {@link RuntimeException} subclass for configuration issues.
 */
public class ConfigurationException extends RuntimeExceptionBase {
  // <editor-fold desc="Construction">

  /**
   *  Constructs a new runtime exception with the specified detail message.
   *  The cause is not initialized, and may subsequently be initialized by a call to {@link #initCause}.
   *
   *  @param configElement The name of the configuration element/property for which the error occurs.
   *  @param configValue The invalid (or absent if <code>null</code>) value that failed to lead to a valid configuration.
   *  @param messageFormat The error message ({@link String#format}-style format. The detail message, after applying any parameters, is saved for later retrieval by the {@link #getMessage()} method.
   *  @param messageParams Message format params.
   */
  public ConfigurationException(String configElement, Object configValue, String messageFormat, Object... messageParams) {
    super(((messageParams != null) && (messageParams.length > 0)) ? String.format(messageFormat, messageParams) : messageFormat);
    this.configElement = configElement;
    this.configValue = configValue;
  }

  /**
   *  Constructs a new runtime exception with the specified detail message and underlying cause.
   *
   *  @param configElement The name of the configuration element/property for which the error occurs.
   *  @param configValue The invalid (or absent if <code>null</code>) value that failed to lead to a valid configuration.
   *  @param cause The underlying cause (which is saved for later retrieval by the {@link #getCause()} method).
   *  @param messageFormat The error message ({@link String#format}-style format. The detail message, after applying any parameters, is saved for later retrieval by the {@link #getMessage()} method.
   *  @param messageParams Message format params.
   */
  public ConfigurationException(String configElement, Object configValue, Throwable cause, String messageFormat, Object... messageParams) {
    super(((messageParams != null) && (messageParams.length > 0)) ? String.format(messageFormat, messageParams) : messageFormat, cause);
    this.configElement = configElement;
    this.configValue = configValue;
  }

  /**
   *  Constructs a new runtime exception with the specified cause and a detail message of {@code (cause==null ? null : cause.toString())} (which typically contains the class and detail message of {@code cause}).
   *  This constructor is useful for runtime exceptions that are little more than wrappers for other throwables.
   *
   *  @param configElement The name of the configuration element/property for which the error occurs.
   *  @param configValue The invalid (or absent if <code>null</code>) value that failed to lead to a valid configuration.
   *  @param cause The underlying cause (which is saved for later retrieval by the {@link #getCause()} method).
   */
  public ConfigurationException(String configElement, Object configValue, Throwable cause) {
    super(cause);
    this.configElement = configElement;
    this.configValue = configValue;
  }

  // </editor-fold>
  // <editor-fold desc="Properties">

  private final String configElement;

  /**
   *  @return The name of the element whose configuration failed, leading to this exception.
   */
  public String getConfigurationElement() {
    return this.configElement;
  }

  private final Object configValue;

  /**
   *  @return The value that let to this exception. Can be null, signifying the absense of a specific erroneous value.
   */
  public Object getConfigurationValue() {
    return this.configValue;
  }

  // </editor-fold>
}
