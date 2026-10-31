// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.config.v1;

import com.pellicciotta.support.annotations.ConfigSetting;
import com.pellicciotta.support.codecs.StandardStringCodecs;
import com.pellicciotta.support.codecs.StringCodec;
import com.pellicciotta.support.config.*;
import com.pellicciotta.support.logging.LogLevel;
import com.pellicciotta.support.logging.spi.LoggerConfig;
import com.pellicciotta.support.utils.StringConversionUtils;

import java.util.ArrayList;
import java.util.List;

public class LoggingConfig
{
  @ConfigSetting(name="level", description="Minimal log level", stringCodec=StandardStringCodecs.LogLevel.class)
  private int level;
  @ConfigSetting(name="prefix", description="Prefix for log messages")
  private String prefix;
  @ConfigSetting(name="show-stack-trace", description="Whether to show full traces for exceptions")
  private boolean showStackTrace = false;
  @ConfigSetting(name="logger", elementType=LoggerConfig.class, description="Logger specific configuration", propertyCodec=LoggerConfig.PropertyCodec.class, xmlCodec=LoggerConfig.XmlCodec.class)
  private final List<LoggerConfig> loggers = new ArrayList<>();

  public int getLevel() {
    return this.level;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public String getPrefix() {
    return this.prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public boolean getShowStackTrace() {
    return this.showStackTrace;
  }

  public void setShowStackTrace(boolean show) {
    this.showStackTrace = show;
  }
}
