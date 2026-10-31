// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

import org.junit.jupiter.api.*;

public class HttpStatusCodesTest
{
  @Test
  public void testConstantValues() {
    Assertions.assertEquals(200, HttpStatusCodes.SC_OK);
    Assertions.assertEquals(201, HttpStatusCodes.SC_CREATED);
    Assertions.assertEquals(202, HttpStatusCodes.SC_ACCEPTED);
    Assertions.assertEquals(204, HttpStatusCodes.SC_NO_CONTENT);
    Assertions.assertEquals(400, HttpStatusCodes.SC_BAD_REQUEST);
    Assertions.assertEquals(401, HttpStatusCodes.SC_UNAUTHORIZED);
    Assertions.assertEquals(403, HttpStatusCodes.SC_FORBIDDEN);
    Assertions.assertEquals(409, HttpStatusCodes.SC_CONFLICT);
    Assertions.assertEquals(429, HttpStatusCodes.SC_TOO_MANY_REQUESTS);
    Assertions.assertEquals(500, HttpStatusCodes.SC_INTERNAL_SERVER_ERROR);
    Assertions.assertEquals(503, HttpStatusCodes.SC_SERVICE_UNAVAILABLE);
  }
}
