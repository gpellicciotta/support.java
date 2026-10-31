// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

import java.io.Writer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HttpRouteTest
{
  @Test
  public void testMethodMismatchDoesNotHandle() throws Exception {
    HttpRoute route = HttpRoutes.route(HttpMethod.GET, "/foo", (req, resp) -> resp.setStatus(200));
    FakeRequest request = new FakeRequest("POST", "/foo");
    FakeResponse response = new FakeResponse();

    Assertions.assertFalse(route.handle(request, response));
  }

  @Test
  public void testPathMismatchDoesNotHandle() throws Exception {
    HttpRoute route = HttpRoutes.route(HttpMethod.GET, "/foo", (req, resp) -> resp.setStatus(200));
    FakeRequest request = new FakeRequest("GET", "/bar");
    FakeResponse response = new FakeResponse();

    Assertions.assertFalse(route.handle(request, response));
  }

  @Test
  public void testMatchInvokesProcessor() throws Exception {
    HttpRoute route = HttpRoutes.route(HttpMethod.GET, "/foo", (req, resp) -> {
      resp.setStatus(200);
      Writer writer = resp.getWriter();
      writer.write("handled");
      writer.flush();
    });
    FakeRequest request = new FakeRequest("GET", "/foo");
    FakeResponse response = new FakeResponse();

    Assertions.assertTrue(route.handle(request, response));
    Assertions.assertEquals(200, response.getStatusCode());
    Assertions.assertEquals("handled", response.getBodyAsString());
  }

  @Test
  public void testNamedGroupsBecomeRequestAttributes() throws Exception {
    HttpRoute route = HttpRoutes.route(HttpMethod.GET, "/users/(?<id>[0-9]+)", (req, resp) -> resp.setStatus(200));
    FakeRequest request = new FakeRequest("GET", "/users/42");
    FakeResponse response = new FakeResponse();

    Assertions.assertTrue(route.handle(request, response));
    Assertions.assertEquals("42", request.getAttribute("id"));
    Assertions.assertSame(route, request.getAttribute(HttpRoute.class.getName()));
  }

  @Test
  public void testMethodIsCaseInsensitive() throws Exception {
    HttpRoute route = HttpRoutes.route(HttpMethod.GET, "/foo", (req, resp) -> resp.setStatus(200));
    FakeRequest request = new FakeRequest("get", "/foo");
    FakeResponse response = new FakeResponse();

    Assertions.assertTrue(route.handle(request, response));
  }
}
