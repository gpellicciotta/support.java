// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging.file;

import com.pellicciotta.support.exceptions.ConfigurationException;
import com.pellicciotta.support.logging.*;
import com.pellicciotta.support.logging.spi.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 *  A {@link LogBackEnd} that appends log messages to a file, as configured through
 *  {@link LoggingConfig#getFilePath()} and {@link LoggingConfig#getFileAppend()}.
 *  <p>
 *  Any missing intermediate directories of the configured file path are created automatically. If no file path
 *  is configured, this back-end silently discards log events rather than failing, since {@link #configure} also
 *  runs with a fresh default {@link LoggingConfig} as part of the no-arg constructor chain.</p>
 */
public class FileBasedLogBackEnd extends LogBackEndBase {
  /**
   *  Create a logging back-end with default (i.e. unconfigured, not writing anywhere yet) configuration.
   */
  public FileBasedLogBackEnd() { }

  /**
   *  Create a logging back-end based on the provided configuration.
   *
   *  @param config The logging configuration.
   */
  public FileBasedLogBackEnd(LoggingConfig config) throws ConfigurationException {
    super(config);
  }

  /** Minimum log level; events below it are discarded. */
  protected int minLevel;
  /** Formatter that turns each {@link LogEvent} into the text line written to the file. */
  protected LogEventFormatter formatter;
  /** Writer on the configured log file; <code>null</code> when no file is open. */
  protected PrintWriter writer;

  /**
   * Closes the open log file, if any, and restores the default minimum level (0) and a default {@link TextFormatter}.
   */
  protected void resetConfiguration() {
    super.resetConfiguration();
    this.closeWriter();
    this.minLevel = 0;
    this.formatter = new TextFormatter();
  }

  private void closeWriter() {
    if (this.writer != null) {
      this.writer.close();
      this.writer = null;
    }
  }

  /**
   *  Configure the back-end: (re)opens the configured log file.
   *
   *  @param config The configuration to be applied.
   */
  @Override
  public synchronized void configure(LoggingConfig config) throws ConfigurationException {
    super.configure(config);
    if (config == null) {
      return ;
    }
    this.minLevel = config.getMinLevel();

    Class<? extends LogEventFormatter> formatterClass = config.getFormatterType();
    try {
      Constructor<? extends LogEventFormatter> formatterCtor = formatterClass.getConstructor();
      LogEventFormatter newFormatter = formatterCtor.newInstance();
      newFormatter.configure(config);
      this.formatter = newFormatter;
    }
    catch (Exception e) {
      throw new ConfigurationException("formatter-type", formatterClass, e);
    }

    // No configured file path: leave unconfigured (no writer), rather than failing, since this method also
    // runs with a fresh default LoggingConfig() as part of this class's own no-arg-constructor chain.
    String filePath = config.getFilePath();
    if (filePath == null || filePath.isBlank()) {
      return ;
    }
    boolean append = config.getFileAppend();
    Path path = Path.of(filePath);
    try {
      Path parent = path.toAbsolutePath().getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      StandardOpenOption existOption = append ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING;
      this.writer = new PrintWriter(new OutputStreamWriter(Files.newOutputStream(path,
          StandardOpenOption.CREATE, existOption), StandardCharsets.UTF_8), false);
    }
    catch (IOException e) {
      throw new ConfigurationException("file-path", filePath, e, "Could not open log file '%s'", filePath);
    }
  }

  @Override
  public synchronized void onLogEvent(LogEvent logEvent) {
    if ((logEvent.getLogLevel() < this.minLevel) || (this.writer == null)) {
      return ;
    }
    this.writer.println(this.formatter.format(logEvent));
    this.writer.flush();
  }

  @Override
  public synchronized void close() {
    super.close();
    this.closeWriter();
  }
}
