// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.sql;

import com.pellicciotta.support.app.Version;
import com.pellicciotta.support.utils.Contract;

/**
 *  The combination of a SQL statement, a version and a comment.
 */
public class VersionedSqlStatement {
  // <editor-fold desc="Construction">

  /**
   *  Creates a statement without input source information.
   *
   *  @param version The version this statement belongs to; must not be null.
   *  @param comment Optional comment describing the statement.
   *  @param sqlStatement The SQL text; must not be null.
   */
  public VersionedSqlStatement(Version version, String comment, String sqlStatement) {
    this(version, comment, sqlStatement, null, -1);
  }

  /**
   *  Creates a statement that remembers where it was read from.
   *
   *  @param version The version this statement belongs to; must not be null.
   *  @param comment Optional comment describing the statement.
   *  @param sqlStatement The SQL text; must not be null.
   *  @param inputSource Optional name of the source the statement came from.
   *  @param startLine Line in the source where the statement starts, or -1 if unknown.
   */
  public VersionedSqlStatement(Version version, String comment, String sqlStatement, String inputSource, int startLine) {
    Contract.isNotNull(version);
    Contract.isNotNull(sqlStatement);

    this.version = version;
    this.comment = comment;
    this.sqlStatement = sqlStatement;
    this.inputSource = inputSource;
    this.startLine = startLine;
  }

  // </editor-fold>
  // <editor-fold desc="Properties">

  private final Version version;

  /**
   *  Returns the version this statement belongs to.
   *
   *  @return The version, never null.
   */
  public Version getVersion() {
    return this.version;
  }

  private final String sqlStatement;

  /**
   *  Returns the SQL text.
   *
   *  @return The SQL statement, never null.
   */
  public String getSqlStatement() {
    return this.sqlStatement;
  }

  private final String comment;

  /**
   *  Returns the comment describing this statement.
   *
   *  @return The comment, or null if none.
   */
  public String getComment() {
    return this.comment;
  }

  final String inputSource;
  final int startLine;

  // </editor-fold>
  // <editor-fold desc="Debugging Aids">

  private String fullText;

  @Override
  public String toString() {
    if (this.fullText == null) {
      StringBuilder sb = new StringBuilder();
      if (this.inputSource != null) {
        sb.append("-- From: ");
        sb.append(this.inputSource);
        sb.append(":");
        sb.append(this.startLine);
        sb.append("\n");
      }
      sb.append("-- <"); sb.append(this.version); sb.append("> ");
      if (this.comment != null) {
        sb.append(this.comment);
      }
      sb.append("\n");
      sb.append(this.sqlStatement);
      this.fullText = sb.toString();
    }
    return this.fullText;
  }

  // </editor-fold>
}
