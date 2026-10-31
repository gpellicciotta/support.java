// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import com.pellicciotta.support.logging.Log;
import com.pellicciotta.support.logging.LogService;
import com.pellicciotta.support.utils.Pair;
import com.pellicciotta.support.utils.StreamUtils;

/**
 *  A class for performing HTTP(s) communication as client in a client/server architecture.
 */
public final class HttpClient implements AutoCloseable {
  // <editor-fold desc="Quick Operations">

  static final Log log = LogService.fromCurrentClass();

  /**
   *  GET the provided URL, returning any response body as text.
   *
   *  @param url The URL to connect to.
   *  @return The response body as text.
   *
   *  @throws IOException If the response code was <code>&gt;= 400</code>.
   */
  public static String getText(String url) throws IOException {
    try (HttpClient client = new HttpClient(HttpMethod.GET, url)) {
      client.connect();
      if (client.responseCode >= 400) {
        throw new IOException("GET " + url + " resulted in " + client.responseCode + " - " + client.responseMessage);
      }
      return client.responseBody;
    }
  }

  /**
   *  GET the provided URL while requesting an <code>application/json</code> return-type and returning any response body as string.
   *
   *  @param url The URL to connect to.
   *  @return The response body as text.
   *
   *  @throws IOException If the response code was <code>&gt;= 400</code>.
   */
  public static String getJson(String url) throws IOException {
    try (HttpClient client = new HttpClient(HttpMethod.GET, url)) {
      client.setAcceptHeader("application/json");
      client.connect();
      if (client.responseCode >= 400) {
        throw new IOException("GET " + url + " resulted in " + client.responseCode + " - " + client.responseMessage);
      }
      return client.responseBody;
    }
  }

  /**
   *  GET the provided URL while requesting a specific return-type and returning any response body as string.
   *
   *  @param url The URL to connect to.
   *  @param acceptHeader The mime-type requested for the response.
   *  @return The response body as text.
   *
   *  @throws IOException If the response code was <code>&gt;= 400</code>.
   */
  public static String getText(String url, String acceptHeader) throws IOException {
    try (HttpClient client = new HttpClient(HttpMethod.GET, url)) {
      client.setAcceptHeader(acceptHeader);
      client.connect();
      if (client.responseCode >= 400) {
        throw new IOException("GET " + url + " resulted in " + client.responseCode + " - " + client.responseMessage);
      }
      return client.responseBody;
    }
  }

  /**
   *  POST name/value pairs to the provided URL while requesting a specific return-type and returning any response body as string.
   *
   *  @param url The URL to connect to.
   *  @param acceptHeader The mime-type requested for the response.
   *  @param nameValuePairs The name/value-pairs to send as form data.
   *
   *  @return The response body as text.
   *
   *  @throws IOException If the response code was <code>&gt;= 400</code>.
   */
  public static String postFormData(String url, String acceptHeader, Pair<String, String>... nameValuePairs) throws IOException {
    try (HttpClient client = new HttpClient(HttpMethod.POST, url)) {
      client.setAcceptHeader(acceptHeader);
      client.setFormDataBody(nameValuePairs);
      client.connect();
      if (client.responseCode >= 400) {
        throw new IOException("POST " + url + " resulted in " + client.responseCode + " - " + client.responseMessage);
      }
      return client.responseBody;
    }
  }

  /**
   *  POST name/value pairs to the provided URL and returning any response body as string.
   *
   *  @param url The URL to connect to.
   *  @param nameValuePairs The name/value-pairs to send as form data.
   *
   *  @return The response body as text.
   *
   *  @throws IOException If the response code was <code>&gt;= 400</code>.
   */
  public static String postFormData(String url, Pair<String, String>... nameValuePairs) throws IOException {
    try (HttpClient client = new HttpClient(HttpMethod.POST, url)) {
      client.setFormDataBody(nameValuePairs);
      client.connect();
      if (client.responseCode >= 400) {
        throw new IOException("POST " + url + " resulted in " + client.responseCode + " - " + client.responseMessage);
      }
      return client.responseBody;
    }
  }

  /**
   *  POST a JSON body to the provided URL and returning any response body as string.
   *
   *  @param url The URL to connect to.
   *  @param json The JSON text to use as body in the request.
   *
   *  @return The response body as text.
   *
   *  @throws IOException If the response code was <code>&gt;= 400</code>.
   */
  public static String postJson(String url, String json) throws IOException {
    try (HttpClient client = new HttpClient(HttpMethod.POST, url)) {
      client.setAcceptHeader("application/json");
      client.setJsonBody(json);
      client.connect();
      if (client.responseCode >= 400) {
        throw new IOException("POST " + url + " resulted in " + client.responseCode + " - " + client.responseMessage);
      }
      return client.responseBody;
    }
  }

  // </editor-fold>
  // <editor-fold desc="Construction">

  /**
   *  Create a new connection with the given method and URL.
   *
   *  @param method The HTTP method to use.
   *  @param url The URL to connect to.
   */
  public HttpClient(HttpMethod method, String url) throws IOException {
    this.method = method;
    this.url = new URL(url);
    URLConnection con = this.url.openConnection();
    this.http = (HttpURLConnection)con;
    this.http.setRequestProperty("User-Agent", "pellicciotta-hiscore");
    this.http.setRequestMethod(this.method.toString());
    this.http.setDoInput(true);
  }

  private final HttpMethod method;
  private final URL url;
  private final HttpURLConnection http;
  private final Map<String, String> formData = new HashMap<>();
  private final Map<String, String> headers = new TreeMap<>(String::compareToIgnoreCase);
  private String requestBody;
  private byte[] requestBodyBytes;
  private String responseBody;
  private byte[] responseBodyBytes;
  private int responseCode;
  private String responseMessage;

  // </editor-fold>
  // <editor-fold desc="Response Properties - Only filled after connect">

  /** @return The HTTP status code; only meaningful after the connection was made. */
  public int getResponseCode() {
    return this.responseCode;
  }

  /** @return The HTTP status message; only meaningful after the connection was made. */
  public String getResponseMessage() {
    return this.responseMessage;
  }

  /** @return The response body decoded as text; only filled after the connection was made. */
  public String getResponseBody() {
    return this.responseBody;
  }

  /** @return All response headers by name; only valid after the connection was made. */
  public Map<String, List<String>> getResponseHeaders() {
    return this.http.getHeaderFields();
  }

  /**
   *  @param name the header name
   *  @return The first value of that response header, or null if absent; only valid after the connection
   *  was made.
   */
  public String getResponseHeader(String name) {
    return this.http.getHeaderField(name);
  }

  /** @return The raw response body; only filled after the connection was made. */
  public byte[] getResponseBodyBytes() {
    return this.responseBodyBytes;
  }

  // </editor-fold>
  // <editor-fold desc="Request Properties - to be set before connect">

  /**
   *  Sets a request header, replacing any previous value; names are matched case-insensitively.
   *  @param name the header name
   *  @param value the header value
   */
  public void setRequestHeader(String name, String value) {
    this.headers.put(name, value);
  }

  /**
   *  Sets the {@code User-Agent} request header.
   *  @param userAgentString the user agent string
   */
  public void setUserAgent(String userAgentString) {
    this.headers.put("User-Agent", userAgentString);
  }

  /**
   *  Sets the {@code Accept} request header.
   *  @param acceptMimeType the accepted MIME type(s)
   */
  public void setAcceptHeader(String acceptMimeType) {
    this.headers.put("Accept", acceptMimeType);
  }

  /**
   *  Sets a URL-encoded form body and the matching {@code Content-Type} (UTF-8).
   *  @param nameValuePairs the form fields, in order
   */
  public void setFormDataBody(Pair<String, String>... nameValuePairs) {
    this.headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (Pair<String, String> e : nameValuePairs) {
      if (!first) {
        sb.append('&');
      }
      sb.append(HttpUtils.urlEncode(e.getFirst()));
      sb.append('=');
      sb.append(HttpUtils.urlEncode(e.getSecond()));
      first = false;
    }
    this.requestBody = sb.toString();
    this.requestBodyBytes = this.requestBody.getBytes(StandardCharsets.UTF_8);
  }

  /**
   *  Sets a JSON request body and {@code Content-Type} {@code application/json; charset=UTF-8}.
   *  @param jsonBody the JSON text; must not be null
   */
  public void setJsonBody(String jsonBody) {
    this.headers.put("Content-Type", "application/json; charset=UTF-8");
    this.requestBody = jsonBody;
    this.requestBodyBytes = this.requestBody.getBytes(StandardCharsets.UTF_8);
  }

  /**
   *  Sets a plain-text request body and {@code Content-Type} {@code text/plain; charset=UTF-8}.
   *  @param textBody the text; must not be null
   */
  public void setTextBody(String textBody) {
    this.headers.put("Content-Type", "text/plain; charset=UTF-8");
    this.requestBody = textBody;
    this.requestBodyBytes = this.requestBody.getBytes(StandardCharsets.UTF_8);
  }

  /**
   *  Sets a binary request body and {@code Content-Type} {@code application/octet-stream}.
   *  @param binaryBody the bytes to send
   */
  public void setBinaryBody(byte[] binaryBody) {
    this.headers.put("Content-Type", "application/octet-stream");
    this.requestBodyBytes = binaryBody;
    this.requestBody = new String(this.requestBodyBytes, StandardCharsets.UTF_8);
  }

  // </editor-fold>
  // <editor-fold desc="Methods">

  /**
   *  Make the connection, which means sending all data and then checking the response.
   */
  public void connect() throws IOException {
    // Set headers:
    for (Map.Entry<String, String> header : this.headers.entrySet()) {
      this.http.setRequestProperty(header.getKey(), header.getValue());
    }
    // Send body if any:
    if (this.requestBodyBytes != null) {
      this.http.setDoOutput(true);
      this.http.setFixedLengthStreamingMode(this.requestBodyBytes.length);
      this.http.connect();
      try (OutputStream os = http.getOutputStream()) {
        os.write(this.requestBodyBytes);
      }
    }
    // Receive input:
    this.http.connect();
    this.responseCode = this.http.getResponseCode();
    this.responseMessage = this.http.getResponseMessage();
    // According to https://bugs.java.com/bugdatabase/view_bug.do?bug_id=4513568:
    InputStream is;
    if (this.responseCode >= 400) {
      is = this.http.getErrorStream();
    }
    else {
      is = this.http.getInputStream();
    }
    if (is != null) {
      this.responseBodyBytes = StreamUtils.readBinaryStream(is);
      Charset charset = StandardCharsets.UTF_8;
      String enc = this.http.getContentEncoding();
      if ((enc != null) && Charset.isSupported(enc)) {
        charset = Charset.forName(enc);
      }
      this.responseBody = new String(this.responseBodyBytes, charset);
    }
    log.debug("%s %s%n%d - %s", this.method, this.url, this.responseCode, this.responseMessage);
  }

  /**
   *  Close any lingering connection.
   */
  @Override
  public void close() {
    this.http.disconnect();
  }

  // </editor-fold>
}
