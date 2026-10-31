// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http.messages;

import com.pellicciotta.support.http.HttpMethod;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Set;

public class ApiInfoMessageTest
{
  @Test
  public void testApiVersionMessageFields() {
    ApiVersionMessage msg = new ApiVersionMessage("1.2.3");
    Assertions.assertEquals("1.2.3", msg.version);
  }

  @Test
  public void testApiVersionMessageRejectsNull() {
    Assertions.assertThrows(RuntimeException.class, () -> new ApiVersionMessage(null));
  }

  @Test
  public void testApiRouteMessageFields() {
    ApiRouteMessage route = new ApiRouteMessage(HttpMethod.GET, "/api/status", "status route", Set.of("admin"));
    Assertions.assertEquals(HttpMethod.GET, route.method);
    Assertions.assertEquals("/api/status", route.uriPattern);
    Assertions.assertEquals("status route", route.description);
    Assertions.assertEquals(Set.of("admin"), route.realms);
  }

  @Test
  public void testApiInfoMessageFields() {
    ApiRouteMessage route = new ApiRouteMessage(HttpMethod.GET, "/api/status", "status route", Set.of());
    ApiInfoMessage info = new ApiInfoMessage("1.2.3", List.of(route));
    Assertions.assertEquals("1.2.3", info.version);
    Assertions.assertEquals(List.of(route), info.apiEndPoints);
  }

  @Test
  public void testApiInfoMessageRejectsNullRoutes() {
    Assertions.assertThrows(RuntimeException.class, () -> new ApiInfoMessage("1.2.3", null));
  }
}
