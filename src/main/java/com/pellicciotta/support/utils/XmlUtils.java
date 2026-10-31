// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.regex.Pattern;

import com.pellicciotta.support.exceptions.FormatException;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

/**
 * A collection of XML related utility methods.
 */
public abstract class XmlUtils
{
  /** Prevent instantiation */
  private XmlUtils() { }

  /**
   * Create an XML DOM Document object from a string.
   *
   * @param xmlText The XML text to be turned into a DOM Document.
   * @return The created XML document.
   *
   * @throws IOException If the xmlText doesn't contain valid XML.
   */
  public static Document createDocument(String xmlText) throws IOException {
    Contract.isNotNull("xmlText", "xmlText");
    Pattern validXmlStart = Pattern.compile("[<][?]xml[ ]", Pattern.CASE_INSENSITIVE);
    if (!validXmlStart.matcher(xmlText).find()) {
      final String defaultXmlHeader = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
      xmlText = defaultXmlHeader + EnvironmentUtils.getLineSeparator() + xmlText;
    }
    return createDocument(new InputSource(new StringReader(xmlText)));
  }

  /**
   * Create an XML DOM Document object from a FileInputStream.
   *
   * @param xmlFile The XML file to be turned into a DOM Document.
   * @return The created XML document.
   *
   * @throws IOException If the xmlFile does not exist, or is not a valid XML file.
   */
  public static Document createDocument(File xmlFile) throws IOException {
    try (FileInputStream fs = new FileInputStream(xmlFile)) {
      return createDocument(new InputSource(fs));
    }
  }

  /**
   * Create an XML DOM Document object from a string.
   *
   * @param xmlSource The XML input source to be turned into a DOM Document.
   * @return The created XML document.
   *
   * @throws IOException If the xmlFileStream cannot be read or it doesn't contain valid XML.
   */
  public static Document createDocument(InputSource xmlSource) throws IOException {
    Contract.isNotNull("xmlSource", xmlSource);
    try {
      DocumentBuilder builder = newSecureDocumentBuilderFactory().newDocumentBuilder();
      // Parse the file into a document
      return builder.parse(xmlSource);
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  /**
   * Build a {@link DocumentBuilderFactory} hardened against XXE (XML External Entity) attacks:
   * no DOCTYPE declarations, no external general/parameter entities. {@code XInclude} stays enabled
   * since it's an actively used feature of this library's own config-file format, not an XXE vector
   * by itself.
   */
  private static DocumentBuilderFactory newSecureDocumentBuilderFactory() throws ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
    factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
    factory.setXIncludeAware(true);
    factory.setNamespaceAware(true);
    return factory;
  }

  /**
   * Create an empty XML DOM Document .
   *
   * @return The created XML document.
   *
   * @throws IOException If the xmlFileStream cannot be read or it doesn't contain valid XML.
   */
  public static Document createDocument() throws IOException {
    try {
      DocumentBuilder builder = newSecureDocumentBuilderFactory().newDocumentBuilder();
      return builder.newDocument();
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  /**
   * Make sure the specified text can be used as text element in a XML document, escaping any special characters if needed.
   *
   * @param text The text that needs to be transformed into a valid XML text element text.
   *
   * @return The specified text with any characters that have a special meaning in XML escaped. The returned text can be used
   *         directly in an XML text element.
   *
   * @throws IllegalArgumentException When text is null.
   */
  public static String escape(String text) throws IllegalArgumentException {
    Contract.isNotNull("text", text);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < text.length(); i++) {
      char ch = text.charAt(i);
      if (ch == '<') {
        sb.append("&lt;");
      } else if (ch == '>') {
        sb.append("&gt;");
      } else if (ch == '&') {
        sb.append("&amp;");
      } else if (ch == '"') {
        sb.append("&quot;");
      } else if (ch == '\'') {
        sb.append("&apos;");
      } else if (ch == '%') {
        sb.append("&#37;");
      } else {
        sb.append(ch);
      }
    }
    return sb.toString();
  }

  /**
   * Create a simple XML element containing only text.
   *
   * @param factory The element factory to be used.
   * @param name The name of the element to be created.
   * @param value The value to be put in a text or CDATA node under the element node.
   *              CDATA will be used only when <code>value.toString()</code> contains &amp;, --, &gt; or &lt;.
   *
   * @return The created element.
   *
   * @throws IllegalArgumentException When any argument is null or when name has zero length.
   * @throws DOMException When name is invalid.
   */
  public static Element createSimpleElement(Document factory, String name, Object value) throws IllegalArgumentException, DOMException {
    Contract.isNotNull("factory", factory);
    Contract.isNotNull("name", name);
    Contract.isInRange("name", name.length(), 1, Integer.MAX_VALUE);
    Contract.isNotNull("value", value);
    Element element = factory.createElement(name);
    String strVal = value.toString();
    Node textEl;
    if (strVal.contains("&") || strVal.contains("--") || strVal.contains(">") || strVal.contains("<")) {
      textEl = factory.createCDATASection(strVal);
    }
    else {
      textEl = factory.createTextNode(strVal);
    }
    element.appendChild(textEl);
    return element;
  }

  /**
   * Retrieve all child elements of the specified parent element.
   *
   * @param parentElement The element whose child elements should be searched.
   *
   * @return A potentially empty array containing all the child elements.
   *
   * @throws IllegalArgumentException When any argument is null.
   */
  public static Element[] getChildElements(Element parentElement) throws IllegalArgumentException {
    Contract.isNotNull("parentElement", parentElement);
    ArrayList<Element> elements = new ArrayList<>();
    NodeList children = parentElement.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node node = children.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        elements.add((Element)node);
      }
    }
    return elements.toArray(new Element[0]);
  }

  /**
   * Retrieve all child elements of the specified parent element that have the specified tag name.
   *
   * @param parentElement The element whose child elements should be searched.
   * @param tagName The tag name to search for.
   *
   * @return A potentially empty array containing all the child elements with the specified tag name.
   *
   * @throws IllegalArgumentException When any argument is null.
   */
  public static Element[] getChildElementsByTagName(Element parentElement, String tagName) throws IllegalArgumentException {
    Contract.isNotNull("parentElement", parentElement);
    Contract.isNotNull("tagName", tagName);
    ArrayList<Element> elements = new ArrayList<>();
    NodeList children = parentElement.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node node = children.item(i);
      if ((node.getNodeType() == Node.ELEMENT_NODE) && (tagName.equals(node.getNodeName()))) {
        elements.add((Element)node);
      }
    }
    return elements.toArray(new Element[0]);
  }

  /**
   * Returns all text found directly under the specified XML node. The returned String is the concatenation of the contents of all
   * text child nodes of the specified element node.
   *
   * @param element The element to retrieve the text for.
   *
   * @return The text found under the specified element node or, if no text can be found, the empty string.
   */
  public static String getText(Element element) {
    Contract.isNotNull("element", element);
    String text = "";
    NodeList children = element.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      switch (children.item(i).getNodeType()) {
        case Node.TEXT_NODE:
        case Node.CDATA_SECTION_NODE:
          Text textNode = (Text) children.item(i);
          text += textNode.getNodeValue();
      }
    }
    return text;
  }

  /**
   * Remove all child elements with the provided tag name.
   *
   * @param parentElement The XML element whose children should be inspected and optionally removed.
   * @param childTagName The tag name to search for.
   */
  public static void removeChildElementsWithTagName(Element parentElement, String childTagName) {
    for (Element el : getChildElementsByTagName(parentElement, childTagName)) {
      parentElement.removeChild(el);
    }
  }

  /**
   * Return either the provided element if it has the requested tagName or a unique direct child with that name.
   *
   * @param parentElement The XML element whose children should be inspected and optionally removed.
   * @param tagName The tag name to search for.
   *
   * @return The provided parentElement, a direct child or <code>null</code> if neither matched the tag name.
   *         <code>null</code> will also be returned if more than one child existed with the given tag name.
   */
  public static Element getParentOrChildWithTagName(Element parentElement, String tagName) {
    Contract.isNotNull("parentElement", parentElement);
    Contract.isNotNull("tagName", tagName);
    String parentTag = parentElement.getTagName();
    if (!parentTag.equals(tagName)) { // Find true root, must be a direct child
      parentElement = getUniqueOptionalChildElementWithTagName(parentElement, tagName);
    }
    return parentElement;
  }

  /**
   * Return the unique child element that has the given tag name.
   *
   * @param parentElement The XML element whose children should be inspected.
   * @param tagName The tag name to search for.
   *
   * @return The unique child element with the given tag name.
   * @throws FormatException If either zero or more than 1 child had the given tag name.
   */
  public static Element getUniqueChildElementWithTagName(Element parentElement, String tagName) {
    Contract.isNotNull("parentElement", parentElement);
    Contract.isNotNull("tagName", tagName);
    Element[] childElements = getChildElementsByTagName(parentElement, tagName);
    if (childElements.length != 1) {
      throw new FormatException(parentElement, "Found " + childElements.length + " <" + tagName + "> child elements of parent element <" + parentElement.getTagName() + "> while exactly 1 is expected");
    }
    return childElements[0];
  }

  /**
   * Return the unique child element that has the given tag name.
   *
   * @param parentElement The XML element whose children should be inspected.
   * @param tagName The tag name to search for.
   *
   * @return The unique child element with the given tag name or <code>null</code> if no child had the given tag name.
   * @throws FormatException If more than 1 child had the given tag name.
   */
  public static Element getUniqueOptionalChildElementWithTagName(Element parentElement, String tagName) {
    Contract.isNotNull("parentElement", parentElement);
    Contract.isNotNull("tagName", tagName);
    Element[] childElements = getChildElementsByTagName(parentElement, tagName);
    if (childElements.length > 1) {
      throw new FormatException(parentElement, "Found " + childElements.length + " <" + tagName + "> child elements of parent element <" + parentElement.getTagName() + "> while max. 1 was expected");
    }
    if (childElements.length == 1) {
      return childElements[0];
    }
    return null;
  }

  /**
   *  Return a string that represents the XML element and it's children as a valid XML document rooted at this element.
   *
   *  @param xmlElement The XML element at the top of an XML sub-tree to be turned into an XML document representation.
   *
   *  @return A multi-line, indented string representation of the XML rooted at xmlElement, without XML declaration.
   *
   *  @throws RuntimeException If determining the string fails for some reason.
   */
  public static String toXmlString(Element xmlElement) {
    return toXmlString(xmlElement, true, true);
  }

  /**
   *  Return a string that represents the XML element and it's children as a valid XML document rooted at this element.
   *
   *  @param xmlElement The XML element at the top of an XML sub-tree to be turned into an XML document representation.
   *  @param omitXmlDeclaration Whether an xml declaration line should be added at the top of the returned string.
   *
   *  @return A multi-line, indented string representation of the XML rooted at xmlElement.
   *
   *  @throws RuntimeException If determining the string fails for some reason.
   */
  public static String toXmlString(Element xmlElement, boolean omitXmlDeclaration) {
    return toXmlString(xmlElement, omitXmlDeclaration, true);
  }

  /**
   *  Return a string that represents the XML element and it's children as a valid XML document rooted at this element.
   *
   *  @param xmlElement The XML element at the top of an XML sub-tree to be turned into an XML document representation.
   *  @param omitXmlDeclaration Whether an xml declaration line should be added at the top of the returned string.
   *  @param indent Whether the returned XML should be indented.
   *
   *  @return A multi-line, indented string representation of the XML rooted at xmlElement.
   *
   *  @throws RuntimeException If determining the string fails for some reason.
   */
  public static String toXmlString(Element xmlElement, boolean omitXmlDeclaration, boolean indent) {
    Contract.isNotNull("xmlElement", xmlElement);
    try {
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      if (indent) {
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      }
      else {
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "0");
      }
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXmlDeclaration ? "yes" : "no");
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      StringWriter writer = new StringWriter();
      StreamResult result = new StreamResult(writer);
      DOMSource source = new DOMSource(xmlElement);
      transformer.transform(source, result);
      String xmlString = result.getWriter().toString();
      return xmlString;
    } catch (Exception e) {
      throw new RuntimeException("Failed to transform XML element to string", e);
    }
  }
}
