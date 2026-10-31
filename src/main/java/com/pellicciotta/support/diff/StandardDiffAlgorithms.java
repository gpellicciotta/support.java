// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.diff;

import com.pellicciotta.support.utils.Contract;

/**
 *  Enumeration of builtin diff algorithms.
 */
public enum StandardDiffAlgorithms {
  /** Myers diff with linear space usage. */
  MYERS_LINEAR_SPACE,
  /** Myers diff: original, forward search, algorithm. */
  MYERS_GREEDY,
  /** Myers diff: original, backward search, algorithm. */
  MYERS_REVERSE_GREEDY,
  /** Histogram diff: divide-and-conquer based on lowest-occurring but common item. */
  HISTOGRAM,
  /** Patience diff: divide-and-conquer based on unique but common item with fallback to Myers diff for the parts divided. */
  PATIENCE_MYERS,
  /** Patience diff: divide-and-conquer based on unique but common item with fallback to Histogram diff for the parts divided. */
  PATIENCE_HISTOGRAM
}
