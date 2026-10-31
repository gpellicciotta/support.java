// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pellicciotta.support.text.TextBuilder;

/**
 * A collection of String related utility methods.
 */
public final class StringUtils {
  /** Prevent instantiation */
  private StringUtils() { }

  /**
   *  Alias for {@link String#format(String, Object...)}.
   *
   *  @param messageOrFormat Message format.
   *  @param messageParams Message format arguments.
   *
   *  @return The formatting result string.
   */
  static String str(String messageOrFormat, Object... messageParams) {
    return String.format(messageOrFormat, messageParams);
  }

  /**
   *  Create a string that is the repetition of a specific character.
   *
   *  @param ch The character to repeat.
   *  @param desiredLength The desired string length. An empty string will be returned if &lt;= 0.
   *
   *  @return A string of the indicated length that consists solely of repetitions of the same character.
   */
  static String repeated(char ch, int desiredLength) {
    if (desiredLength <= 0) {
      return "";
    }
    char[] str = new char[desiredLength];
    for (int i = 0; i < desiredLength; i += 1) {
      str[i] = ch;
    }
    return new String(str);
  }

  /**
   *  Create a string that is the repetition of a specific other string.
   *
   *  @param str The string to repeat.
   *  @param desiredLength The desired string length. An empty string will be returned if &lt;= 0.
   *
   *  @return A string of the indicated length that consists solely of repetitions of the same string.
   *          It is possible that the last (or only) repetition doesn't contain the full str value.
   */
  static String repeated(String str, int desiredLength) {
    Contract.isNotNull("str", str);
    if (desiredLength <= 0) {
      return "";
    }
    if (desiredLength == str.length()) {
      return str;
    }
    if (desiredLength < str.length()) {
      return str.substring(0, desiredLength);
    }
    StringBuilder sb = new StringBuilder();
    int len = 0;
    while (len < desiredLength) {
      sb.append(str);
    }
    String result = sb.toString();
    if (result.length() > desiredLength) {
      return result.substring(0, desiredLength);
    }
    return result;
  }

  /**
   *  Get the last part of a string, the last maxChars characters.
   *
   *  @param str The string to get the last part of.
   *  @param maxChars The max. number of characters to return.
   *  @return The last part of val, max. maxChars characters but possibly less if val.length() &lt; maxChars.
   */
  static String reverseSubstring(String str, int maxChars) {
    Contract.isNotNull("str", str);
    int actualLen = str.length();
    if (actualLen <= maxChars) {
      return str;
    }
    return str.substring(actualLen - maxChars);
  }

  /**
   * Return a copy of sourceString in which all occurrences of originalValue are replaced by replacementValue.
   *
   * @param sourceString The String from which parts should be replaced.
   * @param originalValue The substring value to be replaced.
   * @param replacementValue The replacement string for all occurrences of originalValue in sourceString.
   *
   * @return A copy of sourceString after all replacements have been done.
   */
  public static String replaceAll(String sourceString, String originalValue, String replacementValue){
    Contract.isNotNull("sourceString", sourceString);
    Contract.isNotNull("originalValue", originalValue);
    Contract.isNotNull("replacementValue", replacementValue);
    StringBuilder result = new StringBuilder();
    int c = 0;
    for (c = 0; c < ((sourceString.length() - originalValue.length()) + 1); c += 1) {
      if (sourceString.substring(c, c + originalValue.length()).equals(originalValue)) {
        result.append(replacementValue);
        c += originalValue.length() - 1;
      }
      else {
        result.append(sourceString.charAt(c));
      }
    }
    result.append(sourceString.substring(c));
    return result.toString();
  }

  /**
   * Make a String of the specified length, containing spaces only.
   *
   * @param length Length of string to be created.
   *
   * @return A String of the specified length, containing spaces only.
   */
  public static String makeSpacesString(int length) {
    return makeCharacterRepetitionString(' ', length);
  }

  /**
   * Replace all unprintable characters in the specified string with a printable representation.
   * All java escape characters as documented http://docs.oracle.com/javase/tutorial/java/data/characters.html
   * and whose representation is unprintable, will get represented as a backslash followed by the escape character:
   *   Tab                              : backslash t
   *   Backspace                        : backslash b
   *   Newline                          : backslash n
   *   Carriage return                  : backslash r
   *   Formfeed                         : backslash f
   * Any other non-printable character will be represented as: backslash u &lt;four-hex-digits&gt;
   *
   * @param str String to be made printable.
   *
   * @return A copy of the specified string but with all unprintable characters replaced.
   */
  public static String makePrintable(String str){
    Contract.isNotNull("str", str);

    StringBuilder chars = new StringBuilder(str.length());
    for (char ch : str.toCharArray()) {
      switch (ch) {
        case '\t':
          chars.append("\\t");
          break;
        case '\b':
          chars.append("\\b");
          break;
        case '\r':
          chars.append("\\r");
          break;
        case '\n':
          chars.append("\\n");
          break;
        case '\f':
          chars.append("\\f");
          break;

        default:
          if (Character.isISOControl(ch) || (Character.isWhitespace(ch) && (ch != ' '))) {
            chars.append(String.format("\\u%04x", (int) ch));
          }
          else {
            chars.append(ch);
          }
      }
    }
    return chars.toString();
  }

  /**
   * Replace all unprintable characters in the specified string with a substitution character.
   *
   * @param str String to be made printable.
   * @param substitutionCharacter Character to be used in place of unprintable characters.
   *
   * @return A copy of the specified string but with all unprintable characters replaced by the substitution character.
   */
  public static String makePrintable(String str, char substitutionCharacter){
    Contract.isNotNull("str", str);

    char[] chars = new char[str.length()];
    str.getChars(0, str.length(), chars, 0);
    for (int i = 0; i < chars.length; i++) {
      if (Character.isISOControl(chars[i]) || (Character.isWhitespace(chars[i]) && (chars[i] != ' ') && (chars[i] != '\t'))) {
        chars[i] = substitutionCharacter;
      }
    }
    return String.valueOf(chars);
  }

  /**
   * Make a String by adding the specified character ch length times to an empty String.
   *
   * @param length Length of string to be created.
   * @param ch Character to be used for creating the string.
   *
   * @return A String of the specified length, with all characters equal to ch.
   */
  public static String makeCharacterRepetitionString(char ch, int length){
    Contract.isInRange("length", length, 0, Integer.MAX_VALUE);
    char[] str = new char[length];
    for (int i = 0; i < length; i += 1) {
      str[i] = ch;
    }
    return new String(str);
  }

  /**
   * Determine the number of text lines in the specified string, assuming text lines are separated by the current platform specific
   * end-of-line characters.
   *
   * @param text The text to determine the line count for.
   *
   * @return The number of lines in the specified text.
   */
  public static int getLineCount(String text){
    return getLineCount(text, EnvironmentUtils.getLineSeparator());
  }

  /**
   * Determine the number of text lines in the specified string.
   *
   * @param text The text to determine the line count for.
   * @param lineDelimiter The line delimiter used in the text.
   *
   * @return The number of lines in the specified text. Zero when text is null.
   */
  static public int getLineCount(String text, String lineDelimiter){
    Contract.isNotNull("text", text);
    Contract.isNotNull("lineDelimiter", lineDelimiter);
    int lineCount = 0;
    int lineDelimiterPos = text.indexOf(lineDelimiter);
    do {
      lineCount += 1;
      lineDelimiterPos = text.indexOf(lineDelimiter, lineDelimiterPos + lineDelimiter.length());
    } while (lineDelimiterPos >= 0);
    return lineCount;
  }

  /**
   * Create a new string by using the specified delimiter to join all individual pieces.
   *
   * @param delimiter The delimiter to use between all pieces. Must not be null.
   * @param parts The parts to be joined. May be empty but not null.
   *
   * @return A new string formed by concatenating all parts while ensuring there is a single delimiter between all parts.
   *         If there were no parts, the empty string is returned.
   */
  public static String join(String delimiter, String... parts){
    Contract.isNotNull("delimiter", delimiter);
    Contract.isNotNull("parts", parts);
    if (parts.length == 0) {
      return "";
    }
    if (parts.length == 1) {
      return parts[0];
    }
    StringBuilder sb = new StringBuilder();
    sb.append(parts[0]);
    for (int i = 1; i < parts.length; i++) {
      sb.append(delimiter);
      sb.append(parts[i]);
    }
    return sb.toString();
  }

  /** Overload of {@link #join(String, String...)} */
  public static String join(String delimiter, Collection<String> parts){
    Contract.isNotNull("delimiter", delimiter);
    Contract.isNotNull("parts", parts);
    if (parts.isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (String part : parts) {
      if (first) {
        first = false;
      }
      else {
        sb.append(delimiter);
      }
      sb.append(part);
    }
    return sb.toString();
  }

  /**
   * Split the specified string into pieces that are delimited by line separators in the original string.
   *
   * @param str String to be split.
   *
   * @return The pieces into which the string could be split. This pieces will not include the line separators used to demarcate the
   *         pieces. If no line separator can be found in the original string, only one piece will be returned: the original string
   *         as a whole. If the original string ended with a line separator, the last returned piece will be an empty string. If the
   *         original string started with a line separator, the first returned piece will be an empty string.
   */
  public static String[] split(String str){
    return split(str.replaceAll("\r?\n", "\n"), "\n");
  }

  /**
   * Split the specified string into pieces that are delimited by line separators in the original string.
   *
   * @param str String to be split.
   * @param sep The line delimiter to be used for splitting.
   *
   * @return The pieces into which the string could be split. This pieces will not include the line separators used to demarcate the
   *         pieces. If no line separator can be found in the original string, only one piece will be returned: the original string
   *         as a whole. If the original string ended with a line separator, the last returned piece will be an empty string. If the
   *         original string started with a line separator, the first returned piece will be an empty string.
   */
  public static String[] split(String str, String sep){
    Contract.isNotNull("str", str);
    Contract.isNotNull("sep", sep);
    ArrayList<String> lines = new ArrayList<>();
    int prevIndex = 0;
    int index = 0;
    for (index = str.indexOf(sep, prevIndex); index != -1; index = str.indexOf(sep, prevIndex)) {
      lines.add(str.substring(prevIndex, index));
      prevIndex = index + sep.length();
    }
    lines.add(str.substring(prevIndex));
    return lines.toArray(new String[0]);
  }

  /**
   * Prefix as many space characters to the string as needed to make it the specified size.
   *
   * If the string already has or supersedes the specified size, nothing will be done.
   *
   * @param str String to be padded with space characters.
   * @param size The desired result size.
   *
   * @return The padded string with will either equal the specified string or be padded at the left with spaces, making it the
   *         specified length.
   */
  public static String padLeft(String str, int size) {
    return padLeft(str, size, ' ');
  }

  /**
   * Prefix as many pad characters to the string as needed to make it the specified size.
   *
   * If the string already has or supersedes the specified size, nothing will be done.
   *
   * @param str String to be padded with pad characters.
   * @param size The desired result size.
   * @param padCharacter The pad character to be used.
   *
   * @return The padded string with will either equal the specified string or be padded at the left with pad characters, making it
   *         the specified length.
   */
  public static String padLeft(String str, int size, char padCharacter) {
    Contract.isNotNull("str", str);
    Contract.isInRange("size", size, 0, Integer.MAX_VALUE);
    String paddedString = str;
    if (str.length() < size) {
      paddedString = makeCharacterRepetitionString(padCharacter, size - str.length()) + str;
    }
    return paddedString;
  }

  /**
   * Prefix as many pad characters to the string as needed to make it the specified size, or truncate from the left.
   *
   * If the string already has or supersedes the specified size, nothing will be done.
   *
   * @param str String to be padded with pad characters or truncated to the desired size.
   * @param size The desired result size.
   * @param padCharacter The pad character to be used.
   *
   * @return The padded or truncated string which will be of the desired size.
   */
  public static String padOrTruncateLeft(String str, int size, char padCharacter) {
    Contract.isNotNull("str", str);
    Contract.isInRange("size", size, 0, Integer.MAX_VALUE);
    String paddedString = str;
    int strLen = str.length();
    if (strLen < size) {
      paddedString = makeCharacterRepetitionString(padCharacter, size - strLen) + str;
    }
    else if (strLen > size) {
      paddedString = paddedString.substring(strLen - size);
    }
    return paddedString;
  }

  /**
   * Suffix as many space characters to the string as needed to make it the specified size.
   *
   * If the string already has or supersedes the specified size, nothing will be done.
   *
   * @param str String to be padded with space characters.
   * @param size The desired result size.
   *
   * @return The padded string with will either equal the specified string or be padded at the right with spaces, making it the
   *         specified length.
   */
  public static String padRight(String str, int size) {
    return padRight(str, size, ' ');
  }

  /**
   * Suffix as many pad characters to the string as needed to make it the specified size.
   *
   * If the string already has or supersedes the specified size, nothing will be done.
   *
   * @param str String to be padded with pad characters.
   * @param size The desired result size.
   * @param padCharacter The pad character to be used.
   *
   * @return The padded string with will either equal the specified string or be padded at the right with pad characters, making it
   *         the specified length.
   */
  public static String padRight(String str, int size, char padCharacter) {
    Contract.isNotNull("str", str);
    Contract.isInRange("size", size, 0, Integer.MAX_VALUE);
    String paddedString = str;
    int strLen = str.length();
    if (strLen < size) {
      paddedString = str + makeCharacterRepetitionString(padCharacter, size - strLen);
    }
    return paddedString;
  }

  /**
   * Suffix as many pad characters to the string as needed to make it the specified size, or truncate from the right.
   *
   * If the string already has or supersedes the specified size, it will be truncated.
   *
   * @param str String to be padded with pad characters or truncated to the desired size.
   * @param size The desired result size.
   * @param padCharacter The pad character to be used.
   *
   * @return The padded or truncated string which will be of the desired size.
   */
  public static String padOrTruncateRight(String str, int size, char padCharacter) {
    Contract.isNotNull("str", str);
    Contract.isInRange("size", size, 0, Integer.MAX_VALUE);
    String paddedString = str;
    int strLen = str.length();
    if (strLen < size) {
      paddedString = str + makeCharacterRepetitionString(padCharacter, size - strLen);
    }
    else if (strLen > size) {
      paddedString = paddedString.substring(0, size);
    }
    return paddedString;
  }

  /**
   * Change capitalization as follows: first char will be uppercase, all others lowercase.
   *
   * @param str String to be capitalized.
   *
   * @return The capitalized string.
   */
  public static String capitalize(String str) {
    Contract.isNotNull("str", str);
    if (str.length() < 1) {
      return str;
    }
    else if (str.length() < 2) {
      return str.toUpperCase();
    }
    else {
      return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
  }

  /**
   * Change capitalization as follows: first char will be upper-case, all others as-is.
   *
   * @param str String to be capitalized.
   *
   * @return The string with first letter ensured to be upper-case.
   */
  public static String startWithUpperCase(String str) {
    Contract.isNotNull("str", str);
    if (str.length() < 1) {
      return str;
    }
    else if (str.length() < 2) {
      return str.toUpperCase();
    }
    else {
      return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
  }

  /**
   * Change capitalization as follows: first char will be lower-case, all others as-is.
   *
   * @param str String to be capitalized.
   *
   * @return The string with first letter ensured to be lower-case.
   */
  public static String startWithLowerCase(String str) {
    Contract.isNotNull("str", str);
    if (str.length() < 1) {
      return str;
    }
    else if (str.length() < 2) {
      return str.toLowerCase();
    }
    else {
      return str.substring(0, 1).toLowerCase() + str.substring(1);
    }
  }

  /**
   * Trim all whitespace chars (as determined by Character.isWhitespacE) from the text's left end.
   *
   * @param text The text to be trimmed at the left.
   *
   * @return The trimmed version of text, which could be empty.
   */
  public static String trimLeft(String text){
    Contract.isNotNull("text", text);
    int i = 0;
    for (; i < text.length(); i++) {
      if (!Character.isWhitespace(text.charAt(i))) {
        return text.substring(i);
      }
    }
    return text;
  }

  /**
   * Trim all whitespace chars (as determined by Character.isWhitespace) from the text's right end.
   *
   * @param text The text to be trimmed at the right.
   *
   * @return The trimmed version of text, which could be empty.
   */
  public static String trimRight(String text){
    Contract.isNotNull("text", text);
    int i = text.length() - 1;
    for (; i >= 0; i--) {
      if (!Character.isWhitespace(text.charAt(i))) {
        break;
      }
    }
    return text.substring(0, i + 1);
  }

  /**
   * Trim all current platform specific line delimiters from the text's right end.
   *
   * @param text The text to be trimmed.
   *
   * @return The trimmed version of text.
   */
  public static String trimRightEol(String text){
    return trimRightEol(text, EnvironmentUtils.getLineSeparator());
  }

  /**
   * Trim all line delimiters from the text's right end.
   *
   * @param text The text to determine the line count for.
   * @param lineDelimiter The line delimiter used in the text.
   *
   * @return The trimmed version of text.
   */
  static public String trimRightEol(String text, String lineDelimiter){
    Contract.isNotNull("text", text);
    Contract.isNotNull("lineDelimiter", lineDelimiter);
    String trimmed = text;
    for (int lastDelimPos = trimmed.lastIndexOf(lineDelimiter); (lastDelimPos >= 0)
        && (lastDelimPos == (trimmed.length() - lineDelimiter.length())); lastDelimPos = trimmed.lastIndexOf(lineDelimiter)) {
      trimmed = trimmed.substring(0, lastDelimPos);
    }
    return trimmed;
  }

  /**
   * Expand all tabs in the given text according to the provided tabSize. The expansion happens as follows: *) For each tab
   * character found in text, replace the tab character with a tabSize spaces instead.
   *
   * @param text Text to expand tabs in
   * @param tabSize The number of spaces to use for each tab
   *
   * @return The text with all tabs expanded.
   */
  static public String expandTabs(String text, int tabSize) {
    return expandTabs(text, tabSize, new int[0]).getFirst();
  }

  /**
   * Expand all tabs in the given text according to the provided tabStops. The expansion happens as follows: *) For each tab
   * character found in text, check whether the next index that would get used in the expanded text is smaller than a provided tab
   * stop: if so, append spaces to the expanded text in place of the tab character, until the expanded text's next insert position
   * will be equal to the tab stop *) If no tab stop exists that can be reached by appending spaces to the expanded text, replace
   * the tab character with a single space instead.
   *
   * @param text Text to expand tabs in
   * @param tabStops The tab stop positions, which should be 0-based indexes on a virtual line. Beyond the last tab stop, tabs will
   *          be replaced by a single space. It is assumed that tabStops are ordered according to increasing positions.
   *          If null, will be treated as if no tabStops were provided.
   *
   * @return The text with all tabs expanded.
   */
  static public String expandTabs(String text, int[] tabStops) {
    return expandTabs(text, 1, tabStops).getFirst();
  }

  /**
   * Expand all tabs in the given text according to the provided tabSize and tabStops. The expansion happens as follows: *) For each
   * tab character found in text, check whether the next index that would get used in the expanded text is smaller than a provided
   * tab stop: if so, append spaces to the expanded text in place of the tab character, until the expanded text's next insert
   * position will be equal to the tab stop *) If no tab stop exists that can be reached by appending spaces to the expanded text,
   * replace the tab character with tabSize spaces instead.
   *
   * @param text Text to expand tabs in
   * @param tabSize The number of spaces to use for each tab, after the last tabStop
   * @param tabStops The tab stop positions, which should be 0-based indexes on a virtual line. This array may be empty, in which
   *          case only the tabSize parameter will have immediate effect.
   *          If null, will be treated as if no tabStops were provided.
   *
   * @return A pair consisting of the text with all tabs expanded and a integer list that can be used to map index positions inside
   *         the returned (expanded) text to index positions in the original text.
   */
  static public Pair<String, List<Integer>> expandTabs(String text, int tabSize, int[] tabStops) {
    Contract.isNotNull("text", text);
    Contract.isNotNegative("tabSize", tabSize);
    if (tabStops == null) {
      tabStops = new int[0];
    }

    StringBuilder expandedText = new StringBuilder(text.length() * 2);
    List<Integer> origIndexes = new ArrayList<>(text.length() * 2);
    int logicalInsertIdx = 0;
    int lastUsedTabStopIdx = -1;
    int maxTabStopIdx = (tabStops.length > 0) ? tabStops[tabStops.length - 1] : -1;

    for (int i = 0; i < text.length(); i++) {
      char ch = text.charAt(i);
      if (ch != '\t') { // Normal chars: just add
        expandedText.append(ch);
        origIndexes.add(i);
        logicalInsertIdx += 1;
      }
      else if (logicalInsertIdx >= maxTabStopIdx) { // Beyond last applicable tab stop: tabs expand to n spaces
        for (int j = 0; j < tabSize; j++) {
          expandedText.append(' ');
          origIndexes.add(i);
          logicalInsertIdx += 1;
        }
      }
      else { // Find applicable tabStop
        int tabStopIdx = -1;
        for (int j = lastUsedTabStopIdx + 1; j < tabStops.length; j++) {
          if (logicalInsertIdx < tabStops[j]) {
            tabStopIdx = j;
            break;
          }
        }
        for (int j = logicalInsertIdx; j < tabStops[tabStopIdx]; j++) {
          expandedText.append(' ');
          origIndexes.add(i);
          logicalInsertIdx += 1;
        }
        lastUsedTabStopIdx = tabStopIdx;
      }
    }
    return Pair.of(expandedText.toString(), origIndexes);
  }

  // '${', followed by paramName, optionally followed by ':' and paramFormat, followed by '}'
  private static Pattern FORMAT_REPLACEMENTS_REGEX = Pattern.compile("[$][{]([^:}]+)(?:[:]([^}]+))?[}]");

  /**
   *  Format the provided formatString by replacing any parameters in the form
   *  of <code>${paramName}</code> or <code>${paramName:paramFormat}</code> with values found in the provided replacements.
   *  <p>
   *  Any <code>paramName</code> not found in the provided <code>replacements</code> will lead to the parameter remaining in the returned value.</p>
   *  <p>
   *  If found, the parameter syntax will be substituted either:<ol>
   *    <li>If the replacement value is null: by the empty string</li>
   *    <li>If there is no <code>paramFormat</code>: the stringified version of the replacement value, i.e. <code>replacements.get(name).toString()</code></li>
   *    <li>If there is a <code>paramFormat</code>: the formatted version of the replacement value, i.e. <code>String.format(paramFormat, replacements.get(name))</code></li>
   *  </ol>
   *  <p>
   *  The $ in the name is there to make the distinction with the older {@link #format} methods that search for parameters whose syntax also
   *  uses curly braces but without the leading dollar sign.</p>
   *  <p>
   *  NOTE: a future impl. will allow escaping the $-sign in order to not perform replacement but the current impl. doesn't allow this.<p>
   *
   *  @param formatString The string to search for parameters that can be replaced based on values from the replacements parameter.
   *  @param replacements Replacement values for parameters found in the formatString.
   *
   *  @return The result of replacing any known parameters in formatString with (formatted) replacement values.
   *          If either formatString or replacements is null, formatString will be returned.
   */
  public static String format$(String formatString, Properties replacements) {
    if ((formatString == null) || (replacements == null) || replacements.isEmpty() || (formatString.length() < 4) || !formatString.contains("${")) {
      return formatString;
    }
    StringBuilder sb = new StringBuilder();
    Matcher m = FORMAT_REPLACEMENTS_REGEX.matcher(formatString);
    while (m.find()) {
      String paramName = m.group(1);
      String paramFormat = m.group(2);
      if (replacements.containsKey(paramName)) {
        String replVal = "";
        Object paramVal = replacements.get(paramName);
        if (paramVal != null) {
          if (paramFormat != null) {
            replVal = String.format(paramFormat, paramVal);
          }
          else {
            replVal = paramVal.toString();
          }
        }
        m.appendReplacement(sb, Matcher.quoteReplacement(replVal));
      }
    }
    m.appendTail(sb);
    return sb.toString();
  }

  /**
   *  Format the provided formatString by replacing any parameters in the form
   *  of <code>${paramName}</code> or <code>${paramName:paramFormat}</code> with values found in the provided replacements.
   *  <p>
   *  Any <code>paramName</code> not found in the provided <code>replacements</code> will lead to the parameter remaining in the returned value.</p>
   *  <p>
   *  If found, the parameter syntax will be substituted either:<ol>
   *    <li>If the replacement value is null: by the empty string</li>
   *    <li>If there is no <code>paramFormat</code>: the stringified version of the replacement value, i.e. <code>replacements.get(name).toString()</code></li>
   *    <li>If there is a <code>paramFormat</code>: the formatted version of the replacement value, i.e. <code>String.format(paramFormat, replacements.get(name))</code></li>
   *  </ol>
   *  <p>
   *  The $ in the name is there to make the distinction with the older {@link #format} methods that search for parameters whose syntax also
   *  uses curly braces but without the leading dollar sign.</p>
   *  <p>
   *  NOTE: a future impl. will allow escaping the $-sign in order to not perform replacement but the current impl. doesn't allow this.<p>
   *
   *  @param formatString The string to search for parameters that can be replaced based on values from the replacements parameter.
   *  @param replacements Replacement values for parameters found in the formatString.
   *
   *  @return The result of replacing any known parameters in formatString with (formatted) replacement values.
   *          If either formatString or replacements is null, formatString will be returned.
   */
  public static String format$(String formatString, Map<String, ?> replacements) {
    if ((formatString == null) || (replacements == null) || replacements.isEmpty() || (formatString.length() < 4) || !formatString.contains("${")) {
      return formatString;
    }
    StringBuilder sb = new StringBuilder();
    Matcher m = FORMAT_REPLACEMENTS_REGEX.matcher(formatString);
    while (m.find()) {
      String paramName = m.group(1);
      String paramFormat = m.group(2);
      if (replacements.containsKey(paramName)) {
        Object paramVal = replacements.get(paramName);
        if (paramFormat != null) {
          paramVal = String.format(paramFormat, paramVal);
        }
        m.appendReplacement(sb, "" + paramVal);
      }
    }
    m.appendTail(sb);
    return sb.toString();
  }

  /**
   * Format and return a message string from the specified <code>formatString</code> and the specified <code>replacements</code>.
   * <p>
   * The <code>formatString</code> is a regular string which can contain format parameters.
   * Format parameters are defined as: <code>{parameter-name}</code>, that is: left-curly-brace, followed by a name, followed by a right-curly brace.</p>
   * <p>
   * A parameter name may consists of following characters: [0-9], [a-z], [A-Z], "_" and "-".</p>
   * <p>
   * To specify a literal text part containing the combination "left-curly-brace, number, right-curly-brace", that is not be
   * considered a format parameter, double the curly braces. Two consecutive curly braces (of the same kind) will always be
   * substituted by one curly brace and not considered part of a format parameter.</p>
   * <p>
   * The resulting string will be built by searching the <code>formatString</code> for format parameters. Every format parameter is
   * investigated in turn: the format parameter name is taken and the <code>replacements</code> are searched for an entry with the name as key.
   * If this entry exists, the entry's value is taken and this object's string value (determined by calling <code>toString</code> on the object
   * or "null" if the object is null) is used for replacing the format parameter under investigation. Format parameters that don't
   * have a corresponding <code>replacements</code> entry, are left untouched.</p>
   * <pre>
   *  Examples:
   *
   *    Pattern                      |  Formatting Map          | Result
   *    -----------------------------+--------------------------+-----------------------------------------------
   *    There is {i}.                | { ["i"] = "coffee" }     | There is coffee.
   *    There is {{i}}.              | { ["i"] = "coffee" }     | There is {i}.
   *    There is {i}}.               | { ["i"] = "coffee" }     | There is coffee}.
   *    There is {{{i}}}.            | { ["i"] = "coffee" }     | There is {coffee}.
   *    There is {{.                 | { ["i"] = "coffee" }     | There is {.
   *    There is {{{a}}.             | { ["i"] = "coffee" }     | There is {{a}.
   *    There is {i}.                | { }                      | There is {i}.
   *    There is {a}.                | { ["i"] = "coffee" }     | There is {a}.
   *    There is {i}, {i} and {i}.   | { ["i"] ="coffee" }      | There is coffee, coffee and coffee.
   *    There is {i2}, {i1} and {i3}.| { ["i1"] = "milk",
   *                                     ["i2"] = "coffee",
   *                                     ["i3"] = "sugar" }     | There is coffee, milk and sugar.
   * </pre>
   *
   * @param formatString The string, potentially containing format parameters, that is the basis for building the result.
   * @param replacements Formatting arguments to be used for format parameter substitution.
   *
   * @return A string built by taking the specified <code>formatString</code> and substituting any format parameters.
   */
  public static String format(String formatString, Properties replacements){
    Contract.isNotNull("formatString", formatString);
    Contract.isNotNull("replacements", replacements);
    StringBuilder message = new StringBuilder();
    char current = ' ';        // The current character under investigation
    char previous = ' ';       // The previously encountered character
    boolean skipBrace = false; // Whether to skip the next brace
    for (int i = 0; i < formatString.length(); i++, previous = current) {
      current = formatString.charAt(i);
      if ((current != '{') && (current != '}')) {
        skipBrace = false;
      }
      else if (current == '{') {
        if (skipBrace && (previous == current)) {
          skipBrace = false;
          continue; // Don't add current brace and don't attempt to parse format parameter.
        }
        String formatParameter = parseStringFormatParameter(formatString, i);
        if (formatParameter == null) {
          skipBrace = true;
        }
        else {
          skipBrace = false;
          String parameterName = formatParameter.substring(1, formatParameter.length() - 1);
          String replacement = replacements.getProperty(parameterName);
          if (replacement != null) {
            message.append(replacement);
            i += formatParameter.length() - 1;
            continue; // Don't add current brace any more
          }
        }
      }
      else if (current == '}') {
        if (skipBrace && (previous == current)) {
          skipBrace = false;
          continue; // Don't add current brace.
        }
        skipBrace = true;
      }
      message.append(current);
    }
    return message.toString();
  }

  /**
   * Try to parse a format parameter starting at the specified index.
   *
   * @param formatString The string to parse in.
   * @param startIndex The index in the message pattern to start parsing at.
   *
   * @return The parsed format parameter ({name}), or null if it could none could be parsed.
   */
  private static String parseStringFormatParameter(String formatString, int startIndex) {
    int i = startIndex;
    // Expect starting '{'
    if ((i >= formatString.length()) || (formatString.charAt(i) != '{')) {
      return null;
    }
    i++;
    // Expect name character
    if ((i >= formatString.length()) || !Character.isJavaIdentifierStart(formatString.charAt(i))) {
      return null;
    }
    i++;
    // Look for more characters
    while ((i < formatString.length())
        && (Character.isJavaIdentifierPart(formatString.charAt(i)) || (formatString.charAt(i) == '-'))) {
      i++;
    }
    // Expect ending '}'
    if ((i >= formatString.length()) || (formatString.charAt(i) != '}')) {
      return null;
    }
    i++;
    return formatString.substring(startIndex, i);
  }

  /**
   * Format and return a string from the specified <code>formatString</code> and the specified formatting arguments.
   * <p>
   * The message pattern is a regular string which can contain format parameters. Format parameters are defined as: "{n}", that is:
   * left-curly-brace, followed by a positive number, followed by a right-curly brace.</p>
   * <p>
   * To specify a literal text part containing the combination "left-curly-brace, number, right-curly-brace", that is not be
   * considered a format parameter, double the curly braces. Two consecutive curly braces (of the same kind) will always be
   * substituted by one curly brace and not considered part of a format parameter.</p>
   * <p>
   * The resulting message string will be built by searching the <code>formatString</code> for format parameters. Every format parameter is
   * investigated in turn: the format number is taken and the format arguments array is searched for an index equaling this format
   * number. If this array index exists, the array object at this index is taken and this object's string value (determined by
   * calling <code>toString</code> on the object or <code>null</code> if the object is null) is used for replacing the format parameter under investigation.
   * Format parameters that don't have a corresponding formatting arguments array index, are left untouched.</p>
   *
   * <pre>
   *  Examples:
   *
   *    Pattern                   |  Arguments                    | Result
   *    --------------------------+-------------------------------+-----------------------------------------------
   *    There is {0}.             | { "coffee" }                  | There is coffee.
   *    There is {{0}}.           | { "coffee" }                  | There is {0}.
   *    There is {0}}.            | { "coffee" }                  | There is coffee}.
   *    There is {{{0}}}.         | { "coffee" }                  | There is {coffee}.
   *    There is {{.              | { "coffee" }                  | There is {.
   *    There is {{{a}}.          | { "coffee" }                  | There is {{a}.
   *    There is {0}.             | { }                           | There is {0}.
   *    There is {1}.             | { "coffee" }                  | There is {0}.
   *    There is {0}, {0} and {0}.| { "coffee" }                  | There is coffee, coffee and coffee.
   *    There is {1}, {0} and {2}.| { "milk", "coffee", "sugar" } | There is coffee, milk and sugar.
   * </pre>
   *
   * @param formatString The string, potentially containing format parameters, that is the basis for building the result message.
   * @param arguments Formatting arguments to be used for format parameter substitution.
   *
   * @return A string built by taking the specified <code>formatString</code> and substituting any format parameters.
   */
  public static String format(String formatString, Object[] arguments){
    Contract.isNotNull("formatString", formatString);
    Contract.isNotNull("arguments", arguments);
    StringBuilder message = new StringBuilder();
    char current = ' ';        // The current character under investigation
    char previous = ' ';       // The previously encountered character
    boolean skipBrace = false; // Whether to skip the next brace
    for (int i = 0; i < formatString.length(); i++, previous = current) {
      current = formatString.charAt(i);
      if ((current != '{') && (current != '}')) {
        skipBrace = false;
      }
      else if (current == '{') {
        if (skipBrace && (previous == current)) {
          skipBrace = false;
          continue; // Don't add current brace and don't attempt to parse format parameter.
        }
        String formatParameter = parseIntFormatParameter(formatString, i);
        if (formatParameter == null) {
          skipBrace = true;
        }
        else {
          skipBrace = false;
          try {
            int index = Integer.parseInt(formatParameter.substring(1, formatParameter.length() - 1));
            if ((index >= 0) && (index < arguments.length)) {
              String replacement = "" + arguments[index];
              message.append(replacement);
              i += formatParameter.length() - 1;
              continue; // Don't add current brace
            }
          }
          catch (NumberFormatException nfe) {
            // Ignore and continue normal processing
          }
        }
      }
      else if (current == '}') {
        if (skipBrace && (previous == current)) {
          skipBrace = false;
          continue; // Don't add current brace.
        }
        skipBrace = true;
      }
      message.append(current);
    }
    return message.toString();
  }

  /**
   * Try to parse a format parameter starting at the specified index.
   *
   * @param formatString The string to parse in.
   * @param startIndex The index in the message pattern to start parsing at.
   *
   * @return The parsed format parameter ({n}), or null if it could none could be parsed.
   */
  private static String parseIntFormatParameter(String formatString, int startIndex) {
    int i = startIndex;
    // Expect starting '{'
    if ((i >= formatString.length()) || (formatString.charAt(i) != '{')) {
      return null;
    }
    i++;
    // Expect digit
    if ((i >= formatString.length()) || !Character.isDigit(formatString.charAt(i))) {
      return null;
    }
    i++;
    // Look for more digits
    while ((i < formatString.length()) && Character.isDigit(formatString.charAt(i))) {
      i++;
    }
    // Expect ending '}'
    if ((i >= formatString.length()) || (formatString.charAt(i) != '}')) {
      return null;
    }
    i++;
    return formatString.substring(startIndex, i);
  }

  /**
   * Create a multi-line name/value-pairs table.
   *
   * <pre>
   *  Example: makeTableLines(vals) results in:
   *
   *  setting-a                : value-1-is-very-large
   *  setting-with-a-long-name : value-2
   * </pre>
   *
   * @param vals The pairs of names and values to be put in table lines, where names end up in the first column and values in the second column.
   *
   * @return One or more lines making up for a table where columns 1 and 2 have a fixed width.
   *         Names will be separated from values with a " : " string.
   */
  public static String[] makePairTableLines(List<Pair<String, String>> vals) {
    return makePairTableLines(vals, "", " : ", null);
  }

  /**
   * Create a multi-line name/value-pairs table.
   *
   * <pre>
   *  Example: makeTableLines(vals, "setting: ") results in:
   *
   *  setting: setting-a                : value-1-is-very-large
   *  setting: setting-with-a-long-name : value-2
   * </pre>
   *
   * @param vals The pairs of names and values to be put in table lines, where names end up in the first column and values in the second column.
   * @param rowPrefix The fixed prefix to use for each row.
   *
   * @return One or more lines making up for a table where columns 1 and 2 have a fixed width.
   *         Names will be separated from values with a ':' character if there are only two columns and with a '|' when there are more.
   */
  public static String[] makePairTableLines(List<Pair<String, String>> vals, String rowPrefix) {
    return makePairTableLines(vals, rowPrefix, " : ", null);
  }

  /**
   * Create a multi-line name/value-pairs table.
   *
   * <pre>
   *  Example: makeTableLines(vals, "(", ") = ") results in:
   *
   *  (setting-a                ) = value-1-is-very-large
   *  (setting-with-a-long-name ) = value-2
   * </pre>
   *
   * @param vals The pairs of names and values to be put in table lines, where names end up in the first column and values in the second column.
   * @param rowPrefix The fixed prefix to use for each row.
   * @param columnSeparator The separator between the first column and second column.
   *
   * @return One or more lines making up for a table where columns 1 and 2 have a fixed width.
   */
  public static String[] makePairTableLines(List<Pair<String, String>> vals, String rowPrefix, String columnSeparator) {
    return makePairTableLines(vals, rowPrefix, columnSeparator, null);
  }

  /**
   * Create a multi-line name/value-pairs table.
   *
   * <pre>
   *  Example: makeTableLines(vals, "[ ", " =&gt; ", " ]") results in:
   *
   *  [ setting-a                =&gt; value-1-is-very-large ]
   *  [ setting-with-a-long-name =&gt; value-2               ]
   * </pre>
   *
   * @param vals The pairs of names and values to be put in table lines, where names end up in the first column and values in the second column.
   * @param rowPrefix The fixed prefix to use for each row.
   * @param columnSeparator The separator between the first column and second column.
   * @param rowSuffix The fixed suffix to use for each row. If <code>null</code>, no padding with added to the values in the second column.
   *
   * @return One or more lines making up for a table where columns 1 and 2 have a fixed width.
   */
  public static String[] makePairTableLines(List<Pair<String, String>> vals, String rowPrefix, String columnSeparator, String rowSuffix) {
    Contract.isNotNull("vals", vals);
    List<String[]> rows = new ArrayList<>();
    for (Pair<String, String> p : vals) {
      rows.add(new String[] { p.getFirst(), p.getSecond() });
    }
    return makeTableLines(rows, rowPrefix, columnSeparator, rowSuffix);
  }

  /**
   * Create a multi-line name/value-pairs table.
   *
   * <pre>
   *  Example: makeTableLines(vals) results in:
   *
   *  setting-a                : value-1-is-very-large
   *  setting-with-a-long-name : value-2
   * </pre>
   *
   * @param vals The pairs of names and values to be put in table lines, where names end up in the first column and values in the second column.
   *
   * @return One or more lines making up for a table where columns 1 and 2 have a fixed width.
   *         Names will be separated from values with a " : " string.
   */
  public static String[] makeTableLines(Map<String, String> vals) {
    return makeTableLines(vals, "", " : ", null);
  }

  /**
   * Create a multi-line name/value-pairs table.
   *
   * <pre>
   *  Example: makeTableLines(vals, "setting: ") results in:
   *
   *  setting: setting-a                : value-1-is-very-large
   *  setting: setting-with-a-long-name : value-2
   * </pre>
   *
   * @param vals The pairs of names and values to be put in table lines, where names end up in the first column and values in the second column.
   * @param rowPrefix The fixed prefix to use for each row.
   *
   * @return One or more lines making up for a table where columns 1 and 2 have a fixed width.
   *         Names will be separated from values with a ':' character if there are only two columns and with a '|' when there are more.
   */
  public static String[] makeTableLines(Map<String, String> vals, String rowPrefix) {
    return makeTableLines(vals, rowPrefix, " : ", null);
  }

  /**
   * Create a multi-line name/value-pairs table.
   *
   * <pre>
   *  Example: makeTableLines(vals, "(", ") = ") results in:
   *
   *  (setting-a                ) = value-1-is-very-large
   *  (setting-with-a-long-name ) = value-2
   * </pre>
   *
   * @param vals The pairs of names and values to be put in table lines, where names end up in the first column and values in the second column.
   * @param rowPrefix The fixed prefix to use for each row.
   * @param columnSeparator The separator between the first column and second column.
   *
   * @return One or more lines making up for a table where columns 1 and 2 have a fixed width.
   */
  public static String[] makeTableLines(Map<String, String> vals, String rowPrefix, String columnSeparator) {
    return makeTableLines(vals, rowPrefix, columnSeparator, null);
  }

  /**
   * Create a multi-line name/value-pairs table.
   *
   * <pre>
   *  Example: makeTableLines(vals, "[ ", " =&gt; ", " ]") results in:
   *
   *  [ setting-a                =&gt; value-1-is-very-large ]
   *  [ setting-with-a-long-name =&gt; value-2               ]
   * </pre>
   *
   * @param vals The pairs of names and values to be put in table lines, where names end up in the first column and values in the second column.
   * @param rowPrefix The fixed prefix to use for each row.
   * @param columnSeparator The separator between the first column and second column.
   * @param rowSuffix The fixed suffix to use for each row. If <code>null</code>, no padding with added to the values in the second column.
   *
   * @return One or more lines making up for a table where columns 1 and 2 have a fixed width.
   */
  public static String[] makeTableLines(Map<String, String> vals, String rowPrefix, String columnSeparator, String rowSuffix) {
    Contract.isNotNull("vals", vals);
    List<String[]> rows = new ArrayList<>();
    for (String key : new TreeSet<>(vals.keySet())) { // TreeSet is sorted, so they keys will be iterated in a deterministic, sorted order
      String val = vals.get(key);
      rows.add(new String[] { key, val });
    }
    return makeTableLines(rows, rowPrefix, columnSeparator, rowSuffix);
  }

  /**
   * Create a multi-line table.
   *
   * <pre>
   *  Example: makeTableLines(rows) results in:
   *
   *  setting-a                : value-1-is-very-large
   *  setting-with-a-long-name : value-2
   * </pre>
   *
   * @param rows The list of row values to be put in table lines: the total number of columns will the max. length of any row.
   *
   * @return One or more lines making up for a table where all columns have a fixed width.
   *         Columns will be separated from values with ' : ' characters if there are only two columns and with a ' | ' when there are more.
   */
  public static String[] makeTableLines(List<String[]> rows) {
    return makeTableLines(rows, "", null, null);
  }

  /**
   * Create a multi-line table.
   *
   * <pre>
   *  Example: makeTableLines(rows, "setting: ") results in:
   *
   *  setting: setting-a                : value-1-is-very-large
   *  setting: setting-with-a-long-name : value-2
   * </pre>
   *
   * @param rows The list of row values to be put in table lines: the total number of columns will the max. length of any row.
   * @param rowPrefix The fixed prefix to use for each row.
   *
   * @return One or more lines making up for a table where all columns have a fixed width.
   *         Columns will be separated from values with ' : ' characters if there are only two columns and with ' | ' when there are more.
   */
  public static String[] makeTableLines(List<String[]> rows, String rowPrefix) {
    return makeTableLines(rows, rowPrefix, null, null);
  }

  /**
   * Create a multi-line name/value-pairs table.
   *
   * <pre>
   *  Example: makeTableLines(rows, "(", ") = ") results in:
   *
   *  (setting-a                ) = value-1-is-very-large
   *  (setting-with-a-long-name ) = value-2
   * </pre>
   *
   * @param rows The list of row values to be put in table lines: the total number of columns will the max. length of any row.
   * @param rowPrefix The fixed prefix to use for each row.
   * @param columnSeparator The separator between columns.
   *                        If <code>null</code>, columns will be separated from values with ' : ' characters if there are only two columns and with '|' when there are more.
   *
   * @return One or more lines making up for a table where all columns have a fixed width.
   */
  public static String[] makeTableLines(List<String[]> rows, String rowPrefix, String columnSeparator) {
    return makeTableLines(rows, rowPrefix, columnSeparator, null);
  }

  /**
   * Create a multi-line table.
   *
   * <pre>
   *  Example: makeTableLines(rows, "[ ", " =&gt; ", " ]") results in:
   *
   *  [ setting-a                =&gt; value-1-is-very-large ]
   *  [ setting-with-a-long-name =&gt; value-2               ]
   * </pre>
   *
   * @param rows The list of row values to be put in table lines: the total number of columns will the max. length of any row.
   * @param rowPrefix The fixed prefix to use for each row.
   * @param columnSeparator The separator between columns.
   *                        If <code>null</code>, columns will be separated from values with a ':' character if there are only two columns and with ' | ' when there are more.
   * @param rowSuffix The fixed suffix to use for each row. If <code>null</code>, no padding with added to the values in the last column.
   *
   * @return One or more lines making up for a table where all columns have a fixed width.
   */
  public static String[] makeTableLines(List<String[]> rows, String rowPrefix, String columnSeparator, String rowSuffix) {
    Contract.isNotNull("rows", rows);
    if (rowPrefix == null) {
      rowPrefix = "";
    }
    // Determine max. dimensions
    int colCount = 1;
    int colWidths[] = new int[50];
    int rowHeights[] = new int[rows.size()];
    for (int rowNr = 0; rowNr < rows.size(); rowNr += 1) {
      rowHeights[rowNr] = 1;
      String[] row = rows.get(rowNr);
      if (row.length > colCount) {
        colCount = row.length;
        if (colCount > colWidths.length) {
          colWidths = Arrays.copyOf(colWidths, colCount + 10);
        }
      }
      for (int colIdx = 0; colIdx < row.length; colIdx++) {
        String colVal = row[colIdx];
        String[] colValLines = StringUtils.split(colVal);
        if (colValLines.length > rowHeights[rowNr]) {
          rowHeights[rowNr] = colValLines.length;
        }
        for (String colValLine : colValLines) {
          if (colValLine.length() > colWidths[colIdx]) {
            colWidths[colIdx] = colValLine.length();
          }
        }
      }
    }
    if (columnSeparator == null) {
      columnSeparator = colCount > 2 ? " | " : " : ";
    }
    // Create row:
    List<String> rowLines = new ArrayList<>();
    for (int rowNr = 0; rowNr < rows.size(); rowNr += 1) {
      String[] row = rows.get(rowNr);
      for (int rowInRowNr = 0; rowInRowNr < rowHeights[rowNr]; rowInRowNr++) {
        StringBuilder sb = new StringBuilder();
        sb.append(rowPrefix);
        // For all but the last (which might be the first, if rows only have a single column) column:
        for (int colIdx = 0; colIdx < colCount - 1; colIdx++) {
          String colVal = "";
          if (colIdx < row.length) {
            colVal = row[colIdx];
            if (rowHeights[rowNr] > 1) {
              String[] colValRows = StringUtils.split(colVal);
              if (rowInRowNr < colValRows.length) {
                colVal = colValRows[rowInRowNr];
              }
              else {
                colVal = "";
              }
            }
          }
          if (colVal == null) {
            colVal = "";
          }
          sb.append(StringUtils.padRight(colVal, colWidths[colIdx]));
          sb.append(columnSeparator);
        }
        // For the last column
        int lastColIdx = colCount - 1;
        String colVal = "";
        if (lastColIdx < row.length) {
          colVal = row[lastColIdx];
          if (rowHeights[rowNr] > 1) {
            String[] colValRows = StringUtils.split(colVal);
            if (rowInRowNr < colValRows.length) {
              colVal = colValRows[rowInRowNr];
            }
            else {
              colVal = "";
            }
          }
        }
        if (colVal == null) {
          colVal = "";
        }
        if (rowSuffix == null) {
          sb.append(colVal);
        }
        else {
          sb.append(StringUtils.padRight(colVal, colWidths[lastColIdx]));
          sb.append(rowSuffix);
        }
        rowLines.add(sb.toString());
      }
    }
    return rowLines.toArray(new String[0]);
  }

  /**
   * Create ASCII art ruler lines.
   *
   * <pre>
   *  Examples:
   *  startNumber | endNumber | resulting lines
   *  ------------+-----------+-------------------------------------------------
   *            1 |         5 | "12345"
   *  ------------+-----------+-------------------------------------------------
   *            1 |        23 | "         1         2   "
   *              |           | "12345678901234567890123"
   *  ------------+-----------+-------------------------------------------------
   * </pre>
   *
   * @param startNumber The ruler's start number: the first number shown.
   * @param endNumber The ruler's end number: the last number shown.
   *
   * @return One or more lines making up for an ASCII art ruler. The lines are ordered according to the digit position they
   *         represent. The last line will be filled with digits, the second to last line, when present, will only contain a digit
   *         every ten positions, .....
   */
  public static String[] makeRulerLines(int startNumber, int endNumber) {
    Contract.isInRange("endNumber", endNumber, 0, Integer.MAX_VALUE);
    Contract.isInRange("startNumber", startNumber, 0, endNumber - 1);
    int lineCount = ("" + endNumber).length();
    // Initialize lines to all spaces
    StringBuilder[] lineBuffers = new StringBuilder[lineCount]; // lines[0] will hold digits on every position, lines[1] on every
    // tenth position, ...
    for (int i = 0; i < lineBuffers.length; i++) {
      lineBuffers[i] = new StringBuilder(makeSpacesString((endNumber - startNumber) + 1));
    }
    // Update lines with correct digits
    for (int number = startNumber; number <= endNumber; number++) {
      String numberStr = "" + number;
      if ((number % 10) == 0) {
        for (int digitPos = 0; digitPos < numberStr.length(); digitPos++) {
          int numberStrIndex = numberStr.length() - 1 - digitPos;
          char ch = numberStr.charAt(numberStrIndex);
          lineBuffers[digitPos].setCharAt(number - startNumber, ch);
        }
      }
      else {
        int numberStrIndex = numberStr.length() - 1;
        char ch = numberStr.charAt(numberStrIndex);
        lineBuffers[0].setCharAt(number - startNumber, ch);
      }
    }
    // Make String[] from StringBuilder[]
    String[] lines = new String[lineBuffers.length];
    for (int s = lineBuffers.length - 1, d = 0; s >= 0; s--, d++) {
      lines[d] = lineBuffers[s].toString();
    }
    return lines;
  }

  /**
   * Create a hexadecimal representation of the specified byte buffer.
   *
   * Lines will be formatted as follows: {offset} | {hex} ({dec}) {hex} ({dec}) {hex} ({dec}) ...
   *
   * @param buffer The byte buffer to be represented.
   * @param bytesPerLine The number of bytes that should be put into one line.
   *
   * @return Zero or more lines, representing the buffer. No lines will be returned when the buffer is empty.
   */
  public static String[] makeHexRepresentation(byte[] buffer, int bytesPerLine) {
    return makeHexRepresentation(buffer, 0, buffer.length, bytesPerLine, "{offset} | ", "{hex} ({dec}) ");
  }

  /**
   * Create a hexadecimal representation of the specified byte buffer.
   *
   * Lines will be formatted as follows: {offset} | {hex} ({dec}) {hex} ({dec}) {hex} ({dec}) ...
   *
   * @param buffer The byte buffer to be represented.
   * @param offset The offset in the buffer of the first byte to represent.
   * @param length The number of bytes in buffer to represent, starting from offset.
   * @param bytesPerLine The number of bytes that should be put into one line.
   *
   * @return Zero or more lines, representing the buffer. No lines will be returned when the buffer is empty.
   *
   * @throws IllegalArgumentException When any argument is null.
   * @throws IllegalArgumentException When bytesPerLine is negative or zero.
   */
  public static String[] makeHexRepresentation(byte[] buffer, int offset, int length, int bytesPerLine) {
    return makeHexRepresentation(buffer, offset, length, bytesPerLine, "{offset} | ", "{hex} ({dec}) ");
  }

  /**
   * Create a hexadecimal representation of the specified byte buffer.
   *
   *  See {@link #makeHexRepresentation(byte[], int, int, int, String, String)} for details.
   *
   * @param buffer The byte buffer to be represented.
   * @param bytesPerLine The number of bytes that should be put into one line.
   * @param prefixPattern Pattern string to be used for prefixing each line.
   * @param bytePattern Pattern string to be used for representing every byte.
   * @return Zero or more lines, representing the buffer. No lines will be returned when the buffer is empty.
   *
   * @throws IllegalArgumentException When any argument is null.
   * @throws IllegalArgumentException When bytesPerLine is negative or zero.
   * @throws IllegalArgumentException When bytePattern doesn't contain the substring "{hex}".
   */
  public static String[] makeHexRepresentation(byte[] buffer, int bytesPerLine, String prefixPattern, String bytePattern) {
    return makeHexRepresentation(buffer, 0, buffer.length, bytesPerLine, prefixPattern, bytePattern);
  }

  /**
   * Create a hexadecimal representation of the specified byte buffer.
   *
   * @param buffer The byte buffer to be represented.
   * @param offset The offset in the buffer of the first byte to represent.
   * @param length The number of bytes in buffer to represent, starting from offset.
   * @param bytesPerLine The number of bytes that should be put into one line.
   * @param prefixPattern Pattern string to be used for prefixing each line.
   *        <pre>
   *             This prefix pattern may contain the following format parameters:
   *               {offset}: will be replaced by the byte offset of the first byte represented on the current line.
   *        </pre>
   * @param bytePattern Pattern string to be used for representing every byte.
   *        <pre>
   *             This pattern must contain one of the following format parameters:
   *                {hex}  : will be replaced by the hexadecimal digit represented by the current byte with letters ('a' - 'f') in lowercase.
   *                {HEX}  : will be replaced by the hexadecimal digit represented by the current byte with letters ('A' - 'F') in uppercase.
   *             This pattern may contain following additional format parameters:
   *                {dec}  : will be replaced by the decimal number represented by the current byte.
   *             This pattern may contain following additional format parameters:
   *                {ascii}: will be replaced by the ASCII character represented by the current byte
   *                         if the ASCII character is printable, and otherwise by the '?'-character.
   *        </pre>
   *
   * @return Zero or more lines, representing the buffer. No lines will be returned when the buffer is empty.
   *
   * @throws IllegalArgumentException When any argument is null.
   * @throws IllegalArgumentException When bytesPerLine is negative or zero.
   * @throws IllegalArgumentException When bytePattern doesn't contain the substring "{hex}".
   *
   * <pre>
   * Examples:
   *   Buffer b containing 4 bytes: 0x74 0xa5 0x73 0x74
   *
   *   bytesPerLine | prefixPattern | bytePattern      | Result lines (between [ and ])
   *   -----------------------------+------------------+-----------------------------------------------
   *            100 |        "--&gt; " |       "0x{hex} " | 1.[--&gt; 0x74 0x65 0x73 0x74 ]
   *              3 |        "--&gt; " |       "0x{hex} " | 1.[--&gt; 0x74 0x65 0x73 ]
   *                |               |                  | 2.[--&gt; 0x74 ]
   *              2 |   "{offset}:" | "{HEX} ({dec}) " | 1.[0:74 (116) A5 (101) ]
   *                |               |                  | 2.[2:73 (115) 74 (116) ]
   * </pre>
   */
  public static String[] makeHexRepresentation(byte[] buffer, int offset, int length, int bytesPerLine, String prefixPattern,
                                               String bytePattern) {
    Contract.isNotNull("buffer", buffer);
    Contract.isInRange("offset", offset, 0, buffer.length);
    Contract.isInRange("length", length, 0, buffer.length - offset);
    Contract.isNotNull("prefixPattern", prefixPattern);
    Contract.isNotNull("bytePattern", bytePattern);
    Contract.isInRange("bytesPerLine", bytesPerLine, 1, Integer.MAX_VALUE);
    if ((bytePattern.indexOf("{hex}") < 0) && (bytePattern.indexOf("{HEX}") < 0)) {
      throw new IllegalArgumentException("Argument 'bytePattern' must contain {hex} or {HEX} format parameter.");
    }
    ArrayList<String> lines = new ArrayList<>();
    int offsetMargin = ("" + buffer.length).length();
    Properties p = new Properties();
    for (int i = 0; i < length; i += bytesPerLine) {
      int lineLength = Math.min(bytesPerLine, buffer.length - i);
      String line = toHexString(buffer, i, lineLength, bytePattern);
      p.put("offset", StringUtils.padLeft("" + (offset + i), offsetMargin, '0'));
      lines.add(StringUtils.format(prefixPattern, p) + line);
    }
    return lines.toArray(new String[0]);
  }

  private static final String DEFAULT_BYTE_PATTERN = "{HEX} ";

  /**
   *  Working on the whole buffer and using the bytePattern '{HEX}'.
   *
   *  See {@link #toHexString(byte[], int, int, String)} for details.
   *
   *  @param buffer Buffer to provide a hex representation for.
   *  @return A single line representing the buffer. Will be the empty string if the buffer is empty.
   */
  public static String toHexString(byte[] buffer) {
    return toHexString(buffer, 0, buffer.length, DEFAULT_BYTE_PATTERN);
  }

  /**
   *  Working on the whole buffer.
   *
   *  See {@link #toHexString(byte[], int, int, String)} for details.
   *
   *  @param buffer Buffer to provide a hex representation for.
   *  @param bytePattern Pattern string to be used for representing every byte.
   *  @return A single line representing the buffer. Will be the empty string if the buffer is empty.
   */
  public static String toHexString(byte[] buffer, String bytePattern) {
    return toHexString(buffer, 0, buffer.length, bytePattern);
  }

  /**
   *  Working on a slice of the buffer and using the bytePattern '{HEX}'.
   *
   *  See {@link #toHexString(byte[], int, int, String)} for details.
   *
   *  @param buffer The byte buffer a slice of which needs to be represented.
   *  @param offset The offset in the buffer of the first byte to represent.
   *  @param length The number of bytes in buffer to represent, starting from offset.
   *  @return A single line representing the buffer. Will be the empty string if the buffer is empty.
   */
  public static String toHexString(byte[] buffer, int offset, int length) {
    return toHexString(buffer, offset, length, DEFAULT_BYTE_PATTERN);
  }

  /**
   * Create a single-line hexadecimal representation of the specified byte buffer.
   *
   * <pre>
   * Examples:
   *   Buffer b containing 4 bytes: 0x74 0xa5 0x73 0x74
   *
   *    bytePattern      | Result lines (between [ and ])
   *   -----------------------------+------------------+-----------------------------------------------
   *          "0x{hex} " | [0x74 0xa5 0x73 0x74]
   *    "{HEX} ({dec}) " | [74 (116) A5 (101) 73 (115) 74 (116)]
   * </pre>
   *
   * @param buffer The byte buffer a slice of which needs to be represented.
   * @param offset The offset in the buffer of the first byte to represent.
   * @param length The number of bytes in buffer to represent, starting from offset.
   * @param bytePattern Pattern string to be used for representing every byte.
   *        <pre>
   *             This pattern must contain one of the following format parameters:
   *                {hex}  : will be replaced by the hexadecimal digit represented by the current byte with letters ('a' - 'f') in lowercase.
   *                {HEX}  : will be replaced by the hexadecimal digit represented by the current byte with letters ('A' - 'F') in uppercase.
   *             This pattern may contain following additional format parameters:
   *                {dec}  : will be replaced by the decimal number represented by the current byte.
   *             This pattern may contain following additional format parameters:
   *                {ascii}: will be replaced by the ASCII character represented by the current byte
   *                         if the ASCII character is printable, and otherwise by the '?'-character.
   *        </pre>
   *
   * @return A single line representing the buffer. Will be the empty string if the buffer is empty.
   *
   * @throws IllegalArgumentException When any argument is null.
   * @throws IllegalArgumentException When bytePattern doesn't contain the substring "{hex}" or "{HEX}".
   */
  public static String toHexString(byte[] buffer, int offset, int length, String bytePattern) {
    Contract.isNotNull("buffer", buffer);
    Contract.isInRange("offset", offset, 0, buffer.length);
    Contract.isInRange("length", length, 0, buffer.length - offset);
    Contract.isNotNull("bytePattern", bytePattern);
    if ((bytePattern.indexOf("{hex}") < 0) && (bytePattern.indexOf("{HEX}") < 0)) {
      throw new IllegalArgumentException("Argument 'bytePattern' must contain {hex} or {HEX} format parameter.");
    }
    Properties p = new Properties();
    StringBuilder line = new StringBuilder();
    for (int j = 0; j < length; j++) {
      int b = buffer[offset + j];
      if (b < 0) {
        b += 256;
      }
      String hexDigit = StringUtils.padLeft(Integer.toHexString(b), 2, '0');
      p.put("hex", hexDigit.toLowerCase());
      p.put("HEX", hexDigit.toUpperCase());
      p.put("dec", StringUtils.padLeft("" + b, 3, ' '));
      p.put("ascii", StringUtils.padLeft(toString(new byte[] { buffer[j] }), 3, ' '));
      line.append(StringUtils.format(bytePattern, p));
    }
    return line.toString();
  }

  /**
   *  Create a combined string and hexadecimal representation of the specified byte buffer.
   *  <pre>
   *  Examples:
   *    Buffer b containing 8 bytes: 0x74 0x65 0x73 0x74 0x74 0x65 0x73 0x74
   *
   *    1. bytesPerLine                         = 8
   *       includeSeparatorLines                = true
   *       includeHorizontalPositionInfo        = true
   *       horizontalPositionInfoRepeatCount    = 0
   *       includeVerticalPositionInfo          = true
   *
   *       result lines (between [ and ]):  1.[  | 01234567]
   *                                        2.[--+---------]
   *                                        3.[0 | testtest]
   *                                        4.[  | 76777677]
   *                                        5.[  | 45344534]
   *                                        6.[--+---------]
   *
   *    2. bytesPerLine                         = 4
   *       includeSeparatorLines                = true
   *       includeHorizontalPositionInfo        = true
   *       horizontalPositionInfoRepeatCount    = 0
   *       includeVerticalPositionInfo          = true
   *
   *       result lines (between [ and ]):  1.[  | 0123]
   *                                        2.[--+-----]
   *                                        3.[0 | test]
   *                                        4.[  | 7674]
   *                                        5.[  | 4534]
   *                                        6.[--+-----]
   *                                        7.[4 | test]
   *                                        8.[  | 7674]
   *                                        9.[  | 4534]
   *                                       10.[--+-----]
   *
   *
   *    3. bytesPerLine                         = 4
   *       includeSeparatorLines                = false
   *       includeHorizontalPositionInfo        = true
   *       horizontalPositionInfoRepeatCount    = 0
   *       includeVerticalPositionInfo          = true
   *
   *       result lines (between [ and ]):  1.[  | 0123]
   *                                        2.[--+-----]
   *                                        3.[0 | test]
   *                                        4.[  | 7674]
   *                                        5.[  | 4534]
   *                                        6.[4 | test]
   *                                        7.[  | 7674]
   *                                        8.[  | 4534]
   *                                        9.[--+-----]
   *
   *    3. bytesPerLine                         = 4
   *       includeSeparatorLines                = false
   *       includeHorizontalPositionInfo        = false
   *       horizontalPositionInfoRepeatCount    = 0
   *       includeVerticalPositionInfo          = true
   *
   *       result lines (between [ and ]):  1.[0 | test]
   *                                        2.[  | 7674]
   *                                        3.[  | 4534]
   *                                        4.[4 | test]
   *                                        5.[  | 7674]
   *                                        6.[  | 4534]
   *
   *    4. bytesPerLine                         = 4
   *       includeSeparatorLines                = true
   *       includeHorizontalPositionInfo        = true
   *       horizontalPositionInfoRepeatCount    = 0
   *       includeVerticalPositionInfo          = false
   *
   *       result lines (between [ and ]):  1.[0123]
   *                                        2.[----]
   *                                        3.[test]
   *                                        4.[7674]
   *                                        5.[4534]
   *                                        6.[----]
   *                                        7.[test]
   *                                        8.[7674]
   *                                        9.[4534]
   *                                       10.[----]
   *
   *
   *    5. bytesPerLine                         = 4
   *       includeSeparatorLines                = true
   *       includeHorizontalPositionInfo        = true
   *       horizontalPositionInfoRepeatCount    = 1
   *       includeVerticalPositionInfo          = true
   *
   *       result lines (between [ and ]):  1.[  | 0123]
   *                                        2.[--+-----]
   *                                        3.[0 | test]
   *                                        4.[  | 7674]
   *                                        5.[  | 4534]
   *                                        6.[--+-----]
   *                                        7.[  | 0123]
   *                                        8.[--+-----]
   *                                        9.[4 | test]
   *                                       10.[  | 7674]
   *                                       11.[  | 4534]
   *                                       12.[--+-----]
   *
   *  </pre>
   *
   *  @param buffer The byte buffer to be represented.
   *  @param bytesPerLine The number of bytes that should be put into one line.
   *  @param includeSeparatorLines Whether to include separator lines or not.
   *  @param includeHorizontalPositionInfo Whether to include horizontal position information.
   *  @param horizontalPositionInfoRepeatCount Number of "byte"-lines after which horizontal position information must be repeated. A
   *           value &lt;= 0 will be interpreted to mean that horizontal position info must never be repeated. Only relevant when
   *           includeHorizontalPositionInfo is true.
   *  @param includeVerticalPositionInfo Whether to include vertical position information.
   *
   *  @return Zero or more lines, representing the buffer. No lines will be returned when the buffer is empty.
   *
   *  @throws IllegalArgumentException When any argument is null.
   *  @throws IllegalArgumentException When bytesPerLine is negative or zero.
   */
  public static String[] makeStringAndHexRepresentation(byte[] buffer, int bytesPerLine, boolean includeSeparatorLines,
                                                        boolean includeHorizontalPositionInfo, int horizontalPositionInfoRepeatCount, boolean includeVerticalPositionInfo) {
    Contract.isNotNull("buffer", buffer);
    Contract.isInRange("bytesPerLine", bytesPerLine, 1, Integer.MAX_VALUE);
    if (horizontalPositionInfoRepeatCount <= 0) {
      horizontalPositionInfoRepeatCount = Integer.MAX_VALUE;
    }
    ArrayList<String> lines = new ArrayList<>();
    // Create margin building blocks
    int marginLength = 0;
    String emptyMargin = "";
    String marginSeparator = "";
    if (includeVerticalPositionInfo) {
      marginLength = ("" + buffer.length).length();
      emptyMargin = StringUtils.makeSpacesString(marginLength);
      marginSeparator = " | ";
    }
    // Create rulers
    String[] rulers = new String[0];
    if (includeHorizontalPositionInfo) {
      rulers = StringUtils.makeRulerLines(0, bytesPerLine - 1);
      for (int r = 0; r < rulers.length; r++) {
        rulers[r] = emptyMargin + marginSeparator + rulers[r];
      }
    }
    // Create separator line
    String separatorLine = StringUtils.makeCharacterRepetitionString('-', bytesPerLine);
    if (includeVerticalPositionInfo) {
      separatorLine = StringUtils.makeCharacterRepetitionString('-', marginLength) + "-+-"
          + StringUtils.makeCharacterRepetitionString('-', bytesPerLine);
    }
    // Create and add hex representation lines
    for (int i = 0, linesWritten = 0; i < buffer.length; i += bytesPerLine, linesWritten++) {
      String margin = "";
      if (includeVerticalPositionInfo) {
        margin = StringUtils.padLeft("" + i, marginLength, '0');
      }
      String line1 = "";
      String line2 = "";
      String line3 = "";
      for (int j = 0; (j < bytesPerLine) && ((j + i) < buffer.length); j++) {
        int b = buffer[i + j];
        if (b < 0) {
          b += 256;
        }
        line1 += StringUtils.toString(new byte[] { buffer[i + j] });
        String hexValue = StringUtils.padLeft(Integer.toHexString(b), 2, '0');
        line2 += hexValue.charAt(0);
        line3 += hexValue.charAt(1);
      }
      if ((linesWritten % horizontalPositionInfoRepeatCount) == 0) {
        for (int r = 0; r < rulers.length; r++) {
          lines.add(rulers[r]);
        }
        if (rulers.length > 0) {
          lines.add(separatorLine);
        }
      }
      lines.add(margin + marginSeparator + line1);
      lines.add(emptyMargin + marginSeparator + line2);
      lines.add(emptyMargin + marginSeparator + line3);
      if (includeSeparatorLines) {
        lines.add(separatorLine);
      }
    }
    return lines.toArray(new String[0]);
  }

  /**
   *  Count the number of occurrences of the specified character.
   *
   *  @param searchSpace String to be searched
   *  @param ch The character to search for
   *
   *  @return The number of occurrences found.
   */
  public static int countOccurrences(String searchSpace, char ch) {
    Contract.isNotNull("searchSpace", searchSpace);
    int count = 0;
    for (int i = 0; i < searchSpace.length(); i++) {
      if (searchSpace.charAt(i) == ch) {
        count++;
      }
    }
    return count;
  }

  /**
   *  Count the number of occurrences of the specified substring.
   *
   *  @param searchSpace String to be searched.
   *  @param substr The substring to search for. Should not be empty.
   *
   *  @return The number of non-overlapping of occurrences of substring found in searchSpace.
   */
  public static int countOccurrences(String searchSpace, String substr) {
    Contract.isNotNull("searchSpace", searchSpace);
    Contract.isNotNull("substr", substr);
    Contract.isNotEmpty("substr", substr);
    int count = 0;
    int len = substr.length();
    int idx = searchSpace.indexOf(substr);
    while (idx >= 0) {
      count++;
      idx = searchSpace.indexOf(substr, idx + len);
    }
    return count;
  }

  /**
   * Create a textual representation of the specified map.
   *
   * @param map The map to be represented as a string.
   *
   * @return A textual representation of the specified map, formatted as: "{ &lt;key&gt; -&gt; &lt;value&gt;, &lt;key&gt; -&gt; &lt;value&gt;, ... }"
   *
   * @throws IllegalArgumentException When map is null.
   */
  public static String toString(Map<?, ?> map){
    Contract.isNotNull("map", map);
    StringBuilder rep = new StringBuilder();
    rep.append("{ ");
    for (Iterator<?> it = map.keySet().iterator(); it.hasNext();) {
      Object key = it.next();
      Object value = map.get(key);
      rep.append("" + key + " -> " + value);
      if (it.hasNext()) {
        rep.append(", ");
      }
    }
    rep.append(" }");
    return rep.toString();
  }

  /**
   * Create a textual representation of the specified collection.
   *
   * @param collection The collection to be represented as a string.
   *
   * @return A textual representation of the specified collection, formatted as: "{ &lt;entry&gt;, &lt;entry&gt;, ... }"
   *
   * @throws IllegalArgumentException When collection is null.
   */
  public static String toString(Collection<?> collection){
    Contract.isNotNull("collection", collection);
    StringBuilder rep = new StringBuilder();
    rep.append("{ ");
    for (Iterator<?> it = collection.iterator(); it.hasNext();) {
      Object entry = it.next();
      rep.append("" + entry);
      if (it.hasNext()) {
        rep.append(", ");
      }
    }
    rep.append(" }");
    return rep.toString();
  }

  /**
   * Create a textual representation of the specified byte buffer, interpreting all bytes as ASCII character codes.
   *
   * @param buffer The byte buffer to be represented as a string.
   *
   * @return A textual representation of the specified byte buffer, with unprintable bytes represented as '?'.
   *
   * @throws IllegalArgumentException When buffer is null.
   */
  public static String toString(byte[] buffer) {
    return toString(buffer, '?');
  }

  /**
   * Create a textual representation of the specified byte buffer, interpreting all bytes as ASCII character codes.
   *
   * @param buffer The byte buffer to be represented as a string.
   * @param unprintableChar The character to be used for the representation of bytes that represent unprintable ASCII characters.
   *
   * @return A textual representation of the specified byte buffer, with unprintable bytes represented as '?'.
   *
   * @throws IllegalArgumentException When buffer is null or unprintableChar is itself unprintable.
   */
  public static String toString(byte[] buffer, char unprintableChar) {
    Contract.isNotNull("buffer", buffer);
    Contract.isTrue("unprintableChar is itself printable", isPrintable(unprintableChar));
    StringBuilder rep = new StringBuilder();
    for (int i = 0; i < buffer.length; i++) {
      byte b = buffer[i];
      char c = (char) b;
      if (isPrintable(c)) {
        rep.append(c);
      }
      else {
        rep.append(unprintableChar);
      }
    }
    return rep.toString();
  }

  /**
   * Determine whether the specified character is printable.
   *
   * @param c Character to be tested.
   *
   * @return Whether the specified character is printable.
   */
  public static boolean isPrintable(char c) {
    return !(Character.isISOControl(c) || (Character.isWhitespace(c) && (c != ' ')));
  }

  /**
   * Determine whether a string is <code>null</code> or empty.
   * <p>
   * This is a convenience method equivalent to the expression <code>(str == null) || str.isEmpty())</code></p>
   *
   * @param str The string to be tested.
   *
   * @return Whether the specified string is either <code>null</code> or <code>str.isEmpty()</code>.
   */
  public static boolean isNullOrEmpty(String str) {
    return (str == null) || str.isEmpty();
  }

  /**
   * Determine whether a string is <code>null</code>, empty or contains only whitespace characters.
   * <p>
   * This is a convenience method equivalent to the expression <code>(str == null) || str.trim().isEmpty())</code></p>
   *
   * @param str The string to be tested.
   *
   * @return Whether the specified string is either <code>null</code> or empty after removing all whitespace characters.
   */
  public static boolean isNullOrWhitespace(String str) {
    return (str == null) || str.trim().isEmpty();
  }
}
