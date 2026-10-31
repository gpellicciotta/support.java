// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import com.pellicciotta.support.logging.LogService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PasswordTests {
  @Test
  public void testSaltedHashEncodingAndDecoding() {
    testSaltingAndHashingFor(" ");
    testSaltingAndHashingFor("!:::::?");
    testSaltingAndHashingFor("password");
  }

  private void testSaltingAndHashingFor(final String password) {
    SaltedPasswordHash sph = PasswordUtils.createSaltedPasswordHash(password);
    // Testing encoding + decoding
    String encoded = SaltedPasswordHash.encode(sph);
    SaltedPasswordHash decSph = SaltedPasswordHash.decode(encoded);
    Assertions.assertEquals(sph, decSph);
    // Testing password
    Assertions.assertTrue(PasswordUtils.checkSaltedPasswordHash(password, decSph));
    LogService.info("Salted password hash for '%s' is: [%s]", password, decSph);
  }
}
