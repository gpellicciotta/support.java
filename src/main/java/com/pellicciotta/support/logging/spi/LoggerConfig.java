// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging.spi;

import com.pellicciotta.support.utils.StringConversionUtils;
import com.pellicciotta.support.utils.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Settings specific to one logger.
 */
public class LoggerConfig {
  /**
   *  Create settings for the named logger.
   *
   *  @param name The logger name.
   *  @param minLevel The min. log level to be associated with this logger.
   */
  public LoggerConfig(String name, Integer minLevel) {
    assert name != null;

    this.name = name;
    this.minLogLevel = minLevel;
  }

  private final String name;

  /**
   *  @return The logger name. Never <code>null</code>.
   */
  public String getName() {
    return this.name;
  }

  private final Integer minLogLevel;

  /**
   *  @return The min. log level or <code>null</code> if unset.
   */
  public Integer getMinLogLevel() {
    return this.minLogLevel;
  }

  @Override
  public String toString() {
    return STRING_CODEC.encode(this);
  }

  // <editor-fold desc="Codecs">

  public static class XmlCodec implements com.pellicciotta.support.codecs.XmlCodec<LoggerConfig> {
    @Override
    public LoggerConfig decode(Element from) {
      assert from != null;

      from = XmlUtils.getParentOrChildWithTagName(from, "logger");

      String name = from.getAttribute("name");
      int level = StringConversionUtils.toLogLevel(from.getAttribute("level"));

      return new LoggerConfig(name, level);
    }

    @Override
    public void encode(LoggerConfig config, Element parentTo) {
      assert config != null;
      assert parentTo != null;

      Document xmlDoc = parentTo.getOwnerDocument();
      Element newEl = xmlDoc.createElement("logger");
      newEl.setAttribute("name", config.name);
      newEl.setAttribute("level", StringConversionUtils.fromLogLevel(config.minLogLevel));
      parentTo.appendChild(newEl);
    }
  }

  public static final XmlCodec XML_CODEC = new XmlCodec();

  public static class StringCodec implements com.pellicciotta.support.codecs.StringCodec<LoggerConfig> {
    private final static Pattern REGEX = Pattern.compile("^([^:]+) *[:] *([^ ]+) *$");

    @Override
    public LoggerConfig decode(String from) {
      if (from == null) {
        return null;
      }
      Matcher m = REGEX.matcher(from);
      if (!m.matches()) {
        throw new IllegalArgumentException("Failed to decode " + this.getClass().getSimpleName() + " from '" + from + "'");
      }
      String name = m.group(1);
      int level = StringConversionUtils.toLogLevel(m.group(2));

      return new LoggerConfig(name, level);
    }

    @Override
    public String encode(LoggerConfig config) {
      if (config == null) {
        return null;
      }

      return String.format("%s:%s", config.name, StringConversionUtils.fromLogLevel(config.minLogLevel));
    }
  }

  public static final StringCodec STRING_CODEC = new StringCodec();

  public static class PropertyCodec implements com.pellicciotta.support.codecs.PropertyCodec<LoggerConfig> {
    @Override
    public LoggerConfig decode(Map<String, String> from) {
      assert from != null;

      String name = StringConversionUtils.toExpandedString(from.get("name"));
      int level = StringConversionUtils.toLogLevel(from.get("level"));

      return new LoggerConfig(name, level);
    }

    @Override
    public void encode(LoggerConfig config, Map<String, String> to) {
      assert config != null;
      assert to != null;

      to.put("name", config.name);
      to.put("level", StringConversionUtils.fromLogLevel(config.minLogLevel));
    }
  }

  public static final PropertyCodec PROPERTY_CODEC = new PropertyCodec();

  // </editor-fold>
}
