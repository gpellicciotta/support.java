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
public class LoggingConfig extends SettingsGroup
{
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

  @SuppressWarnings("unchecked")
  public Class<? extends LogBackEnd> getBackEndType() {
    return (Class<? extends LogBackEnd>) this.backEndType.getValueOrDefault();
  }

  public void setBackEndType(Class<? extends LogBackEnd> type) {
    this.backEndType.setValue(type);
  }

  @SuppressWarnings("unchecked")
  public Class<? extends LogEventFormatter> getFormatterType() {
    return (Class<? extends LogEventFormatter>) this.formatterType.getValueOrDefault();
  }

  public void setFormatterType(Class<? extends LogEventFormatter> type) {
    this.formatterType.setValue(type);
  }

  public String getFormatTemplate() {
    return this.formatTemplate.getValueOrDefault();
  }

  public void setFormatTemplate(String template) {
    this.formatTemplate.setValue(template);
  }

  public int getMinLevel() {
    return this.minLevel.getValueOrDefault();
  }

  public void setMinLevel(int level) {
    this.minLevel.setValue(level);
  }

  public boolean getIncludeStackTraces() {
    return this.includeStackTraces.getValueOrDefault();
  }

  public void setIncludeStackTraces(boolean include) {
    this.includeStackTraces.setValue(include);
  }

  public String getFilePath() {
    return this.filePath.getValueOrDefault();
  }

  public void setFilePath(String filePath) {
    this.filePath.setValue(filePath);
  }

  public boolean getFileAppend() {
    return this.fileAppend.getValueOrDefault();
  }

  public void setFileAppend(boolean append) {
    this.fileAppend.setValue(append);
  }

  public Map<String, LoggerConfig> getLoggerConfigs() {
    Map<String, LoggerConfig> namedLoggerConfigs = new TreeMap<>();
    for (LoggerConfig lc : this.loggers.getValueOrDefault()) {
      namedLoggerConfigs.put(lc.getName(), lc);
    }
    return namedLoggerConfigs;
  }

  public void setLoggerConfigs(List<? extends LoggerConfig> configs) {
    this.loggers.setValue(configs);
  }

  public void setLoggerConfigs(Map<String, ? extends LoggerConfig> configs) {
    this.loggers.setValue(new ArrayList<>(configs.values()));
  }

  public void addLoggerConfig(LoggerConfig config) {
    this.loggers.addElementValue(config);
  }

  public LoggerConfig getLoggerConfig(String name) {
    for (LoggerConfig lc : this.loggers.getValue()) {
      if (lc.getName().equals(name)) {
        return lc;
      }
    }
    return null;
  }
}
