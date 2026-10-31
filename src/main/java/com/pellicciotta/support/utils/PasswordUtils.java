// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

/**
 *  Utilities for working with passwords.
 *
 *  <p>Inspiration from: <a href="http://stackoverflow.com/questions/2860943/how-can-i-hash-a-password-in-java">Stackoverflow</a></p>
 */
public final class PasswordUtils {
  // Prevent instantiation
  private PasswordUtils() { }

  // <editor-fold desc="Constants">

  // The higher the number of iterations the more expensive computing the hash is for us and also for an attacker.
  private static final int ITERATIONS         = 20 * 1000;
  private static final int LENGTH_OF_SALT     = 32;
  private static final int DESIRED_KEY_LENGTH = 256;

  // </editor-fold>
  // <editor-fold desc="Operations">

  /**
   *  Compute a salted password hash, where the salt is prepended to the actual hash.
   *
   *  @param password The plain-text password to be hashed, with a random salt.
   *
   *  @return An object containing both the salt and the password hash.
   */
  public static SaltedPasswordHash createSaltedPasswordHash(String password) {
    byte[] salt = createSalt();
    byte[] saltedHash = createSaltedHash(password, salt);
    return new SaltedPasswordHash(salt, saltedHash);
  }

  /**
   *  Check whether the given plain-text password corresponds to a stored salted hash of the password.
   *
   *  @param password The plaintext password to be checked.
   *  @param saltedPasswordHash A previously calculated (with {@link #createSaltedPasswordHash(String)}) value consisting of a salt and the password hash when using that salt.
   *
   *  @return True if the salted password hash calculated with password and salt corresponds exactly to the provided saltedPasswordHash.
   */
  public static boolean checkSaltedPasswordHash(String password, SaltedPasswordHash saltedPasswordHash) {
    byte[] calculatedPasswordHash = createSaltedHash(password, saltedPasswordHash.salt);
    return Arrays.equals(saltedPasswordHash.passwordHash, calculatedPasswordHash);
  }

  // See also: https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#SecretKeyFactory
  private static byte[] createSalt() {
    try {
      byte[] salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(LENGTH_OF_SALT);
      return salt;
    }
    catch (NoSuchAlgorithmException nse) {
      throw new RuntimeException(nse);
    }
  }

  // Using PBKDF2 from Sun, an alternative is https://github.com/wg/scrypt
  // cf. http://www.unlimitednovelty.com/2012/03/dont-use-bcrypt.html
  // See also: https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#SecretKeyFactory
  private static byte[] createSaltedHash(String password, byte[] salt) {
    Contract.isNotNull("salt", salt);
    Contract.isInRange("password.length", password.length(), 1, Integer.MAX_VALUE);
    try {
      SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      SecretKey key = f.generateSecret(new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, DESIRED_KEY_LENGTH));
      return key.getEncoded();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // </editor-fold>
}
