// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.pellicciotta.support.diff.PatienceDiff;
import com.pellicciotta.support.text.SyntaxToken;
import com.pellicciotta.support.utils.Contract;

/**
 *  Context to match in and also publish the results of matching tokens from a character sequence.
 */
public class TokenMatchContext // implements MatchContext<Character, SyntaxToken>
{
  private static class State {
    CharSequence input;
    int inputStartIndex;
    int inputLength;
    int currentOffset;
  }

  public TokenMatchContext(CharSequence sequence, int startIndex, int length) {
    this.input = sequence;
    this.inputStartIndex = startIndex;
    this.inputLength = length;
    this.currentOffset = 0;
  }

  public TokenMatchContext subContextFromStart(int offsetFromStart) {
    Contract.isInRange("offsetFromStart", offsetFromStart, 0, this.inputLength);
    int len = this.inputLength - offsetFromStart;
    return new TokenMatchContext(this.input, this.inputStartIndex + offsetFromStart, len);
  }

  public TokenMatchContext subContextFromStart(int offsetFromStart, int length) {
    Contract.isInRange("offsetFromStart", offsetFromStart, 0, this.inputLength);
    Contract.isInRange("length", length, 0, this.inputLength - offsetFromStart);
    return new TokenMatchContext(this.input, this.inputStartIndex + offsetFromStart, length);
  }

  public TokenMatchContext subContextFromCurrent(int offsetFromCurrent) {
    Contract.isInRange("offsetFromCurrent", offsetFromCurrent, 0, this.inputLength - this.currentOffset);
    int len = this.inputLength - this.currentOffset - offsetFromCurrent;
    return new TokenMatchContext(this.input, this.currentOffset + offsetFromCurrent, len);
  }

  public TokenMatchContext subContextFromCurrent(int offsetFromCurrent, int length) {
    Contract.isInRange("offsetFromCurrent", offsetFromCurrent, 0, this.inputLength - this.currentOffset);
    Contract.isInRange("length", length, 0, this.inputLength - this.currentOffset - offsetFromCurrent);
    return new TokenMatchContext(this.input, this.currentOffset + offsetFromCurrent, length);
  }

  private final CharSequence input;
  private int inputStartIndex;
  private int inputLength;

  /**
   * @return The (absolute) index in the original input sequence that forms the first element considered for matching.
   */
  public int getStartIndex() { return this.inputStartIndex; }

  /**
   * @return The number of elements in the input sequence that are considered for matching.
   */
  public int getLength() { return this.inputLength; }

  private int currentOffset;

  /**
   * @return Whether the next item returned will be the first of this (sub)sequence (which could still be <code>null</code> if the subsequence is empty).
   */
  public boolean atStart() {
    return this.currentOffset == 0;
  }

  /**
   * @return Whether the end of the current (sub)sequence has been reached and all following calls to {@link #next()}
   *         will return <code>null</code> (unless the offset is re-set via {@link #setOffset(int)}).
   */
  public boolean atEnd() {
    return this.currentOffset < this.inputLength;
  }

  /**
   * @return The offset (index relative (to @{@link #getStartIndex()}) of the item that will be returned next.
   */
  public int getOffset() {
    return this.currentOffset;
  }

  /**
   * Set the offset (= relative index) of the item to be returned next.
   *
   * @param nextIndex The relative (to {@link #getStartIndex()}) index of the item that will be returned next.
   */
  private void setOffset(int nextIndex) {
    Contract.isInRange("nextIndex", nextIndex, 0, this.inputLength);
    this.currentOffset = nextIndex;
  }

  public boolean hasNext() {
    return (this.currentOffset < this.inputLength);
  }

  public boolean hasNext(int count) {
    return (this.currentOffset + count < this.inputLength);
  }

  /**
   * Get the item at the current offset.
   *
   * @return The character at the given offset.
   * @throws IllegalStateException If the input is already exhausted.
   */
  public char next() {
    if (this.atEnd()) {
      throw new IllegalStateException("Input is already exhausted");
    }
    char ch = this.input.charAt(this.inputStartIndex + this.currentOffset);
    return ch;
  }

  /**
   * Get the next count items at the current offset.
   *
   * @param count The number of characters to get.
   * @return The next count characters at the current offset.
   * @throws IllegalStateException If the input is already exhausted or can't provide count characters.
   */
  public CharSequence next(int count) {
    if (this.currentOffset + count >= this.inputLength) {
      throw new IllegalStateException("Input cannot provide " + count + " characters");
    }
    return this.input.subSequence(this.inputStartIndex + this.currentOffset, this.inputStartIndex + this.currentOffset + count);
  }

  public CharSequence nextIf(Pattern regex) {
    java.util.regex.Matcher m = regex.matcher(this.input);
    if ( m.find(this.inputStartIndex + this.currentOffset) &&
        (m.start() == this.inputStartIndex + this.currentOffset) &&
        (m.end() - this.inputStartIndex <= this.inputLength)) {
      return this.input.subSequence(m.start(), m.end());
    }
    return null;
  }

  public CharSequence nextIf(CharSequence str) {
    if (this.currentOffset + str.length() >= this.inputLength) {
      return null;
    }
    CharSequence inputStr = this.input.subSequence(this.inputStartIndex + this.currentOffset, this.inputStartIndex + this.currentOffset + str.length());
    if (inputStr.equals(str)) {
      return inputStr;
    }
    return null;
  }

  public CharSequence nextIf(CharSequenceMatcher m) {
    int matchCount = m.match(this.input, this.inputStartIndex + this.currentOffset, this.inputLength - this.currentOffset);
    if (matchCount > 0) {
      return this.input.subSequence(this.inputStartIndex + this.currentOffset, this.inputStartIndex + this.currentOffset + matchCount);
    }
    return null;
  }

  private final List<SyntaxToken> matches = new ArrayList<>();

  List<SyntaxToken> getMatches() {
    return new ArrayList<>(this.matches);
  }

  /**
   *  Record a match and advance the current offset accordingly.
   *
   *  @param match The match to be recorded.
   *  @throw IllegalStateException If match doesn't start at the current offset or would extend beyond the input sequence's end.
   */
  public void recordMatch(SyntaxToken match) {
    if (match.getStartIndex() != this.inputStartIndex + this.currentOffset) {
      throw new IllegalStateException("Match starts at index " + match.getStartIndex() + " while a start at " + (this.inputStartIndex + this.currentOffset) + " (abs. start: " + this.inputStartIndex + " + offset: " + this.currentOffset + ") was expected");
    }
    if (match.getEndIndex() > this.inputStartIndex + this.inputLength) {
      throw new IllegalStateException("Match ends at index " + match.getEndIndex() + " while the max. end index is " + (this.inputStartIndex + this.inputLength) + " (abs. start: " + this.inputStartIndex + " + length: " + this.inputLength + ")");
    }
    this.matches.add(match);
    this.currentOffset += match.getLength();
  }
}
