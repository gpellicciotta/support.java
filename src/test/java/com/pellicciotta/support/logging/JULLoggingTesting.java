// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging;

import com.pellicciotta.support.logging.jul.JULBasedLogBackEnd;
import com.pellicciotta.support.logging.spi.LogBackEnd;
import org.junit.jupiter.api.*;

public class JULLoggingTesting {
  @BeforeAll
  public static void setup() {
    LogBackEnd noop = new JULBasedLogBackEnd();
    LogService.setBackEnd(noop);
  }

  @AfterAll
  public static void cleanup() {
    LogService.setBackEnd(null);
  }

  @Test
  public void logTest() {
    LogService.info("Testing the JUL back-end");

    Log log = LogService.fromCurrentClass();
    log.error("Error from class-based log");
    log.warning("Warning from class-based log");
    log.info("Info from class-based log");
    log.debug("Debug from class-based log");

    try (Span ctx1 = log.start("long-op-1")) {
      ctx1.add("item-1", "val-1");
      ctx1.add("item-2", "val-2");
      log.error("Another message #1");
      log.info("Another message #2");
      try (Span ctx2 = log.start("long-op-2")) {
        log.error("But another message B #1");
        log.info("But another message B #2");
      }
      log.info("Another message #3");
    }
  }
}
