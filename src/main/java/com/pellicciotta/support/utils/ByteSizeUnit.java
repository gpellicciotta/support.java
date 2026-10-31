// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pellicciotta.support.exceptions.FormatException;

/**
 *  Common byte size units, both the IEC and SI units.
 *
 *  Note that common usage of these units is not always consistent nor correct but the below
 *  definitions are according to the actual standard interpretation as described in:
 *    https://en.wikipedia.org/wiki/Kibibyte
 */
public enum ByteSizeUnit {
  /** Byte */
  B(1000, 0),
  /** Kilobyte. */
  KB(1000, 1),
  /** Kibibyte. */
  KIB(1024, 1),
  /** Megabyte. */
  MB(1000, 2),
  /** Mebibyte. */
  MIB(1024, 2),
  /** Gigabyte. */
  GB(1000, 3),
  /** Gibibyte. */
  GIB(1024, 3),
  /** Teraabyte. */
  TB(1000, 4),
  /** Tebibyte. */
  TIB(1024, 4),
  /** Petabyte. */
  PB(1000, 5),
  /** Pebibyte. */
  PIB(1024, 5),
  /** Exabyte. */
  EB(1000, 6),
  /** Exbibyte. */
  EIB(1024, 6);

  private ByteSizeUnit(int base, int exponent) {
    this.byteCount = (long) Math.pow(base, exponent);
  }

  private final long byteCount;

  /**
   *  @return The number of byte represented by this unit.
   *          Can be used as a multiplication factor.
   */
  public long getByteCount() {
    return this.byteCount;
  }

  // [Conversion Methods]

  // Java 7: private static final Pattern BYTE_SIZE_PATTERN = Pattern.compile("^\\s*(?<number>\\d+([.]\\d+)?)\\s*(?<unit>[KMGTP]?[i]?B)\\s*$", Pattern.CASE_INSENSITIVE);
  private static final Pattern BYTE_SIZE_PATTERN = Pattern.compile("^\\s*(\\d+(?:[.]\\d+)?)\\s*([KMGTP]?[i]?B)?\\s*$", Pattern.CASE_INSENSITIVE);

  /**
   *  Try to parse a human readable byte size representation into the number of bytes represented.
   *
   *  @param byteSizeStr The human readable byte size representation which is expected to exist of
   *                     a number and an optional suffix:
   *                     <pre>
   *                       {decimal number}{space}?{unit}?
   *                     </pre>
   *                     The suffix can be B, KB, MB, GB, TB, PB, EB (each a factor 1000 apart)
   *                                    or KiB, MiB, GiB, TiB, PiB, EiB (each a factor 1024 apart).
   *
   *  @return The number of bytes represented by byteSizeStr
   *
   *  @throws FormatException If byteSizeStr cannot be interpreted correctly.
   */
  public static long parseValue(String byteSizeStr) {
    Contract.isNotNull("byteSizeStr", byteSizeStr);
    Contract.isTrue("!byteSizeStr.trim().isEmpty()", !byteSizeStr.trim().isEmpty());
    Matcher m = ByteSizeUnit.BYTE_SIZE_PATTERN.matcher(byteSizeStr.toUpperCase().trim());
    if (m.matches()) {
      double value = Double.parseDouble(m.group(1)); // Java 7: m.group("number")
      long multiplicationFactor = 1;
      String unitStr = m.group(2); // Java 7: m.group("unit");
      if ((unitStr != null)) {
        ByteSizeUnit unit = ByteSizeUnit.valueOf(unitStr);
        multiplicationFactor = unit.getByteCount();
      }
      return (long) (value * multiplicationFactor);
    }
    throw new FormatException(byteSizeStr, "The provided string '" + byteSizeStr + "' cannot be interpreted as a byte size");
  }

  /**
   *  Format a number of bytes into a human-readable representation (which is an approximation of the actual byte count).
   *
   *  @param byteSize The number of bytes to represent in a human readable way.
   *
   *  @return A human readable byte size representation which starts with one or more digits
   *          forming a non-zero number and has a suffix indicating a multiplication factor:
   *          <pre>
   *            {decimal number}{space}{suffix}
   *          </pre>
   *          The suffixes used are: B, KB, MB, GB, TB, EB (where each unit is a factor 1000 apart)
   *          <p>
   *          The {decimal number} will not contain decimals and will use comma as thousands separtor.</p>
   *          <p>
   *          This representation may be an approximation of the exact value, so it is not
   *          guaranteed that using {@link #parseValue(String)} on the result will result in the exact same value.</p>
   */
  public static String formatValue(long byteSize) {
    return formatValue(byteSize, false, 0);
  }

  /**
   *  Format a number of bytes into a human-readable representation (which is an approximation of the actual byte count).
   *
   *  @param byteSize The number of bytes to represent in a human readable way.
   *  @param useIECUnits Whether to use IEC units.
   *                     If true, count in multiples of 1024 (as was commonly the case before 1999) and use the units KiB, MiB, GiB, ...
   *                     If false, multiples of 1000 will be used: 1 KB will be equal to 1000 bytes (as is currently the standard) and the units will be named KB, MB, GB, ...
   *
   *  @return A human readable byte size representation which starts with one or more digits
   *          forming a non-zero number and has a suffix indicating a multiplication factor:
   *          <pre>
   *            {decimal number}{space}?{unit}?
   *          </pre>
   *          The units used are dependent on the useIECUnits parameter.
   *          If false, they are: KB, MB, GB, TB, EB (where each unit is a factor 1000 apart)
   *          If true, they are: KiB, MiB, GiB, TiB, EiB (where each unit is a factor 1024 apart)
   *          <p>
   *          The {decimal number} will not contain decimals and will use comma as thousands separator.</p>
   *          <p>
   *          This representation may be an approximation of the exact value, so it is not
   *          guaranteed that using {@link #parseValue(String)} on the result will result in the exact same value.</p>
   */
  public static String formatValue(long byteSize, boolean useIECUnits) {
    return formatValue(byteSize, useIECUnits, 0);
  }

  /**
   *  Format a number of bytes into a human-readable representation (which is an approximation of the actual byte count).
   *
   *  @param byteSize The number of bytes to represent in a human readable way.
   *  @param useIECUnits Whether to use IEC units.
   *                     If true, count in multiples of 1024 (as was commonly the case before 1999) and use the units KiB, MiB, GiB, ...
   *                     If false, multiples of 1000 will be used: 1 KB will be equal to 1000 bytes (as is currently the standard) and the units will be named KB, MB, GB, ...
   *  @param decimals How many decimals to show.
   *
   *  @return A human readable byte size representation which starts with one or more digits
   *          forming a non-zero number and has a suffix indicating a multiplication factor:
   *          <pre>
   *            {decimal number}{space}?{unit}?
   *          </pre>
   *          The units used are dependent on the useIECUnits parameter.
   *          If false, they are: KB, MB, GB, TB, EB (where each unit is a factor 1000 apart)
   *          If true, they are: KiB, MiB, GiB, TiB, EiB (where each unit is a factor 1024 apart)
   *          <p>
   *          The {decimal number} will not contain decimals and will use comma as thousands separator.</p>
   *          <p>
   *          This representation may be an approximation of the exact value, so it is not
   *          guaranteed that using {@link #parseValue(String)} on the result will result in the exact same value.</p>
   */
  public static String formatValue(long byteSize, boolean useIECUnits, int decimals) {
    Contract.isPositive("byteSize", byteSize);
    int unit = useIECUnits ? 1024 : 1000;
    if (byteSize < unit) {
      return byteSize + "";
    }
    int exp = (int) (Math.log(byteSize) / Math.log(unit));
    String unitStr = "KMGTPE".charAt(exp - 1) + (useIECUnits ? "i" : "") + "B";
    double bytesPerUnit = Math.pow(unit, exp);
    double units = byteSize / bytesPerUnit;
    if (decimals <= 0) {
      return String.format(Locale.US, "%d %s", (long) units, unitStr);
    } else {
      return String.format(Locale.US, "%." + decimals + "f %s", units, unitStr);
    }
  }
}
