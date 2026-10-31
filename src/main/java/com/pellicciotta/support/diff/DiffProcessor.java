// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.diff;

import java.util.*;

/**
 *  An interface that can be implemented to be notified of diff-related events, while diff processing is taking place.
 *
 *  @param <T> The type of elements in the sequences or iterators being compared.
 */
public interface DiffProcessor<T> {
  /**
   *  Processing has started on two sequences with known length.
   */
  default void onStarted(T[] seqA, int aStart, int aEnd, T[] seqB, int bStart, int bEnd) {}

  /**
   *  Processing has started on two streams of unknown length.
   */
  default void onStarted(Iterator<T> itA, Iterator<T> itB) {}

  /**
   *  Processing has started on a specific chunk based on items gathered from the two streams.
   */
  default void onChunkStarted(int chunkIndex) {}

  /**
   *  An edit command has been determined.
   */
  default void onEdit(EditCommand ed) {}

  /**
   *  A match-pair has been determined.
   */
  default void onMatch(MatchPair mp) {}

  /**
   *  Processing has ended on a specific chunk based on items gathered from the two streams.
   */
  default void onChunkEnded(int chunkIndex, int aItems, int bItems, int editScriptLength, int lcsLength) {}

  /**
   *  Processing has ended.
   */
  default void onEnded(int aItems, int bItems, int editScriptLength, int lcsLength) {}
}
