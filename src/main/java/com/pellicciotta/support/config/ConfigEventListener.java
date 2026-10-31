// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.config;

/**
 *  A type that can be registered with {@link Config} to get notifications of certain events.
 *  <p>
 *  This class is concrete with all empty methods so it is easy to derive from it and implement
 *  only those <code>onXxx</code> methods that are needed.
 *  </p>
 */
public class ConfigEventListener {
  /**
   * Invoked immediately before a {@link Config#loadFrom} operation starts.
   */
  public void onLoadStarted() { }

  /**
   * Invoked immediately after a {@link Config#loadFrom} operation ends.
   */
  public void onLoadEnded() { }

  /**
   * Invoked immediately before a {@link Config#saveTo} operation starts.
   */
  public void onSaveStarted() { }

  /**
   * Invoked immediately after a {@link Config#saveTo} operation ends.
   */
  public void onSaveEnded() { }

  /**
   * Invoked when a setting's value has changed.
   *
   * @param setting The setting whose value has changed.
   * @param oldValue The old value.
   * @param newValue The new value.
   */
  public void onValueChanged(ConfigElement setting, Object oldValue, Object newValue) { }

}
