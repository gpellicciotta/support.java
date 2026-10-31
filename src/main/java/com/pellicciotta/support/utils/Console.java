// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

/**
 *  Standard output/error related utilities.
 */
public abstract class Console {

  /** Private to make this a static class. */
  private Console() { }

  /**
   *  Print a formatted message on stdout, via {@link System#out#println}.
   *  If the last message parameter is a {@link Throwable}, also print its full stacktrace.
   *
   *  @param messageOrFormat Message format.
   *  @param messageParams Message format arguments.
   */
  static void stdout(String messageOrFormat, Object... messageParams) {
    System.out.println(StringUtils.str(messageOrFormat, messageParams));
    if ((messageOrFormat != null) && (messageParams.length > 0) && (messageParams[messageParams.length - 1] instanceof Throwable)) { // Last arg is exception: show stack trace
      ((Throwable)messageParams[messageParams.length - 1]).printStackTrace(System.out);
    }
  }

  /**
   *  Print a formatted message on stderr, via {@link System#err#println}.
   *  If the last message parameter is a {@link Throwable}, also print its full stacktrace.
   *
   *  @param messageOrFormat Message format.
   *  @param messageParams Message format arguments.
   */
  static void stderr(String messageOrFormat, Object... messageParams) {
    System.err.println(StringUtils.str(messageOrFormat, messageParams));
    if ((messageOrFormat != null) && (messageParams.length > 0) && (messageParams[messageParams.length - 1] instanceof Throwable)) { // Last arg is exception: show stack trace
      ((Throwable)messageParams[messageParams.length - 1]).printStackTrace(System.err);
    }
  }

  /**
   *  Print a stacktrace on stderr, via {@link System#err#println}.
   */
  static void stacktrace() {
    StackTraceElement[] trace = ExceptionUtils.getStackTrace(1);
    StringBuilder sb = new StringBuilder();
    for (StackTraceElement traceElement : trace) {
      stderr("\tat %s", traceElement);
    }
  }

  /**
   *  Print a stacktrace on stderr, via {@link System#err#println}, after printing a normal message.
   *
   *  @param messageOrFormat Message format of the message to print first.
   *  @param messageParams Message format arguments.
   */
  static void stacktrace(String messageOrFormat, Object... messageParams) {
    StackTraceElement[] trace = ExceptionUtils.getStackTrace(1);
    StringBuilder sb = new StringBuilder();
    stderr(messageOrFormat, messageParams);
    for (StackTraceElement traceElement : trace) {
      stderr("\tat %s", traceElement);
    }
  }
}
