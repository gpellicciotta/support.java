// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.codecs;

/**
 *  The {@link SettingType} both implies the type of value a setting is attached to,
 *  and the way this value type is encoded/decoded to/from <c>String</c>.
 */
public enum SettingType
{
  /** For a setting whose type should get determined automatically based on the value type. */
  AUTO,
  /** For values of type <c>string</c> with no encoding/decoding except for parameter value expansion. */
  STRING,
  /** For values of type <c>bool</c>: encoding/decoding via <c>StringConversionUtils.[from|to]Boolean</c>. */
  BOOLEAN,
  /** For values of type <c>int</c>: encoding/decoding via <c>StringConversionUtils.[from|to]Integer</c>. */
  INTEGER,
  /** For values of type <c>int</c>: encoding/decoding via <c>StringConversionUtils.[from|to]LogLevel</c>. */
  LOG_LEVEL,
  /** For values of type <c>long</c>: encoding/decoding via <c>StringConversionUtils.[from|to]Long</c>. */
  LONG,
  /** For values of type <c>long</c> that have the meaning of a byte size: encoding/decoding via <c>StringConversionUtils.[from|to]ByteSize</c>. */
  BYTE_SIZE,
  /** For values of type <c>long</c>: encoding/decoding via <c>StringConversionUtils.[from|to]Timespan</c>. */
  TIMESPAN,
  /** For values of type <c>double</c>: encoding/decoding via <c>StringConversionUtils.[from|to]Double</c>. */
  DOUBLE,
  /** For values of type <c>decimal</c>: encoding/decoding via <c>StringConversionUtils.[from|to]Decimal</c>. */
  BIG_DECIMAL,
  /** For values of type <c>string</c>: encoding/decoding via <c>StringConversionUtils.[from|to]PlainTextPassword</c>. */
  PASSWORD,
  /** For values of type <c>Date</c>: encoding/decoding via <c>StringConversionUtils.[from|to]DateTime</c>. */
  DATE_TIME,
  /** For values of type <c>Charset</c>: encoding/decoding via <c>StringConversionUtils.[from|to]Charset</c>. */
  CHARSET,
  /** For values of type <c>byte[]</c>: encoding/decoding via <c>StringConversionUtils.[from|to]ByteArray</c>. */
  BYTE_ARRAY,
}
