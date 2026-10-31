// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.diff;

/**
 *  An item in a sequence.
 */
public abstract class Item
{
  protected Item(int index) {
    this.index = index;
  }

  protected final int index;

  /**
   *  @return The index of the item in its originating sequence.
   */
  int index() {
    return this.index;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof Item)) {
      return false;
    }
    if (other == null) {
      return false;
    }
    return this.equals((Item)other);
  }

  public abstract boolean equals(Item otherItem);
}
