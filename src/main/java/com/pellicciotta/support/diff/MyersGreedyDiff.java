// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.diff;

import java.util.ArrayList;
import java.util.List;

import com.pellicciotta.support.utils.CollectionUtils;

/**
 *  Based on: An O(ND) Difference Algorithm and Its Variations, Eugene W. Myers
 *
 *  Note that the algorithm has been adapted to work with zero-based indexes.
 */
public class MyersGreedyDiff<T> extends DiffAlgorithmBase<T>
{
  @Override
  public String name() {
    return "myers greedy diff";
  }

  @Override
  protected void calculateNormalCase(T[] a, int aStart, int aEnd, T[] b, int bStart, int bEnd) {
    this.log.debug("Normal case calculation for A [%d, %d] and B [%d, %d]", aStart, aEnd - 1, bStart, bEnd - 1);
    List<int[]> editGraph = this.calculateEditGraph(a, aStart, aEnd, b, bStart, bEnd);
    this.logEditGraph(editGraph, aEnd - aStart, bEnd - bStart);
    this.calculateMatchesAndEdits(editGraph, a, aStart, aEnd, b, bStart, bEnd);
  }

  private List<int[]> calculateEditGraph(T[] a, int aStart, int aEnd, T[] b, int bStart, int bEnd) {
    this.log.debug("Calculating (forward) edit graph for A[%d, %d] and B[%d, %d]", aStart, aEnd-1, bStart, bEnd-1);

    final int N = aEnd - aStart;
    final int M = bEnd - bStart;
    final int MAX = M + N;
    final int OFFSET = MAX;

    // The end-points of the furthest reachest D-paths.
    // Index is diagonal k, value is index into first sequence (=x). Index into second sequence (=y) can be derived: y = x - k.
    // Furthest reaching D-paths are always on diagonals -D, -D + 2, ... D - 2, D
    // And furthest reaching (D+1)-paths are disjoint with those for D-paths, so the v-array can store both the current (=D) paths and the next (=D+1) paths
    List<int[]> vTrace = new ArrayList<>();

    int[] v = new int[2*MAX + 1]; // From -MAX to +MAX: the 0-based x-coordinates in sequence a for diagonal k and current edit distance
    v[OFFSET + 1] = aStart - 1; // Artificial start point for algorithm
  for_increasing_edit_distance:
    for (int d = 0; d <= MAX; d++) { // For increasing edit distances (max. N+M for N deletes, followed by M inserts if there are zero match points)
      try {
        for (int k = -d; k <= d; k += 2) { // For all diagonals k with the furthest reaching end-points, for the current edit distance D
          int prevX, prevY, x, y;
          // Moving right from k-1 or down from k+1? Try to maximize x
          boolean moveDown =    (k == -d)                                                // At lower-end: can only reach k == -d by always moving down (since that decreases k)
                             || ((k != d) && (v[OFFSET + k - 1] < v[OFFSET + k + 1]));   // k+1's x value is larger so re-using that will keep up with maximal x and can only use k+1 by moving down
          //boolean moveRight =   (k == d)                                               // At high-end: can only reach k == d by always moving right (since that increases k)
          //                   || (v[OFFSET + k - 1] >= v[OFFSET + k + 1]);              // k-1's x value is larger or at least equal and moving right will certainly make our x value maximal
          if (moveDown) { // Also very first move, so using artificial start point
            prevX = v[OFFSET + k + 1];
            prevY = bStart + (prevX - aStart) - (k + 1);
            x = prevX;
            y = prevY + 1;
          }
          else {
            prevX = v[OFFSET + k - 1];
            prevY = bStart + (prevX - aStart) - (k - 1);
            x = prevX + 1;
            y = prevY;
          }
          // Following diagonals on match points:
          String op = moveDown ? "moving down/inserting from b" : "moving right/deleting from a";
          while ((x < aEnd - 1) && (y < bEnd - 1) && (a[x + 1].equals(b[y + 1]))) {
            op += ", then matching '" + a[x + 1] + "' ";
            x += 1;
            y += 1;
          }
          // Store:
          v[OFFSET + k] = x;
          // Check end condition:
          if ((x >= aEnd - 1) && (y >= bEnd - 1)) {
            op += ", end reached !";
            this.log.debug("  D=%3d, k=%3d, (x:%3d, y:%3d)->(x:%3d, y:%3d), %s", d, k, prevX, prevY, x, y, op);
            break for_increasing_edit_distance;
          }
          this.log.debug("  D=%3d, k=%3d, (x:%3d, y:%3d)->(x:%3d, y:%3d), %s", d, k, prevX, prevY, x, y, op);
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
    this.log.debug("Calculating SES and LCS for A[%d, %d] and B[%d, %d] with edit distance %d", aStart, aEnd-1, bStart, bEnd-1, vEditGraph.size());
    MatchPair mp;
    EditCommand ed;
    final int N = aEnd - aStart;
    final int M = bEnd - bStart;
    final int MAX = M + N;
    final int OFFSET = MAX;

    int x = aEnd - 1;
    int y = bEnd - 1;
    for (int d = vEditGraph.size() - 1; d >= 0; d--) {
      int[] v = vEditGraph.get(d);
      // Determine where we would have come from, according to similar logic in calculateEditGraph:
      int k = (x - aStart) - (y - bStart);
      int prevK;
      if ((k == -d) || ((k != d) && (v[OFFSET + k - 1] < v[OFFSET + k + 1]))) {
        prevK = k + 1;
      }
      else {
        prevK = k - 1;
      }
      // Check prev x value (also present in current v-array!)
      int prevX = v[OFFSET + prevK];
      int prevY = bStart + (prevX - aStart) - prevK;
      // Follow snakes backward?
      while ((x > prevX) && (y > prevY)) {
        mp = DiffUtils.createMatchPair(this.a, x, this.b, y);
        assert a[x].equals(b[y]);
        this.cachedLcs.add(0, mp);
        ed = DiffUtils.createKeepCommand(this.a, x, this.b, y);
        this.cachedEs.add(0, ed);
        this.log.debug("  Adding to SES and LCS: %s", ed);
        x -= 1;
        y -= 1;
      }
      // Now determine edit move
      if (d > 0) {
        if (x == prevX) {
          ed = DiffUtils.createInsertCommand(x, this.b, y);
          this.cachedEs.add(0, ed);
          this.log.debug("  Adding to SES: %s", ed);
        }
        else if (y == prevY) {
          ed = DiffUtils.createDeleteCommand(this.a, x, y);
          this.cachedEs.add(0, ed);
          this.log.debug("  Adding to SES: %s", ed);
        }
      }
      x = prevX;
      y = prevY;
    }
  }
}
