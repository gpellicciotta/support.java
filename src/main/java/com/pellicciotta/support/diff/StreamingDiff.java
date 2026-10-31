// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.diff;

import java.util.Iterator;
import java.util.List;

import com.pellicciotta.support.logging.Log;
import com.pellicciotta.support.logging.LogService;
import com.pellicciotta.support.text.TextBuilder;
import com.pellicciotta.support.utils.Contract;
import static com.pellicciotta.support.diff.DiffUtils.ENABLE_LOGGING;

/**
 *  For diff-ing large input streams.
 *  Works with an underlying {@link DiffAlgorithm} that is being used on successive input chunks.
 *  <p>
 *  If possible, successive chunks will be aligned on match-pairs, but only if there is a match-pair in the
 *  last 25% of items of both input chunks.</p>
 */
public class StreamingDiff<T> {
  /**
   *  Create an engine capable of performing a diff between two item streams.
   *
   *  @param algo The underlying {@link DiffAlgorithm} to be used.
   *  @param maxChunkSize The max. size of individual chunks (= items collected from the input streams) on which the LCS/SES will be determined.
   *  @param processor The object that will be notified, chunk by chunk, of diff results.
   */
  public StreamingDiff(DiffAlgorithm<T> algo, int maxChunkSize, DiffProcessor processor) {
    Contract.isNotNull("processor", processor);
    Contract.isNotNull("algo", algo);
    this.processor = processor;
    this.algo = algo;
    this.maxChunkSize = maxChunkSize;
    this.chunkA = (T[])new Object[maxChunkSize];
    this.chunkB = (T[])new Object[maxChunkSize];
  }

  /** The processor notified of diff results. */
  protected final DiffProcessor processor;
  /** The algorithm applied to each chunk. */
  protected final DiffAlgorithm<T> algo;

  private final int maxChunkSize;
  private T[] chunkA;
  private int chunkAConsumed;
  private int chunkALength;
  private T[] chunkB;
  private int chunkBConsumed;
  private int chunkBLength;
  private Iterator<T> streamA;
  private Iterator<T> streamB;

  /** Logger used for debug output. */
  protected final Log log = LogService.fromCurrentClass();

  /**
   *  Determine SES and LCS between two streams and make the results
   *  available via the {@link DiffProcessor} provided at construction time.
   *
   *  @param streamA The first stream.
   *  @param streamB The second stream.
   */
  public void diff(Iterator<T> streamA, Iterator<T> streamB) {
    this.streamA = streamA;
    this.streamB = streamB;

    this.chunkAConsumed = 0;
    this.chunkALength = 0;
    this.chunkBConsumed = 0;
    this.chunkBLength = 0;

    this.processor.onStarted(streamA, streamB);
    if (ENABLE_LOGGING) {
      LogService.debug("Starting diff of two streams");
    }

    int aLength = 0;
    int bLength = 0;
    int lcsLength = 0;
    int editScriptLength = 0;
    int chunks = 0;
    for (; this.readNextChunk(); chunks++) {
      this.processor.onChunkStarted(chunks);
      if (ENABLE_LOGGING) {
        LogService.debug("Chunk #%d started", chunks);
      }
      // Diff
      this.algo.setSequenceA(this.chunkA, 0, this.chunkALength);
      this.algo.setSequenceB(this.chunkB, 0, this.chunkBLength);
      List<EditCommand> currentEditScript = this.algo.calculateEditScript(); // TODO: if passing true, we don't need the next line and can also simplify optionallyEndOnLastMatchPair
      List<MatchPair> currentMatches = this.algo.calculateLongestCommonSubsequence();
      // Retract to last match-pair, if it occurs in the last part
      this.optionallyEndOnLastMatchPair(currentEditScript, currentMatches);
      final int aOffset = aLength;
      final int bOffset = bLength;
      DiffUtils.reportMatchesAndEdits(currentMatches, currentEditScript, new DiffProcessor<T>() {
        @Override
        public void onEdit(EditCommand ed) {
          StreamingDiff.this.processor.onEdit(DiffUtils.createOffsetCommand(ed, aOffset, bOffset));
        }

        @Override
        public void onMatch(MatchPair mp) {
          StreamingDiff.this.processor.onMatch(DiffUtils.createOffsetMatchPair(mp, aOffset, bOffset));
        }
      });
      this.processor.onChunkEnded(chunks, this.chunkAConsumed, this.chunkBConsumed, currentEditScript.size(), currentMatches.size());
      LogService.debug("Chunk #%d ended: a-items: %d, b-items: %d, edit-script-length: %d, lcs-length: %d", chunks, this.chunkAConsumed, this.chunkBConsumed, currentEditScript.size(), currentMatches.size());
      // Update overall stats
      aLength += this.chunkAConsumed;
      bLength += this.chunkBConsumed;
      lcsLength += currentMatches.size();
      editScriptLength += currentEditScript.size();
      // Check whether one stream is already exhausted
      if (!this.streamA.hasNext() || !this.streamB.hasNext()) {
        break;
      }
    }
    // If we get here, at least one stream is exhausted
    if (this.streamA.hasNext()) {
      assert !this.streamB.hasNext();
      chunks += 1;
      int itemsRead = 0;
      this.processor.onChunkStarted(chunks);
      while (this.chunkAConsumed < this.chunkALength) { // First flush what was alraedy read
        T item = this.chunkA[this.chunkAConsumed];
        this.processor.onEdit(DiffUtils.createDeleteCommand(aLength + itemsRead, bLength, item));
        itemsRead += 1;
        this.chunkBConsumed += 1;
      }
      while (this.streamA.hasNext()) { // Then consume rest of stream
        T item = this.streamA.next();
        this.processor.onEdit(DiffUtils.createDeleteCommand(aLength + itemsRead, bLength, item));
        itemsRead += 1;
      }
      this.processor.onChunkEnded(chunks, itemsRead, 0, itemsRead, 0);
      aLength += itemsRead;
      editScriptLength += itemsRead;
    }
    if (this.streamB.hasNext()) {
      assert !this.streamA.hasNext();
      chunks += 1;
      int itemsRead = 0;
      this.processor.onChunkStarted(chunks);
      while (this.chunkBConsumed < this.chunkBLength) { // First flush what was alraedy read
        T item = this.chunkB[this.chunkBConsumed];
        this.processor.onEdit(DiffUtils.createInsertCommand(aLength, bLength + itemsRead, item));
        itemsRead += 1;
        this.chunkBConsumed += 1;
      }
      while (this.streamB.hasNext()) { // Then consume rest of stream
        T item = this.streamB.next();
        this.processor.onEdit(DiffUtils.createInsertCommand(aLength, bLength + itemsRead, item));
        itemsRead += 1;
      }
      this.processor.onChunkEnded(chunks, itemsRead, 0, itemsRead, 0);
      bLength += itemsRead;
      editScriptLength += itemsRead;
    }

    this.processor.onEnded(aLength, bLength, editScriptLength, lcsLength);
  }

  /**
   * Shifts the unconsumed items of each chunk to its start, then refills both chunks from the streams.
   *
   * @return <code>true</code> if at least one chunk holds items to process.
   */
  protected boolean readNextChunk() {
    // Copy over last parts of previous buffers
    for (int k = this.chunkAConsumed; k < this.chunkALength; k++) {
      this.chunkA[k - this.chunkAConsumed] = this.chunkA[k];
    }
    this.chunkALength = this.chunkALength - this.chunkAConsumed;
    this.chunkAConsumed = 0;
    for (int k = this.chunkBConsumed; k < this.chunkBLength; k++) {
      this.chunkB[k - this.chunkBConsumed] = this.chunkB[k];
    }
    this.chunkBLength = this.chunkBLength - this.chunkBConsumed;
    this.chunkBConsumed = 0;
    // Now fill maximally
    while (this.streamA.hasNext() && (this.chunkALength < this.chunkA.length)) {
      T item = this.streamA.next();
      this.chunkA[this.chunkALength] = item;
      this.chunkALength += 1;
    }
    while (this.streamB.hasNext() && (this.chunkBLength < this.chunkB.length)) {
      T item = this.streamB.next();
      this.chunkB[this.chunkBLength] = item;
      this.chunkBLength += 1;
    }
    return (this.chunkALength + this.chunkBLength > 0);
  }

  /**
   * Decides how much of the current chunks is consumed. By default everything; if more input remains and the last
   * match lies in the last 25% of both chunks, edit commands after that match are removed from the script
   * (in place) and only items up to and including the match are consumed.
   *
   * @param currentEditScript The edit script of the current chunk; may be modified.
   * @param currentMatches The matches of the current chunk.
   */
  protected void optionallyEndOnLastMatchPair(List<EditCommand> currentEditScript, List<MatchPair> currentMatches) {
    this.chunkAConsumed = this.chunkALength;
    this.chunkBConsumed = this.chunkBLength;
    if (!this.streamA.hasNext() && !this.streamB.hasNext()) {
      return ;
    }
    if (currentMatches.isEmpty()) {
      return ;
    }
    MatchPair lastMatch = currentMatches.get(currentMatches.size() - 1);
    int aAt75Pct = (int)(this.chunkALength * 0.75);
    int bAt75Pct = (int)(this.chunkBLength * 0.75);
    if ((lastMatch.indexA >= aAt75Pct) && (lastMatch.indexB >= bAt75Pct)) {
      // Remove later edit commands:
      int deleted = 0;
      for (int i = currentEditScript.size() - 1; i >= 0; i--) {
        EditCommand cmd = currentEditScript.get(i);
        if ((cmd.startInA > lastMatch.indexA) && (cmd.startInB > lastMatch.indexB)) {
          currentEditScript.remove(i);
          deleted += 1;
        }
      }
      // Set length and offset in a way that the nextNextChunk
      this.chunkAConsumed = lastMatch.indexA + 1;
      this.chunkBConsumed = lastMatch.indexB + 1;
      if (ENABLE_LOGGING) {
        this.log.debug("Re-aligning on match A[%d]=B[%d]", lastMatch.indexA, lastMatch.indexB);
      }
    }
  }
}
