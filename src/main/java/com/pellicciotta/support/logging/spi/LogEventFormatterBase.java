// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging.spi;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pellicciotta.support.exceptions.ConfigurationException;
import com.pellicciotta.support.logging.LogLevel;
import com.pellicciotta.support.utils.*;

/**
 *  Base class for formatting {@link LogEvent}s as plain text messages.
 */
public abstract class LogEventFormatterBase implements LogEventFormatter {
  // <editor-fold desc="Defaults">

  /** Default {@link SimpleDateFormat} pattern for event times. */
  public static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS000";
  /**
   * Matches <code>${name}</code>, <code>${ctx:name}</code> and <code>${env:name}</code> parameters (optionally
   * with a <code>:format</code> suffix), including leading backslashes.
   */
  protected static final Pattern PARAMETER_REGEX = Pattern.compile("([\\\\]*[$])([{]((?:ctx|env)[:])?([^:}]+)(?:[:]([^}]+))?[}])");
  /** The platform line separator. */
  protected static final String NL = EnvironmentUtils.getLineSeparator();

  // </editor-fold>
  // <editor-fold desc="Construction">

  /**
   * Construct a formatter that is initialized with a default time format.
   */
  protected LogEventFormatterBase() throws ConfigurationException {
    this(new LoggingConfig());
  }

  /**
   * Construct a formatter that is initialized with a default time format and any further configuration that
   * is applicable for this specific formatter.
   *
   * @param config The configuration. Each concrete {@link LogEventFormatter} type determines which config it accepts and uses.
   */
  protected LogEventFormatterBase(LoggingConfig config) throws ConfigurationException {
    this.configure(config);
  }

  // </editor-fold>
  // <editor-fold desc="Properties">

  private LoggingConfig config = null;
  /** The pattern string of the current time format. */
  protected String timeFormatStr;
  /** The UTC {@link DateFormat} built from the current time format pattern. */
  protected DateFormat timeFormat;

  /**
   * @return The time format that will be used.
   */
  public String getTimeFormat() {
    return this.timeFormatStr;
  }

  /**
   * Time format to be used for any subsequent <code>format</code> invocations.
   * UTC will be assumed to be used for the time format.
   *
   * @param format The time format, which should comply with the rules as specified in <code>SimpleDateFormat</code>.
   *               If <code>null</code>, the <code>DEFAULT_TIME_FORMAT</code> will be set.
   */
  public void setTimeFormat(String format) {
    this.timeFormatStr = (format == null ? LogEventFormatterBase.DEFAULT_TIME_FORMAT : format);
    this.timeFormat = new SimpleDateFormat(this.timeFormatStr);
    this.timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  // </editor-fold>
  // <editor-fold desc="LogBackEnd Interface">

  /**
   *  Configure the formatter.
   *
   *  @param config The configuration to be applied.
   */
  public synchronized void configure(LoggingConfig config) throws ConfigurationException {
    this.config = config;
    this.setTimeFormat(LogEventFormatterBase.DEFAULT_TIME_FORMAT);
  }

  // </editor-fold>
  // <editor-fold desc="Formatting">

  /** A shared empty parameter array. */
  protected static final Object[] EMPTY_ARRAY = new Object[0];

  /**
   * Build the substitution values for an event: the context data (<code>origin-*</code> items by name, others as
   * <code>ctx:name</code>) plus <code>thread-id</code>, <code>name</code>, <code>time</code> and
   * <code>level</code>.
   *
   * @param event The event being formatted.
   * @return A new, modifiable map of parameter names to values.
   */
  protected Map<String, Object> getFormattingData(LogEvent event) {
    ContextData cd = event.getContextData();
    // Prepare formatting data (if needed):
    Map<String, Object> formattingData = new HashMap<>();
    if ((cd != null) && !cd.isEmpty()) {
      for (String cdName : cd.nameSet()) {
        Object cdValue = cd.get(cdName);
        if (cdName.startsWith("origin-")) {
          formattingData.put(cdName, cdValue);
        }
        else {
          formattingData.put("ctx:" + cdName, cdValue);
        }
      }
    }
    formattingData.put("thread-id", this.formatThreadId(event.getThreadId()));
    formattingData.put("name", this.formatLogName(event.getLogName()));
    formattingData.put("time", this.formatLogTime(event.getLogTime().toEpochMilli()));
    formattingData.put("level", this.formatLogLevel(event.getLogLevel()));
    return formattingData;
  }

  /**
   * Apply the positional parameters with {@link String#format}, leaving the message unchanged if the format is
   * invalid, then substitute the <code>${...}</code> parameters.
   *
   * @param origMsgFormat The message format.
   * @param formatParams The positional parameters; may be <code>null</code>.
   * @param formattingData The values for the <code>${...}</code> parameters.
   * @return The formatted message.
   */
  protected String formatMessage(String origMsgFormat, Object[] formatParams, Map<String, Object> formattingData) {
    if ((formatParams != null) && (formatParams.length > 0)) { // Handle position arguments first
      try {
        origMsgFormat = String.format(origMsgFormat, formatParams);
      }
      catch (IllegalFormatException fe) {
        // Do nothing, leave orig msg intact
      }
    }
    return this.formatText(origMsgFormat, formattingData);
  }

  /**
   * Substitute <code>${name}</code>, <code>${ctx:name}</code> and <code>${env:name}</code> parameters in the text.
   * Unknown names are left untouched; a backslash before the <code>$</code> suppresses substitution, and backslash
   * pairs collapse to single backslashes.
   *
   * @param origText The text to process.
   * @param replacements The values, keyed by name (<code>ctx:</code> prefixed for context data).
   * @return The text with the parameters replaced.
   */
  protected String formatText(String origText, Map<String, Object> replacements) {
    Matcher m = PARAMETER_REGEX.matcher(origText);
    if (!m.find()) { // No parameters: so leave as-is
      return origText;
    }
    StringBuilder sb = new StringBuilder();
    do {
      // ----1---- ---------------------2----------------------
      //                ------3------   ---4---       ---5---
      // ([\\]*[$])([{]((?:ctx|env)[:])?([^:}]+)(?:[:]([^}]+))?[}])
      int backSlashCount = m.group(1).length() - 1;
      if (backSlashCount % 2 == 1) { // Zero or an uneven number of backslashes: no lookup is to be done
        String rest = m.group(2);
        m.appendReplacement(sb, ""); // The replacement doesn't handle replacing \\ by \ and \$ by $
        int backslashesToKeep = (backSlashCount - 1) / 2;
        for (int i = 0; i < backslashesToKeep; i++) {
          sb.append('\\');
        }
        sb.append('$');
        sb.append(rest);
      }
      else {
        int backslashesToKeep = backSlashCount / 2;
        String backslashes = StringUtils.makeCharacterRepetitionString('\\', backslashesToKeep);
        String ctxName = m.group(3);
        if (ctxName == null) {
          ctxName = "";
        }
        String paramName = m.group(4);
        String paramNameWithContext = ctxName + paramName;
        String paramFormat = m.group(5);
        Object paramValue = null;
        if ("env:".equalsIgnoreCase(ctxName)) {
          paramValue = EnvironmentUtils.getStringProperty(paramName);
        }
        else if (replacements.containsKey(paramNameWithContext)) {
          paramValue = replacements.get(paramNameWithContext);
          if (paramValue == null) {
            paramValue = ""; // So we can treat null as 'not found'
          }
        }
        if (paramValue != null) { // Only apply replacement if name is found
          if ("".equals(paramValue)) {             // Replace by nothing
            m.appendReplacement(sb, ""); // The replacement treats $ and \ specially, that's why we append separately
            sb.append(backslashes);
          }
          else if (paramFormat == null) {
            m.appendReplacement(sb, ""); // The replacement treats $ and \ specially, that's why we append separately
            sb.append(backslashes);
            sb.append(paramValue);
          }
          else {
            String repl = String.format("%" + paramFormat, paramValue);
            m.appendReplacement(sb, ""); // The replacement treats $ and \ specially, that's why we append separately
            sb.append(backslashes);
            sb.append(repl);
          }
        }
      }
    } while (m.find());
    m.appendTail(sb);
    return sb.toString();
  }

  /**
   * Format the thread id.
   *
   * @param threadId The originating event's thread ID to be formatted.
   *
   * @return The string representation for the thread ID, with a trailing space.
   */
  protected String formatThreadId(long threadId) {
    return "" + threadId;
  }

  /**
   * Format the logger name.
   *
   * @param logName The originating log name to be formatted.
   *
   * @return The string representation for the logger name, with a trailing space.
   */
  protected String formatLogName(String logName) {
    return StringUtils.isNullOrEmpty(logName) ? "" : logName;
  }

  /**
   * Format the log time.
   *
   * @param timeMillis The log time to be formatted. Expressed in milliseconds since midnight, 01-01-1970 UTC.
   *
   * @return The string representation for the specified log time.
   */
  protected String formatLogTime(long timeMillis) {
    // In Java 1.6 there is no timezone formatting character that produces a 'Z' (in Java 7 there is 'Z' and in Java 8 there is 'X')
    return this.timeFormat.format(new Date(timeMillis)) + "Z";
  }

  /**
   * Format the log level.
   *
   * @param logLevel The log level to be formatted.
   *
   * @return The string representation for the specified log level that, with a trailing space.
   */
  protected String formatLogLevel(int logLevel) {
    return LogLevel.toString(logLevel);
  }

  // </editor-fold>
}
