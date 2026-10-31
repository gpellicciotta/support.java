// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.codecs;

import org.w3c.dom.Element;

/**
 *  Interface for encoding/decoding a value to/from a {@link String}.
 *
 *  @param <T> The native type to encode from/to.
 */
public interface StringCodec<T>
{
  /**
   *  Decode a {@link String} into a native value.
   *
   *  @param from The {@link String} representation of the native value.
   *  @return The decoded native value.
   *
   *  @throws RuntimeException If from doesn't represent a valid native value.
   */
  T decode(String from);

  /**
   *  Encode a native value into a {@link String}.
   *
   *  @param value The value to be encoded as a {@link String}.
   *  @return The {@link String} representing the encoded value.
   *
   *  @throws RuntimeException If value doesn't represent a valid native value.
   */
  String encode(T value);

  /**
   *  Encode a native value into a {@link String}.
   *
   *  @param value The value to be encoded as a {@link String}.
   *  @param to The {@link StringBuilder} to append the encoded value to, if it is not <code>null</code>.
   *
   *  @throws RuntimeException If value doesn't represent a valid native value.
   */
  default void encode(T value, StringBuilder to) {
    String strValue = this.encode(value);
    if (strValue != null) {
      to.append(strValue);
    }
  }
}
