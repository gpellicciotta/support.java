// Copyright (c) Giovanni Pellicciotta

/**
 *  Diff algorithms that compute the longest common subsequence (LCS) and shortest edit script (SES) of two sequences.
 *  <p>
 *  {@link com.pellicciotta.support.diff.DiffAlgorithm} is the common contract; the implementations are Myers
 *  (greedy, reverse greedy and linear space), histogram and patience diff, selectable through
 *  {@link com.pellicciotta.support.diff.StandardDiffAlgorithms}. Results are expressed as
 *  {@link com.pellicciotta.support.diff.MatchPair}s, {@link com.pellicciotta.support.diff.EditCommand}s and
 *  {@link com.pellicciotta.support.diff.EditRegion}s.</p>
 *  <p>
 *  {@link com.pellicciotta.support.diff.DiffUtils} offers static convenience entry points, and
 *  {@link com.pellicciotta.support.diff.StreamingDiff} diffs large iterators chunk by chunk, reporting to a
 *  {@link com.pellicciotta.support.diff.DiffProcessor} such as
 *  {@link com.pellicciotta.support.diff.CollectingDiffProcessor}.</p>
 */
package com.pellicciotta.support.diff;
