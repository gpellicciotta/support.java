
// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.diff;

import java.util.*;

import static com.pellicciotta.support.diff.DiffUtils.ENABLE_LOGGING;

/**
 *  Based on: https://bramcohen.livejournal.com/73318.html
 *  Basically find the most unique common item and divide the sequences based on that item' match-pair.
 */
public class HistogramDiff<T> extends DiffAlgorithmBase<T> {
  /** Create a histogram diff algorithm. */
  public HistogramDiff() { }

  @Override
  public String name() {
    return "histogram diff";
  }

  @Override
  protected void calculateNormalCase(T[] aSeq, int aSeqStart, int aSeqEnd, T[] bSeq, int bSeqStart, int bSeqEnd) {
    if (ENABLE_LOGGING) {
      this.log.debug("Normal case calculation for A [%d, %d] and B [%d, %d]", aSeqStart, aSeqEnd - 1, bSeqStart, bSeqEnd - 1);
    }
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
    // Determine lowest-occurrence item that appears in both sequences
    int occ = Integer.MAX_VALUE;
    ItemInfo minOcc = null;
    for (ItemInfo ii : items.values()) {
      if ((ii.aIndex >= 0) && (ii.bIndex >= 0) && (ii.aCount + ii.bCount < occ)) {
        minOcc = ii;
        occ = ii.aCount + ii.bCount;
      }
    }
    // Edge case: there are no matching items
    if (minOcc == null) {
      if (ENABLE_LOGGING) {
        this.log.debug("None of %d items are commonly occurring", items.size());
      }
      this.createNoMatchEditScript(aSeq, aSeqStart, aSeqEnd, bSeq, bSeqStart, bSeqEnd);
    }
    else {
      if (ENABLE_LOGGING) {
        this.log.debug("Minimal occurrence is %d for item A[%d]=B[%d]=%s", occ, minOcc.aIndex, minOcc.bIndex, aSeq[minOcc.aIndex]);
      }
      // Normal case: divide on the least occurrence common item
      this.calculateMatchesAndEdits(aSeq, aSeqStart, minOcc.aIndex, bSeq, bSeqStart, minOcc.bIndex);
      this.addMatch(minOcc.aIndex, minOcc.bIndex);
      this.calculateMatchesAndEdits(aSeq, minOcc.aIndex + 1, aSeqEnd, bSeq, minOcc.bIndex + 1, bSeqEnd);
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
