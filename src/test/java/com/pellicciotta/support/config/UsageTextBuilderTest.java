// Copyright (c) Giovanni Pellicciotta
package com.pellicciotta.support.config;

import java.io.IOException;

import com.pellicciotta.support.exceptions.ConfigurationException;
import com.pellicciotta.support.utils.EnvironmentUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *  Unit-tests for the {@link UsageTextBuilder} class.
 */
public class UsageTextBuilderTest {
  static final String NL = EnvironmentUtils.getLineSeparator();

  @Test
  public void testSyntaxOnlyUsage() throws ConfigurationException, IOException {
    UsageTextBuilder utb = new UsageTextBuilder();
    utb.addCommandLineSummary("java -jar diffutils.jar [options] file1 file2");
    Assertions.assertEquals("Usage:" + NL + "    java -jar diffutils.jar [options] file1 file2", utb.toString());

    utb.addCommandLineSummary("java -jar diffutils.jar [options] dir1 dir2", "Comparing two dirs");
    Assertions.assertEquals(  "Usage:" + NL
                        + "    java -jar diffutils.jar [options] file1 file2" + NL
                        + "    java -jar diffutils.jar [options] dir1 dir2" + NL
                        + "        Comparing two dirs", utb.toString());
  }

  @Test
  public void testFullUsage1() throws ConfigurationException, IOException {
    UsageTextBuilder utb = new UsageTextBuilder();
    utb.addCommandLineSummary("java -jar diffutils.jar [options] file1 file2");
    Assertions.assertEquals("Usage:" + NL + "    java -jar diffutils.jar [options] file1 file2", utb.toString());

    utb.addCommandLineSummary("java -jar diffutils.jar [options] dir1 dir2", "Comparing two dirs");
    Assertions.assertEquals(  "Usage:" + NL
                        + "    java -jar diffutils.jar [options] file1 file2" + NL
                        + "    java -jar diffutils.jar [options] dir1 dir2" + NL
                        + "        Comparing two dirs", utb.toString());

    utb.startOptionGroup("Logging Settings");
    utb.addOptionInfo("--logging.console.level=LOGLEVEL", "The threshold for console messages", "[error|warning|info|debug|none]", "'info'");
    utb.startOption("--logging.file.level=LOGLEVEL")
       .addOptionDescription("The threshold for log messages to end up in the log file.\nFor this to work a file must be configured.")
       .addOptionDefaultValueDescription("'debug'")
       .addOptionValidValuesDescription("[error|warning|info|debug|none]")
       .endOption();
    utb.endOptionGroup();

    utb.addOptionInfo("--config-file=FILE", "Main config file", "Any valid file system path", "<none>");

    utb.startExtraInfoGroup("Exit Codes");
    utb.addExtraInfo("%d - Normal end", 0);
    utb.addExtraInfo("%d - Configuration error", 1);
    utb.addExtraInfo("%d - Abnormal end", 2);
    utb.endExtraInfoGroup();

    utb.addExtraInfo("More info can be found in '%s'", "some url");

    utb.startExtraInfoGroup("Extra Resources");
    utb.addExtraInfo("The user guide");
    utb.endExtraInfoGroup();

    Assertions.assertEquals(  "Usage:" + NL
                        + "    java -jar diffutils.jar [options] file1 file2" + NL
                        + "    java -jar diffutils.jar [options] dir1 dir2" + NL
                        + "        Comparing two dirs" + NL
                        + "" + NL
                        + "--config-file=FILE" + NL
                        + "    Main config file" + NL
                        + "    Valid values: Any valid file system path" + NL
                        + "    Default value: <none>" + NL
                        + "" + NL
                        + "Logging Settings:" + NL
                        + "--logging.console.level=LOGLEVEL" + NL
                        + "    The threshold for console messages" + NL
                        + "    Valid values: [error|warning|info|debug|none]" + NL
                        + "    Default value: 'info'" + NL
                        + "--logging.file.level=LOGLEVEL" + NL
                        + "    The threshold for log messages to end up in the log file." + NL
                        + "    For this to work a file must be configured." + NL
                        + "    Valid values: [error|warning|info|debug|none]" + NL
                        + "    Default value: 'debug'" + NL
                        + "" + NL
                        + "More info can be found in 'some url'" + NL
                        + "" + NL
                        + "Exit Codes:" + NL
                        + "    0 - Normal end" + NL
                        + "    1 - Configuration error" + NL
                        + "    2 - Abnormal end" + NL
                        + "" + NL
                        + "Extra Resources:" + NL
                        + "    The user guide"
                        , utb.toString());
  }

  @Test
  public void testFullUsage2() throws ConfigurationException, IOException {
    UsageTextBuilder utb = new UsageTextBuilder();
    utb.addCommandLineSummary("java -jar diffutils.jar [options] file1 file2");
    Assertions.assertEquals("Usage:" + NL + "    java -jar diffutils.jar [options] file1 file2", utb.toString());

    utb.addCommandLineSummary("java -jar diffutils.jar [options] dir1 dir2", "Comparing two dirs");
    Assertions.assertEquals(  "Usage:" + NL
                        + "    java -jar diffutils.jar [options] file1 file2" + NL
                        + "    java -jar diffutils.jar [options] dir1 dir2" + NL
                        + "        Comparing two dirs", utb.toString());

    utb.startOptionGroup("Logging Settings");
    utb.addOptionInfo("--logging.console.level=LOGLEVEL", "The threshold for console messages", "[error|warning|info|debug|none]", "'info'");
    utb.startOption("--logging.file.level=LOGLEVEL")
       .addOptionDescription("The threshold for log messages to end up in the log file.\nFor this to work a file must be configured.")
       .addOptionDefaultValueDescription("'debug'")
       .addOptionValidValuesDescription("[error|warning|info|debug|none]")
       .endOption();
    utb.endOptionGroup();

    utb.addOptionInfo("--config-file=FILE", "Main config file", "Any valid file system path", "<none>");

    utb.addExtraInfo("More info can be found in '%s'", "some url");
    utb.addExtraInfo("     and also in '%s'", "some other url");

    Assertions.assertEquals(  "Usage:" + NL
                        + "    java -jar diffutils.jar [options] file1 file2" + NL
                        + "    java -jar diffutils.jar [options] dir1 dir2" + NL
                        + "        Comparing two dirs" + NL
                        + "" + NL
                        + "--config-file=FILE" + NL
                        + "    Main config file" + NL
                        + "    Valid values: Any valid file system path" + NL
                        + "    Default value: <none>" + NL
                        + "" + NL
                        + "Logging Settings:" + NL
                        + "--logging.console.level=LOGLEVEL" + NL
                        + "    The threshold for console messages" + NL
                        + "    Valid values: [error|warning|info|debug|none]" + NL
                        + "    Default value: 'info'" + NL
                        + "--logging.file.level=LOGLEVEL" + NL
                        + "    The threshold for log messages to end up in the log file." + NL
                        + "    For this to work a file must be configured." + NL
                        + "    Valid values: [error|warning|info|debug|none]" + NL
                        + "    Default value: 'debug'" + NL
                        + "" + NL
                        + "More info can be found in 'some url'" + NL
                        + "     and also in 'some other url'"
                        , utb.toString());
  }

  @Test
  public void testFullUsage3() throws ConfigurationException, IOException {
    UsageTextBuilder utb = new UsageTextBuilder();
    utb.addCommandLineSummary("java -jar diffutils.jar [options] file1 file2");
    Assertions.assertEquals("Usage:" + NL + "    java -jar diffutils.jar [options] file1 file2", utb.toString());

    utb.addCommandLineSummary("java -jar diffutils.jar [options] dir1 dir2", "Comparing two dirs");
    Assertions.assertEquals(  "Usage:" + NL
                        + "    java -jar diffutils.jar [options] file1 file2" + NL
                        + "    java -jar diffutils.jar [options] dir1 dir2" + NL
                        + "        Comparing two dirs", utb.toString());

    utb.addOptionInfo("--config-file=FILE", "Main config file", "Any valid file system path", "<none>");

    utb.addExtraInfo("More info can be found in '%s'", "some url");
    utb.addExtraInfo("     and also in '%s'", "some other url");

    Assertions.assertEquals(  "Usage:" + NL
                        + "    java -jar diffutils.jar [options] file1 file2" + NL
                        + "    java -jar diffutils.jar [options] dir1 dir2" + NL
                        + "        Comparing two dirs" + NL
                        + "" + NL
                        + "--config-file=FILE" + NL
                        + "    Main config file" + NL
                        + "    Valid values: Any valid file system path" + NL
                        + "    Default value: <none>" + NL
                        + "" + NL
                        + "More info can be found in 'some url'" + NL
                        + "     and also in 'some other url'"
                        , utb.toString());
  }

  @Test
  public void testFullUsage4() throws ConfigurationException, IOException {
    UsageTextBuilder utb = new UsageTextBuilder();
    utb.addCommandLineSummary("java -jar diffutils.jar [options] file1 file2");
    Assertions.assertEquals("Usage:" + NL + "    java -jar diffutils.jar [options] file1 file2", utb.toString());

    utb.addCommandLineSummary("java -jar diffutils.jar [options] dir1 dir2", "Comparing two dirs");
    Assertions.assertEquals(  "Usage:" + NL
                        + "    java -jar diffutils.jar [options] file1 file2" + NL
                        + "    java -jar diffutils.jar [options] dir1 dir2" + NL
                        + "        Comparing two dirs", utb.toString());
    utb.startOptionGroup("Configuration File Settings");
    utb.addOptionInfo("--config-file=FILE", "Main config file", "Any valid file system path", "<none>");
    utb.endExtraInfoGroup();
    utb.startOptionGroup("Logging Settings");
    utb.addOptionInfo("--logging.console.level=LOGLEVEL", "The threshold for console messages", "[error|warning|info|debug|none]", "'info'");
    utb.startOption("--logging.file.level=LOGLEVEL")
       .addOptionDescription("The threshold for log messages to end up in the log file.\nFor this to work a file must be configured.")
       .addOptionDefaultValueDescription("'debug'")
       .addOptionValidValuesDescription("[error|warning|info|debug|none]")
       .endOption();
    utb.endOptionGroup();

    Assertions.assertEquals(  "Usage:" + NL
                        + "    java -jar diffutils.jar [options] file1 file2" + NL
                        + "    java -jar diffutils.jar [options] dir1 dir2" + NL
                        + "        Comparing two dirs" + NL
                        + "" + NL
                        + "Configuration File Settings:" + NL
                        + "--config-file=FILE" + NL
                        + "    Main config file" + NL
                        + "    Valid values: Any valid file system path" + NL
                        + "    Default value: <none>" + NL
                        + "" + NL
                        + "Logging Settings:" + NL
                        + "--logging.console.level=LOGLEVEL" + NL
                        + "    The threshold for console messages" + NL
                        + "    Valid values: [error|warning|info|debug|none]" + NL
                        + "    Default value: 'info'" + NL
                        + "--logging.file.level=LOGLEVEL" + NL
                        + "    The threshold for log messages to end up in the log file." + NL
                        + "    For this to work a file must be configured." + NL
                        + "    Valid values: [error|warning|info|debug|none]" + NL
                        + "    Default value: 'debug'"
                        , utb.toString());
  }

  @Test
  public void testNoOptionsUsage() throws ConfigurationException, IOException {
    UsageTextBuilder utb = new UsageTextBuilder();
    utb.addCommandLineSummary("java -jar diffutils.jar [options] file1 file2");
    Assertions.assertEquals("Usage:" + NL + "    java -jar diffutils.jar [options] file1 file2", utb.toString());

    utb.addCommandLineSummary("java -jar diffutils.jar [options] dir1 dir2", "Comparing two dirs");
    Assertions.assertEquals(  "Usage:" + NL
                        + "    java -jar diffutils.jar [options] file1 file2" + NL
                        + "    java -jar diffutils.jar [options] dir1 dir2" + NL
                        + "        Comparing two dirs", utb.toString());
    utb.startExtraInfoGroup("Exit Codes");
    utb.addExtraInfo("%d - Normal end", 0);
    utb.addExtraInfo("%d - Configuration error", 1);
    utb.addExtraInfo("%d - Abnormal end", 2);
    utb.endExtraInfoGroup();

    utb.addExtraInfo("More info can be found in '%s'", "some url");

    utb.startExtraInfoGroup("Extra Resources");
    utb.addExtraInfo("The user guide");
    utb.endExtraInfoGroup();

    Assertions.assertEquals(  "Usage:" + NL
                        + "    java -jar diffutils.jar [options] file1 file2" + NL
                        + "    java -jar diffutils.jar [options] dir1 dir2" + NL
                        + "        Comparing two dirs" + NL
                        + "" + NL
                        + "More info can be found in 'some url'" + NL
                        + "" + NL
                        + "Exit Codes:" + NL
                        + "    0 - Normal end" + NL
                        + "    1 - Configuration error" + NL
                        + "    2 - Abnormal end" + NL
                        + "" + NL
                        + "Extra Resources:" + NL
                        + "    The user guide"
                        , utb.toString());
  }

  @Test
  public void testNoGroups1Usage() throws ConfigurationException, IOException {
    UsageTextBuilder utb = new UsageTextBuilder();
    utb.addCommandLineSummary("java -jar diffutils.jar [options] file1 file2");
    Assertions.assertEquals("Usage:" + NL + "    java -jar diffutils.jar [options] file1 file2", utb.toString());

    utb.addCommandLineSummary("java -jar diffutils.jar [options] dir1 dir2", "Comparing two dirs");
    Assertions.assertEquals(  "Usage:" + NL
                        + "    java -jar diffutils.jar [options] file1 file2" + NL
                        + "    java -jar diffutils.jar [options] dir1 dir2" + NL
                        + "        Comparing two dirs", utb.toString());
    utb.addExtraInfo("%d - Normal end", 0);
    utb.addExtraInfo("%d - Configuration error", 1);
    utb.addExtraInfo("%d - Abnormal end", 2);
    utb.addExtraInfo("More info can be found in '%s'", "some url");

    Assertions.assertEquals(  "Usage:" + NL
                        + "    java -jar diffutils.jar [options] file1 file2" + NL
                        + "    java -jar diffutils.jar [options] dir1 dir2" + NL
                        + "        Comparing two dirs" + NL
                        + "" + NL
                        + "0 - Normal end" + NL
                        + "1 - Configuration error" + NL
                        + "2 - Abnormal end" + NL
                        + "More info can be found in 'some url'"
                        , utb.toString());
  }

  @Test
  public void testNoGroups2Usage() throws ConfigurationException, IOException {
    UsageTextBuilder utb = new UsageTextBuilder();
    utb.addCommandLineSummary("java -jar diffutils.jar [options] file1 file2");
    Assertions.assertEquals("Usage:" + NL + "    java -jar diffutils.jar [options] file1 file2", utb.toString());

    utb.addCommandLineSummary("java -jar diffutils.jar [options] dir1 dir2", "Comparing two dirs");
    Assertions.assertEquals(  "Usage:" + NL
                        + "    java -jar diffutils.jar [options] file1 file2" + NL
                        + "    java -jar diffutils.jar [options] dir1 dir2" + NL
                        + "        Comparing two dirs", utb.toString());
    utb.addOptionInfo("--logging.console.level=LOGLEVEL", "The threshold for console messages", "[error|warning|info|debug|none]", "'info'");
    utb.startOption("--logging.file.level=LOGLEVEL")
       .addOptionDescription("The threshold for log messages to end up in the log file.\nFor this to work a file must be configured.")
       .addOptionDefaultValueDescription("'debug'")
       .addOptionValidValuesDescription("[error|warning|info|debug|none]")
       .endOption();
    utb.addExtraInfo("%d - Normal end", 0);
    utb.addExtraInfo("%d - Configuration error", 1);
    utb.addExtraInfo("%d - Abnormal end", 2);
    utb.addExtraInfo("More info can be found in '%s'", "some url");

    Assertions.assertEquals(  "Usage:" + NL
                        + "    java -jar diffutils.jar [options] file1 file2" + NL
                        + "    java -jar diffutils.jar [options] dir1 dir2" + NL
                        + "        Comparing two dirs" + NL
                        + "" + NL
                        + "--logging.console.level=LOGLEVEL" + NL
                        + "    The threshold for console messages" + NL
                        + "    Valid values: [error|warning|info|debug|none]" + NL
                        + "    Default value: 'info'" + NL
                        + "--logging.file.level=LOGLEVEL" + NL
                        + "    The threshold for log messages to end up in the log file." + NL
                        + "    For this to work a file must be configured." + NL
                        + "    Valid values: [error|warning|info|debug|none]" + NL
                        + "    Default value: 'debug'" + NL
                        + "" + NL
                        + "0 - Normal end" + NL
                        + "1 - Configuration error" + NL
                        + "2 - Abnormal end" + NL
                        + "More info can be found in 'some url'"
                        , utb.toString());
  }

  @Test
  public void testNoSyntaxUsage() throws ConfigurationException, IOException {
    UsageTextBuilder utb = new UsageTextBuilder();
    utb.addOptionInfo("--logging.console.level=LOGLEVEL", "The threshold for console messages", "[error|warning|info|debug|none]", "'info'");
    utb.startOption("--logging.file.level=LOGLEVEL")
       .addOptionDescription("The threshold for log messages to end up in the log file.\nFor this to work a file must be configured.")
       .addOptionDefaultValueDescription("'debug'")
       .addOptionValidValuesDescription("[error|warning|info|debug|none]")
       .endOption();
    utb.addExtraInfo("%d - Normal end", 0);
    utb.addExtraInfo("%d - Configuration error", 1);
    utb.addExtraInfo("%d - Abnormal end", 2);
    utb.addExtraInfo("More info can be found in '%s'", "some url");

    Assertions.assertEquals(  "--logging.console.level=LOGLEVEL" + NL
                        + "    The threshold for console messages" + NL
                        + "    Valid values: [error|warning|info|debug|none]" + NL
                        + "    Default value: 'info'" + NL
                        + "--logging.file.level=LOGLEVEL" + NL
                        + "    The threshold for log messages to end up in the log file." + NL
                        + "    For this to work a file must be configured." + NL
                        + "    Valid values: [error|warning|info|debug|none]" + NL
                        + "    Default value: 'debug'" + NL
                        + "" + NL
                        + "0 - Normal end" + NL
                        + "1 - Configuration error" + NL
                        + "2 - Abnormal end" + NL
                        + "More info can be found in 'some url'"
                        , utb.toString());
  }
}
