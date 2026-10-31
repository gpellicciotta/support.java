// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.diff;

import java.util.ArrayList;
import java.util.List;

import com.pellicciotta.support.utils.CollectionUtils;

/**
 *  Based on: An O(ND) Difference Algorithm and Its Variations, Eugene W. Myers.
 *  Working in reverse, so starting at the end of both sequences instead of at the start.
 *
 *  Note that the algorithm has been adapted to work with zero-based indexes.
 */
public class MyersReverseGreedyDiff<T> extends DiffAlgorithmBase<T>
{
  @Override
  public String name() {
    return "myers reverse greedy diff";
  }

  @Override
  protected void calculateNormalCase(T[] a, int aStart, int aEnd, T[] b, int bStart, int bEnd) {
    this.log.debug("Normal case calculation for A [%d, %d] and B [%d, %d]", aStart, aEnd - 1, bStart, bEnd - 1);
    List<int[]> editGraph = this.calculateEditGraph(a, aStart, aEnd, b, bStart, bEnd);
    this.logEditGraph(editGraph, aEnd - aStart, bEnd - bStart);
    this.calculateMatchesAndEdits(editGraph, a, aStart, aEnd, b, bStart, bEnd);
  }

  private List<int[]> calculateEditGraph(T[] a, int aStart, int aEnd, T[] b, int bStart, int bEnd) {
    this.log.debug("Calculating (backward) edit graph for A[%d, %d] and B[%d, %d]", aStart, aEnd-1, bStart, bEnd-1);

    final int N = aEnd - aStart;
    final int M = bEnd - bStart;
    final int MAX = M + N;
    final int DELTA = N - M;
    final int ABS_DELTA = Math.abs(DELTA);
    final int OFFSET = MAX + ABS_DELTA;

    // The end-points of the furthest reachest D-paths.
    // Index is diagonal k, value is index into first sequence (=x). Index into second sequence (=y) can be derived: y = x - k.
    // Furthest reaching D-paths are always on diagonals -D, -D + 2, ... D - 2, D
    // And furthest reaching (D+1)-paths are disjoint with those for D-paths, so the v-array can store both the current (=D) paths and the next (=D+1) paths
    List<int[]> vTrace = new ArrayList<>();

    int[] v = new int[2*(MAX + ABS_DELTA) + 1]; // From -MAX to +MAX: the 0-based x-coordinates in sequence a for diagonal k and current edit distance
    v[OFFSET + DELTA + 1] = aEnd; // Artificial start point for algorithm
    for_increasing_edit_distance:
    for (int d = 0; d <= MAX; d++) { // For increasing edit distances (max. N+M for N deletes, followed by M inserts if there are zero match points)
      try {
        for (int l = -d; l <= d; l += 2) { // For all bottom-right corner centered diagonals l with the furthest reaching end-points, for the current edit distance D
          int k = l + DELTA;               // Translate l to top-left corner centered diagonal k (as used by the forward search)
          int prevX, prevY, x, y;
          // Moving up from k-1 or left from k+1? Try to minimize x
          boolean moveLeft =    (l == -d)                                                // At lower-end: can only reach l == -d by always moving left (since that decreases l)
                            ||  ((l != d) && (v[OFFSET + k - 1] >= v[OFFSET + k + 1]));  // k+1's x value is smaller so re-using that will keep up with minimal x and can only use k+1 by moving left
          //boolean moveUp    =   (l == d)                                               // At high-end: can only reach l == d by always moving up (since that increases l)
          //                   || (v[OFFSET + k - 1] <= v[OFFSET + k + 1]);              // k-1's x value is smaller or at least equal and moving up will certainly keep that smaller x
          if (moveLeft) { // Also very first move, so using artificial start point
            prevX = v[OFFSET + k + 1];
            prevY = bStart + (prevX - aStart) - (k + 1); // Because k is relative to the region, while x and y are global
            x = prevX - 1;                              // -1 is moving left
            y = prevY;
          }
          else {
            prevX = v[OFFSET + k - 1];
            prevY = bStart + (prevX - aStart) - (k - 1); // (k-1) because we're calculating y based on the x-value of diagonal k-1
            x = prevX;
            y = prevY - 1;                               // -1 is moving up
          }
          // Following diagonals on match points:
          String op = moveLeft ? (d == 0 ? "initial move" : "moving left/deleting from a") : "moving up/inserting from b";
          while ((x >= aStart) && (y >= bStart) && (a[x].equals(b[y]))) {
            op += ", then matching '" + a[x] + "' ";
            x -= 1;
            y -= 1;
          }
          // Store:
          v[OFFSET + k] = x; // Keep storing based on k-value (not l-value)
          // Check end condition:
          if ((x < aStart) && (y < bStart)) {
            op += ", end reached !";
            this.log.debug("  D=%3d, l=%3d, k=%3d, (x:%3d, y:%3d)->(x:%3d, y:%3d), %s", d, l, k, prevX, prevY, x, y, op);
            break for_increasing_edit_distance;
          }
          this.log.debug("  D=%3d, l=%3d, k=%3d, (x:%3d, y:%3d)->(x:%3d, y:%3d), %s", d, l, k, prevX, prevY, x, y, op);
        }
      }
      finally {
        int[] vCopy = CollectionUtils.asIntArray(v);
        vTrace.add(vCopy);
      }
    }
    return vTrace;
  }

  private void calculateMatchesAndEdits(List<int[]> vEditGraph, T[] a, int aStart, int aEnd, T[] b, int bStart, int bEnd) {
    this.log.debug("Calculating SES and LCS for A[%d, %d] and B[%d, %d] with edit distance %d", this.aStart, this.aEnd-1, this.bStart, this.bEnd-1, vEditGraph.size());
    MatchPair mp;
    EditCommand ed;
    final int N = aEnd - aStart;
    final int M = bEnd - bStart;
    final int MAX = M + N;
    final int DELTA = N - M;
    final int ABS_DELTA = Math.abs(DELTA);
    final int OFFSET = MAX + ABS_DELTA;

    int x = aStart - 1;
    int y = bStart - 1;
    for (int d = vEditGraph.size() - 1; d >= 0; d--) {
      int[] v = vEditGraph.get(d);
      // Determine where we would have come from, according to similar logic in calculateEditGraph:
      int k = (x - aStart) - (y - bStart);
      int l = k - DELTA;
      int prevK;
      if ((l == -d) || ((l != d) && (v[OFFSET + k - 1] >= v[OFFSET + k + 1]))) {
        prevK = k + 1;
      }
      else {
        prevK = k - 1;
      }
      // Check prev x value (also present in current v-array!)
      int prevX = v[OFFSET + prevK];
      int prevY = bStart + (prevX - aStart) - prevK;
      // Follow snakes backward?
      while ((x < prevX) && (y < prevY)) {
        mp = DiffUtils.createMatchPair(this.a, x + 1, this.b, y + 1);
        assert a[x + 1].equals(b[y + 1]);
        this.cachedLcs.add(mp);
        ed = DiffUtils.createKeepCommand(this.a, x + 1, this.b, y + 1);
        this.cachedEs.add(ed);
        this.log.debug("  Adding to SES and LCS: %s", ed);
        x += 1;
        y += 1;
      }
      // Now determine edit move
      if (d > 0) {
        if (x == prevX) {
          ed = DiffUtils.createInsertCommand(prevX, this.b, prevY);
          this.cachedEs.add(ed);
          this.log.debug("  Adding to SES: %s", ed);
        }
        else if (y == prevY) {
          ed = DiffUtils.createDeleteCommand(this.a, prevX, prevY);
          this.cachedEs.add(ed);
          this.log.debug("  Adding to SES: %s", ed);
        }
      }
      x = prevX;
      y = prevY;
    }
  }
}
