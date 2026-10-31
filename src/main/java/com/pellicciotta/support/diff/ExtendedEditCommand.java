// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.diff;

import com.pellicciotta.support.text.TextBuilder;

final class ExtendedEditCommand extends EditCommand
{
  public ExtendedEditCommand(EditType type, int startA, int endA, int startB, int endB, Object val) {
    super(type, startA, endA, startB, endB);
    this.val = val;
  }

  final Object val;

  @Override
  public String toString() {
    TextBuilder tb = new TextBuilder();
    switch (this.type) {
      case KEEP:
        for (int i = this.startInA, j = this.startInB; i < this.endInA; i++, j++) {
          tb.append("Keeping A[%d] and matching B[%d] '%s' ", i, j, this.val);
        }
        return tb.toString();
      case DELETE:
        for (int i = this.startInA; i < this.endInA; i++) {
          tb.append("Deleting A[%d] '%s' ", i, this.val);
        }
        return tb.toString();
      case INSERT:
        for (int j = this.startInB; j < this.endInB; j++) {
          tb.append("Inserting B[%d] '%s' after A[%d]", j, this.val, this.startInA);
        }
        return tb.toString();
      default:
        return "unsupported edit command of type " + this.type;
    }
  }
}
