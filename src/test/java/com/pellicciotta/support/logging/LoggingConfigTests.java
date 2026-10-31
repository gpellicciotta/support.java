// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import com.pellicciotta.support.annotations.ConfigSetting;
import com.pellicciotta.support.config.*;
import com.pellicciotta.support.config.v1.AppConfigTests;
import com.pellicciotta.support.logging.jul.JULBasedLogBackEnd;
import com.pellicciotta.support.logging.noop.NoopLogBackEnd;
import com.pellicciotta.support.logging.spi.JsonFormatter;
import com.pellicciotta.support.logging.spi.LoggerConfig;
import com.pellicciotta.support.logging.spi.LoggingConfig;
import com.pellicciotta.support.logging.spi.TextFormatter;
import com.pellicciotta.support.logging.stdout.StdoutBasedLogBackEnd;
import com.pellicciotta.support.utils.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LoggingConfigTests {
  @Test
  public void testLoggingConfig() throws Exception {
    LoggingConfig logging = new LoggingConfig();
    Config config = new Config(logging);

    // Check defaults:
    Assertions.assertEquals(StdoutBasedLogBackEnd.class, logging.getBackEndType());
    Assertions.assertEquals(true, logging.getIncludeStackTraces());
    Assertions.assertEquals(LogLevel.INFO, logging.getMinLevel());
    Assertions.assertNull(logging.getFilePath());
    Assertions.assertEquals(true, logging.getFileAppend());

    // Update directly:
    logging.setBackEndType(JULBasedLogBackEnd.class);
    logging.setIncludeStackTraces(false);
    logging.setMinLevel(LogLevel.ERROR);
    logging.setFilePath("/var/log/app.log");
    logging.setFileAppend(false);

    Assertions.assertEquals(JULBasedLogBackEnd.class, logging.getBackEndType());
    Assertions.assertEquals(false, logging.getIncludeStackTraces());
    Assertions.assertEquals(LogLevel.ERROR, logging.getMinLevel());
    Assertions.assertEquals("/var/log/app.log", logging.getFilePath());
    Assertions.assertEquals(false, logging.getFileAppend());

    System.setProperty("MIN_LOG_LEVEL", "debug");

    // Update from command-line:
    String[] args = CollectionUtils.asArray("run",
        "--config-file=src/test/resources/com/pellicciotta/support/config/app.config",
        "--logging.min-level=${env:MIN_LOG_LEVEL}",
        "--web.host=foo.bar.com",
        "--logging.back-end-type=com.pellicciotta.support.logging.noop.NoopLogBackEnd",
        "--logging.include-stack-traces",
        "--logging.logger.1.name=foo",
        "--logging.logger.1.level=error",
        "--logging.logger.2.name=bar",
        "--logging.logger.2.level=warning",
        "--logging.logger.3.name=com",
        "--logging.logger.3.level=INFO",
        "--enum-f=do-something",
        "--byte-size-c=214 GB",
        "--date-time-e=1974-07-16 12:00:00",
        "foo", "and", "bar");
    config.loadFrom(args);

    Assertions.assertEquals(NoopLogBackEnd.class, logging.getBackEndType());
    Assertions.assertEquals(true, logging.getIncludeStackTraces());
    Assertions.assertEquals(LogLevel.DEBUG, logging.getMinLevel());

    Map<String, LoggerConfig> lcs = logging.getLoggerConfigs();
    Assertions.assertEquals(3, lcs.size());
    Assertions.assertEquals("foo", lcs.get("foo").getName());
    Assertions.assertEquals(LogLevel.ERROR, lcs.get("foo").getMinLogLevel().intValue());
    Assertions.assertEquals("bar", lcs.get("bar").getName());
    Assertions.assertEquals(LogLevel.WARNING, lcs.get("bar").getMinLogLevel().intValue());
    Assertions.assertEquals("com", lcs.get("com").getName());
    Assertions.assertEquals(LogLevel.INFO, lcs.get("com").getMinLogLevel().intValue());
  }

  @Test
  public void testLoggingFormatterConfig() throws Exception {
    LoggingConfig logging = new LoggingConfig();
    logging.setFormatterType(JsonFormatter.class);
    Config config = new Config(logging);

    Assertions.assertEquals(StdoutBasedLogBackEnd.class, logging.getBackEndType());
    Assertions.assertEquals(JsonFormatter.class, logging.getFormatterType());
    Assertions.assertEquals("${level}${indent}", logging.getFormatTemplate());

    // Update directly:
    logging.setFormatTemplate("logging: ${indent}");
    logging.setFormatterType(TextFormatter.class);
    Assertions.assertEquals(TextFormatter.class, logging.getFormatterType());
    Assertions.assertEquals("logging: ${indent}", logging.getFormatTemplate());

    // Update from command-line:
    String[] args = CollectionUtils.asArray("run",
        "--config-file=src/test/resources/com/pellicciotta/support/config/app.config",
        "--logging.min-level=debug",
        "--web.host=foo.bar.com",
        "--logging.formatter-type=com.pellicciotta.support.logging.CustomLogEventFormatter",
        "--logging.format-template=${time} ${level}: ",
        "foo", "and", "bar");
    config.loadFrom(args);

    Assertions.assertEquals(StdoutBasedLogBackEnd.class, logging.getBackEndType());
    Assertions.assertEquals(CustomLogEventFormatter.class, logging.getFormatterType());
    Assertions.assertEquals("${time} ${level}: ", logging.getFormatTemplate());

    LogService.configure(logging);
    LogService.warning("A sample log message");
    LogService.error("There is an error here: %s", new IllegalStateException("Illegal config?"));
  }

  public static void main(String[] args) throws Exception {
    SettingsFactory sf = new SettingsFactory();
    AppConfigTests.Data d = new AppConfigTests.Data();
    Field fld = ReflectionUtils.listAnnotatedFields(AppConfigTests.Data.class, ConfigSetting.class).get(0);

    Setting<Long> s = sf.attachedSetting(Long.class, d, fld);

    System.out.println("1. d.longVal=" + d.longVal);
    System.out.println("2. d.longVal=" + s.getValue());
    System.out.println("3. value-set=" + s.isValueSet());

    d.longVal = 99L;

    System.out.println("4. d.longVal=" + d.longVal);
    System.out.println("5. d.longVal=" + s.getValue());
    System.out.println("6. value-set=" + s.isValueSet());

    s.resetValue();

    System.out.println("7. d.longVal=" + d.longVal);
    System.out.println("8. d.longVal=" + s.getValue());
    System.out.println("9. value-set=" + s.isValueSet());

    s.setValue(2222L);

    System.out.println("10. d.longVal=" + d.longVal);
    System.out.println("11. d.longVal=" + s.getValue());
    System.out.println("12. value-set=" + s.isValueSet());

    Config cfg = sf.attachedConfig(d);

    System.out.println("13. config=" + cfg);

    d.embedded.byteSizeVal = 455L;
    d.embedded.stringVal = "NEWER";

    System.out.println("14. config=" + cfg);

    Setting<String> ss = cfg.getSetting("embedded.string-val");
    ss.setValue("A NEWER VALUE THIS IS");

    System.out.println("15. config=" + cfg);
  }
}
