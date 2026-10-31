// Copyright (c) Giovanni Pellicciotta

/**
 *  Logging API and SPI.
 *  <p>
 *  The {@link com.pellicciotta.support.logging.LogService} is the front-end API to both configure a {@link com.pellicciotta.support.logging.spi.LogBackEnd} that does the heavy lifting
 *  and then use that back-end to obtain {@link com.pellicciotta.support.logging.Log} instances that are the main API for sending log messages
 *  and associating structured data with them.</p>
 *  <p>
 *  Short overview of main types, with a distinction between SPI (= customization of the back-end) and API (= what typical code will use):</p>
 *  <pre>
 *    SPI                                               API
 *    ------------------------------------------------+-----------------------------------------------------
 *    LogBackEnd                                        LogService
 *     - singleton                                         - singleton
 *     - gets configured externally                        - factory / gateway to concrete LogBackEnd
 *     - manages Log and Span instances                    - also conveniences for CLI apps
 *     - routes all data (or not: filtering)
 *
 *    LogDestination (= destination)                    Log (= source)
 *     - final handling of log records                    - main sink for log messages
 *
 *                                                      Span
 *                                                        - main API for associating structured data
 *                                                        - main API for hierarchical structuring of operations and recording their timing
 *
 *    ContextData                                       ContextDataItem
 *     - the full set of context data                     - single name/value pair associated with a span, and hence also available for logging
 *
 *    LogEvent
 *  </pre>
 *  <p>
 *  This should lead to following type of usage:</p>
 *  <pre>
 *    // 1. Setup back-end at start of process:
 *    LogBackEnd lbe = JULLogBackEnd.configure("logging.config");
 *    LogService.setBackEnd(lbe);
 *
 *    // 2. Use in specific class:
 *    Log log = LogService.getLog("db");
 *    try (Span ctx = log.start("db-read")) {
 *      ctx.add("db.name", dbName);
 *      ctx.add("db.statement", dbSelect);
 *      // ...
 *      log.info("succeeded reading %d records from DB", recs.size());
 *    }
 *    catch (Exception e) {
 *      log.error("Read failed: %s", e);
 *    }
 *  </pre>
 */
package com.pellicciotta.support.logging;
