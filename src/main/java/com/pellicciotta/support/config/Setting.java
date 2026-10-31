// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.config;

import java.util.Map;
import java.util.Objects;

import com.pellicciotta.support.text.TextBuilder;
import com.pellicciotta.support.utils.XmlUtils;
import org.w3c.dom.Element;

/**
 *  A setting is a name for an <b>elementary</b>, configurable, typed value.
 *  <p>
 *  This type provides all information and operations needed to work with typed settings.</p>
 *  <p>
 *  It is important to understand that this type should provide all the following:</p><ol>
 *    <li>all descriptive information about the setting, as it should be used in usage texts</li>
 *    <li>operations to retrieve and set a current value associated with the setting</li>
 *    <li>operations to encode and decode this setting's current value to/from different storage types: XML storage is always minimally available</li>
 *    <li>operations to validate and cast arbitrary values</li></ol>
 *  <p>
 *  Design notes:</p><ul>
 *    <li>The value associated with a setting (its {@link #getValue}) is not necessarily stored within the {@link Setting<T>} instance itself.</li>.
 *  </ul>
 *
 *  @param <T> The native type of the setting's value.
 */
public abstract class Setting<T> extends ConfigElement {
  // <editor-fold desc="Construction">

  /**
   *  Create a setting without default value.
   *
   *  @param type The value type.
   *  @param name The simple name of the setting.
   *  @param description A human-readable description of the setting.
   */
  public Setting(Class<T> type, String name, String description) {
    this(type, name, description, null);
  }

  /**
   *  Create a setting with default value.
   *
   *  @param type The value type.
   *  @param name The simple name of the setting.
   *  @param description A human-readable description of the setting.
   *  @param defaultValue Default value. May be <code>null</code>, indicating that there is no default value.
   */
  public Setting(Class<T> type, String name, String description, T defaultValue) {
    super(name, description);
    this.type = type;
    this.defaultValue = defaultValue;
  }

  // </editor-fold>
  // <editor-fold desc="ConfigElement Overrides">

  @Override
  protected boolean isElementary() {
    return true;
  }

  // </editor-fold>
  // <editor-fold desc="Value Operations">

  private final Class<T> type;

  /**
   * @return The native type to which configuration elements can be cast to/from.
   */
  public Class<?> getType() {
    return this.type;
  }

  protected T defaultValue;

  /**
   * @return Whether this setting has a non-<code>null</code> default value.
   */
  public boolean hasDefaultValue() {
    return (this.defaultValue != null);
  }

  /**
   * @return This setting's default value, which can be <code>null</code>.
   */
  public T getDefaultValue() {
    return this.defaultValue;
  }

  /**
   * @return Whether this setting currently has an explicitly set (i.e. non-default) value.
   */
  public boolean isValueSet() {
    T val = this.getValue();
    return (val != null);
  }

  /**
   * @return Get the current value, or the default value if no value was explicitly set. Can return <code>null</code> if there is no default value.
   */
  public T getValueOrDefault() {
    return this.isValueSet() ? this.getValue() : this.defaultValue;
  }

  /**
   * @return Get the current value, or <code>null</code> if no value was explicitly set.
   */
  public abstract T getValue();

  /**
   * Change the current value.
   *
   * @param newValue The new value to be made the current value. If <code>null</code>, the default value will be restored.
   */
  public abstract void setValue(T newValue);

  /**
   * Reset the setting's value to the default value. This is a shortcut for {@code setValue(null)}.
   */
  public void resetValue() {
    this.setValue(null);
  }

  /**
   *  Turn a string representation of a value into the actual value.
   *
   *  @param str String representation. Must not be <code>null</code>.
   *
   *  @return Value of this setting's type.
   */
  public abstract T fromString(String str);

  /**
   *  Turn a typed value into a string representation.
   *
   *  @param val A value. Must not be <code>null</code>.
   *
   *  @return The string representation of this value.
   */
  public abstract String toString(T val);

  // </editor-fold>
  // <editor-fold desc="Persistence Operations">

  protected void reset() {
    this.resetValue();
  }

  @Override
  protected void loadFrom(Element xmlParent) {
    for (Element el : XmlUtils.getChildElementsByTagName(xmlParent, this.getName())) {
      String strVal = XmlUtils.getText(el);
      T val = this.fromString(strVal);
      this.setValue(val);
    }
  }

  @Override
  protected void saveTo(Element xmlParent, boolean saveDefaultValues) {
    if (this.isValueSet() || saveDefaultValues) {
      XmlUtils.removeChildElementsWithTagName(xmlParent, this.getName());
      T val = this.getValue();
      if (val != null) {
        String valStr = this.toString(val);
        Element valEl = XmlUtils.createSimpleElement(xmlParent.getOwnerDocument(), this.getName(), valStr);
        xmlParent.appendChild(valEl);
      }
    }
  }

  @Override
  protected void loadFrom(Map<String, String> props) {
    String cn = this.getFullName();
    if (props.containsKey(cn)) {
      T val = this.fromString(props.get(cn));
      this.setValue(val);
    }
  }

  @Override
  protected void saveTo(Map<String, String> props, boolean saveDefaultValues) {
    String cn = this.getFullName();
    props.remove(cn);
    if (this.isValueSet() || saveDefaultValues) {
      T val = this.getValue();
      if (val != null) {
        String valStr = this.toString(val);
        props.put(cn, valStr);
      }
    }
  }

  // </editor-fold>
  // <editor-fold desc="Debugging Aids">

  public void addTo(TextBuilder tb) {
    tb.append("'%s' setting of type '%s' with value '%s' (%s)", this.getName(), this.getType().getSimpleName(), this.getValueOrDefault(), this.isValueSet() ? "non-default" : ((this.defaultValue != null) ? "default" : "unset"));
  }

  // </editor-fold>
}
