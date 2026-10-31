// Copyright (c) Giovanni Pellicciotta
package com.pellicciotta.support.codecs;

import java.util.Map;
import java.util.function.Function;

/**
 *  A {@link StringCodec} implementation based on two provided functions.
 */
public class FunctionBasedStringCodec<T> implements StringCodec<T> {
  /**
   *  Create a new codec that will use the provided functions.
   *
   *  @param decode The decoding function.
   *  @param encode The encoding function.
   */
  public FunctionBasedStringCodec(Function<String, T> decode, Function<T, String> encode) {
    this.decode = decode;
    this.encode = encode;
  }

  private final Function<String, T> decode;
  private final Function<T, String> encode;

  /** {@inheritDoc} */
  @Override
  public T decode(String from) {
    if (from == null) {
      return null;
    }
    return this.decode.apply(from);
  }

  /** {@inheritDoc} */
  @Override
  public String encode(T value) {
    return this.encode.apply(value);
  }
}
