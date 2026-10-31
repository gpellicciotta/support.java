// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging;

import com.pellicciotta.support.logging.spi.LogBackEnd;
import com.pellicciotta.support.logging.spi.LogBase;
import com.pellicciotta.support.logging.spi.LoggerConfig;
import com.pellicciotta.support.logging.spi.LoggingConfig;
import com.pellicciotta.support.logging.stdout.StdoutBasedLogBackEnd;

import java.lang.reflect.Constructor;

/**
 *  Factory and repository of {@link Log} objects.
 *  <p>
 *  This {@link LogService} should be configured once at the very start of an application, and henceforth only used
 *  to obtain {@link Log} objects.</p>
 */
public final class LogService {
  // <editor-fold desc="Configuration">

  /** Prevent instantiation */
  private LogService() { }

  private static final LogBackEnd builtinDefaultLogBackEnd = new StdoutBasedLogBackEnd();

  private static LogBackEnd logBackEnd = builtinDefaultLogBackEnd;
  private static Log defaultLog = builtinDefaultLogBackEnd.getLog(null);

  /**
   *  @return The currently configured {@link LogBackEnd}.
   */
  public static synchronized LogBackEnd getBackEnd() {
    return logBackEnd;
  }

  /**
   *  Configure a new {@link Log} factory.
   *  <p>
   *  In general, this method should only be used once at the start of a process and never be invoked
   *  from 'normal' code.</p>
   *  <p>
   *  <b>NOTE</b> Any {@link Log} instances created by the old (current) back-end, will remain attached to that previous
   *  back-end.</p>
   *
   *  @param backEnd The code that will be responsible from now on to retrieve/create {@link Log} instances
   *                 for specific names. If <code>null</code> the default configuration will be restored.
   *                 It is assumed that this back-end is already fully configured.
   */
  public static synchronized void setBackEnd(LogBackEnd backEnd) {
    if (backEnd != null) {
      logBackEnd = backEnd;
      defaultLog = backEnd.getLog(null);
    }
    else {
      logBackEnd = builtinDefaultLogBackEnd;
      defaultLog = builtinDefaultLogBackEnd.getLog(null);
    }
  }

  /**
   *  Configure a back-end and then invoke {@link #setBackEnd(LogBackEnd)} with the newly constructed
   *  and configured {@link LogBackEnd}.
   *  <b>NOTE</b> Any {@link Log} instances created by the old (current) back-end, will remain attached to that previous
   *  back-end.</p>
   *
   *  @param config The configuration to be used and applied.
   */
  public static synchronized void configure(LoggingConfig config) throws Exception {
    Class<? extends LogBackEnd> backEndClass = config.getBackEndType();
    Constructor<? extends LogBackEnd> backEndCtor = backEndClass.getConstructor();
    LogBackEnd backEnd = backEndCtor.newInstance();
    backEnd.configure(config);
    setBackEnd(backEnd);
  }

  // </editor-fold>
  // <editor-fold desc="Log Factory">

  /**
   * Create a new {@link Log} named after the currently invoking class.
   *
   * @return A {@link Log} provided by the currently configured log factory for the name that is equal to the
   *         fully qualified class name of the invoking class.
   */
  public static synchronized Log fromCurrentClass() {
    Throwable t = new Throwable();
    StackTraceElement callerStackTraceElement = t.getStackTrace()[1];
    return fromName(callerStackTraceElement.getClassName());
  }

  /**
   * Create a new {@link Log} named after the provided class.
   *
   * @param clazz The class whose fully qualified name to use.
   * @return A {@link Log} provided by the currently configured log factory for the name that is equal to the
   *         fully qualified class name of the provided class. If this name is <code>null</code> or <code>clazz</code>
   *         itself was <code>null</code>, it is up to the configured factory to decide what to return, but typically
   *         it will return a {@link Log} representing the <em>root logger</em>.
   */
  public static synchronized Log fromClass(Class<?> clazz) {
    return fromName(clazz.getCanonicalName());
  }

  /**
   * Create a new {@link Log} for the given name.
   *
   * @param logName The log name to use.
   * @return A {@link Log} provided by the currently configured log factory for the provided name.
   *         If this name is <code>null</code>, it is up to the configured factory to decide what to return, but typically
   *         it will return a {@link Log} representing the <em>root logger</em>.
   */
  public static synchronized Log fromName(String logName) {
    return logBackEnd.getLog(logName);
  }

  // </editor-fold>
  // <editor-fold desc="Default Log Methods">

  /**
   *  Start a span for the default log.
   *  <p>
   *  A span typically represents everything that happens during a certain timespan that is needed
   *  to complete some logical operation (E.g. handle an HTTP request).</p>
   *  <p>
   *  A span can add context-data to all log messages that are emitted while it is effective.
   *  A span also has an associated timer, so that when the span gets closed, the total elapsed time is known.</p>
   *
   *  @param spanName The name to give to this span. This should typically be the name of an operation.
   *
   *  @return A {@link Span} object that can be used to associated context-data with all subsequent log messages,
   *          and with the span itself.
   */
  public static synchronized Span start(String spanName) {
    return defaultLog.start(spanName);
  }

  /**
   *  Close the default log.
   */
  public static synchronized void close() {
    defaultLog.close();
  }

  /**
   * @return Is the {@link LogLevel#ERROR} level currently enabled for the default log?
   */
  public static synchronized boolean isErrorEnabled() {
    return defaultLog.isErrorEnabled();
  }

  /**
   * Log a message at the {@link LogLevel#ERROR} level.
   *
   * @param format The message format.
   * @param args The message parameters.
   */
  public static synchronized void error(String format, Object... args) {
    defaultLog.error(format, args);
  }

  /**
   * @return Is the {@link LogLevel#WARNING} level currently enabled for the default log?
   */
  public static synchronized boolean isWarningEnabled() {
    return defaultLog.isWarningEnabled();
  }

  /**
   * Log a message at the {@link LogLevel#WARNING} level.
   *
   * @param format The message format.
   * @param args The message parameters.
   */
  public static synchronized void warning(String format, Object... args) {
    defaultLog.warning(format, args);
  }

  /**
   * @return Is the {@link LogLevel#INFO} level currently enabled for the default log?
   */
  public static synchronized boolean isInfoEnabled() {
    return defaultLog.isInfoEnabled();
  }

  /**
   * Log a message at the {@link LogLevel#INFO} level.
   *
   * @param format The message format.
   * @param args The message parameters.
   */
  public static synchronized void info(String format, Object... args) {
    defaultLog.info(format, args);
  }

  /**
   * @return Is the {@link LogLevel#DEBUG} level currently enabled for the default log?
   */
  public static synchronized boolean isDebugEnabled() {
    return defaultLog.isDebugEnabled();
  }

  /**
   * Log a message at the {@link LogLevel#DEBUG} level.
   *
   * @param format The message format.
   * @param args The message parameters.
   */
  public static synchronized void debug(String format, Object... args) {
    defaultLog.debug(format, args);
  }

  // </editor-fold>
}
