// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.diff;

import java.util.List;

import com.pellicciotta.support.utils.Contract;

/**
 *  Determine either the shortest edit script or the longest common subsequence of two sequences.
 *
 *  @param <T> The type of elements in the sequences. This type should implement {@link #equals(Object)} and {@link #hashCode()} properly.
 *             This means that T must be a reference type (and hence not be a primitive type).
 */
public abstract class DiffAlgorithm<T>
{
  protected DiffAlgorithm() { }

  /**
   *  @return The name of this algorithm.
   */
  public abstract String name();

  protected T[] a;
  protected int aStart;
  protected int aEnd;

  /**
   *  Set the first (origin) sequence to be compared.
   *
   *  @param seq The sequence to be compared.
   */
  public void setSequenceA(T[] seq) {
    Contract.isNotNull("seq", seq);
    this.setSequenceA(seq, 0, seq.length);
  }

  /**
   *  Set the first (origin) sequence to be compared.
   *
   *  @param seq The sequence to be compared.
   *  @param startIndex The index of the first element within seq to be compared. An inclusive lower bound.
   *  @param endIndex The index of the element just beyond the last element in seq to be compared. An exclusive upper bound.
   */
  public void setSequenceA(T[] seq, int startIndex, int endIndex) {
    Contract.isNotNull("seq", seq);
    Contract.isInRange("startIndex", startIndex,0, endIndex);
    Contract.isInRange("endIndex", endIndex, startIndex, seq.length);
    this.a = seq;
    this.aStart = startIndex;
    this.aEnd = endIndex;
  }

  protected T[] b;
  protected int bStart;
  protected int bEnd;

  /**
   *  Set the second (target) sequence to be compared.
   *
   *  @param seq The sequence to be compared.
   */
  public void setSequenceB(T[] seq) {
    Contract.isNotNull("seq", seq);
    this.setSequenceB(seq, 0, seq.length);
  }

  /**
   *  Set the second (target) sequence to be compared.
   *
   *  @param seq The sequence to be compared.
   *  @param startIndex The index of the first element within seq to be compared. An inclusive lower bound.
   *  @param endIndex The index of the element just beyond the last element in seq to be compared. An exclusive upper bound.
   */
  public void setSequenceB(T[] seq, int startIndex, int endIndex) {
    Contract.isNotNull("seq", seq);
    Contract.isInRange("startIndex", startIndex,0, endIndex);
    Contract.isInRange("endIndex", endIndex, startIndex, seq.length);
    this.b = seq;
    this.bStart = startIndex;
    this.bEnd = endIndex;
  }

  protected static final MatchPair[] NO_MATCHES = new MatchPair[0];

  protected boolean checkSequencesSet() {
    return ((this.a != null) && (this.b != null));
  }

  protected boolean cannotHaveAnyMatches() {
    return ((this.a.length == 0) || (this.aEnd == this.aStart) || (this.b.length == 0) || (this.bEnd == this.bStart));
  }

  /**
   *  Determine the LCS of two sequences a and b.
   *
   *  @return An ordered array of {@link MatchPair}s. Can be empty but never <code>null</code>.
   */
  public abstract List<MatchPair> calculateLongestCommonSubsequence();

  /**
   *  Determine the size of the SES: the shortest edit script.
   *  <p>
   *  This default implementation will simply calculate {@link #calculateEditScript()} and then return
   *  its size, but concrete implementations can offer a more efficient implementation.</p>
   *
   *  @return The size of the edit script. Will maximally be the sum of the lengths of the two sequences.
   */
  public int calculateEditDistance() {
    return this.calculateEditScript(false).size();
  }

  /**
   *  Determine the shortest edit script.
   *
   *  @return An ordered list of {@link EditCommand}s to transform the first sequence into the second. All {@link EditCommand}s
   *          will refer to the indexes of elements in both sequences as they are positioned at the beginning (before any changes).
   */
  public List<EditCommand> calculateEditScript() {
    return this.calculateEditScript(false);
  }

  /**
   *  Determine the shortest edit script.
   *
   *  @param includeKeepCommands Whether to also include {@link EditType#KEEP} commands denoting matching elements.
   *  @return An ordered list of {@link EditCommand}s to transform the first sequence into the second. All {@link EditCommand}s
   *          will refer to the indexes of elements in both sequences as they are positioned at the beginning (before any changes).
   */
  public abstract List<EditCommand> calculateEditScript(boolean includeKeepCommands);

  /**
   *  @return The name of this algorithm.
   */
  @Override
  public String toString() { return this.name(); }
}
