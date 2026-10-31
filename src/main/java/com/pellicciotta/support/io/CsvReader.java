// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.io;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.pellicciotta.support.utils.Contract;

/**
 * A {@link Reader} wrapper for reading CSV data records.
 * <p>
 * Based approximately on descriptions found in:<ul>
 *   <li><a href="https://tools.ietf.org/html/rfc4180">RFC41800</a></li>
 *   <li><a href="http://super-csv.github.io/super-csv/csv_specification.html">CSV Specification</a></li></ul>
 */
public class CsvReader {
  // <editor-fold desc="Defaults">

  /** The default field delimiter */
  public final static char DEFAULT_DELIMITER = ';';
  /** The default field quote delimiter */
  public final static char DEFAULT_QUOTE = '"';

  // </editor-fold>
  // <editor-fold desc="Construction">

  /**
   * Create a reader that reads fields (separated by DEFAULT_DELIMITER) inside records (separated by newline).
   *
   * @param dataSource The csv data source.
   */
  public CsvReader(Reader dataSource) throws IllegalArgumentException {
    this(CsvReader.DEFAULT_DELIMITER, CsvReader.DEFAULT_QUOTE, dataSource);
  }

  /**
   * Create a reader that reads fields (separated by delim) inside records (separated by newline).
   *
   * @param delim The field delimiter. Must not be a quote (' or ") or a backslash (\) character.
   * @param dataSource The csv data source.
   *
   * @throws IllegalArgumentException When delim is invalid.
   */
  public CsvReader(char delim, Reader dataSource) {
    this(delim, CsvReader.DEFAULT_QUOTE, dataSource);
  }

  /**
   * Create a reader that reads fields (separated by delim) inside records (separated by newline).
   *
   * @param delim The field delimiter. Must not be a quote (' or ") or a backslash (\) character.
   * @param quote The quote character that will be used for recognizing quoted fields. Must be ' or ".
   * @param dataSource The csv data source.
   *
   * @throws IllegalArgumentException When delim is invalid.
   */
  public CsvReader(char delim, char quote, Reader dataSource) {
    Contract.isNotNull("dataSource", dataSource);
    if ((delim == '"') || (delim == '\\') || (delim == '"')) {
      throw new IllegalArgumentException("The delimiter must not be a quote or backslash");
    }
    this.delimiter = delim;
    if ((quote != '"') && (quote != '\'')) {
      throw new IllegalArgumentException("The quote must be either a single or double quote character but not '" + quote + "'");
    }
    this.quote = quote;
    if (dataSource instanceof BufferedReader) {
      this.dataSource = (BufferedReader) dataSource;
    }
    else {
      this.dataSource = new BufferedReader(dataSource);
    }
  }

  private BufferedReader dataSource;
  private char delimiter;
  private char quote;
  private String nextLine;

  // </editor-fold>
  // <editor-fold desc="Properties">

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

  // </editor-fold>
  // <editor-fold desc="Operations">

  /**
   * @return Whether there is a next record to read (i.e. whether readNext will return non-null results)
   *
   * @throws IOException If reading fails due to an IO error
   */
  public boolean hasNext() throws IOException {
    if (this.nextLine != null) {
      return true;
    }
    this.nextLine = this.dataSource.readLine();
    return (this.nextLine != null);
  }

  /**
   *  Read the next record (i.e. text line) and split in into fields delimited by delimiter.
   *  <p>
   *  Will take care of the delimiter being part of a field's value too if the
   *  field contents are quoted (surrounded by the configured quote character).
   *  <p>
   *  Note that, for a quoted field:<ul>
   *    <li>pairs of the configured quote character (e.g. "" or '') will be interpreted
   *        as meaning one single quote</li>
   *    <li>a backslash followed by r will be interpreted as a carriage return character (\r)</li>
   *    <li>a backslash followed by n will be interpreted as a newline character (\n)</li>
   *  </ul>
   *  The begin and end quote itself will not be considered part of the field value.
   *  <p>
   *  An empty line will be interpreted as a single, empty field.
   *  <p>
   *  A line contain a single delimiter char will be interpreted as two empty fields
   *
   *  @return The fields found in the next record, or null if there is no next record.
   *
   *  @throws IOException when there is an issue with the underlying data source or when the CSV data is somehow malformed.
   */
  public String[] readNext() throws IOException {
    String line = this.nextLine;
    this.nextLine = null;
    if (line == null) {
      line = this.dataSource.readLine();
    }
    // Special case: end of stream
    if (line == null) {
      return null;
    }
    int lineLength = line.length();
    // Special case: empty line indicates a single empty field
    if (lineLength == 0) {
      String[] fields = { "" };
      return fields;
    }
    List<String> fields = new ArrayList<>();
    for (int i = 0; i < lineLength; i++) {
      // Read next field
      char c = line.charAt(i);
      if (c == this.delimiter) { // Empty ;;
        fields.add("");
      }
      else if (c == this.quote) { // Quoted field
        char quote = c;
        StringBuilder field = new StringBuilder();
        int j = i + 1;
        for (; j < lineLength; j++) {
          c = line.charAt(j);
          if (c == '\\') { // Escape char
            if ((j + 1) < lineLength) {
              char nextC = line.charAt(j + 1);
              if (nextC == 'n') {
                field.append('\n');
                j = j + 1;
              }
              else if (nextC == 'r') {
                field.append('\r');
                j = j + 1;
              }
              else { // No special escape: add the escape char
                field.append(c);
              }
            }
          }
          else if (c == quote) {
            if ((j + 1) < lineLength) {
              char nextC = line.charAt(j + 1);
              if (nextC == quote) { // Doubled quote, so don't add the escape char itself
                field.append(nextC);
                j = j + 1;
              }
              else if (nextC == this.delimiter) {
                break; // Not escaped, so end of field
              }
              else {
                field.append(c); // If not delimiter, keep quote as part of the value (although this should not happen!)
              }
            }
          }
          else {
            field.append(c);
          }
        }
        fields.add(field.toString());
        i = j + 1;
      }
      else { // Normal, unquoted field
        int j = i + 1;
        while ((j < lineLength) && (line.charAt(j) != this.delimiter)) {
          j++;
        }
        fields.add(line.substring(i, j));
        i = j;
      }
      // Skip delimiter
      if (i < lineLength) {
        c = line.charAt(i);
        if (c != this.delimiter) {
          throw new IOException(
              String.format("Failed to read next field value: expecting a delimiter (%c) at position %d but found (%c) instead",
                  this.delimiter, i, c));
        }
      }
    }
    if (line.charAt(lineLength - 1) == this.delimiter) { // If record ends with a delimiter, add empty field
      fields.add("");
    }
    return fields.toArray(new String[0]);
  }

  /**
   *  Close the wrapped reader.
   *
   *  @throws IOException If reading fails due to an IO error
   */
  public void close() throws IOException {
    this.dataSource.close();
  }

  @Override
  public String toString() {
    return this.dataSource.toString();
  }

  // </editor-fold>
}
