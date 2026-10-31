// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.codecs;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.function.Function;
import java.util.regex.Pattern;

import com.pellicciotta.support.utils.RegexReplace;
import com.pellicciotta.support.utils.StringConversionUtils;

/**
 *  Standard codecs for a specific native type that can be used directly.
 *  <p>
 *  The inner classes exist for use in the {@link com.pellicciotta.support.annotations.ConfigSetting} annotation.</p>
 */
public final class StandardStringCodecs {
  /** Prevent instantiation */
  StandardStringCodecs() {  }

  /** Codec for log levels, converting between level names and their integer values. */
  public static final class LogLevel extends FunctionBasedStringCodec<Integer> {
    /** Creates a codec for the log level representation. */
    public LogLevel() {
      super(StringConversionUtils::toLogLevel, StringConversionUtils::fromLogLevel);
    }
  }

  /** Codec for byte sizes, converting between size strings and a byte count. */
  public static final class ByteSize extends FunctionBasedStringCodec<Long> {
    /** Creates a codec for the byte size representation. */
    public ByteSize() {
      super(StringConversionUtils::toByteSize, StringConversionUtils::fromByteSize);
    }
  }

  /** Codec for time spans, converting between duration strings and a millisecond count. */
  public static final class Timespan extends FunctionBasedStringCodec<Long> {
    /** Creates a codec for the time span representation. */
    public Timespan() {
      super(StringConversionUtils::toTimespan, StringConversionUtils::fromTimespan);
    }
  }

  /** Codec for plain-text passwords, held as a string. */
  public static final class Password extends FunctionBasedStringCodec<String> {
    /** Creates a codec for the password representation. */
    public Password() {
      super(StringConversionUtils::toPlainTextPassword, StringConversionUtils::fromPlainTextPassword);
    }
  }

  /** Codec that compiles a string into a {@link Pattern} and encodes a pattern as its source text. */
  public static final class RegexCodec implements StringCodec<Pattern> {
    @Override
    public Pattern decode(String from) {
      return Pattern.compile(from);
    }

    @Override
    public String encode(Pattern value) {
      return value.pattern();
    }
  }

  /** Codec that parses a string into a {@link RegexReplace} and encodes it via its {@code toString()}. */
  public static final class RegexReplaceCodec implements StringCodec<RegexReplace> {
    @Override
    public RegexReplace decode(String from) {
      return RegexReplace.parse(from);
    }

    @Override
    public String encode(RegexReplace value) {
      return value.toString();
    }
  }

  /** Codec for {@code Boolean} values. */
  public static final StringCodec<Boolean>      BOOLEAN       = new FunctionBasedStringCodec<>(StringConversionUtils::toBoolean, StringConversionUtils::fromBoolean);
  /** Codec for {@code Integer} values. */
  public static final StringCodec<Integer>      INTEGER       = new FunctionBasedStringCodec<>(StringConversionUtils::toInt, StringConversionUtils::fromInt);
  /** Codec for log levels as integers. */
  public static final StringCodec<Integer>      LOG_LEVEL     = new LogLevel();
  /** Codec for {@code Long} values. */
  public static final StringCodec<Long>         LONG          = new FunctionBasedStringCodec<>(StringConversionUtils::toLong, StringConversionUtils::fromLong);
  /** Codec for byte sizes as a {@code Long} byte count. */
  public static final StringCodec<Long>         BYTE_SIZE     = new ByteSize();
  /** Codec for time spans as a {@code Long}. */
  public static final StringCodec<Long>         TIMESPAN      = new Timespan();
  /** Codec for {@code BigDecimal} values. */
  public static final StringCodec<BigDecimal>   BIG_DECIMAL   = new FunctionBasedStringCodec<>(StringConversionUtils::toBigDecimal, StringConversionUtils::fromBigDecimal);
  /** Codec for strings; decoding expands the text via {@code StringConversionUtils.toExpandedString}. */
  public static final StringCodec<String>       STRING        = new FunctionBasedStringCodec<>(StringConversionUtils::toExpandedString, Function.identity());
  /** Codec for plain-text passwords. */
  public static final StringCodec<String>       PASSWORD      = new Password();
  /** Codec for regular-expression {@link Pattern}s. */
  public static final StringCodec<Pattern>      REGEX         = new RegexCodec();
  /** Codec for {@link RegexReplace} values. */
  public static final StringCodec<RegexReplace> REGEX_REPLACE = new RegexReplaceCodec();
  /** Codec for {@link Charset}s. */
  public static final StringCodec<Charset>      CHARSET       = new FunctionBasedStringCodec<>(StringConversionUtils::toCharset, StringConversionUtils::fromCharset);
  /** Codec for {@code byte[]} values. */
  public static final StringCodec<byte[]>       BYTE_ARRAY    = new FunctionBasedStringCodec<>(StringConversionUtils::toByteArray, StringConversionUtils::fromByteArray);
  /** Codec for {@code Double} values. */
  public static final StringCodec<Double>       DOUBLE        = new FunctionBasedStringCodec<>(StringConversionUtils::toDouble, StringConversionUtils::fromDouble);
  /** Codec for {@link Date} date-time values. */
  public static final StringCodec<Date>         DATE_TIME     = new FunctionBasedStringCodec<>(StringConversionUtils::toDateTime, StringConversionUtils::fromDateTime);
  /** Codec for {@link Class} objects. */
  public static final StringCodec<Class<?>>     TYPE          = new FunctionBasedStringCodec<>(StringConversionUtils::toClass, StringConversionUtils::fromClass);
}
