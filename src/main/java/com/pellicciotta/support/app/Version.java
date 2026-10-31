// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.app;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pellicciotta.support.utils.Contract;
import com.pellicciotta.support.utils.StringUtils;

/**
 *  An application's version, in line with <a href="https://semver.org/spec/v2.0.0.html">Semantic Versioning 2.0.0</a>.
 *  <p>
 *  Given a version number MAJOR.MINOR.PATCH, increment the:</p><ol>
 *    <li>MAJOR version when you make incompatible API changes,</li>
 *    <li>MINOR version when you add functionality in a backwards compatible manner, and</li>
 *    <li>PATCH version when you make backwards compatible bug fixes.</li></ol>
 *  <p>
 *  Additional labels for pre-release and build metadata are available as extensions to the MAJOR.MINOR.PATCH format.</p>
 */
public class Version implements Comparable<Version> {
  // <editor-fold desc="Construction">

  /**
   *  Create a version with given major, minor and 0 as patch part.
   *
   *  @param major Major version number. Must be zero or positive.
   *  @param minor Minor version number. Must be zero or positive.
   *  @param label Any label attached to the version number (e.g. alpha, pre, ...). Can be <code>null</code>.
   */
  public Version(int major, int minor, String label) {
    this(major, minor, 0, label, new String[0]);
  }

  /**
   *  Create a version with given major, minor and 0 as patch part.
   *
   *  @param major Major version number. Must be zero or positive.
   *  @param minor Minor version number. Must be zero or positive.
   *  @param label Any label attached to the version number (e.g. alpha, pre, ...). Can be <code>null</code>.
   *  @param releaseNotes Any release notes associated with the version. Can be empty.
   */
  public Version(int major, int minor, String label, String[] releaseNotes) {
    this(major, minor, 0, label, releaseNotes);
  }

  /**
   *  Create a version with given major, minor and 0 as patch part.
   *
   *  @param major Major version number. Must be zero or positive.
   *  @param minor Minor version number. Must be zero or positive.
   *  @param patch Patch version number. Must be zero or positive.
   *  @param label Any label attached to the version number (e.g. alpha, pre, ...). Can be <code>null</code>.
   *  @param releaseNotes Any release notes associated with the version. Can be empty.
   */
  public Version(int major, int minor, int patch, String label, String[] releaseNotes) {
    Contract.isNotNegative("major", major);
    Contract.isNotNegative("minor", minor);
    Contract.isNotNegative("patch", patch);
    this.majorVersion = major;
    this.minorVersion = minor;
    this.patchVersion = patch;
    this.label = label;
    this.releaseNotes = releaseNotes == null ? new String[0] : releaseNotes;
    StringBuilder sb = new StringBuilder();
    sb.append(this.majorVersion);
    sb.append('.');
    sb.append(this.minorVersion);
    sb.append('.');
    sb.append(this.patchVersion);
    if (this.label != null) {
      sb.append('-');
      sb.append(this.label);
    }
    this.versionString = sb.toString();
  }

  /**
   *  Create a version based on the provided versionString.
   *
   *  @param versionString A version string which should have following format: <pre>{major}.{minor}[.{patch}][-{label}]</pre>.
   *  @param releaseNotes Any release notes associated with the version. Can be empty.
   */
  public Version(String versionString, String... releaseNotes) {
    Contract.isNotNull("versionString", versionString);
    versionString = versionString.trim();
    this.releaseNotes = (releaseNotes == null) ? new String[0] : releaseNotes;
    Matcher m = VERSION_REGEX.matcher(versionString);
    if (!m.matches()) {
      throw new IllegalArgumentException("Version string '" + versionString + "' has invalid format");
    }
    this.majorVersion = Integer.parseInt(m.group(1), 10);
    this.minorVersion = Integer.parseInt(m.group(2), 10);
    this.patchVersion = (m.group(3) != null) ? Integer.parseInt(m.group(3), 10) : 0;
    this.label = m.group(4);
    StringBuilder sb = new StringBuilder();
    sb.append(this.majorVersion);
    sb.append('.');
    sb.append(this.minorVersion);
    sb.append('.');
    sb.append(this.patchVersion);
    if (this.label != null) {
      sb.append('-');
      sb.append(this.label);
    }
    this.versionString = sb.toString();
  }

  private static Pattern VERSION_REGEX = Pattern.compile("^([0-9]+)[.]([0-9]+)(?:[.]([0-9]+))?(?:[-](.*))?$");

  private final String versionString;
  private final int majorVersion;
  private final int minorVersion;
  private final int patchVersion;
  private final String label;
  private final String[] releaseNotes;

  // </editor-fold>
  // <editor-fold desc="Properties">

  /**
   * @return The full version string, which will be formatted as follows: <pre>${major}.${minor}.${patch}[-${label}]</pre>.
   *         The label part will only only included if there is an actual label. The three version numbers will always be present.
   */
  public String getVersionString() {
    return this.versionString;
  }

  /**
   * @return Major version number.
   */
  public int getMajorVersion() {
    return this.majorVersion;
  }

  /**
   * @return Minor version number.
   */
  public int getMinorVersion() {
    return this.minorVersion;
  }

  /**
   * @return Patch version number.
   */
  public int getPatchVersion() {
    return this.patchVersion;
  }

  /**
   * @return Any version label. Will typically be <code>null</code> for release versions.
   */
  public String getLabel() {
    return this.label;
  }

  /**
   * @return Any associated release notes. Can be empty but will never be <code>null</code>.
   */
  public String[] getReleaseNotes() {
    return this.releaseNotes;
  }

  // </editor-fold>
  // <editor-fold desc="Operations">

  /**
   *  Compare this version to another.
   *  <p>
   *  Comparison will first check the major version number, then the minor, then the patch.</p>
   *  <p>
   *  If the two versions are still considered equal after these three checks, the labels will be compared too:</p><ul>
   *    <li>if only a single version has a label, that version will be considered less (i.e. older). Example: <code>4.5.0-pre</code> is considered less/older than <code>4.5.0</code></li>
   *    <li>if both have a label, the labels will be compared lexicographically. Example: <code>1.2.3-alpha</code> is considered less/older than <code>1.2.3-beta</code> since <code>alpha</code> comes lexicographically before <code>beta</code>.</li></ul>
   *
   *  @param o The other version to compare with.
   *
   *  @return <code>-1</code> If this version is considered less (i.e. older) than the other,
   *          <code>0</code> if both version are considered equal (i.e. the same version), and
   *          <code>+1</code> if this version is considered higher (i.e. newer) than the other.
   */
  @Override
  public int compareTo(Version o) {
    int cmp = Integer.compare(this.majorVersion, o.getMajorVersion());
    if (cmp != 0) {
      return cmp;
    }
    cmp = Integer.compare(this.minorVersion, o.getMinorVersion());
    if (cmp != 0) {
      return cmp;
    }
    cmp = Integer.compare(this.patchVersion, o.getPatchVersion());
    if (cmp != 0) {
      return cmp;
    }
    if (this.label == null) {
      if (o.label == null) {
        return 0;
      }
      else {
        return +1;
      }
    }
    else if (o.label == null) {
      return -1;
    }
    else {
      return this.label.compareTo(o.label);
    }
  }

  /**
   *  Compare this version to another.
   *  <p>
   *  Comparison will happen as described in {@link #compareTo(Version)} after first turning the provided <code>versionString</code> into a {@link Version} object.</p>
   *
   *  @param versionString The other version to compare with.
   *
   *  @return <code>-1</code> If this version is considered less (i.e. older) than the other,
   *          <code>0</code> if both version are considered equal (i.e. the same version), and
   *          <code>+1</code> if this version is considered higher (i.e. newer) than the other.
   */
  public int compareTo(String versionString) {
    return this.compareTo(new Version(versionString));
  }

  /**
   *  Compare this version to another for equality.
   *  <p>
   *  Comparison will happen as described in {@link #compareTo(Version)}.</p>
   *
   *  @param o The other version to compare with. Must be of type {@link Version} or else <code>false</code> will be returned.
   *
   *  @return <code>true</code> only if <code>o</code> is of type {@link Version} and <code>this.compareTo((Version)o)</code> results is <code>0</code>.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Version)) {
      return false;
    }
    Version otherVersion = (Version) o;
    return this.compareTo(otherVersion) == 0;
  }

  /**
   * @return The hash-code of {@link #versionString}.
   */
  @Override
  public int hashCode() {
    return this.versionString.hashCode();
  }

  /**
   *  @return Same as {@link #versionString}.
   */
  @Override
  public String toString() {
    return this.versionString;
  }

  // </editor-fold>
}
