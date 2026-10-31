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

  public Setting<Boolean> standaloneBooleanSetting(String name, String description) {
    return this.standaloneSetting(Boolean.class, name, description, StandardStringCodecs.BOOLEAN);
  }

  public Setting<Boolean> standaloneBooleanSetting(String name, String description, boolean defaultValue) {
    return this.standaloneSetting(Boolean.class, name, description, defaultValue, StandardStringCodecs.BOOLEAN);
  }

  public Setting<Boolean> standaloneBooleanSetting(String name, String description, boolean defaultValue, StringCodec<Boolean> strCodec) {
    return this.standaloneSetting(Boolean.class, name, description, defaultValue, strCodec);
  }

  public Setting<Integer> standaloneIntegerSetting(String name, String description) {
    return this.standaloneSetting(Integer.class, name, description, StandardStringCodecs.INTEGER);
  }

  public Setting<Integer> standaloneIntegerSetting(String name, String description, int defaultValue) {
    return this.standaloneSetting(Integer.class, name, description, defaultValue, StandardStringCodecs.INTEGER);
  }

  public Setting<Integer> standaloneIntegerSetting(String name, String description, int defaultValue, StringCodec<Integer> strCodec) {
    return this.standaloneSetting(Integer.class, name, description, defaultValue, strCodec);
  }

  public Setting<Integer> standaloneLogLevelSetting(String name, String description) {
    return this.standaloneSetting(Integer.class, name, description, StandardStringCodecs.LOG_LEVEL);
  }

  public Setting<Integer> standaloneLogLevelSetting(String name, String description, int defaultValue) {
    return this.standaloneSetting(Integer.class, name, description, defaultValue, StandardStringCodecs.LOG_LEVEL);
  }

  public Setting<Long> standaloneLongSetting(String name, String description) {
    return this.standaloneSetting(Long.class, name, description, StandardStringCodecs.LONG);
  }

  public Setting<Long> standaloneLongSetting(String name, String description, long defaultValue) {
    return this.standaloneSetting(Long.class, name, description, defaultValue, StandardStringCodecs.LONG);
  }

  public Setting<Long> standaloneLongSetting(String name, String description, long defaultValue, StringCodec<Long> strCodec) {
    return this.standaloneSetting(Long.class, name, description, defaultValue, strCodec);
  }

  public Setting<Long> standaloneByteSizeSetting(String name, String description) {
    return this.standaloneSetting(Long.class, name, description, StandardStringCodecs.BYTE_SIZE);
  }

  public Setting<Long> standaloneByteSizeSetting(String name, String description, long defaultValue) {
    return this.standaloneSetting(Long.class, name, description, defaultValue, StandardStringCodecs.BYTE_SIZE);
  }

  public Setting<Long> standaloneTimespanSetting(String name, String description) {
    return this.standaloneSetting(Long.class, name, description, StandardStringCodecs.TIMESPAN);
  }

  public Setting<Long> standaloneTimespanSetting(String name, String description, long defaultValue) {
    return this.standaloneSetting(Long.class, name, description, defaultValue, StandardStringCodecs.TIMESPAN);
  }

  public Setting<Long> standaloneTimespanSetting(String name, String description, long defaultValue, StringCodec<Long> strCodec) {
    return this.standaloneSetting(Long.class, name, description, defaultValue, strCodec);
  }

  public Setting<String> standaloneStringSetting(String name, String description) {
    return this.standaloneSetting(String.class, name, description, StandardStringCodecs.STRING);
  }

  public Setting<String> standaloneStringSetting(String name, String description, String defaultValue) {
    return this.standaloneSetting(String.class, name, description, defaultValue, StandardStringCodecs.STRING);
  }

  public Setting<String> standaloneStringSetting(String name, String description, String defaultValue, StringCodec<String> strCodec) {
    return this.standaloneSetting(String.class, name, description, defaultValue, strCodec);
  }

  public Setting<String> standalonePasswordSetting(String name, String description) {
    return this.standaloneSetting(String.class, name, description, StandardStringCodecs.PASSWORD);
  }

  public Setting<String> standalonePasswordSetting(String name, String description, String defaultValue) {
    return this.standaloneSetting(String.class, name, description, defaultValue, StandardStringCodecs.PASSWORD);
  }

  public Setting<Charset> standaloneCharsetSetting(String name, String description) {
    return this.standaloneSetting(Charset.class, name, description, StandardStringCodecs.CHARSET);
  }

  public Setting<Charset> standaloneCharsetSetting(String name, String description, Charset defaultValue) {
    return this.standaloneSetting(Charset.class, name, description, defaultValue, StandardStringCodecs.CHARSET);
  }

  public Setting<byte[]> standaloneByteArraySetting(String name, String description) {
    return this.standaloneSetting(byte[].class, name, description, StandardStringCodecs.BYTE_ARRAY);
  }

  public Setting<byte[]> standaloneByteArraySetting(String name, String description, byte[] defaultValue) {
    return this.standaloneSetting(byte[].class, name, description, defaultValue, StandardStringCodecs.BYTE_ARRAY);
  }

  public Setting<byte[]> standaloneByteArraySetting(String name, String description, byte[] defaultValue, StringCodec<byte[]> strCodec) {
    return this.standaloneSetting(byte[].class, name, description, defaultValue, strCodec);
  }

  public Setting<Double> standaloneDoubleSetting(String name, String description) {
    return this.standaloneSetting(Double.class, name, description, StandardStringCodecs.DOUBLE);
  }

  public Setting<Double> standaloneDoubleSetting(String name, String description, double defaultValue) {
    return this.standaloneSetting(Double.class, name, description, defaultValue, StandardStringCodecs.DOUBLE);
  }

  public Setting<Double> standaloneDoubleSetting(String name, String description, double defaultValue, StringCodec<Double> strCodec) {
    return this.standaloneSetting(Double.class, name, description, defaultValue, strCodec);
  }

  public Setting<Date> standaloneDateTimeSetting(String name, String description) {
    return this.standaloneSetting(Date.class, name, description, StandardStringCodecs.DATE_TIME);
  }

  public Setting<Date> standaloneDateTimeSetting(String name, String description, Date defaultValue) {
    return this.standaloneSetting(Date.class, name, description, defaultValue, StandardStringCodecs.DATE_TIME);
  }

  public Setting<Date> standaloneDateTimeSetting(String name, String description, Date defaultValue, StringCodec<Date> strCodec) {
    return this.standaloneSetting(Date.class, name, description, defaultValue, strCodec);
  }

  public <E extends Enum<E>> Setting<E> standaloneEnumSetting(Class<E> enumClass, String name, String description) {
    return this.standaloneSetting(enumClass, name, description, enumStringCodec(enumClass));
  }

  public <E extends Enum<E>> Setting<E> standaloneEnumSetting(Class<E> enumClass, String name, String description, E defaultValue) {
    return this.standaloneSetting(enumClass, name, description, defaultValue, enumStringCodec(enumClass));
  }

  public <E extends Enum<E>> Setting<E> standaloneEnumSetting(Class<E> enumClass, String name, String description, E defaultValue, StringCodec<E> stringCodec) {
    return this.standaloneSetting(enumClass, name, description, defaultValue, stringCodec);
  }

  public <E> ListSetting<E> standaloneListSetting(Class<E> elementClass, String name, String description) {
    return new StandaloneListSetting<E>(elementClass, name, description, Collections.emptyList(), getStringCodec(elementClass));
  }

  public <E> ListSetting<E> standaloneListSetting(Class<E> elementClass, String name, String description, List<E> defaultValue) {
    return new StandaloneListSetting<E>(elementClass, name, description, defaultValue, getStringCodec(elementClass));
  }

  public <E> ListSetting<E> standaloneListSetting(Class<E> elementClass, String name, String description, List<E> defaultValue, StringCodec<E> stringCodec) {
    return new StandaloneListSetting<>(elementClass, name, description, defaultValue, stringCodec);
  }

  public <E> ListSetting<E> standaloneListSetting(Class<E> elementClass, String name, String description, List<E> defaultValue, PropertyCodec<E> propertyCodec, XmlCodec<E> xmlCodec) {
    return new StandaloneListSetting<>(elementClass, name, description, defaultValue, propertyCodec, xmlCodec);
  }

  public Setting<Class> standaloneClassSetting(Class baseClass, String name, String description) {
    return this.standaloneSetting(baseClass, name, description, null, classStringCodec(baseClass));
  }

  public Setting<Class> standaloneClassSetting(Class baseClass, String name, String description, Class defaultValue) {
    return this.standaloneSetting(baseClass, name, description, defaultValue, classStringCodec(baseClass));
  }

  public Setting<Class> standaloneClassSetting(Class baseClass, String name, String description, Class defaultValue, StringCodec<Class> stringCodec) {
    return this.standaloneSetting(baseClass, name, description, defaultValue, stringCodec);
  }

  public <T> Setting<T> standaloneSetting(Class<T> valueType, String name, String description) {
    return new StandaloneSetting<>(valueType, name, description, getStringCodec(valueType));
  }

  public <T> Setting<T> standaloneSetting(Class<T> valueType, String name, String description, StringCodec<T> strCodec) {
    return new StandaloneSetting<>(valueType, name, description, strCodec);
  }

  public <T> Setting<T> standaloneSetting(Class<T> valueType, String name, String description, T defaultValue) {
    return new StandaloneSetting<>(valueType, name, description, defaultValue, getStringCodec(valueType));
  }

  public <T> Setting<T> standaloneSetting(Class<T> valueType, String name, String description, T defaultValue, StringCodec<T> strCodec) {
    return new StandaloneSetting<>(valueType, name, description, defaultValue, strCodec);
  }

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

  public <T> Setting<T> attachedSetting(Class<T> valueType, String name, String description, TypedValue<T> tv) {
    return new AttachedSetting<T>(valueType, name, description, tv, getStringCodec(valueType));
  }

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

  public <T> Setting<T> attachedSetting(Class<T> valueType, String name, String description, StringCodec<T> strCodec, TypedValue<T> tv) {
    return new AttachedSetting<T>(valueType, name, description, tv, strCodec);
  }

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

  public <T> Setting<T> attachedSetting(Class<T> valueType, String name, String description, T defaultValue, TypedValue<T> tv) {
    return new AttachedSetting<T>(valueType, name, description, defaultValue, tv, getStringCodec(valueType));
  }

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

  public <T> Setting<T> attachedSetting(Class<T> valueType, String name, String description, T defaultValue, StringCodec<T> strCodec, TypedValue<T> tv) {
    return new AttachedSetting<T>(valueType, name, description, defaultValue, tv, strCodec);
  }

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

  public <T> Setting<T> attachedSetting(Class<T> valueType, Object targetObject, Field targetField) throws Exception {
    targetField.setAccessible(true);
    // Check annotations on field
    final ConfigSetting fieldAnnotation = targetField.getAnnotation(ConfigSetting.class);
    if (fieldAnnotation == null) {
      throw new IllegalArgumentException("Field '" + targetField + "' doesn't have the required ConfigSetting annotation");
    }
    return this.fieldAnnotationBasedSetting(valueType, targetObject, fieldAnnotation, targetField);
  }

  public <T> ListSetting<T> attachedListSetting(Class<T> listElemType, Object targetObject, Field targetField) throws Exception {
    targetField.setAccessible(true);
    // Check annotations on field
    final ConfigSetting fieldAnnotation = targetField.getAnnotation(ConfigSetting.class);
    if (fieldAnnotation == null) {
      throw new IllegalArgumentException("Field '" + targetField + "' doesn't have the required ConfigSetting annotation");
    }
    return this.fieldAnnotationBasedListSetting(listElemType, targetObject, fieldAnnotation, targetField);
  }

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

  public Config attachedConfig(Object targetObject) throws Exception {
    return new Config(this.discoverElements(targetObject));
  }
}
