
// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.diff;

import static com.pellicciotta.support.diff.DiffUtils.ENABLE_LOGGING;

/**
 *  Based on: An O(ND) Difference Algorithm and Its Variations, Eugene W. Myers
 *  Now implementing the 'Linear Space Refinement's
 */
public class MyersLinearSpaceDiff<T> extends DiffAlgorithmBase<T> {
  @Override
  public String name() {
    return "myers linear-space diff";
  }

  /**
   * Finds the middle snake and recursively processes the regions before and after it.
   *
   * @param aSeq The first sequence.
   * @param aSeqStart Inclusive start in the first sequence.
   * @param aSeqEnd Exclusive end in the first sequence.
   * @param bSeq The second sequence.
   * @param bSeqStart Inclusive start in the second sequence.
   * @param bSeqEnd Exclusive end in the second sequence.
   */
  protected void calculateNormalCase(T[] aSeq, int aSeqStart, int aSeqEnd, T[] bSeq, int bSeqStart, int bSeqEnd) {
    if (ENABLE_LOGGING) {
      this.log.debug("Normal case calculation for A [%d, %d] and B [%d, %d]", aSeqStart, aSeqEnd - 1, bSeqStart, bSeqEnd - 1);
    }

    MatchPair mp;
    EditCommand ed;
    String indent = String.format("[(%3d,%3d)-(%3d,%3d)]", aSeqStart, bSeqStart, aSeqEnd, bSeqEnd);

    Middle middleSnake = this.findMiddleSnake(aSeq, aSeqStart, aSeqEnd, bSeq, bSeqStart, bSeqEnd);
    if (ENABLE_LOGGING) {
      this.log.debug("%s - Middle snake found: %s", indent, middleSnake);
    }
    if (middleSnake.d > 1) {
      if (ENABLE_LOGGING) {
        this.log.debug("%s - Calculating front-region: [(%3d,%3d)-(%3d,%3d)]", indent, aSeqStart, bSeqStart, middleSnake.x + 1, middleSnake.y + 1);
      }
      this.calculateMatchesAndEdits(aSeq, aSeqStart, middleSnake.x + 1, bSeq, bSeqStart, middleSnake.y + 1);

      if (ENABLE_LOGGING) {
        this.log.debug("%s - Matches for middle snakes %s?", indent, middleSnake);
      }
      for (int i = middleSnake.x + 1, j = middleSnake.y + 1; i <= middleSnake.u; i++, j++) {
        mp = DiffUtils.createMatchPair(this.a, i, this.b, j);
        assert this.a[i].equals(this.b[j]);
        this.cachedLcs.add(mp);
        ed = DiffUtils.createKeepCommand(this.a, i, this.b, j);
        this.cachedEs.add(ed);
        if (ENABLE_LOGGING) {
          this.log.debug("Matching A[%d] = B[%d] = '%s'", i, j, aSeq[i]);
        }
      }

      if (ENABLE_LOGGING) {
        this.log.debug("%s - Calculating back-region: [(%3d,%3d)-(%3d,%3d)]", indent, middleSnake.u + 1, middleSnake.v + 1, aSeqEnd, bSeqEnd);
      }
      this.calculateMatchesAndEdits(aSeq, middleSnake.u + 1, aSeqEnd, bSeq, middleSnake.v + 1, bSeqEnd);
    }
    else { // Either d == 1 (1 edit: the last item of either A or B, whatever sequence is longer)
           //     Or d == 0 (0 edits, which means both A and B are equal length)
      if (ENABLE_LOGGING) {
        this.log.debug("%s - No further subdivide is possible", indent);
      }
      int i = aSeqStart, j = bSeqStart;
      if ((bSeqEnd - bSeqStart) > (aSeqEnd - aSeqStart)) { // B is 1 larger
        assert (bSeqEnd - bSeqStart) == (aSeqEnd - aSeqStart) + 1;
        if (ENABLE_LOGGING) {
          this.log.debug("Inserting B[%d] = '%s'", j, bSeq[j]);
        }
        ed = DiffUtils.createInsertCommand(i, this.b, j);
        this.cachedEs.add(ed);
        j += 1;
      }
      else if ((aSeqEnd - aSeqStart) > (bSeqEnd - bSeqStart)) { // A is 1 larger
        assert (bSeqEnd - bSeqStart) + 1 == (aSeqEnd - aSeqStart);
        if (ENABLE_LOGGING) {
          this.log.debug("Deleting A[%d] = '%s'", i, aSeq[i]);
        }
        ed = DiffUtils.createDeleteCommand(this.a, i, j);
        this.cachedEs.add(ed);
        i += 1;
      }
      // Equal size assumed
      assert (bSeqEnd - j) == (aSeqEnd - i);
      for (; (i < aSeqEnd) && (j < bSeqEnd); i++, j++) {
        mp = DiffUtils.createMatchPair(this.a, i, this.b, j);
        assert aSeq[i].equals(bSeq[j]);
        this.cachedLcs.add(mp);
        ed = DiffUtils.createKeepCommand(this.a, i, this.b, j);
        this.cachedEs.add(ed);
        if (ENABLE_LOGGING) {
          this.log.debug("Matching A[%d] = B[%d] = '%s'", i, j, aSeq[i]);
        }
      }
    }
  }

  private Middle findMiddleSnake(T[] aSeq, int aSeqStart, int aSeqEnd, T[] bSeq, int bSeqStart, int bSeqEnd) {
    final int N = aSeqEnd - aSeqStart; // Exclusive end points
    final int M = bSeqEnd - bSeqStart;

    if (N + M == 0) {
      return null;
    }

    final int MAX = M + N;         // Max. number of differences = max. size of edit script
    final int DELTA = N - M;       // The difference between forward and backward diagonals
    final int ABS_DELTA = Math.abs(DELTA);
    final int HALF_MAX = (int)Math.ceil(MAX / 2.0); // Upper bound of half
    final int OFFSET = HALF_MAX + ABS_DELTA;
    final boolean CHECK_OVERLAP_AFTER_FORWARD_MOVE = ((ABS_DELTA % 2) != 0); // If DELTA is odd: check after forward move, else check after backward move

    // Storing the furthest reaching end-points
    int[] fV = new int[2*(HALF_MAX + ABS_DELTA) + 1]; // Forward search end-points.  From -HALF_MAX to +HALF_MAX: the 0-based x-coordinates in sequence a for diagonal k and current edit distance
    int[] bV = new int[2*(HALF_MAX + ABS_DELTA) + 1]; // Backward search end-points. From -HALF_MAX to +HALF_MAX: the 0-based x-coordinates in sequence a for diagonal k and current edit distance

    fV[OFFSET + 1] = aSeqStart - 1;    // Start point for forward algorithm (since on first iteration - when D = 0, k = 0 - x will be set to this)
    bV[OFFSET + DELTA + 1] = aSeqEnd;  // Start point for backward algorithm (since on first iteration - when D = 0, l = 0 - x-1 will be set to this - 1)

    for (int d = 0; d <= HALF_MAX; d++) { // For increasing edit distances (max. N+M for N deletes, followed by M inserts if there are zero match points)
      int prevX, x, prevY, y, midX, midY;
      // A. Searching forwards: from top-left to bottom-right / trying to maximize x
      for (int k = -d; k <= d; k += 2) { // For all top-down diagonals k with the furthest reaching end-points, for the current edit distance D
        int l = k - DELTA;
        // Moving right from k-1 or down from k+1? Try to maximize x
        boolean moveDown = (k == -d) || ((k != d) && (fV[OFFSET + k - 1] < fV[OFFSET + k + 1]));
        if (moveDown) { // Also very first move, so using artificial start point
          prevX = fV[OFFSET + k + 1];
          prevY = bSeqStart + (prevX - aSeqStart) - (k + 1);
          x = prevX;
          y = prevY + 1;
        }
        else {
          prevX = fV[OFFSET + k - 1];
          prevY = bSeqStart + (prevX - aSeqStart) - (k - 1);
          x = prevX + 1;
          y = prevY;
        }
        // Following diagonals on match points:
        midX = x;
        midY = y;
        while ((x < aSeqEnd - 1) && (y < bSeqEnd - 1) && (aSeq[x + 1].equals(bSeq[y + 1]))) {
          x += 1;
          y += 1;
        }
        if (ENABLE_LOGGING) {
          String op = moveDown ? (d == 0 ? "initial move" : "moving down/inserting from B") : "moving right/deleting from A";
          for (int m = midX + 1; m <= x; m++) {
            op += ", then matching '" + aSeq[m] + "' ";
          }
          this.log.debug("  Forwards: D=%3d, l=%3d, k=%3d, (x:%3d, y:%3d)->(x:%3d, y:%3d), %s", d, l, k, prevX, prevY, x, y, op);
        }
        // Store:
        fV[OFFSET + k] = x;
        // Check end condition:
        if (   CHECK_OVERLAP_AFTER_FORWARD_MOVE                     // Check for overlap on forward search (i.e. when DELTA is odd)
            && (k >= (DELTA - (d - 1))) && (k <= (DELTA + (d - 1))) // Can only overlap if there are overlapping diagonals, i.e. k in range [-(d-1) + DELTA, +(d-1) + DELTA], and backward search runs one d behind
            && (x >= bV[OFFSET + k])) {                             // There is overlap if furthest reaching x is further (or equal) to the furthest reaching x in backward direction
          Middle middle = new Middle(midX, midY, x, y, 2*d - 1); // Can either be a single point or one or more diagonals
          return middle;
        }
      }
      // B. Searching backwards: from bottom-right to top-left / trying to minimize y
      for (int l = -d; l <= d; l += 2) { // For all bottom-right to top-left diagonals l with the furthest reaching end-points, for the current edit distance D
        int k = l + DELTA;               // Translate to top-left to bottom-right diagonal
        // Moving up from k-1 or left from k+1? Try to minimize x
        boolean moveLeft = (l == -d) || ((l != d) && (bV[OFFSET + k - 1] >= bV[OFFSET + k + 1]));
        if (moveLeft) { // Also very first move, so using artificial start point
          prevX = bV[OFFSET + k + 1];
          prevY = bSeqStart + (prevX - aSeqStart) - (k + 1); // Because k is relative to the region, while x and y are global
          x = prevX - 1;                              // -1 is moving left
          y = prevY;
        }
        else {
          prevX = bV[OFFSET + k - 1];
          prevY = bSeqStart + (prevX - aSeqStart) - (k - 1); // (k-1) because we're calculating y based on the x-value of diagonal k-1
          x = prevX;
          y = prevY - 1;                               // -1 is moving up
        }
        // Following diagonals on match points:
        midX = x;
        midY = y;
        while ((x >= aSeqStart) && (y >= bSeqStart) && (aSeq[x].equals(bSeq[y]))) {
          x -= 1;
          y -= 1;
        }
        if (ENABLE_LOGGING) {
          String op = moveLeft ? (d == 0 ? "initial move" : "moving left/deleting from A") : "moving up/inserting from B";
          for (int m = midX; m > x; m--) {
            op += ", then matching '" + aSeq[m] + "' ";
          }
          this.log.debug("  Bckwards: D=%3d, l=%3d, k=%3d, (x:%3d, y:%3d)->(x:%3d, y:%3d), %s", d, l, k, prevX, prevY, x, y, op);
        }
        // Store:
        bV[OFFSET + k] = x;
        // Check end condition:
        if (!CHECK_OVERLAP_AFTER_FORWARD_MOVE   // Only check on backward search for even DELTAs
            && (k >= -d) && (k <= d)            // Can only overlap if k (= forward diagonal) is in range [-d,d]
            && (x <= fV[OFFSET + k])) {         // There is overlap is the furthest reaching x (in left direction) is further (i.e. smaller) or equal to the furthest reaching x in forward direction
          Middle middle = new Middle(x, y, midX, midY, 2 * d); // Can either be a single point or one or more diagonals
          return middle;
        }
      }
    }
    throw new IllegalStateException("Reaching end of forward/backward scan is not supposed to happen");
  }

  private static class Middle {
    public Middle(int x, int y, int u, int v, int d) {
      this.x = x;
      this.y = y;
      this.u = u;
      this.v = v;
      this.d = d;
    }

    private final int x;
    private final int y;
    private final int u;
    private final int v;
    private final int d;

    @Override
    public String toString() {
      return String.format("M[D=%d,(x,y)=(%2d,%2d),(u,v)=(%2d,%2d)] ", this.d, this.x, this.y, this.u, this.v);
    }
  }
}
