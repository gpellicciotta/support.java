// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *  A collection of byte related utility methods.
 */
public abstract class ByteUtils {

  /** Private to make this a static class. */
  private ByteUtils() { }

  /**
   * Set the specified bit in a byte to 1 (true) or 0 (false).
   *
   * @param b The byte whose bit needs to be set.
   * @param bit The bit to be set. Must be in range [0, 7].
   * @param on Whether to set the bit to 1 (true) or 0 (false).
   *
   * @throws IllegalArgumentException When bit is not in range: [0, 7].
   */
  public static void setBit(byte b, int bit, boolean on) throws IllegalArgumentException {
    Contract.isInRange("bit", bit, 0, 7);
    byte byteWithBitSet = (byte) ((byte) 1 << bit);
    if (on) {
      b = (byte) (b | byteWithBitSet);
    }
    else {
      byte byteWithBitUnset = (byte) ~byteWithBitSet;
      b = (byte) (b & byteWithBitUnset);
    }
  }

  /**
   * Determine the state of the specified bit.
   *
   * @param b The byte whose bit needs to be investigated.
   * @param bit The bit to be investigated. Must be in range [0, 7].
   *
   * @return True means the bit is set (1), false means it is unset (0).
   *
   * @throws IllegalArgumentException When bit is not in range: [0, 7].
   */
  public static boolean getBit(byte b, int bit) throws IllegalArgumentException {
    Contract.isInRange("bit", bit, 0, 7);
    byte byteWithBitSet = (byte) ((byte) 1 << bit);
    return ((b & byteWithBitSet) != 0);
  }

  /**
   *  Determine whether two byte arrays are byte-per-byte equal.
   *
   *  @param data1 The first byte[] to be compared.
   *  @param data2 The second byte[] to be compared.
   *
   *  @return True if equal, false if unequal or having unequal length.
   */
  public static boolean equals(byte[] data1, byte[] data2) {
    Contract.isNotNull("data1", data1);
    Contract.isNotNull("data2", data2);
    if (data1.length != data2.length) {
      return false;
    }
    return equals(data1, 0, data2, 0, data1.length);
  }

  /**
   *  Determine whether two byte slices are byte-per-byte equal.
   *
   *  @param data1 The first byte[] whose slice needs to be compared.
   *  @param offset1 Offset in data1 of the first byte to compare against the first byte of the second slice.
   *  @param data2 The second byte[] whose slice needs to be compared.
   *  @param offset2 Offset in data2 of the first byte to compare against the first byte of the first slice.
   *  @param length Number of bytes to compare.
   *
   *  @return Whether to consider both array slices equal or not.
   */
  public static boolean equals(byte[] data1, int offset1, byte[] data2, int offset2, int length) {
    Contract.isNotNull("data1", data1);
    Contract.isNotNull("data2", data2);
    Contract.isInRange("offset1", offset1, 0, data1.length);
    Contract.isInRange("offset2", offset2, 0, data2.length);
    Contract.isInRange("length", length, 0, data1.length - offset1);
    Contract.isInRange("length", length, 0, data2.length - offset2);
    if (length == 0) {
      return true;
    }
    for (int i = 0; i < length; i++) {
      if (data1[offset1 + i] != data2[offset2 + i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Compares two byte arrays. They are considered equal if, and only if:<ul>
   *   <li>They contain the exact same amount of elements</li>
   *   <li>All elements are equal</li>
   *   <li>All elements are in the exact same order</li>
   * </ul>
   * If both arrays contain the same bytes, but one is larger (and thus containing additional items),
   * the larger array is considered greater.
   * First non-equal byte determines the outcome of the comparison whereby byte values are compared numerically.
   *
   * @param data1 The first byte array.
   * @param data2 The second byte array.
   * @return A negative number if data1 less than data2; positive for data2 less than data1. Zero if equal.
   */
  public static int compare(byte[] data1, byte[] data2) {
    return compare(data1, 0, data1.length, data2, 0, data2.length);
  }

  /**
   * Compares two byte slices. They are considered equal if, and only if:<ul>
   *   <li>They contain the exact same amount of elements</li>
   *   <li>All elements are equal</li>
   *   <li>All elements are in the exact same order</li>
   * </ul>
   * If both array slices contain the same bytes, but one is larger (and thus containing additional items),
   * the larger array is considered greater.
   * First non-equal byte determines the outcome of the comparison whereby byte values are compared numerically.
   *
   * @param data1 The first byte array.
   * @param data1Offset The offset in which comparison should start in the first byte array.
   * @param data1Length The length of the first byte array (number of bytes to consider).
   * @param data2 The second byte array.
   * @param data2Offset Offset in the second byte array at which to start comparing.
   * @param data2Length The number of bytes to compare.
   * @return A negative number if data1 less than data2; positive for data2 less than data1. Zero if equal.
   */
  public static int compare(byte[] data1, int data1Offset, int data1Length, byte[] data2, int data2Offset, int data2Length) {
    Contract.isNotNull("data1", data1);
    Contract.isNotNull("data2", data2);
    Contract.isInRange("data1Offset", data1Offset, 0, data1.length - 1);
    Contract.isInRange("data2Length", data1Length, 0, data1.length - data1Offset);
    Contract.isInRange("data2Offset", data2Offset, 0, data2.length - 1);
    Contract.isInRange("data2Length", data2Length, 0, data2.length - data2Offset);

    int commonLen = Math.min(data1Length, data2Length);
    if (commonLen == 0) {
      return 0;
    }
    int r;
    for (int i = 0; i < commonLen; i++) {
      r = (data1[data1Offset + i] - data2[data2Offset + i]);
      if (r != 0) {
        return r;
      }
    }
    return data1Length - data2Length;
  }

  /**
   * Check whether a specific byte range starts with a specific sequence of bytes.
   *
   * @param data The data bytes to compare value bytes against.
   * @param value The value bytes to compare data bytes against.
   * @return Whether the first value.length bytes of data are identical to the bytes in value.
   */
  public static boolean startsWith(byte[] data, byte[] value) {
    return startsWith(data, 0, data.length, value, 0, value.length);
  }

  /**
   * Check whether a specific byte range starts with a specific sequence of bytes.
   *
   * @param data  The data bytes to compare value bytes against.
   * @param dataOffset Offset in data at which to start the comparison.
   * @param dataLength Number of bytes to consider in data.
   * @param value The value bytes to compare data bytes against.
   * @param valueOffset The offset in value which indicates the first byte to compare.
   * @param valueLength The number of bytes in value to consider for comparison.
   * @return Whether the first value.length bytes of data at dataOffset are identical to the corresponding bytes starting at valueOffset in value.
   *         Always false when valueLength is zero or greater than dataLength.
   */
  public static boolean startsWith(byte[] data, int dataOffset, int dataLength, byte[] value, int valueOffset, int valueLength) {
    Contract.isNotNull("data", data);
    Contract.isNotNull("value", value);

    if (valueLength == 0) {
      return false;
    }
    if (valueLength > dataLength) {
      return false;
    }

    Contract.isInRange("dataOffset", dataOffset, 0, data.length - 1);
    Contract.isInRange("dataLength", dataLength, 0, data.length - dataOffset);
    Contract.isInRange("valueOffset", valueOffset, 0, value.length - 1);
    Contract.isInRange("valueLength", valueLength, 0, value.length - valueOffset);

    for (int j = 0; j < valueLength; j++) {
      if (data[dataOffset + j] != value[j]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Check whether a specific byte range ends with a specific sequence of bytes.
   *
   * @param data The data bytes to compare value bytes against.
   * @param value The value bytes to compare data bytes against.
   * @return Whether the last value.length bytes of data are identical to the bytes in value.
   */
  public static boolean endsWith(byte[] data, byte[] value) {
    return endsWith(data, 0, data.length, value, 0, value.length);
  }

  /**
   * Check whether a specific byte range ends with a specific sequence of bytes.
   *
   * @param data The data bytes to compare value bytes against.
   * @param dataOffset First offset in data to consider part of the range of bytes to get compared.
   * @param dataLength Number of bytes to consider in data.
   * @param value The value bytes to compare data bytes against.
   * @param valueOffset First offset in value to consider part of the range of bytes to get compared.
   * @param valueLength The number of bytes in value to consider for comparison.
   * @return Whether the last value.length bytes of data at dataOffset are identical to the corresponding bytes starting at valueOffset in value.
   *         Always false when valueLength is zero or greater than dataLength.
   */
  public static boolean endsWith(byte[] data, int dataOffset, int dataLength, byte[] value, int valueOffset, int valueLength) {
    Contract.isNotNull("data", data);
    Contract.isNotNull("value", value);

    if (valueLength == 0) {
      return false;
    }
    if (valueLength > dataLength) {
      return false;
    }

    Contract.isInRange("dataOffset", dataOffset, 0, data.length - 1);
    Contract.isInRange("dataLength", dataLength, 0, data.length - dataOffset);
    Contract.isInRange("valueOffset", valueOffset, 0, value.length - 1);
    Contract.isInRange("valueLength", valueLength, 0, value.length - valueOffset);

    int startOffset = (dataOffset + dataLength) - valueLength;
    for (int j = 0; j < valueLength; j++) {
      if (data[startOffset + j] != value[j]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Check whether a specific byte range contains a specific sequence of bytes and returns the index of the first byte.
   *
   * @param data The data bytes to compare value bytes against.
   * @param value The value bytes to compare data bytes against.
   * @return -1 if data doesn't contain value or else the index of the first byte in data where value can be found.
   */
  public static boolean contains(byte[] data, byte[] value) {
    return indexOf(data, 0, data.length, value, 0, value.length) != -1;
  }

  /**
   * Check whether a specific byte range contains a specific sequence of bytes.
   *
   * @param data  The data bytes to compare value bytes against.
   * @param value The value bytes to compare data bytes against.
   * @return True only if there is a byte-sequence of length value.length inside data that contains the exact same bytes as value.
   */
  public static int indexOf(byte[] data, byte[] value) {
    return indexOf(data, 0, data.length, value, 0, value.length);
  }

  /**
   * Check whether a specific byte range contains a specific sequence of bytes.
   *
   * @param data The data bytes to compare value bytes against.
   * @param dataOffset First offset in data to consider part of the range of bytes to get compared.
   * @param dataLength Number of bytes to consider in data.
   * @param value The value bytes to compare data bytes against.
   * @param valueOffset First offset in value to consider part of the range of bytes to get compared.
   * @param valueLength The number of bytes in value to consider for comparison.
   * @return True only if there is a byte-sequence of length valueLength inside data[dataOffset:dataOffset+dataLength-1]
   *         that contains the exact same bytes as value[valueOffset:valueOffset+valueLength-1].
   */
  public static boolean contains(byte[] data, int dataOffset, int dataLength, byte[] value, int valueOffset, int valueLength) {
    return indexOf(data, dataOffset, dataLength, value, valueOffset, valueLength) != -1;
  }

  /**
   * Check whether a specific byte range contains a specific sequence of bytes and return the index where it can be found.
   *
   * @param data The data bytes to compare value bytes against.
   * @param dataOffset First offset in data to consider part of the range of bytes to get compared.
   * @param dataLength Number of bytes to consider in data.
   * @param value The value bytes to compare data bytes against.
   * @param valueOffset First offset in value to consider part of the range of bytes to get compared.
   * @param valueLength The number of bytes in value to consider for comparison.
   * @return The index of the first byte in data (and after dataOffset) where value can be found. -1 if value is not found within data[dataOffset:dataOffset+length[ or valueLength == 0.
   */
  public static int indexOf(byte[] data, int dataOffset, int dataLength, byte[] value, int valueOffset, int valueLength) {
    Contract.isNotNull("data", data);
    Contract.isNotNull("value", value);

    if (valueLength > dataLength) {
      return -1;
    }
    if (valueLength == 0) {
      return -1;
    }

    Contract.isInRange("dataOffset", dataOffset, 0, data.length - 1);
    Contract.isInRange("dataLength", dataLength, 0, data.length - dataOffset);
    Contract.isInRange("valueOffset", valueOffset, 0, value.length - 1);
    Contract.isInRange("valueLength", valueLength, 0, value.length - valueOffset);

    int endIdx = (dataOffset + dataLength) - valueLength;
    dataLoop: for (int i = dataOffset; i <= endIdx; i++) {
      if (data[i] == value[0]) {
        valueLoop: for (int j = 1; j < valueLength; j++) {
          if (data[i + j] != value[j]) {
            continue dataLoop;
          }
        }
        return i;
      }
    }
    return -1;
  }

  /**
   *  Reverse the bytes in a source array, in place.
   *
   *  @param data The source array to be reversed.
   */
  public static void reverse(byte[] data) {
    Contract.isNotNull("data", data);
    int i = 0;
    while (((data.length - 1 - (i * 2)) > 0)) {
      byte temp = data[i];
      // now swap
      data[i] = data[data.length - 1 - i];
      data[data.length - 1 - i] = temp;
      // and move to next
      i++;
    }
  }

  /**
   *  Create a reversed copy of a data array.
   *
   *  @param data The source array.
   *  @return A reversed version of the source array.
   */
  public static byte[] reversed(byte[] data) {
    Contract.isNotNull("data", data);
    byte[] target = new byte[data.length];
    for (int i = 0; i < data.length; i++) {
      target[target.length - i - 1] = data[i];
    }
    return target;
  }

  /**
   *  Create a new byte array that represents a range in an original source array.
   *
   *  @param data The source array to take a sub-range of.
   *  @param offset Offset of the first byte to put in the result array.
   *
   *  @return A sub-range of the data array, starting at offset and extending until the end of the data array.
   */
  public static byte[] slice(byte[] data, int offset) {
    return slice(data, offset, -1);
  }

  /**
   *  Create a new byte array that represents a range in an original source array.
   *
   *  @param data The source array to take a sub-range of.
   *  @param offset Offset of the first byte to put in the result array.
   *  @param length Number of bytes to put in the result array. If &lt; 0, all bytes from offset until the end of source will be taken.
   *
   *  @return A sub-range of the source array, starting at offset.
   */
  public static byte[] slice(byte[] data, int offset, int length) {
    Contract.isNotNull("data", data);
    Contract.isInRange("offset", offset, 0, data.length);
    if (length < 0) {
      length = data.length - offset;
    }
    Contract.isInRange("length", length, 0, data.length - offset);

    byte[] target = new byte[length];
    for (int j = 0, i = offset; (i < data.length) && (j < target.length); i++, j++) {
      target[j] = data[i];
    }
    return target;
  }

  /**
   *  Transform an array of bytes into a hex digits representation.
   *
   *  @param val The <c>byte[]</c> value to represent in hex form.
   *
   *  @return The same hex representation given by {@link StringUtils#toHexString(byte[])}.
   */
  public static String toHexString(byte[] val) {
    return StringUtils.toHexString(val, "{hex}");
  }

  /**
   *  Transform an array of characters that represent hex digits into an array of bytes representing
   *  the values of each pair of hex digits.
   *
   *  @param hexStr The string of hex digits. Letter 'a' thru 'z' may be specified in lower or uppercase.
   *                If this string contains an uneven number of characters, a single '0' will be preprended.
   *                If empty, a single byte with value 0 will be returned.
   *
   *  @return The array of bytes resulting from interpreting every pair of characters as 2 hex digits.
   *
   *  @exception NumberFormatException If any character is not a decimal digit '0'-'9' or letter 'a'-'f'.
   */
  public static byte[] fromHexString(String hexStr) {
    Contract.isNotNull("hexStr", hexStr);
    if (hexStr.isEmpty()) {
      return new byte[] { 0 };
    }
    if ((hexStr.length() % 2) != 0) { // Make sure it is a multiple of 2
      hexStr = "0" + hexStr;
    }
    int byteCount = hexStr.length() / 2; // Each two hex digits will form one byte
    byte[] bytes = new byte[byteCount];
    for (int i = 0; i < hexStr.length(); i += 2) {
      bytes[i / 2] = (byte) (Short.parseShort(hexStr.substring(i, i + 2), 16) & 0xff); // Using short since Byte cannot parse numbers above 127
    }
    return bytes;
  }

  /**
   *  Transform an array of characters that represent bit digits into an array of bytes representing
   *  the values of each group of 8 bit digits.
   *
   *  @param bitStr The string of bit digits, each '0' and '1'. Letter 'a' thru 'z' may be specified in lower or uppercase.
   *                If this string contains an number of characters that is not a multiple of 8, it will
   *                be prepended with minimally number of needed '0's to make it a multiple of 8.
   *                If empty, a single byte with value 0 will be returned.
   *
   *  @return The array of bytes resulting from interpreting every pair of characters as 2 hex digits.
   *
   *  @exception NumberFormatException If any character is not a '0' or '1'.
   */
  public static byte[] fromBitString(String bitStr) {
    Contract.isNotNull("bitStr", bitStr);
    if (bitStr.isEmpty()) {
      return new byte[] { 0 };
    }
    switch (bitStr.length() % 8) { // Make sure it is a multiple of 8
      case 7:
        bitStr = "0" + bitStr;
        break;
      case 6:
        bitStr = "00" + bitStr;
        break;
      case 5:
        bitStr = "000" + bitStr;
        break;
      case 4:
        bitStr = "0000" + bitStr;
        break;
      case 3:
        bitStr = "00000" + bitStr;
        break;
      case 2:
        bitStr = "000000" + bitStr;
        break;
      case 1:
        bitStr = "0000000" + bitStr;
        break;
    }
    int byteCount = bitStr.length() / 8; // Each 8 bits will form one byte
    byte[] bytes = new byte[byteCount];
    for (int i = 0; i < bitStr.length(); i += 8) {
      bytes[i / 8] = (byte)(Short.parseShort(bitStr.substring(i, i + 8), 2) & 0xff); // Using short since Byte cannot parse numbers above 127
    }
    return bytes;
  }

  /**
   *  Compress the data.
   *
   *  @param rawData The data value to be compressed.
   *  @return The compressed data, which can be uncompressed again with {@link #uncompress(byte[])}.
   */
  public static byte[] compress(byte[] rawData) throws IOException {
    Contract.isNotNull("rawData", rawData);
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
        gzip.write(rawData);
      }
      return out.toByteArray();
    }
  }

  /**
   *  Uncompress data that was previously compressed with {@link #compress(byte[])}.
   *
   *  @param compressedData The data value to be uncompressed.
   *  @return The raw data after uncompression.
   */
  public static byte[] uncompress(byte[] compressedData) throws IOException {
    Contract.isNotNull("compressedData", compressedData);
    try (ByteArrayInputStream in = new ByteArrayInputStream(compressedData)) {
      try (GZIPInputStream gunzip = new GZIPInputStream(in)) {
        return gunzip.readAllBytes();
      }
    }
  }

  /**
   *  Returns the MD5 hash of the provided data.
   *
   *  @param data The data value to be hashed.
   *  @return The 16-byte MD5 hash value.
   */
  public static byte[] md5(byte[] data) throws NoSuchAlgorithmException {
    Contract.isNotNull("data", data);
    MessageDigest md5 = MessageDigest.getInstance("MD5");
    byte[] hash = md5.digest(data);
    return hash;
  }

  /**
   *  Returns the SHA-1 hash of the provided data.
   *
   *  @param data The data value to be hashed.
   *  @return The 20-byte SHA-1 hash value.
   */
  public static byte[] sha1(byte[] data) throws NoSuchAlgorithmException {
    Contract.isNotNull("data", data);
    MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
    byte[] hash = sha1.digest(data);
    return hash;
  }

  /**
   *  Returns the SHA-256 hash of the provided data.
   *
   *  @param data The data value to be hashed.
   *  @return The 32-byte SHA-256 hash value.
   */
  public static byte[] sha256(byte[] data) throws NoSuchAlgorithmException {
    Contract.isNotNull("data", data);
    MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
    byte[] hash = sha1.digest(data);
    return hash;
  }
}
