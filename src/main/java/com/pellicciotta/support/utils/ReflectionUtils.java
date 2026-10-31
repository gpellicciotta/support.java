// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.*;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import com.pellicciotta.support.logging.Log;
import com.pellicciotta.support.logging.LogService;

/**
 * Reflection-related utility methods.
 */
public final class ReflectionUtils {
  private static Log log = LogService.fromCurrentClass();

  /** Ensure this class cannot be instantiated. */
  private ReflectionUtils() { }

  /**
   * Determine the first class in the stacktrace that is accepted by the provided filter.
   * So the class containing the method that contains this method's invocation is checked first, and then any classes containing
   * further (going up the stack) method invocations.
   *
   * @param classFilter Filter to determine whether a class is valid or not.
   *
   * @return The containing (directly or indirectly) class that is accepted or null if there is no such class.
   *
   * @throws ClassNotFoundException If determining the class from the stack trace info fails. Should never happen.
   */
  public static Class<?> getCallingClass(Predicate<Class<?>> classFilter) throws ClassNotFoundException {
    StackTraceElement[] stackTrace = ExceptionUtils.getStackTrace(); // Skipping methods in this class
    for (int i = stackTrace.length - 1; i >= 0; i--) {
      StackTraceElement invokingStackElement = stackTrace[i];
      String className = invokingStackElement.getClassName();
      Class<?> cls = Class.forName(className);
      if (classFilter.test(cls)) {
        return cls;
      }
    }
    return null;
  }

  /**
   * List all classes that can be found in the system class path.
   *
   * @return The classes found. Can be empty but never null.
   *
   * @throws IOException When an IO error occurs while trying to find and load a class from the file system.
   */
  public static List<Class<?>> listClassesOnClassPath() throws IOException {
    return listClassesOnClassPath(System.getProperty("java.class.path"), n -> true, Thread.currentThread().getContextClassLoader());
  }

  /**
   * List all classes that can be found in the system class path, whose name pass the filter.
   *
   * @param classFilter A filter on classes to be kept.
   *
   * @return The classes found. Can be empty but never null.
   *
   * @throws IOException When an IO error occurs while trying to find and load a class from the file system.
   */
  public static List<Class<?>> listClassesOnClassPath(Predicate<Class<?>> classFilter) throws IOException {
    return listClassesOnClassPath(System.getProperty("java.class.path"), classFilter, Thread.currentThread().getContextClassLoader());
  }

  /**
   * List all classes that can be found in a list of directories and jar files.
   *
   * @param classPath The {@code System.getProperty("path.separator")} separated path of directory and jar file paths.
   *
   * @return The classes found. Can be empty but never null.
   *
   * @throws IOException When an IO error occurs while trying to find and load a class from the file system.
   */
  public static List<Class<?>> listClassesOnClassPath(String classPath) throws IOException {
    return listClassesOnClassPath(classPath, n -> true, Thread.currentThread().getContextClassLoader());
  }

  /**
   * List all classes that can be found in a list of directories and jar files, whose name pass the filter.
   *
   * @param classPath The {@code System.getProperty("path.separator")} separated path of directory and jar file paths.
   * @param classFilter A filter on classes to be kept.
   * @param classLoader Class loader to use to find+instantiate new classes.
   *
   * @return The classes found. Can be empty but never null.
   *
   * @throws IOException When an IO error occurs while trying to find and load a class from the file system.
   */
  public static List<Class<?>> listClassesOnClassPath(String classPath, Predicate<Class<?>> classFilter, ClassLoader classLoader) throws IOException {
    ArrayList<Class<?>> classes = new ArrayList<>();
    if (classPath.trim().isEmpty()) {
      log.debug("Classpath is empty");
      return classes;
    }
    String[] paths = classPath.split(EnvironmentUtils.getPathSeparator());
    for (String path : paths) {
      log.debug("Searching through path element '%s'", path);
      File file = new File(path);
      if (file.exists()) {
        classes.addAll(listClasses(file, file, classFilter, classLoader));
      }
      else {
        log.debug("Ignoring non-existend path element '%s'", path);
      }
    }
    return classes;
  }

  private static List<Class<?>> listClasses(File root, File file, Predicate<Class<?>> classFilter, ClassLoader classLoader) throws IOException {
    ArrayList<Class<?>> classes = new ArrayList<>();
    Set<Class<?>> foundClasses = new HashSet<>();
    if (file.isDirectory()) {
      log.debug("Searching through directory '%s'", file);
      for (File child : file.listFiles()) {
        for (Class<?> cls : listClasses(root, child, classFilter, classLoader)) {
          if (!foundClasses.contains(cls)) {
            classes.add(cls);
            foundClasses.add(cls);
          }
        }
      }
    }
    else if (file.getName().toLowerCase().endsWith(".jar")) {
      for (Class<?> cls : listClassesInJar(file, classFilter, classLoader)) {
        if (!foundClasses.contains(cls)) {
          classes.add(cls);
          foundClasses.add(cls);
        }
      }
    }
    else if (file.getName().toLowerCase().endsWith(".class")) {
      String className = determineClassName(root, file);
      Class<?> cls = loadClass(className, classLoader);
      if ((cls != null) && classFilter.test(cls) && !foundClasses.contains(cls)) {
        classes.add(cls);
        foundClasses.add(cls);
        log.debug("Found class '%s' in .class file '%s'", cls.getName(), file);
      }
    }
    return classes;
  }

  /**
   * List all classes whose name pass the filer within a given jar file.
   * Uses the current thread's context class loader.
   *
   * @param jarFilePath The path of the jar file to find all classes in.
   * @param classFilter A filter on classes to be kept.
   *
   * @return The classes found. Can be empty but never null.
   *
   * @throws IOException When an IO error occurs while trying to find and load a class from the file system.
   */
  public static List<Class<?>> listClassesInJar(File jarFilePath, Predicate<Class<?>> classFilter) throws IOException {
    return listClassesInJar(jarFilePath, classFilter, Thread.currentThread().getContextClassLoader());
  }

  /**
   * List all classes whose name pass the filter within a given jar file.
   *
   * @param jarFilePath The path of the jar file to find all classes in.
   * @param classFilter A filter on classes to be kept.
   * @param classLoader Class loader to use to find+instantiate new classes.
   *
   * @return The classes found. Can be empty but never null.
   *
   * @throws IOException When an IO error occurs while trying to find and load a class from the file system.
   */
  public static List<Class<?>> listClassesInJar(File jarFilePath, Predicate<Class<?>> classFilter, ClassLoader classLoader) throws IOException {
    Contract.isNotNull("jarFilePath", jarFilePath);
    JarFile jarFile = new JarFile(jarFilePath);
    return listClassesInJar(jarFile, classFilter, classLoader);
  }

  /**
   * List all classes within a given jar file.
   * Uses the current thread's context class loader.
   *
   * @param jarFilePath The jar file to find classes in.
   *
   * @return The classes found. Can be empty but never null.
   *
   * @throws IOException When an IO error occurs while trying to find and load a class from the file system.
   */
  public static List<Class<?>> listClassesInJar(JarFile jarFilePath) throws IOException {
    return listClassesInJar(jarFilePath, n -> true, Thread.currentThread().getContextClassLoader());
  }

  /**
   * List all classes whose name pass the filter within a given jar file.
   *
   * @param jarFile The jar file to find all classes in.
   * @param classFilter A filter on classes to be kept.
   * @param classLoader Class loader to use to find+instantiate new classes.
   *
   * @return The classes found. Can be empty but never null.
   *
   * @throws IOException When an IO error occurs while trying to find and load a class from the file system.
   */
  public static List<Class<?>> listClassesInJar(JarFile jarFile, Predicate<Class<?>> classFilter, ClassLoader classLoader) throws IOException {
    Contract.isNotNull("jarFile", jarFile);
    Contract.isNotNull("classFilter", classFilter);
    Contract.isNotNull("classLoader", classLoader);
    log.debug("Searching classes in jar '%s'", jarFile.getName());
    List<Class<?>> classes = new ArrayList<>();
    Enumeration<JarEntry> entries = jarFile.entries();
    while (entries.hasMoreElements()) {
      JarEntry entry = entries.nextElement();
      String name = entry.getName();
      int extIndex = name.toLowerCase().lastIndexOf(".class");
      if (extIndex > 0) {
        String className = StringUtils.replaceAll(name.substring(0, extIndex), "/", ".");
        Class<?> cls = loadClass(className, classLoader);
        if ((cls != null) && classFilter.test(cls)) {
          classes.add(cls);
          log.debug("Found class '%s' in jar file '%s'", cls.getName(), jarFile.getName());
        }
      }
    }
    return classes;
  }

  private static String determineClassName(File root, File file) {
    StringBuilder sb = new StringBuilder();
    String fileName = file.getName();
    sb.append(fileName.substring(0, fileName.toLowerCase().lastIndexOf(".class")));
    file = file.getParentFile();
    while ((file != null) && !file.equals(root)) {
      sb.insert(0, '.').insert(0, file.getName());
      file = file.getParentFile();
    }
    return sb.toString();
  }

  // Below two methods from: http://dzone.com/snippets/get-all-classes-within-package

  /**
   * List all classes accessible from the current thread's context class loader which belong to the given class,
   * which is the class itself if found, and any contained classes, recursively.
   *
   * @param mainClass The main class.
   *
   * @return The main class + any contained classes. Can be empty but never null.
   *
   * @throws IOException When an IO error occurs while trying to find and load a class from the file system.
   */
  public static List<Class<?>> listClassesInClass(Class<?> mainClass) throws IOException {
    return listClassesInClass(mainClass, n -> true);
  }

  /**
   * List all classes accessible from the current thread's context class loader which belong to the given class,
   * which is the class itself if found, and any contained classes, recursively.
   *
   * @param mainClass The main class.
   * @param classFilter A filter on classes to be kept.
   *
   * @return The main class + any contained classes. Can be empty but never null.
   *
   * @throws IOException When an IO error occurs while trying to find and load a class from the file system.
   */
  public static List<Class<?>> listClassesInClass(Class<?> mainClass, Predicate<Class<?>> classFilter) throws IOException {
    Contract.isNotNull("mainClass", mainClass);
    ArrayList<Class<?>> classes = new ArrayList<>();
    Set<Class<?>> foundClasses = new HashSet<>();
    for (Class<?> cls : listClassesInPackage(mainClass.getPackage().getName(), false)) {
      if ((cls.getCanonicalName() != null) && cls.getCanonicalName().startsWith(mainClass.getCanonicalName()) && !foundClasses.contains(cls)) {
        classes.add(cls);
        foundClasses.add(cls);
      }
    }
    return classes;
  }

  /**
   * List all classes accessible from the context class loader which belong to the given package and all its subpackages.
   * Uses the current thread's context class loader.
   *
   * @param packageName The base package to starting searching in.
   *
   * @return The classes found.
   *
   * @throws IOException When an IO error occurs while trying to find and load a class from the file system.
   */
  public static List<Class<?>> listClassesInPackage(String packageName) throws IOException {
    return listClassesInPackage(packageName, true, n -> true, Thread.currentThread().getContextClassLoader());
  }

  /**
   * List all classes accessible from the context class loader which belong to the given package and optionally subpackages.
   * Uses the current thread's context class loader.
   *
   * @param packageName The base package to starting searching in.
   * @param includeSubpackages Whether to include subpackages.
   *
   * @return The classes found.
   *
   * @throws IOException When an IO error occurs while trying to find and load a class from the file system.
   */
  public static List<Class<?>> listClassesInPackage(String packageName, boolean includeSubpackages) throws IOException {
    return listClassesInPackage(packageName, includeSubpackages, n -> true, Thread.currentThread().getContextClassLoader());
  }

  /**
   * List all classes accessible from the context class loader which belong to the given package and optionally subpackages.
   *
   * @param packageName The base package to starting searching in.
   * @param includeSubpackages Whether to include subpackages.
   * @param classFilter A filter on classes to be kept.
   * @param classLoader Class loader to use to find+instantiate new classes.
   *
   * @return The classes found.
   *
   * @throws IOException When an IO error occurs while trying to find and load a class from the file system.
   */
  public static List<Class<?>> listClassesInPackage(String packageName, boolean includeSubpackages, Predicate<Class<?>> classFilter, ClassLoader classLoader) throws IOException {
    Contract.isNotNull("packageName", packageName);
    Contract.isNotNull("classFilter", classFilter);
    Contract.isNotNull("classLoader", classLoader);
    log.debug("Searching classes in package '%s'", packageName);
    if (includeSubpackages) {
      Predicate<Class<?>> packageOnlyFilter = classFilter.and(cls -> cls.getPackage().getName().startsWith(packageName));
      return listClassesOnClassPath(System.getProperty("java.class.path"), packageOnlyFilter, classLoader);
    }
    else {
      Predicate<Class<?>> packageOnlyFilter = classFilter.and(cls -> cls.getPackage().getName().equals(packageName));
      return listClassesOnClassPath(System.getProperty("java.class.path"), packageOnlyFilter, classLoader);
    }
  }

  /**
   * List all classes in a given directory and optionally its subdirs.
   * Uses the current thread's context class loader.
   *
   * @param directory The base directory to look into for .class files.
   * @param packageName The package name for classes found inside the base directory.
   * @param recurseIntoSubDirs Whether to also list classes found in sub directories.
   *
   * @return The classes found.
   */
  public static List<Class<?>> listClassesInDirectory(File directory, String packageName, boolean recurseIntoSubDirs) {
    return listClassesInDirectory(directory, packageName, recurseIntoSubDirs, n -> true, Thread.currentThread().getContextClassLoader());
  }

  /**
   * List all classes in a given directory and optionally its subdirectories.
   *
   * @param directory The base directory to look into for .class files.
   * @param packageName The package name for classes found inside the base directory.
   * @param recurseIntoSubDirs Whether to also list classes found in sub directories.
   * @param classFilter A filter on classes to be kept.
   * @param classLoader Class loader to use to find+instantiate new classes.
   *
   * @return The classes found.
   */
  public static List<Class<?>> listClassesInDirectory(File directory, String packageName, boolean recurseIntoSubDirs, Predicate<Class<?>> classFilter, ClassLoader classLoader) {
    Contract.isNotNull("directory", directory);
    Contract.isNotNull("packageName", packageName);
    Contract.isNotNull("classFilter", classFilter);
    Contract.isNotNull("classLoader", classLoader);
    LogService.info("Searching classes in directory '%s'", directory);
    log.debug("Searching classes in directory '%s'", directory);
    List<Class<?>> classes = new ArrayList<>();
    Set<Class<?>> foundClasses = new HashSet<>();
    if (!directory.exists()) {
      return classes;
    }
    File[] files = directory.listFiles();
    for (File file : files) {
      if (file.isDirectory() && recurseIntoSubDirs) {
        assert !file.getName().contains(".");
        for (Class<?> cls : listClassesInDirectory(file, packageName + "." + file.getName(), true, classFilter, classLoader)) {
          if ((cls != null) && !foundClasses.contains(cls)) {
            classes.add(cls);
            foundClasses.add(cls);
            log.debug("Found class '%s' in directory '%s'", cls.getName(), file);
          }
        }
      }
      else if (file.getName().endsWith(".class")) {
        String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
        Class<?> cls = loadClass(className, classLoader);
        if ((cls != null) && classFilter.test(cls) && !foundClasses.contains(cls)) {
          classes.add(cls);
          foundClasses.add(cls);
          log.debug("Found class '%s' in .class file '%s'", cls.getName(), file);
        }
      }
    }
    return classes;
  }

  private static Class<?> loadClass(String className, ClassLoader classLoader) {
    try {
      log.debug("Trying to load class '%s'", className);
      Class<?> cls = Class.forName(className, false /* to avoid System.exit or other very bad things from happening due to code in static initializers */, classLoader);
      @SuppressWarnings("unused")
      String clsName = cls.getCanonicalName(); // Because this can lead to MalformedClassName, in which case we want to discard this class
      return cls;
    }
    catch (Throwable e) {
      log.debug("Failed to load class '%s': %s", className, e);
      return null;
    }
  }

  /**
   * Determine the location of the file containing the class definition.
   *
   * @param clazz The class to determine the .class file location for.
   *
   * @return The path of the .class file containing the provided class or null if it cannot be determined.
   */
  public static String getClassFile(Class<?> clazz) {
    // See also: https://stackoverflow.com/questions/320542/how-to-get-the-path-of-a-running-jar-file
    Contract.isNotNull("clazz", clazz);
    String path = getLocationOf(clazz);
    if (path != null) {
      return path;
    }
    URL resource = clazz.getResource(clazz.getSimpleName() + ".class");
    if (resource != null) {
      return resource.getPath();
    }
    // Fall-back to enclosing class
    Class<?> enclosingClass = clazz.getEnclosingClass();
    while (enclosingClass != null) {
      clazz = enclosingClass;
      path = getLocationOf(clazz);
      if (path != null) {
        return path;
      }
      resource = clazz.getResource(clazz.getSimpleName() + ".class");
      if (resource != null) {
        return resource.getPath();
      }
      enclosingClass = clazz.getEnclosingClass();
    }
    return null;
  }

  // Adhoc test:
  //public static void main(String[] args) {
  //  System.out.println(getClassFile(Date.class));
  //  System.out.println(getClassFile(ReflectionUtils.class));
  //  System.out.println(getClassFile(Jetty.class));
  //}

  /**
   *  Get the file system location of the specified class.
   *
   *  @param clazz The class to find the file-system location for: the location it was loaded from.
   *  @return The file-system path to the class-file representing the class or <code>null</code> if unknown.
   */
  private static String getLocationOf(Class<?> clazz) {
    try {
      ProtectionDomain pd = clazz.getProtectionDomain();
      if (pd == null) {
        log.debug("getLocationOf(%s) failed: no protection-domain", clazz);
        return null;
      }
      CodeSource cs = pd.getCodeSource();
      if (cs == null) {
        log.debug("getLocationOf(%s) failed: no code source", clazz);
        return null;
      }
      URL url = cs.getLocation();
      if (url == null) {
        log.debug("getLocationOf(%s) failed: no URL", clazz);
        return null;
      }
      return new File(url.toURI()).getPath();
    }
    catch (Exception e) {
      log.debug("getLocationOf(%s) failed: %s", clazz, e);
      return null;
    }
  }

  /**
   * Determine the root directory in which a class' .class file resides.
   * Root directory means: the directory containing subdirectories representing the class' package components.
   *
   * @param clazz The class to find the file system directory location for.
   *
   * @return The root directory path in which to find the .class file or the .class file's root package directory
   *         (unless the class has no package).
   */
  public static String getClassFileDirectory(Class<?> clazz) {
    String classFile = getClassFile(clazz);
    File dir = new File(classFile).getParentFile();
    int packageDirs = clazz.getPackage().getName().split("[.]").length;
    while ((packageDirs > 0) && (dir.getParentFile() != null)) {
      dir = dir.getParentFile();
      packageDirs--;
    }
    return dir.getPath();
  }

  /**
   *  Determine the build time of a class by looking at its .class file creation time.
   *
   *  @param clazz The class to determine the build time for.
   *
   *  @return The build-time determined by the .class creation time or null if it cannot be determined.
   */
  public static Instant getClassBuildTime(Class<?> clazz) {
    // Based on: http://stackoverflow.com/questions/3336392/java-print-time-of-last-compilation
    long lastModified = -1;
    // Find top-level (i.e. non-nested) class
    while (clazz.getDeclaringClass() != null) {
      clazz = clazz.getDeclaringClass();
    }
    while (clazz.getEnclosingClass() != null) {
      clazz = clazz.getEnclosingClass();
    }
    URL resource = clazz.getResource(clazz.getSimpleName() + ".class");
    if (resource != null) {
      if (resource.getProtocol().equals("file")) {
        try {
          lastModified = new File(resource.toURI()).lastModified();
        }
        catch (URISyntaxException ignored) {
          // Empty on purpose
        }
      }
      else if (resource.getProtocol().equals("jar")) {
        String path = resource.getPath();
        lastModified = new File(path.substring(5 /* skip protocol */, path.indexOf("!"))).lastModified();
      }
      else if (resource.getProtocol().equals("zip")) {
        String path = resource.getPath();
        File jarFileOnDisk = new File(path.substring(0, path.indexOf("!")));
        try (JarFile jf = new JarFile(jarFileOnDisk)) {
          ZipEntry ze = jf.getEntry(path.substring(path.indexOf("!") + 2)); // Skip the ! and the /
          lastModified = ze.getTime();
        }
        catch (IOException | RuntimeException ignored) {
          // Empty on purpose
        }
      }
    }
    if (lastModified != -1) {
      return Instant.ofEpochMilli(lastModified);
    }
    else {
      return null;
    }
  }

  /**
   * List all fields defined on the provided type (or inherited from super types) that have a type can be assigned to the provided type.
   *
   * @param type The type to look into for annotated fields. Also super (i.e. parent) types will be inspected.
   * @param assignableType The type the field type needs to be assign-compatible with, other words <code>assignableType.isAssignableFrom(field.getType())</code> must be <code>true</code>.
   *
   * @return The possibly empty list of fields that have the requested type-compatibility. All fields, irrespective of access modifier, will be returned.
   */
  public static List<Field> listAssignableFields(Class<?> type, Class<?> assignableType) {
    // From: http://stackoverflow.com/questions/6593597/java-seek-a-method-with-specific-annotation-and-its-annotation-element
    final List<Field> fields = new ArrayList<>();
    Class<?> clazz = type;
    while (clazz != Object.class) { // need to iterated thought hierarchy in order to retrieve methods from above the current instance
      // iterate though the list of methods declared in the class represented by klass variable, and add those annotated with the specified annotation
      final List<Field> allFields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
      for (final Field field : allFields) {
        if (assignableType.isAssignableFrom(field.getType())) {
          fields.add(field);
        }
      }
      // Move to the base class in the hierarchy in search for more methods
      clazz = clazz.getSuperclass();
    }
    return fields;
  }

  /**
   * List all fields defined on the provided type (or inherited from super types) that have an annotation of the provided type.
   *
   * @param type The type to look into for annotated fields. Also super (i.e. parent) types will be inspected.
   * @param annotation The type of annotation to look for.
   *
   * @return The possibly empty list of fields that have the requested annotation. All fields, irrespective of access modifier, will be returned.
   */
  public static List<Field> listAnnotatedFields(Class<?> type, Class<? extends Annotation> annotation) {
    // From: http://stackoverflow.com/questions/6593597/java-seek-a-method-with-specific-annotation-and-its-annotation-element
    final List<Field> fields = new ArrayList<>();
    Class<?> clazz = type;
    while (clazz != Object.class) { // need to iterated thought hierarchy in order to retrieve methods from above the current instance
      // iterate though the list of methods declared in the class represented by klass variable, and add those annotated with the specified annotation
      final List<Field> allFields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
      for (final Field field : allFields) {
        if (field.isAnnotationPresent(annotation)) {
          field.getAnnotation(annotation);
          fields.add(field);
        }
      }
      // Move to the base class in the hierarchy in search for more methods
      clazz = clazz.getSuperclass();
    }
    return fields;
  }

  /**
   * List all methods defined on the provided type (or inherited from super types) that have an annotation of the provided type.
   *
   * @param type The type to look into for annotated methods. Also super (i.e. parent) types will be inspected.
   * @param annotation The type of annotation to look for. For repeating annotations, pass the element annotation type.
   *
   * @return The possibly empty list of methods that have the requested annotation. All methods, irrespective of access modifier, will be returned.
   */
  public static List<Method> listAnnotatedMethods(Class<?> type, Class<? extends Annotation> annotation) {
    // Check for repeating
    Class<? extends Annotation> repeatedAnnotation = null;
    if (annotation.isAnnotationPresent(Repeatable.class)) {
      repeatedAnnotation = annotation.getAnnotation(Repeatable.class).value();
    }
    // From: http://stackoverflow.com/questions/6593597/java-seek-a-method-with-specific-annotation-and-its-annotation-element
    final List<Method> methods = new ArrayList<>();
    Class<?> clazz = type;
    while (clazz != Object.class) { // need to iterated thought hierarchy in order to retrieve methods from above the current instance
      // iterate though the list of methods declared in the class represented by klass variable, and add those annotated with the specified annotation
      final List<Method> allMethods = new ArrayList<>(Arrays.asList(clazz.getDeclaredMethods()));
      for (final Method method : allMethods) {
        if (method.isAnnotationPresent(annotation)) {
          methods.add(method);
        }
        else if ((repeatedAnnotation != null) && (method.isAnnotationPresent(repeatedAnnotation))) {
          methods.add(method);
        }
      }
      // Move to the upper class in the hierarchy in search for more methods
      clazz = clazz.getSuperclass();
    }
    return methods;
  }

  /**
   * Create a class loader that can load classes from the list of directories and jar files in the class path.
   *
   * @param classPath The {@code System.getProperty("path.separator")} separated path of directory and jar file paths.
   *
   * @return The {@link ClassLoader}: will be the current context classloader if <code>classPath</code> is empty.
   */
  public static ClassLoader createClassLoaderFromClassPath(String classPath) {
    assert classPath != null;
    if (classPath.trim().isEmpty()) {
      return Thread.currentThread().getContextClassLoader();
    }
    String[] paths = classPath.split(Pattern.quote(EnvironmentUtils.getPathSeparator()));
    return createClassLoaderFromClassPath(paths);
  }

  /**
   * Create a class loader that can load classes from the list of directories and jar files in the class path.
   *
   * @param classPathComponents The list of directory and jar file paths to use for class loading.
   *
   * @return The {@link ClassLoader}: will be the current context classloader if <code>classPathComponents</code> is empty.
   */
  public static ClassLoader createClassLoaderFromClassPath(String[] classPathComponents) {
    assert classPathComponents != null;
    if (classPathComponents.length == 0) {
      return Thread.currentThread().getContextClassLoader();
    }
    // Configure the classloader
    List<URL> locations = new ArrayList<>();
    Set<URL> configuredUrls = new HashSet<>();
    for (String loc : classPathComponents) {
      try {
        File part = new File(loc);
        URL url = part.toURI().toURL();
        if (!configuredUrls.contains(url)) {
          locations.add(url);
          configuredUrls.add(url);
          log.debug("Adding URL to list of locations to search for classes: '%s'", url);
        }
      }
      catch (MalformedURLException e) {
        log.error("Location '%s' is invalid and will be ignored by this class loader", loc);
      }
    }
    return new URLClassLoader(locations.toArray(new URL[0]));
  }
}
