// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.app;

import com.pellicciotta.support.config.CommandLine;
import com.pellicciotta.support.config.Config;
import com.pellicciotta.support.exceptions.ConfigurationException;
import com.pellicciotta.support.logging.spi.LoggingConfig;
import com.pellicciotta.support.mail.MailConfig;

import java.io.IOException;
import java.text.ParseException;

/**
 *  A basic configuration containing logging settings and remembering the last command-line it was loaded from.
 */
public class BasicConfig extends Config {
  /**
   *  Create a default configuration.
   */
  public BasicConfig() {
    this.logging = new LoggingConfig();
    this.initialize(this.logging);
  }

  /** The logging settings. */
  public final LoggingConfig logging;

  private CommandLine cli;

  /** The command-line this configuration was loaded from. May be <code>null</code>. */
  public CommandLine getCommandLine() {
    return this.cli;
  }

  /**
   *  Load configuration settings from the command-line.
   *
   *  @param cli The command-line to start loading configuration settings from.
   *             If the command-line contains a <kbd>--config-file</kbd> argument, settings will first get loaded from the indicated config file,
   *             and other command-line settings can then override the settings loaded from the config file.
   */
  @Override
  public void loadFrom(CommandLine cli) throws IOException {
    this.cli = cli;
    super.loadFrom(cli);
  }
}
