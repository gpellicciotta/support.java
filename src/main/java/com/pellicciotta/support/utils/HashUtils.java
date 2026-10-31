// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *  A collection of utility methods for creating hashes of data.
 */
public abstract class HashUtils {

  /** Private to make this a static class. */
  private HashUtils() { }

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
