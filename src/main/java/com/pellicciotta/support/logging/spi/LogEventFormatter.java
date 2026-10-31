// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging.spi;

/**
 *  For formatting a {@link LogEvent}s into a {@link String} representation.
 */
public interface LogEventFormatter
{
  /**
   *  Configure the formatter.
   *
   *  @param config The configuration to be applied.
   */
  void configure(LoggingConfig config);

  /**
   * Format the specified log record.
   *
   * @param event Log event to be formatted.
   *
   * @return A string representation of the event, which can consist of multiple lines.
   *         The exact format of the message depends on the presence of the parameters in the message format.
   */
  String format(LogEvent event);
}
