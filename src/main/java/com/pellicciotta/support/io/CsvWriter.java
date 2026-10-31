// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.io;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pellicciotta.support.utils.Contract;
import com.pellicciotta.support.utils.EnvironmentUtils;
import com.pellicciotta.support.utils.StringConversionUtils;

/**
 * A {@link Writer} wrapper-class for writing CSV data records, while ensuring proper escaping of embedded delimiter (and quote chars).
 * <p>
 * Based approximately on descriptions found in:<ul>
 *   <li><a href="https://tools.ietf.org/html/rfc4180">RFC41800</a></li>
 *   <li><a href="http://super-csv.github.io/super-csv/csv_specification.html">CSV Specification</a></li></ul>
 */
public class CsvWriter {
  // TODO: implementation idea: have special header with types of individual fields, so that on read, we can already parse back into correct type

  // <editor-fold desc="Defaults">

  /** The default field delimiter */
  public final static char DEFAULT_DELIMITER = ';';
  /** The default field quote delimiter */
  public final static char DEFAULT_QUOTE = '"';

  // </editor-fold>
  // <editor-fold desc="Construction">

  /**
   * Create a writer that writes fields (separated by DEFAULT_DELIMITER) inside records (separated by newline).
   *
   * @param dataSink The csv data sink.
   */
  public CsvWriter(Writer dataSink) throws IllegalArgumentException {
    this(CsvWriter.DEFAULT_DELIMITER, CsvWriter.DEFAULT_QUOTE, dataSink);
  }

  /**
   * Create a writer that writes fields (separated by delim) inside records (separated by newline).
   *
   * @param delim The field delimiter. Must not be a quote (' or ") or a backslash (\) character.
   * @param dataSink The csv data sink
   *
   * @throws IllegalArgumentException When delim is invalid.
   */
  public CsvWriter(char delim, Writer dataSink) {
    this(delim, CsvWriter.DEFAULT_QUOTE, dataSink);
  }

  /**
   * Create a writer that writes fields (separated by delim) inside records (separated by newline).
   *
   * @param delim The field delimiter. Must not be a quote (' or ") or a backslash (\) character.
   * @param quote The quote character that will be used for quoting fields. Must be ' or ".
   * @param dataSink The csv data sink
   *
   * @throws IllegalArgumentException When delim is invalid.
   */
  public CsvWriter(char delim, char quote, Writer dataSink) {
    Contract.isNotNull("dataSink", dataSink);
    if ((delim == '"') || (delim == '\\') || (delim == '"')) {
      throw new IllegalArgumentException("The delimiter must not be a quote or backslash");
    }
    this.delimiter = delim;
    if ((quote != '"') && (quote != '\'')) {
      throw new IllegalArgumentException("The quote must be either a single or double quote character but not '" + quote + "'");
    }
    this.delimiter = delim;
    this.quote = quote;
    this.dataSink = dataSink;
    this.currentRecordFields = new ArrayList<>();
    this.recordsWritten = 0;
  }

  private Writer dataSink;
  private char delimiter;
  private char quote;
  private boolean autoFlush;
  final private List<String> currentRecordFields;
  private int recordsWritten;

  // </editor-fold>
  // <editor-fold desc="Properties">

  /**
   * @return The underlying datasink being used.
   */
  public Writer getDataSink() {
    return this.dataSink;
  }

  /**
   * @return The field delimiter.
   */
  public char getDelimiter() {
    return this.delimiter;
  }

  /**
   * @return The field quote character.
   */
  public char getQuote() {
    return this.quote;
  }

  /**
   * @return Whether every writeNext will lead to a flush on the underlying dataSink.
   */
  public boolean isAutoFlush() {
    return this.autoFlush;
  }

  /**
   * @param autoFlush Whether every writeNext will lead to a flush on the underlying dataSink.
   */
  public void setAutoFlush(boolean autoFlush) {
    this.autoFlush = autoFlush;
  }

  // </editor-fold>
  // <editor-fold desc="Operations">

  /**
   * Escape a field value if needed, resulting in a string that can be directly written out.
   *
   * Escaping means:<ol>
   *   <li>The field value gets surrounded by quotes</li>
   *   <li>Any quote characters will get doubled</li>
   *   <li>Any \r or \n will be changed to a backslash character followed by the letter 'r' or 'n'</li>
   * </ol>
   * When needed:<ul>
   *   <li>If it contains the delimiter or quote character</li>
   *   <li>If it starts with an equal sign (for Excel!)</li>
   *   <li>If it contains \r or \n characters</li>
   * </ul>
   *
   * @param fieldValue The field value to be escaped, if needed.
   * @return The fieldValue as it will be written out, either unaltered, or escaped if needed.
   */
  public String escapeFieldValue(String fieldValue) {
    Contract.isNotNull("fieldValue", fieldValue);
    // Escaping needed? Yes if embedded delimiter or when starting with a quote or equal sign
    if ((fieldValue.indexOf('\n') >= 0)
        || (fieldValue.indexOf('\r') >= 0)
        || (fieldValue.indexOf(this.delimiter) >= 0)
        || (fieldValue.indexOf(this.quote) >= 0)
        || (fieldValue.startsWith("="))) {
      StringBuilder escapedField = new StringBuilder();
      escapedField.append(this.quote);
      for (char c : fieldValue.toCharArray()) {
        if (c == this.quote) { // Double for escaping
          escapedField.append(c);
          escapedField.append(c);
        }
        else if (c == '\r') {
          escapedField.append("\\r");
        }
        else if (c == '\n') {
          escapedField.append("\\n");
        }
        else {
          escapedField.append(c);
        }
      }
      escapedField.append(this.quote);
      return escapedField.toString();
    }
    else {
      return fieldValue;
    }
  }

  /**
   *  Add a field to be written with the next record (i.e. with the next {@link #writeNext(String...)}).
   *
   *  @param field The field value to be written. If <code>null</code>, an empty string will get written.
   */
  public void addField(String field) {
    this.currentRecordFields.add(field == null ? "" : this.escapeFieldValue(field));
  }

  /**
   *  Add a field to be written with the next record (i.e. with the next {@link #writeNext(String...)}).
   *
   *  @param field The field value to be written. If <code>null</code>, an empty string will get written.
   */
  public void addField(boolean field) {
    this.currentRecordFields.add(this.escapeFieldValue(StringConversionUtils.fromBoolean(field)));
  }

  /**
   *  Add a field to be written with the next record (i.e. with the next {@link #writeNext(String...)}).
   *
   *  @param field The field value to be written. If <code>null</code>, an empty string will get written.
   */
  public void addField(Boolean field) {
    this.currentRecordFields.add(field == null ? "" : this.escapeFieldValue(StringConversionUtils.fromBoolean(field)));
  }

  /**
   *  Add a field to be written with the next record (i.e. with the next {@link #writeNext(String...)}).
   *
   *  @param field The field value to be written. If <code>null</code>, an empty string will get written.
   */
  public void addField(int field) {
    this.currentRecordFields.add(this.escapeFieldValue("" + field));
  }

  /**
   *  Add a field to be written with the next record (i.e. with the next {@link #writeNext(String...)}).
   *
   *  @param field The field value to be written. If <code>null</code>, an empty string will get written.
   */
  public void addField(Integer field) {
    this.currentRecordFields.add(field == null ? "" : this.escapeFieldValue(StringConversionUtils.fromInt(field)));
  }

  /**
   *  Add a field to be written with the next record (i.e. with the next {@link #writeNext(String...)}).
   *
   *  @param field The field value to be written. If <code>null</code>, an empty string will get written.
   */
  public void addField(long field) {
    this.currentRecordFields.add(this.escapeFieldValue("" + field));
  }

  /**
   *  Add a field to be written with the next record (i.e. with the next {@link #writeNext(String...)}).
   *
   *  @param field The field value to be written. If <code>null</code>, an empty string will get written.
   */
  public void addField(Long field) {
    this.currentRecordFields.add(field == null ? "" : this.escapeFieldValue(StringConversionUtils.fromLong(field)));
  }

  /**
   *  Add a field to be written with the next record (i.e. with the next {@link #writeNext(String...)}).
   *
   *  @param field The field value to be written. If <code>null</code>, an empty string will get written.
   */
  public void addField(double field) {
    this.currentRecordFields.add(this.escapeFieldValue(StringConversionUtils.fromDouble(field)));
  }

  /**
   *  Add a field to be written with the next record (i.e. with the next {@link #writeNext(String...)}).
   *
   *  @param field The field value to be written. If <code>null</code>, an empty string will get written.
   */
  public void addField(Double field) {
    this.currentRecordFields.add(field == null ? "" : this.escapeFieldValue(StringConversionUtils.fromDouble(field)));
  }

  /**
   *  Add a field to be written with the next record (i.e. with the next {@link #writeNext(String...)}).
   *
   *  @param field The field value to be written. If <code>null</code>, an empty string will get written.
   */
  public void addField(BigDecimal field) {
    this.currentRecordFields.add(field == null ? "" : this.escapeFieldValue(StringConversionUtils.fromBigDecimal(field)));
  }

  /**
   *  Add a field to be written with the next record (i.e. with the next {@link #writeNext(String...)}).
   *
   *  @param field The field value to be written. If <code>null</code>, an empty string will get written.
   */
  public void addField(Charset field) {
    this.currentRecordFields.add(field == null ? "" : this.escapeFieldValue(StringConversionUtils.fromCharset(field)));
  }

  /**
   *  Add a field to be written with the next record (i.e. with the next {@link #writeNext(String...)}).
   *
   *  @param field The field value to be written. If <code>null</code>, an empty string will get written.
   */
  public void addField(byte[] field) {
    this.currentRecordFields.add(field == null ? "" : this.escapeFieldValue(StringConversionUtils.fromByteArray(field)));
  }

  /**
   *  Add a field to be written with the next record (i.e. with the next {@link #writeNext(String...)}).
   *
   *  @param field The field value to be written. If <code>null</code>, an empty string will get written.
   */
  public void addField(Date field) {
    this.currentRecordFields.add(field == null ? "" : this.escapeFieldValue(StringConversionUtils.fromDateTime(field)));
  }

  /**
   *  Add a field to be written with the next record (i.e. with the next {@link #writeNext(String...)}).
   *
   *  @param field The field value to be written. If <code>null</code>, an empty string will get written.
   */
  public void addField(Enum field) {
    this.currentRecordFields.add(field == null ? "" : this.escapeFieldValue(StringConversionUtils.fromEnum(field)));
  }

  /**
   *  Add a field to be written with the next record (i.e. with the next {@link #writeNext(String...)}).
   *
   *  @param field The field value to be written. If <code>null</code>, an empty string will get written.
   */
  public void addField(Class<?> field) {
    this.currentRecordFields.add(field == null ? "" : this.escapeFieldValue(StringConversionUtils.fromClass(field)));
  }

  /**
   * Write the next record (i.e. text line) making sure fields get delimited and properly quoted.
   * <p>
   * If a field contains the delimiter, a carriage return or newline char or it starts with a quote or equal-sign character,
   * it will be enclosed in quotes and any embedded quote characters, escape chars, newline and carriage return chars will be properly escaped:
   * </p><ul>
   *   <li><pre> " becomes: "" </pre> if " is the quote character</li>
   *   <li><pre> \ becomes: \\ </pre> if ' is the quote character</li>
   *   <li><pre> nl becomes: \n </pre></li>
   *   <li><pre> cr becomes: \r </pre></li></ul>
   *
   * @param fields The fields to be written out, after any other fields that have been added via {@link #addField(String)}.
   *
   * @return The line that was written out, excluding the line terminator.
   *
   * @throws IOException when there is an issue with the underlying data sink.
   */
  public String writeNext(String... fields) throws IOException {
    // Add additional fields, possibly escaping them:
    for (int i = 0; i < fields.length; i++) {
      this.addField(fields[i]);
    }
    // Now assemble them into a record:
    StringBuilder record = new StringBuilder();
    for (int i = 0; i < this.currentRecordFields.size(); i++) {
      if (i > 0) {
        record.append(this.delimiter);
      }
      record.append(this.currentRecordFields.get(i));
    }
    this.currentRecordFields.clear();
    // Write out the record:
    String recordLine = record.toString();
    if (recordsWritten != 0) {
      this.dataSink.write(EnvironmentUtils.getLineSeparator() + recordLine);
    }
    else {
      this.dataSink.write(recordLine);
    }
    this.recordsWritten += 1;
    if (this.autoFlush) {
      this.dataSink.flush();
    }
    return recordLine;
  }

  /**
   * Flush the wrapped writer.
   *
   * @throws IOException If flushing fails due to an IO error.
   */
  public void flush() throws IOException {
    this.dataSink.flush();
  }

  /**
   * Close the wrapped writer.
   *
   * @throws IOException If closing fails due to an IO error.
   */
  public void close() throws IOException {
    this.dataSink.close();
  }

  @Override
  public String toString() {
    return this.dataSink.toString();
  }

  // </editor-fold>
}
