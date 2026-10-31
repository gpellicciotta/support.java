// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A collection of File related utility methods.
 */
public final class FileUtils {
  /** Prevent instantiation */
  private FileUtils() { }

  // <editor-fold desc="Constants">

  /**
   *  File filter that will accept all paths.
   */
  public static final FileFilter NO_FILTER = new FileFilter() {
    @Override
    public boolean accept(File f) {
      return true;
    }
  };

  /**
   *  File filter that will only accept paths for which {@link File#isFile()} returns true.
   */
  public static final FileFilter FILES_ONLY_FILTER = new FileFilter() {
    @Override
    public boolean accept(File f) {
      return f.isFile();
    }
  };

  /**
   *  File filter that will only accept paths for which {@link FileUtils#isDirectory(File)} returns true.
   */
  public static final FileFilter DIRECTORIES_ONLY_FILTER = new FileFilter() {
    @Override
    public boolean accept(File f) {
      return isDirectory(f);
    }
  };

  // </editor-fold>
  // <editor-fold desc="Operations">

  /**
   * Return a list of all files satisfying fileFilter, that are contained in rootDir, either directly or indirectly (i.e. in
   * sub-directories of rootDir). The rootDir itself will never be included in the list of files.
   *
   * @param rootDir The root directory to start searching for files.
   * @param fileFilter Filter for determining whether a file should be accepted and returned, or not. All accepted files (which can
   *          be directories too, of course) will be returned. There is no need to accept child-directories for the sole purpose of
   *          getting a chance to accept their files: that's what this method is doing for you.
   *
   * @return The list of accepted file objects (which can be a mix of actual files and/or directories). Will be empty if nothing got
   *         accepted by the fileFilter.
   *
   * @throws IllegalArgumentException If rootDir is not an existing directory or fileFilter is null.
   */
  public static List<File> listFilesRecursively(File rootDir, FileFilter fileFilter) {
    Contract.isNotNull("rootDir", rootDir);
    Contract.isTrue("rootDir.isDirectory()", rootDir.isDirectory());
    Contract.isNotNull("fileFilter", fileFilter);

    List<File> accepted = new ArrayList<>();
    for (File file : rootDir.listFiles()) {
      if (fileFilter.accept(file)) {
        accepted.add(file);
      }
      if (file.isDirectory()) {
        accepted.addAll(listFilesRecursively(file, fileFilter));
      }
    }
    return accepted;
  }

  /**
   * A normalized version of the path:<ul>
   *   <li>always using / as file separator</li>
   *   <li>with a trailing / if the path denotes an existing directory</li>
   *   <li>on file system that are case insensitive, with all chars turned into lowercase</li></ul>
   *
   * @param path The file path to be normalized.
   *
   * @return A normalized version of the path or null if path was null.
   */
  public static String getNormalizedPath(String path) {
    if (path == null) {
      return null;
    }
    String normPath = path.replace('\\', '/');
    if (isDirectory(path) && !normPath.endsWith("/")) {
      normPath = normPath + "/";
    }
    if (EnvironmentUtils.isWindows()) {
      normPath = normPath.toLowerCase();
    }
    return normPath;
  }

  /**
   * A canonical and absolute version of the path, optionally with / as file separator. Paths denoting directories will end with the
   * file separator.
   *
   * @param path The file path to be canonicalized.
   * @param forwardSeparators Whether to use / as separator for all path components.
   *
   * @return Canonical version of path.
   *
   * @throws IllegalArgumentException When the argument is null.
   * @throws IOException When getting the path fails due to a file system http error.
   */
  public static String getCanonicalPath(String path, boolean forwardSeparators) throws IOException {
    Contract.isNotNull("path", path);
    // Note: we cannot just use getCanonicalPath(String, boolean) here since the isDirectory(String) method is better than isDirectory(File)
    String canonPath = new File(path).getCanonicalPath();
    if (isDirectory(path) && !(canonPath.endsWith("/") || canonPath.endsWith("\\"))) {
      canonPath = canonPath + File.separator;
    }
    if (forwardSeparators) {
      return canonPath.replace('\\', '/');
    }
    else {
      return canonPath;
    }
  }

  /**
   * A canonical and absolute version of the path, optionally with / as file separator. Paths denoting directories will end with the
   * file separator.
   *
   * @param path The file path to be canonicalized.
   * @param forwardSeparators Whether to use / as separator for all path components.
   *
   * @return Canonical version of path.
   *
   * @throws IllegalArgumentException When the argument is null.
   * @throws IOException When getting the path fails due to a file system http error.
   */
  public static String getCanonicalPath(File path, boolean forwardSeparators) throws IOException {
    Contract.isNotNull("path", path);
    String canonPath = path.getCanonicalPath();
    if (isDirectory(path) && !(canonPath.endsWith("/") || canonPath.endsWith("\\"))) {
      canonPath = canonPath + File.separator;
    }
    if (forwardSeparators) {
      return canonPath.replace('\\', '/');
    }
    else {
      return canonPath;
    }
  }

  /**
   *  Same as <code>getCanonicalPath(path, false)</code>.
   *
   *  @param path The path to make canonical.
   *  @throws IOException If determining the path fails.
   */
  public static String getCanonicalPath(String path) throws IOException {
    return getCanonicalPath(path, false);
  }

  /**
   *  Same as <code>getCanonicalPath(path, false)</code>
   *
   *  @param path The path to make canonical.
   *  @throws IOException If determining the path fails.
   */
  public static File getCanonicalPath(File path) throws IOException {
    Contract.isNotNull("path", path);
    return path.getCanonicalFile();
  }

  /**
   *  Check whether the canonical form of <code>file</code> lies within (or equals) the canonical form of
   *  <code>baseDir</code>. Typically used to guard against a path escaping a configured base directory
   *  (e.g. via <code>..</code> segments).
   *
   *  @param file The file to check.
   *  @param baseDir The base directory that <code>file</code> is expected to be contained in.
   *
   *  @return Whether <code>file</code>'s canonical path equals or starts with <code>baseDir</code>'s canonical path.
   *
   *  @throws IllegalArgumentException When any argument is null.
   *  @throws IOException When canonicalizing either path fails.
   */
  public static boolean isContainedIn(File file, File baseDir) throws IOException {
    Contract.isNotNull("file", file);
    Contract.isNotNull("baseDir", baseDir);
    String canonicalFile = file.getCanonicalPath();
    String canonicalBase = baseDir.getCanonicalPath();
    return canonicalFile.equals(canonicalBase) || canonicalFile.startsWith(canonicalBase + File.separator);
  }

  /** Overload of {@link #isContainedIn(File, File)} */
  public static boolean isContainedIn(String file, String baseDir) throws IOException {
    Contract.isNotNull("file", file);
    Contract.isNotNull("baseDir", baseDir);
    return isContainedIn(new File(file), new File(baseDir));
  }

  /**
   * Whether the specified file name denotes a directory.
   *
   * @param path The file path whose type needs to be checked.
   *
   * @return True if the fileName exists and is a directory or if the fileName path ends with a directory separator.
   *
   * @throws IllegalArgumentException When the argument is null.
   */
  public static boolean isDirectory(String path) {
    Contract.isNotNull("path", path);
    return path.endsWith("/") || path.endsWith("\\") || new File(path).isDirectory();
  }

  /** Overload of {@link #isDirectory(String)} */
  public static boolean isDirectory(File path) {
    Contract.isNotNull("path", path);
    String pathStr = path.toString();
    return path.isDirectory() || pathStr.endsWith("/") || pathStr.endsWith("\\");
  }

  /**
   * Whether the specified file name exists on the file system.
   *
   * @param fileName The file name whose existence needs to be checked.
   *
   * @return True of exists or false otherwise.
   *
   * @throws IllegalArgumentException When the argument is null.
   */
  public static boolean exists(String fileName) {
    Contract.isNotNull("fileName", fileName);
    return exists(new File(fileName));
  }

  /**
   *  Overload of {@link #exists(String)}
   *
   *  @param file The file path to check.
   *  @return If this file path exists.
   */
  public static boolean exists(File file) {
    Contract.isNotNull("file", file);
    return file.exists();
  }

  /**
   * Determine the list of directory names that are specified in fileName, if any. The directory names are determined based on the
   * actual fileName string. This method will not try to canonicalize fileName or make its path absolute. This methods will also not
   * return the last part of fileName if fileName is a directory itself.
   *
   * @param fileName The file name to determine the directory name for.
   *
   * @return The list of directory names that can be found in fileName, in order. This list can be empty.
   *
   * @throws IllegalArgumentException When the argument is null.
   */
  public static String[] getDirectoryNames(String fileName) {
    return getDirectoryNames(fileName, true);
  }

  /** Overload of {@link #getDirectoryNames(String)} */
  public static String[] getDirectoryNames(File fileName) {
    return getDirectoryNames(fileName, true);
  }

  /**
   * Get the parent directory part of a fileName path.
   * <p>
   * Both \\ and / will be recognized as directory separators (even on POSIX systems).
   *
   * @param fileName The file name to determine the directory name for.
   *
   * @return The parent directory of fileName or null if none.
   *
   * @throws IllegalArgumentException When the argument is null.
   */
  public static String getDirectory(String fileName) {
    Contract.isNotNull("fileName", fileName);

    StringBuilder dnb = new StringBuilder();
    String prefix = getPrefix(fileName);
    if (prefix != null) {
      fileName = fileName.substring(prefix.length());
      dnb.append(prefix);
    }
    String[] parts = fileName.split("[/\\\\]");
    for (int i = 0; i < (parts.length - 1); i++) { // Add all but last
      dnb.append(parts[i]);
      dnb.append(EnvironmentUtils.getFileSeparator());
    }
    if (dnb.length() == 0) {
      return null;
    }
    return dnb.toString();
  }

  /** Overload of {@link #getDirectory(String)} */
  public static String getDirectory(File fileName) {
    Contract.isNotNull("fileName", fileName);

    return getDirectory(fileName.getPath());
  }

  /**
   * Determine the list of directory names that are specified in fileName, if any. The directory names are determined based on the
   * actual fileName string. This method will not try to canonicalize fileName or make its path absolute.
   * <p>
   * Both \\ and / will be recognized as directory separators (even on POSIX systems).
   *
   * @param fileName The file name to determine the directory name for.
   * @param onlyParentDirectories Don't return the last part of fileName if fileName specifies a directory.
   *
   * @return The list of directory names that can be found in fileName, in order. This list can be empty. If based on an absolute
   *         fileName, the first element of the returned array will be the file-system prefix (drive letter on Windows or / on POSIX
   *         or // or \\ for UNC Paths).
   *
   * @throws IllegalArgumentException When the argument is null.
   */
  public static String[] getDirectoryNames(File fileName, boolean onlyParentDirectories) {
    Contract.isNotNull("fileName", fileName);

    // NOTE: actual impl. in getDirectoryNames(String, boolean) since new File(...) will strip ending directory separator,
    // meaning we have less clues to determine whether the path actually denotes a directory
    return getDirectoryNames(fileName.toString(), onlyParentDirectories);
  }

  /** Overload of {@link #getDirectoryNames(String,boolean)} */
  public static String[] getDirectoryNames(String fileName, boolean onlyParentDirectories) {
    Contract.isNotNull("fileName", fileName);

    List<String> dirNames = new ArrayList<>();
    String prefix = getPrefix(fileName);
    if (prefix != null) {
      fileName = fileName.substring(prefix.length());
      dirNames.add(prefix);
    }
    String[] parts = fileName.split("[/\\\\]");
    for (int i = 0; i < (parts.length - 1); i++) { // Add all but last
      dirNames.add(parts[i]);
    }
    if (!onlyParentDirectories && isDirectory(fileName)) { // Add last, if dir
      dirNames.add(parts[parts.length - 1]);
    }
    return dirNames.toArray(new String[] {});
  }

  /**
   * Determine the common base path of both files.
   *
   * @param file1 First file to compare
   * @param file2 Second file to compare
   *
   * @return Either null if there is no common base path or the longest common base path of both files.
   * @throws IOException When the operation fails due to a file system http error.
   */
  public static File getCommonBasePath(File file1, File file2) throws IOException {
    Contract.isNotNull("file1", file1);
    Contract.isNotNull("file2", file2);

    String[] file1Dirs = getDirectoryNames(file1);
    String[] file2Dirs = getDirectoryNames(file2);
    int maxCommonPartLen = Math.min(file1Dirs.length, file2Dirs.length);
    File commonBasePath = null;
    for (int i = 0; i < maxCommonPartLen; i++) {
      if (file1Dirs[i].equals(file2Dirs[i])) {
        commonBasePath = new File(commonBasePath, file1Dirs[i]);
      }
      else {
        break;
      }
    }
    return commonBasePath;
  }

  /**
   * Create a file path that is relative to the provided baseDirPath.
   *
   * @param filePath The file path to make relative.
   * @param baseDirPath Path to a base directory to be the starting point of the relative path.
   *
   * @return A path relative to baseDirPath or the original filePath if it is not contained in baseDirPath.
   * @throws IOException When the operation fails due to a file system http error.
   */
  public static File getRelativePath(File filePath, File baseDirPath) throws IOException {
    return new File(getRelativePath(filePath.toString(), baseDirPath.toString()));
  }

  /**
   * Create a file path that is relative to the provided baseDirPath.
   *
   * @param filePath The file path to make relative.
   * @param baseDirPath Path to a base directory to be the starting point of the relative path.
   *
   * @return A path relative to baseDirPath or the original filePath if it is not contained in baseDirPath.
   * @throws IOException When the operation fails due to a file system http error.
   */
  public static String getRelativePath(String filePath, String baseDirPath) throws IOException {
    Contract.isNotNull("filePath", filePath);
    Contract.isNotNull("baseDirPath", baseDirPath);
    Contract.isTrue("isDirectory(baseDirPath) || !exists(baseDirPath)", isDirectory(baseDirPath) || !exists(baseDirPath));
    // Special case: if filePath == baseDirPath, the relative path is .
    if (new File(filePath).equals(new File(baseDirPath))) {
      return ".";
    }
    // Otherwise check the common parts:
    String[] fileDirs = getDirectoryNames(getCanonicalPath(filePath), false);
    String[] baseDirs = getDirectoryNames(getCanonicalPath(baseDirPath), false);
    // Determine common parts
    int i = 0;
    while ((i < baseDirs.length) && (i < fileDirs.length) && (fileDirs[i].equals(baseDirs[i]))) {
      i++;
    }
    i = i - 1;
    // Determine relative path
    if (i < 0) { // No common part
      return filePath;
    }
    else if (i == (baseDirs.length - 1)) { // Purely nested: filePath is under (hierarchically) baseDirPath
      /*
       * Example 1 with filePath a file: baseDirPath: z/a/ -> [z][a] filePath: z/a/bb.txt -&gt; [z][a] return: bb.txt
       *
       * Example 2 with filePath a directory: baseDirPath: z/a/ -> [z][a] filePath: z/a/b -&gt; [z][a][b] return: b
       */
      String relPath = "";
      for (int j = i + 1; j < fileDirs.length; j++) {
        relPath = relPath + fileDirs[j] + File.separator;
      }
      if (!isDirectory(filePath)) {
        return relPath + new File(filePath).getName();
      }
      else {
        return relPath;
      }
    }
    else { // filePath is above (hierarchically) baseDirPath
      /*
       * Example 1 with filePath a file: baseDirPath: z/a/b/c/d -> [z][a][b][c] filePath: z/a/d/bb.txt -&gt; [z][a] return:
       * ../../../d/bb.txt
       *
       * Example 2 with filePath a directory: baseDirPath: z/a/b/c/d -> [z][a][b][c] filePath: z/a -&gt; [z][a] return: ../../..
       */
      String relPath = "";
      for (int j = baseDirs.length - 1; j > i; j--) {
        relPath = relPath + ".." + File.separator;
      }
      for (int j = i + 1; j < fileDirs.length; j++) {
        relPath = relPath + fileDirs[j] + File.separator;
      }
      if (!isDirectory(filePath)) {
        return relPath + new File(filePath).getName();
      }
      else {
        return relPath;
      }
    }
  }

  private static final Pattern WINDOWS_PREFIX_PATTERN = Pattern.compile("^([a-zA-Z][:][/\\\\]?)");

  /**
   * The prefix of the specified file name is system dependent.
   *
   * The prefix concept is used to handle root directories on UNIX platforms, and drive specifiers, root directories and UNC
   * pathnames on Microsoft Windows platforms, as follows: For UNIX platforms, the prefix of an absolute pathname is always "/".
   * Relative pathnames have no prefix. The abstract pathname denoting the root directory has the prefix "/" and an empty name
   * sequence. For Microsoft Windows platforms, the prefix of a pathname that contains a drive specifier consists of the drive
   * letter followed by ":" and possibly followed by "\\" if the pathname is absolute. The prefix of a UNC pathname is "\\\\"; the
   * hostname and the share name are the first two names in the name sequence. A relative pathname that does not specify a drive has
   * no prefix.
   *
   * @param fileName The file name to extract the prefix from.
   *
   * @return The prefix for this file (either /, \\ or //, or [A-Z]:/ or [A-Z]:\) or null if it has no prefix.
   *
   * @throws IllegalArgumentException When the argument is null.
   */
  public static String getPrefix(String fileName) {
    Contract.isNotNull("fileName", fileName);
    String prefix = null;
    if (fileName.replace('\\', '/').startsWith("//")) { // Windows UNC path
      prefix = fileName.substring(0, 2);
    }
    else if (fileName.startsWith("/")) {
      prefix = "/";
    }
    else {
      Matcher m = FileUtils.WINDOWS_PREFIX_PATTERN.matcher(fileName);
      if (m.find()) {
        prefix = m.group(1);
      }
    }
    return prefix;
  }

  /** Overload of {@link #getPrefix(String)} */
  public static String getPrefix(File file) {
    Contract.isNotNull("file", file);
    return getPrefix(file.getPath());
  }

  /**
   * The base name of the specified file name is the last part of the file path specified by fileName, but with the extension
   * stripped off.
   *
   * @param fileName The file name to extract the base name from.
   *
   * @return The base name for this file. This may equal fileName of fileName was already a relative name with no path components
   *         and no extension.
   *
   * @throws IllegalArgumentException When the argument is null.
   */
  public static String getBaseName(String fileName) {
    Contract.isNotNull("fileName", fileName);
    return getBaseName(new File(fileName));
  }

  /** Overload of {@link #getBaseName(String)} */
  public static String getBaseName(File file) {
    Contract.isNotNull("file", file);
    String name = file.getName(); // Making sure to only check the file name part
    int index = name.lastIndexOf('.');
    if (index > 0) {
      return name.substring(0, index);
    }
    else {
      return name;
    }
  }

  /**
   * The file name but with the extension taken off.
   * The extension is taken to be the string found after the last dot ('.') character
   * in the last part of the file path specified by fileName. If the last part of the file path starts with a dot, this dot is never
   * considered the dot starting an extension.
   *
   * @param fileName The file name to strip the extension from.
   *
   * @return The file name without extension.
   *
   * @throws IllegalArgumentException When the argument is null.
   */
  public static String getNameWithoutExtension(String fileName) {
    Contract.isNotNull("fileName", fileName);
    String fileExt = getExtension(fileName);
    if (fileExt.isEmpty()) {
      return fileName;
    }
    else {
      return fileName.substring(0, fileName.length() - fileExt.length() - 1 /* for the dot */);
    }
  }

  /** Overload of {@link #getNameWithoutExtension(String)} */
  public static String getNameWithoutExtension(File file) {
    Contract.isNotNull("file", file);
    return getNameWithoutExtension(file.getPath());
  }

  /**
   * The file extension of the specified file name. The extension is taken to be the string found after the last dot ('.') character
   * in the last part of the file path specified by fileName. If the last part of the file path starts with a dot, this dot is never
   * considered the dot starting an extension.
   *
   * @param fileName The file name to extract the extension from.
   *
   * @return The file extension (without leading dot) or the empty string if none could be found.
   *
   * @throws IllegalArgumentException When the argument is null.
   */
  public static String getExtension(String fileName) {
    Contract.isNotNull("fileName", fileName);
    return getExtension(new File(fileName));
  }

  /** Overload of {@link #getExtension(String)} */
  public static String getExtension(File file) {
    Contract.isNotNull("file", file);
    String name = file.getName(); // Making sure to only check the name part
    int index = name.lastIndexOf('.');
    if (index > 0) {
      return name.substring(index + 1);
    }
    return "";
  }

  /**
   * Return the contents of a text file in one operation.
   *
   * @param fileName The name of the file whose contents need to be returned.
   *
   * @return The file contents.
   *
   * @throws IllegalArgumentException When the argument is null.
   * @throws IOException When the specified file doesn't exist.
   */
  public static String readTextFile(String fileName) throws IOException {
    Contract.isNotNull("fileName", fileName);
    return readTextFile(new File(fileName));
  }

  /** Overload of {@link #readTextFile(String)} */
  public static String readTextFile(File file) throws IOException {
    Contract.isNotNull("file", file);
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      return readTextFile(reader);
    }
  }

  /** Overload of {@link #readTextFile(String)} */
  public static String readTextFile(Reader reader) throws IOException {
    Contract.isNotNull("reader", reader);
    if (reader instanceof BufferedReader) {
      return readTextFile((BufferedReader)reader);
    }
    else { // Wrap if needed, for efficiency
      try (BufferedReader bufReader = new BufferedReader(reader)) {
        return readTextFile(bufReader);
      }
    }
  }

  /** Overload of {@link #readTextFile(String)} */
  private static String readTextFile(BufferedReader reader) throws IOException {
    Contract.isNotNull("reader", reader);
    StringBuilder sb = new StringBuilder(1024);
    char[] chars = new char[1024];
    int numRead = 0;
    while ((numRead = reader.read(chars)) > -1) {
      sb.append(String.valueOf(chars, 0, numRead));
    }
    return sb.toString();
  }

  /**
   * Return the contents of a text file in one operation.
   *
   * @param fileName The name of the file whose contents need to be returned.
   *
   * @return The file contents, split up in individual text lines.
   *
   * @throws IllegalArgumentException When the argument is null.
   * @throws IOException When the specified file doesn't exist.
   */
  public static List<String> readAllTextLines(String fileName) throws IOException {
    Contract.isNotNull("fileName", fileName);
    return readAllTextLines(new File(fileName));
  }

  /** Overload of {@link #readAllTextLines(String)} */
  public static List<String> readAllTextLines(File file) throws IOException {
    Contract.isNotNull("file", file);
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      return readAllTextLines(reader);
    }
  }

  /** Overload of {@link #readAllTextLines(String)} */
  public static List<String> readAllTextLines(Reader reader) throws IOException {
    Contract.isNotNull("reader", reader);
    if (reader instanceof BufferedReader) {
      return readAllTextLines((BufferedReader)reader);
    }
    else { // Wrap if needed, for efficiency
      try (BufferedReader bufReader = new BufferedReader(reader)) {
        return readAllTextLines(bufReader);
      }
    }
  }

  /** Overload of {@link #readAllTextLines(String)} */
  private static List<String> readAllTextLines(BufferedReader reader) throws IOException {
    List<String> result = new ArrayList<>();
    for (; ; ) {
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      result.add(line);
    }
    return result;
  }

  /**
   * Create or overwrite the contents of a text file with new contents.
   *
   * @param fileName The name of the file whose contents need to be written. When it doesn't exist yet, it will be created.
   * @param contents The new file contents.
   *
   * @throws IllegalArgumentException When any argument is null.
   * @throws IOException When the specified file doesn't exist or reading its contents fails.
   */
  public static void writeTextFile(String fileName, String contents) throws IOException {
    Contract.isNotNull("fileName", fileName);
    Contract.isNotNull("contents", contents);
    writeTextFile(new File(fileName), contents);
  }

  /** Overload of {@link #writeTextFile(String,String)} */
  public static void writeTextFile(File file, String contents) throws IOException {
    Contract.isNotNull("file", file);
    Contract.isNotNull("contents", contents);
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
      writer.write(contents);
      writer.flush();
    }
  }

  /**
   * Return the contents of a binary file in one operation.
   *
   * @param fileName The name of the file whose contents need to be returned.
   *
   * @return The file contents.
   *
   * @throws IllegalArgumentException When the argument is null.
   * @throws IOException When the specified file doesn't exist or reading its contents fails.
   */
  public static byte[] readBinaryFile(String fileName) throws IOException {
    Contract.isNotNull("fileName", fileName);
    return readBinaryFile(new File(fileName));
  }

  /** Overload of {@link #readBinaryFile(String)} */
  public static byte[] readBinaryFile(File file) throws IOException {
    Contract.isNotNull("file", file);
    byte[] bb = new byte[(int) file.length()];
    try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream(file))) {
      int totalBytesRead = 0;
      while ((totalBytesRead < bb.length) && ((reader.read(bb, totalBytesRead, bb.length - totalBytesRead)) > -1)) {
        // Keep reading until input is exhausted
      }
    }
    return bb;
  }

  /**
   * Create or overwrite the contents of a binary file with the new contents.
   *
   * @param fileName The name of the file whose contents need to be written. When it doesn't exist yet, it will be created.
   * @param contents The new file contents.
   *
   * @throws IllegalArgumentException When any argument is null.
   * @throws IOException When writing the new contents fails due to an IO error.
   */
  public static void writeBinaryFile(String fileName, byte[] contents) throws IOException {
    Contract.isNotNull("fileName", fileName);
    Contract.isNotNull("contents", contents);
    writeBinaryFile(new File(fileName), contents);
  }

  /** Overload of {@link #writeBinaryFile(String,byte[])} */
  public static void writeBinaryFile(File file, byte[] contents) throws IOException {
    Contract.isNotNull("file", file);
    Contract.isNotNull("contents", contents);
    try (BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(file))) {
      writer.write(contents);
      writer.flush();
    }
  }


  /**
   *  Lock the file path so this process has exclusive access to it.
   *
   *  @param path The file path to be locked.
   *  @return A {@link FileLock} if locking succeeded, and <code>null</code> otherwise.
   *          The returned {@link FileLock} should be closed to release the lock.
   */
  public static FileLock lock(String path) {
    Contract.isNotNull("path", path);
    return lock(new File(path));
  }

  /**
   *  Check whether a file is locked by another process.
   *
   *  @param path The file path to be locked.
   *  @return A {@link FileLock} if locking succeeded, and <code>null</code> otherwise.
   *          The returned {@link FileLock} should be closed to release the lock.
   */
  public static FileLock lock(File path) {
    Contract.isNotNull("path", path);
    try {
      final RandomAccessFile raf = new RandomAccessFile(path, "rw");
      final FileLock lock = raf.getChannel().tryLock();
      if (lock == null) {
        raf.close();
        return null;
      }
      // Ensure to also close the RAF on close of the lock
      return new FileLock(lock.channel(), lock.position(), lock.size(), lock.isShared()) {
        @Override
        public boolean isValid() {
          return lock.isValid();
        }

        @Override
        public void release() throws IOException {
          lock.release();
          raf.close();
        }
      };
    }
    catch (IOException e) {
      return null;
    }
  }

  /**
   *  Check whether a file is locked by another process.
   *
   *  @param path The file path to be checked.
   *  @return <code>true</code> if the file is already locked, <code>false</code> if it isn't or the file simply doesn't exist.
   */
  public static boolean isLocked(String path) {
    Contract.isNotNull("path", path);
    return isLocked(new File(path));
  }

  /**
   *  Check whether a file is locked by another process.
   *
   *  @param path The file path to be checked.
   *  @return <code>true</code> if the file is already locked, <code>false</code> if it isn't or the file simply doesn't exist.
   */
  public static boolean isLocked(File path) {
    Contract.isNotNull("file", path);
    if (!path.exists()) {
      return false;
    }
    try {
      try (RandomAccessFile raf = new RandomAccessFile(path, "rw")) {
        FileChannel rafChannel = raf.getChannel();
        try (FileLock fl = rafChannel.tryLock()) {
          if (fl != null) {
            fl.release();
            return false;
          }
        }
      }
    }
    catch (IOException e) {
      // Ignore
    }
    return true;
  }

  // </editor-fold>
}
