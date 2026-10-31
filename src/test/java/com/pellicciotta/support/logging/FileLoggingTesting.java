// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.pellicciotta.support.logging.file.FileBasedLogBackEnd;
import com.pellicciotta.support.logging.spi.LoggingConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileLoggingTesting {
  @TempDir
  Path tempDir;

  private FileBasedLogBackEnd activeBackEnd;

  private void activate(FileBasedLogBackEnd backEnd) {
    this.activeBackEnd = backEnd;
    LogService.setBackEnd(backEnd);
  }

  @AfterEach
  public void cleanup() {
    LogService.setBackEnd(null);
    if (this.activeBackEnd != null) {
      this.activeBackEnd.close();
      this.activeBackEnd = null;
    }
  }

  @Test
  public void logsToFileAndCreatesMissingDirectories() throws IOException {
    Path logFile = this.tempDir.resolve("nested/dir/app.log");
    LoggingConfig config = new LoggingConfig();
    config.setFilePath(logFile.toString());

    this.activate(new FileBasedLogBackEnd(config));
    LogService.info("Testing the file back-end");
    LogService.error("An error message");
    this.activeBackEnd.close();

    Assertions.assertTrue(Files.exists(logFile));
    List<String> lines = Files.readAllLines(logFile);
    Assertions.assertTrue(lines.stream().anyMatch(l -> l.contains("Testing the file back-end")));
    Assertions.assertTrue(lines.stream().anyMatch(l -> l.contains("An error message")));
  }

  @Test
  public void appendsByDefaultButCanOverwrite() throws IOException {
    Path logFile = this.tempDir.resolve("app.log");
    LoggingConfig config = new LoggingConfig();
    config.setFilePath(logFile.toString());
    Assertions.assertTrue(config.getFileAppend());

    this.activate(new FileBasedLogBackEnd(config));
    LogService.info("First line");
    this.activeBackEnd.close();

    this.activate(new FileBasedLogBackEnd(config));
    LogService.info("Second line");
    this.activeBackEnd.close();

    Assertions.assertEquals(2, Files.readAllLines(logFile).size());

    config.setFileAppend(false);
    this.activate(new FileBasedLogBackEnd(config));
    LogService.info("Only line");
    this.activeBackEnd.close();

    List<String> lines = Files.readAllLines(logFile);
    Assertions.assertEquals(1, lines.size());
    Assertions.assertTrue(lines.get(0).contains("Only line"));
  }

  @Test
  public void discardsEventsWhenNoFilePathConfigured() {
    this.activate(new FileBasedLogBackEnd());
    Assertions.assertDoesNotThrow(() -> LogService.info("Nowhere to go"));
  }
}
