// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.config;

import com.pellicciotta.support.codecs.StringCodec;

/**
 *  A {@link Setting} that holds and manages its own value.
 *
 *  @param <T> The type of native value held by this setting.
 */
public class StandaloneSetting<T> extends Setting<T> {
  /**
   *  Create a standalone setting with the given properties and no default value.
   *
   *  @param type The value type.
   *  @param name The simple name of the setting.
   *  @param description A human-readable description of the setting.
   *  @param strCodec The codec to use to encode/decode from native type to/from <code>String</code>.
   */
  public StandaloneSetting(Class<T> type, String name, String description, StringCodec<T> strCodec) {
    this(type, name, description, null, strCodec);
  }

  /**
   *  Create a standalone setting with the given properties.
   *
   *  @param type The value type.
   *  @param name The simple name of the setting.
   *  @param description A human-readable description of the setting.
   *  @param defaultValue  The setting's default value. May be <code>null</code> to indicate that it doesn't have a default value.
   *  @param strCodec The codec to use to encode/decode from native type to/from <code>String</code>.
   */
  public StandaloneSetting(Class<T> type, String name, String description, T defaultValue, StringCodec<T> strCodec) {
    super(type, name, description, defaultValue);
    this.strCodec = strCodec;
    this.value = null;
  }

  private T value;
  private StringCodec<T> strCodec;

  /**
   *  This setting's current, explicitly set, value.
   *
   *  @return The current value, which can be <code>null</code> if no current value is set.
   */
  @Override
  public T getValue() {
    return this.value;
  }

  /**
   *  Set this setting's current value.
   *
   *  @param newValue The new value to set. If <code>null</code>, the setting will not have an explicitly set value any more.
   */
  @Override
  public void setValue(T newValue) {
    T oldValue = this.getValue();
    this.value = newValue;
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
