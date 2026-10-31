// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import com.pellicciotta.support.logging.LogService;
import static com.pellicciotta.support.utils.Random.*;

public class RandomUtilsTest {
  public static void main(String[] args) {
    for (int i = 0; i < 1000; i++) {
      int len = intInRange(5, 25);
      String str = string(len);
      LogService.info("Random %d-length string: [%s]", len, str);
      if (str.length() != len) {
        LogService.info("Got %d length string while requested was %d", str.length(), len);
      }
      String abcStr = string(len, "abc");
      LogService.info("Random %d-length abc-string: [%s]", len, abcStr);
      String hexStr = hexString(13);
      LogService.info("Random %d-length hex string: [%s]", len, hexStr);
      String base64Str = base64String(13);
      LogService.info("Random %d-length base64 string: [%s]", len, base64Str);
    }
  }
}
