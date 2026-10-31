// Copyright (c) Giovanni Pellicciotta
package com.pellicciotta.support.codecs;

import com.pellicciotta.support.utils.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *  An {@link XmlCodec} that will simply use an {@link StringCodec} to transform that native value to/from a {@link String}
 *  representation, which is used as the XML element value. The property name will the provided name.
 */
public final class StringCodecBasedXmlCodec<T> implements XmlCodec<T> {
  /**
   *  Create a new codec that will use the provided name and codec.
   *
   *  @param xmlElName The name to use for encoding/decoding a specific XML element.
   *  @param strCodec The {@link StringCodec} to use to transform from/to a {@link String} XML element value.
   */
  public StringCodecBasedXmlCodec(String xmlElName, StringCodec<T> strCodec) {
    this.xmlElName = xmlElName;
    this.strCodec = strCodec;
  }

  private final String xmlElName;
  private final StringCodec<T> strCodec;

  /** {@inheritDoc} */
  @Override
  public T decode(Element from) {
    assert from != null;
    if (!from.getTagName().equals(this.xmlElName)) {
      throw new IllegalArgumentException("XML element has invalid tag name '" + from.getTagName() + "': '" + xmlElName + "' is expected");
    }
    return this.strCodec.decode(XmlUtils.getText(from));
  }

  /** {@inheritDoc} */
  @Override
  public void encode(T value, Element parentTo) {
    Document xml = parentTo.getOwnerDocument();
    Element el = null;
    if (value != null) {
      StringBuilder sb = new StringBuilder();
      this.strCodec.encode(value, sb);
      String strVal = sb.toString();
      el = XmlUtils.createSimpleElement(xml, this.xmlElName, strVal);
    }
    else {
      el = xml.createElement(this.xmlElName);
    }
    parentTo.appendChild(el);
  }
}
