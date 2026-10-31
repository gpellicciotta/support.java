// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.tools.diff;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.pellicciotta.support.Info;
import com.pellicciotta.support.annotations.CommandLineAction;
import com.pellicciotta.support.app.*;
import com.pellicciotta.support.config.CommandLine;
import com.pellicciotta.support.diff.*;
import com.pellicciotta.support.exceptions.ConfigurationException;
import com.pellicciotta.support.logging.Log;
import com.pellicciotta.support.logging.LogService;
import com.pellicciotta.support.text.TextBuilder;
import com.pellicciotta.support.utils.*;

/**
 * Command-line tool that diffs two text files and prints the result in the configured output format.
 */
public class DiffTool extends BasicApp<DiffToolConfig> {
  // <editor-fold desc="Command-line Entry-Point">

  /**
   *  Diff two files or directories.
   *
   *  @param args Command-line arguments can be:
   *              <pre>
   *              Options:
   *                 --config-file=FILE_PATH          Config file to use.
   *                 --meta-data-only                 For a directory compare: don't compare actual file contents.
   *                 --stop-on-first-diff             Only look for an indication of differences, not for a full list.
   *                 --algorithm=ALGO_NAME            The underlying diff algorithm to be used.
   *                 --use-colors                     Whether to use colors in output.
   *                 --context-size=INTEGER           Number of context lines to show in unified diff output.
   *                 --output-data=LCS|SES
   *                 --output-format=JSON|CSV|TEXT
   *
   *              Actions:
   *                 diff                             Compare two files or two directories
   *                 version                          Show version info and exit
   *                 help                             Show help info and exit
   *                 list-algorithms                  List available diff algorithms
   *                 list-config                      List active configuration
   *                 list-config-options              List all possible configuration settings
   *              </pre>
   */
  public static void main(String[] args) {
    System.exit(new DiffTool().run(args));
  }

  // </editor-fold>

  private static final String NAME = "diff";
  private static final String PRETTY_NAME = "Diff";
  private static final String DESCRIPTION = "A text file comparison tool";
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
  protected DiffToolConfig getInitialConfiguration() {
    return new DiffToolConfig();
  }

  @Override
  protected String getCommandLinePrefix() {
    return String.format("java -cp [CLASSPATH] tools %s", this.getName());
  }

  @Override
  protected Log getAppLog() {
    return LogService.getBackEnd().getLog(NAME);
  }

  /**
   * Prints the available diff algorithms and their characteristics to standard output.
   *
   * @param cli The parsed command line.
   * @param config The tool configuration.
   * @param positionalArguments The action arguments, unused.
   * @return {@link ExitCode#NORMAL}.
   */
  @CommandLineAction(name = "list-algorithms", description =  "List all available diff algorithms")
  protected int listDiffAlgorithms(CommandLine cli, DiffToolConfig config, String[] positionalArguments) {
    System.out.println("Available diff algorithms:");
    System.out.println("  myers-linear-space: the traditional diff as used in most other tools");
    System.out.println("  histogram         : histogram diff which typically is faster and better for source code");
    System.out.println("  patience-myers    : patience diff combined with myers linear space");
    System.out.println("  patience-histogram: patience diff combined with histogram");
    System.out.println("  myers-greedy      : a slower myers diff implementing the basic greedy algorithm / use only for testing");
    return ExitCode.NORMAL;
  }

  /**
   * Diffs the two files named by the positional arguments.
   *
   * @param cli The parsed command line.
   * @param config The tool configuration.
   * @param positionalArguments Exactly two file paths.
   * @return {@link ExitCode#CONFIG_ERROR} unless exactly two arguments are given, else the comparison result.
   * @throws ConfigurationException If either path does not exist or is a directory.
   */
  @CommandLineAction(name = "diff", description =  "Determine the diff between two text files", commandLineSyntax = "diff <file-1> <file-2>")
  protected int runDiffAction(CommandLine cli, DiffToolConfig config, String[] positionalArguments) {
    Log log = this.getAppLog();
    if (positionalArguments.length != 2) {
      log.error("2 arguments are needed but %d have been provided", positionalArguments.length);
      return ExitCode.CONFIG_ERROR;
    }
    File f1 = new File(positionalArguments[0]);
    File f2 = new File(positionalArguments[1]);
    if (!f1.exists() || f1.isDirectory()) {
      throw new ConfigurationException("command-line", cli, "File 1 '%s' doesn't exist or is not a file", f1);
    }
    if (!f2.exists() || f2.isDirectory()) {
      throw new ConfigurationException("command-line", cli, "File 2 '%s' doesn't exist or is not a file", f2);
    }
    this.config = config;
    return this.runFileCompare(f1, f2);
  }

  private DiffToolConfig config;
  private DiffFile file1;
  private DiffFile file2;

  private List<MatchPair> lcs;
  private List<EditCommand> ses;

  /**
   * Compares two files with the configured algorithm and prints the result in the configured format.
   * Lines matching the configured ignore patterns are excluded from the comparison.
   *
   * @param f1 The first file.
   * @param f2 The second file.
   * @return {@link ExitCode#NORMAL}, or {@link ExitCode#INTERNAL_ERROR} if the comparison fails.
   */
  protected int runFileCompare(File f1, File f2) {
    Log log = this.getAppLog();
    try {
      log.info("Determining diffs between '%s' and '%s':", f1, f2);
      this.file1 = new DiffFile(f1, this.config);
      this.file2 = new DiffFile(f2, this.config);

      DiffAlgorithm<String> engine = DiffUtils.create(this.config.getAlgorithm());

      engine.setSequenceA(this.file1.filteredLines);
      engine.setSequenceB(this.file2.filteredLines);

      if (this.file1.filtered || this.file2.filtered) {
        List<MatchPair> filteredLcs = engine.calculateLongestCommonSubsequence();
        List<EditCommand> filteredSes = engine.calculateEditScript(true);
        this.lcs = new ArrayList<>();
        for (MatchPair fmp : filteredLcs) {
          int idxA = fmp.getIndexA();
          int offA = idxA < this.file1.filteredLineOffsets.length ? this.file1.filteredLineOffsets[idxA] : this.file1.filteredLineOffsets[this.file1.filteredLineOffsets.length - 1];
          int idxB = fmp.getIndexB();
          int offB = idxB < this.file2.filteredLineOffsets.length ? this.file2.filteredLineOffsets[idxB] : this.file2.filteredLineOffsets[this.file2.filteredLineOffsets.length - 1];

          MatchPair mp = DiffUtils.createOffsetMatchPair(fmp, offA, offB);
          this.lcs.add(mp);
        }
        this.ses = new ArrayList<>();
        for (EditCommand fec : filteredSes) {
          int idxA = fec.getStartInA();
          int offA = idxA < this.file1.filteredLineOffsets.length ? this.file1.filteredLineOffsets[idxA] : this.file1.filteredLineOffsets[this.file1.filteredLineOffsets.length - 1];
          int idxB = fec.getStartInB();
          int offB = idxB < this.file2.filteredLineOffsets.length ? this.file2.filteredLineOffsets[idxB] : this.file2.filteredLineOffsets[this.file2.filteredLineOffsets.length - 1];

          EditCommand ec = DiffUtils.createOffsetCommand(fec, offA, offB);
          this.ses.add(ec);
        }
      }
      else {
        this.lcs = engine.calculateLongestCommonSubsequence();
        this.ses = engine.calculateEditScript(true);
      }

      switch (this.config.getOutputFormat()) {
        case JSON_SES:
          this.printSesAsJson();
          break;
        case JSON_LCS:
          this.printLcsAsJson();
          break;
        case SIDE_BY_SIDE:
          this.printSideBySideDiff();
          break;
        case UNIFIED_DIFF:
        default:
          this.printUnifiedDiff();
          break;
      }

      return ExitCode.NORMAL;
    }
    catch (Exception e) {
      log.error("No differences found due to fatal error: %s", e);
      return ExitCode.INTERNAL_ERROR;
    }
  }

  private void printLcsAsJson() throws IOException {
    TextBuilder tb = new TextBuilder();
    tb.appendLine("{");
    tb.appendLine("  \"sequence-a\": {");
    tb.appendLine("    \"path\": \"%s\"", FileUtils.getCanonicalPath(this.file1.file, true));
    tb.appendLine("    \"last-modified-time\": \"%s\"", DateTimeUtils.formatDateTime(this.file1.lastModifiedTime, "yyyy-MM-dd HH:mm:ss.SSSSSS Z"));
    tb.appendLine("  },");
    tb.appendLine("  \"sequence-b\": {");
    tb.appendLine("    \"path\": \"%s\"", FileUtils.getCanonicalPath(this.file2.file, true));
    tb.appendLine("    \"last-modified-time\": \"%s\"", DateTimeUtils.formatDateTime(this.file2.lastModifiedTime, "yyyy-MM-dd HH:mm:ss.SSSSSS Z"));
    tb.appendLine("  },");
    tb.appendLine("  \"lcs\": [");
    for (MatchPair mp : this.lcs) {
      tb.appendLine("    [%d, %d],", mp.getIndexA(), mp.getIndexB());
    }
    tb.appendLine("  ]");
    tb.appendLine("}");
    LogService.info(tb.toString());
  }

  private void printSesAsJson() throws IOException {
    TextBuilder tb = new TextBuilder();
    tb.appendLine("{");
    tb.appendLine("  \"sequence-a\": {");
    tb.appendLine("    \"path\": \"%s\"", FileUtils.getCanonicalPath(this.file1.file, true));
    tb.appendLine("    \"last-modified-time\": \"%s\"", DateTimeUtils.formatDateTime(this.file1.lastModifiedTime, "yyyy-MM-dd HH:mm:ss.SSSSSS Z"));
    tb.appendLine("  },");
    tb.appendLine("  \"sequence-b\": {");
    tb.appendLine("    \"path\": \"%s\"", FileUtils.getCanonicalPath(this.file2.file, true));
    tb.appendLine("    \"last-modified-time\": \"%s\"", DateTimeUtils.formatDateTime(this.file2.lastModifiedTime, "yyyy-MM-dd HH:mm:ss.SSSSSS Z"));
    tb.appendLine("  },");
    tb.appendLine("  \"ses\": [");
    for (EditCommand cmd : this.ses) {
      String t = " ";
      switch (cmd.getType()) {
        case DELETE: t = "-"; break;
        case INSERT: t = "+"; break;
        default: t = " "; break;
      }
      tb.appendLine("    [\"%s\", %d, %d],", t, this.file1.lines[cmd.getStartInA()], this.file2.lines[cmd.getStartInB()]);
    }
    tb.appendLine("  ]");
    tb.appendLine("}");
    LogService.info(tb.toString());
  }

  // Based on: https://en.wikipedia.org/wiki/Diff#:~:text=The%20unified%20format%20(or%20unidiff,input%20to%20the%20patch%20program.
  //      and: https://unix.stackexchange.com/questions/480443/how-shall-i-understand-the-unified-format-of-diff-output
  private void printUnifiedDiff() throws IOException {
    // Currently only implementing unified diff with infinite context
    TextBuilder tb = new TextBuilder();
    tb.appendLine("--- %s %s", FileUtils.getCanonicalPath(this.file1.file, true), DateTimeUtils.formatDateTime(this.file1.lastModifiedTime, "yyyy-MM-dd HH:mm:ss.SSSSSS Z"));
    tb.appendLine("+++ %s %s", FileUtils.getCanonicalPath(this.file2.file, true), DateTimeUtils.formatDateTime(this.file2.lastModifiedTime, "yyyy-MM-dd HH:mm:ss.SSSSSS Z"));

    // Handle edge case of no differences:
    if ((this.lcs.size() == this.file1.filteredLines.length) && (this.lcs.size() == this.file2.filteredLines.length)) {
      LogService.info(tb.toString());
      return ;
    }

    // Use regions to print unified diff:
    List<EditRegion> diffRegions = DiffUtils.diffRegionsFromMatches(this.lcs, 0, this.file1.lines.length, 0, this.file2.lines.length, this.config.getContextSize());
    for (EditRegion r : diffRegions) {
      tb.appendLine("@@ -%d,%d +%d,%d @@", r.getIndexA() + 1, r.getLengthA(), r.getIndexB() + 1, r.getLengthB());
      for (EditCommand c : r.getCommands()) {
        switch (c.getType()) {
          case DELETE:
            addUnifiedDelete(tb, this.file1.lines[c.getStartInA()], this.config.getUseColors());
            break;
          case INSERT:
            addUnifiedInsert(tb, this.file2.lines[c.getStartInB()], this.config.getUseColors());
            break;
          case KEEP:
            addUnifiedMatch(tb, this.file1.lines[c.getStartInA()], this.config.getUseColors());
            break;
        }
      }
    }
    LogService.info(tb.toString());
  }

  private static void addUnifiedDelete(TextBuilder tb, String itemToDelete, boolean useColors) {
    if (useColors) {
      tb.append("\u001b[31m");
    }
    tb.appendLine("-%s", itemToDelete);
    if (useColors) {
      tb.append("\u001b[0m");
    }
  }

  private static void addUnifiedInsert(TextBuilder tb, String itemToInsert, boolean useColors) {
    if (useColors) {
      tb.append("\u001b[32m");
    }
    tb.appendLine("+%s", itemToInsert);
    if (useColors) {
      tb.append("\u001b[0m");
    }
  }

  private static void addUnifiedMatch(TextBuilder tb, String matchingItem, boolean useColors) {
    if (useColors) {
      tb.append("\u001b[90m");
    }
    tb.appendLine(" %s", matchingItem);
    if (useColors) {
      tb.append("\u001b[0m");
    }
  }

  private void printSideBySideDiff() throws IOException {
    // Currently only implementing unified diff with infinite context
    TextBuilder tb = new TextBuilder();
    tb.appendLine("--- %s %s", FileUtils.getCanonicalPath(this.file1.file, true), DateTimeUtils.formatDateTime(this.file1.lastModifiedTime, "yyyy-MM-dd HH:mm:ss.SSSSSS Z"));
    tb.appendLine("+++ %s %s", FileUtils.getCanonicalPath(this.file2.file, true), DateTimeUtils.formatDateTime(this.file2.lastModifiedTime, "yyyy-MM-dd HH:mm:ss.SSSSSS Z"));

    // Handle edge case of no differences:
    if ((this.lcs.size() == this.file1.filteredLines.length) && (this.lcs.size() == this.file2.filteredLines.length)) {
      LogService.info(tb.toString());
      return ;
    }

    // Use regions to print unified diff:
    List<EditRegion> diffRegions = DiffUtils.diffRegionsFromMatches(this.lcs, 0, this.file1.lines.length, 0, this.file2.lines.length, this.config.getContextSize());
    for (EditRegion r : diffRegions) {
      tb.appendLine();
      addSideBySide(tb, String.format("@lines %d-%d", r.getIndexA() + 1, r.getIndexA() + r.getLengthA()), " ",
                        String.format("@lines %d-%d", r.getIndexB() + 1, r.getIndexB() + r.getLengthB()));
      for (EditCommand c : r.getCommands()) {
        switch (c.getType()) {
          case DELETE:
            addSideBySide(tb, this.file1.lines[c.getStartInA()], "<", "");
            break;
          case INSERT:
            addSideBySide(tb, "", ">", this.file2.lines[c.getStartInB()]);
            break;
          case KEEP:
            addSideBySide(tb, this.file1.lines[c.getStartInA()], "|", this.file2.lines[c.getStartInB()]);
            break;
        }
      }
    }
    LogService.info(tb.toString());
  }

  private void addSideBySide(TextBuilder tb, String left, String op, String right) {
    final int size = ((int)Math.floor(this.config.getMaxLineSize() / 2)) - 3;
    if (this.config.getUseColors()) {
      switch (op) {
        case "+":
        case ">":
          tb.append("\u001b[32m");
          op = ">";
          break;
        case "-":
        case "<":
          tb.append("\u001b[31m");
          op = "<";
          break;
        case "|":
          tb.append("\u001b[90m");
          op = "|";
          break;
        default:
        case " ":
          op = " ";
          break;
      }
    }
    tb.append("%s %s %s", StringUtils.padOrTruncateRight(left, size, ' '), op, StringUtils.padOrTruncateRight(right, size, ' '));
    if (this.config.getUseColors()) {
      tb.append("\u001b[0m");
    }
    tb.appendLine();
  }
}
