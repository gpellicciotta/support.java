// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.*;
import java.security.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pellicciotta.support.exceptions.FormatException;
import com.pellicciotta.support.logging.LogLevel;

/**
 *  Utility class to convert between strings and various primitive types.
 *  <p>
 *  All methods allow replacing first any parameters of the form <code>${context:name}</code> or <code>${name}</code>
 *  before doing any additional conversion: see the {@link #toExpandedString(String)} comments for more details.</p>
 */
public final class StringConversionUtils {
  /** Prevent instantiation. */
  private StringConversionUtils() { }

  // '${', optionally followed by 'env:', followed by a name, optionally followed by ':' and paramFormat, followed by '}'
  private static Pattern FORMAT_REPLACEMENTS_REGEX = Pattern.compile("[$][{](?:(env)[:])?([^:}]+)(?:[:]([^}]+))?[}]");

  /**
   *  Expand the provided string value by replacing any contained parameters of forms:<ul>
   *    <li><code>${context:name}</code></li>
   *    <li><code>${context:name:format}</code></li></ul>
   *  Where:<ul>
   *    <li><code>context</code> defines the search context and is currently restricted to <code>env</code>: searching in the system properties and environment variables.</li>
   *    <li><code>name</code> is the name to search for.
   *        If no <code>context</code> is given, it is searched in the <code>replacements</code> parameter.</li>
   *    <li><code>format</code> is the format to apply to the parameter value when determining the replacement text.
   *        This <code>format</code> must be a valid String format as it will be used as follows: <code>String.format("{format}", value)</code>.</li></ul>
   *  <p>
   *  Search order with context <code>env</code> will be: first search in system properties, and only if not found in true environment variables.</p>
   *
   *  @param parameterizedString The string to search for parameters that can be replaced based on values from system properties or from environment variables.

   *  @return The result of replacing any known parameters in  with (formatted) system property or environment variable values.
   *          If  is null,  will be returned.
   */
  public static String toExpandedString(String parameterizedString) {
    return toExpandedString(parameterizedString, (Map<String, ?>) null);
  }

  /**
   *  Expand the provided string value by replacing any contained parameters of forms:<ul>
   *    <li><code>${name}</code></li>
   *    <li><code>${name:format}</code></li>
   *    <li><code>${context:name}</code></li>
   *    <li><code>${context:name:format}</code></li></ul>
   *  Where:<ul>
   *    <li><code>context</code> defines the search context and is currently restricted to <code>env</code>: searching in the system properties and environment variables.</li>
   *    <li><code>name</code> is the name to search for.
   *        If no <code>context</code> is given, it is searched in the <code>replacements</code> parameter.</li>
   *    <li><code>format</code> is the format to apply to the parameter value when determining the replacement text.
   *        This <code>format</code> must be a valid String format as it will be used as follows: <code>String.format("{format}", value)</code>.</li></ul>
   *  <p>
   *  Search order with context <code>env</code> will be: first search in system properties, and only if not found in true environment variables.</p>
   *
   *  @param parameterizedString The string to search for parameters that can be replaced based on values from the replacements parameter or from environment variables or system properties.
   *  @param replacements Replacement values for parameters found in the parameterizedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The result of replacing any known parameters in parameterizedString with (formatted) replacement, environment variable or system property values.
   *          If parameterizedString is null, parameterizedString will be returned.
   */
  public static String toExpandedString(String parameterizedString, Properties replacements) {
    return toExpandedString(parameterizedString, CollectionUtils.propertiesToMap(replacements));
  }

  /**
   *  Expand the provided string value by replacing any contained parameters of forms:<ul>
   *    <li><code>${name}</code></li>
   *    <li><code>${name:format}</code></li>
   *    <li><code>${context:name}</code></li>
   *    <li><code>${context:name:format}</code></li></ul>
   *  Where:<ul>
   *    <li><code>context</code> defines the search context and is currently restricted to <code>env</code>: searching in the system properties and environment variables.</li>
   *    <li><code>name</code> is the name to search for.
   *        If no <code>context</code> is given, it is searched in the <code>replacements</code> parameter.</li>
   *    <li><code>format</code> is the format to apply to the parameter value when determining the replacement text.
   *        This <code>format</code> must be a valid String format as it will be used as follows: <code>String.format("{format}", value)</code>.</li></ul>
   *  <p>
   *  Search order with context <code>env</code> will be: first search in system properties, and only if not found in true environment variables.</p>
   *
   *  @param parameterizedString The string to search for parameters that can be replaced based on values from the replacements parameter or from environment variables or system properties.
   *  @param replacements Replacement values for parameters found in the parameterizedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The result of replacing any known parameters in parameterizedString with (formatted) replacement, environment variable or system property values.
   *          If parameterizedString is null, parameterizedString will be returned.
   */
  public static String toExpandedString(String parameterizedString, Map<String, ?> replacements) {
    if ((parameterizedString == null) || (parameterizedString.length() < 4) || !parameterizedString.contains("${")) {
      return parameterizedString;
    }
    if (replacements == null) {
      replacements = Collections.emptyMap();
    }
    StringBuilder sb = new StringBuilder();
    Matcher m = FORMAT_REPLACEMENTS_REGEX.matcher(parameterizedString);
    while (m.find()) {
      String context = m.group(1);
      String paramName = m.group(2);
      String paramFormat = m.group(3);
      if (replacements.containsKey(paramName)) {
        String replVal = "";
        Object paramVal = replacements.get(paramName);
        if (paramVal != null) {
          if (paramFormat != null) {
            replVal = String.format(paramFormat, paramVal);
          }
          else {
            replVal = paramVal.toString();
          }
        }
        m.appendReplacement(sb, Matcher.quoteReplacement(replVal));
      }
      else if (context != null) {
        String paramVal = System.getProperty(paramName);
        if (paramVal == null) {
          paramVal = System.getenv(paramName);
        }
        if (paramVal != null) {
          if (paramFormat != null) {
            paramVal = String.format(paramFormat, paramVal);
          }
          m.appendReplacement(sb, Matcher.quoteReplacement(paramVal));
        }
      }
    }
    m.appendTail(sb);
    return sb.toString();
  }

  private static final byte[] SYMMETRIC_ENCRYPTION_KEY = new byte[]{
      (byte) 0x60, (byte) 0x38, (byte) 0x7a, (byte) 0xbe, (byte) 0xf6, (byte) 0x3a, (byte) 0xb7, (byte) 0x03,
      (byte) 0x3e, (byte) 0x6d, (byte) 0x2b, (byte) 0x4b, (byte) 0x23, (byte) 0x4d, (byte) 0x2f, (byte) 0x58
  };

  /**
   *  Decrypt an encrypted and base64-encoded version of a string into the original string, with a built-in key.
   *
   *  @param encryptedVal The encrypted and base64-encoded result of applying {@link #symmetricEncode}.
   *  @return The original (decrypted and decoded) string that was encyrpted and base64 encoded.
   *
   *  @throws IllegalArgumentException If val doesn't represent a correct base64 encoded string or a correctly encrypted value.
   */
  public static String symmetricDecode(String encryptedVal) {
    assert encryptedVal != null;

    try {
      return symmetricDecode(DEFAULT_ENCRYPTION_SCHEME, SYMMETRIC_ENCRYPTION_KEY, encryptedVal);
    }
    catch (Exception e) {
      throw new IllegalArgumentException("Failed to decrypt the provided value '" + encryptedVal + "'", e);
    }
  }

  /**
   *  Decrypt an encrypted and base64-encoded version of a string into the original string, with a provided key.
   *
   *  @param encryptedVal The encrypted and base64-encoded result of applying {@link #symmetricEncode}.
   *  @param key The secret key that was also used while encoding.
   *
   *  @return The original (decrypted and decoded) string that was encyrpted and base64 encoded.
   *
   *  @throws IllegalArgumentException If val doesn't represent a correct base64 encoded string or a correctly encrypted value.
   */
  public static String symmetricDecode(String encryptedVal, byte[] key) {
    assert encryptedVal != null;

    try {
      return symmetricDecode(DEFAULT_ENCRYPTION_SCHEME, key, encryptedVal);
    }
    catch (Exception e) {
      throw new IllegalArgumentException("Failed to decrypt the provided value '" + encryptedVal + "'", e);
    }
  }

  private static String symmetricDecode(String symmKeyAlgorithmAndTransformation, byte[] encryptionKey,
                                        String encodedVal) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
    String symmKeyAlgorithm = symmKeyAlgorithmAndTransformation;
    if (symmKeyAlgorithmAndTransformation.contains("/")) { // Format is {algorithm} or {algorithm/mode/padding}
      symmKeyAlgorithm = symmKeyAlgorithmAndTransformation.substring(0, symmKeyAlgorithmAndTransformation.indexOf("/"));
    }
    final Key key = new SecretKeySpec(encryptionKey, symmKeyAlgorithm);
    final Cipher c = Cipher.getInstance(symmKeyAlgorithmAndTransformation);
    c.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(new byte[16]));
    byte[] encryptedData = Base64.getDecoder()
                                 .decode(encodedVal.getBytes(StandardCharsets.UTF_8));
    final byte[] plainData = c.doFinal(encryptedData);
    return new String(plainData, StandardCharsets.UTF_8);
  }

  /**
   *  Encrypt and then base64-encode a string with a built-in key.
   *
   *  @param val The string to be encrypted and base64-encoded.
   *  @return The encrypted and base64-encoded version of the provided value, which can later be decrypted via {@link #symmetricDecode(String)}.
   */
  public static String symmetricEncode(String val) {
    assert val != null;

    try {
      return symmetricEncode(DEFAULT_ENCRYPTION_SCHEME, SYMMETRIC_ENCRYPTION_KEY, val);
    }
    catch (Exception e) {
      throw new RuntimeException("Failed to encrypt the provided value '" + val + "'", e);
    }
  }

  /**
   *  Encrypt and then base64-encode a string with a provided key.
   *
   *  @param val The string to be encrypted and base64-encoded.
   *  @param secretKey The secret key to use for encoding.
   *
   *  @return The encrypted and base64-encoded version of the provided value, which can later be decrypted via {@link #symmetricDecode(String)}.
   */
  public static String symmetricEncode(String val, byte[] secretKey) {
    assert val != null;

    try {
      return symmetricEncode(DEFAULT_ENCRYPTION_SCHEME, secretKey, val);
    }
    catch (Exception e) {
      throw new RuntimeException("Failed to encrypt the provided value '" + val + "'", e);
    }
  }

  private static String symmetricEncode(String symmKeyAlgorithmAndTransformation, byte[] encryptionKey,
                                        String plainVal) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
    String symmKeyAlgorithm = symmKeyAlgorithmAndTransformation;
    if (symmKeyAlgorithmAndTransformation.contains("/")) { // Format is {algorithm} or {algorithm/mode/padding}
      symmKeyAlgorithm = symmKeyAlgorithmAndTransformation.substring(0, symmKeyAlgorithmAndTransformation.indexOf("/"));
    }
    byte[] plainData = plainVal.getBytes(StandardCharsets.UTF_8);
    final Key key = new SecretKeySpec(encryptionKey, symmKeyAlgorithm);
    final Cipher c = Cipher.getInstance(symmKeyAlgorithmAndTransformation);
    c.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(new byte[16]));
    final byte[] encryptedData = c.doFinal(plainData);
    byte[] encodedData = Base64.getEncoder()
                               .encode(encryptedData);
    return new String(encodedData, StandardCharsets.UTF_8);
  }

  /**
   *  Decode a base64-encoded version of a string into the original string,
   *  <p>
   *  The base64 encoded bytes will be interpreted as representing the <code>UTF-8</code> character encoding of a string.</p>
   *
   *  @param base64Val The base64-encoded value.
   *  @return The string that was encoded as base64.
   *
   *  @throws IllegalArgumentException If val doesn't represent a correct base64 encoded string.
   */
  public static String base64Decode(String base64Val) {
    assert base64Val != null;

    byte[] valData = Base64.getDecoder()
                           .decode(base64Val.getBytes(StandardCharsets.UTF_8));
    return new String(valData, StandardCharsets.UTF_8);
  }

  /**
   *  Encode a string into a base64-encoded version.
   *  <p>
   *  It are the bytes representing the <code>UTF-8</code> character encoding of the string, that are being encoded into <code>base64</code>.</p>
   *
   *  @param val The string to be encoded.
   *  @return The base64 encoded version of the string.
   */
  public static String base64Encode(String val) {
    assert val != null;

    byte[] valData = val.getBytes(StandardCharsets.UTF_8);
    return new String(Base64.getEncoder()
                            .encode(valData), StandardCharsets.UTF_8);
  }

  private final static String DEFAULT_ENCRYPTION_SCHEME = "AES/CBC/PKCS5Padding";

  /**
   *  Interpret and (depending on the encoding scheme) decode an encoded password value into a plain password string.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret, which currently can have one of following formats, after parameter expansion:<ul>
   *               <li><code>aes:xxxx</code>: <code>xxxx</code>, after trimming and decryption via AES with a symmetric key, represents the base64 encoded value of the password</li>
   *               <li><code>base64:xxxx</code>: <code>xxxx</code> represents the base64 encoded value of the password</li>
   *               <li><code>plain:xxxx</code>: <code>xxxx</code>, after trimming, is the password</li>
   *               <li><code>xxxx</code>: <code>xxxx</code>, after trimming, is the password</li>
   *             </ul>
   *             Must not be null.
   *
   *  @return The password represented by parameterizedString.
   */
  public static String toPlainTextPassword(String parameterizedString) {
    return toPlainTextPassword(parameterizedString, true);
  }

  /**
   *  Interpret and (depending on the encoding scheme) decode an encoded password value into a plain password string.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret, which currently can have one of following formats, after parameter expansion:<ul>
   *               <li><code>aes:xxxx</code>: <code>xxxx</code>, after trimming and decryption via AES with a symmetric key, represents the base64 encoded value of the password</li>
   *               <li><code>base64:xxxx</code>: <code>xxxx</code> represents the base64 encoded value of the password</li>
   *               <li><code>plain:xxxx</code>: <code>xxxx</code>, after trimming, is the password</li>
   *               <li><code>xxxx</code>: <code>xxxx</code>, after trimming, is the password</li>
   *             </ul>
   *             Must not be null.
   *  @param replacements Replacement values for parameters found in the parameterizedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The password represented by parameterizedString.
   */
  public static String toPlainTextPassword(String parameterizedString, Properties replacements) {
    return toPlainTextPassword(toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret and (depending on the encoding scheme) decode an encoded password value into a plain password string.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret, which currently can have one of following formats, after parameter expansion:<ul>
   *               <li><code>aes:xxxx</code>: <code>xxxx</code>, after trimming and decryption via AES with a symmetric key, represents the base64 encoded value of the password</li>
   *               <li><code>base64:xxxx</code>: <code>xxxx</code> represents the base64 encoded value of the password</li>
   *               <li><code>plain:xxxx</code>: <code>xxxx</code>, after trimming, is the password</li>
   *               <li><code>xxxx</code>: <code>xxxx</code>, after trimming, is the password</li>
   *             </ul>
   *             Must not be null.
   *  @param replacements Replacement values for parameters found in the parameterizedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The password represented by parameterizedString.
   */
  public static String toPlainTextPassword(String parameterizedString, Map<String, ?> replacements) {
    return toPlainTextPassword(toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret and (depending on the encoding scheme) decode an encoded password value into a plain password string.
   *
   *  @param val The value to interpret, which currently can have one of following formats:<ul>
   *               <li><code>aes:xxxx</code>: <code>xxxx</code>, after trimming and decryption via AES with a symmetric key, represents the base64 encoded value of the password</li>
   *               <li><code>base64:xxxx</code>: <code>xxxx</code> represents the base64 encoded value of the password</li>
   *               <li><code>plain:xxxx</code>: <code>xxxx</code>, after trimming, is the password</li>
   *               <li><code>xxxx</code>: <code>xxxx</code>, after trimming, is the password</li>
   *             </ul>
   *             Must not be null.
   *  @param expand Whether to first replace any parameter syntax in val.
   *
   *  @return The password represented by val.
   */
  public static String toPlainTextPassword(String val, boolean expand) {
    if (expand) {
      val = toExpandedString(val);
    }
    if (val == null) {
      return null;
    }
    val = val.trim();

    if (val.startsWith("aes:")) {
      try {
        val = val.substring("aes:".length())
                 .trim();
        return symmetricDecode(val);
      }
      catch (Exception e) {
        throw new IllegalArgumentException("Failed to decode the provided password with a symmetric key via AES", e);
      }
    }
    else if (val.startsWith("base64:")) {
      val = val.substring("base64:".length())
               .trim();
      return base64Decode(val);
    }
    else if (val.startsWith("plain:")) {
      return val.substring("plain:".length())
                .trim();
    }
    else {
      return val.trim();
    }
  }

  /**
   *  Encode a plain password string according to the <code>aes</code> encoding scheme.
   *
   *  @param plainPassword The plain-text password value. This must not start nor end with whitespace.
   *
   *  @return The password that will be aes-encrypted and base64-encoded.
   *
   *  @exception FormatException If val is not a valid value.
   */
  public static String fromPlainTextPassword(String plainPassword) {
    return fromPlainTextPassword(plainPassword, "aes");
  }

  /**
   *  Encode a plain password string according to a specified encoding scheme.
   *  <p>
   *  Note that <b>none</b> of the current encoding schemes should be considered secure:
   *  they merely offer varying degrees of obfuscation. Even when using <kbd>AES</kbd>, which in itself is a
   *  secure algorithm, the result will not be very hard to un-encode. That's also the reason this
   *  method uses the word <kbd>encode</kbd> rather than <kbd>encrypt</kbd>, since it is merely that.</p>
   *
   *  @param encodingScheme The encoding scheme to use.
   *                        Currently the following schemes are supported: <ul>
   *                          <li><code>aes</code>: aes encrypted and base64 encoded value of the password</li>
   *                          <li><code>base64</code>: base64 encoded value of the password</li>
   *                          <li><code>plain</code> or empty: no encoding is applied at all, the password will remain in plain-text</li>
   *                        </ul>
   *  @param plainPassword The plain-text password value. This must not start nor end with whitespace.
   *
   *  @return The password encoded according to the requested scheme.
   *
   *  @exception FormatException If val is not a valid value.
   */
  public static String fromPlainTextPassword(String plainPassword, String encodingScheme) {
    Contract.isNotNull("encodingScheme", encodingScheme);
    Contract.isNotNull("plainPassword", plainPassword);
    switch (encodingScheme.toLowerCase()) {
      case "aes":
        try {
          return "aes:" + symmetricEncode(plainPassword);
        }
        catch (Exception e) {
          throw new IllegalArgumentException("Failed to encode the provided password with a symmetric key via AES", e);
        }

      case "base64":
        return "base64:" + base64Encode(plainPassword);

      case "plain":
      case "":
        return "plain:" + plainPassword;

      default:
        throw new IllegalArgumentException("The encoding schem '" + encodingScheme + "' is not supported");
    }
  }

  private final static String DEFAULT_TRUE_VALUE_STRING = "yes";
  private final static String DEFAULT_FALSE_VALUE_STRING = "no";
  private final static Pattern TRUE_PATTERN = Pattern.compile("(yes|true|on|enabled)", Pattern.CASE_INSENSITIVE);
  private final static Pattern FALSE_PATTERN = Pattern.compile("(no|false|off|disabled)", Pattern.CASE_INSENSITIVE);

  /**
   *  Interpret a string value as a boolean value.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret, after parameter expansion.
   *
   *  @return True if parameterizedString is any of the words (ignoring case and surrounding whitespace): 'true', 'yes' or 'on'.
   *          False if parameterizedString is any of the words (ignoring case and surrounding whitespace): 'false', 'no' or 'off'.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static boolean toBoolean(String parameterizedString) {
    return toBoolean(parameterizedString, true);
  }

  /**
   *  Interpret a string value as a boolean value.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret, after parameter expansion.
   *  @param replacements Replacement values for parameters found in the parameterizedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return True if parameterizedString is any of the words (ignoring case and surrounding whitespace): 'true', 'yes' or 'on'.
   *          False if parameterizedString is any of the words (ignoring case and surrounding whitespace): 'false', 'no' or 'off'.
   *
   *
   *  @exception FormatException If val is not a valid value.
   */
  public static boolean toBoolean(String parameterizedString, Properties replacements) {
    return toBoolean(toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret a string value as a boolean value.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret, after parameter expansion.
   *  @param replacements Replacement values for parameters found in the parameterizedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return True if parameterizedString is any of the words (ignoring case and surrounding whitespace): 'true', 'yes' or 'on'.
   *          False if parameterizedString is any of the words (ignoring case and surrounding whitespace): 'false', 'no' or 'off'.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static boolean toBoolean(String parameterizedString, Map<String, ?> replacements) {
    return toBoolean(toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret a string value as a boolean value.
   *
   *  @param val The value to interpret.
   *  @param expand Whether to first replace any parameter syntax in val.
   *
   *  @return True if val is any of the words (ignoring case and surrounding whitespace): 'true', 'yes' or 'on'.
   *          False if val is any of the words (ignoring case and surrounding whitespace): 'false', 'no' or 'off'.
   *
   *  @exception FormatException If val is not a valid value.
   */
  public static boolean toBoolean(String val, boolean expand) {
    if (expand) {
      val = toExpandedString(val);
    }
    val = val.trim();
    if (TRUE_PATTERN.matcher(val).matches()) {
      return true;
    }
    if (FALSE_PATTERN.matcher(val).matches()) {
      return false;
    }
    throw new FormatException(val, "The provided value '" + val + "' cannot be interpreted as valid boolean value");
  }

  /**
   *  Interpret a string value as a boolean value.
   *
   *  @param val The value to interpret.
   *  @param expand Whether to first replace any parameter syntax in val.
   *  @param nullVal The value to return if val is null.
   *
   *  @return True if val is any of the words (ignoring case and surrounding whitespace): 'true', 'yes' or 'on'.
   *          False if val is any of the words (ignoring case and surrounding whitespace): 'false', 'no' or 'off'.
   *
   *  @exception FormatException If val is not a valid value.
   */
  public static boolean toBoolean(String val, boolean expand, boolean nullVal) {
    if (val == null) {
      return nullVal;
    }
    if (expand) {
      val = toExpandedString(val);
    }
    val = val.trim();
    if (TRUE_PATTERN.matcher(val).matches()) {
      return true;
    }
    if (FALSE_PATTERN.matcher(val).matches()) {
      return false;
    }
    throw new FormatException(val, "The provided value '" + val + "' cannot be interpreted as valid boolean value");
  }

  /**
   *  Return the canonical string representation of the provided boolean value.
   *
   *  @param val The boolean value to represent as a string.
   *
   *  @return The canonical representation of the provided boolean value.
   */
  public static String fromBoolean(boolean val) {
    return val ? DEFAULT_TRUE_VALUE_STRING : DEFAULT_FALSE_VALUE_STRING;
  }

  /**
   *  Return the canonical string representation of the provided boolean value.
   *
   *  @param val The boolean value to represent as a string.
   *
   *  @return The canonical representation of the provided boolean value.
   */
  public static String fromBoolean(Boolean val) {
    return fromBoolean(val, null);
  }

  /**
   *  Return the canonical string representation of the provided boolean value.
   *
   *  @param val The boolean value to represent as a string.
   *  @param nullVal The value to return if val is null.
   *
   *  @return The canonical representation of the provided boolean value.
   */
  public static String fromBoolean(Boolean val, String nullVal) {
    if (val == null) {
      return nullVal;
    }
    return val ? DEFAULT_TRUE_VALUE_STRING : DEFAULT_FALSE_VALUE_STRING;
  }

  /**
   *  Interpret a string value as an int value.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret as a decimal (i.e. base 10) integer value, after parameter expansion.
   *
   *  @return The integer value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception FormatException If val is not a valid value.
   */
  public static int toInt(String parameterizedString) {
    return toInt(parameterizedString, true);
  }

  /**
   *  Interpret a string value as an int value.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret as a decimal (i.e. base 10) integer value, after parameter expansion.
   *  @param replacements Replacement values for parameters found in the parameterizedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The integer value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static int toInt(String parameterizedString, Properties replacements) {
    return toInt(toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret a string value as an int value.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret as a decimal (i.e. base 10) integer value, after parameter expansion.
   *  @param replacements Replacement values for parameters found in the parameterizedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The integer value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static int toInt(String parameterizedString, Map<String, ?> replacements) {
    return toInt(toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret a string value as an int value.
   *
   *  @param val The value to interpret as a decimal (i.e. base 10) integer value.
   *  @param expand Whether to first replace any parameter syntax in val.
   *
   *  @return The integer value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception FormatException If val is not a valid value.
   */
  public static int toInt(String val, boolean expand) {
    if (expand) {
      val = toExpandedString(val);
    }
    val = val.trim();
    return Integer.parseInt(val, 10);
  }

  /**
   *  Interpret a string value as an int value.
   *
   *  @param val The value to interpret as a decimal (i.e. base 10) integer value.
   *  @param expand Whether to first replace any parameter syntax in val.
   *  @param nullVal The value to return if val is null.
   *
   *  @return The integer value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception FormatException If val is not a valid value.
   */
  public static int toInt(String val, boolean expand, int nullVal) {
    if (val == null) {
      return nullVal;
    }
    if (expand) {
      val = toExpandedString(val);
    }
    val = val.trim();
    return Integer.parseInt(val, 10);
  }

  /**
   *  Return the canonical string representation of the provided integer value.
   *
   *  @param val The int value to represent as a string.
   *
   *  @return The canonical representation of the provided integer value.
   */
  public static String fromInt(int val) {
    return Integer.toString(val, 10);
  }

  /**
   *  Return the canonical string representation of the provided integer value.
   *
   *  @param val The int value to represent as a string.
   *
   *  @return The canonical representation of the provided integer value.
   */
  public static String fromInt(Integer val) {
    return fromInt(val, null);
  }

  /**
   *  Return the canonical string representation of the provided integer value.
   *
   *  @param val The int value to represent as a string.
   *  @param nullVal The value to return if val is null.
   *
   *  @return The canonical representation of the provided integer value.
   */
  public static String fromInt(Integer val, String nullVal) {
    if (val == null) {
      return nullVal;
    }
    return Integer.toString(val, 10);
  }

  /**
   *  Interpret a string value as an long value.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret as a decimal (i.e. base 10) long value, after parameter expansion.
   *
   *  @return The integer value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static long toLong(String parameterizedString) {
    return toLong(parameterizedString, true);
  }

  /**
   *  Interpret a string value as an long value.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret as a decimal (i.e. base 10) long value, after parameter expansion.
   *  @param replacements Replacement values for parameters found in the formatString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The integer value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static long toLong(String parameterizedString, Properties replacements) {
    return toLong(toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret a string value as an long value.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret as a decimal (i.e. base 10) integer value, after parameter expansion.
   *  @param replacements Replacement values for parameters found in the parameterizedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The integer value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static long toLong(String parameterizedString, Map<String, ?> replacements) {
    return toLong(toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret a string value as an long value.
   *
   *  @param val The value to interpret as a decimal (i.e. base 10) long value.
   *  @param expand Whether to first replace any parameter syntax in val.
   *
   *  @return The long value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception FormatException If val is not a valid value.
   */
  public static long toLong(String val, boolean expand) {
    if (expand) {
      val = toExpandedString(val);
    }
    val = val.trim();
    return Long.parseLong(val, 10);
  }

  /**
   *  Interpret a string value as an long value.
   *
   *  @param val The value to interpret as a decimal (i.e. base 10) long value.
   *  @param expand Whether to first replace any parameter syntax in val.
   *  @param nullVal The value to return if val is null.
   *
   *  @return The long value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception FormatException If val is not a valid value.
   */
  public static long toLong(String val, boolean expand, long nullVal) {
    if (val == null) {
      return nullVal;
    }
    if (expand) {
      val = toExpandedString(val);
    }
    val = val.trim();
    return Long.parseLong(val, 10);
  }

  /**
   *  Return the canonical string representation of the provided long value.
   *
   *  @param val The long value to represent as a string.
   *  @return The canonical representation of the provided long value.
   */
  public static String fromLong(long val) {
    return Long.toString(val, 10);
  }

  /**
   *  Return the canonical string representation of the provided long value.
   *
   *  @param val The long value to represent as a string.
   *  @return The canonical representation of the provided long value.
   */
  public static String fromLong(Long val) {
    return fromLong(val, null);
  }

  /**
   *  Return the canonical string representation of the provided long value.
   *
   *  @param val The long value to represent as a string.
   *  @param nullVal The value to return if val is null.
   *
   *  @return The canonical representation of the provided long value.
   */
  public static String fromLong(Long val, String nullVal) {
    if (val == null) {
      return nullVal;
    }
    return Long.toString(val, 10);
  }

  /**
   *  Interpret a string value as a decimal value.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret as a double (i.e. base 10) value, after parameter expansion.
   *
   *  @return The double value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static double toDouble(String parameterizedString) {
    return toDouble(parameterizedString, true);
  }

  /**
   *  Interpret a string value as a decimal value.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret as a decimal (i.e. base 10) value, after parameter expansion.
   *  @param replacements Replacement values for parameters found in the toExpandedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The double value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception FormatException If toExpandedString is not a valid value.
   */
  public static double toDouble(String parameterizedString, Properties replacements) {
    return toDouble(toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret a string value as a decimal value.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret as a decimal (i.e. base 10) long value, after parameter expansion.
   *  @param replacements Replacement values for parameters found in the parameterizedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The double value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static double toDouble(String parameterizedString, Map<String, ?> replacements) {
    return toDouble(toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret a string value as an decimal value.
   *
   *  @param val The value to interpret as a decimal (i.e. base 10) value. Must use dot as decimal separator.
   *  @param expand Whether to first replace any parameter syntax in val.
   *
   *  @return The double value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception FormatException If val is not a valid value.
   */
  public static double toDouble(String val, boolean expand) {
    return toBigDecimal(val, expand).doubleValue();
  }

  /**
   *  Interpret a string value as an decimal value.
   *
   *  @param val The value to interpret as a decimal (i.e. base 10) value. Must use dot as decimal separator.
   *  @param expand Whether to first replace any parameter syntax in val.
   *  @param nullVal The value to return if val is null.
   *
   *  @return The double value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception FormatException If val is not a valid value.
   */
  public static double toDouble(String val, boolean expand, double nullVal) {
    if (val == null) {
      return nullVal;
    }
    return toBigDecimal(val, expand).doubleValue();
  }

  /**
   *  Return the canonical string representation of the provided {@link double} value.
   *
   *  @param val The double value to represent as a string.
   *
   *  @return The canonical representation of the provided double value.
   */
  public static String fromDouble(double val) {
    return fromBigDecimal(new BigDecimal(val));
  }

  /**
   *  Return the canonical string representation of the provided {@link double} value.
   *
   *  @param val The double value to represent as a string.
   *
   *  @return The canonical representation of the provided double value.
   */
  public static String fromDouble(Double val) {
    return fromDouble(val, null);
  }

  /**
   *  Return the canonical string representation of the provided {@link double} value.
   *
   *  @param val The double value to represent as a string.
   *  @param nullVal The value to return if val is null.
   *
   *  @return The canonical representation of the provided double value.
   */
  public static String fromDouble(Double val, String nullVal) {
    if (val == null) {
      return nullVal;
    }
    return fromBigDecimal(new BigDecimal(val));
  }

  /**
   *  Interpret a string value as a decimal value.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret as a decimal (i.e. base 10) value, after parameter expansion.
   *
   *  @return The integer value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static BigDecimal toBigDecimal(String parameterizedString) {
    return toBigDecimal(parameterizedString, true);
  }

  /**
   *  Interpret a string value as a decimal value.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret as a decimal (i.e. base 10) value, after parameter expansion.
   *  @param replacements Replacement values for parameters found in the toExpandedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The integer value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception FormatException If toExpandedString is not a valid value.
   */
  public static BigDecimal toBigDecimal(String parameterizedString, Properties replacements) {
    return toBigDecimal(toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret a string value as a decimal value.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret as a decimal (i.e. base 10) long value, after parameter expansion.
   *  @param replacements Replacement values for parameters found in the parameterizedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The integer value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static BigDecimal toBigDecimal(String parameterizedString, Map<String, ?> replacements) {
    return toBigDecimal(toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret a string value as an decimal value.
   *
   *  @param val The value to interpret as a decimal (i.e. base 10) value. Must use dot as decimal separator.
   *  @param expand Whether to first replace any parameter syntax in val.
   *
   *  @return The decimal value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception FormatException If val is not a valid value.
   */
  public static BigDecimal toBigDecimal(String val, boolean expand) {
    return toBigDecimal(val, expand, null);
  }

  /**
   *  Interpret a string value as an decimal value.
   *
   *  @param val The value to interpret as a decimal (i.e. base 10) value. Must use dot as decimal separator.
   *  @param expand Whether to first replace any parameter syntax in val.
   *  @param nullVal The value to return if val is null.
   *
   *  @return The decimal value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception FormatException If val is not a valid value.
   */
  public static BigDecimal toBigDecimal(String val, boolean expand, BigDecimal nullVal) {
    if (val == null) {
      return nullVal;
    }
    if (expand) {
      val = toExpandedString(val);
    }
    val = val.trim();
    return new BigDecimal(val);
  }

  /**
   *  Return the canonical string representation of the provided {@link BigDecimal} value.
   *
   *  @param val The decimal value to represent as a string.
   *
   *  @return The canonical representation of the provided decimal value.
   */
  public static String fromBigDecimal(BigDecimal val) {
    return fromBigDecimal(val, null);
  }

  /**
   *  Return the canonical string representation of the provided {@link BigDecimal} value.
   *
   *  @param val The decimal value to represent as a string.
   *  @param nullVal The value to return if val is null.
   *  @return The canonical representation of the provided decimal value.
   */
  public static String fromBigDecimal(BigDecimal val, String nullVal) {
    if (val == null) {
      return nullVal;
    }
    return val.toString();
  }

  /**
   *  Interpret a string value as an {@link Charset} value, using {@link Charset#forName(String)}.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret, after parameter expansion. If equal to 'default', the default charset will get returned.

   *  @return The {@link Charset} value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception IllegalCharsetNameException If parameterizedString is not a valid value.
   *  @exception UnsupportedCharsetException If parameterizedString is not a valid value.
   */
  public static Charset toCharset(String parameterizedString) {
    return toCharset(parameterizedString, true);
  }

  /**
   *  Interpret a string value as an {@link Charset} value, using {@link Charset#forName(String)}.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret, after parameter expansion. If equal to 'default', the default charset will get returned.
   *  @param replacements Replacement values for parameters found in the parameterizedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The {@link Charset} value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception IllegalCharsetNameException If parameterizedString is not a valid value.
   *  @exception UnsupportedCharsetException If parameterizedString is not a valid value.
   */
  public static Charset toCharset(String parameterizedString, Properties replacements) {
    return toCharset(toExpandedString(parameterizedString, replacements));
  }

  /**
   *  Interpret a string value as an {@link Charset} value, using {@link Charset#forName(String)}.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret, after parameter expansion. If equal to 'default', the default charset will get returned.
   *  @param replacements Replacement values for parameters found in the parameterizedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The {@link Charset} value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception IllegalCharsetNameException If parameterizedString is not a valid value.
   *  @exception UnsupportedCharsetException If parameterizedString is not a valid value.
   */
  public static Charset toCharset(String parameterizedString, Map<String, ?> replacements) {
    return toCharset(toExpandedString(parameterizedString, replacements));
  }

  /**
   *  Interpret a string value as an {@link Charset} value, using {@link Charset#forName(String)}.
   *
   *  @param val The value to interpret. If equal to 'default', the default charset will get returned.
   *  @param expand Whether to first replace any parameter syntax in val.
   *
   *  @return The {@link Charset} value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception IllegalCharsetNameException If val is not a valid value.
   *  @exception UnsupportedCharsetException If val is not a valid value.
   */
  public static Charset toCharset(String val, boolean expand) {
    return toCharset(val, expand, null);
  }

  /**
   *  Interpret a string value as an {@link Charset} value, using {@link Charset#forName(String)}.
   *
   *  @param val The value to interpret. If equal to 'default', the default charset will get returned.
   *  @param expand Whether to first replace any parameter syntax in val.
   *  @param nullVal The value to return if val is null.
   *
   *  @return The {@link Charset} value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception IllegalCharsetNameException If val is not a valid value.
   *  @exception UnsupportedCharsetException If val is not a valid value.
   */
  public static Charset toCharset(String val, boolean expand, Charset nullVal) {
    if (val == null) {
      return nullVal;
    }
    if (expand) {
      val = toExpandedString(val);
    }
    val = val.trim();
    if (val.equalsIgnoreCase("default")) {
      return Charset.defaultCharset();
    }
    return Charset.forName(val);
  }

  /**
   *  Return the canonical string representation of the provided {@link Charset} value.
   *
   *  @param val The charset value to represent as a string.
   *
   *  @return The canonical representation of the provided {@link Charset}.
   */
  public static String fromCharset(Charset val) {
    return fromCharset(val, null);
  }

  /**
   *  Return the canonical string representation of the provided {@link Charset} value.
   *
   *  @param val The charset value to represent as a string.
   *  @param nullVal The value to return if val is null.
   *  @return The canonical representation of the provided {@link Charset}.
   */
  public static String fromCharset(Charset val, String nullVal) {
    if (val == null) {
      return nullVal;
    }
    return val.name().toLowerCase();
  }

  /**
   *  Interpret a string value as an log level, using {@link LogLevel#parse(String)}.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret, after parameter expansion.
   *
   *  @return The integer value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static int toLogLevel(String parameterizedString) {
    return toLogLevel(parameterizedString, true);
  }

  /**
   *  Interpret a string value as an log level, using {@link LogLevel#parse(String)}.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret, after parameter expansion.
   *  @param replacements Replacement values for parameters found in the parameterizedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The integer value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static int toLogLevel(String parameterizedString, Properties replacements) {
    return toLogLevel(toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret a string value as an log level, using {@link LogLevel#parse(String)}.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret, after parameter expansion.
   *  @param replacements Replacement values for parameters found in the parameterizedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The integer value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static int toLogLevel(String parameterizedString, Map<String, ?> replacements) {
    return toLogLevel(toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret a string value as an log level, using {@link LogLevel#parse(String)}.
   *
   *  @param val The value to interpret.
   *  @param expand Whether to first replace any parameter syntax in val.
   *
   *  @return The integer value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception FormatException If val is not a valid value.
   */
  public static int toLogLevel(String val, boolean expand) {
    return toLogLevel(val, expand, LogLevel.NONE);
  }

  /**
   *  Interpret a string value as an log level, using {@link LogLevel#parse(String)}.
   *
   *  @param val The value to interpret.
   *  @param expand Whether to first replace any parameter syntax in val.
   *  @param nullVal The value to return if val is null.
   *
   *  @return The integer value represented by the given string, ignoring surrounding whitespace.
   *
   *  @exception FormatException If val is not a valid value.
   */
  public static int toLogLevel(String val, boolean expand, int nullVal) {
    if (val == null) {
      return nullVal;
    }
    if (expand) {
      val = toExpandedString(val);
    }
    val = val.trim();
    return LogLevel.parse(val);
  }

  /**
   *  Return the canonical string representation of the provided log-level value, using {@link LogLevel#toString(int)}.
   *
   *  @param val The log-level value to represent as a string.
   *
   *  @return The canonical representation of the provided log level.
   */
  public static String fromLogLevel(int val) {
    return LogLevel.toString(val).toLowerCase();
  }

  /**
   *  Return the canonical string representation of the provided log-level value, using {@link LogLevel#toString(int)}.
   *
   *  @param val The log-level value to represent as a string.
   *
   *  @return The canonical representation of the provided log level.
   */
  public static String fromLogLevel(Integer val) {
    return fromLogLevel(val, null);
  }

  /**
   *  Return the canonical string representation of the provided log-level value, using {@link LogLevel#toString(int)}.
   *
   *  @param val The log-level value to represent as a string.
   *  @param nullVal The value to return if val is null.
   *  @return The canonical representation of the provided log level.
   */
  public static String fromLogLevel(Integer val, String nullVal) {
    if (val == null) {
      return nullVal;
    }
    return LogLevel.toString(val).toLowerCase();
  }

  /**
   *  Interpret a string value as an byte[] value, assuming the string contains hex digit pairs.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret, after parameter expansion. If it contains an uneven number of hex digits, a leading zero will be assumed.
   *
   *  @return The byte[] value represented by the given string, ignoring any whitespace.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static byte[] toByteArray(String parameterizedString) {
    return toByteArray(parameterizedString, true);
  }

  /**
   *  Interpret a string value as an byte[] value, assuming the string contains hex digit pairs.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret, after parameter expansion. If it contains an uneven number of hex digits, a leading zero will be assumed.
   *  @param replacements Replacement values for parameters found in the parameterizedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The byte[] value represented by the given string, ignoring any whitespace.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static byte[] toByteArray(String parameterizedString, Properties replacements) {
    return toByteArray(toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret a string value as an byte[] value, assuming the string contains hex digit pairs.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret, after parameter expansion. If it contains an uneven number of hex digits, a leading zero will be assumed.
   *  @param replacements Replacement values for parameters found in the parameterizedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The byte[] value represented by the given string, ignoring any whitespace.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static byte[] toByteArray(String parameterizedString, Map<String, ?> replacements) {
    return toByteArray(toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret a string value as a byte[] value, assuming the string contains hex digit pairs.
   *
   *  @param val The value to interpret. If it contains an uneven number of hex digits, a leading zero will be assumed.
   *  @param expand Whether to first replace any parameter syntax in val.
   *
   *  @return The byte[] value represented by the given string, ignoring any whitespace.
   *
   *  @exception FormatException If val is not a valid value.
   */
  public static byte[] toByteArray(String val, boolean expand) {
    return toByteArray(val, expand, null);
  }

  /**
   *  Interpret a string value as a byte[] value, assuming the string contains hex digit pairs.
   *
   *  @param val The value to interpret. If it contains an uneven number of hex digits, a leading zero will be assumed.
   *  @param expand Whether to first replace any parameter syntax in val.
   *  @param nullVal The value to return if val is null.
   *
   *  @return The byte[] value represented by the given string, ignoring any whitespace.
   *
   *  @exception FormatException If val is not a valid value.
   */
  public static byte[] toByteArray(String val, boolean expand, byte[] nullVal) {
    if (val == null) {
      return nullVal;
    }
    if (expand) {
      val = toExpandedString(val);
    }
    val = val.trim();
    // Find all hex digits
    byte[] digits = new byte[val.length()];
    int digitCount = 0;
    for (int i = 0; i < val.length(); i++) {
      char ch = val.charAt(i);
      if (Character.isWhitespace(ch)) {
        continue;
      }
      switch (ch) {
        case '0':
          digits[digitCount++] = (byte) 0;
          break;
        case '1':
          digits[digitCount++] = (byte) 1;
          break;
        case '2':
          digits[digitCount++] = (byte) 2;
          break;
        case '3':
          digits[digitCount++] = (byte) 3;
          break;
        case '4':
          digits[digitCount++] = (byte) 4;
          break;
        case '5':
          digits[digitCount++] = (byte) 5;
          break;
        case '6':
          digits[digitCount++] = (byte) 6;
          break;
        case '7':
          digits[digitCount++] = (byte) 7;
          break;
        case '8':
          digits[digitCount++] = (byte) 8;
          break;
        case '9':
          digits[digitCount++] = (byte) 9;
          break;
        case 'a':
        case 'A':
          digits[digitCount++] = (byte) 10;
          break;
        case 'b':
        case 'B':
          digits[digitCount++] = (byte) 11;
          break;
        case 'c':
        case 'C':
          digits[digitCount++] = (byte) 12;
          break;
        case 'd':
        case 'D':
          digits[digitCount++] = (byte) 13;
          break;
        case 'e':
        case 'E':
          digits[digitCount++] = (byte) 14;
          break;
        case 'f':
        case 'F':
          digits[digitCount++] = (byte) 15;
          break;
        default:
          throw new FormatException(ch, "Found invalid character '%s' at position %d", ch, (i + 1));
      }
    }
    int byteCount = (digitCount / 2) + (digitCount % 2); // 1 extra if uneven
    byte[] bytes = new byte[byteCount];
    int currentByte = 0;
    int currentDigit = 0;
    if ((digitCount % 2) == 1) { // Uneven, so prefix with extra zero
      bytes[currentByte++] = digits[currentDigit++];
    }
    for (; (currentDigit + 1) < digits.length; currentDigit += 2) {
      byte highNibble = digits[currentDigit];
      byte lowNibble = digits[currentDigit + 1];
      bytes[currentByte++] = (byte) ((byte) (highNibble << 4) | lowNibble);
    }
    return bytes;
  }

  /**
   *  Return the canonical string representation of the provided byte[] value.
   *
   *  @param val The value to represent as a string.
   *
   *  @return The canonical representation of the provided byte[], which will be a sequence
   *          of lowercase hex-digit pairs.
   */
  public static String fromByteArray(byte[] val) {
    return fromByteArray(val, null);
  }

  /**
   *  Return the canonical string representation of the provided byte[] value.
   *
   *  @param val The value to represent as a string.
   *  @param nullVal The value to return if val is null.
   *  @return The canonical representation of the provided byte[], which will be a sequence
   *          of lowercase hex-digit pairs.
   */
  public static String fromByteArray(byte[] val, String nullVal) {
    if (val == null) {
      return nullVal;
    }
    return StringUtils.toHexString(val, "{hex}");
  }

  /**
   *  Return the canonical string representation of a slice of the provided byte[] value.
   *  <p>
   *  This method ensures no byte array copying is needed.</p>
   *
   *  @param val The value to represent as a string.
   *  @param offset The offset of the first byte within val to be represented in the string.
   *  @param length The number of bytes within val, starting from the given offset, to represent in the string.
   *
   *  @return The canonical representation of the provided byte[], which will be a sequence
   *          of lowercase hex-digit pairs.
   */
  public static String fromByteArray(byte[] val, int offset, int length) {
    return StringUtils.toHexString(val, offset, length, "{hex}");
  }

  /**
   *  Interpret a string value as an byte size value, using {@link ByteSizeUnit#parseValue(String)}.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret, after parameter expansion.
   *
   *  @return The number of bytes represented by parameterizedString, ignoring surrounding whitespace,
   *          an interpreting a byte-unit suffix according to {@link ByteSizeUnit#parseValue(String)}.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static long toByteSize(String parameterizedString) {
    return toByteSize(parameterizedString, true);
  }

  /**
   *  Interpret a string value as an byte size value, using {@link ByteSizeUnit#parseValue(String)}.
   *
   *  @param parameterizedString The value to interpret, after parameter expansion.
   *  @param replacements Replacement values for parameters found in the parameterizedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The number of bytes represented by val, ignoring surrounding whitespace,
   *          an interpreting a byte-unit suffix according to {@link ByteSizeUnit#parseValue(String)}.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static long toByteSize(String parameterizedString, Properties replacements) {
    return toByteSize(toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret a string value as an byte size value, using {@link ByteSizeUnit#parseValue(String)}.
   *
   *  @param parameterizedString The value to interpret, after parameter expansion.
   *  @param replacements Replacement values for parameters found in the parameterizedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The number of bytes represented by val, ignoring surrounding whitespace,
   *          an interpreting a byte-unit suffix according to {@link ByteSizeUnit#parseValue(String)}.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static long toByteSize(String parameterizedString, Map<String, ?> replacements) {
    return toByteSize(toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret a string value as an byte size value, using {@link ByteSizeUnit#parseValue(String)}.
   *
   *  @param val The value to interpret.
   *  @param expand Whether to first replace any parameter syntax in val.
   *
   *  @return The number of bytes represented by val, ignoring surrounding whitespace,
   *          an interpreting a byte-unit suffix according to {@link ByteSizeUnit#parseValue(String)}.
   *
   *  @exception FormatException If val is not a valid value.
   */
  public static long toByteSize(String val, boolean expand) {
    return toByteSize(val, expand, 0);
  }

  /**
   *  Interpret a string value as an byte size value, using {@link ByteSizeUnit#parseValue(String)}.
   *
   *  @param val The value to interpret.
   *  @param expand Whether to first replace any parameter syntax in val.
   *  @param nullVal The value to return if val is null.
   *
   *  @return The number of bytes represented by val, ignoring surrounding whitespace,
   *          an interpreting a byte-unit suffix according to {@link ByteSizeUnit#parseValue(String)}.
   *
   *  @exception FormatException If val is not a valid value.
   */
  public static long toByteSize(String val, boolean expand, long nullVal) {
    if (val == null) {
      return nullVal;
    }
    if (expand) {
      val = toExpandedString(val);
    }
    val = val.trim();
    return ByteSizeUnit.parseValue(val);
  }

  /**
   *  Return the canonical string representation of the provided byte-size value.
   *  <p>
   *  Uses {@link ByteSizeUnit#formatValue(long)}.
   *  </p>
   *
   *  @param val The number of bytes to be represented as a string.
   *
   *  @return The canonical representation of the provided byte size.
   *          This representation may be an approximation of the exact value, so it is not
   *          guaranteed that using {@link StringConversionUtils#toByteSize(String)} on the result will
   *          result in the exact same value.
   */
  public static String fromByteSize(long val) {
    return ByteSizeUnit.formatValue(val);
  }

  /**
   *  Return the canonical string representation of the provided byte-size value.
   *  <p>
   *  Uses {@link ByteSizeUnit#formatValue(long)}.
   *  </p>
   *
   *  @param val The number of bytes to be represented as a string.
   *
   *  @return The canonical representation of the provided byte size.
   *          This representation may be an approximation of the exact value, so it is not
   *          guaranteed that using {@link StringConversionUtils#toByteSize(String)} on the result will
   *          result in the exact same value.
   */
  public static String fromByteSize(Long val) {
    return fromByteSize(val, null);
  }

  /**
   *  Return the canonical string representation of the provided byte-size value.
   *  <p>
   *  Uses {@link ByteSizeUnit#formatValue(long)}.
   *  </p>
   *
   *  @param val The number of bytes to be represented as a string.
   *  @param nullVal The value to return if val is null.
   *
   *  @return The canonical representation of the provided byte size.
   *          This representation may be an approximation of the exact value, so it is not
   *          guaranteed that using {@link StringConversionUtils#toByteSize(String)} on the result will
   *          result in the exact same value.
   */
  public static String fromByteSize(Long val, String nullVal) {
    if (val == null) {
      return nullVal;
    }
    return ByteSizeUnit.formatValue(val);
  }

  /**
   *  Interpret a string value as an date/time value.
   *  First expand any embedded parameter syntax.
   *  <p>
   *  Uses {@link DateTimeUtils#parseDateTime(String)} which will try to interpret the string
   *  according to the ISO date/time formats first, but later also according to the
   *  localized date/time formats.</p>
   *
   *  @param parameterizedString The value to interpret, after parameter expansion.
   *
   *  @return The date/time value, according to {@link DateTimeUtils#parseDateTime(String)}.
   *
   *  @exception FormatException If parameterizedString is not a valid date/time value.
   */
  public static Date toDateTime(String parameterizedString) {
    return toDateTime(parameterizedString, true);
  }

  /**
   *  Interpret a string value as an date/time value.
   *  First expand any embedded parameter syntax.
   *  <p>
   *  Uses {@link DateTimeUtils#parseDateTime(String)} which will try to interpret the string
   *  according to the ISO date/time formats first, but later also according to the
   *  localized date/time formats.</p>
   *
   *  @param parameterizedString The value to interpret, after parameter expansion.
   *  @param replacements Replacement values for parameters found in the parameterizedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.

   *  @return The date/time value, according to {@link DateTimeUtils#parseDateTime(String)}.
   *
   *  @exception FormatException If parameterizedString is not a valid date/time value.
   */
  public static Date toDateTime(String parameterizedString, Properties replacements) {
    return toDateTime(toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret a string value as an date/time value.
   *  First expand any embedded parameter syntax.
   *  <p>
   *  Uses {@link DateTimeUtils#parseDateTime(String)} which will try to interpret the string
   *  according to the ISO date/time formats first, but later also according to the
   *  localized date/time formats.</p>
   *
   *  @param parameterizedString The value to interpret, after parameter expansion.
   *  @param replacements Replacement values for parameters found in the parameterizedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The date/time value, according to {@link DateTimeUtils#parseDateTime(String)}.
   *
   *  @exception FormatException If parameterizedString is not a valid date/time value.
   */
  public static Date toDateTime(String parameterizedString, Map<String, ?> replacements) {
    return toDateTime(toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret a string value as an date/time value.
   *  <p>
   *  Uses {@link DateTimeUtils#parseDateTime(String)} which will try to interpret the string
   *  according to the ISO date/time formats first, but later also according to the
   *  localized date/time formats.</p>
   *
   *  @param val The value to interpret.
   *  @param expand Whether to first replace any parameter syntax in val.
   *
   *  @return The date/time value, according to {@link DateTimeUtils#parseDateTime(String)}.
   *
   *  @exception FormatException If val is not a valid date/time value.
   */
  public static Date toDateTime(String val, boolean expand) {
    return toDateTime(val, expand, null);
  }

  /**
   *  Interpret a string value as an date/time value.
   *  <p>
   *  Uses {@link DateTimeUtils#parseDateTime(String)} which will try to interpret the string
   *  according to the ISO date/time formats first, but later also according to the
   *  localized date/time formats.</p>
   *
   *  @param val The value to interpret.
   *  @param expand Whether to first replace any parameter syntax in val.
   *  @param nullVal The value to return if val is null.
   *
   *  @return The date/time value, according to {@link DateTimeUtils#parseDateTime(String)}.
   *
   *  @exception FormatException If val is not a valid date/time value.
   */
  public static Date toDateTime(String val, boolean expand, Date nullVal) {
    if (val == null) {
      return nullVal;
    }
    if (expand) {
      val = toExpandedString(val);
    }
    val = val.trim();
    return DateTimeUtils.parseDateTime(val);
  }

  /**
   *  Return the canonical string representation of the provided date/time value.
   *  <p>
   *  Uses {@link DateTimeUtils#formatDateTime(Date)}.
   *  </p>
   *
   *  @param val The date/time to be represented as a string.
   *  @return The canonical representation of the provided date/time.
   */
  public static String fromDateTime(Date val) {
    return fromDateTime(val, null);
  }

  /**
   *  Return the canonical string representation of the provided date/time value.
   *  <p>
   *  Uses {@link DateTimeUtils#formatDateTime(Date)}.
   *  </p>
   *
   *  @param val The date/time to be represented as a string.
   *  @param nullVal The value to return if val is null.
   *
   *  @return The canonical representation of the provided date/time.
   */
  public static String fromDateTime(Date val, String nullVal) {
    if (val == null) {
      return nullVal;
    }
    return DateTimeUtils.formatDateTime(val);
  }

  /**
   *  Interpret a string value as an timespan value.
   *  First expand any embedded parameter syntax.
   *  <p>
   *  Uses {@link DateTimeUtils#parseTimespan(String)} whose most general format is <kbd>(-)?[h:mm:ss.mmm]</kbd></p>
   *
   *  @param parameterizedString The value to interpret, after parameter expansion.
   *
   *  @return The timespan value, according to {@link DateTimeUtils#parseTimespan(String)}.
   *
   *  @exception FormatException If parameterizedString is not a valid date/time value.
   */
  public static long toTimespan(String parameterizedString) {
    return toTimespan(parameterizedString, true);
  }

  /**
   *  Interpret a string value as an timespan value.
   *  First expand any embedded parameter syntax.
   *  <p>
   *  Uses {@link DateTimeUtils#parseTimespan(String)} whose most general format is <kbd>(-)?[h:mm:ss.mmm]</kbd></p>
   *
   *  @param parameterizedString The value to interpret, after parameter expansion.
   *  @param replacements Replacement values for parameters found in the parameterizedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The timespan value, according to {@link DateTimeUtils#parseTimespan(String)}.
   *
   *  @exception FormatException If parameterizedString is not a valid date/time value.
   */
  public static long toTimespan(String parameterizedString, Properties replacements) {
    return toTimespan(toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret a string value as an timespan value.
   *  First expand any embedded parameter syntax.
   *  <p>
   *  Uses {@link DateTimeUtils#parseTimespan(String)} whose most general format is <kbd>(-)?[h:mm:ss.mmm]</kbd></p>
   *
   *  @param parameterizedString The value to interpret, after parameter expansion.
   *  @param replacements Replacement values for parameters found in the parameterizedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The timespan value, according to {@link DateTimeUtils#parseTimespan(String)}.
   *
   *  @exception FormatException If parameterizedString is not a valid date/time value.
   */
  public static long toTimespan(String parameterizedString, Map<String, ?> replacements) {
    return toTimespan(toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret a string value as an timespan value.
   *  <p>
   *  Uses {@link DateTimeUtils#parseTimespan(String)} whose most general format is <kbd>(-)?[h:mm:ss.mmm]</kbd></p>
   *
   *  @param val The value to interpret.
   *  @param expand Whether to first replace any parameter syntax in val.
   *
   *  @return The timespan value, according to {@link DateTimeUtils#parseTimespan(String)}.
   *
   *  @exception FormatException If val is not a valid date/time value.
   */
  public static long toTimespan(String val, boolean expand) {
    if (expand) {
      val = toExpandedString(val);
    }
    val = val.trim();
    return DateTimeUtils.parseTimespan(val);
  }

  /**
   *  Interpret a string value as an timespan value.
   *  <p>
   *  Uses {@link DateTimeUtils#parseTimespan(String)} whose most general format is <kbd>(-)?[h:mm:ss.mmm]</kbd></p>
   *
   *  @param val The value to interpret.
   *  @param expand Whether to first replace any parameter syntax in val.
   *  @param nullVal The value to return if val is null.
   *
   *  @return The timespan value, according to {@link DateTimeUtils#parseTimespan(String)}.
   *
   *  @exception FormatException If val is not a valid date/time value.
   */
  public static long toTimespan(String val, boolean expand, long nullVal) {
    if (val == null) {
      return nullVal;
    }
    if (expand) {
      val = toExpandedString(val);
    }
    val = val.trim();
    return DateTimeUtils.parseTimespan(val);
  }

  /**
   *  Return the canonical string representation of the provided timespan value, which will be <kbd>(-)?[h:mm:ss.mmm]</kbd>.
   *  <p>
   *  Uses {@link DateTimeUtils#formatTimespan(long)}.</p>
   *
   *  @param val The timespan to be represented as a string.
   *  @return The canonical representation of the provided date/time.
   */
  public static String fromTimespan(long val) {
    return DateTimeUtils.formatTimespan(val);
  }

  /**
   *  Return the canonical string representation of the provided timespan value, which will be <kbd>(-)?[h:mm:ss.mmm]</kbd>.
   *  <p>
   *  Uses {@link DateTimeUtils#formatTimespan(long)}.</p>
   *
   *  @param val The timespan to be represented as a string.
   *  @return The canonical representation of the provided date/time.
   */
  public static String fromTimespan(Long val) {
    return fromTimespan(val, null);
  }

  /**
   *  Return the canonical string representation of the provided timespan value, which will be <kbd>(-)?[h:mm:ss.mmm]</kbd>.
   *  <p>
   *  Uses {@link DateTimeUtils#formatTimespan(long)}.</p>
   *
   *  @param val The timespan to be represented as a string.
   *  @param nullVal The value to return if val is null.
   *
   *  @return The canonical representation of the provided date/time.
   */
  public static String fromTimespan(Long val, String nullVal) {
    if (val == null) {
      return nullVal;
    }
    return DateTimeUtils.formatTimespan(val);
  }

  /**
   *  Interpret a string value as an enum value.
   *  First expand any embedded parameter syntax.
   *
   *  @param <T> The enum type.
   *  @param enumType The type of enum value to convert to.
   *  @param parameterizedString The value to interpret, after parameter expansion.
   *
   *  @return The enum value represented by the given string, ignoring surrounding whitespace and case,
   *          and transforming and dashes to underscores.
   *          E.g. Some-Value, some-value and Some_Value will all be transformed to SOME_VALUE before being interpreted as enum value.
   *
   *  @exception FormatException If parameterizedString is not a valid enum value.
   */
  public static <T extends Enum<T>> T toEnum(Class<T> enumType, String parameterizedString) {
    return toEnum(enumType, parameterizedString, true);
  }

  /**
   *  Interpret a string value as an enum value.
   *  First expand any embedded parameter syntax.
   *
   *  @param <T> The enum type.
   *  @param enumType The type of enum value to convert to.
   *  @param parameterizedString The value to interpret, after parameter expansion.
   *  @param replacements Replacement values for parameters found in the formatString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The enum value represented by the given string, ignoring surrounding whitespace and case,
   *          and transforming and dashes to underscores.
   *          E.g. Some-Value, some-value and Some_Value will all be transformed to SOME_VALUE before being interpreted as enum value.
   *
   *  @exception FormatException If parameterizedString is not a valid enum value.
   */
  public static <T extends Enum<T>> T toEnum(Class<T> enumType, String parameterizedString, Properties replacements) {
    return toEnum(enumType, toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret a string value as an enum value.
   *  First expand any embedded parameter syntax.
   *
   *  @param <T> The enum type.
   *  @param enumType The type of enum value to convert to.
   *  @param parameterizedString The value to interpret, after parameter expansion.
   *  @param replacements Replacement values for parameters found in the parameterizedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The enum value represented by the given string, ignoring surrounding whitespace and case,
   *          and transforming and dashes to underscores.
   *          E.g. Some-Value, some-value and Some_Value will all be transformed to SOME_VALUE before being interpreted as enum value.
   *
   *  @exception FormatException If parameterizedString is not a valid enum value.
   */
  public static <T extends Enum<T>> T toEnum(Class<T> enumType, String parameterizedString, Map<String, ?> replacements) {
    return toEnum(enumType, toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret a string value as an enum value.
   *
   *  @param <T> The enum type.
   *  @param enumType The type of enum value to convert to.
   *  @param val The value to interpret.
   *  @param expand Whether to first replace any parameter syntax in val.
   *
   *  @return The enum value represented by the given string, ignoring surrounding whitespace and case,
   *          and transforming and dashes to underscores.
   *          E.g. Some-Value, some-value and Some_Value will all be transformed to SOME_VALUE before being interpreted as enum value.
   *
   *  @exception FormatException If val is not a valid enum value.
   */
  public static <T extends Enum<T>> T toEnum(Class<T> enumType, String val, boolean expand) {
    Contract.isNotNull("enumType", enumType);
    if (expand) {
      val = toExpandedString(val);
    }
    if (val == null) {
      return null;
    }
    val = val.trim();
    String normalizedVal = val.trim()
                              .replace(' ', '_')
                              .replace('-', '_')
                              .toUpperCase();
    return Enum.valueOf(enumType, normalizedVal);
  }

  /**
   *  Return the canonical string representation of the provided enum value.
   *
   *  @param <T> The enum type.
   *  @param val The enum value to represent as a string.
   *
   *  @return The canonical representation of the provided enum value, typically in all-lowercase and with dashes as word separator.
   */
  public static <T extends Enum<T>> String fromEnum(T val) {
    return fromEnum(val, null);
  }

  /**
   *  Return the canonical string representation of the provided enum value.
   *
   *  @param <T> The enum type.
   *  @param val The enum value to represent as a string.
   *  @param nullVal The value to return if val is null.
   *  @return The canonical representation of the provided enum value, typically in all-lowercase and with dashes as word separator.
   */
  public static <T extends Enum<T>> String fromEnum(T val, String nullVal) {
    if (val == null) {
      return nullVal;
    }
    return val.toString()
              .replace('_', '-')
              .toLowerCase();
  }

  /**
   *  Interpret a string value as a class type.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret, after parameter expansion.
   *
   *  @return The class type represented by the given string, ignoring surrounding whitespace and case.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static Class<?> toClass(String parameterizedString) {
    return toClass(parameterizedString, true);
  }

  /**
   *  Interpret a string value as a class type.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret, after parameter expansion.
   *  @param replacements Replacement values for parameters found in the formatString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The class type represented by the given string, ignoring surrounding whitespace and case.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static Class<?> toClass(String parameterizedString, Properties replacements) {
    return toClass(toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret a string value as a class type.
   *  First expand any embedded parameter syntax.
   *
   *  @param parameterizedString The value to interpret, after parameter expansion.
   *  @param replacements Replacement values for parameters found in the parameterizedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The class type represented by the given string, ignoring surrounding whitespace and case.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static Class<?> toClass(String parameterizedString, Map<String, ?> replacements) {
    return toClass(toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret a string value as class type.
   *
   *  @param val The value to interpret.
   *  @param expand Whether to first replace any parameter syntax in val.
   *
   *  @return The class type represented by the given string, ignoring surrounding whitespace and case.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static Class<?> toClass(String val, boolean expand) {
    return toClass(val, expand, null);
  }

  /**
   *  Interpret a string value as class type.
   *
   *  @param val The value to interpret.
   *  @param expand Whether to first replace any parameter syntax in val.
   *  @param nullVal The value to return if val is null.
   *
   *  @return The class type represented by the given string, ignoring surrounding whitespace and case.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static Class<?> toClass(String val, boolean expand, Class<?> nullVal) {
    if (val == null) {
      return nullVal;
    }
    if (expand) {
      val = toExpandedString(val);
    }
    try {
      return Class.forName(val.trim());
    }
    catch (ClassNotFoundException e) {
      throw new FormatException(val, e);
    }
  }

  /**
   *  Interpret a string value as a class type.
   *  First expand any embedded parameter syntax.
   *
   *  @param baseType The type the resulting class should derive from.
   *  @param parameterizedString The value to interpret, after parameter expansion.
   *
   *  @return The class type represented by the given string, ignoring surrounding whitespace and case.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static <E> Class<? extends E> toClass(Class<? extends E> baseType, String parameterizedString) {
    return toClass(baseType, parameterizedString, true);
  }

  /**
   *  Interpret a string value as a class type.
   *  First expand any embedded parameter syntax.
   *
   *  @param baseType The type the resulting class should derive from.
   *  @param parameterizedString The value to interpret, after parameter expansion.
   *  @param replacements Replacement values for parameters found in the formatString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The class type represented by the given string, ignoring surrounding whitespace and case.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static <E> Class<? extends E> toClass(Class<? extends E> baseType, String parameterizedString, Properties replacements) {
    return toClass(baseType, toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret a string value as a class type.
   *  First expand any embedded parameter syntax.
   *
   *  @param baseType The type the resulting class should derive from.
   *  @param parameterizedString The value to interpret, after parameter expansion.
   *  @param replacements Replacement values for parameters found in the parameterizedString when replacement parameters don't indicate another search context. If <code>null</code>, parameters without context will not get replaced.
   *
   *  @return The class type represented by the given string, ignoring surrounding whitespace and case.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static <E> Class<? extends E> toClass(Class<? extends E> baseType, String parameterizedString, Map<String, ?> replacements) {
    return toClass(baseType, toExpandedString(parameterizedString, replacements), false);
  }

  /**
   *  Interpret a string value as class type.
   *
   *  @param baseType The type the resulting class should derive from.
   *  @param val The value to interpret.
   *  @param expand Whether to first replace any parameter syntax in val.
   *
   *  @return The class type represented by the given string, ignoring surrounding whitespace and case.
   *
   *  @exception FormatException If parameterizedString is not a valid value.
   */
  public static <E> Class<? extends E> toClass(Class<? extends E> baseType, String val, boolean expand) {
    Class<?> type = null;
    type = toClass(val, expand);
    if (baseType.isAssignableFrom(type)) {
      return (Class<? extends E>)type;
    }
    throw new FormatException(val, "A type '%s' was found but it doesn't derive from '%s'", type.getCanonicalName(), baseType.getCanonicalName());
  }

  /**
   *  Return the canonical string representation of the provided class type.
   *
   *  @param val The class to represent as a string.
   *
   *  @return The canonical representation of the provided class, so simply <code>val.getCanonicalName()</code>.
   */
  public static String fromClass(Class<?> val) {
    return fromClass(val, null);
  }

  /**
   *  Return the canonical string representation of the provided class type.
   *
   *  @param val The class to represent as a string.
   *  @param nullVal The value to return if val is null.
   *  @return The canonical representation of the provided class, so simply <code>val.getCanonicalName()</code>.
   */
  public static String fromClass(Class<?> val, String nullVal) {
    if (val == null) {
      return nullVal;
    }
    return val.getCanonicalName();
  }
}
