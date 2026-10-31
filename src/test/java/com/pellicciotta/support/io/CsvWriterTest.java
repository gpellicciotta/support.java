// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.io;

import com.pellicciotta.support.utils.DateTimeUtils;
import com.pellicciotta.support.utils.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;

public class CsvWriterTest {
  enum Colors {
    MARINE_BLUE,
    DARK_GRAY
  }

  @Test
  public void testWritingRecords() throws IOException {
    StringWriter sw = new StringWriter();
    CsvWriter csvWriter = new CsvWriter(';', sw);
    csvWriter.writeNext("a", "record", "with", "three", " fields");
    csvWriter.addField("A;String");
    csvWriter.addField(100);
    csvWriter.addField(Long.valueOf("123456789"));
    csvWriter.writeNext();
    csvWriter.addField(DateTimeUtils.dateTime(2021, 1, 1, 9, 30, 40));
    csvWriter.addField((Double)null);
    csvWriter.addField(Colors.MARINE_BLUE);
    csvWriter.addField(new byte[] { (byte)0xAA, (byte)0xBB });
    csvWriter.writeNext("the end of", "the record");
    csvWriter.flush();
    String[] lines = StringUtils.split(sw.toString());
    Assertions.assertEquals(3, lines.length);
    Assertions.assertEquals("a;record;with;three; fields", lines[0]);
    Assertions.assertEquals("\"A;String\";100;123456789", lines[1]);
    Assertions.assertEquals("2021-01-01 09:30:40;;marine-blue;aabb;the end of;the record", lines[2]);
  }
}
