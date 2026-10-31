// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.io;

import java.io.IOException;
import java.io.Writer;

import com.pellicciotta.support.utils.*;

/**
 * A specialized {@link Writer} subclass with support for indentation and prefixes.
 * <p>
 * The methods in this class can be put in two categories:</p><ol>
 *   <li>Methods for actual writing content (<code>write</code> and <code>writeln</code>)</li>
 *   <li>Methods for defining various properties that determine how the actual text will be formatted (<code>setXXX</code>, <code>indent</code>, <code>undent</code>).
 *       The various properties can be understood easier when considering this
 *       class to model a paper and pen.
 *       The paper has fixed dimensions (max line size and 1 position per character), while the pen ensures a certain layout (via indentation, wrapping, and prefixing).</li></ol>
 */
public class TextWriter extends Writer {
  // <editor-fold desc="Support Types">

  /**
   *  Possible actions to take when a line is too long.
   */
  public enum TextTooLongAction {
    /** Wrap onto the next line. */
    WRAP,
    /** Truncate. */
    TRUNCATE,
  }

  // </editor-fold>
  // <editor-fold desc="Defaults">

  /** Default max. indentation. */
  public static final int DEFAULT_MAX_INDENT = 40;
  /** Default indent step size. */
  public static final int DEFAULT_INDENT_STEP_SIZE = 2;
  /** Default indentation. */
  public static final int DEFAULT_INDENT = 0;

  /** Default text prefix. */
  public static String DEFAULT_PREFIX = "";
  /** Default max. line size. */
  public static int DEFAULT_MAX_LINE_SIZE = Integer.MAX_VALUE;
  /** Default indent to use for lines with wrapped text. */
  public static int DEFAULT_WRAP_INDENT_SIZE = 2;
  /** Default that determines whether wrapping should be tried on whitespace boundaries. */
  public static boolean DEFAULT_WRAP_ON_WHITESPACE = true;
  /** Default action to take when a text is too long to fit on a single line. */
  public static TextTooLongAction DEFAULT_TEXT_TOO_LONG_ACTION = TextTooLongAction.WRAP;
  /** Default suffix to use for lines that get truncated. */
  public static String DEFAULT_TRUNCATE_SUFFIX = "...";

  // </editor-fold>
  // <editor-fold desc="Construction">

  /**
   * Build a writer with the specified output sink.
   *
   * @param out The writer to be encapsulated.
   *
   * @throws IllegalArgumentException When out is null.
   * @throws IOException When out is not open for writing.
   */
  public TextWriter(Writer out) throws IOException {
    Contract.isNotNull("out", out);
    this.actualWriter = out;
    this.indentPrefix = false;
    this.prefix = DEFAULT_PREFIX;
    this.repeatPrefix = null;
    this.indentation = DEFAULT_INDENT;
    this.indentStepSize = DEFAULT_INDENT_STEP_SIZE;
    this.maxLineSize = DEFAULT_MAX_LINE_SIZE;
    this.maxIndentation = DEFAULT_MAX_INDENT;
    this.wrapIndentSize = DEFAULT_WRAP_INDENT_SIZE;
    this.wrapOnWhitespace = DEFAULT_WRAP_ON_WHITESPACE;
    this.textTooLongAction = DEFAULT_TEXT_TOO_LONG_ACTION;
    this.truncateSuffix = DEFAULT_TRUNCATE_SUFFIX;
    this.autoFlushEnabled = true;
    this.reset();
  }

  private Writer actualWriter;

  // </editor-fold>
  // <editor-fold desc="Properties">

  private boolean autoFlushEnabled;

  /**
   * @return Whether messages that are written should be flushed automatically.
   */
  public synchronized boolean getAutoFlushEnabled() {
    return this.autoFlushEnabled;
  }

  /**
   * Enable or disable the auto-flush behavior. When enabled, every message written will be flushed automatically.
   *
   * @param enabled Whether messages should be flushed automatically.
   */
  public synchronized void setAutoFlushEnabled(boolean enabled) {
    this.autoFlushEnabled = enabled;
  }

  private boolean indentPrefix;

  /**
   * @return Whether prefixes will also be indented.
   */
  public synchronized boolean getIndentPrefix() {
    return this.indentPrefix;
  }

  /**
   * Determine whether prefixes should be indented too.
   *
   * @param indentPrefix Is true, prefixes will be indented.
   */
  public synchronized void setIndentPrefix(boolean indentPrefix) {
    this.indentPrefix = indentPrefix;
  }

  private String prefix;

  /**
   * @return Current prefix text.
   */
  public synchronized String getPrefix() {
    return this.prefix;
  }

  /**
   * Set prefix: this text will be prefixed for each new message (before the very first string written via the writexxx methods and
   * after each subsequent newline).
   *
   * @param prefix New prefix text to be used.
   */
  public synchronized void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  private String repeatPrefix;

  /**
   * @return Current repeat prefix text. If null, this means the normal prefix will be used for the second and subsequent lines in a
   *         writexx call.
   */
  public synchronized String getRepeatPrefix() {
    return this.repeatPrefix;
  }

  /**
   * Set text that will be prefixed for each new message after the first newline: so this prefix will only be used for the second
   * and subsequent lines in a single writexx call.
   *
   * @param prefix New prefix text to be used for non-first lines. If null, the original prefix will be re-used for subsequent
   *          lines.
   */
  public synchronized void setRepeatPrefix(String prefix) {
    this.repeatPrefix = prefix;
  }

  private int maxIndentation;

  /**
   * @return Current maximum indentation.
   */
  public synchronized int getMaxIndent() {
    return this.maxIndentation;
  }

  /**
   * Set the maximum indentation. The current indentation will be adapted to be valid with respect to the new maximum indent size.
   *
   * @param newMaxIndentation New maximum indentation. Must be in range [0, maxLineSize - 1].
   *
   * @throws IllegalArgumentException When newMaxIndentation is not valid.
   */
  public synchronized void setMaxIndent(int newMaxIndentation) {
    Contract.isInRange("newMaxIndentation", newMaxIndentation, 0, Integer.MAX_VALUE);
    this.maxIndentation = newMaxIndentation;
  }

  private int indentStepSize;

  /**
   * @return Current indent step size. This size indicates the amount of indentation characters (spaces) that get added to or
   *         subtracted from the current indent size when indenting, resp. undenting.
   */
  public synchronized int getIndentStepSize() {
    return this.indentStepSize;
  }

  /**
   * Set the indent step size to be used when indenting or undenting.
   *
   * @param newIndentStepSize New indent step size to be used. Must be in range [1, MAX_INT].
   *
   * @throws IllegalArgumentException When newIndentSize is not valid.
   */
  public synchronized void setIndentStepSize(int newIndentStepSize) {
    Contract.isInRange("newIndentStepSize", newIndentStepSize, 1, Integer.MAX_VALUE);
    this.indentStepSize = newIndentStepSize;
  }

  private int indentation;

  /**
   * @return Current indentation (number of spaces that will be put at the start of each following new line).
   */
  public synchronized int getIndent() {
    return this.indentation;
  }

  /**
   * Set the indentation to be used for following new lines.
   *
   * @param newIndentation New indentation to be used. Must be in range [0, getMaxIndent()].
   *
   * @throws IllegalArgumentException When newIndentation is not valid.
   */
  public synchronized void setIndent(int newIndentation) {
    Contract.isInRange("newIndentation", newIndentation, 0, Integer.MAX_VALUE);
    this.indentation = newIndentation;
  }

  /**
   * Increase indentation by indentStepSize.
   */
  public synchronized void indent() {
    this.indentation = Math.min(this.indentation + this.indentStepSize, Math.min(this.maxIndentation, this.maxLineSize));
  }

  /**
   * Decrease indentation by indentStepSize.
   */
  public synchronized void undent() {
    this.indentation = Math.max(this.indentation - this.indentStepSize, 0);
  }

  // [Properties]

  private int wrapIndentSize;

  /**
   * @return Current wrap indent step size. This size indicates the amount of indentation characters (spaces) that get added to the
   *         current indent size when creating extra lines for a message that is too long to fit within the current line bounds
   *         (between indentation and maxLineSize): the message wraps to following lines.
   */
  public synchronized int getWrapIndentSize() {
    return this.wrapIndentSize;
  }

  /**
   * Set the wrap indent size to be used when creating extra lines for a message that is too long to fit on the current line.
   *
   * @param newWrapIndentSize New wrap indent size to be used. Must be in range [0, MAX_INT].
   *
   * @throws IllegalArgumentException When newWrapIndentSize is not valid.
   */
  public synchronized void setWrapIndentSize(int newWrapIndentSize) {
    Contract.isInRange("newWrapIndentSize", newWrapIndentSize, 0, Integer.MAX_VALUE);
    this.wrapIndentSize = newWrapIndentSize;
  }

  private int maxLineSize;

  /**
   * @return The currently configured maximum line size.
   */
  public synchronized int getMaxLineSize() {
    return this.maxLineSize;
  }

  /**
   * Set the maximum line size.
   *
   * @param newMaxLineSize The new maximum line size. If the current indentation is bigger than the new maximum, it will be
   *          decreased to max(newMaxLineSize - 10, 0). Must be in range [1, MAX_INT].
   *
   * @throws IllegalArgumentException When newMaxLineSize is not valid.
   */
  public synchronized void setMaxLineSize(int newMaxLineSize) {
    Contract.isInRange("newMaxLineSize", newMaxLineSize, 1, Integer.MAX_VALUE);
    this.maxLineSize = newMaxLineSize;
  }

  private boolean wrapOnWhitespace;

  /**
   * @return Whether wrapping will currently only occur on whitespace bounds.
   */
  public synchronized boolean getWrapOnWhitespace() {
    return this.wrapOnWhitespace;
  }

  /**
   * Indicate whether wrapping should occur on whitespace bounds or not. If set to false, wrapping can occur in the middle of words.
   *
   * @param wrapOnWhitespace Whether too allow wrapping only on whitespace bounds. Note that when there is no whitespace that can be
   *          used for splitting the original message text, the text will be split anyway.
   */
  public synchronized void setWrapOnWhitespace(boolean wrapOnWhitespace) {
    this.wrapOnWhitespace = wrapOnWhitespace;
  }

  private TextTooLongAction textTooLongAction;

  /**
   * @return Action that gets taken when a messages get too long to fit on the current line.
   */
  public synchronized TextTooLongAction getTextTooLongAction() {
    return this.textTooLongAction;
  }

  /**
   * Set action to undertake when a message gets too long to fit on the current line.
   *
   * @param action The action to undertake.
   */
  public synchronized void setTextTooLongAction(TextTooLongAction action) {
    this.textTooLongAction = action;
  }

  private String truncateSuffix;

  /**
   * @return Current suffix for truncated lines. If null, this means no suffix will be added to a truncated line.
   */
  public synchronized String getTruncateSuffix() {
    return this.truncateSuffix;
  }

  /**
   * Set text that will be suffixed to each truncated line.
   *
   * @param suffix New suffix text to be used for truncated lines. This suffix will be added to the already truncated line, which
   *          mean the total line length could be suffix.length() characters larger than the max-line-size
   */
  public synchronized void setTruncateSuffix(String suffix) {
    this.truncateSuffix = suffix;
  }

  // </editor-fold>
  // <editor-fold desc="Writing State">

  private int currentLine;

  /**
   * @return Current 0-based line index.
   */
  public synchronized int getCurrentLine() {
    return this.currentLine;
  }

  private int nextOutputColumn;

  /**
   * @return 0-based index of the character position within the current line at which the next character will get written.
   */
  public synchronized int getNextOutputColumn() {
    return this.nextOutputColumn;
  }

  // </editor-fold>
  // <editor-fold desc="Output Operations">

  /**
   *  Set the 0-based position at which the next character should get written.
   *
   *  @param newNextOutputColumn The character position within a line.
   *  @param onNewLineIfNeeded Whether to start a new line if the current line has already advanced beyond newNextOutputColumn.
   *
   *  @throws IOException If writing was needed and failed.
   */
  public synchronized void setNextOutputColumn(int newNextOutputColumn, boolean onNewLineIfNeeded) throws IOException {
    if (newNextOutputColumn == this.nextOutputColumn) {
      return;
    }
    else if (newNextOutputColumn >= this.nextOutputColumn) {
      this.write(StringUtils.makeSpacesString(newNextOutputColumn - this.nextOutputColumn));
    }
    else if (onNewLineIfNeeded) {
      // Temporarily disable prefix and indentation
      int oldIndentation = this.indentation;
      this.indentation = 0;
      String oldPrefix = this.prefix;
      this.prefix = "";
      // Make sure we are on a new line
      this.write(EnvironmentUtils.getLineSeparator() + StringUtils.makeSpacesString(newNextOutputColumn));
      // Re-enable prefix and indentation
      this.indentation = oldIndentation;
      this.prefix = oldPrefix;
    }
  }

  /**
   *  Flush any unflushed contents while start on an empty line.
   *
   *  @throws IOException If writing was needed and failed.
   */
  public synchronized void reset() throws IOException {
    this.actualWriter.flush();
    this.nextOutputColumn = 0;
    this.currentLine = 0;
  }

  private void addIndent() throws IOException {
    String indentString = StringUtils.makeSpacesString(this.indentation);
    if (indentString.length() > 0) {
      this.actualWriter.write(indentString);
      this.nextOutputColumn += indentString.length();
    }
  }

  private void addWrapIndent() throws IOException {
    String wrapIndentString = StringUtils.makeSpacesString(this.wrapIndentSize);
    if (wrapIndentString.length() > 0) {
      this.actualWriter.write(wrapIndentString);
      this.nextOutputColumn += wrapIndentString.length();
    }
  }

  private void addPrefix(boolean firstLine) throws IOException {
    if ((this.prefix != null) && (this.prefix.length() > 0)) {
      if (firstLine || (this.repeatPrefix == null)) {
        this.actualWriter.write(this.prefix);
        this.nextOutputColumn += this.prefix.length();
      }
      else {
        this.actualWriter.write(this.repeatPrefix);
        this.nextOutputColumn += this.repeatPrefix.length();
      }
    }
  }

  private void addText(String line) throws IOException {
    if ((this.nextOutputColumn + line.length()) <= this.maxLineSize) {
      this.actualWriter.write(line);
      this.nextOutputColumn += line.length();
    }
    else if (this.textTooLongAction == TextTooLongAction.TRUNCATE) {
      if (this.nextOutputColumn >= this.maxLineSize) {
        // Do nothing as we have already truncated the line previously
      }
      else {
        String[] lineParts = this.splitLine(line);
        String truncatedLine = lineParts[0] + this.truncateSuffix;
        this.actualWriter.write(truncatedLine);
        this.nextOutputColumn += truncatedLine.length();
        this.nextOutputColumn = Math.max(this.nextOutputColumn, this.maxLineSize);
      }
    }
    else { // Wrapping
      String[] lineParts = this.splitLine(line);
      // Finish current line
      this.actualWriter.write(lineParts[0]);
      this.actualWriter.write(EnvironmentUtils.getLineSeparator());
      this.currentLine += 1;
      this.nextOutputColumn = 0;
      // Start new line
      if (this.indentPrefix) {
        this.addIndent();
        this.addPrefix(false);
      }
      else {
        this.addPrefix(false);
        this.addIndent();
      }
      this.addWrapIndent();
      // Add rest of line (via recursive call to ensure further wrapping if necessary)
      this.write(lineParts[1]);
    }
  }

  private String[] splitLine(String line) {
    int splitPoint = (this.maxLineSize - this.nextOutputColumn);
    if (this.wrapOnWhitespace) {
      // Try to find split point on a whitespace character
      for (int i = splitPoint - 1; i >= 0; i--) {
        if (Character.isWhitespace(line.charAt(i))) {
          splitPoint = i + 1;
          break;
        }
      }
    }
    return new String[] { line.substring(0, splitPoint), line.substring(splitPoint) };
  }

  private void doWrite(String message) throws IOException {
    String lines[] = StringUtils.split(message);
    for (int i = 0; i < lines.length; i++) {
      int lineLength = lines[i].length();
      // Message ended in newline: stop processing now so that addPrefix, addIndent won't be called
      if ((i == (lines.length - 1)) && (lineLength == 0)) {
        break;
      }
      // If at start of line: do prefix and indentation
      if (this.nextOutputColumn == 0) {
        if (this.indentPrefix) {
          this.addIndent();
          this.addPrefix(i == 0);
        }
        else {
          this.addPrefix(i == 0);
          this.addIndent();
        }
      }
      // Write text:
      this.addText(lines[i]);
      // If current line ended with newline (see StringUtils.split for details)
      if ((i + 1) < lines.length) {
        this.actualWriter.write(EnvironmentUtils.getLineSeparator());
        this.currentLine += 1;
        this.nextOutputColumn = 0;
      }
    }
    if (!message.isEmpty() && this.autoFlushEnabled) {
      this.actualWriter.flush();
    }
  }

  /**
   *  Write the given message, after formatting.
   *
   *  @param messageOrFormat A message, possible with format specifiers as are allowed in {@link String#format(String, Object...)}.
   *  @param details Any formatting arguments.
   *
   *  @throws IOException If writing failed.
   */
  public synchronized void write(String messageOrFormat, Object... details) throws IOException {
    if (details.length > 0) {
      this.doWrite(String.format(messageOrFormat, details));
    }
    else {
      this.doWrite(messageOrFormat);
    }
  }

  /**
   *  Write the given message, after formatting and add an additional newline.
   *
   *  @param messageOrFormat A message, possible with format specifiers as are allowed in {@link String#format(String, Object...)}.
   *  @param details Any formatting arguments.
   *
   *  @throws IOException If writing failed.
   */
  public synchronized void writeln(String messageOrFormat, Object... details) throws IOException {
    if (details.length > 0) {
      this.write(messageOrFormat + EnvironmentUtils.getLineSeparator(), details);
    }
    else {
      this.doWrite(messageOrFormat + EnvironmentUtils.getLineSeparator());
    }
  }

  /**
   *  Write a single newline character (which can actually be two characters once written).
   *
   *  @throws IOException If writing failed.
   */
  public synchronized void writeln() throws IOException {
    this.doWrite(EnvironmentUtils.getLineSeparator());
  }

  // </editor-fold>
  // <editor-fold desc="Writer Implementation">

  /**
   *  @see java.io.Writer#write(String)
   *
   *  @throws IOException If writing failed.
   */
  @Override
  public synchronized void write(String str) throws IOException {
    this.doWrite(str);
  }

  /**
   *  @see java.io.Writer#write(char[], int, int)
   *
   *  @throws IOException If writing failed.
   */
  @Override
  public synchronized void write(char[] cbuf, int off, int len) throws IOException {
    this.doWrite(new String(cbuf, off, len));
  }

  /**
   *  @see java.io.Writer#close()
   *
   *  @throws IOException If writing failed.
   */
  @Override
  public synchronized void close() throws IOException {
    this.actualWriter.close();
  }

  /**
   *  @see java.io.Writer#flush()
   *
   *  @throws IOException If writing failed.
   */
  @Override
  public synchronized void flush() throws IOException {
    this.actualWriter.flush();
  }

  // </editor-fold>
  // <editor-fold desc="Human-readable Representation">

  @Override
  public synchronized String toString() {
    return this.actualWriter.toString();
  }

  // </editor-fold>
}
