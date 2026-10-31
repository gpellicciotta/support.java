// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.sql;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pellicciotta.support.app.Version;
import com.pellicciotta.support.exceptions.FormatException;
import com.pellicciotta.support.logging.Log;
import com.pellicciotta.support.logging.LogService;
import com.pellicciotta.support.utils.Contract;

/**
 *  <p>
 *  A SQL upgrade script is a text file that contains SQL statements that are preceded with
 *  a special form of comment:
 *  <pre>
 *    -- &lt;version&gt; comment
 *  </pre>
 *  and whose purpose it is to upgrade a database schema from a specific version to a later version.</p>
 *  <p>
 *  Note that each individual SQL statement must be preceded with this form of command, in order to
 *  not worry about SQL parsing itself.</p>
 *  <p>
 *  This {@link SqlScriptFileReader} class enables reading these types of upgrade script files.</p>
 */
public class SqlScriptFileReader {
  // <editor-fold desc="Construction">

  /**
   *  Default constructor.
   */
  public SqlScriptFileReader() { }

  private static final Log log = LogService.fromCurrentClass();

  // </editor-fold>
  // <editor-fold desc="Read/Parse Operations">

  /**
   * Try to read and parse the inputStream as a series of {@link VersionedSqlStatement}s.
   *
   * @param sqlFileName The input file to be interpreted as a valid SQL script file.
   *
   * @return An ordered list of {@link VersionedSqlStatement}s.
   *
   * @throws IOException If the inputStream is invalid or has an invalid internal format.
   */
  public List<VersionedSqlStatement> readResource(File sqlFileName) throws IOException {
    Contract.isNotNull(sqlFileName);
    try (FileInputStream fis = new FileInputStream(sqlFileName)) {
      return this.readResource(sqlFileName.getName(), fis);
    }
  }

  /**
   * Try to read and parse the inputStream as a series of {@link VersionedSqlStatement}s.
   *
   * @param sqlFileName The input file to be interpreted as a valid SQL script file.
   *
   * @return An ordered list of {@link VersionedSqlStatement}s.
   *
   * @throws IOException If the inputStream is invalid or has an invalid internal format.
   */
  public List<VersionedSqlStatement> readFile(String sqlFileName) throws IOException {
    Contract.isNotNull(sqlFileName);
    try (FileInputStream fis = new FileInputStream(sqlFileName)) {
      return this.readResource(sqlFileName, fis);
    }
  }

  /**
   * Try to read and parse the inputStream as a series of {@link VersionedSqlStatement}s.
   *
   * @param resourceName The name of the (embedded) resource representing a SQL script.
   *
   * @return An ordered list of {@link VersionedSqlStatement}s.
   *
   * @throws IOException If the inputStream is invalid or has an invalid internal format.
   */
  public List<VersionedSqlStatement> readResource(String resourceName) throws IOException {
    Contract.isNotNull(resourceName);
    if (resourceName.startsWith("/")) { // Doesn't work if starting with slash
      resourceName = resourceName.substring(1);
    }
    try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
      return this.readResource(resourceName, is);
    }
  }

  /**
   * Try to read and parse the inputStream as a series of {@link VersionedSqlStatement}s.
   *
   * @param inputName The name to be associated with the inputStream.
   * @param inputStream The input stream to be interpreted as a valid SQL script file.
   *
   * @return An ordered list of {@link VersionedSqlStatement}s.
   *
   * @throws IOException If the inputStream is invalid or has an invalid internal format.
   */
  public List<VersionedSqlStatement> readResource(String inputName, InputStream inputStream) throws IOException {
    Contract.isNotNull(inputName);
    Contract.isNotNull(inputStream);
    try (InputStreamReader isr = new InputStreamReader(inputStream)) {
      try (BufferedReader br = new BufferedReader(isr)) {
        this.resetProcessing(inputName);
        String line;
        while ((line = br.readLine()) != null) {
          this.processLine(line);
        }
        this.finishCurrentPartProcessing();
        return this.parts;
      }
    }
  }

  private String sqlScriptName;
  private Version curVersion;
  private int curStartLine;
  private int curLineNum;
  private String curComment;
  private String curStmt;
  private List<VersionedSqlStatement> parts;

  private final static Pattern SQL_VERSIONED_START_COMMENT = Pattern.compile("^\\s*[-][-]\\s+[<](\\d+[.]\\d+(?:[.]\\d+)?(?:-pre)?(?:\\s+[(][^)]+[)])?)[>]\\s+(.*)$", Pattern.CASE_INSENSITIVE);

  private void resetProcessing(String name) {
    this.parts = new ArrayList<>();
    this.sqlScriptName = name;
    this.curVersion = null;
    this.curStartLine = -1;
    this.curLineNum = 0;
    this.curComment = null;
    this.curStmt = null;
  }

  private void processLine(String curLineText) throws IOException {
    this.curLineNum += 1;
    // trace("Processing line #%d: [%s]", this.curLineNum, curLineText);
    curLineText = curLineText.trim();
    if (curLineText.isEmpty()) { // Ignore blank lines
      // trace("-> empty and ignored");
      return ;
    }
    Matcher m = SqlScriptFileReader.SQL_VERSIONED_START_COMMENT.matcher(curLineText);
    if (!m.matches()) { // Not a special comment line
      // trace("-> not a special comment line");
      if (curLineText.startsWith("--")) {
        // trace("-> comment line and ignored");
        ; // Normal comment line: ignore
      }
      else {
        // trace("-> statement line");
        // Non-comment line: add to current statement
        if (this.curVersion == null) { // No new part started yet
          throw new IOException(String.format("SQL statement at line #%d in '%s' is not preceded by a correct version-comment line: %s", this.curLineNum, this.sqlScriptName, curLineText));
        }
        int endOfLineCommentIdx = curLineText.indexOf("--");
        this.curStmt += " ";
        if (endOfLineCommentIdx > 0) {
          this.curStmt += curLineText.substring(0, endOfLineCommentIdx);
        }
        else {
          this.curStmt += curLineText;
        }
        return ;
      }
      return ;
    }
    // trace("-> special comment line");
    // A special comment line was found: start new part after finishing the previous part
    this.finishCurrentPartProcessing();
    this.curComment = "" + m.group(2);
    String versionStr = m.group(1);
    try {
      this.curVersion = new Version(versionStr);
      this.curStartLine = this.curLineNum + 1;
      this.curStmt = "";
    }
    catch (FormatException e) {
      throw new IOException(String.format("SQL version comment has an invalid version '%s'at line #%d in '%s': %s", versionStr, this.curLineNum, this.sqlScriptName, curLineText), e);
    }
  }

  private void finishCurrentPartProcessing() {
    if ((this.curVersion != null) && (this.curStmt != null) && !this.curStmt.trim().isEmpty()) {
      this.curStmt = this.curStmt.trim();
      char lastChar = this.curStmt.charAt(this.curStmt.length() - 1);
      if ("\\/;!".indexOf(lastChar) >= 0) { // Get rid of terminating characters
        this.curStmt = this.curStmt.substring(0, this.curStmt.length() - 1);
      }
      VersionedSqlStatement part = new VersionedSqlStatement(this.curVersion, this.curComment, this.curStmt, this.sqlScriptName, this.curStartLine);
      this.parts.add(part);
      // Reset for next part:
      this.curVersion = null;
      this.curStartLine = -1;
      this.curComment = null;
      this.curStmt = null;
    }
  }

  // </editor-fold>
  // <editor-fold desc="Testing">

  public static void main(String[] args) {
    if (args.length != 1) {
      log.error("Usage: java %s <sql-script-file>", SqlScriptFileReader.class.getName());
      System.exit(1);
    }
    File sqlFile = new File(args[0]);
    log.info("Reading parts from '%s'...", sqlFile);
    try {
      List<VersionedSqlStatement> parts = new SqlScriptFileReader().readResource(sqlFile);
      log.info("Found following parts: ");
      int partNum = 1;
      for (VersionedSqlStatement part : parts) {
        log.info("  Part #%d at line %d in %s", partNum++, part.startLine, part.inputSource);
        log.info("    Version: %s", part.getVersion());
        log.info("    Comment: %s", part.getComment());
        log.info("    SQL    : %s", part.getSqlStatement());
      }
      System.exit(0);
    }
    catch (IOException e) {
      log.error("Reading failed: %s", e);
      System.exit(2);
    }
  }

  // </editor-fold>
}
