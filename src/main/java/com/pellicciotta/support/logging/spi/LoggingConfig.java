// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging.spi;

import com.pellicciotta.support.config.ListSetting;
import com.pellicciotta.support.config.Setting;
import com.pellicciotta.support.config.SettingsFactory;
import com.pellicciotta.support.config.SettingsGroup;
import com.pellicciotta.support.logging.LogLevel;
import com.pellicciotta.support.logging.stdout.StdoutBasedLogBackEnd;

import java.util.*;

/**
 *  Logging configuration {@link SettingsGroup}.
 */
public class LoggingConfig extends SettingsGroup {
  /** Create the settings group <code>logging</code> with all settings at their defaults. */
  public LoggingConfig() {
    super("logging", "Logging back-end settings");
    SettingsFactory create = new SettingsFactory();
    this.backEndType = create.standaloneClassSetting(LogBackEnd.class, "back-end-type", "The type of logging back-end to be used", StdoutBasedLogBackEnd.class);
    this.formatterType = create.standaloneClassSetting(LogEventFormatter.class, "formatter-type", "The type of the log event formatter to be used", TextFormatter.class);
    this.formatTemplate = create.standaloneStringSetting("format-template", "The log event message format template: whether used depends on the configured 'formatter-type'.", "${level}${indent}");
    this.minLevel = create.standaloneLogLevelSetting("min-level", "The min. log level for all non-explicitly configured loggers", LogLevel.INFO);
    this.includeStackTraces = create.standaloneBooleanSetting("include-stack-traces", "Whether stack-traces should get logged", true);
    this.filePath = create.standaloneStringSetting("file-path", "The path of the log file to write to, when using a file-based log back-end", null);
    this.fileAppend = create.standaloneBooleanSetting("file-append", "Whether to append to an existing log file (true) or overwrite/truncate it (false), when using a file-based log back-end", true);
    this.loggers = create.standaloneListSetting(LoggerConfig.class, "logger", "Named-logger specific settings", Collections.emptyList(), LoggerConfig.PROPERTY_CODEC, LoggerConfig.XML_CODEC);
    this.addChildren(this.backEndType, this.formatterType, this.formatTemplate, this.minLevel, this.includeStackTraces, this.filePath, this.fileAppend, this.loggers);
  }

  private final Setting<Class> backEndType;
  private final Setting<Class> formatterType;
  private final Setting<String> formatTemplate;
  private final Setting<Integer> minLevel;
  private final Setting<Boolean> includeStackTraces;
  private final Setting<String> filePath;
  private final Setting<Boolean> fileAppend;
  private final ListSetting<LoggerConfig> loggers;

  /**
   * @return The configured {@link LogBackEnd} class, or the default (stdout) when unset.
   */
  @SuppressWarnings("unchecked")
  public Class<? extends LogBackEnd> getBackEndType() {
    return (Class<? extends LogBackEnd>) this.backEndType.getValueOrDefault();
  }

  /**
   * @param type The {@link LogBackEnd} class to use.
   */
  public void setBackEndType(Class<? extends LogBackEnd> type) {
    this.backEndType.setValue(type);
  }

  /**
   * @return The configured {@link LogEventFormatter} class, or {@link TextFormatter} when unset.
   */
  @SuppressWarnings("unchecked")
  public Class<? extends LogEventFormatter> getFormatterType() {
    return (Class<? extends LogEventFormatter>) this.formatterType.getValueOrDefault();
  }

  /**
   * @param type The {@link LogEventFormatter} class to use.
   */
  public void setFormatterType(Class<? extends LogEventFormatter> type) {
    this.formatterType.setValue(type);
  }

  /**
   * @return The message format template, which only some formatters use.
   */
  public String getFormatTemplate() {
    return this.formatTemplate.getValueOrDefault();
  }

  /**
   * @param template The message format template.
   */
  public void setFormatTemplate(String template) {
    this.formatTemplate.setValue(template);
  }

  /**
   * @return The minimum log level for loggers without their own setting.
   */
  public int getMinLevel() {
    return this.minLevel.getValueOrDefault();
  }

  /**
   * @param level The minimum log level for loggers without their own setting.
   */
  public void setMinLevel(int level) {
    this.minLevel.setValue(level);
  }

  /**
   * @return Whether stack traces are logged.
   */
  public boolean getIncludeStackTraces() {
    return this.includeStackTraces.getValueOrDefault();
  }

  /**
   * @param include Whether stack traces are logged.
   */
  public void setIncludeStackTraces(boolean include) {
    this.includeStackTraces.setValue(include);
  }

  /**
   * @return The log file path for file-based back-ends; <code>null</code> when unset.
   */
  public String getFilePath() {
    return this.filePath.getValueOrDefault();
  }

  /**
   * @param filePath The log file path for file-based back-ends.
   */
  public void setFilePath(String filePath) {
    this.filePath.setValue(filePath);
  }

  /**
   * @return Whether file-based back-ends append to an existing file (true) or truncate it (false).
   */
  public boolean getFileAppend() {
    return this.fileAppend.getValueOrDefault();
  }

  /**
   * @param append Whether file-based back-ends append to an existing file (true) or truncate it (false).
   */
  public void setFileAppend(boolean append) {
    this.fileAppend.setValue(append);
  }

  /**
   * @return A new map of the named-logger settings by logger name, sorted by name.
   */
  public Map<String, LoggerConfig> getLoggerConfigs() {
    Map<String, LoggerConfig> namedLoggerConfigs = new TreeMap<>();
    for (LoggerConfig lc : this.loggers.getValueOrDefault()) {
      namedLoggerConfigs.put(lc.getName(), lc);
    }
    return namedLoggerConfigs;
  }

  /**
   * Replace all named-logger settings.
   *
   * @param configs The new settings.
   */
  public void setLoggerConfigs(List<? extends LoggerConfig> configs) {
    this.loggers.setValue(configs);
  }

  /**
   * Replace all named-logger settings with the values of the map; the keys are ignored.
   *
   * @param configs The new settings.
   */
  public void setLoggerConfigs(Map<String, ? extends LoggerConfig> configs) {
    this.loggers.setValue(new ArrayList<>(configs.values()));
  }

  /**
   * @param config A named-logger setting to add.
   */
  public void addLoggerConfig(LoggerConfig config) {
    this.loggers.addElementValue(config);
  }

  /**
   * @param name The logger name.
   * @return The setting for that logger, or <code>null</code> if there is none.
   */
  public LoggerConfig getLoggerConfig(String name) {
    for (LoggerConfig lc : this.loggers.getValue()) {
      if (lc.getName().equals(name)) {
        return lc;
      }
    }
    return null;
  }
}
