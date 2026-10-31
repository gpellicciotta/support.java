// Copyright (c) Giovanni Pellicciotta

/**
 *  File-based logging back-end.
 *  <p>
 *  {@link com.pellicciotta.support.logging.file.FileBasedLogBackEnd} is a
 *  {@link com.pellicciotta.support.logging.spi.LogBackEnd} that formats each log event and writes it to a file
 *  configured through {@link com.pellicciotta.support.logging.spi.LoggingConfig}. Missing parent directories are
 *  created, and the file is either appended to or truncated. Without a configured path, events are discarded.</p>
 */
package com.pellicciotta.support.logging.file;
