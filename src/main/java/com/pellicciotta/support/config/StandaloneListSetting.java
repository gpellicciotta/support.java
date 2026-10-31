// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.config;

import java.util.ArrayList;
import java.util.List;

import com.pellicciotta.support.codecs.PropertyCodec;
import com.pellicciotta.support.codecs.StringCodec;
import com.pellicciotta.support.codecs.XmlCodec;

/**
 *  A {@link ListSetting} that holds and manages its own value.
 *
 *  @param <T> The type of elementary native values held by this setting.
 */
public class StandaloneListSetting<T> extends ListSetting<T> {
  /**
   *  Create a standalone list setting with the given properties and no default value.
   *
   *  @param elementType The element value type.
   *  @param name The simple name of the setting.
   *  @param description A human-readable description of the setting.
   *  @param defaultValue  The setting's default value. May be <code>null</code> to indicate that it doesn't have a default value.
   *  @param strCodec The codec to use to encode/decode from native element type to/from <code>String</code>.
   */
  public StandaloneListSetting(Class<T> elementType, String name, String description, List<T> defaultValue, StringCodec<T> strCodec) {
    super(elementType, name, description, defaultValue, strCodec);
    this.value = null;
  }

  /**
   *  Create a standalone setting with the given properties.
   *
   *  @param elementType The element value type.
   *  @param name The simple name of the setting.
   *  @param description A human-readable description of the setting.
   *  @param defaultValue  The setting's default value. May be <code>null</code> to indicate that it doesn't have a default value.
   *  @param propertyCodec Property codec. Should assumed all properties starting with (or being equal to) this setting's simple name.
   *  @param xmlCodec XML codec. Should assume XML elements named after this setting's simple name.
   */
  public StandaloneListSetting(Class<T> elementType, String name, String description, List<T> defaultValue, PropertyCodec<T> propertyCodec, XmlCodec<T> xmlCodec) {
    super(elementType, name, description, defaultValue, propertyCodec, xmlCodec);
    this.value = null;
  }

  private List<T> value;

  /**
   *  This setting's current, explicitly set, value.
   *
   *  @return The current value, which can be <code>null</code> if no current value is set.
   */
  @Override
  public List<T> getValue() {
    return this.value;
  }

  /**
   *  Set this setting's current value.
   *
   *  @param newValue The new value to set. If <code>null</code>, the setting will not have an explicitly set value any more.
   */
  @Override
  public void setValue(List<? extends T> newValue) {
    List<T> oldValue = this.getValue();
    List<T> curValue = oldValue;
    if (oldValue != null) {
      oldValue = new ArrayList<>(oldValue); // Make a copy, since we're going to re-use the original list (since it likely is a final field)
      curValue.clear();
      if (newValue != null) {
        curValue.addAll(newValue);
      }
    }
    else if (newValue != null) {
      curValue = new ArrayList<>(newValue);
      this.value = curValue;
    }
    if (this.config != null) {
      this.config.fireValueChanged(this, oldValue, newValue);
    }
  }

  @Override
  public void addElementValue(T newValue) {
    if (this.value == null) {
      this.value = new ArrayList<T>();
    }
    this.value.add(newValue);
  }

  @Override
  public void clearElementValues() {
    if (this.value != null) {
      this.value.clear();
    }
  }
}
