// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 *  A collection of precondition checks.
 */
public final class Contract {
  /** Prevent instantiation */
  private Contract() {  }

  /**
   * Verifies that a condition is true.
   *
   * @param conditionDescription Description of the condition, used in the exception message.
   * @param condition The condition to check.
   * @throws IllegalArgumentException if {@code condition} is not true.
   */
  public static void isTrue(String conditionDescription, boolean condition) {
    if (!condition) {
      throw new IllegalArgumentException("Condition '" + conditionDescription + "' must be true");
    }
  }

  /**
   * Verifies that a condition is true.
   *
   * @param condition The condition to check.
   * @throws IllegalArgumentException if {@code condition} is not true.
   */
  public static void isTrue(boolean condition) {
    if (!condition) {
      throw new IllegalArgumentException("Condition must be true");
    }
  }

  /**
   * Verifies that a condition is false.
   *
   * @param conditionDescription Description of the condition, used in the exception message.
   * @param condition The condition to check.
   * @throws IllegalArgumentException if {@code condition} is not false.
   */
  public static void isFalse(String conditionDescription, boolean condition) {
    if (condition) {
      throw new IllegalArgumentException("Condition '" + conditionDescription + "' must be false");
    }
  }

  /**
   * Verifies that a condition is false.
   *
   * @param condition The condition to check.
   * @throws IllegalArgumentException if {@code condition} is not false.
   */
  public static void isFalse(boolean condition) {
    if (condition) {
      throw new IllegalArgumentException("Condition must be false");
    }
  }

  /**
   * Verifies that an argument is not {@code null}.
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is {@code null}.
   */
  public static void isNotNull(String argName, Object arg) {
    if (arg == null) {
      throw new IllegalArgumentException("Argument '" + argName + "' must not be null");
    }
  }

  /**
   * Verifies that an argument is not {@code null}.
   *
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is {@code null}.
   */
  public static void isNotNull(Object arg) {
    if (arg == null) {
      throw new IllegalArgumentException("Argument must not be null");
    }
  }

  /**
   * Verifies that a int argument is equal to zero.
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is not zero.
   */
  public static void isZero(String argName, int arg) {
    if (arg != 0) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is not zero");
    }
  }

  /**
   * Verifies that a int argument is equal to zero.
   *
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is not zero.
   */
  public static void isZero(int arg) {
    if (arg != 0) {
      throw new IllegalArgumentException("Argument value " + arg + " is not zero");
    }
  }

  /**
   * Verifies that a int argument is different from zero.
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is zero.
   */
  public static void isNotZero(String argName, int arg) {
    if (arg == 0) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is zero");
    }
  }

  /**
   * Verifies that a int argument is different from zero.
   *
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is zero.
   */
  public static void isNotZero(int arg) {
    if (arg == 0) {
      throw new IllegalArgumentException("Argument value " + arg + " is zero");
    }
  }

  /**
   * Verifies that a int argument is greater than zero.
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is not positive.
   */
  public static void isPositive(String argName, int arg) {
    if (arg <= 0) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is not positive");
    }
  }

  /**
   * Verifies that a int argument is greater than zero.
   *
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is not positive.
   */
  public static void isPositive(int arg) {
    if (arg <= 0) {
      throw new IllegalArgumentException("Argument value " + arg + " is not positive");
    }
  }

  /**
   * Verifies that a int argument is less than or equal to zero.
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is positive.
   */
  public static void isNotPositive(String argName, int arg) {
    if (arg > 0) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is positive");
    }
  }

  /**
   * Verifies that a int argument is less than or equal to zero.
   *
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is positive.
   */
  public static void isNotPositive(int arg) {
    if (arg > 0) {
      throw new IllegalArgumentException("Argument value " + arg + " is positive");
    }
  }

  /**
   * Verifies that a int argument is less than zero.
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is not negative.
   */
  public static void isNegative(String argName, int arg) {
    if (arg >= 0) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is not negative");
    }
  }

  /**
   * Verifies that a int argument is less than zero.
   *
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is not negative.
   */
  public static void isNegative(int arg) {
    if (arg >= 0) {
      throw new IllegalArgumentException("Argument value " + arg + " is not negative");
    }
  }

  /**
   * Verifies that a int argument is greater than or equal to zero.
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is negative.
   */
  public static void isNotNegative(String argName, int arg) {
    if (arg < 0) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is negative");
    }
  }

  /**
   * Verifies that a int argument is greater than or equal to zero.
   *
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is negative.
   */
  public static void isNotNegative(int arg) {
    if (arg < 0) {
      throw new IllegalArgumentException("Argument value " + arg + " is negative");
    }
  }

  /**
   * Verifies that a int argument is inside the closed range [{@code start}, {@code end}].
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The argument to check.
   * @param start Lower bound of the range (inclusive).
   * @param end Upper bound of the range (inclusive).
   * @throws IllegalArgumentException if {@code arg} is not within the range.
   */
  public static void isInRange(String argName, int arg, int start, int end) {
    if ((arg < start) || (arg > end)) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is not in range [" + start + ", " + end + "]");
    }
  }

  /**
   * Verifies that a int argument is inside the closed range [{@code start}, {@code end}].
   *
   * @param arg The argument to check.
   * @param start Lower bound of the range (inclusive).
   * @param end Upper bound of the range (inclusive).
   * @throws IllegalArgumentException if {@code arg} is not within the range.
   */
  public static void isInRange(int arg, int start, int end) {
    if ((arg < start) || (arg > end)) {
      throw new IllegalArgumentException("Argument value " + arg + " is not in range [" + start + ", " + end + "]");
    }
  }

  /**
   * Verifies that a int argument is outside the closed range [{@code start}, {@code end}].
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The argument to check.
   * @param start Lower bound of the range (inclusive).
   * @param end Upper bound of the range (inclusive).
   * @throws IllegalArgumentException if {@code arg} is within the range.
   */
  public static void isNotInRange(String argName, int arg, int start, int end) {
    if ((arg >= start) && (arg <= end)) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is in range [" + start + ", " + end + "]");
    }
  }

  /**
   * Verifies that a int argument is outside the closed range [{@code start}, {@code end}].
   *
   * @param arg The argument to check.
   * @param start Lower bound of the range (inclusive).
   * @param end Upper bound of the range (inclusive).
   * @throws IllegalArgumentException if {@code arg} is within the range.
   */
  public static void isNotInRange(int arg, int start, int end) {
    if ((arg >= start) && (arg <= end)) {
      throw new IllegalArgumentException("Argument value " + arg + " is in range [" + start + ", " + end + "]");
    }
  }

  /**
   * Verifies that a long argument is equal to zero.
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is not zero.
   */
  public static void isZero(String argName, long arg) {
    if (arg != 0) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is not zero");
    }
  }

  /**
   * Verifies that a long argument is equal to zero.
   *
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is not zero.
   */
  public static void isZero(long arg) {
    if (arg != 0) {
      throw new IllegalArgumentException("Argument value " + arg + " is not zero");
    }
  }

  /**
   * Verifies that a long argument is different from zero.
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is zero.
   */
  public static void isNotZero(String argName, long arg) {
    if (arg == 0) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is zero");
    }
  }

  /**
   * Verifies that a long argument is different from zero.
   *
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is zero.
   */
  public static void isNotZero(long arg) {
    if (arg == 0) {
      throw new IllegalArgumentException("Argument value " + arg + " is zero");
    }
  }

  /**
   * Verifies that a long argument is greater than zero.
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is not positive.
   */
  public static void isPositive(String argName, long arg) {
    if (arg <= 0) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is not positive");
    }
  }

  /**
   * Verifies that a long argument is greater than zero.
   *
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is not positive.
   */
  public static void isPositive(long arg) {
    if (arg <= 0) {
      throw new IllegalArgumentException("Argument value " + arg + " is not positive");
    }
  }

  /**
   * Verifies that a long argument is less than or equal to zero.
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is positive.
   */
  public static void isNotPositive(String argName, long arg) {
    if (arg > 0) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is positive");
    }
  }

  /**
   * Verifies that a long argument is less than or equal to zero.
   *
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is positive.
   */
  public static void isNotPositive(long arg) {
    if (arg > 0) {
      throw new IllegalArgumentException("Argument value " + arg + " is positive");
    }
  }

  /**
   * Verifies that a long argument is less than zero.
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is not negative.
   */
  public static void isNegative(String argName, long arg) {
    if (arg >= 0) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is not negative");
    }
  }

  /**
   * Verifies that a long argument is less than zero.
   *
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is not negative.
   */
  public static void isNegative(long arg) {
    if (arg >= 0) {
      throw new IllegalArgumentException("Argument value " + arg + " is not negative");
    }
  }

  /**
   * Verifies that a long argument is greater than or equal to zero.
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is negative.
   */
  public static void isNotNegative(String argName, long arg) {
    if (arg < 0) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is negative");
    }
  }

  /**
   * Verifies that a long argument is greater than or equal to zero.
   *
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is negative.
   */
  public static void isNotNegative(long arg) {
    if (arg < 0) {
      throw new IllegalArgumentException("Argument value " + arg + " is negative");
    }
  }

  /**
   * Verifies that a long argument is inside the closed range [{@code start}, {@code end}].
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The argument to check.
   * @param start Lower bound of the range (inclusive).
   * @param end Upper bound of the range (inclusive).
   * @throws IllegalArgumentException if {@code arg} is not within the range.
   */
  public static void isInRange(String argName, long arg, long start, long end) {
    if ((arg < start) || (arg > end)) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is not in range [" + start + ", " + end + "]");
    }
  }

  /**
   * Verifies that a long argument is inside the closed range [{@code start}, {@code end}].
   *
   * @param arg The argument to check.
   * @param start Lower bound of the range (inclusive).
   * @param end Upper bound of the range (inclusive).
   * @throws IllegalArgumentException if {@code arg} is not within the range.
   */
  public static void isInRange(long arg, long start, long end) {
    if ((arg < start) || (arg > end)) {
      throw new IllegalArgumentException("Argument value " + arg + " is not in range [" + start + ", " + end + "]");
    }
  }

  /**
   * Verifies that a long argument is outside the closed range [{@code start}, {@code end}].
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The argument to check.
   * @param start Lower bound of the range (inclusive).
   * @param end Upper bound of the range (inclusive).
   * @throws IllegalArgumentException if {@code arg} is within the range.
   */
  public static void isNotInRange(String argName, long arg, long start, long end) {
    if ((arg >= start) && (arg <= end)) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is in range [" + start + ", " + end + "]");
    }
  }

  /**
   * Verifies that a long argument is outside the closed range [{@code start}, {@code end}].
   *
   * @param arg The argument to check.
   * @param start Lower bound of the range (inclusive).
   * @param end Upper bound of the range (inclusive).
   * @throws IllegalArgumentException if {@code arg} is within the range.
   */
  public static void isNotInRange(long arg, long start, long end) {
    if ((arg >= start) && (arg <= end)) {
      throw new IllegalArgumentException("Argument value " + arg + " is in range [" + start + ", " + end + "]");
    }
  }

  /**
   * Verifies that a double argument is equal to zero.
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is not zero.
   */
  public static void isZero(String argName, double arg) {
    if (arg != 0.0d) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is not zero");
    }
  }

  /**
   * Verifies that a double argument is equal to zero.
   *
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is not zero.
   */
  public static void isZero(double arg) {
    if (arg != 0.0d) {
      throw new IllegalArgumentException("Argument value " + arg + " is not zero");
    }
  }

  /**
   * Verifies that a double argument is different from zero.
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is zero.
   */
  public static void isNotZero(String argName, double arg) {
    if (arg == 0.0d) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is zero");
    }
  }

  /**
   * Verifies that a double argument is different from zero.
   *
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is zero.
   */
  public static void isNotZero(double arg) {
    if (arg == 0.0d) {
      throw new IllegalArgumentException("Argument value " + arg + " is zero");
    }
  }

  /**
   * Verifies that a double argument is greater than zero.
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is not positive.
   */
  public static void isPositive(String argName, double arg) {
    if (arg <= 0.0d) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is not positive");
    }
  }

  /**
   * Verifies that a double argument is greater than zero.
   *
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is not positive.
   */
  public static void isPositive(double arg) {
    if (arg <= 0.0d) {
      throw new IllegalArgumentException("Argument value " + arg + " is not positive");
    }
  }

  /**
   * Verifies that a double argument is less than or equal to zero.
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is positive.
   */
  public static void isNotPositive(String argName, double arg) {
    if (arg > 0.0d) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is positive");
    }
  }

  /**
   * Verifies that a double argument is less than or equal to zero.
   *
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is positive.
   */
  public static void isNotPositive(double arg) {
    if (arg > 0.0d) {
      throw new IllegalArgumentException("Argument value " + arg + " is positive");
    }
  }

  /**
   * Verifies that a double argument is less than zero.
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is not negative.
   */
  public static void isNegative(String argName, double arg) {
    if (arg >= 0.0d) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is not negative");
    }
  }

  /**
   * Verifies that a double argument is less than zero.
   *
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is not negative.
   */
  public static void isNegative(double arg) {
    if (arg >= 0.0d) {
      throw new IllegalArgumentException("Argument value " + arg + " is not negative");
    }
  }

  /**
   * Verifies that a double argument is greater than or equal to zero.
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is negative.
   */
  public static void isNotNegative(String argName, double arg) {
    if (arg < 0.0d) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is negative");
    }
  }

  /**
   * Verifies that a double argument is greater than or equal to zero.
   *
   * @param arg The argument to check.
   * @throws IllegalArgumentException if {@code arg} is negative.
   */
  public static void isNotNegative(double arg) {
    if (arg < 0.0d) {
      throw new IllegalArgumentException("Argument value " + arg + " is negative");
    }
  }

  /**
   * Verifies that a double argument is inside the closed range [{@code start}, {@code end}].
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The argument to check.
   * @param start Lower bound of the range (inclusive).
   * @param end Upper bound of the range (inclusive).
   * @throws IllegalArgumentException if {@code arg} is not within the range.
   */
  public static void isInRange(String argName, double arg, double start, double end) {
    if ((arg < start) || (arg > end)) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is not in range [" + start + ", " + end + "]");
    }
  }

  /**
   * Verifies that a double argument is inside the closed range [{@code start}, {@code end}].
   *
   * @param arg The argument to check.
   * @param start Lower bound of the range (inclusive).
   * @param end Upper bound of the range (inclusive).
   * @throws IllegalArgumentException if {@code arg} is not within the range.
   */
  public static void isInRange(double arg, double start, double end) {
    if ((arg < start) || (arg > end)) {
      throw new IllegalArgumentException("Argument value " + arg + " is not in range [" + start + ", " + end + "]");
    }
  }

  /**
   * Verifies that a double argument is outside the closed range [{@code start}, {@code end}].
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The argument to check.
   * @param start Lower bound of the range (inclusive).
   * @param end Upper bound of the range (inclusive).
   * @throws IllegalArgumentException if {@code arg} is within the range.
   */
  public static void isNotInRange(String argName, double arg, double start, double end) {
    if ((arg >= start) && (arg <= end)) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is in range [" + start + ", " + end + "]");
    }
  }

  /**
   * Verifies that a double argument is outside the closed range [{@code start}, {@code end}].
   *
   * @param arg The argument to check.
   * @param start Lower bound of the range (inclusive).
   * @param end Upper bound of the range (inclusive).
   * @throws IllegalArgumentException if {@code arg} is within the range.
   */
  public static void isNotInRange(double arg, double start, double end) {
    if ((arg >= start) && (arg <= end)) {
      throw new IllegalArgumentException("Argument value " + arg + " is in range [" + start + ", " + end + "]");
    }
  }

  /**
   * Verifies that an argument equals one of the given values.
   *
   * @param arg The argument to check.
   * @param validValues The values {@code arg} must equal one of; an empty array accepts nothing.
   * @throws IllegalArgumentException if {@code arg} equals none of the values.
   */
  public static void isIn(char arg, char... validValues) {
    for (char v : validValues) {
      if (arg == v) {
        return;
      }
    }
    throw new IllegalArgumentException("Argument value " + arg + " must be one of " + java.util.Arrays.toString(validValues));
  }

  /**
   * Verifies that an argument equals one of the given values.
   *
   * @param arg The argument to check.
   * @param validValues The values {@code arg} must equal one of; an empty array accepts nothing.
   * @throws IllegalArgumentException if {@code arg} equals none of the values.
   */
  public static void isIn(int arg, int... validValues) {
    for (int v : validValues) {
      if (arg == v) {
        return;
      }
    }
    throw new IllegalArgumentException("Argument value " + arg + " must be one of " + java.util.Arrays.toString(validValues));
  }

  /**
   * Verifies that an argument equals one of the given values.
   *
   * @param arg The argument to check.
   * @param validValues The values {@code arg} must equal one of; an empty array accepts nothing.
   * @throws IllegalArgumentException if {@code arg} equals none of the values.
   */
  public static void isIn(long arg, long... validValues) {
    for (long v : validValues) {
      if (arg == v) {
        return;
      }
    }
    throw new IllegalArgumentException("Argument value " + arg + " must be one of " + java.util.Arrays.toString(validValues));
  }

  /**
   * Verifies that an argument equals one of the given values.
   *
   * @param arg The argument to check.
   * @param validValues The values {@code arg} must equal one of; an empty array accepts nothing.
   * @throws IllegalArgumentException if {@code arg} equals none of the values.
   */
  public static void isIn(double arg, double... validValues) {
    for (double v : validValues) {
      if (arg == v) {
        return;
      }
    }
    throw new IllegalArgumentException("Argument value " + arg + " must be one of " + java.util.Arrays.toString(validValues));
  }

  /**
   * Verifies that an argument equals one of the given values. A {@code null} {@code arg} is accepted only if the
   * values contain {@code null}.
   *
   * @param <T> The type of the argument and the values.
   * @param arg The argument to check.
   * @param validValues The values {@code arg} must equal one of; an empty array accepts nothing.
   * @throws IllegalArgumentException if {@code arg} equals none of the values.
   */
  public static <T> void isIn(T arg, T... validValues) {
    for (T v : validValues) {
      if (Objects.equals(arg, v)) {
        return;
      }
    }
    StringBuilder sb = new StringBuilder();
    for (T v : validValues) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(v == null ? "null" : "'" + v + "'");
    }
    throw new IllegalArgumentException("Argument " + (arg == null ? "null" : "'" + arg + "'") + " must be one of [" + sb + "]");
  }

  /**
   * Verifies that an argument is not equal to any of the given values.
   *
   * @param arg The argument to check.
   * @param invalidValues The values {@code arg} must not equal.
   * @throws IllegalArgumentException if {@code arg} equals one of the values.
   */
  public static void isNotIn(char arg, char... invalidValues) {
    for (char iv : invalidValues) {
      if (arg == iv) {
        throw new IllegalArgumentException("Argument must not be equal to " + iv);
      }
    }
  }

  /**
   * Verifies that an argument is not equal to any of the given values.
   *
   * @param arg The argument to check.
   * @param invalidValues The values {@code arg} must not equal.
   * @throws IllegalArgumentException if {@code arg} equals one of the values.
   */
  public static void isNotIn(int arg, int... invalidValues) {
    for (int iv : invalidValues) {
      if (arg == iv) {
        throw new IllegalArgumentException("Argument must not be equal to " + iv);
      }
    }
  }

  /**
   * Verifies that an argument is not equal to any of the given values.
   *
   * @param arg The argument to check.
   * @param invalidValues The values {@code arg} must not equal.
   * @throws IllegalArgumentException if {@code arg} equals one of the values.
   */
  public static void isNotIn(long arg, long... invalidValues) {
    for (long iv : invalidValues) {
      if (arg == iv) {
        throw new IllegalArgumentException("Argument must not be equal to " + iv);
      }
    }
  }

  /**
   * Verifies that an argument is not equal to any of the given values.
   *
   * @param arg The argument to check.
   * @param invalidValues The values {@code arg} must not equal.
   * @throws IllegalArgumentException if {@code arg} equals one of the values.
   */
  public static void isNotIn(double arg, double... invalidValues) {
    for (double iv : invalidValues) {
      if (arg == iv) {
        throw new IllegalArgumentException("Argument must not be equal to " + iv);
      }
    }
  }

  /**
   * Verifies that an argument is not equal to any of the given values. A {@code null} {@code arg} is rejected
   * only if a {@code null} value is listed.
   *
   * @param arg The argument to check.
   * @param invalidValues The values {@code arg} must not equal.
   * @throws IllegalArgumentException if {@code arg} equals one of the values.
   */
  public static <T> void isNotIn(T arg, T... invalidValues) {
    if (arg == null) {
      for (T iv : invalidValues) {
        if (iv == null) {
          throw new IllegalArgumentException("Argument must not be null");
        }
      }
    }
    else {
      for (T iv : invalidValues) {
        if (arg.equals(iv)) {
          throw new IllegalArgumentException("Argument must not be equal to " + iv);
        }
      }
    }
  }

  /**
   * Verifies that a string argument is empty.
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The string to check; must not be {@code null}.
   * @throws IllegalArgumentException if {@code arg} is not empty.
   * @throws NullPointerException if {@code arg} is {@code null}.
   */
  public static void isEmpty(String argName, String arg) {
    if (!arg.isEmpty()) {
      throw new IllegalArgumentException("Argument '" + argName + "' must be empty, but contains '" + arg + "'");
    }
  }

  /**
   * Verifies that a string argument is empty.
   *
   * @param arg The string to check; must not be {@code null}.
   * @throws IllegalArgumentException if {@code arg} is not empty.
   * @throws NullPointerException if {@code arg} is {@code null}.
   */
  public static void isEmpty(String arg) {
    if (!arg.isEmpty()) {
      throw new IllegalArgumentException("Argument must be empty, but contains '" + arg + "'");
    }
  }

  /**
   * Verifies that a string argument is not empty.
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The string to check; must not be {@code null}.
   * @throws IllegalArgumentException if {@code arg} is empty.
   * @throws NullPointerException if {@code arg} is {@code null}.
   */
  public static void isNotEmpty(String argName, String arg) {
    if (arg.isEmpty()) {
      throw new IllegalArgumentException("Argument '" + argName + "' must not be empty");
    }
  }

  /**
   * Verifies that a string argument is not empty.
   *
   * @param arg The string to check; must not be {@code null}.
   * @throws IllegalArgumentException if {@code arg} is empty.
   * @throws NullPointerException if {@code arg} is {@code null}.
   */
  public static void isNotEmpty(String arg) {
    if (arg.isEmpty()) {
      throw new IllegalArgumentException("Argument must not be empty");
    }
  }

  /**
   * Verifies that a collection argument is not empty.
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The collection to check; must not be {@code null}.
   * @throws IllegalArgumentException if {@code arg} is empty.
   * @throws NullPointerException if {@code arg} is {@code null}.
   */
  public static void isNotEmpty(String argName, Collection<?> arg) {
    if (arg.isEmpty()) {
      throw new IllegalArgumentException("Argument '" + argName + "' must not be empty");
    }
  }

  /**
   * Verifies that a collection argument is not empty.
   *
   * @param arg The collection to check; must not be {@code null}.
   * @throws IllegalArgumentException if {@code arg} is empty.
   * @throws NullPointerException if {@code arg} is {@code null}.
   */
  public static void isNotEmpty(Collection<?> arg) {
    if (arg.isEmpty()) {
      throw new IllegalArgumentException("Argument must not be empty");
    }
  }

  /**
   * Verifies that a map argument is not empty.
   *
   * @param arg The map to check; must not be {@code null}.
   * @throws IllegalArgumentException if {@code arg} is empty.
   * @throws NullPointerException if {@code arg} is {@code null}.
   */
  public static void isNotEmpty(Map<?, ?> arg) {
    if (arg.isEmpty()) {
      throw new IllegalArgumentException("Argument must not be empty");
    }
  }

  /**
   * Verifies that a array argument is not empty.
   *
   * @param argName Name of the argument, used in the exception message.
   * @param arg The array to check; must not be {@code null}.
   * @throws IllegalArgumentException if {@code arg} is empty.
   * @throws NullPointerException if {@code arg} is {@code null}.
   */
  public static <T> void isNotEmpty(String argName, T[] arg) {
    if (arg.length == 0) {
      throw new IllegalArgumentException("Argument '" + argName + "' must not be empty");
    }
  }

  /**
   * Verifies that a array argument is not empty.
   *
   * @param arg The array to check; must not be {@code null}.
   * @throws IllegalArgumentException if {@code arg} is empty.
   * @throws NullPointerException if {@code arg} is {@code null}.
   */
  public static <T> void isNotEmpty(T[] arg) {
    if (arg.length == 0) {
      throw new IllegalArgumentException("Argument must not be empty");
    }
  }
}
