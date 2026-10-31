// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.diff;

import java.util.List;

import com.pellicciotta.support.utils.Contract;

/**
 *  An edit region is a sequence of {@link EditCommand}s that cover/affect continuous regions of items in both sequence.
 */
public class EditRegion
{
  /**
   * @param commands A non-empty series of commands that must affect contiguous regions in both sequences.
   */
  public EditRegion(List<EditCommand> commands) {
    Contract.isNotNull("commands", commands);
    Contract.isPositive("commands.size()", commands.size());
    this.commands = commands.toArray(new EditCommand[0]);
    int lastI = this.commands[0].endInA - 1;
    int lastJ = this.commands[0].endInB - 1;
    EditType lastType = this.commands[0].type;
    for (int i = 1; i < this.commands.length; i++) {
      EditCommand cmd = this.commands[i];
      if ((cmd.type == EditType.DELETE) && (cmd.startInA < lastI + 1)) {
        throw new IllegalArgumentException("command[" + i + "].startA=" + cmd.startInA + " doesn't continue last command ending in " + lastI);
      }
      if ((cmd.type == EditType.INSERT) && (cmd.startInB < lastJ + 1)) {
        throw new IllegalArgumentException("command[" + i + "].startB=" + cmd.startInB + " doesn't continue last command ending in " + lastJ);
      }
      lastI = cmd.endInA - 1;
      lastJ = cmd.endInB - 1;
    }
    this.indexA = this.commands[0].startInA;
    this.lengthA = (lastI - this.indexA) + 1;
    this.indexB = this.commands[0].startInB;
    this.lengthB = (lastJ - this.indexB) + 1;
  }

  /**
   * @param commands A non-empty series of commands that must affect contiguous regions in both sequences.
   */
  public EditRegion(EditCommand[] commands) {
    Contract.isNotNull("commands", commands);
    Contract.isPositive("commands.length", commands.length);
    this.commands = commands;
    int lastI = this.commands[0].endInA - 1;
    int lastJ = this.commands[0].endInB - 1;
    boolean keepOnly = (this.commands[0].type == EditType.KEEP);
    for (int i = 1; i < this.commands.length; i++) {
      EditCommand cmd = this.commands[i];
      if (   (keepOnly && (cmd.type != EditType.KEEP))
          || (!keepOnly && (cmd.type == EditType.KEEP))) {
        throw new IllegalArgumentException("command[" + i + "].type=" + cmd.type + " is not allowed for this region");
      }
      if (cmd.startInA != lastI + 1) {
        throw new IllegalArgumentException("command[" + i + "].startA=" + cmd.startInA + " doesn't continue last command ending in " + lastI);
      }
      if (cmd.startInB != lastJ + 1) {
        throw new IllegalArgumentException("command[" + i + "].startB=" + cmd.startInB + " doesn't continue last command ending in " + lastJ);
      }
      lastI = cmd.endInA - 1;
      lastJ = cmd.endInB - 1;
    }
    this.indexA = this.commands[0].startInA;
    this.lengthA = (lastI - this.indexA) + 1;
    this.indexB = this.commands[0].startInB;
    this.lengthB = (lastJ - this.indexB) + 1;
  }

  final EditCommand[] commands;

  public EditCommand[] getCommands() {
    return this.commands;
  }

  final int indexA;

  /**
   *  @return Index (zero-based) of a first differing or matching item in the first stream or sequence.
   */
  public int getIndexA() {
    return this.indexA;
  }

  final int lengthA;

  /**
   *  @return The number of differing or matching items in the first stream.
   */
  public int getLengthA() {
    return this.lengthA;
  }

  final int indexB;

  /**
   *  @return Index (zero-based) of a first differing or matching item in the second stream or sequence.
   */
  public int getIndexB() {
    return this.indexB;
  }

  final int lengthB;

  /**
   *  @return The number of differing or matching items in the second stream.
   */
  public int getLengthB() {
    return this.lengthB;
  }

  @Override
  public String toString() {
    return String.format("@@ -%d,%d +%d,%d @@", this.indexA + 1, this.lengthA, this.indexB + 1, this.lengthB);
  }
}
