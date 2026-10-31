// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ContractTest {
  @Test
  public void isTrueWithoutDescriptionRejectsFalseWithAccurateMessage() {
    IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Contract.isTrue(false));
    assertEquals("Condition must be true", e.getMessage());
    assertDoesNotThrow(() -> Contract.isTrue(true));
  }

  @Test
  public void isFalseWithDescriptionRejectsTrueWithAccurateMessage() {
    IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Contract.isFalse("x is empty", true));
    assertEquals("Condition 'x is empty' must be false", e.getMessage());
    assertDoesNotThrow(() -> Contract.isFalse("x is empty", false));
  }

  // The primitive overloads are ambiguous with the generic one for inline varargs, so arrays are passed explicitly.
  @Test
  public void isInAcceptsPrimitivesEqualToOneOfTheValues() {
    assertDoesNotThrow(() -> Contract.isIn('b', new char[] {'a', 'b'}));
    assertDoesNotThrow(() -> Contract.isIn(3, new int[] {1, 2, 3}));
    assertDoesNotThrow(() -> Contract.isIn(3L, new long[] {1L, 3L}));
    assertDoesNotThrow(() -> Contract.isIn(2.5, new double[] {1.0, 2.5}));
  }

  @Test
  public void isInRejectsPrimitivesNotEqualToAnyValue() {
    assertThrows(IllegalArgumentException.class, () -> Contract.isIn('z', new char[] {'a', 'b'}));
    IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Contract.isIn(5, new int[] {1, 2, 3}));
    assertEquals("Argument value 5 must be one of [1, 2, 3]", e.getMessage());
    assertThrows(IllegalArgumentException.class, () -> Contract.isIn(5L, new long[] {1L, 2L}));
    assertThrows(IllegalArgumentException.class, () -> Contract.isIn(5.5, new double[] {1.0, 2.0}));
    assertThrows(IllegalArgumentException.class, () -> Contract.isIn(1, new int[0]));
  }

  @Test
  public void isInAcceptsObjectsEqualToAnyOfTheValuesNotJustTheFirst() {
    assertDoesNotThrow(() -> Contract.isIn("a", "a", "b"));
    assertDoesNotThrow(() -> Contract.isIn("b", "a", "b"));
    assertDoesNotThrow(() -> Contract.isIn(null, "a", null));
  }

  @Test
  public void isInRejectsObjectsEqualToNoneOfTheValues() {
    IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> Contract.isIn("z", "a", "b"));
    assertEquals("Argument 'z' must be one of ['a', 'b']", e.getMessage());
    assertThrows(IllegalArgumentException.class, () -> Contract.isIn(null, "a", "b"));
    assertThrows(IllegalArgumentException.class, () -> Contract.isIn("a", new String[0]));
  }
}
