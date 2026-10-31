// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.*;

import com.pellicciotta.support.exceptions.FormatException;
import com.pellicciotta.support.utils.*;

/**
 *  A set of utility methods for working with typical HTTP-related data or performing typical operations.
 */
public final class HttpUtils
{
  /** Prevent instantiation. */
  private HttpUtils() {  }

  // <editor-fold desc="Support Operations">

  /** @return A newly generated, random unique id, suitable e.g. as a session or request identifier. */
  public static String createUniqueId() {
    return UUID.randomUUID().toString();
  }

  /**
   * Combine two paths into a single path.
   * <p>
   * Examples:
   * <pre>
   *   combinePaths("/foo/bar", null) returns "/foo/bar"
   *   combinePaths(null, "foo") returns "/foo"
   *   combinePaths(".", "foo") returns "/foo"
   *   combinePaths(".", "/foo") returns "/foo"
   *   combinePaths("foo", "bar") returns "/foo/bar"
   *   combinePaths("/foo/", "/bar") returns "/foo/bar"
   *   combinePaths("/foo/", "/bar/zoo/") returns "/foo/bar/zoo/"
   * </pre>
   * </p>
   * @param basePath A base-path.
   * @param extraPath The extra path.
   *
   * @return A combined path that will contain only <code>basePath</code> if <code>extraPath == ".", "" or null</code>,
   *         or only <code>extraPath</code> if <code>basePath == ".", "" or null</code>.
   *         In all other cases the <code>/</code>-concatenated version of both, ensuring there is a single
   *         <code>/</code> between both components.
   *         Also always ensuring the resulting path starts with a leading <code>/</code>.
   */
  public static String combinePaths(String basePath, String extraPath) {
    String combinedPath = "";
    if (StringUtils.isNullOrEmpty(basePath) || ".".equals(basePath)) {
      if (StringUtils.isNullOrEmpty(extraPath) || ".".equals(extraPath)) {
        return "/";
      }
      else {
        combinedPath = extraPath;
      }
    }
    else {
      if (StringUtils.isNullOrEmpty(extraPath) || ".".equals(extraPath)) {
        combinedPath = basePath;
      }
      else {
        if (basePath.endsWith("/") && extraPath.startsWith("/")) {
          combinedPath = basePath + extraPath.substring(1);
        }
        else if (basePath.endsWith("/") || extraPath.startsWith("/")) {
          combinedPath = basePath + extraPath;
        }
        else {
          combinedPath = basePath + "/" + extraPath;
        }
      }
    }
    if (!combinedPath.startsWith("/")) {
      combinedPath = "/" + combinedPath;
    }
    return combinedPath;
  }

  /**
   *  Get the mime-type based on a file path.
   *  <p>
   *  Determination is best-effort based and only for the following extensions, is there a guarantee of a deterministic outcome:</p>
   *  <pre>
   *    .js, .mjs              application/javascript
   *    .json                  application/json
   *    .xml                   application/xml
   *    .htm, .html            text/html
   *    .css                   text/css
   *    .md                    text/markdown
   *    .txt                   text/plain
   *  </pre>
   *  <p>
   *  When not clear, 'application/octet-stream' will be returned.</p>
   *
   *  @param path The file path or name to determine a mime-type for.
   *
   *  @return The mime-type based on the file path's extension, or <kbd>application/octet-stream</kbd> as fallback.
   */
  public static String getMimeType(String path) {
    Contract.isNotNull("path", path);
    return getMimeType(new File(path));
  }

  /**
   *  Get the mime-type based on a file path.
   *  <p>
   *  Determination is best-effort based and only for the following extensions, is there a guarantee of a deterministic outcome:</p>
   *  <pre>
   *    .js, .mjs              application/javascript
   *    .json                  application/json
   *    .xml                   application/xml
   *    .htm, .html            text/html
   *    .css                   text/css
   *    .md                    text/markdown
   *    .txt                   text/plain
   *  </pre>
   *  <p>
   *  When not clear, 'application/octet-stream' will be returned.</p>
   *
   *  @param path The file path or name to determine a mime-type for.
   *
   *  @return The mime-type based on the file path's extension, or <kbd>application/octet-stream</kbd> as fallback.
   */
  public static String getMimeType(File path) {
    Contract.isNotNull("path", path);
    // Deterministic outcome based on extension:
    switch (FileUtils.getExtension(path).toLowerCase()) {
      case "htm":
      case "html": return "text/html";
      case "css":  return "text/css";
      case "md":   return "text/markdown";
      case "mjs":
      case "js":   return "application/javascript";
      case "json": return "application/json";
      case "xml":  return "application/xml";
      case "txt":  return "text/plain";
    }
    // Non-deterministic outcome:
    FileNameMap mimeTypes = URLConnection.getFileNameMap();
    String contentType = mimeTypes.getContentTypeFor(path.getName());
    if (contentType != null) {
      return contentType;
    }
    // Also try:
    try {
      contentType = Files.probeContentType(path.toPath());
      if (contentType != null) {
        return contentType;
      }
    }
    catch (IOException ioe) {
      // Ignore
    }
    // Fallback:
    return "application/octet-stream";
  }

  /**
   *  URL-encode a formatted message.
   *
   *  @param msgFormat The message to URL-encode, after filling any params.
   *  @param params {@link String#format(String, Object...)}-style params.
   *
   *  @return The URL-encoded result.
   */
  @SuppressWarnings("deprecation")
  public static String urlEncode(String msgFormat, String... params) {
    String msg = msgFormat;
    if (params.length > 0) {
      msg = String.format(msgFormat, (Object[])params);
    }
    try {
      return URLEncoder.encode(msg, StandardCharsets.UTF_8.name());
    }
    catch (UnsupportedEncodingException uee) {
      return URLEncoder.encode(msg);
    }
  }

  /**
   *  Create a URL query string with the provided parameters.
   *
   *  @param queryParams Pairs of names and values. Must not be null.
   *
   *  @return The URL-encoded result query string, which is the &amp;-concatenated list of pairs, each of which has its parts URL-encoded and both parts separated by a '='-sign.
   *          Note that this query string will not start with '?' and it will be empty if no queryParams were provided.
   */
  @SafeVarargs
  public static String buildQueryString(Pair<String, String>... queryParams) {
    Contract.isNotNull("queryParams", queryParams);
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (Pair<String, String> e : queryParams) {
      if (!first) {
        sb.append('&');
      }
      sb.append(HttpUtils.urlEncode(e.getFirst()));
      sb.append('=');
      sb.append(HttpUtils.urlEncode(e.getSecond()));
      first = false;
    }
    return sb.toString();
  }

  /**
   *  Decode an URL-encoded message.
   *
   *  @param urlEncodedMessage The message to URL-decode.
   *
   *  @return The URL-decoded result.
   */
  @SuppressWarnings("deprecation")
  public static String urlDecode(String urlEncodedMessage) {
    try {
      return URLDecoder.decode(urlEncodedMessage, StandardCharsets.UTF_8.name());
    }
    catch (UnsupportedEncodingException uee) {
      return URLDecoder.decode(urlEncodedMessage);
    }
  }

  /**
   *  Calculate the HMAC-SHA256 of a message using a caller-supplied key.
   *
   *  @param message The message to sign.
   *  @param key The HMAC key. Never a library constant — the caller owns and supplies this.
   *  @return The base64-encoded HMAC-SHA256 signature bytes.
   */
  public static byte[] hmacSha256(String message, byte[] key) {
    Contract.isNotNull("key", key);
    try {
      Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKey = new SecretKeySpec(key, "HmacSHA256");
      sha256_HMAC.init(secretKey);
      return  Base64.getEncoder().encode(sha256_HMAC.doFinal(message.getBytes()));
    }
    catch (Exception e) {
      throw new RuntimeException("Failed to determine HMAC SHA256 hash", e);
    }
  }

  /**
   *  Encode a payload into a full JWT token.
   *
   *  @param payload The payload (i.e. JWT body) to encode.
   *  @param secret The HMAC signing key. Caller-owned; never store or reuse a library-provided constant.
   *  @return A complete JWT token.
   */
  public static String encodeJwt(String payload, byte[] secret) {
    Contract.isNotNull("secret", secret);
    Base64.Encoder encoder = Base64.getUrlEncoder();
    String header = new String(encoder.encode("{\"alg\":\"HS256\"}".getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    String body = new String(encoder.encode(payload.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    String headerAndBody = header + "." + body;
    String signature = new String(encoder.encode(hmacSha256(headerAndBody, secret)), StandardCharsets.UTF_8);
    String fullJWT = headerAndBody + "." + signature;
    return fullJWT;
  }

  /**
   *  Decode a JWT token into its three parts: header, body and signature, without validating the signature.
   *
   *  @param jwtToken The JWT token to decode.
   *
   *  @return The three parts of the JWT token, base64-decoded but not validated.
   *
   *  @throws com.pellicciotta.support.exceptions.FormatException If the JWT token cannot be properly split into three parts and each part base64-decoded.
   */
  public static String[] decodeJwt(String jwtToken) {
    return decodeJwt(jwtToken, false, null);
  }

  /**
   *  Decode a JWT token into its three parts: header, body and signature.
   *
   *  @param jwtToken The JWT token to decode.
   *  @param validate Whether to validate the signature.
   *  @param secret The HMAC signing key the token was signed with. Required (and must not be null) when
   *                <code>validate == true</code>; ignored otherwise.
   *
   *  @return The three parts of the JWT token, base64-decoded but not validated.
   *
   *  @throws com.pellicciotta.support.exceptions.FormatException If the JWT token cannot be properly split into three parts and each part base64-decoded.
   *  @throws IllegalArgumentException If <code>validate == true</code> and <code>secret == null</code>.
   *  @throws IllegalStateException If <code>validate == true</code> and the signature doesn't match.
   */
  public static String[] decodeJwt(String jwtToken, boolean validate, byte[] secret) {
    if (validate) {
      Contract.isNotNull("secret", secret);
    }
    String[] result = new String[3];
    // Splitting + url+base64-decoding:
    String[] split_string = jwtToken.split("\\.");
    if (split_string.length != 3) {
      throw new FormatException(jwtToken, "The provided jwt token doesn't contain 3 parts");
    }
    String base64EncodedHeader = split_string[0];
    String base64EncodedBody = split_string[1];
    String base64EncodedSignature = split_string[2];
    Base64.Decoder decoder = Base64.getUrlDecoder();
    // Header:
    try {
      String header = new String(decoder.decode(base64EncodedHeader), StandardCharsets.UTF_8);
      result[0] = header;
    }
    catch (IllegalArgumentException e) {
      throw new FormatException(e, base64EncodedHeader, "JWT token header is not base64-encoded");
    }
    // Body:
    try {
      String body = new String(decoder.decode(base64EncodedBody), StandardCharsets.UTF_8);
      result[1] = body;
    }
    catch (IllegalArgumentException e) {
      throw new FormatException(e, base64EncodedBody, "JWT token body is not base64-encoded");
    }
    // Signature:
    try {
      String signature = new String(decoder.decode(base64EncodedSignature), StandardCharsets.UTF_8);
      result[2] = signature;
    }
    catch (IllegalArgumentException e) {
      throw new FormatException(e, base64EncodedSignature, "JWT token signature is not base64-encoded");
    }
    // Validate
    if (validate) {
      String headerAndBody = base64EncodedHeader + "." + base64EncodedBody;
      Base64.Encoder encoder = Base64.getUrlEncoder();
      byte[] calculatedSignatureBytes = encoder.encode(hmacSha256(headerAndBody, secret));
      byte[] providedSignatureBytes = base64EncodedSignature.getBytes(StandardCharsets.UTF_8);
      // Constant-time, byte-exact comparison: avoids both a timing side-channel and the case-insensitivity
      // bug of comparing base64 text with equalsIgnoreCase (base64 is case-sensitive).
      if (!MessageDigest.isEqual(calculatedSignatureBytes, providedSignatureBytes)) {
        throw new IllegalStateException("The calculated signature is invalid");
      }
    }
    return result;
  }

  /**
   *  Encode a user without optional password as a BASE-64 encoded value that can be used
   *  for the HTTP Authorization header, after the fixed text <kbd>Authorization: Basic </kbd>.
   *
   *  The resulting value can be decoded again into its constituent parts via {@link #decodeBasicAuthorizationParts(String)}.
   *
   *  @param user The user name. Must not be null.
   *
   * @return The resulting base64-encoded value of the string <kbd>user:</kbd>, so basically the user, followed by a colon.
   */
  public static String encodeBasicAuthorizationParts(String user) {
    return encodeBasicAuthorizationParts(user, null);
  }

  /**
   *  Encode the user and optional password as a BASE-64 encoded value that can be used
   *  for the HTTP Authorization header, after the fixed text <kbd>Authorization: Basic </kbd>.
   *
   *  The resulting value can be decoded again into its constituent parts via {@link #decodeBasicAuthorizationParts(String)}.
   *
   *  @param user The user name. Must not be null.
   *  @param password The password. Can be null if no password is provided.
   *
   * @return The resulting base64-encoded value of the string <kbd>user:password</kbd>, so basically the user and password
   *         parts, separated by a colon, or simply <kbd>user:</kbd> if no password is provided.
   */
  public static String encodeBasicAuthorizationParts(String user, String password) {
    Contract.isNotNull("user", user);
    String userAndPasswordStr = user + ":" + (password != null ? password : "");
    byte[] userAndPassword = userAndPasswordStr.getBytes(StandardCharsets.UTF_8);
    byte[] base64EncodedUserAndPassword = Base64.getEncoder().encode(userAndPassword);
    String basicAuthCode = new String(base64EncodedUserAndPassword, StandardCharsets.US_ASCII);
    return basicAuthCode;
  }

  /**
   *  Encode the user and optional password as a BASE-64 encoded value that can be used
   *  for the HTTP Authorization header, after the fixed text <kbd>Authorization: Basic </kbd>.
   *
   *  The resulting value can be decoded again into its constituent parts via {@link #decodeBasicAuthorizationParts(String)}.
   *
   *  @param userAndPassword The user name and optional password. The second part must not be null.
   *
   * @return The resulting base64-encoded value of the string <kbd>user:password</kbd>, so basically the user and password
   *         parts, separated by a colon, or simply <kbd>user:</kbd> if no password is provided.
   */
  public static String encodeBasicAuthorizationParts(Pair<String, String> userAndPassword) {
    Contract.isNotNull("userAndPassword", userAndPassword);
    return encodeBasicAuthorizationParts(userAndPassword.getFirst(), userAndPassword.getSecond());
  }

  /**
   *  The 'Basic' authorization header contains the base64-encoded value of a string
   *  that contains a username, a colon and an optional password.
   *
   *  @param basicAuthCode The user and password encoded part, found after <kbd>Authorization: Basic </kbd>.
   *  @return A pair consisting of the decoded user and password values.
   *
   *  @exception FormatException If the passed in basicAuthCode has an invalid structure.
   */
  public static Pair<String, String> decodeBasicAuthorizationParts(String basicAuthCode) {
    Contract.isNotNull("basicAuthCode", basicAuthCode);
    byte[] userAndPassword;
    String userAndPasswordStr;
    try {
      byte[] base64EncodedUserAndPassword = basicAuthCode.getBytes(StandardCharsets.US_ASCII);
      userAndPassword = Base64.getDecoder().decode(base64EncodedUserAndPassword);
      userAndPasswordStr = new String(userAndPassword, StandardCharsets.UTF_8);
    }
    catch (Exception e) {
      throw new FormatException(e, "Basic authentication code '%s' has a wrong structure", basicAuthCode);
    }
    // Verify:
    String[] parts = userAndPasswordStr.split("[:]", -1);
    if (parts.length != 2) {
      throw new FormatException("Basic authentication code '%s' has a wrong structure", basicAuthCode);
    }
    parts[0] = parts[0].trim();
    parts[1] = parts[1].trim();

    String user = parts[0];
    if (user.isEmpty()) {
      throw new FormatException("Basic authentication code '%s' has a missing user part", basicAuthCode);
    }
    String pwd = parts[1];
    if (pwd.isEmpty()) {
      pwd = null;
    }
    return Pair.of(user, pwd);
  }

  // </editor-fold>
}
