// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.tools.diff;

/**
 * Output types.
 */
public enum DiffOutputFormat {
  /**
   * Unified diff text.
   */
  UNIFIED_DIFF,
  /**
   * JSON listing of the shortest edit script.
   */
  JSON_SES,
  /**
   * JSON listing of the longest common subsequence.
   */
  JSON_LCS,
  /**
   * Two-column text, old content on the left and new content on the right.
   */
  SIDE_BY_SIDE
}
