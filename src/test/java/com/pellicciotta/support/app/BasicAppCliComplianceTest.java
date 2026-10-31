// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.app;

import com.pellicciotta.support.config.CommandLine;
import com.pellicciotta.support.logging.Log;
import com.pellicciotta.support.logging.LogLevel;
import com.pellicciotta.support.logging.LogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 *  Verifies general-guidelines.md CLI compliance of {@link BasicApp}'s built-in <code>version</code>/<code>help</code>
 *  actions and <code>--verbose</code> handling, as exercised by every tool built on top of it.
 */
public class BasicAppCliComplianceTest
{
  private static class SampleApp extends BasicApp<BasicConfig> {
    @Override public String getName() { return "sample"; }
    @Override public Version getVersion() { return new Version(1, 2, 3, null, new String[0]); }
    @Override public String getPrettyName() { return "Sample App"; }
    @Override public String getDescription() { return "A sample app used for testing"; }
    @Override protected BasicConfig getInitialConfiguration() { return new BasicConfig(); }
    @Override protected Log getAppLog() { return LogService.getBackEnd().getLog("sample"); }
    @Override protected String getCommandLinePrefix() { return "java -cp [CLASSPATH] tools sample"; }
  }

  private ByteArrayOutputStream capturedOut;
  private PrintStream originalOut;

  @BeforeEach
  void redirectStdOut() {
    this.originalOut = System.out;
    this.capturedOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(this.capturedOut, true, StandardCharsets.UTF_8));
  }

  @AfterEach
  void restoreStdOut() {
    System.setOut(this.originalOut);
  }

  @Test
  void versionActionPrintsSingleLineInNameVersionCopyrightFormat() {
    int exitCode = new SampleApp().run(new String[] { "version" });
    assertEquals(ExitCode.NORMAL, exitCode);
    assertEquals("sample v1.2.3 - Copyright (c) Giovanni Pellicciotta" + System.lineSeparator(), this.capturedOut.toString(StandardCharsets.UTF_8));
  }

  @Test
  void versionOptionAliasesMatchVersionAction() {
    for (String versionArg : new String[] { "--version", "-v" }) {
      this.capturedOut.reset();
      int exitCode = new SampleApp().run(new String[] { versionArg });
      assertEquals(ExitCode.NORMAL, exitCode, "for " + versionArg);
      assertEquals("sample v1.2.3 - Copyright (c) Giovanni Pellicciotta" + System.lineSeparator(), this.capturedOut.toString(StandardCharsets.UTF_8), "for " + versionArg);
    }
  }

  @Test
  void helpActionStartsWithVersionLineAndIncludesDescriptionAndUsage() {
    int exitCode = new SampleApp().run(new String[] { "help" });
    assertEquals(ExitCode.NORMAL, exitCode);
    String output = this.capturedOut.toString(StandardCharsets.UTF_8);
    assertTrue(output.startsWith("sample v1.2.3 - Copyright (c) Giovanni Pellicciotta" + System.lineSeparator()), output);
    assertTrue(output.contains("A sample app used for testing"), output);
  }

  @Test
  void helpOptionAliasesAllExitNormally() {
    for (String helpArg : new String[] { "--help", "-h" }) {
      this.capturedOut.reset();
      int exitCode = new SampleApp().run(new String[] { helpArg });
      assertEquals(ExitCode.NORMAL, exitCode, "for " + helpArg);
    }
  }

  @Test
  void verboseFlagLowersMinLogLevelToDebug() throws Exception {
    BasicConfig config = new BasicConfig();
    CommandLine cli = CommandLine.of(new String[] { "--verbose" });
    new SampleApp().applyVerboseOption(cli, config);
    assertEquals(LogLevel.DEBUG, config.logging.getMinLevel());
  }

  @Test
  void explicitMinLevelWinsOverVerboseFlag() throws Exception {
    BasicConfig config = new BasicConfig();
    CommandLine cli = CommandLine.of(new String[] { "--verbose", "--logging.min-level=WARNING" });
    config.logging.setMinLevel(LogLevel.WARNING);
    new SampleApp().applyVerboseOption(cli, config);
    assertEquals(LogLevel.WARNING, config.logging.getMinLevel());
  }

  @Test
  void withoutVerboseFlagMinLevelIsUnchanged() throws Exception {
    BasicConfig config = new BasicConfig();
    CommandLine cli = CommandLine.of(new String[0]);
    int defaultMinLevel = config.logging.getMinLevel();
    new SampleApp().applyVerboseOption(cli, config);
    assertEquals(defaultMinLevel, config.logging.getMinLevel());
  }
}
