// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.sql;

import com.pellicciotta.support.app.Version;
import com.pellicciotta.support.utils.Contract;

/**
 *  The combination of a SQL statement, a version and a comment.
 */
public class VersionedSqlStatement {
  // <editor-fold desc="Construction">

  public VersionedSqlStatement(Version version, String comment, String sqlStatement) {
    this(version, comment, sqlStatement, null, -1);
  }

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

  public Version getVersion() {
    return this.version;
  }

  private final String sqlStatement;

  public String getSqlStatement() {
    return this.sqlStatement;
  }

  private final String comment;

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
