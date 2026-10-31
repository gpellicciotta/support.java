// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.sql;

import com.pellicciotta.support.config.Config;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DbConfigTest {
  @Test
  public void testDefaults() {
    DbConfig db = new DbConfig("auth");
    new Config(db);

    Assertions.assertEquals("jdbc:mariadb://localhost:3306/auth", db.getJdbcUrl());
    Assertions.assertEquals("pellicciotta", db.getUser());
    Assertions.assertNull(db.getPassword());
  }

  @Test
  public void testDefaultDbNameIsParameterized() {
    DbConfig auth = new DbConfig("auth");
    DbConfig counters = new DbConfig("counters");

    Assertions.assertEquals("jdbc:mariadb://localhost:3306/auth", auth.getJdbcUrl());
    Assertions.assertEquals("jdbc:mariadb://localhost:3306/counters", counters.getJdbcUrl());
  }

  @Test
  public void testNullDefaultDbNameRejected() {
    Assertions.assertThrows(IllegalArgumentException.class, () -> new DbConfig(null));
  }

  @Test
  public void testSettersOverrideDefaults() {
    DbConfig db = new DbConfig("counters");

    db.setJdbcUrl("jdbc:mariadb://db-host:3306/other");
    db.setUser("someone");
    db.setPassword("secret");

    Assertions.assertEquals("jdbc:mariadb://db-host:3306/other", db.getJdbcUrl());
    Assertions.assertEquals("someone", db.getUser());
    Assertions.assertEquals("secret", db.getPassword());
  }
}
