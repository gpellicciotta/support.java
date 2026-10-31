// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.annotations;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 *  Annotation to be applied to methods that can act as HTTP handlers.
 *  <p>
 *  These methods should have the {@link com.pellicciotta.support.http.HttpRequestHandler} signature.</p>
 *  <p>
 *  This annotation is needed so that multiple {@link WebRoute} annotations can be applied to a single method.</p>
 */
@Inherited
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface WebRoutes {
  /**
   *  @return The {@link WebRoute}s this method is associated with.
   */
  WebRoute[] value();
}
