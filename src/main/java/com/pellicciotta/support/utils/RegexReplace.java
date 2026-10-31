// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;


import java.util.regex.Pattern;

/**
 * A regular expression and a replacement expression.
 */
public class RegexReplace implements Comparable<RegexReplace> {

  // <editor-fold desc="Construction">

  /**
   * Create a new {@link RegexReplace}.
   *
   * @param regex The pattern to be matched.
   * @param replacement The replacement expression.
   */
  public RegexReplace(String regex, String replacement) {
    Contract.isNotNull("regex", regex);
    Contract.isNotNull("replacement", replacement);
    this.match = Pattern.compile(regex);
    this.replacement = replacement;
  }

  /**
   * Create a new {@link RegexReplace}.
   *
   * @param regex The pattern to be matched.
   * @param replacement The replacement expression.
   */
  public RegexReplace(Pattern regex, String replacement) {
    Contract.isNotNull("regex", regex);
    Contract.isNotNull("replacement", replacement);
    this.match = regex;
    this.replacement = replacement;
  }

  /**
   * Create a new {@link RegexReplace} which is really a copy of the given one.
   *
   * @param template Template to be copied. Must not be <c>null</c>.
   */
  public RegexReplace(RegexReplace template) {
    Contract.isNotNull("template", template);
    this.match = template.match;
    this.replacement = template.replacement;
  }

  /**
   *  Parse a string of the form <kbd>/pattern/replacement/</kbd> into a {@link RegexReplace} object.
   *
   *  @param str The value to be parsed.
   *  @return The parsed {@link RegexReplace} object.
   */
  public static RegexReplace parse(String str) {
    if (str.charAt(0) != '/') {
      throw new IllegalArgumentException("The value must start with /");
    }
    else if (str.charAt(str.length() - 1) != '/') {
      throw new IllegalArgumentException("The value must end with /");
    }
    String pattern = "";
    String replacement = "";
    boolean escaped = false;
    for (int i = 1; i < str.length() - 1; i++) {
      char ch = str.charAt(i);
      if (ch == '/') {
        if (escaped) {
          escaped = !escaped;
        }
        else {
          pattern = str.substring(1, i);
          replacement = str.substring(i + 1, str.length() - 1);
          break;
        }
      }
    }
    return new RegexReplace(Pattern.compile(pattern), replacement);
  }

  // </editor-fold>
  // <editor-fold desc="Properties">

  private final Pattern match;
  private final String replacement;

  /**
   * @return The match regex.
   */
  public Pattern getMatchRegex() {
    return this.match;
  }

  /**
   * @return The replacement expression.
   */
  public String getReplacement() {
    return this.replacement;
  }

  // </editor-fold>
  // <editor-fold desc="Comparison">

  @Override
  public int hashCode() {
    int hash = 7;
    hash = (37 * hash) + (this.match != null ? this.match.hashCode() : 0);
    hash = (37 * hash) + (this.replacement != null ? this.replacement.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    if (!(other instanceof RegexReplace)) {
      return false;
    }
    RegexReplace otherPair = (RegexReplace)other;
    return Checks.areEqual(this.match, otherPair.match) && Checks.areEqual(this.replacement, otherPair.replacement);
  }

  /**
   * Whether the current object is alphabetically before, after or equal to the provided one.
   * Comparison will be based string representation of the pattern and its replacement.
   *
   * @param o {@link RegexReplace} to compare against.
   *
   * @return A negative number if this object is before the to be compared one, 0 if both versions are equal and a positive
   *         number if this object is after the to be compared one.
   */
  @Override
  public int compareTo(RegexReplace o) {
    return this.toString().compareTo(o.toString());
  }

  // </editor-fold>
  // <editor-fold desc="Debugging">

  /**
   * @return A human-readable representation.
   */
  @Override
  public String toString() {
    return "/" + this.match + "/" + this.replacement + "/";
  }

  // </editor-fold>
}
