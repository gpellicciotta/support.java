// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import java.util.Arrays;
import java.util.Collection;

/**
 *  A collection of checks.
 */
public abstract class Checks {

  /** Prevent instantiation */
  private Checks() { }

  /**
   * Check that an object is not <code>null</code>.
   *
   * @param o The object to check.
   *
   * @return True when if not <code>null</code>, false otherwise.
   */
  public static boolean isNotNull(Object o) {
    return o != null;
  }

  /**
   * Check that two objects are equal.
   *
   * Two objects o1 and o2 are considered equal if (o1 == null ? o2 == null : o1.equals(o2))
   *
   * @param o1 First object to compare.
   * @param o2 Second object to compare.
   *
   * @return True when both objects are equal, false otherwise.
   */
  public static boolean areEqual(Object o1, Object o2) {
    return (o1 == null ? o2 == null : o1.equals(o2));
  }

  /**
   * Check that two object arrays are equal.
   *
   * Returns true if the two specified arrays of Objects are equal to one another. The two arrays are considered equal if both
   * arrays contain the same number of elements, and all corresponding pairs of elements in the two arrays are equal. Two objects e1
   * and e2 are considered equal if (e1 == null ? e2 == null : e1.equals(e2)). In other words, the two arrays are equal if they
   * contain the same elements in the same order. Also, two arrays are considered equal if both are null.
   *
   * @param oa1 First object array to compare.
   * @param oa2 Second object array to compare.
   *
   * @return True when both object arrays are equal, false otherwise.
   */
  public static boolean areEqual(Object[] oa1, Object[] oa2) {
    return Arrays.equals(oa1, oa2);
  }

  /**
   * Check that a string argument has a length within the specified range.
   *
   * @param value String whose length needs to be checked.
   * @param minLength Minimum character length, inclusive.
   * @param maxLength Maximum character length, inclusive.
   *
   * @return True when value is not null and value.length() in [minLength, maxLength], false otherwise.
   */
  public static boolean hasLengthInRange(String value, int minLength, int maxLength) {
    if (value == null) {
      return false;
    }
    return ((value.length() >= minLength) && (value.length() <= maxLength));
  }

  /**
   * Check that a value falls within a specified range.
   *
   * @param value String whose length needs to be checked.
   * @param minValue Minimum character length, inclusive.
   * @param maxValue Maximum character length, inclusive.
   *
   * @return True when value in range [minValue, maxValue], false otherwise.
   */
  public static boolean isInRange(int value, int minValue, int maxValue) {
    return ((value >= minValue) && (value <= maxValue));
  }

  /**
   * Check that a value falls within a specified range.
   *
   * @param value String whose length needs to be checked.
   * @param minValue Minimum character length, inclusive.
   * @param maxValue Maximum character length, inclusive.
   *
   * @return True when value in range [minValue, maxValue], false otherwise.
   */
  public static boolean isInRange(long value, long minValue, long maxValue) {
    return ((value >= minValue) && (value <= maxValue));
  }

  /**
   * Check that a value falls within a specified range.
   *
   * @param value String whose length needs to be checked.
   * @param minValue Minimum character length, inclusive.
   * @param maxValue Maximum character length, inclusive.
   *
   * @return True when value is not null and in range [minValue, maxValue], false otherwise.
   */
  public static boolean isInRange(Comparable value, Comparable minValue, Comparable maxValue) {
    return (value != null) && ((value.compareTo(minValue) >= 0) && (value.compareTo(maxValue) <= 0));
  }

  /**
   * Check that a string value is present in a specified collection of valid values.
   *
   * @param value Value to be checked in a case-sensitive fashion.
   * @param validValues Array of valid values.
   *
   * @return True if value is one of the valid values, false otherwise.
   */
  public static boolean isIn(String value, String... validValues) {
    return isIn(value, Arrays.asList(validValues));
  }

  /**
   * Check that a string value is present in a specified collection of valid values.
   *
   * @param value Value to be checked in a case-insensitive fashion.
   * @param validValues Array of valid values.
   *
   * @return True if value is one of the valid values, false otherwise.
   */
  public static boolean isInIgnoringCase(String value, String... validValues) {
    if (validValues == null) {
      return false;
    }
    for (String vv : validValues) {
      if (vv.equalsIgnoreCase(value)) {
        return false;
      }
    }
    return false;
  }

  /**
   * Check that a value is present in a specified collection of valid values.
   *
   * @param value Value to be checked.
   * @param validValues Array of valid values.
   *
   * @return True if value is one of the valid values, false otherwise.
   */
  public static <T> boolean isIn(T value, T... validValues) {
    return isIn(value, Arrays.asList(validValues));
  }

  /**
   * Check that a value is present in a specified collection of valid values.
   *
   * @param value Value to be checked.
   * @param validValues Collection of valid values.
   *
   * @return True if value is one of the valid values, false otherwise or when validValues is null.
   */
  public static <T> boolean isIn(T value, Collection<T> validValues) {
    if (validValues == null) {
      return false;
    }
    for (Object vv : validValues) {
      if (vv.equals(value)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check that a value is not present in a specified collection of invalid values.
   *
   * @param value Value to be checked, with case sensitivity.
   * @param invalidValues Array of invalid values.
   *
   * @return False if value is one of the valid values, true otherwise or when invalidValues is null.
   */
  public static boolean isNotIn(String value, String... invalidValues) {
    return isNotIn(value, Arrays.asList(invalidValues));
  }

  /**
   * Check that a value is not present in a specified collection of invalid values.
   *
   * @param value Value to be checked in a case-insensitive fashion.
   * @param invalidValues Array of invalid values.
   *
   * @return False if value is one of the valid values, true otherwise or when invalidValues is null.
   */
  public static boolean isNotInIgnoringCase(String value, String... invalidValues) {
    if (invalidValues == null) {
      return true;
    }
    for (String vv : invalidValues) {
      if (vv.equalsIgnoreCase(value)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Check that a value is not present in a specified collection of invalid values.
   *
   * @param value Value to be checked.
   * @param invalidValues Array of invalid values.
   *
   * @return False if value is one of the valid values, true otherwise or when invalidValues is null.
   */
  public static <T> boolean isNotIn(T value, T... invalidValues) {
    return isNotIn(value, Arrays.asList(invalidValues));
  }

  /**
   * Check that a value is not present in a specified collection of invalid values.
   *
   * @param value Value to be checked.
   * @param invalidValues Collection of invalid values.
   *
   * @return False if value is one of the valid values, true otherwise or when invalidValues is null.
   */
  public static <T> boolean isNotIn(T value, Collection<T> invalidValues) {
    if (invalidValues == null) {
      return true;
    }
    for (Object vv : invalidValues) {
      if (vv.equals(value)) {
        return false;
      }
    }
    return true;
  }
}
