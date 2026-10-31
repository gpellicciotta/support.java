// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import com.pellicciotta.support.http.HttpUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HttpUtilsTests
{
  @Test
  public void testGetMimeType()
  {
    Assertions.assertEquals("text/plain", HttpUtils.getMimeType("./foo.txt"));
    Assertions.assertEquals("application/javascript", HttpUtils.getMimeType("./foo.js"));
    Assertions.assertEquals("application/javascript", HttpUtils.getMimeType("./foo.mjs"));
    Assertions.assertEquals("text/html", HttpUtils.getMimeType("./foo.htm"));
    Assertions.assertEquals("text/html", HttpUtils.getMimeType("./foo.html"));
    Assertions.assertEquals("text/markdown", HttpUtils.getMimeType("./foo.md"));
    Assertions.assertEquals("text/css", HttpUtils.getMimeType("./foo.css"));
    Assertions.assertEquals("application/json", HttpUtils.getMimeType("./foo.json"));
    Assertions.assertEquals("application/xml", HttpUtils.getMimeType("./foo.xml"));
    Assertions.assertEquals("application/octet-stream", HttpUtils.getMimeType("./foo.bin"));
  }
}
