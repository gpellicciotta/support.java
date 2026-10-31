// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.diff;

import java.util.*;

/**
 *  A {@link DiffProcessor} that simply collects alls matches and edit commands.
 *
 *  @param <T> The type of elements in the sequences or iterators being compared.
 */
public class CollectingDiffProcessor<T> implements DiffProcessor<T> {
  public CollectingDiffProcessor() { }

  private final List<MatchPair> matches = new ArrayList<>();

  public List<MatchPair> getLongestCommonSubsequence() {
    return this.matches;
  }

  private final List<EditCommand> edits = new ArrayList<>();

  public List<EditCommand> getEditScript() {
    return this.edits;
  }

  public void onStarted(Iterator<T> itA, Iterator<T> itB) {
    this.matches.clear();
    this.edits.clear();
  }

  @Override
  public void onStarted(T[] seqA, int aStart, int aEnd, T[] seqB, int bStart, int bEnd) {
    this.matches.clear();
    this.edits.clear();
  }

  public void onEdit(EditCommand ed) {
    this.edits.add(ed);
  }

  public void onMatch(MatchPair mp) {
    this.matches.add(mp);
  }
}
