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

  public static final class LogLevel extends FunctionBasedStringCodec<Integer> {
    public LogLevel() {
      super(StringConversionUtils::toLogLevel, StringConversionUtils::fromLogLevel);
    }
  }

  public static final class ByteSize extends FunctionBasedStringCodec<Long> {
    public ByteSize() {
      super(StringConversionUtils::toByteSize, StringConversionUtils::fromByteSize);
    }
  }

  public static final class Timespan extends FunctionBasedStringCodec<Long> {
    public Timespan() {
      super(StringConversionUtils::toTimespan, StringConversionUtils::fromTimespan);
    }
  }

  public static final class Password extends FunctionBasedStringCodec<String> {
    public Password() {
      super(StringConversionUtils::toPlainTextPassword, StringConversionUtils::fromPlainTextPassword);
    }
  }

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

  public static final StringCodec<Boolean>      BOOLEAN       = new FunctionBasedStringCodec<>(StringConversionUtils::toBoolean, StringConversionUtils::fromBoolean);
  public static final StringCodec<Integer>      INTEGER       = new FunctionBasedStringCodec<>(StringConversionUtils::toInt, StringConversionUtils::fromInt);
  public static final StringCodec<Integer>      LOG_LEVEL     = new LogLevel();
  public static final StringCodec<Long>         LONG          = new FunctionBasedStringCodec<>(StringConversionUtils::toLong, StringConversionUtils::fromLong);
  public static final StringCodec<Long>         BYTE_SIZE     = new ByteSize();
  public static final StringCodec<Long>         TIMESPAN      = new Timespan();
  public static final StringCodec<BigDecimal>   BIG_DECIMAL   = new FunctionBasedStringCodec<>(StringConversionUtils::toBigDecimal, StringConversionUtils::fromBigDecimal);
  public static final StringCodec<String>       STRING        = new FunctionBasedStringCodec<>(StringConversionUtils::toExpandedString, Function.identity());
  public static final StringCodec<String>       PASSWORD      = new Password();
  public static final StringCodec<Pattern>      REGEX         = new RegexCodec();
  public static final StringCodec<RegexReplace> REGEX_REPLACE = new RegexReplaceCodec();
  public static final StringCodec<Charset>      CHARSET       = new FunctionBasedStringCodec<>(StringConversionUtils::toCharset, StringConversionUtils::fromCharset);
  public static final StringCodec<byte[]>       BYTE_ARRAY    = new FunctionBasedStringCodec<>(StringConversionUtils::toByteArray, StringConversionUtils::fromByteArray);
  public static final StringCodec<Double>       DOUBLE        = new FunctionBasedStringCodec<>(StringConversionUtils::toDouble, StringConversionUtils::fromDouble);
  public static final StringCodec<Date>         DATE_TIME     = new FunctionBasedStringCodec<>(StringConversionUtils::toDateTime, StringConversionUtils::fromDateTime);
  public static final StringCodec<Class<?>>     TYPE          = new FunctionBasedStringCodec<>(StringConversionUtils::toClass, StringConversionUtils::fromClass);
}
