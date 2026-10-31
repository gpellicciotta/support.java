// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.app;

/**
 *  Standard application (i.e. process) exit codes.
 */
public final class ExitCode
{
  /** Prevent instantiation */
  private ExitCode() { }

  /**  Normal exit: the requested processing has ended normally. */
  public static final int NORMAL = 0;

  /**  A configuration error has occurred preventing the requested processing to start. */
  public static final int CONFIG_ERROR = 1;

  /**  A processing error has occurred, leading to part of the requested processing to not finish. */
  public static final int PROCESSING_ERROR = 2;

  // More application-specific processing errors should get codes between 3 and 127

  /**  Abnormal exit due to an unexpected error/failure situation. */
  public static final int INTERNAL_ERROR = 127; // Max. positive number fitting in a byte.
}
