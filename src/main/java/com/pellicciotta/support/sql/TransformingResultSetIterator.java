// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.sql;

import java.io.Closeable;
import java.sql.*;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

import com.pellicciotta.support.utils.Contract;

/**
 *  An iterator that transforms the elements provided by an underlying {@link java.sql.ResultSet}.
 */
public class TransformingResultSetIterator<T, R extends ResultSet> implements Iterator<T>, AutoCloseable {
  // <editor-fold desc="Construction">

  /**
   * Creates a new iterator using the provided one as element source.
   *
   * @param source The underlying resultSet, whose elements should get transformed.
   * @param transform Function to apply to each element before returning it.
   */
  public TransformingResultSetIterator(R source, Function<R, T> transform) {
    Contract.isNotNull(source);
    Contract.isNotNull(transform);
    this.source = source;
    this.transform = transform;
    try {
      if (this.source.next()) {
        this.nextElement = this.transform.apply(this.source);
      }
      else {
        this.nextElement = null;
        this.source.close();
      }
    }
    catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private final R source;
  private Function<R, T> transform;
  private T nextElement;

  // </editor-fold>
  // <editor-fold desc="Iterator Interface">

  /**
   * Returns whether another element is to be expected
   *
   * @return True if {@link #next()} will return another element.
   */
  @Override
  public boolean hasNext() {
    return this.nextElement != null;
  }

  /**
   * Fetches the next element, if any, from the underlying {@link Iterator}
   * and transforms it.
   *
   * @return The next, transformed, element.
   *
   * @throws NoSuchElementException if there is no next element.
   */
  @Override
  public T next() {
    if (this.nextElement == null) {
      throw new NoSuchElementException();
    }
    T currElement = this.nextElement;
    try {
      // From ResultSet docs:
      //   If the result set type is <code>TYPE_FORWARD_ONLY</code>, it is vendor specified
      //   whether their JDBC driver implementation will return <code>false</code> or
      //   throw an <code>SQLException</code> on a subsequent call to <code>next</code>.
      if (!this.source.isClosed() && this.source.next()) {
        this.nextElement = this.transform.apply(this.source);
      }
      else {
        this.nextElement = null;
        if (!this.source.isClosed()) {
          this.source.close();
        }
      }
    }
    catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return currElement;
  }

  /**
   * Closes the underlying {@link ResultSet}.
   *
   * @throws RuntimeException If closing fails.
   */
  @Override
  public void close() {
    try {
      Statement stmt = this.source.getStatement();
      if ((stmt != null) && !stmt.isClosed()) {
        stmt.close();
      }
      if (!this.source.isClosed()) {
        this.source.close();
      }
    }
    catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  // </editor-fold>
}
