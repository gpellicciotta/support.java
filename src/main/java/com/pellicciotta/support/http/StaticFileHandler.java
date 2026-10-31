// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

import com.pellicciotta.support.cache.Cache;
import com.pellicciotta.support.cache.MemoryCache;
import com.pellicciotta.support.cache.StubCache;
import com.pellicciotta.support.logging.Log;
import com.pellicciotta.support.logging.LogService;
import com.pellicciotta.support.utils.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;

/**
 *  A {@link HttpRequestHandler} that serves files found on the file system or, failing that, the classpath.
 *  Replaces the old {@code javax.servlet}-based {@code StaticServlet} — same behavior, no servlet container
 *  involved.
 */
public class StaticFileHandler implements HttpRequestHandler {
  // Inspired by: http://bruno.defraine.net/StaticServlet.java

  /**
   *  @param baseDir The root directory to serve files from. If <code>null</code>, defaults to the current directory.
   *  @param welcomeFiles File-names to look for and serve whenever a request path resolves to a directory.
   *  @param notFoundFile File to serve if no actual mapping can be found. Can be relative to <code>baseDir</code> too. Can be <code>null</code>.
   *  @param cacheResults Whether files, once loaded, may be cached.
   */
  public StaticFileHandler(String baseDir, List<String> welcomeFiles, String notFoundFile, boolean cacheResults) {
    this(baseDir, welcomeFiles, notFoundFile, cacheResults, null);
  }

  /**
   *  @param baseDir The root directory to serve files from. If <code>null</code>, defaults to the current directory.
   *  @param welcomeFiles File-names to look for and serve whenever a request path resolves to a directory.
   *  @param notFoundFile File to serve if no actual mapping can be found. Can be relative to <code>baseDir</code> too. Can be <code>null</code>.
   *  @param cacheResults Whether files, once loaded, may be cached.
   *  @param urlPrefix If non-<code>null</code>/non-empty, this handler only serves requests whose path starts
   *    with this prefix (matched as a full path segment, e.g. prefix <code>/js/client</code> matches
   *    <code>/js/client/index.js</code> but not <code>/js/clientFoo</code>); the prefix is stripped from the
   *    request path before it's resolved against <code>baseDir</code>. Requests that don't match the prefix
   *    are not handled (<code>handle()</code> returns <code>false</code>), letting them fall through to the
   *    next registered handler. <code>null</code> or empty means "no prefix filtering," matching every request
   *    exactly as before.
   */
  public StaticFileHandler(String baseDir, List<String> welcomeFiles, String notFoundFile, boolean cacheResults, String urlPrefix) {
    this.log = LogService.fromCurrentClass();
    this.baseDir = !StringUtils.isNullOrEmpty(baseDir) ? baseDir : ".";
    this.welcomeFiles = welcomeFiles != null ? welcomeFiles : Collections.emptyList();
    this.cacheResults = cacheResults;
    this.cache = cacheResults ? new MemoryCache<>(100) : new StubCache<>(100);
    this.urlPrefix = normalizeUrlPrefix(urlPrefix);
    if (!StringUtils.isNullOrEmpty(notFoundFile)) {
      String path = notFoundFile.startsWith("/") ? notFoundFile : "/" + notFoundFile;
      this.notFoundResource = this.findStaticResource(path);
      if (this.notFoundResource == null) {
        this.log.error("Failed to load not-found-file '%s'", notFoundFile);
      }
      else {
        this.log.info("File '%s' of type %s loaded as 'not-found-file'", notFoundFile, this.notFoundResource.getMimeType());
      }
    }
    this.log.info("StaticFileHandler has been initialized%n  base-dir: '%s'%n  welcome-files: %s%n  cache-results: %s%n  not-found-file: %s%n  url-prefix: %s",
        this.baseDir,
        CollectionUtils.toString(this.welcomeFiles),
        StringConversionUtils.fromBoolean(this.cacheResults),
        notFoundFile != null ? notFoundFile : "<none>",
        this.urlPrefix != null ? this.urlPrefix : "<none>");
  }

  /** @param baseDir The root directory to serve files from. If <code>null</code>, defaults to the current directory. */
  public StaticFileHandler(String baseDir) {
    this(baseDir, Collections.emptyList(), null, false, null);
  }

  private static String normalizeUrlPrefix(String urlPrefix) {
    if (StringUtils.isNullOrEmpty(urlPrefix)) {
      return null;
    }
    String prefix = urlPrefix.startsWith("/") ? urlPrefix : "/" + urlPrefix;
    if (prefix.endsWith("/") && (prefix.length() > 1)) {
      prefix = prefix.substring(0, prefix.length() - 1);
    }
    return "/".equals(prefix) ? null : prefix;
  }

  private final Log log;
  private final String baseDir;
  private final List<String> welcomeFiles;
  private final boolean cacheResults;
  private final Cache<String, Resource> cache;
  private final String urlPrefix;
  private Resource notFoundResource;

  /**
   *  Strips {@link #urlPrefix} from <code>reqPath</code> when it matches as a full path segment.
   *  @return the stripped path (leading <code>"/"</code> preserved), or <code>null</code> if
   *    <code>reqPath</code> doesn't match {@link #urlPrefix}.
   */
  private String stripUrlPrefix(String reqPath) {
    if (reqPath.equals(this.urlPrefix)) {
      return "/";
    }
    if (reqPath.startsWith(this.urlPrefix + "/")) {
      return reqPath.substring(this.urlPrefix.length());
    }
    return null;
  }

  @Override
  public boolean handle(Request request, Response response) throws IOException {
    if (!"GET".equalsIgnoreCase(request.getMethod()) && !"HEAD".equalsIgnoreCase(request.getMethod())) {
      return false; // Not ours to handle; let another handler (or the 404 fallback) deal with it
    }
    String reqPath = request.getRequestURI();
    if (this.urlPrefix != null) {
      reqPath = this.stripUrlPrefix(reqPath);
      if (reqPath == null) {
        return false; // Doesn't match our prefix; let another handler deal with it
      }
    }
    Resource resource = this.findStaticResourceCached(reqPath);
    boolean found = (resource != null);
    if (!found) {
      resource = this.notFoundResource;
    }
    if (resource == null) {
      return false;
    }
    long lastModifiedSeconds = found ? (resource.getLastModified() / 1000) : 0;
    if (lastModifiedSeconds > 0) {
      response.setHeader("Last-Modified", DateTimeFormatter.RFC_1123_DATE_TIME.format(Instant.ofEpochSecond(lastModifiedSeconds).atZone(ZoneOffset.UTC)));
      if (isNotModifiedSince(request.getHeader("If-Modified-Since"), lastModifiedSeconds)) {
        response.setStatus(304);
        return true;
      }
    }
    response.setStatus(200);
    response.setContentType(resource.getMimeType());
    int len = resource.getContentLength();
    if (len >= 0) {
      response.setContentLength(len);
    }
    try (InputStream is = resource.getContentStream(); OutputStream os = response.getOutputStream()) {
      StreamUtils.copy(is, os, false);
    }
    return true;
  }

  /** @return <code>true</code> if <code>ifModifiedSince</code> is a valid HTTP date at or after the resource time; malformed values are ignored. */
  private static boolean isNotModifiedSince(String ifModifiedSince, long lastModifiedSeconds) {
    if (StringUtils.isNullOrEmpty(ifModifiedSince)) {
      return false;
    }
    try {
      return ZonedDateTime.parse(ifModifiedSince.trim(), DateTimeFormatter.RFC_1123_DATE_TIME).toEpochSecond() >= lastModifiedSeconds;
    }
    catch (DateTimeParseException e) {
      return false;
    }
  }

  private Resource findStaticResourceCached(String reqPath) {
    if (this.cacheResults) {
      synchronized (this.cache) {
        Resource cached = this.cache.get(reqPath);
        if (cached != null) {
          return cached;
        }
      }
    }
    Resource resource = this.findStaticResource(reqPath);
    if ((resource != null) && this.cacheResults) {
      synchronized (this.cache) {
        this.cache.put(reqPath, resource);
      }
    }
    return resource;
  }

  private Resource findStaticResource(String reqPath) {
    File resourceAsFile = this.findAsFile(reqPath); // First try on file-system
    if (resourceAsFile != null) {
      return this.findStaticResourceOnFileSystem(reqPath, resourceAsFile);
    }
    else { // Then as classpath resource, possibly inside a jar
      return this.findStaticResourceOnClasspath(reqPath);
    }
  }

  private File findAsFile(String reqPath) {
    File filePath = new File(this.baseDir, reqPath.startsWith("/") ? reqPath.substring(1) : reqPath);
    try {
      if (!FileUtils.isContainedIn(filePath, new File(this.baseDir))) {
        // Resolved outside base-dir (e.g. via a ".." segment in the request path) - refuse to serve it
        return null;
      }
    }
    catch (IOException e) {
      // Can't verify containment - refuse to serve it, same as if it were confirmed outside base-dir
      return null;
    }
    return filePath.exists() ? filePath : null;
  }

  private Resource findStaticResourceOnFileSystem(String reqPath, File filePath) {
    // If directory: try finding any of the welcome files
    if (filePath.isDirectory()) {
      this.log.debug("Request path '%s' is directory: looking for welcome file (as file)", reqPath);
      for (String welcomeFile : this.welcomeFiles) {
        File possibleFile = new File(filePath, welcomeFile);
        if (possibleFile.isFile()) {
          filePath = possibleFile;
          this.log.debug("Welcome file '%s' found for request path '%s'", filePath, reqPath);
          break;
        }
      }
    }
    if (filePath.isFile()) {
      final String mimeType = HttpUtils.getMimeType(filePath.getPath());
      this.log.info("File '%s' of type %s found for request path '%s'", filePath, mimeType, reqPath);
      return new StaticFileBasedResource(filePath, mimeType);
    }
    this.log.debug("Failed to find static resource (as file) for request path '%s': '%s' doesn't point to a file", reqPath, filePath);
    return null;
  }

  private Resource findStaticResourceOnClasspath(String reqPath) {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    URL url = classLoader.getResource(reqPath.startsWith("/") ? reqPath.substring(1) : reqPath);
    if (url == null) {
      this.log.debug("Failed to find static resource (as classpath resource) for request path '%s': resource doesn't exist", reqPath);
      return null;
    }
    try {
      if (!"jar".equals(url.getProtocol())) { // Not inside a jar: just use as-is
        final String mimeType = HttpUtils.getMimeType(reqPath);
        return new StaticUrlBasedResource(url, -1, mimeType, -1);
      }
      // Try as a JAR Entry
      ZipEntry ze = ((JarURLConnection)url.openConnection()).getJarEntry();
      if (ze == null) { // Unexpected, still try to use as URL
        final String mimeType = HttpUtils.getMimeType(reqPath);
        return new StaticUrlBasedResource(url, -1, mimeType, -1);
      }
      // If directory: try finding any of the welcome files
      if (ze.isDirectory()) {
        this.log.debug("Request path '%s' is directory: looking for welcome file (as classpath resource)", reqPath);
        for (String welcomeFile : this.welcomeFiles) {
          String welcomePath = HttpUtils.combinePaths(reqPath, welcomeFile);
          url = classLoader.getResource(welcomePath.startsWith("/") ? welcomePath.substring(1) : welcomePath);
          if (url == null) {
            continue;
          }
          ze = ((JarURLConnection)url.openConnection()).getJarEntry();
          if ((ze != null) && !ze.isDirectory()) {
            this.log.debug("Welcome file URL '%s' found for request path '%s'", url.getPath(), reqPath);
            break;
          }
        }
      }
      if ((ze != null) && !ze.isDirectory()) {
        final String mimeType = HttpUtils.getMimeType(url.getPath());
        this.log.info("File '%s' of type %s found for request path '%s'", url.getPath(), mimeType, reqPath);
        return new StaticUrlBasedResource(url, ze.getTime(), mimeType, (int)ze.getSize());
      }
    }
    catch (MalformedURLException e) {
      this.log.debug("Failed to find static resource (as classpath resource) for request path '%s': URL is malformed: %s", reqPath, e);
    }
    catch (Exception e) {
      this.log.debug("Failed to load static resource (as classpath resource) for request path '%s': %s", reqPath, e);
    }
    return null;
  }
}
