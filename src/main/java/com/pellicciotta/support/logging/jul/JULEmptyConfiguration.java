// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging.jul;

/**
 *  Class with empty constructor so it can be used as value of <kbd>-Djava.util.logging.config.class=</kbd>
 *  and ensure the JUL configuration starts out empty.
 */
public final class JULEmptyConfiguration {
  /**
   * Creates the instance JUL requires; it performs no configuration, leaving JUL empty.
   */
  public JULEmptyConfiguration() {  }
}
