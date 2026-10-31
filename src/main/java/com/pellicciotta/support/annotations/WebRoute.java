// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.annotations;

import java.lang.annotation.*;

import com.pellicciotta.support.http.HttpMethod;

/**
 *  Annotation to be applied to methods that can act as HTTP handlers.
 *  <p>
 *  These methods should have the {@link com.pellicciotta.support.http.HttpRequestHandler} signature.</p>
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(WebRoutes.class)
public @interface WebRoute {
  /**
   *  The HTTP method that needs to match.
   */
  HttpMethod method() default HttpMethod.GET;

  /**
   *  The URI path that needs to match. May use regex syntax.
   */
  String path() default "/";

  /**
   *  A human-readable description of this route.
   */
  String description() default "";

  /**
   *  The authentication realms this route is restricted too. May be a comma-separate list of realm names.
   */
  String realms() default "";
}
