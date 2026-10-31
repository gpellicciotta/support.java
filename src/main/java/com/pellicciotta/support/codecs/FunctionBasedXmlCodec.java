// Copyright (c) Giovanni Pellicciotta
package com.pellicciotta.support.codecs;

import org.w3c.dom.Element;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 *  An {@link XmlCodec} implementation based on two provided functions.
 */
public class FunctionBasedXmlCodec<T> implements XmlCodec<T>
{
  /**
   *  Create a new codec that will use the provided functions.
   *
   *  @param decode The decoding function.
   *  @param encode The encoding function.
   */
  public FunctionBasedXmlCodec(Function<Element, T> decode, BiConsumer<T, Element> encode) {
    this.decode = decode;
    this.encode = encode;
  }

  private final Function<Element, T> decode;
  private final BiConsumer<T, Element> encode;

  /** {@inheritDoc} */
  @Override
  public T decode(Element from) {
    if (from == null) {
      return null;
    }
    return this.decode.apply(from);
  }

  /** {@inheritDoc} */
  @Override
  public void encode(T value, Element parentTo) {
    this.encode.accept(value, parentTo);
  }
}
