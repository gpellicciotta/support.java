// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

import com.pellicciotta.support.logging.Log;
import com.pellicciotta.support.logging.LogService;
import org.junit.jupiter.api.*;

public class HttpUtilsTest {
  @Test
  public void testCombinePaths() {
    Assertions.assertEquals("/", HttpUtils.combinePaths(null, null));
    Assertions.assertEquals("/", HttpUtils.combinePaths("", null));
    Assertions.assertEquals("/", HttpUtils.combinePaths(".", null));

    Assertions.assertEquals("/", HttpUtils.combinePaths(null, "."));
    Assertions.assertEquals("/", HttpUtils.combinePaths("", "."));
    Assertions.assertEquals("/", HttpUtils.combinePaths(".", "."));

    Assertions.assertEquals("/", HttpUtils.combinePaths(null, ""));
    Assertions.assertEquals("/", HttpUtils.combinePaths("", ""));
    Assertions.assertEquals("/", HttpUtils.combinePaths(".", ""));

    Assertions.assertEquals("/foo", HttpUtils.combinePaths(null, "foo"));
    Assertions.assertEquals("/foo", HttpUtils.combinePaths("", "foo"));
    Assertions.assertEquals("/foo", HttpUtils.combinePaths(".", "foo"));

    Assertions.assertEquals("/foo", HttpUtils.combinePaths(null, "/foo"));
    Assertions.assertEquals("/foo", HttpUtils.combinePaths("", "/foo"));
    Assertions.assertEquals("/foo", HttpUtils.combinePaths(".", "/foo"));

    Assertions.assertEquals("/foo", HttpUtils.combinePaths("foo",null));
    Assertions.assertEquals("/foo/", HttpUtils.combinePaths("/foo/", ""));
    Assertions.assertEquals("/foo", HttpUtils.combinePaths("foo", "."));

    Assertions.assertEquals("/foo/bar", HttpUtils.combinePaths("/foo/bar", null));

    Assertions.assertEquals("/foo", HttpUtils.combinePaths(".", "/foo"));
    Assertions.assertEquals("/foo/bar", HttpUtils.combinePaths("foo", "bar")) ;
    Assertions.assertEquals("/foo/bar", HttpUtils.combinePaths("/foo/", "/bar"));
    Assertions.assertEquals("/foo/goo/bar/zoo/", HttpUtils.combinePaths("foo/goo", "/bar/zoo/"));
  }

  @Test
  public void testEncodeDecodeJwt() {
    byte[] secret = "test-only-jwt-secret".getBytes();
    for (String val : new String[] { "foobar", "!@#$%^", "1234567890" }) {
      String encoded = HttpUtils.encodeJwt(val, secret);
      String decoded = HttpUtils.decodeJwt(encoded, true, secret)[1];
      Assertions.assertEquals(val, decoded);
    }
  }

  @Disabled
  @Test
  public void adhocEncodeDecode() {
    String[] args = new String[] { "encode", "foobar" };
    byte[] secret = "test-only-jwt-secret".getBytes();
    Log log = LogService.fromName("jwt-test");
    if (args.length != 2) {
      log.error("Invalid command-line: 2 args expected");
      System.exit(1);
    }
    String arg = args[1];
    switch (args[0].toLowerCase()) {
      case "encode-jwt":
      case "encode":
        String result = HttpUtils.encodeJwt(arg, secret);
        log.info("JWT: [%s]", result);
        return ;
      case "decode-jwt":
      case "decode":
        try {
          String[] jwt = HttpUtils.decodeJwt(arg, true, secret);
          log.info("JWT: [%s|%s]", jwt[0], jwt[1]);
        }
        catch (IllegalStateException e) {
          String[] jwt = HttpUtils.decodeJwt(arg);
          log.info("JWT: [%s|%s]", jwt[0], jwt[1]);
        }
        return ;
      default:
        log.error("Invalid command '" + args[0] + "'");
        System.exit(1);
    }
  }
}
