// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.config.v0;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import com.pellicciotta.support.app.ExitCode;
import com.pellicciotta.support.config.*;
import com.pellicciotta.support.logging.LogLevel;
import com.pellicciotta.support.logging.LogService;
import com.pellicciotta.support.logging.spi.LoggerConfig;
import com.pellicciotta.support.utils.CollectionUtils;
import com.pellicciotta.support.utils.DateTimeUtils;
import com.pellicciotta.support.utils.XmlUtils;
import org.junit.jupiter.api.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *  v0: Implementing all configuration as direct sub-classes of Config, Setting and SettingsGroup.
 */
public class AppConfigTests {
  @Test
  public void testDefaults() {
    AppConfig cfg = new AppConfig();

    Assertions.assertEquals("no string", cfg.stringA.getValueOrDefault());
    Assertions.assertEquals(1234L, cfg.longD.getValueOrDefault().longValue());
    Assertions.assertEquals(443, cfg.web.httpsPort.getValueOrDefault().intValue());
    Assertions.assertEquals(2, cfg.pairsListG.getDefaultValue().size());
  }

  @Test
  public void testUpdating() {
    AppConfig cfg = new AppConfig();

    Assertions.assertEquals("no string", cfg.stringA.getValueOrDefault());
    Assertions.assertNull(cfg.stringA.getValue());
    Assertions.assertFalse(cfg.stringA.isValueSet());
    cfg.stringA.setValue("a string");
    Assertions.assertTrue(cfg.stringA.isValueSet());
    Assertions.assertEquals("a string", cfg.stringA.getValue());
    Assertions.assertEquals("a string", cfg.stringA.getValueOrDefault());

    Assertions.assertEquals(1234L, cfg.longD.getValueOrDefault().longValue());
    Assertions.assertFalse(cfg.longD.isValueSet());
    cfg.longD.setValue(9876L);
    Assertions.assertTrue(cfg.longD.isValueSet());
    Assertions.assertEquals(9876L, cfg.longD.getValue().longValue());
    Assertions.assertEquals(9876L, cfg.longD.getValueOrDefault().longValue());

    Assertions.assertEquals(443, cfg.web.httpsPort.getValueOrDefault().intValue());
    Assertions.assertFalse(cfg.web.httpsPort.isValueSet());
    cfg.web.httpsPort.setValue(5544);
    Assertions.assertTrue(cfg.web.httpsPort.isValueSet());
    Assertions.assertEquals(5544, cfg.web.httpsPort.getValue().intValue());
    Assertions.assertEquals(5544, cfg.web.httpsPort.getValueOrDefault().intValue());

    Assertions.assertFalse(cfg.pairsListG.isValueSet());
    cfg.pairsListG.addElementValue(new AppConfig.StringIntegerPair("first real", 333));
    Assertions.assertTrue(cfg.pairsListG.isValueSet());
    Assertions.assertEquals(1, cfg.pairsListG.getValue().size());
    Assertions.assertEquals("first real", cfg.pairsListG.getValue().get(0).getFirst());
    Assertions.assertEquals(333L, cfg.pairsListG.getValue().get(0).getSecond().longValue());
    Assertions.assertEquals(333L, cfg.pairsListG.getValueOrDefault().get(0).getSecond().longValue());
  }

  @Test
  public void testLoadAndSave() throws IOException, ParseException {
    AppConfig cfg = new AppConfig();

    Assertions.assertEquals("no string", cfg.stringA.getValueOrDefault());
    Assertions.assertEquals(5L, cfg.byteSizeC.getValueOrDefault().longValue());
    Assertions.assertEquals("localhost", cfg.web.host.getValueOrDefault());

    Setting<Boolean> logS = (Setting<Boolean>)cfg.getSetting("logging.show-stack-trace");
    Assertions.assertEquals(false, logS.getValueOrDefault().booleanValue());

    String[] args = buildTestCommandLine();
    cfg.loadFrom(args);

    Assertions.assertEquals(true, logS.getValueOrDefault().booleanValue());
    Assertions.assertEquals(214000000000L, cfg.byteSizeC.getValueOrDefault().longValue());
    Assertions.assertEquals("foo.bar.com", cfg.web.host.getValueOrDefault());
    Assertions.assertEquals(DateTimeUtils.parseDateTime("1974-07-16 12:00:00"), cfg.dateTimeE.getValueOrDefault());

    List<? extends AppConfig.StringIntegerPair> pairsG = cfg.pairsListG.getValue();
    Assertions.assertEquals(3, pairsG.size());
    Assertions.assertEquals("a first pair", pairsG.get(0).getFirst());
    Assertions.assertEquals(1001, pairsG.get(0).getSecond().longValue());
    Assertions.assertEquals("a second pair", pairsG.get(1).getFirst());
    Assertions.assertEquals(1002, pairsG.get(1).getSecond().longValue());
    Assertions.assertEquals("a third pair", pairsG.get(2).getFirst());
    Assertions.assertEquals(1003, pairsG.get(2).getSecond().longValue());

    List<? extends LoggerConfig> loggers = cfg.logging.loggers.getValue();
    Assertions.assertEquals(3, loggers.size());
    Assertions.assertEquals(LogLevel.WARNING, loggers.get(1).getMinLogLevel().longValue());
    Assertions.assertEquals("com.foo.bar", loggers.get(1).getName());

    Document doc = XmlUtils.createDocument();
    Element xmlConfig = doc.createElement("configuration");
    doc.appendChild(xmlConfig);
    cfg.saveTo(xmlConfig, true);
    Element[] xmlLogging = XmlUtils.getChildElementsByTagName(xmlConfig, "logging");
    Assertions.assertEquals(1, xmlLogging.length);
    Element[] xmlLogger = XmlUtils.getChildElementsByTagName(xmlLogging[0], "logger");
    Assertions.assertEquals(3, xmlLogger.length);

    cfg.loadFrom(doc);

    pairsG = cfg.pairsListG.getValue();
    Assertions.assertEquals(3, pairsG.size());
    Assertions.assertEquals("a first pair", pairsG.get(0).getFirst());
    Assertions.assertEquals(1001, pairsG.get(0).getSecond().longValue());
    Assertions.assertEquals("a second pair", pairsG.get(1).getFirst());
    Assertions.assertEquals(1002, pairsG.get(1).getSecond().longValue());
    Assertions.assertEquals("a third pair", pairsG.get(2).getFirst());
    Assertions.assertEquals(1003, pairsG.get(2).getSecond().longValue());

    loggers = cfg.logging.loggers.getValue();
    Assertions.assertEquals(3, loggers.size());
    Assertions.assertEquals(LogLevel.WARNING, loggers.get(1).getMinLogLevel().longValue());
    Assertions.assertEquals("com.foo.bar", loggers.get(1).getName());

    Map<String, String> props = new TreeMap<>(); // Using TreeMap to ensure keys are sorted
    cfg.saveTo(props, true);
    Assertions.assertEquals(1, xmlLogging.length);
    Assertions.assertEquals(3, xmlLogger.length);

    cfg.loadFrom(props);

    pairsG = cfg.pairsListG.getValue();
    Assertions.assertEquals(3, pairsG.size());
    Assertions.assertEquals("a first pair", pairsG.get(0).getFirst());
    Assertions.assertEquals(1001, pairsG.get(0).getSecond().longValue());
    Assertions.assertEquals("a second pair", pairsG.get(1).getFirst());
    Assertions.assertEquals(1002, pairsG.get(1).getSecond().longValue());
    Assertions.assertEquals("a third pair", pairsG.get(2).getFirst());
    Assertions.assertEquals(1003, pairsG.get(2).getSecond().longValue());

    loggers = cfg.logging.loggers.getValue();
    Assertions.assertEquals(3, loggers.size());
    Assertions.assertEquals(LogLevel.WARNING, loggers.get(1).getMinLogLevel().longValue());
    Assertions.assertEquals("com.foo.bar", loggers.get(1).getName());
  }

  private static String[] buildTestCommandLine() {
    return CollectionUtils.asArray("run",
        "--config-file=src/test/resources/com/pellicciotta/support/config/app.config",
        "--logging.level=error",
        "--web.host=foo.bar.com",
        "--logging.show-stack-trace",
        "--byte-size-c=214 GB",
        "--date-time-e=1974-07-16 12:00:00",
        "--pairs-g.1=\"a first pair\":1001",
        "--pairs-g.2=\"a second pair\":1002",
        "--pairs-g.3=\"a third pair\":1003",
        "--logging.logger.1.name=com.foo",
        "--logging.logger.1.level=info",
        "--logging.logger.2.name=com.foo.bar",
        "--logging.logger.2.level=warning",
        "--logging.logger.3.name=com.foo.bar.baz",
        "--logging.logger.3.level=debug",
        "foo", "and", "bar");
  }

  // Invoke with: --config-file=src\test\java\com\pellicciotta\support\config\spi\app.config
  //              --logging.level=error --logging.show-stack-trace --byte-size-f=214GB
  public static void main(String[] args) {
    if (args.length == 0) {
      args = buildTestCommandLine();
    }
    int exitCode = ExitCode.NORMAL;
    try {
      LogService.info("Configuring based on following command-line: %s", CollectionUtils.toString(args));
      AppConfig cfg = new AppConfig();
      String[] posArgs = cfg.loadFrom(args);
      LogService.info("Positional arguments: %s", CollectionUtils.toString(posArgs));
      String settingsTable = cfg.toTableString();
      LogService.info("%s", settingsTable);
    }
    catch (Exception e) {
      LogService.error("Failed to configure: %s", e);
      exitCode = ExitCode.INTERNAL_ERROR;
    }
    System.exit(exitCode);
  }
}
