// Copyright (c) Giovanni Pellicciotta
package com.pellicciotta.support.codecs;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 *  An {@link PropertyCodec} implementation based on two provided functions.
 */
public class FunctionBasedPropertyCodec<T> implements PropertyCodec<T> {
  /**
   *  Create a new codec that will use the provided functions.
   *
   *  @param decode The decoding function.
   *  @param encode The encoding function.
   */
  public FunctionBasedPropertyCodec(Function<Map<String, String>, T> decode, BiConsumer<T, Map<String, String>> encode) {
    this.decode = decode;
    this.encode = encode;
  }

  private final Function<Map<String, String>, T> decode;
  private final BiConsumer<T, Map<String, String>> encode;

  /** {@inheritDoc} */
  @Override
  public T decode(Map<String, String> from) {
    if (from == null) {
      return null;
    }
    return this.decode.apply(from);
  }

  /** {@inheritDoc} */
  @Override
  public void encode(T value, Map<String, String> to) {
    this.encode.accept(value, to);
  }
}
