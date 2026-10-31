// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging;

/**
 *  Standard log levels, where each level indicates an <em>importance</em>.
 *
 *  More important log levels have higher numeric values.
 *  <p>
 *  Log levels are integers in order to make it easy for client code to define and use intermediate/additional levels (e.g. DEBUG2, DEBUG3).</p>
 *  <p>
 *  While many logging APIs provide also a FATAL or CRITICAL level, this has been omitted as it isn't perceived to provide additional benefit above using
 *  the ERROR level with message and data.</p>
 */
public final class LogLevel
{
  /** Prevent instantiation */
  private LogLevel() { }

  /**
   *  A pseudo-level indicating that no logging is to happen at all.
   */
  public static final int NONE = Integer.MAX_VALUE;

  /**
   *  Level associated with error situations, both those that can be considered 'fatal' and those from which recovery and continued processing is possible.
   */
  public static final int ERROR = 1000;

  /**
   *  Level associated with situations that deserve attention because they can either lead to an error situation or they are highly exceptional and/or unexpected.
   */
  public static final int WARNING = 900;

  /**
   *  Level for normal informative messages, including providing configuration, progress or general status information.
   */
  public static final int INFO = 800;

  /**
   *  Level for detail messages, typically needed only to diagnose a presumed or proven error situation. These kinds of log messages are typically primarily intended for
   *  software developers. Suppressed on stdout/stderr unless explicitly enabled (e.g. via <code>--debug</code>).
   */
  public static final int DEBUG = 500;

  /**
   *  A pseudo-level indicating that everything should get logged.
   */
  public static final int ALL = Integer.MIN_VALUE;

  /**
   *  Provide a string representation for the level that can be parsed back into a level via the parse
   *  method.
   *
   *  For all levels that don't match the predefined levels, just a string representation of the number.
   *  For levels that do correspond to the predefined levels, the strings:<ul>
   *    <li><code>NONE</code></li>
   *    <li><code>ERROR</code></li>
   *    <li><code>WARNING</code></li>
   *    <li><code>INFO</code></li>
   *    <li><code>DEBUG</code></li>
   *    <li><code>ALL</code></li>
   *  </ul>
   *
   *  @param level The level to be represented as a string.
   *  @return A string representation of the level.
   */
  public static String toString(int level) {
    if (level == LogLevel.NONE) {
      return "NONE";
    } else if (level == LogLevel.ERROR) {
      return "ERROR";
    } else if (level == LogLevel.WARNING) {
      return "WARNING";
    } else if (level == LogLevel.INFO) {
      return "INFO";
    } else if (level == LogLevel.DEBUG) {
      return "DEBUG";
    } else if (level == LogLevel.ALL) {
      return "ALL";
    } else {
      return "" + level;
    }
  }

  /**
   *  Parse a string representation into a integer log level value.
   *  If the string represents an integer, that value will be returned.
   *  If the string matches one of the following string in a case-insensitive way,
   *  the corresponding int value will be returned:<ul>
   *    <li><code>NONE</code></li>
   *    <li><code>ERROR</code></li>
   *    <li><code>WARNING</code></li>
   *    <li><code>INFO</code></li>
   *    <li><code>DEBUG</code></li>
   *    <li><code>ALL</code></li>
   *  </ul>
   *
   *  @param levelStr The string representing a log level.
   *  @return The integer log level value represented by the levelStr.
   *
   *  @throws IllegalArgumentException If the provided levelStr is not valid.
   */
  public static int parse(String levelStr) {
    assert levelStr != null;

    levelStr = levelStr.toUpperCase().trim();
    if ("NONE".equals(levelStr)) {
      return LogLevel.NONE;
    } else if ("ERROR".equals(levelStr)) {
      return LogLevel.ERROR;
    } else if ("WARNING".equals(levelStr)) {
      return LogLevel.WARNING;
    } else if ("INFO".equals(levelStr)) {
      return LogLevel.INFO;
    } else if ("DEBUG".equals(levelStr)) {
      return LogLevel.DEBUG;
    } else if ("ALL".equals(levelStr)) {
      return LogLevel.ALL;
    }
    try {
      return Integer.parseInt(levelStr);
    } catch (NumberFormatException nfe) {
      throw new IllegalArgumentException("Value '" + levelStr + "' cannot be parsed as a valid level", nfe);
    }
  }
}
