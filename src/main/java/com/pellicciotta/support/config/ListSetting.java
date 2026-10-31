// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.config;

import javax.naming.OperationNotSupportedException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pellicciotta.support.codecs.*;
import com.pellicciotta.support.text.TextBuilder;
import com.pellicciotta.support.utils.*;
import org.w3c.dom.Element;

/**
 *  A list setting is a name for a list of <b>elementary</b> values of the same type.
 *  <p>
 *  This type provides all information and operations needed to work with typed list settings.</p>
 *  <p>
 *  It is important to understand that this type should provide all the following:</p><ol>
 *    <li>all descriptive information about the setting, as it should be used in usage texts</li>
 *    <li>operations to retrieve and set a current list of values associated with the setting</li>
 *    <li>operations to encode and decode this setting's current value to/from different storage types: XML storage is always minimally available</li>
 *    <li>operations to validate and cast arbitrary values</li></ol>
 *  <p>
 *  Design notes:</p><ul>
 *    <li>The list of values associated with a setting (its {@link #getValue}) is not necessarily stored within the {@link ListSetting <T>} instance itself.</li>.
 *  </ul>
 *
 *  @param <T> The native type of the setting's value.
 */
public abstract class ListSetting<T> extends Setting<List<? extends T>> {
  // <editor-fold desc="Construction">

  /**
   *  Create a setting with default value.
   *
   *  @param elementType The element value type.
   *  @param name The simple name of the setting.
   *  @param description A human-readable description of the setting.
   *  @param defaultValue Default value. May be <code>null</code>, indicating that there is no default value.
   *  @param stringCodec The string codec to create XML and property codecs from.
   */
  @SuppressWarnings("unchecked")
  public ListSetting(Class<T> elementType, String name, String description, List<? extends T> defaultValue, StringCodec<T> stringCodec) {
    super((Class<List<? extends T>>) defaultValue.getClass(), name, description);
    this.elementType = elementType;
    this.defaultValue = defaultValue;
    this.propsCodec = new StringCodecBasedPropertyCodec<>("", stringCodec);
    this.xmlCodec = new StringCodecBasedXmlCodec<>(name, stringCodec);
    this.stringCodec = stringCodec;
  }

  /**
   *  Create a setting with default value.
   *
   *  @param elementType The element value type.
   *  @param name The simple name of the setting.
   *  @param description A human-readable description of the setting.
   *  @param defaultValue Default value. May be <code>null</code>, indicating that there is no default value.
   *  @param propertyCodec Property codec. Should assume all properties starting with (or being equal to) this setting's simple name.
   *  @param xmlCodec XML codec. Should assume XML elements named after this setting's simple name.
   */
  @SuppressWarnings("unchecked")
  public ListSetting(Class<T> elementType, String name, String description, List<? extends T> defaultValue, PropertyCodec<T> propertyCodec, XmlCodec<T> xmlCodec) {
    super((Class<List<? extends T>>) defaultValue.getClass(), name, description);
    this.elementType = elementType;
    this.defaultValue = defaultValue;
    this.propsCodec = propertyCodec;
    this.xmlCodec = xmlCodec;
    this.stringCodec = null;
  }

  // </editor-fold>
  // <editor-fold desc="ConfigElement Overrides">

  @Override
  protected boolean isElementary() {
    return false;
  }

  // </editor-fold>
  // <editor-fold desc="Value Operations">

  private final Class<T> elementType;

  /**
   * @return The native type to which configuration elements can be cast to/from.
   */
  public Class<?> getElementType() {
    return this.elementType;
  }

  /**
   * Add a new value to the end of the current list of values.
   *
   * @param newValue The new elementary value to be added.
   */
  public abstract void addElementValue(T newValue);

  /**
   *  Make the list of elementary values empty.
   */
  public abstract void clearElementValues();

  // </editor-fold>
  // <editor-fold desc="Persistence Operations">

  /** Codec used to convert single elements to and from strings. */
  protected final StringCodec<T> stringCodec;
  /** Codec used to convert single elements to and from XML. */
  protected final XmlCodec<T> xmlCodec;
  /** Codec used to convert single elements to and from property values. */
  protected final PropertyCodec<T> propsCodec;

  /** Resets the list to its default value. */
  protected void reset() {
    this.resetValue();
  }

  @Override
  public List<T> fromString(String str) {
    // TODO: better implementation
    if (str.trim().equals("[]")) {
      return Collections.emptyList();
    }
    if (str.startsWith("[") && str.endsWith("]")) {
      str = str.substring(1, str.length() - 1);
    }
    String[] strVals = str.split(" *[,] *");
    List<T> vals = new ArrayList<>();
    for (String strVal : strVals) {
      vals.add(this.stringCodec.decode(strVal));
    }
    return vals;
  }

  @Override
  public String toString(List<? extends T> vals) {
    if ((vals == null) || vals.isEmpty()) {
      return "[]";
    }
    else {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      for (int i = 0; i < vals.size() - 1; i++) {
        sb.append(this.toElementString(vals.get(i)));
        sb.append(", ");
      }
      sb.append(this.toElementString(vals.get(vals.size() - 1)));
      sb.append("]");
      return sb.toString();
    }
  }

  /**
   *  Get a string-representation of a single element.
   *
   *  @param val The element to get a string representation for.
   *
   *  @return The string representation of the element.
   */
  public String toElementString(T val) {
    if (this.stringCodec != null) {
      return this.stringCodec.encode(val);
    }
    Map<String, String> props = new TreeMap<>();
    this.propsCodec.encode(val, props);
    StringBuilder sb = new StringBuilder();
    sb.append("{ ");
    boolean first = true;
    for (Map.Entry<String, String> p : props.entrySet()) {
      if (first) {
        first = false;
      }
      else {
        sb.append(", ");
      }
      sb.append("\"");
      sb.append(p.getKey().replace("\"", "\\\""));
      sb.append("\": \"");
      sb.append(p.getValue().replace("\"", "\\\""));
      sb.append("\"");
    }
    sb.append(" }");
    return sb.toString();
  }

  @Override
  protected void loadFrom(Element xmlParent) {
    List<T> newVals = new ArrayList<>();
    for (Element el : XmlUtils.getChildElementsByTagName(xmlParent, this.getName())) {
      T val = this.xmlCodec.decode(el);
      newVals.add(val);
    }
    if (!newVals.isEmpty()) {
      this.setValue(newVals);
    }
  }

  @Override
  protected void saveTo(Element xmlParent, boolean saveDefaultValues) {
    if (this.isValueSet()) {
      // Remove any existing
      XmlUtils.removeChildElementsWithTagName(xmlParent, this.getName());
      List<? extends T> currVals = saveDefaultValues ? this.getValueOrDefault() : this.getValue();
      if (currVals != null) {
        for (T val : currVals) {
          this.xmlCodec.encode(val, xmlParent);
        }
      }
    }
  }

  @Override
  protected void loadFrom(Map<String, String> props) {
    if (props.containsKey(this.getFullName())) {
      String listVal = props.get(this.getFullName());
      List<T> vals = this.fromString(listVal);
      this.setValue(vals);
      return ;
    }
    Pattern numPattern = Pattern.compile("^[0-9]+$");
    Pattern propPattern = Pattern.compile(Pattern.quote(this.getFullName()) + "[.]([^.]+)(?:[.](.+))?");
    Map<String, Map<String, String>> listElementProps = new TreeMap<>(); // TreeMap will do the sorting for us
    // Find all properties pertaining to this list setting while also grouping them per list element
    for (Map.Entry<String, String> nv : props.entrySet()) {
      String fullPropName = nv.getKey();
      String propValue = nv.getValue();
      Matcher m = propPattern.matcher(fullPropName);
      if (m.matches()) {
        String elemName = m.group(1);
        if ((elemName.length() < 20) && numPattern.matcher(elemName).matches()) { // If pure digits, extend left with zeroes to 20
          elemName = StringUtils.padLeft(elemName, 20, '0');    // so that textual compare will be the same as a numeric compare
        }
        listElementProps.putIfAbsent(elemName, new HashMap<>());
        String elemPropName = m.group(2);
        if ((elemPropName == null) || (elemPropName.isEmpty())) {
          elemPropName = "";
        }
        Map<String, String> itemProps = listElementProps.get(elemName);
        itemProps.put(elemPropName, propValue);
      }
    }
    List<T> newVals = new ArrayList<>();
    for (Map<String, String> elementProps : listElementProps.values()) { // In sorted order:
      T val = this.propsCodec.decode(elementProps);
      newVals.add(val);
    }
    if (!newVals.isEmpty()) {
      this.setValue(newVals);
    }
  }

  /**
   *  Get a properties-representation of a single element.
   *
   *  @param val The element to get a string representation for.
   *
   *  @return The map/properties representation of the element.
   *          The keys will not contain the full element name.
   *          If there is a single property, they key might be empty.
   */
  public Map<String, String> toElementProps(T val) {
    Map<String, String> valProps = new HashMap<>();
    this.propsCodec.encode(val, valProps);
    return valProps;
  }

  @Override
  protected void saveTo(Map<String, String> props, boolean saveDefaultValues) {
    final String cn = this.getFullName();
    PropertiesUtils.removeMatchingKeys(props, (key) -> key.equals(cn) || key.startsWith(cn + "."));
    List<? extends T> currVals = saveDefaultValues ? this.getValueOrDefault() : this.getValue();
    if (currVals != null) {
      int i = 1;
      for (T val : currVals) {
        Map<String, String> valProps = new HashMap<>();
        this.propsCodec.encode(val, valProps);
        for (Map.Entry<String, String> propEntry : valProps.entrySet()) {
          String fullKey = propEntry.getKey();
          if ((fullKey == null) || fullKey.isEmpty()) {
            fullKey = cn + "." + i;
          }
          else {
            fullKey = cn + "." + i + "." + fullKey;
          }
          props.put(fullKey, propEntry.getValue());
        }
        i += 1;
      }
    }
  }

  // </editor-fold>
  // <editor-fold desc="Debugging Aids">

  /**
   * Appends a human-readable description of this list setting, including its element type and values, to the given builder.
   *
   * @param tb The builder to append to.
   */
  public void addTo(TextBuilder tb) {
    tb.append("'%s' list setting with element type '%s' and values: %s (%s)", this.getName(), this.getElementType().getSimpleName(), this.toString(this.getValueOrDefault()), this.isValueSet() ? "non-default" : ((this.defaultValue != null) ? "default" : "unset"));
  }

  // </editor-fold>
}
