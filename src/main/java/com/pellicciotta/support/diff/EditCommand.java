// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.diff;

import java.util.Objects;

/**
 *  Can be either of:<ul>
 *    Delete: delete of one or more items from sequence A
 *    Insert: insert of one or more items from sequence B
 *    Replace: combination of Delete and Insert command
 *    Keep: not really an 'edit' command, but makes it clear to keep one or more corresponding items from sequences A and B
 *  </ul>
 */
public class EditCommand {
  EditCommand(EditType type, int startA, int endA, int startB, int endB) {
    this.type = type;
    this.startInA = startA;
    this.endInA = endA;
    this.startInB = startB;
    this.endInB = endB;
  }

  final EditType type;

  /** @return The type of this command. */
  public EditType getType() {
    return this.type;
  }

  final int startInA;

  /** @return The zero-based start index of the affected range in the first sequence. */
  public int getStartInA() {
    return this.startInA;
  }

  final int endInA;

  final int startInB;

  /** @return The zero-based start index of the affected range in the second sequence. */
  public int getStartInB() {
    return this.startInB;
  }

  final int endInB;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EditCommand)) {
      return false;
    }
    EditCommand that = (EditCommand) o;
    return this.startInA == that.startInA && this.endInA == that.endInA && this.startInB == that.startInB && this.endInB == that.endInB && this.type == that.type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.type, this.startInA, this.endInA, this.startInB, this.endInB);
  }

  @Override
  public String toString() {
    switch (this.type) {
      case KEEP:
        if (((this.endInA - this.startInA) == 1) && ((this.endInB - this.startInB) == 1)) {
          return String.format("Keeping A[%d] and matching B[%d]", this.startInA, this.startInB);
        }
        else {
          return String.format("Keeping A[%d, %d[ and matching B[%d, %d[", this.startInA, this.endInA, this.startInB, this.endInB);
        }
      case DELETE:
        if ((this.endInA - this.startInA) == 1) {
          return String.format("Deleting A[%d]", this.startInA);
        }
        else {
          return String.format("Deleting A[%d, %d[", this.startInA, this.endInA);
        }
      case INSERT:
        if ((this.endInB - this.startInB) == 1) {
          return String.format("Inserting B[%d] after A[%d]", this.startInB, this.startInA);
        }
        else {
          return String.format("Inserting B[%d, %d[ after A[%d]", this.startInB, this.endInB, this.startInA);
        }
      default:
        return "unsupported edit command of type " + this.type;
    }
  }
}
