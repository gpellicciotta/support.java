// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.diff;

import java.util.*;

/**
 *  A {@link DiffProcessor} that simply collects alls matches and edit commands.
 *
 *  @param <T> The type of elements in the sequences or iterators being compared.
 */
public class CollectingDiffProcessor<T> implements DiffProcessor<T> {
  /** Create a processor with empty result lists. */
  public CollectingDiffProcessor() { }

  private final List<MatchPair> matches = new ArrayList<>();

  /** @return The live list of {@link MatchPair}s collected so far, in the order they were reported. */
  public List<MatchPair> getLongestCommonSubsequence() {
    return this.matches;
  }

  private final List<EditCommand> edits = new ArrayList<>();

  /** @return The live list of {@link EditCommand}s collected so far, in the order they were reported. */
  public List<EditCommand> getEditScript() {
    return this.edits;
  }

  /**
   * Clears any previously collected matches and edit commands.
   *
   * @param itA The first stream; not used.
   * @param itB The second stream; not used.
   */
  public void onStarted(Iterator<T> itA, Iterator<T> itB) {
    this.matches.clear();
    this.edits.clear();
  }

  @Override
  public void onStarted(T[] seqA, int aStart, int aEnd, T[] seqB, int bStart, int bEnd) {
    this.matches.clear();
    this.edits.clear();
  }

  /**
   * Collects the edit command.
   *
   * @param ed The edit command to collect.
   */
  public void onEdit(EditCommand ed) {
    this.edits.add(ed);
  }

  /**
   * Collects the match-pair.
   *
   * @param mp The match-pair to collect.
   */
  public void onMatch(MatchPair mp) {
    this.matches.add(mp);
  }
}
