// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.pellicciotta.support.text.TextBuilder;

/**
 * A collection of Exception related utility methods.
 */
public final class ExceptionUtils
{
  /** Prevent instantiation */
  private ExceptionUtils() {}

  /**
   * Same as {@code formatExceptionWithStackTrace(exception, true, true, Integer.MAX_VALUE)}.
   *
   * @param exception The throwable whose stack trace should be formatted.
   *
   * @return See {@link #formatExceptionWithStackTrace(Throwable, boolean, boolean, int)}.
   */
  public static String formatExceptionWithStackTrace(Throwable exception) {
    return formatExceptionWithStackTrace(exception, true, true, Integer.MAX_VALUE);
  }

  /**
   * Same as {@code formatExceptionWithStackTrace(exception, includeCauses, includeCauses, Integer.MAX_VALUE)}.
   *
   * @param exception The throwable whose stack trace should be formatted.
   * @param includeCauses Whether any cause of the throwable (and recursively its own cause) should be added to the returned string representation too.
   *
   * @return See {@link #formatExceptionWithStackTrace(Throwable, boolean, boolean, int)}.
   */
  public static String formatExceptionWithStackTrace(Throwable exception, boolean includeCauses) {
    return formatExceptionWithStackTrace(exception, includeCauses, includeCauses, Integer.MAX_VALUE);
  }

  /**
   * Same as {@code formatExceptionWithStackTrace(exception, includeCauses, includeCauses, maxFramesPerException)}.
   *
   * @param exception The throwable whose stack trace should be formatted.
   * @param includeCauses Whether any cause of the throwable (and recursively its own cause) should be added to the returned string representation too.
   * @param maxFramesPerException The max. number of stack frames to include, per exception.
   *
   * @return See {@link #formatExceptionWithStackTrace(Throwable, boolean, boolean, int)}.
   */
  public static String formatExceptionWithStackTrace(Throwable exception, boolean includeCauses, int maxFramesPerException) {
    return formatExceptionWithStackTrace(exception, includeCauses, includeCauses, maxFramesPerException);
  }

  /**
   * Return a string representation of the throwable's stacktrace.
   * <p>
   * The returned format is similar to, but not entirely the same as that returned from {@link Throwable#printStackTrace(PrintWriter)}: it doesn't
   * list suppressed exceptions nor does it omit common stacktrace parts.
   *
   * @param exception The throwable whose stack trace should be formatted.
   * @param includeCauses Whether any cause of the throwable (and recursively its own cause) should be added to the returned string representation too.
   * @param includeSuppressions Whether any suppressed throwables (recursively, also of the causes) should be added to the returned string representation too.
   * @param maxFramesPerException The max. number of stack frames to include, per exception.
   *
   * @return A string representation of the specified exception with its stacktrace formatted as:
   *         <pre>
   *           Exception: {exception type} {exception message}
   *             at {stack frame location}                                               repeated as many times as needed
   *           Caused by: {cause exception type} {cause exception message}               repeated as many times as there are underlying causes
   *             at {stack frame location}                                               repeated as many times as needed
   *             Suppressing: {suppressed exception type} {suppressed exception message} repeated as many times as there are underlying suppressed exceptions
   *               at {stack frame location}                                             repeated as many times as needed
   *           Suppressing: {suppressed exception type} {suppressed exception message}   repeated as many times as there are underlying suppressed exceptions
   *             at {stack frame location}                                               repeated as many times as needed
   *             Caused by: {cause exception type} {cause exception message}             repeated as many times as there are underlying causes
   *               at {stack frame location}                                             repeated as many times as needed
   *         </pre>
   */
  public static String formatExceptionWithStackTrace(Throwable exception, boolean includeCauses, boolean includeSuppressions, int maxFramesPerException) {
    Contract.isNotNull(exception);
    TextBuilder tb = new TextBuilder();
    addClassAndMessage(exception, tb, "Exception: ");
    addStackFrames(exception, tb, maxFramesPerException);
    if (includeCauses) {
      addCauses(exception, tb, maxFramesPerException, includeSuppressions);
    }
    if (includeSuppressions) {
      addSuppressions(exception, tb, maxFramesPerException, includeCauses);
    }
    return tb.toString();
  }

  private static void addClassAndMessage(Throwable exception, TextBuilder tb, String prefix) {
    tb.append(prefix);
    tb.append(exception.getClass().getName());
    String msg = exception.getMessage();
    if ((msg != null) && !msg.isEmpty()) {
      tb.append(" ");
      tb.append(msg);
    }
  }

  private static void addStackFrames(Throwable exception, TextBuilder tb, int maxFramesPerException) {
    if (maxFramesPerException > 0) {
      StackTraceElement[] st = exception.getStackTrace();
      for (int i = 0; (i < maxFramesPerException) && (i < st.length); i++) {
        tb.appendLine();
        tb.append("  at: %s", st[i]);
      }
    }
  }

  private static void addCauses(Throwable exception, TextBuilder tb, int maxFramesPerException, boolean includeSuppressions) {
    for (Throwable causingException = exception.getCause(); causingException != null; causingException = causingException.getCause()) {
      tb.appendLine();
      addClassAndMessage(causingException, tb, "Caused by: ");
      addStackFrames(causingException, tb, maxFramesPerException);
      addSuppressions(causingException, tb, maxFramesPerException, true);
    }
  }

  private static void addSuppressions(Throwable exception, TextBuilder tb, int maxFramesPerException, boolean includeCauses) {
    for (Throwable suppressedException : exception.getSuppressed()) {
      tb.appendLine();
      addClassAndMessage(suppressedException, tb, "Suppressing: ");
      addStackFrames(suppressedException, tb, maxFramesPerException);
      if (includeCauses) {
        addCauses(suppressedException, tb, maxFramesPerException, true);
      }
    }
  }

  /**
   * Return the internal causes of the throwable: that is, the list of {@link Throwable}s that have been thrown, resulting in the specified
   * throwable to be thrown.
   *
   * @param throwable The {@link Throwable} whose internal causes should be returned.
   *
   * @return Array of {@link Throwable}s. Will be empty if throwable has no internal causes.
   */
  public static Throwable[] getCauses(Throwable throwable) {
    ArrayList<Throwable> causes = new ArrayList<>();
    for (Throwable tw = throwable.getCause(); tw != null; tw = tw.getCause()) {
      causes.add(tw);
    }
    return causes.toArray(new Throwable[] {});
  }

  /**
   * Return a string representation of the specified stack frames.
   *
   * @param stackFrames Stack frames to be represented.
   *
   * @return A string representation of the specified stack frames. Every stack frame will be converted into one line.
   */
  public static String formatStackTrace(StackTraceElement[] stackFrames) {
    return formatStackTrace(stackFrames, "");
  }

  /**
   * Return a string representation of the specified stack frames.
   *
   * @param stackFrames Stack frames to be represented.
   * @param linePrefix The prefix to use for each new line holding a stack frame element representation.
   *
   * @return A string representation of the specified stack frame elements, each on a separate line.
   */
  public static String formatStackTrace(StackTraceElement[] stackFrames, String linePrefix) {
    Contract.isNotNull(stackFrames);
    Contract.isNotNull(linePrefix);
    TextBuilder stack = new TextBuilder();
    for (int i = 0; i < stackFrames.length; i++) {
      stack.append(linePrefix);
      stack.append(stackFrames[i].toString());
      if ((i + 1) < stackFrames.length) {
        stack.appendLine();
      }
    }
    return stack.toString();
  }

  /**
   * Return the current stack trace, not including the call to this method.
   *
   * @return The current stack trace, represented as an array of stack trace elements. The first stack\ trace element represents the
   *         method that contains the call to this method.
   */
  public static StackTraceElement[] getCurrentStackTrace() {
    return getStackTrace(1);
  }

  /**
   * Return a stack trace that is derived from the current stack trace, but with the skipCount + 1 topmost elements discarded. If
   * skipCount is 0, the stack trace representing the call to this method is represented.
   *
   * @param skipCount The number of topmost stack trace elements to be skipped in the current stack trace.
   * @return An adapted stack trace.
   *
   * @throws IllegalArgumentException When the requested stack trace cannot be built.
   */
  public static StackTraceElement[] getStackTrace(int skipCount) throws IllegalArgumentException {
    Throwable t = new Throwable();
    StackTraceElement[] actualStackTrace = t.getStackTrace();
    Contract.isInRange(skipCount, 0, actualStackTrace.length - 1);
    StackTraceElement[] returnStackTrace = new StackTraceElement[actualStackTrace.length - skipCount - 1];
    for (int i = skipCount + 1, j = 0; i < actualStackTrace.length; i++, j++) {
      returnStackTrace[j] = actualStackTrace[i];
    }
    return returnStackTrace;
  }

  /**
   * Return a stack trace that is derived from the current stack trace but with the topmost n elements skipped if they represent
   * locations (&lt;packageName&gt;.&lt;className&gt;.&lt;methodName&gt;) that should be skipped because a prefix of the location matches any of the
   * skipPrefixes. If skipPrefixes is empty, the stack trace representing the call to this method is represented.
   *
   * @param skipPrefixes List of &lt;packageName&gt;.&lt;className&gt;.&lt;methodName&gt; prefixes that, when found in a topmost stackTraceElement,
   *          will lead to this element being skipped.
   * @return An adapted stack trace.
   *
   * @throws IllegalArgumentException When the requested stack trace cannot be built.
   */
  public static StackTraceElement[] getStackTrace(String... skipPrefixes) throws IllegalArgumentException {
    Throwable t = new Throwable();
    t.fillInStackTrace();
    StackTraceElement[] stackTrace = t.getStackTrace();
    int i = 1; // Always skip last StackTracElement representing this method call
    // Skip topmost elements that match any of the skipPrefixes:
    skipLoop: for (; i < stackTrace.length; i++) {
      String location = stackTrace[i].getClassName() + "." + stackTrace[i].getMethodName();
      for (String skipPrefix : skipPrefixes) {
        if (location.startsWith(skipPrefix)) {
          continue skipLoop;
        }
      }
      break skipLoop;
    }
    // Copy over the rest:
    List<StackTraceElement> returnStackTrace = new ArrayList<>();
    for (; i < stackTrace.length; i++) {
      returnStackTrace.add(stackTrace[i]);
    }
    return returnStackTrace.toArray(new StackTraceElement[0]);
  }
}
