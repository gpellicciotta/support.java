// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.config.v1;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.Date;

import com.pellicciotta.support.annotations.ConfigGroup;
import com.pellicciotta.support.annotations.ConfigSetting;
import com.pellicciotta.support.app.ExitCode;
import com.pellicciotta.support.codecs.StandardStringCodecs;
import com.pellicciotta.support.codecs.StringCodec;
import com.pellicciotta.support.config.*;
import com.pellicciotta.support.logging.LogService;
import com.pellicciotta.support.utils.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *  v1: Making configuration available indirectly, based on annotations.
 *      When you're only interested in getting the values, and there is no need for much meta-info.
 */
public class AppConfig {
  enum Action {
    RUN,
    DO_SOMETHING,
    AVOID_ERRORS
  }

  @ConfigSetting(name="string-a", description="A string setting")
  String stringA = "no string";
  @ConfigSetting(name="boolean-b", description="A boolean size")
  boolean booleanB =  true;
  @ConfigSetting(name="byte-size-c", description="A byte size", stringCodec=StandardStringCodecs.ByteSize.class)
  long byteSizeC = 5L;
  @ConfigSetting(name="long-d", description="A long size")
  long longD = 1234L;
  @ConfigSetting(name="date-time-e", description="A date/time size")
  Date dateTimeE = new Date();
  @ConfigSetting(name="enum-f", description="An enum setting")
  Action enumF = Action.AVOID_ERRORS;

  @ConfigGroup(name="web", description="Web server configuration")
  WebConfig web = new WebConfig();

  @ConfigGroup(name="logging", description="Logging configuration")
  LoggingConfig log = new LoggingConfig();
}
