// Copyright (c) Giovanni Pellicciotta
package com.pellicciotta.support.codecs;

import java.lang.module.Configuration;
import java.util.Map;

/**
 *  A {@link PropertyCodec} that will simply use an {@link StringCodec} to transform a native value to/from a {@link String}
 *  representation, which is used as the property value. The property name will the provided name.
 */
public final class StringCodecBasedPropertyCodec<T> implements PropertyCodec<T>
{
  /**
   *  Create a new codec that will use the provided name and codec.
   *
   *  @param propName The name to use for encoding/decoding a specific property.
   *  @param strCodec The {@link StringCodec} to use to transform from/to a {@link String} property value.
   */
  public StringCodecBasedPropertyCodec(String propName, StringCodec<T> strCodec) {
    this.propName = propName;
    this.strCodec = strCodec;
  }

  private final String propName;
  private final StringCodec<T> strCodec;

  /** {@inheritDoc} */
  @Override
  public T decode(Map<String, String> from) {
    assert from != null;
    if (!from.containsKey(this.propName)) {
      return null;
    }
    String strVal = from.get(this.propName);
    return this.strCodec.decode(strVal);
  }

  /** {@inheritDoc} */
  @Override
  public void encode(T value, Map<String, String> to) {
    assert to != null;
    StringBuilder sb = new StringBuilder();
    this.strCodec.encode(value, sb);
    to.put(this.propName, sb.toString());
  }
}
