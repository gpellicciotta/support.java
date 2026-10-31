// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.annotations;

import java.lang.annotation.*;

/**
 *  Annotation that can be applied to public API elements that should be considered
 *  internal implementation details, and hence not be used by code outside the distribution package
 *  in which the element with this annotation is itself contained.
 *
 *  Applying this annotation is similar to saying that an element is 'internal' in C# terms.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
public @interface Internal {
  /** @return Un-used value that can be used to give a short-description of why an element is internal. */
  String value() default "internal";
}
