// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.annotations;

import java.lang.annotation.*;

/**
 *  Mark a field as defining/holding a group of configuration settings.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface ConfigGroup
{
  /** The simple name of the settings group. */
  String name();

  /** The full, human-readable, multi-line description for this settings group. */
  String description();
}
