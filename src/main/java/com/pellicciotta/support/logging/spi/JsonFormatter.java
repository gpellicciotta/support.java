// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging.spi;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pellicciotta.support.logging.LogLevel;
import com.pellicciotta.support.text.TextBuilder;
import com.pellicciotta.support.utils.*;

/**
 *  For logging {@link LogEvent}s as structured JSON objects, themselves provided as a single {@link String}.
 */
public class JsonFormatter extends LogEventFormatterBase {
  // <editor-fold desc="Construction">

  /**
   * Construct a default formatter.
   * <p>
   * It will be initialized to:<ul>
   *   <li>Include full stack trace information.</li>
   *   <li>Include all context data.</li></ul>
   */
  public JsonFormatter() {
    this(true, true);
  }

  /**
   * Construct a formatter that is initialized with the specified properties.
   *
   * @param includeAllContextData Whether to include all context data.
   * @param includeStackTrace Whether to include the full stack trace.
  */
  public JsonFormatter(boolean includeAllContextData, boolean includeStackTrace) {
    this.setIncludeAllContextData(includeAllContextData);
    this.setIncludeStackTrace(includeStackTrace);
  }

  // </editor-fold>
  // <editor-fold desc="Properties">

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
      tb.appendLine("\"id\": %d,", event.getId());
      tb.appendLine("\"time\": \"%s\",", this.formatLogTime(event.getLogTime().toEpochMilli()));
      tb.appendLine("\"level\": %d,", event.getLogLevel());
      tb.appendLine("\"level-name\": \"%s\",", this.formatLogLevel(event.getLogLevel()));
      tb.appendLine("\"thread-id\": %d,", event.getThreadId());
      String origMsg = event.getMessage();
      tb.appendLine("\"message-format\": \"%s\",", origMsg);
      String msg = this.formatMessage(origMsg, event.getParams(), this.getFormattingData(event));
      tb.appendLine("\"message\": \"%s\",", msg);
      if (this.includeAllContextData) {
        ContextData cd = event.getContextData();
        if ((cd != null) && !cd.isEmpty()) {
          tb.appendLine("\"context-data\": {");
          try (TextBuilder.Group cdGrp = tb.start()) {
            for (String cdName : cd.nameSet()) {
              Object cdValue = cd.get(cdName);
              tb.appendLine("\"%s\": %s,", cdName, this.objToString(cdValue));
            }
          }
          tb.appendLine("},");
        }
      }
      Throwable exc = event.getCause();
      if (exc != null) {
        tb.appendLine("\"cause\": %s,", this.objToString(exc));
        if (this.includeStackTrace) {
          tb.appendLine("\"stack-trace\": [");
          try (TextBuilder.Group stGrp = tb.start()) {
            for (String stackTraceLine : StringUtils.split(ExceptionUtils.formatExceptionWithStackTrace(exc, true))) {
              tb.appendLine("%s,", this.objToString(stackTraceLine));
            }
          }
          tb.appendLine("],");
        }
      }
    }
    tb.append("}");
    return tb.toString();
  }

  private String objToString(Object obj) {
    if (obj == null) {
      return "null";
    }
    String strVal = obj.toString().replace("\"", "\\\"");
    return "\"" + strVal + "\"";
  }

  // </editor-fold>
}
