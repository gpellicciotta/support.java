// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.tools.charsets;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.pellicciotta.support.Info;
import com.pellicciotta.support.annotations.CommandLineAction;
import com.pellicciotta.support.app.*;
import com.pellicciotta.support.config.CommandLine;
import com.pellicciotta.support.logging.Log;
import com.pellicciotta.support.logging.LogService;
import com.pellicciotta.support.utils.CollectionUtils;

public class CharsetsGenerator extends BasicApp<CharsetsGeneratorConfig>
{
  // <editor-fold desc="Command-line Entry-Point">

  /**
   *  List all known charsets.
   *
   *  @param args Command-line arguments can be:
   *              <pre>
   *              Options:
   *                 --config-file=FILE_PATH          Config file to use.
   *
   *              Actions:
   *                 list                             List all available character sets
   *                 version                          Show version info and exit
   *                 help                             Show help info and exit
   *                 list-config                      List active configuration
   *                 list-config-options              List all possible configuration settings
   *              </pre>
   */
  public static void main(String[] args) {
    System.exit(new CharsetsGenerator().run(args));
  }

  // </editor-fold>

  private static final String NAME = "charsets";
  private static final String PRETTY_NAME = "Charsets Generator";
  private static final String DESCRIPTION = "Generate charset info";
  private static final Version VERSION = new Version(Info.VERSION);

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Version getVersion() {
    return VERSION;
  }

  @Override
  public String getPrettyName() {
    return PRETTY_NAME;
  }

  @Override
  public String getDescription() {
    return DESCRIPTION;
  }

  @Override
  protected CharsetsGeneratorConfig getInitialConfiguration() {
    return new CharsetsGeneratorConfig();
  }

  @Override
  protected String getCommandLinePrefix() {
    return String.format("java -cp [CLASSPATH] tools %s", this.getName());
  }

  @Override
  protected Log getAppLog() {
    return LogService.getBackEnd().getLog(NAME);
  }

  @CommandLineAction(name = "list", description = "List all known charsets")
  protected int runGenerateAction(CommandLine cli, CharsetsGeneratorConfig config, String[] positionalArguments) {
    Log log = this.getAppLog();
    try {
      SortedMap<String, Charset> charsets = Charset.availableCharsets();
      for (Map.Entry<String, Charset> cs : charsets.entrySet()) {
        System.out.printf("%-30s  Aliases: %s%n", cs.getKey(), CollectionUtils.toString(cs.getValue().aliases(), "", ", ", ""));
      }
      return ExitCode.NORMAL;
    }
    catch (Exception e) {
      log.error("Generating all charsets failed due to: %s", e);
      return ExitCode.INTERNAL_ERROR;
    }
  }

  private static String displayTimeZone(TimeZone tz) {
    long hours = TimeUnit.MILLISECONDS.toHours(tz.getRawOffset());
    long minutes = TimeUnit.MILLISECONDS.toMinutes(tz.getRawOffset()) - TimeUnit.HOURS.toMinutes(hours);
    // avoid -4:-30 issue
    minutes = Math.abs(minutes);
    String result = "";
    if (hours >= 0) {
      result = String.format("(GMT+%d:%02d) %s", hours, minutes, tz.getID());
    }
    else {
      result = String.format("(GMT%d:%02d) %s", hours, minutes, tz.getID());
    }
    /*
    result += " displ:'" + tz.getDisplayName()
           + "', dst:" + tz.useDaylightTime()
        + ", long-std-name: " + tz.getDisplayName(false, TimeZone.LONG, Locale.ENGLISH)
        + ", short-std-name: " + tz.getDisplayName(false, TimeZone.SHORT, Locale.ENGLISH)
        + ", long-dst-name: " + tz.getDisplayName(true, TimeZone.LONG, Locale.ENGLISH)
        + ", short-dst-name: " + tz.getDisplayName(true, TimeZone.SHORT, Locale.ENGLISH);
    */
    return result;
  }
}
