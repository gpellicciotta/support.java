// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import java.io.*;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pellicciotta.support.logging.Log;
import com.pellicciotta.support.logging.LogService;

/**
 * A better {@link java.util.Random}.
 */
public final class Random
{
  /** Prevent instantiation */
  private Random() { }

  private static final java.util.Random rnd = new java.util.Random();

  // <editor-fold desc="Operations">

  /**
   *  Return a random UUID.
   *
   *  @return A UUID value: a 128-bit long value represented in four groups as <code>xxxxxxxx-xxx-xxx-xxx-xxxxxxxxxxxx</code>.
   */
  public static String uuid() {
    return UUID.randomUUID().toString();
  }

  /**
   *  Return a random UUID, as bytes.
   *
   *  @return A UUID value: a 128-bit long value represented as bytes.
   */
  public static byte[] uuidBytes() {
    UUID uuid = UUID.randomUUID();
    ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
    bb.putLong(uuid.getMostSignificantBits());
    bb.putLong(uuid.getLeastSignificantBits());
    return bb.array();
  }

  /**
   *  Return a random enum value.
   *
   *  @param enumType The enum class to pick a value from.
   */
  public static <T extends Enum<T>> T value(Class<T> enumType) {
    return element(enumType.getEnumConstants());
  }

  /**
   *  Return a random element.
   *
   *  @param array The array to pick a random element from. This array must not be null or empty, otherwise an {@link IllegalArgumentException} will be thrown.
   */
  public static <T> T element(T[] array) {
    Contract.isNotNull(array);
    Contract.isNotEmpty(array);
    int idx = intInRange(array.length - 1);
    return array[idx];
  }

  /**
   *  Return a random element.
   *
   *  @param collection The collection to pick a random element from. This collection must not be null or empty, otherwise an {@link IllegalArgumentException} will be thrown.
   */
  public static <T> T element(Collection<T> collection) {
    Contract.isNotNull(collection);
    Contract.isNotEmpty(collection);
    Object[] array = collection.toArray();
    return (T)element(array);
  }

  /**
   *  Return a random key.
   *
   *  @param map The map to pick a random key from. This map must not be null or empty, otherwise an {@link IllegalArgumentException} will be thrown.
   */
  public static <K, V> K key(Map<K, V> map) {
    Contract.isNotNull(map);
    Contract.isNotEmpty(map);
    return element(map.keySet());
  }

  /**
   *  Return a random value.
   *
   *  @param map The map to pick a random value from. This map must not be null or empty, otherwise an {@link IllegalArgumentException} will be thrown.
   */
  public static <K, V> V value(Map<K, V> map) {
    Contract.isNotNull(map);
    Contract.isNotEmpty(map);
    return element(map.values());
  }

  /**
   *  Return a random sequence of bytes.
   *
   *  @param length The length of the to-be returned byte array.
   *
   *  @return An array of the requested length filled with random bytes.
   */
  public static byte[] bytes(int length) {
    SecureRandom random = new SecureRandom();
    byte[] bytes = new byte[length];
    random.nextBytes(bytes);
    return bytes;
  }

  /**
   *  Return a random sequence of bytes encoded as hex-pairs.
   *
   *  @param length The number of random bytes to generate.
   *
   *  @return A hex-encoded string representing length random bytes. All hex pairs will be represented in lowercase.
   *          The length of the string will be <code>length x 2</code>.
   */
  public static String hexString(int length) {
    byte[] data = bytes(length);
    return ByteUtils.toHexString(data);
  }

  /**
   *  Return a random sequence of bytes encoded as base64.
   *
   *  @param length The number of random bytes to generate.
   *
   *  @return A base64-encoded string representing length random bytes.
   */
  public static String base64String(int length) {
    byte[] data = bytes(length);
    return Base64.getEncoder().encodeToString(data);
  }

  private static final String RANDOM_STRING_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

  /**
   *  Return a random string of the specified length, containing alphanumeric characters.
   *
   *  @param length The length of the to-be returned string.
   *
   *  @return A string of the requested length with lower and uppercase alphanumeric characters only.
   */
  public static String string(int length) {
    return string(length, RANDOM_STRING_CHARS);
  }

  /**
   *  Return a random string of a random (but constrained) length, containing alphanumeric characters.
   *
   *  @param minLength The min. length of the to-be returned string.
   *  @param maxLength The max. length of the to-be returned string.
   *
   *  @return A string of lower and uppercase alphanumeric characters only.
   */
  public static String string(int minLength, int maxLength) {
    Contract.isNotNegative("minLength", minLength);
    Contract.isInRange("maxLength", maxLength, minLength, Integer.MAX_VALUE);
    int len = minLength;
    if (minLength != maxLength) {
      len = intInRange(minLength, maxLength);
    }
    return string(len, RANDOM_STRING_CHARS);
  }

  /**
   *  Return a random string of the specified length, containing characters chosen randomly from the provided set.
   *
   *  @param length The length of the to-be returned string.
   *  @param validChars The set of valid characters to choose from. If this contains duplicates, these characters will have a higher chance of being used.
   *
   *  @return A string of the requested length with only the valid characters.
   */
  public static String string(int length, String validChars) {
    Contract.isNotNegative("length", length);
    StringBuilder sb = new StringBuilder();
    rnd.ints(length, 0, validChars.length()).forEachOrdered((i) -> {
      sb.append(validChars.charAt(i));
    });
    return sb.toString();
  }

  /**
   *  Return a random integer.
   *
   *  @return A random int value.
   */
  public static int anyInt() {
    return rnd.nextInt();
  }

  /**
   *  Return a random integer between zero and max, so literally in the range <code>[0, max]</code>
   *  where both bounds are inclusive, i.e. there is a chance they get returned too.
   *
   *  @param max Max. value to return, inclusive. Must be larger than zero.
   */
  public static int intInRange(int max) {
    return intInRange(0, max);
  }

  /**
   *  Return a random integer between min and max, so literally in the range <code>[min, max]</code>
   *  where both bounds are inclusive, i.e. there is a chance they get returned too.
   *
   *  @param min Min. value to return, inclusive.
   *  @param max Max. value to return, inclusive. Must be strictly larger than min.
   */
  public static int intInRange(int min, int max) {
    Contract.isTrue(max > min);
    int bound = ((max - min) + 1);
    int num = rnd.nextInt(bound);
    return min + num;
  }

  /**
   *  Return a random long.
   *
   *  @return A random long value.
   */
  public static long anyLong() {
    return rnd.nextLong();
  }

  /**
   *  Return a random long between zero and max, so literally in the range <code>[0, max]</code>
   *  where both bounds are inclusive, i.e. there is a chance they get returned too.
   *
   *  @param max Max. value to return, inclusive. Must be larger than zero.
   */
  public static long longInRange(long max) {
    return longInRange(0, max);
  }

  /**
   *  Return a random long between min and max, so literally in the range <code>[min, max]</code>
   *  where both bounds are inclusive, i.e. there is a chance they get returned too.
   *
   *  @param min Min. value to return, inclusive.
   *  @param max Max. value to return, inclusive. Must be strictly larger than min.
   */
  public static long longInRange(long min, long max) {
    Contract.isTrue(max > min);
    long bound = ((max - min) + 1);
    if (bound < Integer.MAX_VALUE) {
      long num = (long)intInRange((int) bound);
      return num + min;
    }
    else {
      return rnd.longs(min, max + 1).findFirst().getAsLong();
    }
  }

  /**
   *  Return a random boolean.
   *
   *  @return A random boolean value.
   */
  public static boolean anyBool() {
    return rnd.nextBoolean();
  }

  /**
   *  Return a random double.
   *
   *  @return A random double value.
   */
  public static double anyDouble() {
    return rnd.nextDouble();
  }

  /**
   *  Return a random {@link Date} between min and max, so literally in the range <code>[min, max]</code>
   *  where both bounds are inclusive, i.e. there is a chance they get returned too.
   *
   *  @param min Min. date value to return, inclusive.
   *  @param max Max. date value to return, inclusive. Must be strictly larger than min.
   */
  public static Date dateInRange(Date min, Date max) {
    long earliest = min.getTime();
    long latest = max.getTime();
    long actual = longInRange(earliest, latest);
    return new Date(actual);
  }

  // </editor-fold>
}
