
// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.diff;

import java.util.*;

import static com.pellicciotta.support.diff.DiffUtils.ENABLE_LOGGING;

/**
 *  Based on: https://bramcohen.livejournal.com/73318.html
 */
public class PatienceDiff<T> extends DiffAlgorithmBase<T> {
  public PatienceDiff(DiffAlgorithm<T> fallback) {
    this.fallback = (fallback != null) ? fallback : new MyersLinearSpaceDiff<>();
  }

  private final DiffAlgorithm<T> fallback;

  @Override
  public String name() {
    return "patience diff with " + this.fallback.name();
  }

  @Override
  protected void calculateNormalCase(T[] aSeq, int aSeqStart, int aSeqEnd, T[] bSeq, int bSeqStart, int bSeqEnd) {
    if (ENABLE_LOGGING) {
      this.log.debug("Normal case calculation for A [%d, %d] and B [%d, %d]", aSeqStart, aSeqEnd - 1, bSeqStart, bSeqEnd - 1);
    }
    // Determine common unique lines, and ordered in a way that works both for sequence A and B
    List<Match> lis = this.uniqueCommonAndIncreasingItems(aSeq, aSeqStart, aSeqEnd, bSeq, bSeqStart, bSeqEnd);
    if (lis.isEmpty()) {
      if (ENABLE_LOGGING) {
        this.log.debug("No common items so falling back to calculation with %s", this.fallback.name());
      }
      this.fallback.setSequenceA(aSeq, aSeqStart, aSeqEnd);
      this.fallback.setSequenceB(bSeq, bSeqStart, bSeqEnd);
      for (EditCommand ed : this.fallback.calculateEditScript(true)) {
        this.cachedEs.add(ed);
        if (ed.type == EditType.KEEP) {
          MatchPair mp = new MatchPair(ed.startInA, ed.startInB);
          this.cachedLcs.add(mp);
        }
      }
    }
    else { // Synchronize based on that common increasing sequence of match points:
      if (ENABLE_LOGGING) {
        this.log.debug("%d common items so dividing sequences", lis.size());
      }
      int i = aSeqStart;
      int j = bSeqStart;
      for (Match m : lis) {
        this.calculateMatchesAndEdits(aSeq, i, m.aIndex, bSeq, j, m.bIndex);
        this.addMatch(m.aIndex, m.bIndex);
        i = m.aIndex + 1;
        j = m.bIndex + 1;
      }
      if ((i < aSeqEnd) || (j < bSeqEnd)) {
        this.calculateMatchesAndEdits(aSeq, i, aSeqEnd, bSeq, j, bSeqEnd);
      }
    }
  }

  protected List<Match> uniqueCommonAndIncreasingItems(T[] aSeq, int aSeqStart, int aSeqEnd, T[] bSeq, int bSeqStart, int bSeqEnd) {
    // Determine common unique lines:
    Map<Integer, ItemInfo> items = new HashMap<>();
    for (int i = aSeqStart; i < aSeqEnd; i++) {
      T item = aSeq[i];
      int itemHash = item.hashCode();
      ItemInfo itemInfo = items.get(itemHash);
      if (itemInfo == null) {
        itemInfo = new ItemInfo(i, 0, -1, 0);
        items.put(itemHash, itemInfo);
      }
      itemInfo.aCount += 1;
    }
    for (int j = bSeqStart; j < bSeqEnd; j++) {
      T item = bSeq[j];
      int itemHash = item.hashCode();
      ItemInfo itemInfo = items.get(itemHash);
      if (itemInfo == null) {
        itemInfo = new ItemInfo(-1, 0, j, 0);
        items.put(itemHash, itemInfo);
      }
      if (itemInfo.bIndex < 0) {
        itemInfo.bIndex = j;
      }
      itemInfo.bCount += 1;
    }
    // Determine common, yet unique items
    List<Match> commonUniqueItems = new ArrayList<>();
    for (ItemInfo ii : items.values()) {
      if ((ii.aIndex >= 0) && (ii.bIndex >= 0) && (ii.aCount == 1) && (ii.bCount == 1)) {
        commonUniqueItems.add(new Match(ii.aIndex, ii.bIndex, aSeq[ii.aIndex].toString()));
      }
    }
    // No common items:
    if (commonUniqueItems.isEmpty()) {
      return Collections.emptyList();
    }
    // Order according to occurrence in A
    commonUniqueItems.sort(Comparator.comparingInt((Match i) -> i.aIndex));
    List<Match> lis = this.longestIncreasingSubsequence(commonUniqueItems);
    return lis;
  }

  /**
   *  Use patience sort to determine the longest increasing subsequence of items.
   *  @param items Items that are pre-ordered on their aIndex value.
   *  @return The longest subsequence of items that also have increasing bIndex values.
   */
  private List<Match> longestIncreasingSubsequence(List<Match> items) {
    List<Match> stacks = new ArrayList<>();
    // All items go on stacks:
    // 1. First item starts first stack
    // 2. Then always on top of the first (from the left) stack whose current top-item is larger
    //    Also always keep pointer to top-item of one stack before (if any)
    all_items:
    for (Match newItem : items) {
      for (int i = 0; i < stacks.size(); i++) { // TODO: replace by binary search
        Match topItem = stacks.get(i);
        if (topItem.bIndex > newItem.bIndex) {
          Match prevStackTop = null;
          if (i > 0) {
            prevStackTop = stacks.get(i - 1);
          }
          newItem.next = topItem;
          newItem.prev = prevStackTop;
          stacks.set(i, newItem);
          continue all_items;
        }
      }
      // If we get here, we need to start a new stack
      Match prevStackTop = null;
      if (!stacks.isEmpty()) {
        prevStackTop = stacks.get(stacks.size() - 1);
      }
      newItem.prev = prevStackTop;
      stacks.add(newItem);
    }
    // 3. Now go from last stack to first, following pointers and adding items in reverse order
    List<Match> subSeq = new ArrayList<>(stacks.size());
    Match item = stacks.get(stacks.size() - 1);
    subSeq.add(item);
    while (item.prev != null) {
      item = item.prev;
      subSeq.add(0, item);
    }
    return subSeq;
  }

  private static class Match {
    Match(int aIndex, int bIndex, String d) {
      this.aIndex = aIndex;
      this.bIndex = bIndex;
      this.prev = null;
      this.next = null;
      this.d = d;
    }

    private String d;

    private int aIndex;
    private int bIndex;
    private Match prev;
    private Match next;

    @Override
    public String toString() {
      return String.format("A[%d]=B[%d]=%s", this.aIndex, this.bIndex, this.d);
    }
  }

  private static class ItemInfo {
    ItemInfo(int aIndex, int bIndex) {
      this.aIndex = aIndex;
      this.aCount = 0;
      this.bIndex = bIndex;
      this.bCount = 0;
    }

    ItemInfo(int aIndex, int aCount, int bIndex, int bCount) {
      this.aIndex = aIndex;
      this.aCount = aCount;
      this.bIndex = bIndex;
      this.bCount = bCount;
    }

    private int aIndex;
    private int aCount;
    private int bIndex;
    private int bCount;
  }
}
