// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.annotations;

import java.lang.annotation.*;

import com.pellicciotta.support.codecs.*;

/**
 *  Mark a field as defining/holding a configuration setting value.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface ConfigSetting {
  /** The simple name of the configuration setting. */
  String name();

  /** The full, human-readable, multi-line description for this setting. */
  String description();

  /** The element type: should typically only be provided for list-settings in which it indicates the list element type. */
  /*Class<? extends StringCodec<?>> */Class elementType() default void.class;

  /** The codec to use to encode/decode string values into this field's native type. */
  /*Class<? extends StringCodec<?>> */Class stringCodec() default void.class;

  /** The codec to use to encode/decode property values into this field's native type. */
  /*Class<? extends PropertyCodec<?>>*/Class propertyCodec() default void.class;

  /** The codec to use to encode/decode an XML sub-tree into this field's native type. */
  /*Class<? extends XmlCodec<?>>*/Class xmlCodec() default void.class;
}
