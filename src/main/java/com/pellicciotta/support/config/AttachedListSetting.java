// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.config;

import com.pellicciotta.support.codecs.PropertyCodec;
import com.pellicciotta.support.codecs.StringCodec;
import com.pellicciotta.support.codecs.TypedValue;
import com.pellicciotta.support.codecs.XmlCodec;

import java.util.ArrayList;
import java.util.List;

/**
 *  A {@link ListSetting} that holds and manages an external {@link TypedValue}.
 *
 *  @param <T> The type of native value represented by this setting.
 */
public class AttachedListSetting<T> extends ListSetting<T>
{
  /**
   *  Create an attached setting with the given properties and no default value.
   *
   *  @param elementType The element value type.
   *  @param name The simple name of the setting.
   *  @param description A human-readable description of the setting.
   *  @param defaultValue  The setting's default value. May be <code>null</code> to indicated that it doesn't have a default value.
   *  @param val The attached value.
   *  @param strCodec The codec to use to encode/decode from native element type to/from <code>String</code>.
   */
  public AttachedListSetting(Class<T> elementType, String name, String description, List<T> defaultValue, TypedValue<List<T>> val, StringCodec<T> strCodec) {
    super(elementType, name, description, defaultValue, strCodec);
    this.val = val;
  }

  /**
   *  Create an attached setting with the given properties and no default value.
   *
   *  @param elementType The element value type.
   *  @param name The simple name of the setting.
   *  @param description A human-readable description of the setting.
   *  @param defaultValue  The setting's default value. May be <code>null</code> to indicated that it doesn't have a default value.
   *  @param val The attached value.
   *  @param propertyCodec Property codec. Should assumed all properties starting with (or being equal to) this setting's simple name.
   *  @param xmlCodec XML codec. Should assume XML elements named after this setting's simple name.
   */
  public AttachedListSetting(Class<T> elementType, String name, String description, List<T> defaultValue, TypedValue<List<T>> val, PropertyCodec<T> propertyCodec, XmlCodec<T> xmlCodec) {
    super(elementType, name, description, defaultValue, propertyCodec, xmlCodec);
    this.val = val;
    this.val.set(defaultValue);
  }

  private TypedValue<List<T>> val;

  /** {@inheritDoc} */
  @Override
  public List<T> getValue() {
    return this.val.get();
  }

  /** {@inheritDoc} */
  @Override
  public void setValue(List<? extends T> newValue) {
    List<T> oldValue = this.val.get();
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
      this.val.set(curValue);
    }
    if (this.config != null) {
      this.config.fireValueChanged(this, oldValue, newValue);
    }
  }

  @Override
  public void addElementValue(T newValue) {
    List<T> valList = this.val.get();
    if (valList == null) {
      valList = new ArrayList<T>();
      this.val.set(valList);
    }
    valList.add(newValue);
  }

  @Override
  public void clearElementValues() {
    List<T> valList = this.val.get();
    if (valList != null) {
      valList.clear();
    }
  }

  @Override
  public List<T> fromString(String str) {
    throw new UnsupportedOperationException("not implemented"); // TODO: implement in terms of JSON or XML
  }

  @Override
  public String toString(List<? extends T> val) {
    throw new UnsupportedOperationException("not implemented"); // TODO: implement in terms of JSON or XML
  }
}
