// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging;

import com.pellicciotta.support.logging.Log;
import com.pellicciotta.support.logging.LogService;
import com.pellicciotta.support.logging.noop.NoopLogBackEnd;
import com.pellicciotta.support.logging.spi.LogBackEnd;
import org.junit.jupiter.api.*;

public class NoopLoggingTesting
{
  @BeforeAll
  public static void setup() {
    LogBackEnd noop = new NoopLogBackEnd();
    LogService.setBackEnd(noop);
  }

  @AfterAll
  public static void cleanup() {
    LogService.setBackEnd(null);
  }

  @Test
  public void logTest() {
    LogService.info("Testing the noop back-end");

    Log log = LogService.fromCurrentClass();
    log.error("Error from class-based log");
    log.warning("Warning from class-based log");
    log.info("Info from class-based log");
    log.debug("Debug from class-based log");
  }
}
