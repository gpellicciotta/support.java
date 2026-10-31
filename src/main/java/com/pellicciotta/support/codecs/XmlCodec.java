// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.codecs;

import org.w3c.dom.Element;

/**
 *  Interface for encoding/decoding a value to/from XML.
 *
 *  @param <T> The native type to encode from/to.
 */
public interface XmlCodec<T>
{
  /**
   *  Decode an XML element into a native value.
   *
   *  @param from The XML element representation of the native value.
   *  @return The decoded native value.
   *
   *  @throws RuntimeException If from doesn't represent a valid native value.
   */
  T decode(Element from);

  /**
   *  Encode a native value into an XML representation.
   *
   *  @param value The value to be encoded as XML.
   *  @param parentTo The parent XML element to hang the encoded XML representation under.
   *
   *  @throws RuntimeException If value doesn't represent a valid native value.
   */
  void encode(T value, Element parentTo);
}
