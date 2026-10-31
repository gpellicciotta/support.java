// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InfoTest
{
  @Test
  public void testStaticInfo() {
    assertEquals("support.java", Info.NAME);
  }
}
