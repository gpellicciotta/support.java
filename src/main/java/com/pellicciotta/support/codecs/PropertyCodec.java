// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.codecs;

import java.util.Map;

/**
 *  Interface for encoding/decoding a value to/from name/value pairs (i.e. properties).
 *
 *  @param <T> The native type to encode from/to.
 */
public interface PropertyCodec<T> {
  /**
   *  Decode one or multiple name/value pairs into a native value.
   *
   *  @param from The name/value pairs representation of the native value.
   *  @return The decoded native value.
   *
   *  @throws RuntimeException If from doesn't represent a valid native value.
   */
  T decode(Map<String, String> from);

  /**
   *  Encode a native value into one or more name/value pairs.
   *
   *  @param value The value to be encoded as name/value pairs.
   *  @param to The map to add the name/value pair(s) to.
   *
   *  @throws RuntimeException If value doesn't represent a valid native value.
   */
  void encode(T value, Map<String, String> to);
}
