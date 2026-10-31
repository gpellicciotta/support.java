// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

import com.pellicciotta.support.exceptions.FormatException;

/**
 *  A value type consisting of two element: a salt and a password hash that was calculated by that salt.
 */
public final class SaltedPasswordHash
{
  // <editor-fold desc="Construction">

  /**
   *  Create a new value.
   *
   *  @param salt The salt that was used to generated the password hash.
   *  @param passwordHash The password hash.
   */
  SaltedPasswordHash(byte[] salt, byte[] passwordHash) {
    assert salt != null;
    assert passwordHash != null;

    this.salt = salt;
    this.passwordHash = passwordHash;
  }

  final byte[] salt;
  final byte[] passwordHash;

  // </editor-fold>
  // <editor-fold desc="Operations">

  /**
   *  Interpret a string value as a {@link SaltedPasswordHash} value.
   *
   *  @param val The value to interpret.
   *
   *  @return The decoded {@link SaltedPasswordHash} value.
   *
   *  @exception FormatException If val cannot be decided into a valid value.
   */
  public static SaltedPasswordHash decode(String val) {
    Contract.isNotNull("val", val);
    String[] parts = val.split("[:]");
    if (parts.length != 2) {
      throw new FormatException(val, "The provided value '" + val + "' cannot be interpreted as valid salted password-hash value");
    }
    byte[] salt = StringConversionUtils.toByteArray(parts[0], false);
    byte[] passwordHash = StringConversionUtils.toByteArray(parts[1], false);
    return new SaltedPasswordHash(salt, passwordHash);
  }

  /**
   *  Encode a {@link SaltedPasswordHash} value as a string.
   *
   *  @param val The {@link SaltedPasswordHash} value to represent as a string.
   *
   *  @return The canonical representation of the provided {@link SaltedPasswordHash} value.
   *          This canonical representation will use only base64 characters.
   */
  public static String encode(SaltedPasswordHash val) {
    Contract.isNotNull("val", val);
    StringBuilder sb = new StringBuilder();
    sb.append(StringConversionUtils.fromByteArray(val.salt));
    sb.append(':');
    sb.append(StringConversionUtils.fromByteArray(val.passwordHash));
    return sb.toString();
  }

  // </editor-fold>
  // <editor-fold desc="Value Type">

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || this.getClass() != o.getClass()) return false;
    SaltedPasswordHash that = (SaltedPasswordHash) o;
    return Arrays.equals(this.salt, that.salt) &&
           Arrays.equals(this.passwordHash, that.passwordHash);
  }

  @Override
  public int hashCode() {
    int result = Arrays.hashCode(this.salt);
    result = 31 * result + Arrays.hashCode(this.passwordHash);
    return result;
  }

  // </editor-fold>
  // <editor-fold desc="Debugging">

  @Override
  public String toString() {
    return encode(this);
  }

  // </editor-fold>
}
