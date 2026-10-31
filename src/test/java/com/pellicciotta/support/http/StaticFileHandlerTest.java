// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class StaticFileHandlerTest
{
  @TempDir
  Path tempDir;

  @Test
  public void testServesFileWithCorrectMimeType() throws Exception {
    Files.writeString(this.tempDir.resolve("style.css"), "body { color: red; }");
    StaticFileHandler handler = new StaticFileHandler(this.tempDir.toString());
    FakeRequest request = new FakeRequest("GET", "/style.css");
    FakeResponse response = new FakeResponse();

    Assertions.assertTrue(handler.handle(request, response));
    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertEquals("text/css", response.getHeader("Content-Type"));
    Assertions.assertEquals("body { color: red; }", response.getBodyAsString());
  }

  @Test
  public void testWelcomeFileServedForDirectory() throws Exception {
    Files.writeString(this.tempDir.resolve("index.html"), "<html>home</html>");
    StaticFileHandler handler = new StaticFileHandler(this.tempDir.toString(), List.of("index.html"), null, false);
    FakeRequest request = new FakeRequest("GET", "/");
    FakeResponse response = new FakeResponse();

    Assertions.assertTrue(handler.handle(request, response));
    Assertions.assertEquals("<html>home</html>", response.getBodyAsString());
  }

  @Test
  public void testUnknownPathWithoutNotFoundFileIsNotHandled() throws Exception {
    StaticFileHandler handler = new StaticFileHandler(this.tempDir.toString());
    FakeRequest request = new FakeRequest("GET", "/missing.txt");
    FakeResponse response = new FakeResponse();

    Assertions.assertFalse(handler.handle(request, response));
  }

  @Test
  public void testUnknownPathFallsBackToNotFoundFile() throws Exception {
    Files.writeString(this.tempDir.resolve("404.html"), "not found here");
    StaticFileHandler handler = new StaticFileHandler(this.tempDir.toString(), List.of(), "404.html", false);
    FakeRequest request = new FakeRequest("GET", "/missing.txt");
    FakeResponse response = new FakeResponse();

    Assertions.assertTrue(handler.handle(request, response));
    Assertions.assertEquals("not found here", response.getBodyAsString());
  }

  @Test
  public void testNonGetRequestIsNotHandled() throws Exception {
    Files.writeString(this.tempDir.resolve("style.css"), "body {}");
    StaticFileHandler handler = new StaticFileHandler(this.tempDir.toString());
    FakeRequest request = new FakeRequest("POST", "/style.css");
    FakeResponse response = new FakeResponse();

    Assertions.assertFalse(handler.handle(request, response));
  }

  @Test
  public void testPathTraversalOutsideBaseDirIsRefused() throws Exception {
    Path outsideFile = Files.writeString(this.tempDir.getParent().resolve("outside-" + this.tempDir.getFileName() + ".txt"), "secret");
    try {
      StaticFileHandler handler = new StaticFileHandler(this.tempDir.toString());
      FakeRequest request = new FakeRequest("GET", "/../" + outsideFile.getFileName());
      FakeResponse response = new FakeResponse();

      Assertions.assertFalse(handler.handle(request, response));
    }
    finally {
      Files.deleteIfExists(outsideFile);
    }
  }

  @Test
  public void testCacheResultsCachesResolutionNotContent() throws Exception {
    // cache-results caches the resolved Resource (its File reference + MIME type), not its bytes -
    // content is always read live from disk on every request, cached or not.
    Path file = this.tempDir.resolve("cached.txt");
    Files.writeString(file, "original");
    StaticFileHandler handler = new StaticFileHandler(this.tempDir.toString(), List.of(), null, true);
    FakeRequest request1 = new FakeRequest("GET", "/cached.txt");
    FakeResponse response1 = new FakeResponse();
    Assertions.assertTrue(handler.handle(request1, response1));
    Assertions.assertEquals("original", response1.getBodyAsString());

    Files.writeString(file, "changed");
    FakeRequest request2 = new FakeRequest("GET", "/cached.txt");
    FakeResponse response2 = new FakeResponse();
    Assertions.assertTrue(handler.handle(request2, response2));

    Assertions.assertEquals("changed", response2.getBodyAsString());
  }

  @Test
  public void testUncachedResultReflectsFileChanges() throws Exception {
    Path file = this.tempDir.resolve("uncached.txt");
    Files.writeString(file, "original");
    StaticFileHandler handler = new StaticFileHandler(this.tempDir.toString(), List.of(), null, false);
    FakeRequest request1 = new FakeRequest("GET", "/uncached.txt");
    FakeResponse response1 = new FakeResponse();
    Assertions.assertTrue(handler.handle(request1, response1));
    Assertions.assertEquals("original", response1.getBodyAsString());

    Files.writeString(file, "changed");
    FakeRequest request2 = new FakeRequest("GET", "/uncached.txt");
    FakeResponse response2 = new FakeResponse();
    Assertions.assertTrue(handler.handle(request2, response2));

    Assertions.assertEquals("changed", response2.getBodyAsString());
  }

  @Test
  public void testPrefixedRequestResolvesAgainstPrefixedBaseDir() throws Exception {
    Files.writeString(this.tempDir.resolve("client.js"), "console.log('hi');");
    StaticFileHandler handler = new StaticFileHandler(this.tempDir.toString(), List.of(), null, false, "/js/client");
    FakeRequest request = new FakeRequest("GET", "/js/client/client.js");
    FakeResponse response = new FakeResponse();

    Assertions.assertTrue(handler.handle(request, response));
    Assertions.assertEquals("console.log('hi');", response.getBodyAsString());
  }

  @Test
  public void testNonMatchingPrefixFallsThrough() throws Exception {
    Files.writeString(this.tempDir.resolve("client.js"), "console.log('hi');");
    StaticFileHandler handler = new StaticFileHandler(this.tempDir.toString(), List.of(), null, false, "/js/client");
    FakeRequest request = new FakeRequest("GET", "/other/client.js");
    FakeResponse response = new FakeResponse();

    Assertions.assertFalse(handler.handle(request, response));
  }

  @Test
  public void testPrefixMatchedOnlyAsFullPathSegment() throws Exception {
    Files.writeString(this.tempDir.resolve("client.js"), "console.log('hi');");
    StaticFileHandler handler = new StaticFileHandler(this.tempDir.toString(), List.of(), null, false, "/js/client");
    FakeRequest request = new FakeRequest("GET", "/js/clientFoo/client.js");
    FakeResponse response = new FakeResponse();

    Assertions.assertFalse(handler.handle(request, response));
  }

  @Test
  public void testWelcomeFileResolutionWorksForPrefixedDirectoryRequest() throws Exception {
    Files.writeString(this.tempDir.resolve("index.html"), "<html>home</html>");
    StaticFileHandler handler = new StaticFileHandler(this.tempDir.toString(), List.of("index.html"), null, false, "/js/client");
    FakeRequest request = new FakeRequest("GET", "/js/client/");
    FakeResponse response = new FakeResponse();

    Assertions.assertTrue(handler.handle(request, response));
    Assertions.assertEquals("<html>home</html>", response.getBodyAsString());
  }

  @Test
  public void testPrefixedRequestAtExactPrefixServesWelcomeFile() throws Exception {
    Files.writeString(this.tempDir.resolve("index.html"), "<html>home</html>");
    StaticFileHandler handler = new StaticFileHandler(this.tempDir.toString(), List.of("index.html"), null, false, "/js/client");
    FakeRequest request = new FakeRequest("GET", "/js/client");
    FakeResponse response = new FakeResponse();

    Assertions.assertTrue(handler.handle(request, response));
    Assertions.assertEquals("<html>home</html>", response.getBodyAsString());
  }

  @Test
  public void testPathTraversalOutsidePrefixedBaseDirIsRefused() throws Exception {
    Path outsideFile = Files.writeString(this.tempDir.getParent().resolve("outside-" + this.tempDir.getFileName() + ".txt"), "secret");
    try {
      StaticFileHandler handler = new StaticFileHandler(this.tempDir.toString(), List.of(), null, false, "/js/client");
      FakeRequest request = new FakeRequest("GET", "/js/client/../" + outsideFile.getFileName());
      FakeResponse response = new FakeResponse();

      Assertions.assertFalse(handler.handle(request, response));
    }
    finally {
      Files.deleteIfExists(outsideFile);
    }
  }

  private static final long MODIFIED_MILLIS = 1_700_000_000_000L;
  private static final String MODIFIED_HTTP_DATE = DateTimeFormatter.RFC_1123_DATE_TIME.format(Instant.ofEpochMilli(MODIFIED_MILLIS).atZone(ZoneOffset.UTC));

  private Path writeModifiedFile(String name) throws Exception {
    Path file = Files.writeString(this.tempDir.resolve(name), "body {}");
    Files.setLastModifiedTime(file, java.nio.file.attribute.FileTime.fromMillis(MODIFIED_MILLIS + 456)); // Sub-second part must be ignored
    return file;
  }

  @Test
  public void testServedFileCarriesLastModifiedHeader() throws Exception {
    this.writeModifiedFile("style.css");
    StaticFileHandler handler = new StaticFileHandler(this.tempDir.toString());
    FakeResponse response = new FakeResponse();

    Assertions.assertTrue(handler.handle(new FakeRequest("GET", "/style.css"), response));
    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertEquals(MODIFIED_HTTP_DATE, response.getHeader("Last-Modified"));
  }

  @Test
  public void testEqualIfModifiedSinceGets304WithoutBody() throws Exception {
    this.writeModifiedFile("style.css");
    StaticFileHandler handler = new StaticFileHandler(this.tempDir.toString());
    FakeRequest request = new FakeRequest("GET", "/style.css").withHeader("If-Modified-Since", MODIFIED_HTTP_DATE);
    FakeResponse response = new FakeResponse();

    Assertions.assertTrue(handler.handle(request, response));
    Assertions.assertEquals(304, response.getStatusCode());
    Assertions.assertEquals("", response.getBodyAsString());
    Assertions.assertEquals(MODIFIED_HTTP_DATE, response.getHeader("Last-Modified"));
  }

  @Test
  public void testLaterIfModifiedSinceGets304ForHead() throws Exception {
    this.writeModifiedFile("style.css");
    StaticFileHandler handler = new StaticFileHandler(this.tempDir.toString());
    String later = DateTimeFormatter.RFC_1123_DATE_TIME.format(Instant.ofEpochMilli(MODIFIED_MILLIS + 3_600_000L).atZone(ZoneOffset.UTC));
    FakeRequest request = new FakeRequest("HEAD", "/style.css").withHeader("If-Modified-Since", later);
    FakeResponse response = new FakeResponse();

    Assertions.assertTrue(handler.handle(request, response));
    Assertions.assertEquals(304, response.getStatusCode());
    Assertions.assertEquals("", response.getBodyAsString());
  }

  @Test
  public void testEarlierIfModifiedSinceGetsFullResponse() throws Exception {
    this.writeModifiedFile("style.css");
    StaticFileHandler handler = new StaticFileHandler(this.tempDir.toString());
    String earlier = DateTimeFormatter.RFC_1123_DATE_TIME.format(Instant.ofEpochMilli(MODIFIED_MILLIS - 3_600_000L).atZone(ZoneOffset.UTC));
    FakeRequest request = new FakeRequest("GET", "/style.css").withHeader("If-Modified-Since", earlier);
    FakeResponse response = new FakeResponse();

    Assertions.assertTrue(handler.handle(request, response));
    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertEquals("body {}", response.getBodyAsString());
  }

  @Test
  public void testMalformedIfModifiedSinceGetsFullResponse() throws Exception {
    this.writeModifiedFile("style.css");
    StaticFileHandler handler = new StaticFileHandler(this.tempDir.toString());
    FakeRequest request = new FakeRequest("GET", "/style.css").withHeader("If-Modified-Since", "yesterday-ish");
    FakeResponse response = new FakeResponse();

    Assertions.assertTrue(handler.handle(request, response));
    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertEquals("body {}", response.getBodyAsString());
  }
}
