// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging.jul;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pellicciotta.support.logging.spi.ContextData;
import com.pellicciotta.support.utils.*;

/**
 *  Provides a custom text log formatter whose default output format complies with logging guidelines for
 *  logging to file.
 */
public class TextFormatter extends java.util.logging.SimpleFormatter {
  // <editor-fold desc="Defaults">

  public static final String DEFAULT_ERROR_LEVEL_PREFIX   = "**[ERROR]**";
  public static final String DEFAULT_WARNING_LEVEL_PREFIX = "**[WARN]** ";
  public static final String DEFAULT_INFO_LEVEL_PREFIX    = "**[INFO]** ";
  public static final String DEFAULT_DEBUG_LEVEL_PREFIX   = "**[DEBUG]**";
  public static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
  public static final String DEFAULT_MESSAGE_PREFIX_FORMAT = "${time} ${level}  ${indent}";
  public static final String DEFAULT_ROOT_LOG_NAME = "<root>";

  private static final Pattern PARAMETER_REGEX = Pattern.compile("([\\\\]*[$])([{]((?:ctx|env)[:])?([^:}]+)(?:[:]([^}]+))?[}])");
  private static final String NL = EnvironmentUtils.getLineSeparator();

  // </editor-fold>
  // <editor-fold desc="Construction">

  /**
   * Construct a default formatter.
   * <p>
   * It will be initialized to:<ul>
   *   <li>Use the default {@link #DEFAULT_MESSAGE_PREFIX_FORMAT} format.</li>
   *   <li>Add stack trace information</li>
   *   <li>Trim leading whitespace</li>
   *   <li>Trim trailing whitespace</li></ul>
   */
  public TextFormatter() {
    this(DEFAULT_MESSAGE_PREFIX_FORMAT, true, true, true);
  }

  /**
   * Construct a formatter that is initialized with the specified properties.
   *
   * @param messagePrefixFormat The message prefix format to use. See {@link #setMessagePrefixFormat(String)} for details.
   * @param showStackTrace Whether stack trace information should be added when formatting a log record.
   * @param trimLeadingWhitespace Whether leading whitespace should get trimmed from the log message.
   * @param trimTrailingWhitespace Whether trailing whitespace should get trimmed from the formatted log record text.
   */
  public TextFormatter(String messagePrefixFormat, boolean showStackTrace, boolean trimLeadingWhitespace, boolean trimTrailingWhitespace) {
    this.errorLevelPrefix = TextFormatter.DEFAULT_ERROR_LEVEL_PREFIX;
    this.warningLevelPrefix = TextFormatter.DEFAULT_WARNING_LEVEL_PREFIX;
    this.infoLevelPrefix = TextFormatter.DEFAULT_INFO_LEVEL_PREFIX;
    this.debugLevelPrefix = TextFormatter.DEFAULT_DEBUG_LEVEL_PREFIX;
    this.setTimeFormat(TextFormatter.DEFAULT_TIME_FORMAT);
    this.setMessagePrefixFormat(messagePrefixFormat);
    this.setShowStackTrace(showStackTrace);
    this.setTrimLeadingWhitespace(trimLeadingWhitespace);
    this.setTrimTrailingWhitespace(trimTrailingWhitespace);
    this.setRootLogName(DEFAULT_ROOT_LOG_NAME);
  }

  // </editor-fold>
  // <editor-fold desc="Properties">

  protected String messagePrefixFormat;
  protected boolean messagePrefixFormatContainsParameters;

  /**
   * @return The currently used format according to which a prefix to the actual log message will get created and used.
   */
  public String getMessagePrefixFormat() {
    return this.messagePrefixFormat;
  }

  /**
   * The prefix to be prepended to the first line when formatting a log event.
   * <p>
   * The actual log message will get appended, and if it contains multiple lines, they will get
   * aligned by prefixing the second and further message lines with enough spaces to line up
   * with the start of the first message line.</p>
   * <p>
   *
   * @param prefixFormat The first line prefix format.
   *                     <p>This prefix can contain parameters that will get replaced by actual values that
   *                     come from the event being logged. Currently, following parameters will be recognized:</p><ul>
   *                       <li><code>${thread-id}</code>: will get replaced by the <code>formatThreadId({@link LogRecord#getThreadID()})</code> value</li>
   *                       <li><code>${name}</code>: will get replaced by the <code>formatLogName({@link LogRecord#getLoggerName()})</code> value</li>
   *                       <li><code>${time}</code>: will get replaced by the <code>formatLogTime({@link LogRecord#getMillis()})</code> value</li>
   *                       <li><code>${level}</code>: will get replaced by the <code>formatLogLevel({@link LogRecord#getLevel()})</code> value</li>
   *                       <li><code>${indent}</code>: will get replaced by the spaces at the start of {@link LogRecord#getMessage()}</li>
   *                       <li><code>${origin-class}</code>: will get replaced by the class originating the log event, if this info is available; else empty.</li>
   *                       <li><code>${origin-method}</code>: will get replaced by the method originating the log event, if this info is available; else empty.</li>
   *                       <li><code>${origin-file}</code>: will get replaced by the source file originating the log event, if this info is available; else empty.</li>
   *                       <li><code>${origin-line}</code>: will get replaced by the line number in the source file originating the log event, if this info is available; else empty.</li>
   *                       <li><code>${ctx:xxx}</code>: will get replaced by the context data item named 'xxx' in the current log event</li>
   *                       <li><code>${env:xxx}</code>: will get replaced by the &lt;appSettings&gt; setting with name 'xxx', or else an environment variable with that name. If no such item exists, will be left untouched.</li>
   *                     </ul>
   *                     <p>
   *                     If a parameter is immediately preceded by a backslash, it will be treated as normal text, while the backslash will be removed from the final result.
   *                     E.g. <code>\${ctx:xxx}</code> will result in the text <code>${ctx:xxx}</code> as result, but no lookup nor substitution of <code>xxx</code> will take place.
   *                     Each pair of backslashes that precedes a <code>$</code>, will also be replaced by a single backslash,
   *                     e.g.
   *                     <pre>
   *                       assuming <code>ctx:xxx</code> has the value <code>yyyy</code>:
   *                         <code>\\${ctx:xxx}</code> becomes: <code>\yyy</code>
   *                         <code>\\\${ctx:xxx}</code> becomes: <code>\${ctx:xxx}</code> as the first pair of backslashes becomes a single backslash, and the following backslash escapes the dollar-sign
   *                     </pre>
   *                     <p>
   *                     If <code>null</code>, {@link #DEFAULT_MESSAGE_PREFIX_FORMAT} will be used.</p>
   */
  public void setMessagePrefixFormat(String prefixFormat) {
    if (prefixFormat == null) {
      this.messagePrefixFormat = DEFAULT_MESSAGE_PREFIX_FORMAT;
      this.messagePrefixFormatContainsParameters = true;
    }
    else {
      this.messagePrefixFormat = prefixFormat;
      this.messagePrefixFormatContainsParameters = PARAMETER_REGEX.matcher(this.messagePrefixFormat).find();
    }
  }

  protected boolean showStackTrace;

  /**
   * Whether stack trace information should be provided when formatting log records that contain a Throwable reference.
   *
   * @return True when stack trace information will be provided.
   */
  public boolean getShowStackTrace() {
    return this.showStackTrace;
  }

  /**
   * Determine whether stack trace information should be provided when formatting log records that contain a Throwable reference.
   *
   * @param show True when stack trace information must be provided, false otherwise.
   */
  public void setShowStackTrace(boolean show) {
    this.showStackTrace = show;
  }

  protected boolean trimLeadingWhitespace;

  /**
   * Whether leading whitespace characters should get stripped from the actual message.
   * <p>
   * Note that any leading whitespace, also when trimmed, will still be used to determine the replacement value for the <code>${indent}</code> parameter.
   *
   * @return True when leading whitespace should get trimmed.
   */
  public boolean getTrimLeadingWhitespace() {
    return this.trimLeadingWhitespace;
  }

  /**
   * Determine whether leading whitespace characters should get trimmed from log message.
   *
   * @param trim Whether to trim leading whitespace automatically.
   */
  public void setTrimLeadingWhitespace(boolean trim) {
    this.trimLeadingWhitespace = trim;
  }

  protected boolean trimTrailingWhitespace;

  /**
   * Whether trailing whitespace characters (including any end-of-line chars) should get trimmed from log messages.
   *
   * @return True when trailing whitespace should get trimmed automatically.
   */
  public boolean getTrimTrailingWhitespace() {
    return this.trimTrailingWhitespace;
  }

  /**
   * Determine whether trailing whitespace characters (including any end-of-line chars) should get trimmed from log messages.
   *
   * @param trim Whether to trim trailing whitespace automatically.
   */
  public void setTrimTrailingWhitespace(boolean trim) {
    this.trimTrailingWhitespace = trim;
  }

  protected String rootLogName;

  /**
   * The name to be used when replacing <code>${name}</code> for an even originating from the root logger (which has an empty string as name).
   *
   * @return The name that will be used for the root logger.
   */
  public String getRootLogName() {
    return this.rootLogName;
  }

  /**
   * The name to be used when replacing <code>${name}</code> for an even originating from the root logger (which has an empty string as name).
   *
   * @param name The name to be used. If null, will default to the default.
   */
  public void setRootLogName(String name) {
    this.rootLogName = (name != null) ? name : DEFAULT_ROOT_LOG_NAME;
  }

  protected String errorLevelPrefix;

  /**
   * @return The error level prefix that will be used.
   */
  public String getErrorLevelPrefix() {
    return this.errorLevelPrefix;
  }

  /**
   * Error level prefix to be used when showLogLevel is true.
   *
   * @param prefix The prefix to be used or null if the default prefix should be used.
   */
  public void setErrorLevelPrefix(String prefix) {
    this.errorLevelPrefix = (prefix == null ? TextFormatter.DEFAULT_ERROR_LEVEL_PREFIX : prefix);
  }

  protected String warningLevelPrefix;

  /**
   * @return The warning level prefix to will be used.
   */
  public String getWarningLevelPrefix() {
    return this.warningLevelPrefix;
  }

  /**
   * Warning level prefix to be used when showLogLevel is true.
   *
   * @param prefix The prefix to be used or null if the default prefix should be used.
   */
  public void setWarningLevelPrefix(String prefix) {
    this.warningLevelPrefix = (prefix == null ? TextFormatter.DEFAULT_WARNING_LEVEL_PREFIX : prefix);
  }

  protected String infoLevelPrefix;

  /**
   * @return The info level prefix to will be used.
   */
  public String getInfoLevelPrefix() {
    return this.infoLevelPrefix;
  }

  /**
   * Info level prefix to be used when showLogLevel is true.
   *
   * @param prefix The prefix to be used or null if the default prefix should be used.
   */
  public void setInfoLevelPrefix(String prefix) {
    this.infoLevelPrefix = (prefix == null ? TextFormatter.DEFAULT_INFO_LEVEL_PREFIX : prefix);
  }

  protected String debugLevelPrefix;

  /**
   * @return The debug level prefix to will be used.
   */
  public String getDebugLevelPrefix() {
    return this.debugLevelPrefix;
  }

  /**
   * Debug level prefix to be used when showLogLevel is true.
   *
   * @param prefix The prefix to be used or null if the default prefix should be used.
   */
  public void setDebugLevelPrefix(String prefix) {
    this.debugLevelPrefix = (prefix == null ? TextFormatter.DEFAULT_DEBUG_LEVEL_PREFIX : prefix);
  }

  protected String timeFormatStr;
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
    this.timeFormatStr = (format == null ? TextFormatter.DEFAULT_TIME_FORMAT : format);
    this.timeFormat = new SimpleDateFormat(this.timeFormatStr);
    this.timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  // [SimpleFormatter Overrides]

  private static final Object[] EMPTY_ARRAY = new Object[0];

  /**
   * Format the specified log record.
   *
   * @param record Record to be formatted.
   *
   * @return A string representation of the record, which can consist of multiple lines but which will always end with a newline character.
   *         The exact format of the message depends on the presence of the parameters in the message format: see {@link #setMessagePrefixFormat(String)}
   *         for the list of possible parameter names.
   */
  @Override
  public synchronized String format(LogRecord record) {
    // Determine arguments, indent, exception and context data
    ContextData cd = null;
    Throwable exc = record.getThrown();
    String indent = "";
    // Copy orig args, but replace exceptions by their messages
    Object[] origArgs = record.getParameters();
    Object[] args = EMPTY_ARRAY;
    if ((origArgs != null) && (origArgs.length > 0)) {
      int len = origArgs.length;
      // Check for context data
      if (origArgs[origArgs.length - 1] instanceof ContextData) {
        cd = (ContextData)origArgs[origArgs.length - 1];
        len -= 1;
      }
      // Copy all but context data
      args = new Object[len];
      System.arraycopy(origArgs, 0, args, 0, len);
      // There is at least one exception object: replace arg by its message
      if (exc != null) {
        for (int i = 0; i < args.length; i++) { // Use message property for formatting Throwable objects
          Object currentArg = args[i];
          if (currentArg instanceof Throwable) {
            args[i] = ((Throwable)currentArg).getMessage();
          }
        }
      }
    }
    // Split indent from actual message
    String recMsg = record.getMessage();
    String origMsg = "" + recMsg; // NOTE: the "" + will ensure 'null' is used if record.getMessage() is actually null
    int msgIndent = 0;
    if (recMsg != null) {
      for (int i = 0; (i < origMsg.length()) && (origMsg.charAt(i) == ' '); i++) {
        msgIndent += 1;
      }
      if (msgIndent > 0) {
        indent = origMsg.substring(0, msgIndent);
        if (this.trimLeadingWhitespace) {
          origMsg = origMsg.substring(msgIndent);
        }
      }
    }
    // Prepare formatting data (if needed):
    Map<String, Object> formattingData = new HashMap<>();
    if (origMsg.contains("$") || this.messagePrefixFormatContainsParameters) {
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
      formattingData.put("thread-id", this.formatThreadId(record.getThreadID()));
      formattingData.put("name", this.formatLogName(record.getLoggerName()));
      formattingData.put("time", this.formatLogTime(record.getMillis()));
      formattingData.put("level", this.formatLogLevel(record.getLevel()));
      formattingData.put("indent", indent);
    }
    // Format prefix
    String prefix = formatText(this.messagePrefixFormat, formattingData);
    // Format message
    if (args.length > 0) { // Handle position arguments first
      try {
        origMsg = String.format(origMsg, args);
      }
      catch (IllegalFormatException fe) {
        // Do nothing, leave orig msg intact
      }
    }
    origMsg = formatText(origMsg, formattingData);
    // Concatenate into final result
    StringBuilder fullMsg = new StringBuilder();
    // Strip trailing line space (on the last line), if requested
    if (this.trimTrailingWhitespace) {
      origMsg = StringUtils.trimRight(origMsg);
    }
    String[] msgLines = StringUtils.split(origMsg);
    fullMsg.append(prefix);
    fullMsg.append(msgLines[0]);
    String nextLineIndentStr = "";
    if ((msgLines.length > 1) || (this.showStackTrace && (exc != null))) { // Make sure all lines beyond the first get indented by the proper amount
      nextLineIndentStr = StringUtils.makeSpacesString(prefix.length());
    }
    // Append continuation lines
    for (int i = 1; i < msgLines.length; i++) {
      fullMsg.append(NL);
      fullMsg.append(nextLineIndentStr);
      fullMsg.append(msgLines[i]);
    }
    // Append stacktrace if needed
    if (this.showStackTrace && (exc != null)) {
      for (String stackTraceLine : StringUtils.split(ExceptionUtils.formatExceptionWithStackTrace(exc, true))) {
        fullMsg.append(NL);
        fullMsg.append(nextLineIndentStr);
        fullMsg.append(stackTraceLine);
      }
    }
    fullMsg.append(NL);
    return fullMsg.toString();
  }

  private String formatText(String origText, Map<String, Object> replacements) {
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
  protected String formatThreadId(int threadId) {
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
    return StringUtils.isNullOrEmpty(logName) ? this.rootLogName : logName;
  }

  /**
   * Format the log time as <code>[YYYY-MM-DD HH:MM:SS]</code>, per the logging guideline.
   *
   * @param timeMillis The log time to be formatted. Expressed in milliseconds since midnight, 01-01-1970 UTC.
   *
   * @return The bracketed string representation for the specified log time.
   */
  protected String formatLogTime(long timeMillis) {
    return "[" + this.timeFormat.format(new Date(timeMillis)) + "]";
  }

  /**
   * Format the log level.
   *
   * @param level The log level to be formatted.
   *
   * @return The string representation for the specified log level that, with a trailing space.
   */
  protected String formatLogLevel(Level level) {
    int logLevel = level.intValue();
    String prefix = this.debugLevelPrefix;
    if (logLevel >= Level.SEVERE.intValue()) {
      prefix = this.errorLevelPrefix;
    }
    else if (logLevel >= Level.WARNING.intValue()) {
      return this.warningLevelPrefix;
    }
    else if (logLevel >= Level.INFO.intValue()) {
      return this.infoLevelPrefix;
    }
    return prefix;
  }

  // </editor-fold>
}
