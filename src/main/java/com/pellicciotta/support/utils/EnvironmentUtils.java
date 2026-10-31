// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import com.pellicciotta.support.exceptions.FormatException;

import java.io.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Class providing http to information about the current runtime environment.
 */
public abstract class EnvironmentUtils
{
  /** Prevent instantiation */
  private EnvironmentUtils() { }

  // <editor-fold desc="General Environment Properties">

  /**
   * Lists all Java system property names.
   *
   * @return The sorted list of property names.
   */
  public static List<String> getAllSystemPropertyNames() {
    SortedSet<String> propNames = new TreeSet<>();
    propNames.addAll(System.getProperties().stringPropertyNames());
    return new ArrayList<String>(propNames);
  }

  /**
   * Lists all property names, that is: all names of Java system properties and of environment variables.
   *
   * @return The sorted list of property names.
   */
  public static List<String> getAllPropertyNames() {
    SortedSet<String> propNames = new TreeSet<>();
    propNames.addAll(System.getProperties().stringPropertyNames());
    propNames.addAll(System.getenv().keySet());
    return new ArrayList<String>(propNames);
  }

  /**
   * Lists all Java system properties only.
   *
   * @return The map of property names to values.
   */
  public static Map<String, String> getAllSystemProperties() {
    return PropertiesUtils.propertiesToMap(System.getProperties());
  }

  /**
   * Lists all properties: the names and values of all Java system properties and environment variables.
   *
   * @return The map of property names to values. If both a system properties and env. variable have the same name,
   *         it will be the value of the system property that is returned.
   */
  public static Map<String, String> getAllProperties() {
    Map<String, String> props = new HashMap<>();
    props.putAll(System.getenv());
    props.putAll(PropertiesUtils.propertiesToMap(System.getProperties()));
    return props;
  }

  /**
   * Determine whether the specified name is a Java system property.
   *
   * @param name The system property name to be checked.
   *
   * @return True when a system property with the specified name exists, false otherwise.
   */
  public static boolean isSystemProperty(String name) {
    Contract.isNotNull("name", name);
    return System.getProperty(name) != null;
  }

  /**
   * Determine whether the specified system property or environment variable exists.
   *
   * @param name The system property or environment variable name to be checked.
   *
   * @return True when a system property or environment variable with the specified name exists, false otherwise.
   */
  public static boolean isProperty(String name) {
    Contract.isNotNull("name", name);
    if (System.getProperty(name) != null) {
      return true;
    }
    return (System.getenv(name) != null);
  }

  /**
   * Get the named system property or environment variable or environment variable value as a string.
   *
   * @param propertyName The name of the property of environment variable.
   *
   * @return The value of the system property, if it exists or else the value of the environment variable.
   *         Will be <code>null</code> if no such property exists.
   */
  public static String getStringProperty(String propertyName) {
    Contract.isNotNull("propertyName", propertyName);
    String propVal = System.getProperty(propertyName);
    if (propVal != null) {
      return propVal;
    }
    return System.getenv(propertyName);
  }

  /**
   * Get the named system property or environment variable or environment variable as a string.
   *
   * @param propertyName The name of the property or environment variable.
   * @param defaultValue The value to be used when the property is not defined.
   *
   * @return The system property or environment variable value or the default value if no such property value exists.
   */
  public static String getStringProperty(String propertyName, String defaultValue) {
    String propVal = getStringProperty(propertyName);
    if (propVal == null) {
      propVal = defaultValue;
    }
    return propVal;
  }

  /**
   * Get the named system property or environment variable or environment variable as a boolean.
   *
   * @param propertyName The name of the property.
   *
   * @return The value of the system property or environment variable, interpreted as a boolean value.
   *
   * @throws FormatException When the property value doesn't specify a boolean value.
   */
  public static boolean getBooleanProperty(String propertyName) throws FormatException {
    Contract.isNotNull("propertyName", propertyName);
    String propVal = getStringProperty(propertyName);
    return StringConversionUtils.toBoolean(propVal, false);
  }

  /**
   * Get the named system property or environment variable as a boolean.
   *
   * @param propertyName The name of the property.
   * @param defaultValue The value to be used when the property is not defined
   *
   * @return The named system property or environment variable value or else the default value, if no such named property exists.
   *
   * @throws FormatException When the system property exists but its value doesn't specify a boolean value.
   */
  public static boolean getBooleanProperty(String propertyName, boolean defaultValue) throws FormatException {
    Contract.isNotNull("propertyName", propertyName);
    String propVal = getStringProperty(propertyName);
    if (propVal == null) {
      return defaultValue;
    }
    return StringConversionUtils.toBoolean(propVal, false);
  }

  /**
   * Get the named system property or environment variable as an integer.
   *
   * @param propertyName The name of the property.
   *
   * @return The named system property or environment variable value.
   *
   * @throws FormatException When the property value doesn't specify an integer value.
   */
  public static int getIntegerProperty(String propertyName) throws FormatException {
    Contract.isNotNull("propertyName", propertyName);
    String propVal = getStringProperty(propertyName);
    return StringConversionUtils.toInt(propVal, false);
  }

  /**
   * Get the named system property or environment variable as an integer.
   *
   * @param propertyName The name of the property.
   * @param defaultValue The value to be used when the property is not defined.
   *
   * @return The named system property or environment variable value or else the default value, if no such named property exists.
   *
   * @throws FormatException When the system property exists but its value doesn't specify an integer value.
   */
  public static int getIntegerProperty(String propertyName, int defaultValue) throws FormatException {
    Contract.isNotNull("propertyName", propertyName);
    String propVal = getStringProperty(propertyName);
    if (propVal == null) {
      return defaultValue;
    }
    return StringConversionUtils.toInt(propVal, false);
  }

  /**
   * Get the named system property or environment variable as a double.
   *
   * @param propertyName The name of the property.
   *
   * @return The named system property or environment variable or null if doesn't exist.
   *
   * @throws IllegalArgumentException When propertyName doesn't specify an existing system property.
   * @throws FormatException When the property value doesn't specify a double value.
   */
  public static double getDoubleProperty(String propertyName) throws FormatException {
    Contract.isNotNull("propertyName", propertyName);
    String propVal = getStringProperty(propertyName);
    return StringConversionUtils.toDouble(propVal, false);
  }

  /**
   * Get the named system property or environment variable as a double.
   *
   * @param propertyName The name of the property.
   * @param defaultValue The value to be used when the property is not defined.
   *
   * @return The named system property or environment variable value or else the default value, if no such named property exists.
   *
   * @throws FormatException When the system property exists but its value doesn't specify a double value.
   */
  public static double getDoubleProperty(String propertyName, double defaultValue) throws FormatException {
    Contract.isNotNull("propertyName", propertyName);
    String propVal = getStringProperty(propertyName);
    if (propVal == null) {
      return defaultValue;
    }
    return StringConversionUtils.toDouble(propVal, false);
  }

  // </editor-fold>
  // <editor-fold desc="Specific Environment Properties">

  /**
   *  @return The result of {@code System.getProperty("java.home")}.
   */
  public static String getJavaInstallationDirectory() {
    return System.getProperty("java.home");
  }

  /**
   *  @return The result of {@code System.getProperty("java.version")}.
   */
  public static String getJavaVersion() {
    return System.getProperty("java.version");
  }

  /**
   *  @return The result of {@code System.getProperty("java.class.path")}.
   */
  public static String getJavaClassPath() {
    return System.getProperty("java.class.path");
  }

  /**
   *  @return The result of {@code System.getProperty("java.class.path")}, split by {@code System.getProperty("java.separator"}
   */
  public static String[] getJavaClassPathComponents() {
    return System.getProperty("java.class.path").split(Pattern.quote(getPathSeparator()));
  }

  /**
   *  @return The result of {@code System.getProperty("os.name")}.
   */
  public static String getOsName() {
    return System.getProperty("os.name");
  }

  /**
   *  @return The result of {@code System.getProperty("os.version")}.
   */
  public static String getOsVersion() {
    return System.getProperty("os.version");
  }

  /**
   *  @return Whether the OS on which the current JVM is running is a Windows OS.
   */
  public static boolean isWindows() {
    return getOsName().toLowerCase().indexOf("win") >= 0;
  }

  /**
   *  @return The result of {@code System.getProperty("line.separator")}.
   */
  public static String getLineSeparator() {
    return System.getProperty("line.separator");
  }

  /**
   *  @return The result of {@code System.getProperty("file.separator")}.
   */
  public static String getFileSeparator() {
    return System.getProperty("file.separator");
  }

  /**
   *  @return The result of {@code System.getProperty("path.separator")}.
   */
  public static String getPathSeparator() {
    return System.getProperty("path.separator");
  }

  /**
   *  @return The result of {@code System.getProperty("user.name")}.
   */
  public static String getUser() {
    return System.getProperty("user.name");
  }

  /**
   *  @return The result of {@code System.getProperty("user.home")}.
   */
  public static String getHomeDirectory() {
    return System.getProperty("user.home");
  }

  /**
   *  @return The result of {@code System.getProperty("user.dir")}.
   */
  public static String getCurrentDirectory() {
    return System.getProperty("user.dir");
  }

  // </editor-fold>

  // <editor-fold desc="Debugging Aids">

  /**
   *  Go through each Java system property, in name order.
   *
   *  @param propertyConsumer Code that can do something with the passed down property name and value.
   */
  public static void forEachSystemProperty(BiConsumer<String, String> propertyConsumer) {
    Contract.isNotNull(propertyConsumer);
    for (String propName : getAllSystemPropertyNames()) {
      String propVal = System.getProperty(propName);
      propertyConsumer.accept(propName, propVal);
    }
  }

  /**
   *  Go through each Java system or environment variable property, in name order.
   *
   *  @param propertyConsumer Code that can do something with the passed down property name and value.
   */
  public static void forEachProperty(BiConsumer<String, String> propertyConsumer) {
    Contract.isNotNull(propertyConsumer);
    for (String propName : getAllPropertyNames()) {
      String propVal = getStringProperty(propName);
      propertyConsumer.accept(propName, propVal);
    }
  }

  // </editor-fold>
}
