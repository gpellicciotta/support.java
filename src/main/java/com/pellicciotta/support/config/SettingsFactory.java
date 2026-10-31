// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import com.pellicciotta.support.annotations.ConfigGroup;
import com.pellicciotta.support.annotations.ConfigSetting;
import com.pellicciotta.support.codecs.*;
import com.pellicciotta.support.logging.LogService;
import com.pellicciotta.support.utils.*;

/**
 * A factory for {@link Setting}s, {@link SettingsGroup}s and {@link Config}s.
 * <p>
 * All <code>standaloneXxx</code> methods return a {@link Setting} that encapsulates its own value,
 * while all <code>attachedXxx</code> methods return a {@link Setting} that works with an existing pair of get/set functions.</p>
 */
public class SettingsFactory {
  // <editor-fold desc="Support Functions" >

  private static String getName(String name, Field targetField) {
    if (StringUtils.isNullOrEmpty(name)) {
      name = targetField.getName().replaceAll("([a-z0-9])([A-Z])", "$1_$2").replace('_', '-').toLowerCase();
    }
    return name;
  }

  private static Class<?> getReflectionBasedType(Class<?> type) {
    if (type == byte.class) {
      type = Byte.class;
    }
    else if (type == char.class) {
      type = Character.class;
    }
    else if (type == boolean.class) {
      type = Boolean.class;
    }
    else if (type == short.class) {
      type = Short.class;
    }
    else if (type == int.class) {
      type = Integer.class;
    }
    else if (type == long.class) {
      type = Long.class;
    }
    else if (type == float.class) {
      type = Float.class;
    }
    else if (type == double.class) {
      type = Double.class;
    }
    return type;
  }

  @SuppressWarnings("unchecked")
  private static <T> StringCodec<T> getStringCodec(Class<T> type) {
    if ((type == Boolean.class) || (type == boolean.class)) {
      return (StringCodec<T>)StandardStringCodecs.BOOLEAN;
    }
    else if ((type == Integer.class) || (type == int.class)) {
      return (StringCodec<T>)StandardStringCodecs.INTEGER;
    }
    else if ((type == Long.class) || (type == long.class)) {
      return (StringCodec<T>)StandardStringCodecs.LONG;
    }
    else if ((type == Double.class) || (type == double.class)) {
      return (StringCodec<T>)StandardStringCodecs.DOUBLE;
    }
    else if (type == BigDecimal.class) {
      return (StringCodec<T>)StandardStringCodecs.BIG_DECIMAL;
    }
    else if (type == Date.class) {
      return (StringCodec<T>)StandardStringCodecs.DATE_TIME;
    }
    else if (type == String.class) {
      return (StringCodec<T>)StandardStringCodecs.STRING;
    }
    else if (type == byte[].class) {
      return (StringCodec<T>)StandardStringCodecs.BYTE_ARRAY;
    }
    else if (type == Pattern.class) {
      return (StringCodec<T>)StandardStringCodecs.REGEX;
    }
    else if (type == Class.class) {
      return (StringCodec<T>)StandardStringCodecs.TYPE;
    }
    else if (type.isEnum()) {
      Class<? extends Enum> enumClass = (Class<? extends Enum>)type;
      final StringCodec<T> tStringCodec = (StringCodec<T>) enumStringCodec(enumClass);
      return tStringCodec;
    }
    throw new UnsupportedOperationException("No string-codec available for value type " + type);
  }

  private static StringCodec<Class<?>> classStringCodec(Class<?> baseType) {
    return new StringCodec<Class<?>>() {
      @Override
      public Class<?> decode(String from) {
        return StringConversionUtils.toClass(baseType, from);
      }

      @Override
      public String encode(Class<?> value) {
        return StringConversionUtils.fromClass(value);
      }
    };
  }

  private static <F extends Enum<F>> StringCodec<F> enumStringCodec(Class<F> enumClass) {
    return new StringCodec<F>() {
      @Override
      public F decode(String from) {
        return StringConversionUtils.toEnum(enumClass, from);
      }

      @Override
      public String encode(F value) {
        return StringConversionUtils.fromEnum(value);
      }
    };
  }

  private <T> Setting<T> fieldAnnotationBasedSetting(Class<T> valueType, Object targetObject, ConfigSetting fieldAnnotation, Field targetField) throws Exception {
    final String name = getName(fieldAnnotation.name(), targetField);
    final String description = fieldAnnotation.description();
    targetField.setAccessible(true);
    final T defaultValue = (T)targetField.get(targetObject);
    final TypedValue<T> fieldVal = new TypedValue<T>() {
      @Override
      public T get() {
        try {
          return (T)targetField.get(targetObject);
        }
        catch (IllegalAccessException e) {
          LogService.error("Failed to get value of field '%s': %s", targetField.getName(), e);
        }
        return null;
      }
      @Override
      public void set(T value) {
        try {
          targetField.set(targetObject, value);
        }
        catch (IllegalAccessException e) {
          LogService.error("Failed to set value of field '%s': %s", targetField.getName(), e);
        }
      }
    };

    StringCodec<T> strCodec = null;
    Class<?> strCodecClass = fieldAnnotation.stringCodec();
    if ((strCodecClass != null) && StringCodec.class.isAssignableFrom(strCodecClass)) {
      Constructor ctor = strCodecClass.getDeclaredConstructor(new Class<?>[0]);
      strCodec = (StringCodec<T>) ctor.newInstance();
      return new AttachedSetting<T>(valueType, name, description, defaultValue, fieldVal, strCodec);
    }
    else {
      strCodec = this.getStringCodec(valueType);
      return new AttachedSetting<T>(valueType, name, description, defaultValue, fieldVal, strCodec);
    }
  }

  private <T> ListSetting<T> fieldAnnotationBasedListSetting(Class<T> elemType, Object targetObject, ConfigSetting fieldAnnotation, Field targetField) throws Exception {
    final String name = getName(fieldAnnotation.name(), targetField);
    final String description = fieldAnnotation.description();
    targetField.setAccessible(true);
    final List<T> defaultValue = (List<T>)targetField.get(targetObject);
    final TypedValue<List<T>> fieldVal = new TypedValue<List<T>>() {
      @Override
      public List<T> get() {
        try {
          return (List<T>)targetField.get(targetObject);
        }
        catch (IllegalAccessException e) {
          LogService.error("Failed to get value of field '%s': %s", targetField.getName(), e);
        }
        return null;
      }
      @Override
      public void set(List<T> value) {
        try {
          targetField.set(targetObject, value);
        }
        catch (IllegalAccessException e) {
          LogService.error("Failed to set value of field '%s': %s", targetField.getName(), e);
        }
      }
    };
    StringCodec<T> strCodec = null;
    PropertyCodec<T> propCodec = null;
    XmlCodec<T> xmlCodec = null;
    Class<?> strCodecClass = fieldAnnotation.stringCodec();
    Class<?> propertyCodecClass = fieldAnnotation.propertyCodec();
    Class<?> xmlCodecClass = fieldAnnotation.xmlCodec();
    if ((strCodecClass != null) && StringCodec.class.isAssignableFrom(strCodecClass)) {
      Constructor ctor = strCodecClass.getDeclaredConstructor(new Class<?>[0]);
      strCodec = (StringCodec<T>) ctor.newInstance();
      return new AttachedListSetting<T>(elemType, name, description, defaultValue, fieldVal, strCodec);
    }
    else if (   (propertyCodecClass != null) && PropertyCodec.class.isAssignableFrom(propertyCodecClass)
             && (xmlCodecClass != null) && XmlCodec.class.isAssignableFrom(xmlCodecClass)) {
      Constructor ctor1 = propertyCodecClass.getDeclaredConstructor(new Class<?>[0]);
      propCodec = (PropertyCodec<T>) ctor1.newInstance();
      Constructor ctor2 = xmlCodecClass.getDeclaredConstructor(new Class<?>[0]);
      xmlCodec = (XmlCodec<T>) ctor2.newInstance();
      return new AttachedListSetting<T>(elemType, name, description, defaultValue, fieldVal, propCodec, xmlCodec);
    }
    else {
      strCodec = this.getStringCodec(elemType);
      return new AttachedListSetting<T>(elemType, name, description, defaultValue, fieldVal, strCodec);
    }
  }

  // </editor-fold>
  // <editor-fold desc="Factory Functions" />

  /** Creates a standalone boolean {@link Setting} that stores its own value, with no default value. */
  public Setting<Boolean> standaloneBooleanSetting(String name, String description) {
    return this.standaloneSetting(Boolean.class, name, description, StandardStringCodecs.BOOLEAN);
  }

  /** Creates a standalone boolean {@link Setting} that stores its own value, with the given default value. */
  public Setting<Boolean> standaloneBooleanSetting(String name, String description, boolean defaultValue) {
    return this.standaloneSetting(Boolean.class, name, description, defaultValue, StandardStringCodecs.BOOLEAN);
  }

  /** Creates a standalone boolean {@link Setting} that stores its own value, with a default and custom codec. */
  public Setting<Boolean> standaloneBooleanSetting(String name, String description, boolean defaultValue, StringCodec<Boolean> strCodec) {
    return this.standaloneSetting(Boolean.class, name, description, defaultValue, strCodec);
  }

  /** Creates a standalone integer {@link Setting} that stores its own value, with no default value. */
  public Setting<Integer> standaloneIntegerSetting(String name, String description) {
    return this.standaloneSetting(Integer.class, name, description, StandardStringCodecs.INTEGER);
  }

  /** Creates a standalone integer {@link Setting} that stores its own value, with the given default value. */
  public Setting<Integer> standaloneIntegerSetting(String name, String description, int defaultValue) {
    return this.standaloneSetting(Integer.class, name, description, defaultValue, StandardStringCodecs.INTEGER);
  }

  /** Creates a standalone integer {@link Setting} that stores its own value, with a default and custom codec. */
  public Setting<Integer> standaloneIntegerSetting(String name, String description, int defaultValue, StringCodec<Integer> strCodec) {
    return this.standaloneSetting(Integer.class, name, description, defaultValue, strCodec);
  }

  /** Creates a standalone log-level (integer) {@link Setting} that stores its own value, with no default value. */
  public Setting<Integer> standaloneLogLevelSetting(String name, String description) {
    return this.standaloneSetting(Integer.class, name, description, StandardStringCodecs.LOG_LEVEL);
  }

  /** Creates a standalone log-level (integer) {@link Setting} that stores its own value, with a default value. */
  public Setting<Integer> standaloneLogLevelSetting(String name, String description, int defaultValue) {
    return this.standaloneSetting(Integer.class, name, description, defaultValue, StandardStringCodecs.LOG_LEVEL);
  }

  /** Creates a standalone long {@link Setting} that stores its own value, with no default value. */
  public Setting<Long> standaloneLongSetting(String name, String description) {
    return this.standaloneSetting(Long.class, name, description, StandardStringCodecs.LONG);
  }

  /** Creates a standalone long {@link Setting} that stores its own value, with the given default value. */
  public Setting<Long> standaloneLongSetting(String name, String description, long defaultValue) {
    return this.standaloneSetting(Long.class, name, description, defaultValue, StandardStringCodecs.LONG);
  }

  /** Creates a standalone long {@link Setting} that stores its own value, with a default and custom codec. */
  public Setting<Long> standaloneLongSetting(String name, String description, long defaultValue, StringCodec<Long> strCodec) {
    return this.standaloneSetting(Long.class, name, description, defaultValue, strCodec);
  }

  /** Creates a standalone byte-size (long) {@link Setting} that stores its own value, with no default value. */
  public Setting<Long> standaloneByteSizeSetting(String name, String description) {
    return this.standaloneSetting(Long.class, name, description, StandardStringCodecs.BYTE_SIZE);
  }

  /** Creates a standalone byte-size (long) {@link Setting} that stores its own value, with the given default value. */
  public Setting<Long> standaloneByteSizeSetting(String name, String description, long defaultValue) {
    return this.standaloneSetting(Long.class, name, description, defaultValue, StandardStringCodecs.BYTE_SIZE);
  }

  /** Creates a standalone timespan (long) {@link Setting} that stores its own value, with no default value. */
  public Setting<Long> standaloneTimespanSetting(String name, String description) {
    return this.standaloneSetting(Long.class, name, description, StandardStringCodecs.TIMESPAN);
  }

  /** Creates a standalone timespan (long) {@link Setting} that stores its own value, with the given default value. */
  public Setting<Long> standaloneTimespanSetting(String name, String description, long defaultValue) {
    return this.standaloneSetting(Long.class, name, description, defaultValue, StandardStringCodecs.TIMESPAN);
  }

  /**
   * Creates a standalone timespan (long) {@link Setting} that stores its own value, with a default and custom
   * codec.
   */
  public Setting<Long> standaloneTimespanSetting(String name, String description, long defaultValue, StringCodec<Long> strCodec) {
    return this.standaloneSetting(Long.class, name, description, defaultValue, strCodec);
  }

  /** Creates a standalone string {@link Setting} that stores its own value, with no default value. */
  public Setting<String> standaloneStringSetting(String name, String description) {
    return this.standaloneSetting(String.class, name, description, StandardStringCodecs.STRING);
  }

  /** Creates a standalone string {@link Setting} that stores its own value, with the given default value. */
  public Setting<String> standaloneStringSetting(String name, String description, String defaultValue) {
    return this.standaloneSetting(String.class, name, description, defaultValue, StandardStringCodecs.STRING);
  }

  /** Creates a standalone string {@link Setting} that stores its own value, with a default and custom codec. */
  public Setting<String> standaloneStringSetting(String name, String description, String defaultValue, StringCodec<String> strCodec) {
    return this.standaloneSetting(String.class, name, description, defaultValue, strCodec);
  }

  /** Creates a standalone password (string) {@link Setting} that stores its own value, with no default value. */
  public Setting<String> standalonePasswordSetting(String name, String description) {
    return this.standaloneSetting(String.class, name, description, StandardStringCodecs.PASSWORD);
  }

  /** Creates a standalone password (string) {@link Setting} that stores its own value, with the given default value. */
  public Setting<String> standalonePasswordSetting(String name, String description, String defaultValue) {
    return this.standaloneSetting(String.class, name, description, defaultValue, StandardStringCodecs.PASSWORD);
  }

  /** Creates a standalone charset {@link Setting} that stores its own value, with no default value. */
  public Setting<Charset> standaloneCharsetSetting(String name, String description) {
    return this.standaloneSetting(Charset.class, name, description, StandardStringCodecs.CHARSET);
  }

  /** Creates a standalone charset {@link Setting} that stores its own value, with the given default value. */
  public Setting<Charset> standaloneCharsetSetting(String name, String description, Charset defaultValue) {
    return this.standaloneSetting(Charset.class, name, description, defaultValue, StandardStringCodecs.CHARSET);
  }

  /** Creates a standalone byte-array {@link Setting} that stores its own value, with no default value. */
  public Setting<byte[]> standaloneByteArraySetting(String name, String description) {
    return this.standaloneSetting(byte[].class, name, description, StandardStringCodecs.BYTE_ARRAY);
  }

  /** Creates a standalone byte-array {@link Setting} that stores its own value, with the given default value. */
  public Setting<byte[]> standaloneByteArraySetting(String name, String description, byte[] defaultValue) {
    return this.standaloneSetting(byte[].class, name, description, defaultValue, StandardStringCodecs.BYTE_ARRAY);
  }

  /** Creates a standalone byte-array {@link Setting} that stores its own value, with a default and custom codec. */
  public Setting<byte[]> standaloneByteArraySetting(String name, String description, byte[] defaultValue, StringCodec<byte[]> strCodec) {
    return this.standaloneSetting(byte[].class, name, description, defaultValue, strCodec);
  }

  /** Creates a standalone double {@link Setting} that stores its own value, with no default value. */
  public Setting<Double> standaloneDoubleSetting(String name, String description) {
    return this.standaloneSetting(Double.class, name, description, StandardStringCodecs.DOUBLE);
  }

  /** Creates a standalone double {@link Setting} that stores its own value, with the given default value. */
  public Setting<Double> standaloneDoubleSetting(String name, String description, double defaultValue) {
    return this.standaloneSetting(Double.class, name, description, defaultValue, StandardStringCodecs.DOUBLE);
  }

  /** Creates a standalone double {@link Setting} that stores its own value, with a default and custom codec. */
  public Setting<Double> standaloneDoubleSetting(String name, String description, double defaultValue, StringCodec<Double> strCodec) {
    return this.standaloneSetting(Double.class, name, description, defaultValue, strCodec);
  }

  /** Creates a standalone date-time {@link Setting} that stores its own value, with no default value. */
  public Setting<Date> standaloneDateTimeSetting(String name, String description) {
    return this.standaloneSetting(Date.class, name, description, StandardStringCodecs.DATE_TIME);
  }

  /** Creates a standalone date-time {@link Setting} that stores its own value, with the given default value. */
  public Setting<Date> standaloneDateTimeSetting(String name, String description, Date defaultValue) {
    return this.standaloneSetting(Date.class, name, description, defaultValue, StandardStringCodecs.DATE_TIME);
  }

  /** Creates a standalone date-time {@link Setting} that stores its own value, with a default and custom codec. */
  public Setting<Date> standaloneDateTimeSetting(String name, String description, Date defaultValue, StringCodec<Date> strCodec) {
    return this.standaloneSetting(Date.class, name, description, defaultValue, strCodec);
  }

  /** Creates a standalone enum {@link Setting} that stores its own value, with no default value. */
  public <E extends Enum<E>> Setting<E> standaloneEnumSetting(Class<E> enumClass, String name, String description) {
    return this.standaloneSetting(enumClass, name, description, enumStringCodec(enumClass));
  }

  /** Creates a standalone enum {@link Setting} that stores its own value, with the given default value. */
  public <E extends Enum<E>> Setting<E> standaloneEnumSetting(Class<E> enumClass, String name, String description, E defaultValue) {
    return this.standaloneSetting(enumClass, name, description, defaultValue, enumStringCodec(enumClass));
  }

  /** Creates a standalone enum {@link Setting} that stores its own value, with a default and custom codec. */
  public <E extends Enum<E>> Setting<E> standaloneEnumSetting(Class<E> enumClass, String name, String description, E defaultValue, StringCodec<E> stringCodec) {
    return this.standaloneSetting(enumClass, name, description, defaultValue, stringCodec);
  }

  /**
   * Creates a standalone {@link ListSetting} that stores its own values, empty by default. Elements are
   * converted using the standard codec for the element type.
   */
  public <E> ListSetting<E> standaloneListSetting(Class<E> elementClass, String name, String description) {
    return new StandaloneListSetting<E>(elementClass, name, description, Collections.emptyList(), getStringCodec(elementClass));
  }

  /**
   * Creates a standalone {@link ListSetting} that stores its own values, with the given default values. Elements
   * are converted using the standard codec for the element type.
   */
  public <E> ListSetting<E> standaloneListSetting(Class<E> elementClass, String name, String description, List<E> defaultValue) {
    return new StandaloneListSetting<E>(elementClass, name, description, defaultValue, getStringCodec(elementClass));
  }

  /**
   * Creates a standalone {@link ListSetting} that stores its own values, with the given default values. Elements
   * are converted using the given codec(s).
   */
  public <E> ListSetting<E> standaloneListSetting(Class<E> elementClass, String name, String description, List<E> defaultValue, StringCodec<E> stringCodec) {
    return new StandaloneListSetting<>(elementClass, name, description, defaultValue, stringCodec);
  }

  /**
   * Creates a standalone {@link ListSetting} that stores its own values, with the given default values. Elements
   * are converted using the given codec(s).
   */
  public <E> ListSetting<E> standaloneListSetting(Class<E> elementClass, String name, String description, List<E> defaultValue, PropertyCodec<E> propertyCodec, XmlCodec<E> xmlCodec) {
    return new StandaloneListSetting<>(elementClass, name, description, defaultValue, propertyCodec, xmlCodec);
  }

  /**
   * Creates a standalone {@link Setting} for classes assignable to the given base class that stores its own
   * value, with no default value.
   */
  public Setting<Class> standaloneClassSetting(Class baseClass, String name, String description) {
    return this.standaloneSetting(baseClass, name, description, null, classStringCodec(baseClass));
  }

  /**
   * Creates a standalone {@link Setting} for classes assignable to the given base class that stores its own
   * value, with the given default value.
   */
  public Setting<Class> standaloneClassSetting(Class baseClass, String name, String description, Class defaultValue) {
    return this.standaloneSetting(baseClass, name, description, defaultValue, classStringCodec(baseClass));
  }

  /**
   * Creates a standalone {@link Setting} for classes assignable to the given base class that stores its own
   * value, with a default and custom codec.
   */
  public Setting<Class> standaloneClassSetting(Class baseClass, String name, String description, Class defaultValue, StringCodec<Class> stringCodec) {
    return this.standaloneSetting(baseClass, name, description, defaultValue, stringCodec);
  }

  /**
   * Creates a standalone {@link Setting} that stores its own value, with no default value, converted using the
   * standard codec for the value type.
   */
  public <T> Setting<T> standaloneSetting(Class<T> valueType, String name, String description) {
    return new StandaloneSetting<>(valueType, name, description, getStringCodec(valueType));
  }

  /** Creates a standalone {@link Setting} that stores its own value, with no default value, using the given codec. */
  public <T> Setting<T> standaloneSetting(Class<T> valueType, String name, String description, StringCodec<T> strCodec) {
    return new StandaloneSetting<>(valueType, name, description, strCodec);
  }

  /**
   * Creates a standalone {@link Setting} that stores its own value, with the given default value, converted
   * using the standard codec for the value type.
   */
  public <T> Setting<T> standaloneSetting(Class<T> valueType, String name, String description, T defaultValue) {
    return new StandaloneSetting<>(valueType, name, description, defaultValue, getStringCodec(valueType));
  }

  /** Creates a standalone {@link Setting} that stores its own value, with a default and custom codec. */
  public <T> Setting<T> standaloneSetting(Class<T> valueType, String name, String description, T defaultValue, StringCodec<T> strCodec) {
    return new StandaloneSetting<>(valueType, name, description, defaultValue, strCodec);
  }

  /**
   * Creates a {@link Setting} whose value is read and written through the given getter and setter, using the
   * standard codec for the value type.
   */
  public <T> Setting<T> attachedSetting(Class<T> valueType, String name, String description, Supplier<T> getF, Consumer<T> setF) {
    final TypedValue<T> tv = new TypedValue<T>() {
      @Override public T get() {
        return getF.get();
      }
      @Override public void set(T value) {
        setF.accept(value);
      }
    };
    return this.attachedSetting(valueType, name, description, tv);
  }

  /**
   * Creates a {@link Setting} whose value is read and written through the given {@link TypedValue}, using the
   * standard codec for the value type.
   */
  public <T> Setting<T> attachedSetting(Class<T> valueType, String name, String description, TypedValue<T> tv) {
    return new AttachedSetting<T>(valueType, name, description, tv, getStringCodec(valueType));
  }

  /**
   * Creates a {@link Setting} whose value is read and written through the given getter and setter, using the
   * given codec.
   */
  public <T> Setting<T> attachedSetting(Class<T> valueType, String name, String description, StringCodec<T> strCodec, Supplier<T> getF, Consumer<T> setF) {
    final TypedValue<T> tv = new TypedValue<T>() {
      @Override public T get() {
        return getF.get();
      }
      @Override public void set(T value) {
        setF.accept(value);
      }
    };
    return this.attachedSetting(valueType, name, description, strCodec, tv);
  }

  /**
   * Creates a {@link Setting} whose value is read and written through the given {@link TypedValue}, using the
   * given codec.
   */
  public <T> Setting<T> attachedSetting(Class<T> valueType, String name, String description, StringCodec<T> strCodec, TypedValue<T> tv) {
    return new AttachedSetting<T>(valueType, name, description, tv, strCodec);
  }

  /**
   * Creates a {@link Setting} with a default value, whose value is read and written through the given getter and
   * setter, using the standard codec for the value type.
   */
  public <T> Setting<T> attachedSetting(Class<T> valueType, String name, String description, T defaultValue, Supplier<T> getF, Consumer<T> setF) {
    final TypedValue<T> tv = new TypedValue<T>() {
      @Override public T get() {
        return getF.get();
      }
      @Override public void set(T value) {
        setF.accept(value);
      }
    };
    return this.attachedSetting(valueType, name, description, defaultValue, tv);
  }

  /**
   * Creates a {@link Setting} with a default value, whose value is read and written through the given {@link
   * TypedValue}, using the standard codec for the value type.
   */
  public <T> Setting<T> attachedSetting(Class<T> valueType, String name, String description, T defaultValue, TypedValue<T> tv) {
    return new AttachedSetting<T>(valueType, name, description, defaultValue, tv, getStringCodec(valueType));
  }

  /**
   * Creates a {@link Setting} with a default value, whose value is read and written through the given getter and
   * setter, using the given codec.
   */
  public <T> Setting<T> attachedSetting(Class<T> valueType, String name, String description, T defaultValue, StringCodec<T> strCodec, Supplier<T> getF, Consumer<T> setF) {
    final TypedValue<T> tv = new TypedValue<T>() {
      @Override public T get() {
        return getF.get();
      }
      @Override public void set(T value) {
        setF.accept(value);
      }
    };
    return this.attachedSetting(valueType, name, description, defaultValue, strCodec, tv);
  }

  /**
   * Creates a {@link Setting} with a default value, whose value is read and written through the given {@link
   * TypedValue}, using the given codec.
   */
  public <T> Setting<T> attachedSetting(Class<T> valueType, String name, String description, T defaultValue, StringCodec<T> strCodec, TypedValue<T> tv) {
    return new AttachedSetting<T>(valueType, name, description, defaultValue, tv, strCodec);
  }

  /**
   * Creates a {@link Setting} attached to a field annotated with {@link ConfigSetting}. For {@link List} fields, the annotation must declare an <code>elementType</code> and a {@link ListSetting} is returned.
   *
   * @throws IllegalArgumentException If the annotation is missing, or the element type is missing for a list field.
   * @throws Exception If reflection or codec instantiation fails.
   */
  public Setting<?> attachedSetting(Object targetObject, Field targetField) throws Exception {
    targetField.setAccessible(true);
    // Check annotations on field
    final ConfigSetting fieldAnnotation = targetField.getAnnotation(ConfigSetting.class);
    if (fieldAnnotation == null) {
      throw new IllegalArgumentException("Field '" + targetField + "' doesn't have the required ConfigSetting annotation");
    }
    Class<?> valueType = targetField.getType();
    if (List.class.isAssignableFrom(valueType)) {
      Class<?> elemType = fieldAnnotation.elementType();
      if ((elemType == null) || (elemType == void.class)) {
        throw new IllegalArgumentException("Field '" + targetField + "' misses a mandatory elementType value in its ConfigSetting annotation");
      }
      return this.fieldAnnotationBasedListSetting(elemType, targetObject, fieldAnnotation, targetField);
    }
    else {
      return this.fieldAnnotationBasedSetting(valueType, targetObject, fieldAnnotation, targetField);
    }
  }

  /**
   * Creates a {@link Setting} of the given type attached to a field annotated with {@link ConfigSetting}, using the annotation's name, description and codec.
   *
   * @throws IllegalArgumentException If the field has no {@link ConfigSetting} annotation.
   * @throws Exception If reflection or codec instantiation fails.
   */
  public <T> Setting<T> attachedSetting(Class<T> valueType, Object targetObject, Field targetField) throws Exception {
    targetField.setAccessible(true);
    // Check annotations on field
    final ConfigSetting fieldAnnotation = targetField.getAnnotation(ConfigSetting.class);
    if (fieldAnnotation == null) {
      throw new IllegalArgumentException("Field '" + targetField + "' doesn't have the required ConfigSetting annotation");
    }
    return this.fieldAnnotationBasedSetting(valueType, targetObject, fieldAnnotation, targetField);
  }

  /**
   * Creates a {@link ListSetting} attached to a {@link List} field annotated with {@link ConfigSetting}.
   *
   * @throws IllegalArgumentException If the field has no {@link ConfigSetting} annotation.
   * @throws Exception If reflection or codec instantiation fails.
   */
  public <T> ListSetting<T> attachedListSetting(Class<T> listElemType, Object targetObject, Field targetField) throws Exception {
    targetField.setAccessible(true);
    // Check annotations on field
    final ConfigSetting fieldAnnotation = targetField.getAnnotation(ConfigSetting.class);
    if (fieldAnnotation == null) {
      throw new IllegalArgumentException("Field '" + targetField + "' doesn't have the required ConfigSetting annotation");
    }
    return this.fieldAnnotationBasedListSetting(listElemType, targetObject, fieldAnnotation, targetField);
  }

  /**
   * Creates a {@link SettingsGroup} attached to a field annotated with {@link ConfigGroup}, containing the elements discovered by reflection on the field's value.
   *
   * @throws IllegalArgumentException If the field has no {@link ConfigGroup} annotation.
   * @throws Exception If reflection fails.
   */
  public SettingsGroup<?> attachedSettingsGroup(Object targetObject, Field targetField) throws Exception {
    targetField.setAccessible(true);
    // Check annotations on field + create group
    final ConfigGroup fieldAnnotation = targetField.getAnnotation(ConfigGroup.class);
    if (fieldAnnotation == null) {
      throw new IllegalArgumentException("Field '" + targetField + "' doesn't have the required ConfigGroup annotation");
    }
    final String name = getName(fieldAnnotation.name(), targetField);
    final String description = fieldAnnotation.description();
    Object fieldObject = targetField.get(targetObject);
    Class<?> targetClass = fieldObject.getClass();
    SettingsGroup group = new SettingsGroup(name, description, this.discoverElements(fieldObject));
    return group;
  }

  ConfigElement[] discoverElements(Object targetObject) throws Exception {
    List<ConfigElement> configElements = new ArrayList<>();
    Class<?> targetClass = targetObject.getClass();
    for (Field fld : ReflectionUtils.listAssignableFields(targetClass, ConfigElement.class)) {
      configElements.add((ConfigElement)fld.get(targetObject));
    }
    for (Field fld : ReflectionUtils.listAnnotatedFields(targetClass, ConfigSetting.class)) {
      Setting<?> set = this.attachedSetting(targetObject, fld);
      configElements.add(set);
    }
    for (Field fld : ReflectionUtils.listAnnotatedFields(targetClass, ConfigGroup.class)) {
      SettingsGroup<?> grp = this.attachedSettingsGroup(targetObject, fld);
      configElements.add(grp);
    }
    return configElements.toArray(new ConfigElement[0]);
  }

  /**
   * Creates a {@link Config} from the elements discovered by reflection on the given object: its {@link ConfigElement} fields and its fields annotated with {@link ConfigSetting} or {@link ConfigGroup}.
   *
   * @param targetObject The object to inspect.
   * @throws Exception If reflection fails.
   */
  public Config attachedConfig(Object targetObject) throws Exception {
    return new Config(this.discoverElements(targetObject));
  }
}
