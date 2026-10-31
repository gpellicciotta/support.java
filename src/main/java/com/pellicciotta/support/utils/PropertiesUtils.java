// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import com.pellicciotta.support.logging.Log;
import com.pellicciotta.support.logging.LogService;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A set of utility methods for working with {@link Properties} or {@link Map<String,String>} collections.
 */
public final class PropertiesUtils
{
  private static Log log = LogService.fromCurrentClass();

  /** Prevent instantiation */
  private PropertiesUtils() { }

  // <editor-fold desc="Load Methods">

  /**
   *  Load properties from the named properties file, but not if a property with the same name already exists.
   *
   *  @param propsFileName The name of the properties file to load system properties from.
   */
  public static int loadSystemProperties(String propsFileName) throws IOException {
    return loadSystemProperties(propsFileName, false, false);
  }

  /**
   *  Load properties from the named properties file.
   *
   *  @param propsFileName The name of the properties file to load system properties from.
   *  @param overrideExisting Whether existing system properties will be overridden when also loaded from a file.
   *  @param alsoLoadFromParentDirs If <code>true</code>, all parent directories will be inspected for
   *                                files with the same name and, if found, additional properties from these
   *                                files will be loaded. The word 'additional' is important here:
   *                                properties that already were loaded, will never be overwritten.
   */
  public static int loadSystemProperties(String propsFileName, boolean overrideExisting, boolean alsoLoadFromParentDirs) throws IOException {
    Properties p = new Properties();
    File fileToCheck = new File(propsFileName);
    int filesLoaded = 0;
    Properties newProps = new Properties();
    if (!alsoLoadFromParentDirs) {
      if (fileToCheck.isFile()) {
        Properties extraProps = loadProperties(fileToCheck);
        PropertiesUtils.mergeInto(extraProps, newProps);
        filesLoaded += 1;
      }
    }
    else {
      String[] dirs = FileUtils.getDirectoryNames(fileToCheck.getAbsolutePath(), true);
      if (dirs.length > 0) {
        // Determine all props files to be checked, from most specific /a/b/c/d.props to least: /d.props
        String baseName = fileToCheck.getName();
        ArrayList<File> filesToCheck = new ArrayList<>(dirs.length);
        File lastParentDir = null;
        for (int i = 0; i < dirs.length; i++) {
          if (i == 0) {
            lastParentDir = new File(dirs[0]);
          }
          else {
            lastParentDir = new File(lastParentDir, dirs[i]);
          }
          File f = new File(lastParentDir, baseName);
          filesToCheck.add(0, f);
        }
        // Check each file in turn
        for (File parentFileToCheck : filesToCheck) {
          if (parentFileToCheck.isFile()) {
            Properties extraProps = loadProperties(parentFileToCheck);
            PropertiesUtils.mergeInto(extraProps, newProps);
            filesLoaded += 1;
          }
        }
      }
    }
    if (filesLoaded == 0) {
      throw new FileNotFoundException("No properties file named '" + propsFileName + "' could be found, also not in any parent directories");
    }
    int copied = 0;
    for (String propName : newProps.stringPropertyNames()) {
      if (overrideExisting || (System.getProperty(propName) == null)) {
        String propVal = newProps.getProperty(propName);
        System.setProperty(propName, propVal);
        copied += 1;
      }
    }
    return copied;
  }

  /**
   *  Load a properties file.
   *
   *  @param propsFile The file to load properties from.
   *  @return The loaded {@link Properties} object.
   *
   *  @throws IOException When loading the properties fails for some reason.
   */
  public static Properties loadProperties(File propsFile) throws IOException {
    Properties p = new Properties();
    log.debug("Trying to load properties from '%s'", propsFile);
    if (!propsFile.isFile()) {
      throw new FileNotFoundException(propsFile.getPath());
    }
    try (BufferedReader reader = new BufferedReader(new FileReader(propsFile))) {
      p.load(reader);
    }
    log.debug("Loaded %d properties from '%s'", p.size(), propsFile);
    return p;
  }

  // </editor-fold>
  // <editor-fold desc="Utility Methods">

  /**
   *  Copy all properties from <code>src</code> into <code>trg</code>, also when
   *  an equally named property already exists in <code>trg</code>.
   *
   *  @param src The source set of properties.
   *  @param trg The target set of properties.
   *
   *  @return The number of properties copied over. Will be equal to <code>src.size()</code>.
   */
  public static int copy(Properties src, Properties trg) {
    Contract.isNotNull(src);
    Contract.isNotNull(trg);

    int copied = 0;
    for (String propName : src.stringPropertyNames()) {
      trg.setProperty(propName, src.getProperty(propName));
      copied += 1;
    }
    return copied;
  }

  /**
   *  Copy all properties from <code>src</code> that don't exist int <code>trg</code>.
   *
   *  @param src The source set of properties.
   *  @param trg The target set of properties.
   *
   *  @return The number of properties copied over.
   */
  public static int mergeInto(Properties src, Properties trg) {
    Contract.isNotNull(src);
    Contract.isNotNull(trg);

    int copied = 0;
    for (String propName : src.stringPropertyNames()) {
      if (!trg.containsKey(propName)) {
        trg.setProperty(propName, src.getProperty(propName));
        copied += 1;
      }
    }
    return copied;
  }

  /**
   *  Create a Properties object with the same keys and values as the provided map.
   *
   *  @param map Map whose entries to use for initializing a Properties object.
   *  @return A newly created Properties object.
   */
  public static Properties mapToProperties(Map<String, String> map) {
    return CollectionUtils.mapToProperties(map);
  }

  /**
   *  Create a Map&lt;String, String&gt; object with the same keys and values as the provided properties.
   *
   *  @param props Properties object whose entries to use for initializing a Map&lt;String, String&gt; object.
   *  @return A newly created Map&lt;String, String&gt; object.
   *
   *  @throws ClassCastException If any property in props has a non-String key or value.
   */
  public static Map<String, String> propertiesToMap(Properties props) {
    return CollectionUtils.propertiesToMap(props);
  }

  /**
   *  Remove all keys whose name is accepted by the given filter.
   *
   *  @param props Properties object whose entries to remove based on a filter.
   *  @param keyFilter Any key accepted by this filter will be removed.
   *
   *  @return The number of entries that were removed.
   *
   *  @throws ClassCastException If any property in props has a non-String key or value.
   */
  public static int removeMatchingKeys(Properties props, Predicate<String> keyFilter) {
    int removed = 0;
    for (String key : matchingKeys(props, keyFilter)) {
      props.remove(key);
    }
    return removed;
  }

  /**
   *  Remove all keys whose name is accepted by the given filter.
   *
   *  @param props Map whose entries to remove based on a filter.
   *  @param keyFilter Any key accepted by this filter will be removed.
   *
   *  @return The number of entries that were removed.
   */
  public static int removeMatchingKeys(Map<String, String> props, Predicate<String> keyFilter) {
    int removed = 0;
    for (String key : matchingKeys(props, keyFilter)) {
      props.remove(key);
    }
    return removed;
  }

  /**
   *  Return all keys whose name is accepted by the given filter.
   *
   *  @param props Properties object whose keys to select based on a filter.
   *  @param keyFilter Any key accepted by this filter will be removed.
   *
   *  @return A possibly empty list of keys.
   *
   *  @throws ClassCastException If any property in props has a non-String key or value.
   */
  public static List<String> matchingKeys(Properties props, Predicate<String> keyFilter) {
    Contract.isNotNull(props);
    Contract.isNotNull(keyFilter);
    List<String> matchingKeys = new ArrayList<>();
    for (String key : props.stringPropertyNames()) {
      if (keyFilter.test(key)) {
        matchingKeys.add(key);
      }
    }
    return matchingKeys;
  }

  /**
   *  Return all keys whose name is accepted by the given filter.
   *
   *  @param props Map whose keys to select based on a filter.
   *  @param keyFilter Any key accepted by this filter will be removed.
   *
   *  @return A possibly empty list of keys.
   */
  public static List<String> matchingKeys(Map<String, String> props, Predicate<String> keyFilter) {
    Contract.isNotNull(props);
    Contract.isNotNull(keyFilter);
    List<String> matchingKeys = new ArrayList<>();
    for (String key : props.keySet()) {
      if (keyFilter.test(key)) {
        matchingKeys.add(key);
      }
    }
    return matchingKeys;
  }

  /**
   *  Return a new map with only those entries accepted by the keyFilter
   *  and with key names transformed by keyTransform.
   *
   *  @param props Map whose entries to select and whose keys to transform for creating a new map.
   *  @param keyFilter Any key accepted by this filter will be retained and transformed. If <code>null</code>, all entries will be retained.
   *  @param keyTransform A key transformation function. The result will be the new key used in the returned map. If <code>null</code>, original key names will be kept.
   *
   *  @return A possibly empty map with all selected entries, and with transformed key names (but same values).
   *
   *  @throws ClassCastException If any property in props has a non-String key.
   */
  public static Properties filterAndTransform(Properties props, Predicate<String> keyFilter, Function<String, String> keyTransform) {
    Contract.isNotNull(props);
    if (keyFilter == null) {
      keyFilter = (s) -> true;
    }
    if (keyTransform == null) {
      keyTransform = (s) -> s;
    }
    Properties newProps = new Properties();
    for (Map.Entry<Object, Object> entry : props.entrySet()) {
      String key = (String)entry.getKey();
      if (keyFilter.test(key)) {
        String newKey = keyTransform.apply(key);
        newProps.put(newKey, entry.getValue());
      }
    }
    return newProps;
  }

  /**
   *  Return a new map with only those entries accepted by the keyFilter
   *  and with key names transformed by keyTransform.
   *
   *  @param props Map whose entries to select and whose keys to transform for creating a new map.
   *  @param keyFilter Any key accepted by this filter will be retained and transformed. If <code>null</code>, all entries will be retained.
   *  @param keyTransform A key transformation function. The result will be the new key used in the returned map. If <code>null</code>, original key names will be kept.
   *
   *  @return A possibly empty map with all selected entries, and with transformed key names (but same values).
   */
  public static Map<String, String> filterAndTransform(Map<String, String> props, Predicate<String> keyFilter, Function<String, String> keyTransform) {
    Contract.isNotNull(props);
    if (keyFilter == null) {
      keyFilter = (s) -> true;
    }
    if (keyTransform == null) {
      keyTransform = (s) -> s;
    }
    Map<String, String> newProps = new HashMap<>();
    for (Map.Entry<String, String> entry : props.entrySet()) {
      String key = entry.getKey();
      if (keyFilter.test(key)) {
        String newKey = keyTransform.apply(key);
        newProps.put(newKey, entry.getValue());
      }
    }
    return newProps;
  }

  // </editor-fold>
}
