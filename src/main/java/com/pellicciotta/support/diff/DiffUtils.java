// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.diff;

import java.util.*;

/**
 *  Utilities for calculating LCS or SES, and also for representing them.
 */
public final class DiffUtils {
  // <editor-fold desc="Internal Support Routines">

  private DiffUtils() { }

  static final boolean ENABLE_LOGGING = false; // Only while debugging

  public static <T> MatchPair createOffsetMatchPair(MatchPair original, int aOffset, int bOffset) {
    if (original instanceof ExtendedMatchPair) {
      return new ExtendedMatchPair(aOffset + original.indexA, bOffset + original.indexB, ((ExtendedMatchPair) original).val);
    }
    return new MatchPair(aOffset + original.indexA, bOffset + original.indexB);
  }

  static <T> MatchPair createMatchPair(int aItemToKeepIndex, int bItemToKeepIndex) {
    return new MatchPair(aItemToKeepIndex, bItemToKeepIndex);
  }

  static <T> MatchPair createMatchPair(T[] a, int aItemToKeepIndex, T[] b, int bItemToKeepIndex) {
    if (ENABLE_LOGGING) {
      Object val = a[aItemToKeepIndex];
      return new ExtendedMatchPair(aItemToKeepIndex, bItemToKeepIndex, val);
    }
    else {
      return new MatchPair(aItemToKeepIndex, bItemToKeepIndex);
    }
  }

  public static <T> EditCommand createOffsetCommand(EditCommand original, int aOffset, int bOffset) {
    if (original instanceof ExtendedEditCommand) {
      return new ExtendedEditCommand(original.type, aOffset + original.startInA, aOffset + original.endInA, bOffset + original.startInB, bOffset + original.endInB, ((ExtendedEditCommand) original).val);
    }
    return new EditCommand(original.type, aOffset + original.startInA, aOffset + original.endInA, bOffset + original.startInB, bOffset + original.endInB);
  }

  static <T> EditCommand createKeepCommand(int aItemToKeepIndex, int bItemToKeepIndex) {
    return new EditCommand(EditType.KEEP, aItemToKeepIndex, aItemToKeepIndex + 1, bItemToKeepIndex, bItemToKeepIndex + 1);
  }

  static <T> EditCommand createKeepCommand(T[] a, int aItemToKeepIndex, T[] b, int bItemToKeepIndex) {
    if (ENABLE_LOGGING) {
      Object val = a[aItemToKeepIndex];
      return new ExtendedEditCommand(EditType.KEEP, aItemToKeepIndex, aItemToKeepIndex + 1, bItemToKeepIndex, bItemToKeepIndex + 1, val);
    }
    else {
      return new EditCommand(EditType.KEEP, aItemToKeepIndex, aItemToKeepIndex + 1, bItemToKeepIndex, bItemToKeepIndex + 1);
    }
  }

  static <T> EditCommand createDeleteCommand(int aItemToDeleteIndex, int bItemIndex) {
    return new EditCommand(EditType.DELETE, aItemToDeleteIndex, aItemToDeleteIndex + 1, bItemIndex, bItemIndex);
  }

  static <T> EditCommand createDeleteCommand(int aItemToDeleteIndex, int bItemIndex, Object aVal) {
    if (ENABLE_LOGGING && (aVal != null)) {
      return new ExtendedEditCommand(EditType.DELETE, aItemToDeleteIndex, aItemToDeleteIndex + 1, bItemIndex, bItemIndex, aVal);
    }
    else {
      return new EditCommand(EditType.DELETE, aItemToDeleteIndex, aItemToDeleteIndex + 1, bItemIndex, bItemIndex);
    }
  }

  static <T> EditCommand createDeleteCommand(T[] a, int aItemToDeleteIndex, int bItemIndex) {
    if (ENABLE_LOGGING) {
      Object val = a[aItemToDeleteIndex];
      return new ExtendedEditCommand(EditType.DELETE, aItemToDeleteIndex, aItemToDeleteIndex + 1, bItemIndex, bItemIndex, val);
    }
    else {
      return new EditCommand(EditType.DELETE, aItemToDeleteIndex, aItemToDeleteIndex + 1, bItemIndex, bItemIndex);
    }
  }

  static <T> EditCommand createInsertCommand(int insertAfterAItemIndex, int bItemToInsertIndex) {
    return new EditCommand(EditType.INSERT, insertAfterAItemIndex, insertAfterAItemIndex, bItemToInsertIndex, bItemToInsertIndex + 1);
  }

  static <T> EditCommand createInsertCommand(int insertAfterAItemIndex, int bItemToInsertIndex, Object bVal) {
    if (ENABLE_LOGGING && (bVal != null)) {
      return new ExtendedEditCommand(EditType.INSERT, insertAfterAItemIndex, insertAfterAItemIndex, bItemToInsertIndex, bItemToInsertIndex + 1, bVal);
    }
    else {
      return new EditCommand(EditType.INSERT, insertAfterAItemIndex, insertAfterAItemIndex, bItemToInsertIndex, bItemToInsertIndex + 1);
    }
  }

  static <T> EditCommand createInsertCommand(int insertAfterAItemIndex, T[] b, int bItemToInsertIndex) {
    if (ENABLE_LOGGING) {
      Object val = b[bItemToInsertIndex];
      return new ExtendedEditCommand(EditType.INSERT, insertAfterAItemIndex, insertAfterAItemIndex, bItemToInsertIndex, bItemToInsertIndex + 1, val);
    }
    else {
      return new EditCommand(EditType.INSERT, insertAfterAItemIndex, insertAfterAItemIndex, bItemToInsertIndex, bItemToInsertIndex + 1);
    }
  }

  static <T> EditCommand createEditCommand(EditType type, T[] a, int aStartIndex, int aEndIndex, T[] b, int bStartIndex, int bEndIndex) {
    if (ENABLE_LOGGING) {
      Object val = null;
      switch (type) {
        case INSERT:
          val = b[bStartIndex];
          break;
        case KEEP:
        case DELETE:
          val = a[aStartIndex];
          break;
      }
      return new ExtendedEditCommand(type, aStartIndex, aEndIndex, bStartIndex, bEndIndex, val);
    }
    else {
      return new EditCommand(type, aStartIndex, aEndIndex, bStartIndex, bEndIndex);
    }
  }

  // </editor-fold>

  /**
   *  Calculate the edit distance (= length of SES) between two sequences.
   *  Uses {@link StandardDiffAlgorithms#MYERS_LINEAR_SPACE}.
   *
   *  @param a The first sequence.
   *  @param b The second sequence.
   *  @return The size of shortest edit script to transfer sequence a into sequence b.
   */
  public static <T> int dist(T[] a, T[] b) {
    return dist(StandardDiffAlgorithms.MYERS_LINEAR_SPACE, a, 0, a.length, b, 0, b.length);
  }

  /**
   *  Calculate the edit distance (= length of SES) between two sequences.
   *  Uses {@link StandardDiffAlgorithms#MYERS_LINEAR_SPACE}.
   *
   *  @param a The first sequence.
   *  @param aStart The first element to consider within a. An inclusive lower bound.
   *  @param aEnd The element one beyond the last to consider within a. An exclusive upper bound.
   *  @param b The second sequence.
   *  @param bStart The first element to consider within b. An inclusive lower bound.
   *  @param bEnd The element one beyond the last to consider within b. An exclusive upper bound.
   *  @return The size of shortest edit script to transfer sequence a into sequence b.
   */
  public static <T> int dist(T[] a, int aStart, int aEnd, T[] b, int bStart, int bEnd) {
    return dist(StandardDiffAlgorithms.MYERS_LINEAR_SPACE, a, aStart, aEnd, b, bStart, bEnd);
  }

  /**
   *  Calculate the edit distance (= length of SES) between two sequences.
   *
   *  @param algo The builtin algorithm to be used.
   *  @param a The first sequence.
   *  @param b The second sequence.
   *  @return The size of shortest edit script to transfer sequence a into sequence b.
   */
  public static <T> int dist(StandardDiffAlgorithms algo, boolean includeLcs, T[] a, T[] b) {
    return dist(algo, a, 0, a.length, b, 0, b.length);
  }

  /**
   *  Calculate the edit distance (= length of SES) between two sequences.
   *  Uses {@link StandardDiffAlgorithms#MYERS_LINEAR_SPACE}.
   *
   *  @param a The first sequence.
   *  @param b The second sequence.
   *  @return The size of shortest edit script to transfer sequence a into sequence b.
   */
  public static <T> int dist(StandardDiffAlgorithms algo, T[] a, T[] b) {
    return dist(algo, a, 0, a.length, b, 0, b.length);
  }

  /**
   *  Calculate the edit distance (= length of SES) between two sequences.
   *
   *  @param algo The builtin algorithm to be used.
   *  @param a The first sequence.
   *  @param aStart The first element to consider within a. An inclusive lower bound.
   *  @param aEnd The element one beyond the last to consider within a. An exclusive upper bound.
   *  @param b The second sequence.
   *  @param bStart The first element to consider within b. An inclusive lower bound.
   *  @param bEnd The element one beyond the last to consider within b. An exclusive upper bound.
   *  @return The size of shortest edit script to transfer sequence a into sequence b.
   */
  public static <T> int dist(StandardDiffAlgorithms algo, T[] a, int aStart, int aEnd, T[] b, int bStart, int bEnd) {
    DiffAlgorithm<T> engine = create(algo);
    engine.setSequenceA(a, aStart, aEnd);
    engine.setSequenceB(b, bStart, bEnd);
    return engine.calculateEditDistance();
  }

  /**
   *  Calculate the shortest edit script to transfer sequence a into sequence b.
   *  Uses {@link StandardDiffAlgorithms#MYERS_LINEAR_SPACE} and doesn't include KEEP edit commands.
   *
   *  @param a The first sequence.
   *  @param b The second sequence.
   *  @return The shortest edit script to transfer sequence a into sequence b.
   */
  public static <T> List<EditCommand> ses(T[] a, T[] b) {
    return ses(StandardDiffAlgorithms.MYERS_LINEAR_SPACE, false, a, 0, a.length, b, 0, b.length);
  }

  /**
   *  Calculate the shortest edit script to transfer sequence a into sequence b.
   *  Uses {@link StandardDiffAlgorithms#MYERS_LINEAR_SPACE} and doesn't include KEEP edit commands.
   *
   *  @param a The first sequence.
   *  @param aStart The first element to consider within a. An inclusive lower bound.
   *  @param aEnd The element one beyond the last to consider within a. An exclusive upper bound.
   *  @param b The second sequence.
   *  @param bStart The first element to consider within b. An inclusive lower bound.
   *  @param bEnd The element one beyond the last to consider within b. An exclusive upper bound.
   *  @return The shortest edit script to transfer sequence a into sequence b.
   */
  public static <T> List<EditCommand> ses(T[] a, int aStart, int aEnd, T[] b, int bStart, int bEnd) {
    return ses(StandardDiffAlgorithms.MYERS_LINEAR_SPACE, false, a, aStart, aEnd, b, bStart, bEnd);
  }

  /**
   *  Calculate the shortest edit script to transfer sequence a into sequence b.
   *
   *  @param algo The builtin algorithm to be used.
   *  @param includeLcs Whether to also include KEEP edit commands.
   *  @param a The first sequence.
   *  @param b The second sequence.
   *  @return The shortest edit script to transfer sequence a into sequence b.
   */
  public static <T> List<EditCommand> ses(StandardDiffAlgorithms algo, boolean includeLcs, T[] a, T[] b) {
    return ses(algo, includeLcs, a, 0, a.length, b, 0, b.length);
  }

  /**
   *  Calculate the shortest edit script to transfer sequence a into sequence b.
   *  Uses {@link StandardDiffAlgorithms#MYERS_LINEAR_SPACE}.
   *
   *  @param includeLcs Whether to also include KEEP edit commands.
   *  @param a The first sequence.
   *  @param aStart The first element to consider within a. An inclusive lower bound.
   *  @param aEnd The element one beyond the last to consider within a. An exclusive upper bound.
   *  @param b The second sequence.
   *  @param bStart The first element to consider within b. An inclusive lower bound.
   *  @param bEnd The element one beyond the last to consider within b. An exclusive upper bound.
   *  @return The shortest edit script to transfer sequence a into sequence b.
   */
  public static <T> List<EditCommand> ses(boolean includeLcs, T[] a, int aStart, int aEnd, T[] b, int bStart, int bEnd) {
    return ses(StandardDiffAlgorithms.MYERS_LINEAR_SPACE, includeLcs, a, aStart, aEnd, b, bStart, bEnd);
  }

  /**
   *  Calculate the shortest edit script to transfer sequence a into sequence b.
   *
   *  @param algo The builtin algorithm to be used.
   *  @param includeLcs Whether to also include KEEP edit commands.
   *  @param a The first sequence.
   *  @param aStart The first element to consider within a. An inclusive lower bound.
   *  @param aEnd The element one beyond the last to consider within a. An exclusive upper bound.
   *  @param b The second sequence.
   *  @param bStart The first element to consider within b. An inclusive lower bound.
   *  @param bEnd The element one beyond the last to consider within b. An exclusive upper bound.
   *  @return The shortest edit script to transfer sequence a into sequence b.
   */
  public static <T> List<EditCommand> ses(StandardDiffAlgorithms algo, boolean includeLcs, T[] a, int aStart, int aEnd, T[] b, int bStart, int bEnd) {
    DiffAlgorithm<T> engine = create(algo);
    engine.setSequenceA(a, aStart, aEnd);
    engine.setSequenceB(b, bStart, bEnd);
    return engine.calculateEditScript(includeLcs);
  }

  /**
   *  Calculate the shortest edit script based on an existing longest common subsequence.
   *
   *  @param lcs The LCS(a, b) to base the SES off.
   *  @param aStart The first element to consider within a. An inclusive lower bound.
   *  @param aEnd The element one beyond the last to consider within a. An exclusive upper bound.
   *  @param bStart The first element to consider within b. An inclusive lower bound.
   *  @param bEnd The element one beyond the last to consider within b. An exclusive upper bound.
   *  @param includeKeepCommands Whether to also have KEEP commands indicating matches.
   *  @return The shortest edit script to transfer sequence a into sequence b.
   */
  public static <T> List<EditCommand> sesFromMatches(List<MatchPair> lcs, int aStart, int aEnd, int bStart, int bEnd, boolean includeKeepCommands) {
    List<EditCommand> ses = new ArrayList<>();
    int i = aStart;
    int j = bStart;
    for (MatchPair mp : lcs) {
      while (i < mp.indexA) {
        ses.add(DiffUtils.createDeleteCommand(i, j));
        i += 1;
      }
      while (j < mp.indexB) {
        ses.add(DiffUtils.createInsertCommand(i, j));
        j += 1;
      }
      if (includeKeepCommands) {
        ses.add(DiffUtils.createKeepCommand(i, j));
      }
      i += 1;
      j += 1;
    }
    while (i < aEnd) {
      ses.add(DiffUtils.createDeleteCommand(i, j));
      i += 1;
    }
    while (j < bEnd) {
      ses.add(DiffUtils.createInsertCommand(i, j));
      j += 1;
    }
    return ses;
  }

  /**
   *  Calculate the set of match and edit regions based on an LCS.
   *
   *  @param lcs The LCS(a, b) to base the edit/match regions off.
   *  @param aStart The first element to consider within a. An inclusive lower bound.
   *  @param aEnd The element one beyond the last to consider within a. An exclusive upper bound.
   *  @param bStart The first element to consider within b. An inclusive lower bound.
   *  @param bEnd The element one beyond the last to consider within b. An exclusive upper bound.
   *  @return The sequence of match and non-match regions.
   */
  public static <T> List<EditRegion> regionsFromMatches(List<MatchPair> lcs, int aStart, int aEnd, int bStart, int bEnd) {
    List<EditRegion> regions = new ArrayList<>();
    List<EditCommand> diffCommands = new ArrayList<>();
    List<EditCommand> matchCommands = new ArrayList<>();
    int i = aStart;
    int j = bStart;
    for (MatchPair mp : lcs) {
      // Check whether we're continuing a match region:
      if (!matchCommands.isEmpty()) {
        if ((mp.indexA == i) && (mp.indexB == j)) { // Continuing match
          matchCommands.add(DiffUtils.createKeepCommand(i, j));
          i += 1;
          j += 1;
          continue;
        }
        else {
          // End last match region:
          regions.add(new EditRegion(matchCommands));
          matchCommands.clear();
        }
      }
      // Region from last match:
      diffCommands.clear();
      while (i < mp.indexA) {
        diffCommands.add(DiffUtils.createDeleteCommand(i, j));
        i += 1;
      }
      while (j < mp.indexB) {
        diffCommands.add(DiffUtils.createInsertCommand(i, j));
        j += 1;
      }
      if (!diffCommands.isEmpty()) {
        regions.add(new EditRegion(diffCommands));
      }
      // Add match
      matchCommands.add(DiffUtils.createKeepCommand(i, j));
      i += 1;
      j += 1;
    }
    // If still matches:
    if (!matchCommands.isEmpty()) {
      regions.add(new EditRegion(matchCommands));
      matchCommands.clear();
    }
    // If still items: new non-match region
    diffCommands.clear();
    while (i < aEnd) {
      diffCommands.add(DiffUtils.createDeleteCommand(i, j));
      i += 1;
    }
    while (j < bEnd) {
      diffCommands.add(DiffUtils.createInsertCommand(i, j));
      j += 1;
    }
    if (!diffCommands.isEmpty()) {
      regions.add(new EditRegion(diffCommands));
    }
    return regions;
  }

  /**
   *  Calculate the set of difference regions based on an LCS, while including context.
   *
   *  @param lcs The LCS(a, b) to base the edit/match regions off.
   *  @param aStart The first element to consider within a. An inclusive lower bound.
   *  @param aEnd The element one beyond the last to consider within a. An exclusive upper bound.
   *  @param bStart The first element to consider within b. An inclusive lower bound.
   *  @param bEnd The element one beyond the last to consider within b. An exclusive upper bound.
   *  @param contextSize The number of items of context to include in each difference region.
   *  @return The sequence of difference regions, including context. Note that regions that overlap based on context will get merged.
   */
  public static <T> List<EditRegion> diffRegionsFromMatches(List<MatchPair> lcs, int aStart, int aEnd, int bStart, int bEnd, int contextSize) {
    List<EditRegion> regions = DiffUtils.regionsFromMatches(lcs, aStart, aEnd, bStart, bEnd);

    List<EditRegion> diffRegions = new ArrayList<>();
    List<EditCommand> currentRegionCommands = new ArrayList<>();

    // Pre-fill with first region context lines, but only if first region is a match region:
    int i = 0;
    EditRegion firstRegion = regions.get(0);
    EditCommand[] firstRegionCommands = firstRegion.getCommands();
    if (firstRegionCommands[0].getType() == EditType.KEEP) {
      for (int j = Math.max(0, firstRegionCommands.length - contextSize); j < firstRegionCommands.length; j++) {
        currentRegionCommands.add(firstRegionCommands[j]);
      }
      i += 1;
    }

    // Now find actual regions:
    for (; i < regions.size(); i++) {
      // Edit region:
      EditRegion region = regions.get(i);
      EditCommand[] regionCommands = region.getCommands();
      assert regionCommands[0].getType() != EditType.KEEP;
      // Add all:
      for (int j = 0; j < regionCommands.length; j++) {
        currentRegionCommands.add(regionCommands[j]);
      }
      // Check whether there is a next match region to merge OR add context items from:
      if (i + 1 < regions.size()) {
        EditRegion nextRegion = regions.get(i + 1);
        EditCommand[] nextRegionCommands = nextRegion.getCommands();
        assert nextRegionCommands[0].getType() == EditType.KEEP;
        if (nextRegionCommands.length > 2 * contextSize) {
          // Take post-context
          for (int j = 0; j < contextSize; j++) {
            currentRegionCommands.add(nextRegionCommands[j]);
          }
          // End region
          EditRegion newRegion = new EditRegion(currentRegionCommands);
          diffRegions.add(newRegion);
          currentRegionCommands.clear();
          // Start next region already with context
          for (int j = Math.max(0, nextRegionCommands.length - contextSize); j < nextRegionCommands.length; j++) {
            currentRegionCommands.add(nextRegionCommands[j]);
          }
          i += 1;
        }
        else { // Just merge entirely
          for (int j = 0; j < nextRegionCommands.length; j++) {
            currentRegionCommands.add(nextRegionCommands[j]);
          }
          i += 1;
        }
      }
      else {
        EditRegion newRegion = new EditRegion(currentRegionCommands);
        diffRegions.add(newRegion);
        currentRegionCommands.clear();
      }
    }

    // Add last region
    if (!currentRegionCommands.isEmpty()) {
      EditRegion newRegion = new EditRegion(currentRegionCommands);
      diffRegions.add(newRegion);
      currentRegionCommands.clear();
    }

    return diffRegions;
  }

  /**
   *  Calculate the longest common subsequence based on an existing shortest edit script
   *
   *  @param ses The SES to base the LCS off.
   *  @param aStart The first element to consider within a. An inclusive lower bound.
   *  @param aEnd The element one beyond the last to consider within a. An exclusive upper bound.
   *  @param bStart The first element to consider within b. An inclusive lower bound.
   *  @param bEnd The element one beyond the last to consider within b. An exclusive upper bound.
   *  @return The longest common subsequence of matching items.
   */
  public static <T> List<MatchPair> lcsFromEdits(List<EditCommand> ses, int aStart, int aEnd, int bStart, int bEnd) {
    List<MatchPair> lcs = new ArrayList<>();
    int i = aStart;
    int j = bStart;
    for (EditCommand ec : ses) {
      if (ec.type == EditType.KEEP) {
        continue ;
      }
      if (ec.type == EditType.DELETE) {
        while (i < ec.startInA) {
          lcs.add(DiffUtils.createMatchPair(i, j));
          i += 1;
          j += 1;
        }
        i = ec.endInA;
      }
      else if (ec.type == EditType.INSERT) {
        while (j < ec.startInB) {
          lcs.add(DiffUtils.createMatchPair(i, j));
          i += 1;
          j += 1;
        }
        j = ec.endInB;
      }
    }
    while ((i < aEnd) && (j < bEnd)) {
      lcs.add(DiffUtils.createMatchPair(i, j));
      i += 1;
      j += 1;
    }
    return lcs;
  }

  /**
   *  Calculate the longest common subsequence between sequences a and b.
   *  Uses {@link StandardDiffAlgorithms#MYERS_LINEAR_SPACE}.
   *
   *  @param a The first sequence.
   *  @param b The second sequence.
   *  @return The longest common subsequence of matching items.
   */
  public static <T> List<MatchPair> lcs(T[] a, T[] b) {
    return lcs(StandardDiffAlgorithms.MYERS_LINEAR_SPACE, a, 0, a.length, b, 0, b.length);
  }

  /**
   *  Calculate the longest common subsequence between sequences a and b.
   *
   *  @param algo The builtin algorithm to be used.
   *  @param a The first sequence.
   *  @param b The second sequence.
   *  @return The longest common subsequence of matching items.
   */
  public static <T> List<MatchPair> lcs(StandardDiffAlgorithms algo, T[] a, T[] b) {
    return lcs(algo, a, 0, a.length, b, 0, b.length);
  }

  /**
   *  Calculate the longest common subsequence between sequences a and b.
   *  Uses {@link StandardDiffAlgorithms#MYERS_LINEAR_SPACE}.
   *
   *  @param a The first sequence.
   *  @param aStart The first element to consider within a. An inclusive lower bound.
   *  @param aEnd The element one beyond the last to consider within a. An exclusive upper bound.
   *  @param b The second sequence.
   *  @param bStart The first element to consider within b. An inclusive lower bound.
   *  @param bEnd The element one beyond the last to consider within b. An exclusive upper bound.
   *  @return The longest common subsequence of matching items.
   */
  public static <T> List<MatchPair> lcs(T[] a, int aStart, int aEnd, T[] b, int bStart, int bEnd) {
    return lcs(StandardDiffAlgorithms.MYERS_LINEAR_SPACE, a, aStart, aEnd, b, bStart, bEnd);
  }

  /**
   *  Calculate the longest common subsequence between sequences a and b.
   *
   *  @param algo The builtin algorithm to be used.
   *  @param a The first sequence.
   *  @param aStart The first element to consider within a. An inclusive lower bound.
   *  @param aEnd The element one beyond the last to consider within a. An exclusive upper bound.
   *  @param b The second sequence.
   *  @param bStart The first element to consider within b. An inclusive lower bound.
   *  @param bEnd The element one beyond the last to consider within b. An exclusive upper bound.
   *  @return The longest common subsequence of matching items.
   */
  public static <T> List<MatchPair> lcs(StandardDiffAlgorithms algo, T[] a, int aStart, int aEnd, T[] b, int bStart, int bEnd) {
    DiffAlgorithm<T> engine = create(algo);
    engine.setSequenceA(a, aStart, aEnd);
    engine.setSequenceB(b, bStart, bEnd);
    return engine.calculateLongestCommonSubsequence();
  }

  /**
   *  Calculate the LCS and SES between sequences a and b.
   *
   *  @param algo The builtin algorithm to be used.
   *  @param a The first sequence.
   *  @param aStart The first element to consider within a. An inclusive lower bound.
   *  @param aEnd The element one beyond the last to consider within a. An exclusive upper bound.
   *  @param b The second sequence.
   *  @param bStart The first element to consider within b. An inclusive lower bound.
   *  @param bEnd The element one beyond the last to consider within b. An exclusive upper bound.
   *  @param processor The object that will be notified, chunk by chunk, of diff results.
   */
  public static <T> void diff(DiffAlgorithm<T> algo, T[] a, int aStart, int aEnd, T[] b, int bStart, int bEnd, DiffProcessor processor) {
    DiffAlgorithm<T> engine = algo;
    engine.setSequenceA(a, aStart, aEnd);
    engine.setSequenceB(b, bStart, bEnd);
    processor.onStarted(a, aStart, aEnd, b, bStart, bEnd);
    processor.onChunkStarted(0);
    List<MatchPair> lcs = engine.calculateLongestCommonSubsequence();
    List<EditCommand> ses = engine.calculateEditScript(false);
    reportMatchesAndEdits(lcs, ses, processor);
    processor.onChunkEnded(0, aEnd - aStart, bEnd - bStart, ses.size(), lcs.size());
    processor.onEnded(aEnd - aStart, bEnd - bStart, ses.size(), lcs.size());
  }

  /**
   *  Calculate the LCS and SES between sequences a and b.
   *
   *  @param algo The builtin algorithm to be used.
   *  @param a The first sequence.
   *  @param aStart The first element to consider within a. An inclusive lower bound.
   *  @param aEnd The element one beyond the last to consider within a. An exclusive upper bound.
   *  @param b The second sequence.
   *  @param bStart The first element to consider within b. An inclusive lower bound.
   *  @param bEnd The element one beyond the last to consider within b. An exclusive upper bound.
   *  @param processor The object that will be notified, chunk by chunk, of diff results.
   */
  public static <T> void diff(StandardDiffAlgorithms algo, T[] a, int aStart, int aEnd, T[] b, int bStart, int bEnd, DiffProcessor processor) {
    DiffAlgorithm<T> engine = create(algo);
    engine.setSequenceA(a, aStart, aEnd);
    engine.setSequenceB(b, bStart, bEnd);
    processor.onStarted(a, aStart, aEnd, b, bStart, bEnd);
    processor.onChunkStarted(0);
    List<MatchPair> lcs = engine.calculateLongestCommonSubsequence();
    List<EditCommand> ses = engine.calculateEditScript(false);
    reportMatchesAndEdits(lcs, ses, processor);
    processor.onChunkEnded(0, aEnd - aStart, bEnd - bStart, ses.size(), lcs.size());
    processor.onEnded(aEnd - aStart, bEnd - bStart, ses.size(), lcs.size());
  }

  /**
   *  Calculate the LCS and SES between iterators a and b.
   *
   *  @param algo The algorithm to be used.
   *  @param a The first iterator.
   *  @param b The second iterator.
   *  @param batchSize The max. number of elements to read before calculating an LCS/SES.
   *  @param processor The object that will be notified, chunk by chunk, of diff results.
   */
  public static <T> void diff(DiffAlgorithm<T> algo, Iterator<T> a, Iterator<T> b, int batchSize, DiffProcessor processor) {
    StreamingDiff<T> streamingDiff = new StreamingDiff<>(algo, batchSize, processor);
    streamingDiff.diff(a, b);
  }

  /**
   *  Calculate the LCS and SES between iterators a and b.
   *
   *  @param algo The builtin algorithm to be used.
   *  @param a The first iterator.
   *  @param b The second iterator.
   *  @param batchSize The max. number of elements to read before calculating an LCS/SES.
   *  @param processor The object that will be notified, chunk by chunk, of diff results.
   */
  public static <T> void diff(StandardDiffAlgorithms algo, Iterator<T> a, Iterator<T> b, int batchSize, DiffProcessor processor) {
    StreamingDiff<T> streamingDiff = new StreamingDiff<>(create(algo), batchSize, processor);
    streamingDiff.diff(a, b);
  }

  static void reportMatchesAndEdits(List<MatchPair> lcs, List<EditCommand> ses, DiffProcessor<?> processor) {
    MatchPair mp = null;
    EditCommand ec = null;
    int mIdx = 0;
    int mLen = lcs.size();
    int eIdx = 0;
    int eLen = ses.size();
    while ((mIdx < mLen) && (eIdx < eLen)) {
      if (mp == null) {
        mp = lcs.get(mIdx);
      }
      if (ec == null) {
        ec = ses.get(eIdx);
      }
      switch (ec.type) {
        case KEEP:
        case DELETE: // Compare a indexes
          if (mp.indexA <= ec.startInA) {
            processor.onMatch(mp);
            mp = null;
            mIdx += 1; // So in the next turn, we take the next match point
          }
          else {
            processor.onEdit(ec);
            ec = null;
            eIdx += 1; // So in the next turn, we take the next edit command
          }
          break;
        case INSERT: // Compare b indexes
          if (mp.indexB <= ec.startInB) {
            processor.onMatch(mp);
            mp = null;
            mIdx += 1; // So in the next turn, we take the next match point
          }
          else {
            processor.onEdit(ec);
            ec = null;
            eIdx += 1; // So in the next turn, we take the next edit command
          }
          break;
      }
    }
    // Process remaining matches OR edit commands
    for ( ; mIdx < mLen; mIdx += 1) {
      mp = lcs.get(mIdx);
      processor.onMatch(mp);
    }
    for ( ; eIdx < eLen; eIdx += 1) {
      ec = ses.get(eIdx);
      processor.onEdit(ec);
    }
  }

  /**
   *  Create an instance of the builtin diff algorithm.
   *
   *  @param algo The algorithm to be created.
   *  @return An instance of the builtin algorithm.
   */
  public static <T> DiffAlgorithm<T> create(StandardDiffAlgorithms algo) {
    switch (algo) {
      case MYERS_LINEAR_SPACE:
        return new MyersLinearSpaceDiff<T>();
      case MYERS_GREEDY:
        return new MyersGreedyDiff<>();
      case MYERS_REVERSE_GREEDY:
        return new MyersReverseGreedyDiff<>();
      case HISTOGRAM:
        return new HistogramDiff<>();
      case PATIENCE_MYERS:
        return new PatienceDiff<>(new MyersLinearSpaceDiff<>());
      case PATIENCE_HISTOGRAM:
        return new PatienceDiff<>(new HistogramDiff<>());
    }
    throw new IllegalStateException("We should never get here");
  }
}
