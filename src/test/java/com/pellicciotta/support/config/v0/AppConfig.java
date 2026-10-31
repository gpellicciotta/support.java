// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.config.v0;

import com.pellicciotta.support.codecs.StringCodec;
import com.pellicciotta.support.config.*;
import com.pellicciotta.support.utils.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  v0: Implementing all configuration as direct sub-classes of Config, Setting and SettingsGroup.
 *      When you want full control and also access to all available meta-info.
 */
public class AppConfig extends Config
{
  private static enum Action {
    RUN,
    DO_SOMETHING,
    AVOID_ERRORS
  }

  public static class StringIntegerPair extends Pair<String, Integer> {
    public StringIntegerPair(String first, Integer second) {
      super(first, second);
    }
  };

  public static class StringIntegerPairsList extends ArrayList<StringIntegerPair> { }

  public AppConfig() {
    SettingsFactory create = new SettingsFactory();

    this.stringA = create.standaloneStringSetting("string-a", "A string setting", "no string");
    this.booleanB = create.standaloneBooleanSetting("boolean-b", "A boolean size", true);
    this.byteSizeC = create.standaloneByteSizeSetting("byte-size-c", "A byte size", 5L);
    this.longD = create.standaloneLongSetting("long-d", "A long size", 1234L);
    this.dateTimeE = create.standaloneDateTimeSetting("date-time-e", "A date/time size", new Date());
    this.enumF = create.standaloneEnumSetting(Action.class,"enum-f", "An action enum", Action.AVOID_ERRORS);
    List<StringIntegerPair> pairListDefaultValue = new ArrayList<>();
    pairListDefaultValue.add(new StringIntegerPair("A first val", 100));
    pairListDefaultValue.add(new StringIntegerPair("A second val", 200));
    this.pairsListG = new StandaloneListSetting<StringIntegerPair>(StringIntegerPair.class, "pairs-g", "A list of pairs", pairListDefaultValue, new StringCodec<StringIntegerPair>() {
      final Pattern REGEX = Pattern.compile("^[\"](.*)[\"] *[:] *([0-9]+) *$");

      @Override
      public StringIntegerPair decode(String from) {
        Matcher m = REGEX.matcher(from);
        if (!m.matches()) {
          throw new IllegalArgumentException("Value '" + from + "' is not a valid StringIntegerPair");
        }
        String strVal = m.group(1).replaceAll("[\"][\"]", "\"");
        int intVal = Integer.parseInt(m.group(2));
        return new StringIntegerPair(strVal, intVal);
      }

      @Override
      public String encode(StringIntegerPair value) {
        return String.format("\"%s\":%d", value.getFirst().replaceAll("[\"]", "\"\""), value.getSecond());
      }
    });
    this.web = new WebConfig();
    this.logging = new LoggingConfig();

    this.initialize(this.stringA, this.booleanB, this.byteSizeC, this.longD, this.dateTimeE, this.enumF, this.pairsListG, this.logging, this.web);
  }

  Setting<String> stringA;
  Setting<Boolean> booleanB;
  Setting<Long> byteSizeC;
  Setting<Long> longD;
  Setting<Date> dateTimeE;
  Setting<Action> enumF;
  ListSetting<StringIntegerPair> pairsListG;

  WebConfig web;
  LoggingConfig logging;


  LoggingConfig getLoggingSettings() {
    return this.logging;
  }

  WebConfig getWebSettings() {
    return this.web;
  }
}
