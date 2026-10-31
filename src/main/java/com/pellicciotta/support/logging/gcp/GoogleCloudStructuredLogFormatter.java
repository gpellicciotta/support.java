// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging.gcp;

import com.pellicciotta.support.logging.LogLevel;
import com.pellicciotta.support.logging.spi.*;
import com.pellicciotta.support.text.TextBuilder;
import com.pellicciotta.support.utils.ExceptionUtils;
import com.pellicciotta.support.utils.StringUtils;

/**
 *  For logging {@link LogEvent}s as structured JSON objects, themselves provided as a single {@link String}.
 *  <p>
 *  The logged JSON object is compatible with https://cloud.google.com/logging/docs/reference/v2/rest/v2/LogEntry
 *  and the actual format written based on: https://cloud.google.com/logging/docs/structured-logging
 *  </p>
 */
public class GoogleCloudStructuredLogFormatter extends LogEventFormatterBase {
  // <editor-fold desc="Construction">

  /**
   * Construct a default formatter.
   * <p>
   * It will be initialized to:<ul>
   *   <li>Include full stack trace information.</li>
   *   <li>Include all context data.</li></ul>
   */
  public GoogleCloudStructuredLogFormatter() {
    this(true, true);
  }

  /**
   * Construct a formatter that is initialized with the specified properties.
   *
   * @param includeAllContextData Whether to include all context data.
   * @param includeStackTrace Whether to include the full stack trace.
  */
  public GoogleCloudStructuredLogFormatter(boolean includeAllContextData, boolean includeStackTrace) {
    this.setIncludeAllContextData(includeAllContextData);
    this.setIncludeStackTrace(includeStackTrace);
  }

  // </editor-fold>
  // <editor-fold desc="Properties">

  /** Whether stack trace information is included for events that carry a cause. */
  protected boolean includeStackTrace;

  /**
   * Whether stack trace information should be provided when formatting log events that have a non-<code>null</code> {@link LogEvent#getCause()}.
   *
   * @return True when stack trace information will be included.
   */
  public boolean getIncludeStackTrace() {
    return this.includeStackTrace;
  }

  /**
   * Determine whether stack trace information should be included when formatting log events that have a non-<code>null</code> {@link LogEvent#getCause()}.
   *
   * @param include True when stack trace information must be included, false otherwise.
   */
  public void setIncludeStackTrace(boolean include) {
    this.includeStackTrace = include;
  }

  /** Whether all context data is included in the formatted output. */
  protected boolean includeAllContextData;

  /**
   * Whether to include all context-data.
   *
   * @return True when all context-data should get included.
   */
  public boolean getIncludeAllContextData() {
    return this.includeAllContextData;
  }

  /**
   * Determine whether all context-data should be included.
   *
   * @param include Whether to include all context-data.
   */
  public void setIncludeAllContextData(boolean include) {
    this.includeAllContextData = include;
  }

  // </editor-fold>
  // <editor-fold desc="Formatting">

  private static String getLogSeverity(int level) {
    switch (level) {
      case LogLevel.ERROR:   return "ERROR";   //return 500;
      case LogLevel.WARNING: return "WARNING"; //return 400;
      case LogLevel.INFO:    return "INFO";    //return 200;
      case LogLevel.DEBUG:   return "DEBUG";   //return 100;
      default:               return "DEFAULT"; //return 0;
    }
  }

  /**
   * Format the specified log record.
   *
   * @param event Log event to be formatted.
   *
   * @return A string containing a JSON-representation of the record.
   */
  public synchronized String format(LogEvent event) {
    TextBuilder tb = new TextBuilder();
    tb.appendLine("{");
    try (TextBuilder.Group objGrp = tb.start()) {
      String origMsg = event.getMessage();
      String msg = this.formatMessage(origMsg, event.getParams(), this.getFormattingData(event));
      tb.appendLine("\"severity\": \"%s\",", getLogSeverity(event.getLogLevel()));
      tb.appendLine("\"timestamp\": \"%s\",", this.formatLogTime(event.getLogTime().toEpochMilli()));
      tb.appendLine("\"message\": %s,", objToJsonString(msg));
      tb.appendLine("\"logName\": %s,", objToJsonString(event.getLogName()));
      tb.appendLine("\"id\": %d,", event.getId());
      tb.appendLine("\"thread-id\": %d,", event.getThreadId());
      tb.appendLine("\"level\": %d,", event.getLogLevel());
      tb.append("\"level-name\": %s", objToJsonString(this.formatLogLevel(event.getLogLevel())));
      if (this.includeAllContextData) {
        tb.appendLine(",");
        ContextData cd = event.getContextData();
        if ((cd != null) && !cd.isEmpty()) {
          tb.appendLine("\"context-data\": {");
          try (TextBuilder.Group cdGrp = tb.start()) {
            boolean first = true;
            for (String cdName : cd.nameSet()) {
              Object cdValue = cd.get(cdName);
              if (!first) {
                tb.appendLine(",");
              }
              tb.append("%s: %s", objToJsonString(cdName), this.objToJsonString(cdValue));
              first = false;
            }
          }
          tb.append("}");
          if (cd.contains("origin-file")) {
            tb.appendLine(",");
            tb.appendLine("\"logging.googleapis.com/sourceLocation\": {");
            tb.appendLine("\"file\": %s", objToJsonString(cd.get("origin-file")));
            if (cd.contains("origin-line")) {
              tb.appendLine(", \"line\": %s", objToJsonString(cd.get("origin-line")));
            }
            if (cd.contains("origin-class") && cd.contains("origin-method")) {
              tb.appendLine(", \"function\": %s", objToJsonString(String.format("%s.%s", cd.get("origin-class"), cd.get("origin-method"))));
            }
            tb.append("}");
          }
        }
      }
      Throwable exc = event.getCause();
      if (exc != null) {
        tb.appendLine(",");
        tb.appendLine("\"cause\": %s,", this.objToJsonString(exc));
        if (this.includeStackTrace) {
          tb.appendLine("\"stack-trace\": [");
          try (TextBuilder.Group stGrp = tb.start()) {
            boolean first = true;
            for (String stackTraceLine : StringUtils.split(ExceptionUtils.formatExceptionWithStackTrace(exc, true))) {
              if (!first) {
                tb.appendLine(",");
              }
              tb.append("%s", this.objToJsonString(stackTraceLine));
              first = false;
            }
          }
          tb.append("]");
        }
      }
    }
    tb.append("}");
    return tb.toString().replaceAll("\r?\n", " ");
  }

  private String objToJsonString(Object obj) {
    if (obj == null) {
      return "null";
    }
    String strVal = obj.toString().replace("\"", "\\\"");
    return "\"" + strVal + "\"";
  }

  // </editor-fold>
}
