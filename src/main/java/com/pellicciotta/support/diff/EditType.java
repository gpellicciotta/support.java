// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.diff;

/**
 *  Enumeration of edit types.
 */
public enum EditType {
  /** Keep an item, so really: don't edit */
  KEEP,
  /** Delete an item (from sequence A). */
  DELETE,
  /** Insert an item (from sequence B). */
  INSERT
}
