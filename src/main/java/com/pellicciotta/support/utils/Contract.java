// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import java.util.Collection;
import java.util.Map;

/**
 *  A collection of precondition checks.
 */
public final class Contract {
  /** Prevent instantiation */
  private Contract() {  }

  public static void isTrue(String conditionDescription, boolean condition) {
    if (!condition) {
      throw new IllegalArgumentException("Condition '" + conditionDescription + "' must be true");
    }
  }

  public static void isTrue(boolean condition) {
    if (!condition) {
      throw new IllegalArgumentException("Condition must not be true");
    }
  }

  public static void isFalse(String conditionDescription, boolean condition) {
    if (condition) {
      throw new IllegalArgumentException("Condition '" + conditionDescription + "' must be true");
    }
  }

  public static void isFalse(boolean condition) {
    if (condition) {
      throw new IllegalArgumentException("Condition must be false");
    }
  }

  public static void isNotNull(String argName, Object arg) {
    if (arg == null) {
      throw new IllegalArgumentException("Argument '" + argName + "' must not be null");
    }
  }

  public static void isNotNull(Object arg) {
    if (arg == null) {
      throw new IllegalArgumentException("Argument must not be null");
    }
  }

  public static void isZero(String argName, int arg) {
    if (arg != 0) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is not zero");
    }
  }

  public static void isZero(int arg) {
    if (arg != 0) {
      throw new IllegalArgumentException("Argument value " + arg + " is not zero");
    }
  }

  public static void isNotZero(String argName, int arg) {
    if (arg == 0) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is zero");
    }
  }

  public static void isNotZero(int arg) {
    if (arg == 0) {
      throw new IllegalArgumentException("Argument value " + arg + " is zero");
    }
  }

  public static void isPositive(String argName, int arg) {
    if (arg <= 0) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is not positive");
    }
  }

  public static void isPositive(int arg) {
    if (arg <= 0) {
      throw new IllegalArgumentException("Argument value " + arg + " is not positive");
    }
  }

  public static void isNotPositive(String argName, int arg) {
    if (arg > 0) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is positive");
    }
  }

  public static void isNotPositive(int arg) {
    if (arg > 0) {
      throw new IllegalArgumentException("Argument value " + arg + " is positive");
    }
  }

  public static void isNegative(String argName, int arg) {
    if (arg >= 0) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is not negative");
    }
  }

  public static void isNegative(int arg) {
    if (arg >= 0) {
      throw new IllegalArgumentException("Argument value " + arg + " is not negative");
    }
  }

  public static void isNotNegative(String argName, int arg) {
    if (arg < 0) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is negative");
    }
  }

  public static void isNotNegative(int arg) {
    if (arg < 0) {
      throw new IllegalArgumentException("Argument value " + arg + " is negative");
    }
  }

  public static void isInRange(String argName, int arg, int start, int end) {
    if ((arg < start) || (arg > end)) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is not in range [" + start + ", " + end + "]");
    }
  }

  public static void isInRange(int arg, int start, int end) {
    if ((arg < start) || (arg > end)) {
      throw new IllegalArgumentException("Argument value " + arg + " is not in range [" + start + ", " + end + "]");
    }
  }

  public static void isNotInRange(String argName, int arg, int start, int end) {
    if ((arg >= start) && (arg <= end)) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is in range [" + start + ", " + end + "]");
    }
  }

  public static void isNotInRange(int arg, int start, int end) {
    if ((arg >= start) && (arg <= end)) {
      throw new IllegalArgumentException("Argument value " + arg + " is in range [" + start + ", " + end + "]");
    }
  }

  public static void isZero(String argName, long arg) {
    if (arg != 0) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is not zero");
    }
  }

  public static void isZero(long arg) {
    if (arg != 0) {
      throw new IllegalArgumentException("Argument value " + arg + " is not zero");
    }
  }

  public static void isNotZero(String argName, long arg) {
    if (arg == 0) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is zero");
    }
  }

  public static void isNotZero(long arg) {
    if (arg == 0) {
      throw new IllegalArgumentException("Argument value " + arg + " is zero");
    }
  }

  public static void isPositive(String argName, long arg) {
    if (arg <= 0) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is not positive");
    }
  }

  public static void isPositive(long arg) {
    if (arg <= 0) {
      throw new IllegalArgumentException("Argument value " + arg + " is not positive");
    }
  }

  public static void isNotPositive(String argName, long arg) {
    if (arg > 0) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is positive");
    }
  }

  public static void isNotPositive(long arg) {
    if (arg > 0) {
      throw new IllegalArgumentException("Argument value " + arg + " is positive");
    }
  }

  public static void isNegative(String argName, long arg) {
    if (arg >= 0) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is not negative");
    }
  }

  public static void isNegative(long arg) {
    if (arg >= 0) {
      throw new IllegalArgumentException("Argument value " + arg + " is not negative");
    }
  }

  public static void isNotNegative(String argName, long arg) {
    if (arg < 0) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is negative");
    }
  }

  public static void isNotNegative(long arg) {
    if (arg < 0) {
      throw new IllegalArgumentException("Argument value " + arg + " is negative");
    }
  }

  public static void isInRange(String argName, long arg, long start, long end) {
    if ((arg < start) || (arg > end)) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is not in range [" + start + ", " + end + "]");
    }
  }

  public static void isInRange(long arg, long start, long end) {
    if ((arg < start) || (arg > end)) {
      throw new IllegalArgumentException("Argument value " + arg + " is not in range [" + start + ", " + end + "]");
    }
  }

  public static void isNotInRange(String argName, long arg, long start, long end) {
    if ((arg >= start) && (arg <= end)) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is in range [" + start + ", " + end + "]");
    }
  }

  public static void isNotInRange(long arg, long start, long end) {
    if ((arg >= start) && (arg <= end)) {
      throw new IllegalArgumentException("Argument value " + arg + " is in range [" + start + ", " + end + "]");
    }
  }

  public static void isZero(String argName, double arg) {
    if (arg != 0.0d) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is not zero");
    }
  }

  public static void isZero(double arg) {
    if (arg != 0.0d) {
      throw new IllegalArgumentException("Argument value " + arg + " is not zero");
    }
  }

  public static void isNotZero(String argName, double arg) {
    if (arg == 0.0d) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is zero");
    }
  }

  public static void isNotZero(double arg) {
    if (arg == 0.0d) {
      throw new IllegalArgumentException("Argument value " + arg + " is zero");
    }
  }

  public static void isPositive(String argName, double arg) {
    if (arg <= 0.0d) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is not positive");
    }
  }

  public static void isPositive(double arg) {
    if (arg <= 0.0d) {
      throw new IllegalArgumentException("Argument value " + arg + " is not positive");
    }
  }

  public static void isNotPositive(String argName, double arg) {
    if (arg > 0.0d) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is positive");
    }
  }

  public static void isNotPositive(double arg) {
    if (arg > 0.0d) {
      throw new IllegalArgumentException("Argument value " + arg + " is positive");
    }
  }

  public static void isNegative(String argName, double arg) {
    if (arg >= 0.0d) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is not negative");
    }
  }

  public static void isNegative(double arg) {
    if (arg >= 0.0d) {
      throw new IllegalArgumentException("Argument value " + arg + " is not negative");
    }
  }

  public static void isNotNegative(String argName, double arg) {
    if (arg < 0.0d) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is negative");
    }
  }

  public static void isNotNegative(double arg) {
    if (arg < 0.0d) {
      throw new IllegalArgumentException("Argument value " + arg + " is negative");
    }
  }

  public static void isInRange(String argName, double arg, double start, double end) {
    if ((arg < start) || (arg > end)) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is not in range [" + start + ", " + end + "]");
    }
  }

  public static void isInRange(double arg, double start, double end) {
    if ((arg < start) || (arg > end)) {
      throw new IllegalArgumentException("Argument value " + arg + " is not in range [" + start + ", " + end + "]");
    }
  }

  public static void isNotInRange(String argName, double arg, double start, double end) {
    if ((arg >= start) && (arg <= end)) {
      throw new IllegalArgumentException("Argument '" + argName + "' with value " + arg + " is in range [" + start + ", " + end + "]");
    }
  }

  public static void isNotInRange(double arg, double start, double end) {
    if ((arg >= start) && (arg <= end)) {
      throw new IllegalArgumentException("Argument value " + arg + " is in range [" + start + ", " + end + "]");
    }
  }

  public static void isIn(char arg, char... invalidValues) {
    for (char iv : invalidValues) {
      if (arg == iv) {
        throw new IllegalArgumentException("Argument value must not be equal to " + iv);
      }
    }
  }

  public static void isIn(int arg, int... invalidValues) {
    for (int iv : invalidValues) {
      if (arg == iv) {
        throw new IllegalArgumentException("Argument must not be equal to " + iv);
      }
    }
  }

  public static void isIn(long arg, long... invalidValues) {
    for (long iv : invalidValues) {
      if (arg == iv) {
        throw new IllegalArgumentException("Argument must not be equal to " + iv);
      }
    }
  }

  public static void isIn(double arg, double... invalidValues) {
    for (double iv : invalidValues) {
      if (arg == iv) {
        throw new IllegalArgumentException("Argument must not be equal to " + iv);
      }
    }
  }

  public static <T> void isIn(T arg, T... validValues) {
    if (arg == null) {
      for (T iv : validValues) {
        if (iv == null) {
          return ;
        }
      }
      throw new IllegalArgumentException("Argument must not be null");
    }
    else {
      boolean first = true;
      StringBuilder sb = new StringBuilder();
      for (T iv : validValues) {
        if (arg.equals(iv)) {
          return ;
        }
        if (first) {
          first = false;
        }
        else {
          sb.append(", ");
        }
        sb.append("'");
        sb.append(iv.toString());
        sb.append("'");
        throw new IllegalArgumentException("Argument must not equal to one of " + iv);
      }
    }
  }

  public static void isNotIn(char arg, char... invalidValues) {
    for (char iv : invalidValues) {
      if (arg == iv) {
        throw new IllegalArgumentException("Argument must not be equal to " + iv);
      }
    }
  }

  public static void isNotIn(int arg, int... invalidValues) {
    for (int iv : invalidValues) {
      if (arg == iv) {
        throw new IllegalArgumentException("Argument must not be equal to " + iv);
      }
    }
  }

  public static void isNotIn(long arg, long... invalidValues) {
    for (long iv : invalidValues) {
      if (arg == iv) {
        throw new IllegalArgumentException("Argument must not be equal to " + iv);
      }
    }
  }

  public static void isNotIn(double arg, double... invalidValues) {
    for (double iv : invalidValues) {
      if (arg == iv) {
        throw new IllegalArgumentException("Argument must not be equal to " + iv);
      }
    }
  }

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

  public static void isEmpty(String argName, String arg) {
    if (!arg.isEmpty()) {
      throw new IllegalArgumentException("Argument '" + argName + "' must be empty, but contains '" + arg + "'");
    }
  }

  public static void isEmpty(String arg) {
    if (!arg.isEmpty()) {
      throw new IllegalArgumentException("Argument must be empty, but contains '" + arg + "'");
    }
  }

  public static void isNotEmpty(String argName, String arg) {
    if (arg.isEmpty()) {
      throw new IllegalArgumentException("Argument '" + argName + "' must not be empty");
    }
  }

  public static void isNotEmpty(String arg) {
    if (arg.isEmpty()) {
      throw new IllegalArgumentException("Argument must not be empty");
    }
  }

  public static void isNotEmpty(String argName, Collection<?> arg) {
    if (arg.isEmpty()) {
      throw new IllegalArgumentException("Argument '" + argName + "' must not be empty");
    }
  }

  public static void isNotEmpty(Collection<?> arg) {
    if (arg.isEmpty()) {
      throw new IllegalArgumentException("Argument must not be empty");
    }
  }

  public static void isNotEmpty(Map<?, ?> arg) {
    if (arg.isEmpty()) {
      throw new IllegalArgumentException("Argument must not be empty");
    }
  }

  public static <T> void isNotEmpty(String argName, T[] arg) {
    if (arg.length == 0) {
      throw new IllegalArgumentException("Argument '" + argName + "' must not be empty");
    }
  }

  public static <T> void isNotEmpty(T[] arg) {
    if (arg.length == 0) {
      throw new IllegalArgumentException("Argument must not be empty");
    }
  }
}
