// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.diff;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.pellicciotta.support.logging.Log;
import com.pellicciotta.support.logging.LogService;
import com.pellicciotta.support.text.TextBuilder;
import com.pellicciotta.support.utils.CollectionUtils;
import static com.pellicciotta.support.diff.DiffUtils.ENABLE_LOGGING;

/**
 *  Base class for implementing variations of a diff algorithm for calculating LCS and SES.
 *
 *  Note that the algorithm has been adapted to work with zero-based indexes.
 */
public abstract class DiffAlgorithmBase<T> extends DiffAlgorithm<T> {
  /** Whether {@link #cachedLcs} and {@link #cachedEs} are valid for the current sequences. */
  protected boolean calculationsDone;
  /** The longest common subsequence from the last calculation. */
  protected final List<MatchPair> cachedLcs = new ArrayList<>();
  /** The edit script, including KEEP commands, from the last calculation. */
  protected final List<EditCommand> cachedEs = new ArrayList<>();

  /** Logger used for debug output of the calculations. */
  protected final Log log = LogService.fromCurrentClass();

  /**
   * Sets the first sequence like the super-class and invalidates any cached results.
   *
   * @param seq The sequence to be compared.
   * @param startIndex The index of the first element to compare. An inclusive lower bound.
   * @param endIndex The index just beyond the last element to compare. An exclusive upper bound.
   */
  public void setSequenceA(T[] seq, int startIndex, int endIndex) {
    super.setSequenceA(seq, startIndex, endIndex);
    this.calculationsDone = false;
  }

  /**
   * Sets the second sequence like the super-class and invalidates any cached results.
   *
   * @param seq The sequence to be compared.
   * @param startIndex The index of the first element to compare. An inclusive lower bound.
   * @param endIndex The index just beyond the last element to compare. An exclusive upper bound.
   */
  public void setSequenceB(T[] seq, int startIndex, int endIndex) {
    super.setSequenceB(seq, startIndex, endIndex);
    this.calculationsDone = false;
  }

  /**
   *  Determine the LCS of two sequences a and b.
   *
   *  @return An ordered array of {@link MatchPair}s. Can be empty but never <code>null</code>.
   */
  public List<MatchPair> calculateLongestCommonSubsequence() {
    if (!this.calculationsDone) {
      this.calculate();
    }
    List<MatchPair> lcs = new ArrayList<>(this.cachedLcs);
    return lcs;
  }

  /**
   *  Determine the size of the SES: the shortest edit script.
   *  <p>
   *  This implementation will simply implement the greedy algorithm.</p>
   *
   *  @return The size of the edit script. Will maximally be the sum of the lengths of the two sequences.
   */
  public int calculateEditDistance() {
    final int N = this.aEnd - this.aStart;
    final int M = this.bEnd - this.bStart;
    final int MAX = M + N;
    final int OFFSET = MAX;
    int[] v = new int[2*MAX + 1]; // From -MAX to +MAX: the 0-based x-coordinates in sequence a for diagonal k and current edit distance
    v[OFFSET + 1] = this.aStart - 1; // Artificial start point for algorithm
    for (int d = 0; d <= MAX; d++) { // For increasing edit distances (max. N+M for N deletes, followed by M inserts if there are zero match points)
      for (int k = -d; k <= d; k += 2) { // For all diagonals k with the furthest reaching end-points, for the current edit distance D
        int x, y;
        // Moving right from k-1 or down from k+1? Try to maximize x
        boolean moveDown =    (k == -d)                                                // At lower-end: can only reach k == -d by always moving down (since that decreases k)
            || ((k != d) && (v[OFFSET + k - 1] < v[OFFSET + k + 1]));   // k+1's x value is larger so re-using that will keep up with maximal x and can only use k+1 by moving down
        if (moveDown) { // Also very first move, so using artificial start point
          x = v[OFFSET + k + 1];
        }
        else {
          x = v[OFFSET + k - 1] + 1;
        }
        y = this.bStart + (x - this.aStart) - k;
        // Following diagonals on match points:
        while ((x < this.aEnd - 1) && (y < this.bEnd - 1) && (this.a[x + 1].equals(this.b[y + 1]))) {
          x += 1;
          y += 1;
        }
        // Store:
        v[OFFSET + k] = x;
        // Check end condition:
        if ((x >= this.aEnd - 1) && (y >= this.bEnd - 1)) {
          return d;
        }
      }
    }
    throw new IllegalStateException("We should never reach this point");
  }

  /**
   *  Determine the shortest edit script.
   *
   *  @param includeKeepCommands Whether to also include {@link EditType#KEEP} commands denoting matching elements.
   *  @return An ordered list of {@link EditCommand}s to transform the first sequence into the second. All {@link EditCommand}s
   *          will refer to the indexes of elements in both sequences as they are positioned at the beginning (before any changes).
   */
  public List<EditCommand> calculateEditScript(boolean includeKeepCommands) {
    if (!this.calculationsDone) {
      this.calculate();
    }
    if (!includeKeepCommands) {
      return this.cachedEs.stream().filter(ed -> ed.type != EditType.KEEP).collect(Collectors.toList());
    }
    return new ArrayList<>(this.cachedEs);
  }

  /**
   * Calculates the LCS and the edit script (with KEEP commands) for the current sequences into the caches.
   * Handles empty sequences directly and otherwise delegates to {@link #calculateMatchesAndEdits}.
   */
  protected void calculate() {
    if (ENABLE_LOGGING) {
      this.log.info("Calculating LCS and SES for A[%d, %d] and B[%d, %d] with %s", this.aStart, this.aEnd - 1, this.bStart, this.bEnd - 1, this.name());
      if (Math.max(this.aEnd - this.aStart, this.bEnd - this.bStart) < 30) {
        this.log.debug("  A=%s", CollectionUtils.toString(this.a, this.aStart, this.aEnd));
        this.log.debug("  B=%s", CollectionUtils.toString(this.b, this.bStart, this.bEnd));
      }
    }
    // Clear caches:
    this.calculationsDone = false;
    this.cachedLcs.clear();
    this.cachedEs.clear();
    // Check special 'can't match' condition:
    if (this.cannotHaveAnyMatches()) {
      this.createNoMatchEditScript(this.a, this.aStart, this.aEnd, this.b, this.bStart, this.bEnd);
    }
    else {
      this.calculateMatchesAndEdits(this.a, this.aStart, this.aEnd, this.b, this.bStart, this.bEnd);
    }
    if (ENABLE_LOGGING) {
      this.logEditScript(this.cachedEs.stream()
                                      .filter(ed -> ed.type != EditType.KEEP)
                                      .collect(Collectors.toList()));
      this.logLcs(this.cachedLcs);
    }
    this.calculationsDone = true;
  }

  /**
   * Appends matches and edits for the given sub-ranges to the caches.
   * Matching elements at the start and end are matched directly; the remaining middle is delegated to
   * {@link #calculateNormalCase} unless one side is empty.
   *
   * @param aSeq The first sequence.
   * @param aSeqStart Inclusive start in the first sequence.
   * @param aSeqEnd Exclusive end in the first sequence.
   * @param bSeq The second sequence.
   * @param bSeqStart Inclusive start in the second sequence.
   * @param bSeqEnd Exclusive end in the second sequence.
   */
  protected void calculateMatchesAndEdits(T[] aSeq, int aSeqStart, int aSeqEnd, T[] bSeq, int bSeqStart, int bSeqEnd) {
    // Pre-match matching elements at start and end to shorten sequences to be compared:
    List<MatchPair> frontMatches = new ArrayList<>();
    for (; (aSeqStart < aSeqEnd) && (bSeqStart < bSeqEnd); aSeqStart++, bSeqStart++) {
      if (!aSeq[aSeqStart].equals(bSeq[bSeqStart])) {
        break;
      }
      frontMatches.add(this.createMatchPair(aSeqStart, bSeqStart));
    }
    if (ENABLE_LOGGING) {
      if(!frontMatches.isEmpty()) {
        this.log.debug("%d front-matches found, moving start of A to %d, and start of B to %d", frontMatches.size(), aSeqStart, bSeqStart);
      }
    }
    List<MatchPair> endMatches = new ArrayList<>();
    for (; (aSeqEnd > aSeqStart) && (bSeqEnd > bSeqStart); aSeqEnd--, bSeqEnd--) {
      if (!aSeq[aSeqEnd - 1].equals(bSeq[bSeqEnd - 1])) {
        break;
      }
      endMatches.add(0, this.createMatchPair(aSeqEnd - 1, bSeqEnd - 1));
    }
    if (ENABLE_LOGGING) {
      if (!endMatches.isEmpty()) {
        this.log.debug("%d end-matches found, moving end of A to %d, and end of B to %d", endMatches.size(), aSeqEnd, bSeqEnd);
      }
    }
    // Handle edge cases
    if (aSeqStart == aSeqEnd) { // A sequence is empty, so either B sequence is also empty or if it still has elements: they need to be inserted
      if (ENABLE_LOGGING) {
        if (bSeqEnd == bSeqStart) {
          this.log.debug("Edge case found: A and B are identical");
        }
        else {
          this.log.debug("Edge case found: A is fully matched: deleting (rest of) B: [%d,%d]", bSeqStart, bSeqEnd - 1);
        }
      }
      int lastAIndex = aSeqStart;
      for (MatchPair mp : frontMatches) {
        this.cachedLcs.add(mp);
        this.cachedEs.add(DiffUtils.createKeepCommand(this.a, mp.indexA, this.b, mp.indexB));
        if (ENABLE_LOGGING) {
          this.log.debug("Matching %s", mp);
        }
        lastAIndex = mp.indexA;
      }
      for ( ; bSeqStart < bSeqEnd; bSeqStart++) {
        this.cachedEs.add(DiffUtils.createInsertCommand(lastAIndex, this.b, bSeqStart));
        if (ENABLE_LOGGING) {
          this.log.debug("Inserting B[%d] = '%s'", bSeqStart, bSeq[bSeqStart]);
        }
      }
      for (MatchPair mp : endMatches) {
        this.cachedLcs.add(mp);
        this.cachedEs.add(DiffUtils.createKeepCommand(this.a, mp.indexA, this.b, mp.indexB));
        if (ENABLE_LOGGING) {
          this.log.debug("Matching %s", mp);
        }
      }
    }
    else if (bSeqStart == bSeqEnd) { // B sequence is empty but A is no, so all remaining A elements must be deleted
      if (ENABLE_LOGGING) {
        this.log.debug("Edge case found: B is fully matched: inserting (rest of) A: [%d,%d]", aSeqStart, aSeqEnd - 1);
      }
      int lastBIndex = bSeqStart;
      for (MatchPair mp : frontMatches) {
        this.cachedLcs.add(mp);
        this.cachedEs.add(DiffUtils.createKeepCommand(this.a, mp.indexA, this.b, mp.indexB));
        if (ENABLE_LOGGING) {
          this.log.debug("Matching %s", mp);
        }
        lastBIndex = mp.indexB;
      }
      for ( ; aSeqStart < aSeqEnd; aSeqStart++) {
        this.cachedEs.add(DiffUtils.createDeleteCommand(this.a, aSeqStart, bSeqEnd));
        if (ENABLE_LOGGING) {
          this.log.debug("Deleting A[%d] = '%s'", aSeqStart, aSeq[aSeqStart]);
        }
      }
      for (MatchPair mp : endMatches) {
        this.cachedLcs.add(mp);
        this.cachedEs.add(DiffUtils.createKeepCommand(this.a, mp.indexA, this.b, mp.indexB));
        if (ENABLE_LOGGING) {
          this.log.debug("Matching %s", mp);
        }
      }
    }
    else { // More general case: both A and B still have elements
      for (MatchPair mp : frontMatches) {
        this.cachedLcs.add(mp);
        this.cachedEs.add(DiffUtils.createKeepCommand(this.a, mp.indexA, this.b, mp.indexB));
        if (ENABLE_LOGGING) {
          this.log.debug("Matching %s", mp);
        }
      }
      this.calculateNormalCase(aSeq, aSeqStart, aSeqEnd, bSeq, bSeqStart, bSeqEnd);
      for (MatchPair mp : endMatches) {
        this.cachedLcs.add(mp);
        this.cachedEs.add(DiffUtils.createKeepCommand(this.a, mp.indexA, this.b, mp.indexB));
        if (ENABLE_LOGGING) {
          this.log.debug("Matching %s", mp);
        }
      }
    }
  }

  /**
   * Algorithm-specific step: appends matches and edits for ranges that both still have elements
   * and share no common prefix or suffix.
   *
   * @param aSeq The first sequence.
   * @param aSeqStart Inclusive start in the first sequence.
   * @param aSeqEnd Exclusive end in the first sequence.
   * @param bSeq The second sequence.
   * @param bSeqStart Inclusive start in the second sequence.
   * @param bSeqEnd Exclusive end in the second sequence.
   */
  protected abstract void calculateNormalCase(T[] aSeq, int aSeqStart, int aSeqEnd, T[] bSeq, int bSeqStart, int bSeqEnd);

  /**
   * Appends an edit script that deletes all given elements of the first sequence, then inserts all given
   * elements of the second sequence.
   *
   * @param aSeq The first sequence.
   * @param aSeqStart Inclusive start in the first sequence.
   * @param aSeqEnd Exclusive end in the first sequence.
   * @param bSeq The second sequence.
   * @param bSeqStart Inclusive start in the second sequence.
   * @param bSeqEnd Exclusive end in the second sequence.
   */
  protected void createNoMatchEditScript(T[] aSeq, int aSeqStart, int aSeqEnd, T[] bSeq, int bSeqStart, int bSeqEnd) {
    // First delete all from first sequence
    for (int i = aSeqStart; i < aSeqEnd; i++) {
      this.addDelete(i, bSeqStart);
    }
    // Then insert all from second sequence
    for (int j = bSeqStart; j < bSeqEnd; j++) {
      this.addInsert(aSeqEnd, j);
    }
  }

  /**
   * Appends a delete command for one element of the first sequence.
   *
   * @param aIndex Index of the element to delete in the first sequence.
   * @param bIndex Current position in the second sequence.
   */
  protected void addDelete(int aIndex, int bIndex) {
    this.cachedEs.add(DiffUtils.createDeleteCommand(this.a, aIndex, bIndex));
    if (ENABLE_LOGGING) {
      this.log.debug("Deleting A[%d] = '%s'", aIndex, this.a[aIndex]);
    }
  }

  /**
   * Appends an insert command for one element of the second sequence.
   *
   * @param aIndex Index in the first sequence after which to insert.
   * @param bIndex Index of the element to insert in the second sequence.
   */
  protected void addInsert(int aIndex, int bIndex) {
    this.cachedEs.add(DiffUtils.createInsertCommand(aIndex, this.b, bIndex));
    if (ENABLE_LOGGING) {
      this.log.debug("Inserting B[%d] = '%s'", bIndex, this.b[bIndex]);
    }
  }

  /**
   * Creates a match-pair for the current sequences without adding it to the caches.
   *
   * @param aIndex Index in the first sequence.
   * @param bIndex Index in the second sequence.
   * @return The new match-pair.
   */
  protected MatchPair createMatchPair(int aIndex, int bIndex) {
    return DiffUtils.createMatchPair(this.a, aIndex, this.b, bIndex);
  }

  /**
   * Records a match by appending a KEEP command to the edit script and a match-pair to the LCS.
   *
   * @param aIndex Index in the first sequence.
   * @param bIndex Index in the second sequence.
   */
  protected void addMatch(int aIndex, int bIndex) {
    this.cachedEs.add(DiffUtils.createKeepCommand(this.a, aIndex, this.b, bIndex));
    MatchPair mp = DiffUtils.createMatchPair(this.a, aIndex, this.b, bIndex);
    if (ENABLE_LOGGING) {
      this.log.debug("Matching %s", mp);
    }
    this.cachedLcs.add(mp);
  }

  /**
   * Writes the edit script to the debug log.
   *
   * @param script The edit commands to log.
   */
  protected void logEditScript(List<EditCommand> script) {
    TextBuilder tb = new TextBuilder();
    tb.append("SES with length %d:", script.size());
    for (EditCommand ed : script) {
      tb.appendLine();
      tb.append("  %s", ed);
    }
    this.log.debug(tb.toString());
  }

  /**
   * Writes the longest common subsequence to the debug log.
   *
   * @param matches The match-pairs to log.
   */
  protected void logLcs(List<MatchPair> matches) {
    TextBuilder tb = new TextBuilder();
    tb.append("LCS with length %d:", matches.size());
    for (MatchPair mp : matches) {
      tb.appendLine();
      tb.append("  %s", mp);
    }
    this.log.debug(tb.toString());
  }

  /**
   * Writes the Myers edit graph, one V array per edit distance, to the debug log.
   *
   * @param editGraph The V arrays, indexed by edit distance.
   * @param N The length of the first sequence range.
   * @param M The length of the second sequence range.
   */
  protected void logEditGraph(List<int[]> editGraph, int N, int M) {
    final int MAX = M + N;
    final int DELTA = N - M;
    TextBuilder tb = new TextBuilder();
    tb.append("Edit graph for %d edit distance (delta=%d):", editGraph.size(), DELTA);
    for (int d = 0; d < editGraph.size(); d++) {
      int[] v = editGraph.get(d);
      if (d == 0) {
        tb.appendLine();
        this.appendEditGraphHeader(tb, v);
      }
      tb.appendLine();
      this.appendEditGraphDist(tb, v, d, DELTA);
    }
    this.log.debug(tb.toString());
  }

  private void appendEditGraphHeader(TextBuilder tb, int v[]) {
    int MAX = (v.length - 1) / 2;
    tb.append("  V[D=  ?]: |");
    for (int i = 0; i < v.length; i++) {
      int vK = i - MAX;
      tb.append(" k = %3d  |", vK);
    }
  }

  private void appendEditGraphDist(TextBuilder tb, int[] v, int d, int DELTA) {
    int MAX = (v.length - 1) / 2;
    tb.append("  V[D=%3d]: |", d);
    for (int i = 0; i < v.length; i++) {
      int vK = i - MAX;
      int vL = vK - DELTA;
      int vX = v[i];
      int vY = this.bStart + (vX - this.aStart) - vK;
      if ((vL < -d) || (vL > d) || ((Math.abs(d)%2) != (Math.abs(vL)%2))) {
        tb.append("          |");
      }
      else {
        tb.append("(%3d, %3d)|", vX, vY);
      }
    }
  }
}
