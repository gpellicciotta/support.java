// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.config;

import com.pellicciotta.support.codecs.StringCodec;
import com.pellicciotta.support.codecs.TypedValue;

/**
 *  A {@link Setting} that holds and manages an external {@link TypedValue}.
 *
 *  @param <T> The type of native value represented by this setting.
 */
public class AttachedSetting<T> extends Setting<T>
{
  /**
   *  Create an attached setting with the given properties and no default value.
   *
   *  @param type The value type.
   *  @param name The simple name of the setting.
   *  @param description A human-readable description of the setting.
   *  @param val The attached value.
   *  @param strCodec The codec to use to encode/decode from native type to/from <code>String</code>.
   */
  public AttachedSetting(Class<T> type, String name, String description, TypedValue<T> val, StringCodec<T> strCodec) {
    this(type, name, description, null, val, strCodec);
  }

  /**
   *  Create an attached setting with the given properties and no default value.
   *
   *  @param type The value type.
   *  @param name The simple name of the setting.
   *  @param description A human-readable description of the setting.
   *  @param defaultValue  The setting's default value. May be <code>null</code> to indicated that it doesn't have a default value.
   *  @param val The attached value.
   *  @param strCodec The codec to use to encode/decode from native type to/from <code>String</code>.
   */
  public AttachedSetting(Class<T> type, String name, String description, T defaultValue, TypedValue<T> val, StringCodec<T> strCodec) {
    super(type, name, description, defaultValue);
    this.strCodec = strCodec;
    this.val = val;
    this.val.set(defaultValue);
  }

  private TypedValue<T> val;
  private StringCodec<T> strCodec;

  /** {@inheritDoc} */
  @Override
  public T getValue() {
    return this.val.get();
  }

  /** {@inheritDoc} */
  @Override
  public void setValue(T newValue) {
    T oldValue = this.val.get();
    this.val.set(newValue);
    if (this.config != null) {
      this.config.fireValueChanged(this, oldValue, newValue);
    }
  }

  /** {@inheritDoc} */
  @Override
  public T fromString(String str) {
    return this.strCodec.decode(str);
  }

  /** {@inheritDoc} */
  @Override
  public String toString(T val) {
    return this.strCodec.encode(val);
  }
}
