// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.config.v1;

import java.lang.reflect.Field;
import java.util.Date;

import com.pellicciotta.support.annotations.ConfigGroup;
import com.pellicciotta.support.annotations.ConfigSetting;
import com.pellicciotta.support.app.ExitCode;
import com.pellicciotta.support.config.*;
import com.pellicciotta.support.logging.LogService;
import com.pellicciotta.support.utils.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *  v1: Making configuration available indirectly, based on annotations
 */
public class AppConfigTests
{
  @Test
  public void testDefaults() throws Exception {
    AppConfig appCfg = new AppConfig();

    SettingsFactory create = new SettingsFactory();
    Config cfg = create.attachedConfig(appCfg);

    Assertions.assertEquals("no string", cfg.getSetting("string-a").getValue());
    Assertions.assertEquals("no string", appCfg.stringA);
    Assertions.assertEquals(AppConfig.Action.AVOID_ERRORS, appCfg.enumF);
    Assertions.assertEquals(5L, ((Setting<Long>)cfg.getSetting("byte-size-c")).getValue().longValue());
    Assertions.assertEquals(5L, appCfg.byteSizeC);
    Assertions.assertEquals("localhost", appCfg.web.getHost());
  }

  @Test
  public void testUpdating() throws Exception {
    AppConfig appCfg = new AppConfig();

    SettingsFactory create = new SettingsFactory();
    Config cfg = create.attachedConfig(appCfg);

    Setting<String> stringASetting = cfg.getSetting("string-a");
    Assertions.assertEquals("no string", stringASetting.getValue());
    Assertions.assertEquals("no string", appCfg.stringA);
    Assertions.assertTrue(stringASetting.isValueSet());
    stringASetting.setValue("a string");
    Assertions.assertEquals("a string", stringASetting.getValue());
    Assertions.assertEquals("a string", appCfg.stringA);
    Assertions.assertTrue(stringASetting.isValueSet());

    Setting<AppConfig.Action> enumFSetting = cfg.getSetting("enum-f");
    Assertions.assertEquals(AppConfig.Action.AVOID_ERRORS, enumFSetting.getValue());
    Assertions.assertEquals(AppConfig.Action.AVOID_ERRORS, appCfg.enumF);
    Assertions.assertTrue(enumFSetting.isValueSet());
    enumFSetting.setValue(AppConfig.Action.DO_SOMETHING);
    Assertions.assertEquals(AppConfig.Action.DO_SOMETHING, enumFSetting.getValue());
    Assertions.assertEquals(AppConfig.Action.DO_SOMETHING, appCfg.enumF);
    Assertions.assertTrue(enumFSetting.isValueSet());

    Setting<Long> longDSetting = cfg.getSetting("long-d");
    Assertions.assertEquals(1234L, longDSetting.getValue().longValue());
    Assertions.assertEquals(1234L, appCfg.longD);
    boolean isSet = longDSetting.isValueSet();
    Assertions.assertTrue(isSet);
    appCfg.longD = 9876L;
    Assertions.assertEquals(9876L, longDSetting.getValue().longValue());
    Assertions.assertEquals(9876L, appCfg.longD);
    Assertions.assertTrue(longDSetting.isValueSet());

    Setting<Integer> webHttpsPortSetting = cfg.getSetting("web.https-port");
    Assertions.assertEquals(443, webHttpsPortSetting.getValue().intValue());
    Assertions.assertEquals(443, appCfg.web.getHttpsPort());
    Assertions.assertTrue(webHttpsPortSetting.isValueSet());
    webHttpsPortSetting.setValue(5544);
    //web.httpsPort = 5544;
    Assertions.assertEquals(5544, webHttpsPortSetting.getValue().intValue());
    Assertions.assertEquals(5544, appCfg.web.getHttpsPort());
    Assertions.assertTrue(webHttpsPortSetting.isValueSet());
  }

  @Test
  public void testLoad() throws Exception {
    AppConfig appCfg = new AppConfig();

    SettingsFactory create = new SettingsFactory();
    Config cfg = create.attachedConfig(appCfg);

    Assertions.assertEquals("no string", cfg.getSetting("string-a").getValue());
    Assertions.assertEquals("no string", appCfg.stringA);
    Assertions.assertEquals(AppConfig.Action.AVOID_ERRORS, appCfg.enumF);
    Assertions.assertEquals(5L, ((Setting<Long>)cfg.getSetting("byte-size-c")).getValue().longValue());
    Assertions.assertEquals(5L, appCfg.byteSizeC);
    Assertions.assertEquals("localhost", appCfg.web.getHost());

    Setting<Boolean> logS = (Setting<Boolean>)cfg.getSetting("logging.show-stack-trace");
    Assertions.assertEquals(false, logS.getValue().booleanValue());

    String[] args = CollectionUtils.asArray("run",
        "--config-file=src/test/resources/com/pellicciotta/support/config/app.config",
        "--logging.level=error",
        "--web.host=foo.bar.com",
        "--logging.show-stack-trace",
        "--enum-f=do-something",
        "--byte-size-c=214 GB",
        "--date-time-e=1974-07-16 12:00:00",
        "foo", "and", "bar");
    cfg.loadFrom(args);

    Assertions.assertEquals(true, logS.getValue().booleanValue());
    Assertions.assertEquals(214000000000L, appCfg.byteSizeC);
    Assertions.assertEquals(AppConfig.Action.DO_SOMETHING, appCfg.enumF);
    Assertions.assertEquals("foo.bar.com", appCfg.web.getHost());
    Assertions.assertEquals(DateTimeUtils.parseDateTime("1974-07-16 12:00:00"), appCfg.dateTimeE);
  }

  // Invoke with: --config-file=C:\Users\gpellicciotta\Google Drive\dev-projects\support.java\src\test\java\com\pellicciotta\support\config\spi\app.config
  //              --logging.level=error --logging.show-stack-trace --byte-size-f=214GB
  public static void main1(String[] args) {
    if (args.length == 0) {
      args = CollectionUtils.asArray("run",
                                               "--config-file=src/test/java/com/pellicciotta/support/config/spi/app.config",
                                               "--save-config-file=src/test/java/com/pellicciotta/support/config/spi/app.saved.config",
                                               "--logging.level=error",
                                               "--web.host=foo.bar.com",
                                               "--logging.show-stack-trace",
                                               "--byte-size-f=214 GB",
                                               "foo", "and", "bar");
    }
    int exitCode = ExitCode.NORMAL;
    try {
      LogService.info("Configuring based on following command-line: %s", CollectionUtils.toString(args));
      AppConfig appCfg = new AppConfig();

      SettingsFactory create = new SettingsFactory();
      Config cfg = create.attachedConfig(appCfg);

      String[] posArgs = cfg.loadFrom(args);
      LogService.info("Positional arguments: %s", CollectionUtils.toString(posArgs));
      LogService.info("%s", cfg);
      Setting<Boolean> s = (Setting<Boolean>)cfg.getSetting("logging.show-stack-trace");
      LogService.info("%s=%s", s.getFullName(), s.getValueOrDefault());
      s.setValue(false);
      LogService.info("%s", cfg);
    }
    catch (Exception e) {
      LogService.error("Failed to configure: %s", e);
      exitCode = ExitCode.INTERNAL_ERROR;
    }
    System.exit(exitCode);
  }

  /*
    public static void main(String[] args) {
    // 0. Configure the configuration needs and structure itself (this is a meta-config):
    LoggingConfig loggingConfig = new LoggingConfig();
    WebConfig webConfig = new WebConfig(); //

    ConfigUtils config = new ConfigUtils();
    config.add(loggingConfig);
    config.add(webConfig);

    // 1. Load actual setting values
    config.loadFromCommandLine(args);
    config.loadFrom("config.xml");

    // 2. Use config values
    //String host = ((Setting<String>)httpConfig.getChild("http.host")).getValue();
    String host = webConfig.getHost();
    //int port = ((Setting<Integer>)webConfig.getChild("http.port")).getValue();
    int port = webConfig.getHttpPort();

    // 3. Save config values
    config.saveTo("new-config.xml");
  }
  */


  public static class Data {
    @ConfigSetting(name="long-val", description="a loooooooong value")
    public Long longVal = 125L;

    @ConfigGroup(name="embedded", description="embedded vals")
    public Embedded embedded = new Embedded();

    public static class Embedded {
      @ConfigSetting(name="byte-size", description="a bytesize value")
      public Long byteSizeVal = 33L;

      @ConfigSetting(name="string-val", description="a string value")
      public String stringVal = "BOOHOO";
    }
  }

  public static void main(String[] args) throws Exception {
    SettingsFactory sf = new SettingsFactory();
    Data d = new Data();
    Field fld = ReflectionUtils.listAnnotatedFields(Data.class, ConfigSetting.class).get(0);

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
