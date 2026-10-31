// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.mail;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MailAttachmentTest {
  @TempDir
  Path dir;

  private File file() throws Exception {
    return Files.writeString(dir.resolve("report.txt"), "content").toFile();
  }

  @Test
  public void toStringShowsTheFileNameWhenNoExplicitNameWasGiven() throws Exception {
    MailAttachment attachment = new MailAttachment(null, file(), "text/plain");
    assertEquals("[attachment 'report.txt' (mime-type: text/plain)]", attachment.toString());
  }

  @Test
  public void toStringShowsTheExplicitNameWhenGiven() throws Exception {
    MailAttachment attachment = new MailAttachment("Q3.txt", file(), "text/plain");
    assertEquals("[attachment 'Q3.txt' (mime-type: text/plain)]", attachment.toString());
  }
}
