// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.codecs;

/**
 *  Interface for getting/setting a type value.
 *
 *  @param <T> The native type to set/get.
 */
public interface TypedValue<T>
{
  /**
   *  Get the value.
   *
   *  @return The current value.
   */
  T get();

  /**
   *  Set the value.
   *
   *  @param value The to-be value.
   *
   *  @throws RuntimeException If the to-be value is not valid.
   */
  void set(T value);
}
