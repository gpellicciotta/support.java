// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.config.v0;

import com.pellicciotta.support.codecs.*;
import com.pellicciotta.support.config.*;
import com.pellicciotta.support.logging.LogLevel;
import com.pellicciotta.support.logging.spi.LoggerConfig;

import java.util.Collections;

public class LoggingConfig extends SettingsGroup {
  public LoggingConfig() {
    super("logging", "Logging configuration");
    SettingsFactory create = new SettingsFactory();
    this.level = create.standaloneLogLevelSetting("level", "Minimal log level", LogLevel.INFO);
    this.prefix = create.standaloneStringSetting("prefix", "Prefix for log messages");
    this.showStackTrace = create.standaloneBooleanSetting("show-stack-trace", "Whether to show full traces for exceptions", false);
    this.loggers = create.standaloneListSetting(LoggerConfig.class,
        "logger", "Log destination specific configuration",
          Collections.emptyList(), LoggerConfig.PROPERTY_CODEC, LoggerConfig.XML_CODEC);
    this.addChildren(this.level, this.prefix, this.showStackTrace, this.loggers);
  }

  Setting<Integer> level;
  Setting<String> prefix;
  Setting<Boolean> showStackTrace;
  ListSetting<LoggerConfig> loggers;

  public int getLevel() {
    return this.level.getValueOrDefault();
  }

  public void setLevel(int level) {
    this.level.setValue(level);
  }

  public String getPrefix() {
    return this.prefix.getValueOrDefault();
  }

  public void setPrefix(String prefix) {
    this.prefix.setValue(prefix);
  }

  public boolean getShowStackTrace() {
    return this.showStackTrace.getValueOrDefault();
  }

  public void setShowStackTrace(boolean show) {
    this.showStackTrace.setValue(show);
  }
}
