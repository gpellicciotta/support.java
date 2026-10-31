// Copyright (c) Giovanni Pellicciotta

/**
 *  Google Cloud Logging support.
 *  <p>
 *  {@link com.pellicciotta.support.logging.gcp.GoogleCloudStructuredLogFormatter} turns a log event into a single
 *  structured JSON string compatible with the Google Cloud <code>LogEntry</code> format. Optionally, it includes
 *  stack traces and all context data. It plugs into any back-end that accepts a
 *  {@link com.pellicciotta.support.logging.spi.LogEventFormatter}.</p>
 */
package com.pellicciotta.support.logging.gcp;
