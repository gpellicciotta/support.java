// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.http;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import com.pellicciotta.support.annotations.WebRoute;
import com.pellicciotta.support.annotations.WebRoutes;
import com.pellicciotta.support.utils.ReflectionUtils;
import com.pellicciotta.support.utils.StringUtils;

/**
 *  Factory methods for building {@link HttpRoute}s, either directly or from {@link WebRoute}-annotated methods.
 */
public final class HttpRoutes
{
  /** Prevent instantiation. */
  private HttpRoutes() { }

  /**
   *  Create a route that handles a specific HTTP method and path.
   *
   *  @param method The HTTP method for this route.
   *  @param uriPattern The URI regex to match with the {@link Request#getRequestURI()} for this route.
   *  @param processor The method to invoke when method and uriPattern match.
   *
   *  @return The {@link HttpRoute} that will, if activated, invoke the specified processor.
   */
  public static HttpRoute route(HttpMethod method, String uriPattern, HttpRequestProcessor processor) {
    return new HttpRoute(method, uriPattern, processor, Collections.emptySet(), "");
  }

  /**
   *  Create a route that handles a specific HTTP method and path.
   *
   *  @param method The HTTP method for this route.
   *  @param uriPattern The URI regex to match with the {@link Request#getRequestURI()} for this route.
   *  @param processor The method to invoke when method and uriPattern match.
   *  @param description A human-readable description for this route.
   *
   *  @return The {@link HttpRoute} that will, if activated, invoke the specified processor.
   */
  public static HttpRoute route(HttpMethod method, String uriPattern, HttpRequestProcessor processor, String description) {
    return new HttpRoute(method, uriPattern, processor, Collections.emptySet(), description);
  }

  /**
   *  Create a route that handles a specific HTTP method and path.
   *
   *  @param method The HTTP method for this route.
   *  @param uriPattern The URI regex to match with the {@link Request#getRequestURI()} for this route.
   *  @param processor The method to invoke when method and uriPattern match.
   *  @param realms The auth. realms to associate with this route. Can be empty.
   *  @param description A human-readable description for this route.
   *
   *  @return The {@link HttpRoute} that will, if activated, invoke the specified processor.
   */
  public static HttpRoute route(HttpMethod method, String uriPattern, HttpRequestProcessor processor, Set<String> realms, String description) {
    return new HttpRoute(method, uriPattern, processor, realms, description);
  }

  /**
   *  Create a route that handles a specific HTTP method and path.
   *
   *  @param obj The object to invoke the method on, once activated.
   *  @param method The method must have a {@link WebRoute} annotation and that will be invoked once this route gets activated.
   *
   *  @return The {@link HttpRoute}s that will, if activated, invoke the specified method on the specified object. Cannot be empty.
   *
   *  @throws IllegalArgumentException If the provided method doesn't have the {@link WebRoute} annotation.
   */
  public static Set<HttpRoute> route(Object obj, Method method) {
    if (!method.isAnnotationPresent(WebRoute.class) && !method.isAnnotationPresent(WebRoutes.class)) {
      throw new IllegalArgumentException("The provided method '" + method + "' doesn't have the required @WebRoute annotation");
    }
    Set<HttpRoute> routes = new TreeSet<>();
    for (WebRoute routeAnnotation : method.getAnnotationsByType(WebRoute.class)) {
      HttpMethod httpMethod = routeAnnotation.method();
      String pathPattern = routeAnnotation.path();
      Set<String> realms = new HashSet<>();
      String realmsString = routeAnnotation.realms();
      if (!StringUtils.isNullOrWhitespace(realmsString)) {
        for (String realm : realmsString.split("[,]")) {
          realm = realm.trim();
          if (!realm.isEmpty()) {
            realms.add(realm);
          }
        }
      }
      String description = routeAnnotation.description();
      final HttpRequestProcessor processor = new HttpRequestProcessor() {
        @Override
        public void process(Request request, Response response) throws Exception {
          method.invoke(obj, request, response);
        }
      };
      HttpRoute route = new HttpRoute(httpMethod, pathPattern, processor, realms, description);
      routes.add(route);
    }
    return routes;
  }

  /**
   *  Create all routes that are associated via {@link WebRoute} annotations with methods on the provided object.
   *
   *  @param obj The object whose class to inspect for {@link WebRoute} annotations.
   *
   *  @return The set of {@link HttpRoute}s that were created based on {@link WebRoute} annotations. Each route will result in invoking an method on the provided object.
   */
  public static Set<HttpRoute> routes(Object obj) {
    Set<HttpRoute> routes = new TreeSet<>();
    for (Method m : ReflectionUtils.listAnnotatedMethods(obj.getClass(), WebRoute.class)) {
      routes.addAll(route(obj, m));
    }
    return routes;
  }
}
