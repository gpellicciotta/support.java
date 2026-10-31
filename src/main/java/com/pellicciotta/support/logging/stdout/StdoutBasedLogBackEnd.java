// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging.stdout;

import com.pellicciotta.support.exceptions.ConfigurationException;
import com.pellicciotta.support.logging.*;
import com.pellicciotta.support.logging.spi.*;
import com.pellicciotta.support.utils.Contract;
import com.pellicciotta.support.utils.DateTimeUtils;

import java.lang.reflect.Constructor;

/**
 *  A {@link LogBackEnd} that writes log message to stdout/stderr. Default min. log-level is {@link LogLevel#INFO}.
 */
public class StdoutBasedLogBackEnd extends LogBackEndBase {
  /**
   *  Create a logging back-end with default configuration.
   */
  public StdoutBasedLogBackEnd() throws ConfigurationException {
    this(new LoggingConfig());
  }

  /**
   *  Create a logging back-end based on the provided configuration.
   *
   *  @param config The logging configuration.
  **/
  public StdoutBasedLogBackEnd(LoggingConfig config) throws ConfigurationException {
    super(config);
  }

  protected int minLevel;

  /**
   *  Set the min. log level. Log events with lower levels will be discarded.
   *
   *  @param minLogLevel The min. log level.
   */
  public synchronized void setMinLevel(int minLogLevel) {
    this.minLevel = minLogLevel;
  }

  protected static final TextFormatter DEFAULT_FORMATTER;
  static {
    // No ${time}: timestamps are stripped on stdout/stderr per the logging guideline.
    DEFAULT_FORMATTER = new TextFormatter("${level}  ${indent}", false, true, true);
    // INFO is stripped on stdout/stderr per the logging guideline; ERROR/WARNING/DEBUG keep their default tags.
    DEFAULT_FORMATTER.setInfoLevelPrefix("");
  }

  protected LogEventFormatter formatter;

  /**
   *  Set the formatter to use for turning a {@link LogEvent} into a {@link String} that gets written to <code>stdout</code>.
   *
   *  @param formatter The formatter to use. If <code>null</code>, the default formatter will be used again.
   */
  public synchronized void setFormatter(LogEventFormatter formatter) {
    this.formatter = formatter == null ? DEFAULT_FORMATTER : formatter;
  }

  protected int getMinLogLevel(String logName) {
    return this.minLevel;
  }

  // </editor-fold>
  // <editor-fold desc="LogBackEndBase Overrides">

  protected void resetConfiguration() {
    super.resetConfiguration();
    this.minLevel = 0;
    this.formatter = DEFAULT_FORMATTER;
  }

  /**
   *  Configure the back-end.
   *
   *  @param config The configuration to be applied.
   */
  @Override
  public synchronized void configure(LoggingConfig config) throws ConfigurationException {
    super.configure(config);
    if (config == null) {
      this.minLevel = LogLevel.INFO;
      this.formatter = DEFAULT_FORMATTER;
      return ;
    }
    this.minLevel = config.getMinLevel();
    Class<? extends LogEventFormatter> formatterClass = config.getFormatterType();
    Constructor<? extends LogEventFormatter> formatterCtor = null;
    try {
      formatterCtor = formatterClass.getConstructor();
      LogEventFormatter formatter = formatterCtor.newInstance();
      formatter.configure(config);
      this.formatter = formatter;
      if (this.formatter instanceof TextFormatter) {
        ((TextFormatter)this.formatter).setInfoLevelPrefix("");
      }
    }
    catch (Exception e) {
      throw new ConfigurationException("formatter-type", formatterClass, e);
    }
  }

  @Override
  public synchronized void onLogEvent(LogEvent logEvent) {
    if (logEvent.getLogLevel() < this.minLevel) {
      return ;
    }
    String formatted = this.formatter.format(logEvent);
    System.out.println(formatted);
    System.out.flush();
  }

  // </editor-fold>
}
