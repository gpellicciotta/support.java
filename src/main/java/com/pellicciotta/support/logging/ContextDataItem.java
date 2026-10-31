// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging;

/**
 *  A data item that can be associated with a particular {@link Log} object or {@link Span},
 *  hereby providing an extra means of passing data into each log invocation.
 *  <p>
 *  Possible use cases:<ul>
 *    <li>Associate extra formatting information with a specific {@link Log} object.</li>
 *    <li>Associate a session ID with all log messages emitted from a specific {@link Log} object.</li>
 *  </ul>
 */
public interface ContextDataItem extends AutoCloseable
{
  /**
   * @return The name of this contextual data item.
   */
  String getName();

  /**
   *  @return The value of this contextual data item. Can be <code>null</code>.
   */
  Object getValue();

  /** @return Whether this data item is closed and hence no longer associated with a {@link Log} or {@link Span}. */
  boolean isClosed();

  /**
   * Removes this data items from the {@link Log} or {@link Span} it was associated with.
   */
  void close();
}
