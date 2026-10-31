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
}
