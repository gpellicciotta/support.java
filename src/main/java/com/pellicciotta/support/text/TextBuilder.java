// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.text;

import java.util.ArrayDeque;
import java.util.Deque;

import com.pellicciotta.support.utils.*;

/**
 *  A fluent API for building multi-line blocks of text.
 */
public final class TextBuilder {
  // <editor-fold desc="Support Types">

  /**
   *  A grouping of text lines which are logically indented w.r.t. the previous line.
   */
  public class Group implements AutoCloseable {
    private Group() {
      TextBuilder.this.startGroup(this);
    }

    /**
     *  Close this group and hence also remove the extra indentation.
     */
    @Override
    public void close() {
      TextBuilder.this.closeGroup(this);
    }
  }

  // </editor-fold>

  // <editor-fold desc="Construction">

  /** Create a builder that writes into a new, empty {@link StringBuilder}, indenting groups by two spaces. */
  public TextBuilder() {
    this.sb = new StringBuilder();
  }

  /**
   *  Create a builder that appends to the given buffer, indenting groups by two spaces.
   *
   *  @param sb The target buffer; must not be null.
   */
  public TextBuilder(StringBuilder sb) {
    Contract.isNotNull(sb);
    this.sb = sb;
  }

  /**
   *  Create a builder that appends to the given buffer.
   *
   *  @param sb The target buffer; must not be null.
   *  @param indentSize The number of spaces each open group adds to the indentation; must not be negative.
   */
  public TextBuilder(StringBuilder sb, int indentSize) {
    Contract.isNotNull(sb);
    Contract.isTrue(indentSize >= 0);
    this.sb = sb;
    this.indentStepSize = indentSize;
  }

  // </editor-fold>
  // <editor-fold desc="Build API">

  private final StringBuilder sb;
  private final Deque<Group> openGroups = new ArrayDeque<>();
  private boolean endedInNewLine = true;
  private int indentStepSize = 2;

  /**
   *  Finalize the building, after which this builder will get reset to its initial state.
   *
   *  @return The text built so far.
   */
  public String build() {
    String txt = this.sb.toString();
    this.reset();
    return txt;
  }

  /**
   *  Reset this builder to its initial state.
   *
   *  @return This builder.
   */
  public TextBuilder reset() {
    this.sb.setLength(0);
    return this;
  }

  /**
   *  Register a group as open, which indents subsequent lines.
   *
   *  @param group The group to open.
   */
  protected void startGroup(Group group) {
    this.openGroups.addLast(group);
  }

  /**
   * Start a new group, which also adds increment for all lines in this group.
   */
  public Group start() {
    return new Group();
  }

  /**
   *  Close the group and any groups opened after it.
   *
   *  @param group The group to close.
   *  @throws IllegalArgumentException If the group is not currently open.
   */
  protected void closeGroup(Group group) {
    if (!this.openGroups.contains(group)) {
      throw new IllegalArgumentException("The provided group is not currently open in this TextBuilder");
    }
    for (Group last = this.openGroups.removeLast(); (last != group); last = this.openGroups.removeLast()) { /* Do nothing */  }
  }

  /**
   * Close all open groups up to and including the provided group.
   *
   * @param group The group to get closed.
   * @throws IllegalArgumentException If group is not currently opened.
   */
  public void close(Group group) {
    this.closeGroup(group);
  }

  /**
   *  Append text, indenting each line that starts at the beginning of a line.
   *
   *  @param format The {@link String#format} pattern; used as-is when there are no arguments.
   *  @param args The format arguments.
   *  @return This builder.
   */
  public TextBuilder append(String format, Object... args) {
    if (args.length == 0) {
      this.addText(format);
    }
    else {
      this.addText(String.format(format, args));
    }
    return this;
  }

  /**
   *  Format and append text followed by a line break.
   *
   *  @param format The {@link String#format} pattern; always formatted, so a literal percent sign must be escaped.
   *  @param args The format arguments.
   *  @return This builder.
   */
  public TextBuilder appendLine(String format, Object... args) {
    String txt = String.format(format + "%n", args);
    this.addText(txt);
    return this;
  }

  /**
   *  Append a line break.
   *
   *  @return This builder.
   */
  public TextBuilder appendLine() {
    String txt = String.format("%n");
    this.addText(txt);
    return this;
  }

  private void addText(String text) {
    final String indent = StringUtils.makeCharacterRepetitionString(' ', this.openGroups.size() * this.indentStepSize);
    final String lines[] = StringUtils.split(text);
    for (int i = 0; i < lines.length; i++) {
      if (i == (lines.length - 1)) { // For last line:
        int lineLength = lines[i].length();
        if (lineLength == 0) { // Message ended with newline: already written at end of previous line
          // Do nothing
        }
        else {
          if (this.endedInNewLine) {
            sb.append(indent);
            this.endedInNewLine = false;
          }
          sb.append(lines[i]);
          this.endedInNewLine = false;
        }
      }
      else { // For all other lines:
        if (this.endedInNewLine) {
          sb.append(indent);
          this.endedInNewLine = false;
        }
        sb.append(lines[i]);
        sb.append(EnvironmentUtils.getLineSeparator());
        this.endedInNewLine = true;
      }
    }
  }

  // </editor-fold>
  // <editor-fold desc="Human-readable representation">

  /** @return The text built so far. */
  @Override
  public String toString() {
    return this.sb.toString();
  }

  // </editor-fold>
}
