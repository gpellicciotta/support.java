// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging.jul;

import java.time.Instant;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;

import com.pellicciotta.support.logging.*;
import com.pellicciotta.support.logging.spi.*;
import com.pellicciotta.support.utils.Contract;
import com.pellicciotta.support.utils.StringUtils;

/**
 *  JUL utilities.
 */
public final class JULUtils
{
  /** Prevent instantiation */
  private JULUtils() {}

  /**
   *  Convert an integer as defined by {@link LogLevel} to the nearest {@link java.util.logging.Level} constant.
   *
   *  All int values equal or above <code>Level.SEVERE.intValue()</code> will be mapped to <code>Level.SEVERE</code>,
   *  all equal or above <code>Level.WARNING.intValue()</code> but bigger than <code>Level.INFO.intValue()</code> to <code>Level.WARNING</code>, etc.
   *  all equal or above <code>Level.INFO.intValue()</code> but bigger than <code>Level.FINE.intValue()</code> to <code>Level.FINE</code>, etc.
   *  while all smaller than <code>Level.FINEST.intValue()</code> to <code>Level.FINEST</code>.
   *
   *  @param level The integer level to get mapped to a {@link java.util.logging.Level} constant.
   *
   *  @return The {@link java.util.logging.Level} that is mapped from the provided level.
   */
  public static Level toNearestLevel(int level) {
    // NOTE: the inconsistency in mapping to FINER and FINEST while not mapping to CONFIG is meant
    if (level == LogLevel.NONE) {
      return Level.OFF;
    } else if (level >= LogLevel.ERROR) {
      return Level.SEVERE;
    } else if (level >= LogLevel.WARNING) {
      return Level.WARNING;
    } else if (level >= LogLevel.INFO) {
      return Level.INFO;
    } else if (level >= LogLevel.DEBUG) {
      return Level.FINE;
    } else {
      return Level.FINEST;
    }
  }

  /**
   *  From {@link Level} to {@link LogLevel}.
   *
   *  @param level The {@link Level} to convert.
   *  @return The corresponding {@link LogLevel}.
   */
  public static int toLogLevel(Level level) {
    int intLevel = level.intValue();
    if (intLevel >= Level.SEVERE.intValue()) {
      return LogLevel.ERROR;
    }
    else if (intLevel >= Level.WARNING.intValue()) {
      return LogLevel.WARNING;
    }
    else if (intLevel >= Level.CONFIG.intValue()) {
      return LogLevel.INFO;
    }
    else {
      return LogLevel.DEBUG;
    }
  }

  /**
   *  From {@link LogRecord} to {@link LogEvent}.
   *
   *  @param logRecord The {@link LogRecord} to convert.
   *  @return The corresponding {@link LogEvent}.
   */
  public static LogEvent toLogEvent(LogRecord logRecord) {
    String logName = logRecord.getLoggerName();
    int logLevel = toLogLevel(logRecord.getLevel());
    String logMessage = logRecord.getMessage();
    Object[] msgParams = logRecord.getParameters();
    if (msgParams == null) {
      msgParams = new Object[0];
    }
    Throwable cause = logRecord.getThrown();
    Instant logTime = logRecord.getInstant();
    long threadId = logRecord.getThreadID();
    List<ContextDataItem> ctxVals = new ArrayList<>();
    String className = logRecord.getSourceClassName();
    if (className != null) {
      ctxVals.add(new ContextDataItemBase(LogBase.ORIGIN_CLASS, className, LogService.getBackEnd()));
    }
    if (className != null) {
      ctxVals.add(new ContextDataItemBase(LogBase.ORIGIN_CLASS, className, LogService.getBackEnd()));
    }
    ContextData ctx = new ContextData(ctxVals);
    return new LogEvent(threadId, logTime, logName, logLevel, logMessage, msgParams, ctx, cause);
  }

  private static class ReroutingHandler extends Handler
  {
    @Override
    public void publish(LogRecord record) {
      LogService.getBackEnd().onLogEvent(toLogEvent(record));
    }

    @Override
    public void flush() {  }

    @Override
    public void close() throws SecurityException {  }
  }

  /**
   *  Get a {@link Handler} that simply routes all {@link LogRecord}s to the current configured {@link LogBackEnd}.
   *
   *  @return A re-routing handler.
   */
  public static Handler createReroutingHandler() {
    return new ReroutingHandler();
  }

  /**
   *  Remove all existing handlers from the root logger and add a new {@link #createReroutingHandler()} instead.
   */
  public static void resetRootJULLogger() {
    LogBackEnd logBackEnd = LogService.getBackEnd();
    if (logBackEnd instanceof JULBasedLogBackEnd) {
      return ;
    }
    Logger rootLogger = LogManager.getLogManager().getLogger("");
    try {
      List<Handler> oldHandlers = Arrays.asList(rootLogger.getHandlers());
      for (Handler h : oldHandlers) {
        rootLogger.removeHandler(h);
      }
      rootLogger.addHandler(createReroutingHandler());
    }
    catch (Exception e) {
      LogService.error("Failed to reset JUL logger: %s", e);
    }
  }

  public static void configure(String prefix, int level) {
    LogManager.getLogManager().reset();
    Logger rootLogger = LogManager.getLogManager().getLogger("");
    for (Handler h : rootLogger.getHandlers()) {
      rootLogger.removeHandler(h);
      h.close();
    }
    Handler consoleHandler = createConsoleHandler(level, createConsoleFormatter(prefix, true));
    rootLogger.addHandler(consoleHandler);
  }

  /**
   *  Create a new handler for console output, including
   *  the use of a formatter created by createConsoleFormatter.
   *
   *  @param logLevel The min. log level for console output. Use {@link LogLevel} constants.
   *  @param formatter The formatter to use.
   *
   *  @return A pre-configured console handler.
   */
  public static ConsoleHandler createConsoleHandler(int logLevel, Formatter formatter) {
    Contract.isNotNull("formatter", formatter);
    ConsoleHandler consoleOutput = new ConsoleHandler();
    consoleOutput.setFormatter(formatter);
    consoleOutput.setLevel(JULUtils.toNearestLevel(logLevel));
    return consoleOutput;
  }

  /**
   *  Create a log event formatter for log event output to the console.
   *
   *  @param logPrefix The application name (+ colon + space) that will be prefixed to all console output.
   *  @param showStackTraces Whether stack traces should be shown.
   *
   *  @return A pre-configured formatter.
   */
  public static TextFormatter createConsoleFormatter(String logPrefix, boolean showStackTraces) {
    TextFormatter consoleFormatter = new TextFormatter();
    consoleFormatter.setShowStackTrace(showStackTraces);
    consoleFormatter.setInfoLevelPrefix("");
    consoleFormatter.setTrimTrailingWhitespace(true);
    consoleFormatter.setTrimLeadingWhitespace(true);
    if (!StringUtils.isNullOrEmpty(logPrefix)) {
      consoleFormatter.setMessagePrefixFormat(logPrefix + ": ${indent}${level}");
    }
    else {
      consoleFormatter.setMessagePrefixFormat("${indent}${level}");
    }
    return consoleFormatter;
  }
}
