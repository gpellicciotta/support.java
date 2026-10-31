// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.tools.diff;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import com.pellicciotta.support.utils.FileUtils;

/**
 *  Information about one particular file participating in a diff operation.
 */
class DiffFile
{
  public DiffFile(File file, DiffToolConfig config) throws IOException {
    this.file = file;
    this.lastModifiedTime = new Date(this.file.lastModified());
    this.lines = FileUtils.readAllTextLines(this.file).toArray(new String[0]);
    List<String> fLines = new ArrayList<>();
    List<Integer> fLineOffsets = new ArrayList<>();
    Pattern[] ignoreRegexes = config.getRegexIgnores().toArray(new Pattern[0]);
    int ignored = 0;
    for (int i = 0; i < this.lines.length; i++) {
      if (!this.ignore(this.lines[i], ignoreRegexes)) {
        fLines.add(this.lines[i]);
        fLineOffsets.add(ignored);
      }
      else {
        ignored += 1;
      }
    }
    if (ignored > 0) {
      this.filteredLines = fLines.toArray(new String[0]);
      this.filteredLineOffsets = fLineOffsets.stream()
                                             .mapToInt((Integer i) -> i.intValue())
                                             .toArray();
      this.filtered = true;
    }
    else {
      this.filteredLines = this.lines;
      this.filteredLineOffsets = new int[this.filteredLines.length]; // All zeros
      this.filtered = false;
    }
  }

  private boolean ignore(String line, Pattern[] ignoreRegexes) {
    for (Pattern ir : ignoreRegexes) {
      if (ir.matcher(line).matches()) {
        return true;
      }
    }
    return false;
  }

  final File file;
  final Date lastModifiedTime;
  final String[] lines;
  final String[] filteredLines;
  final int[] filteredLineOffsets;
  boolean filtered;

  /**
   * @param filteredLineIndex The filtered line index.
   * @return The original line contents.
   */
  public String getLine(int filteredLineIndex) {
    return this.filteredLines[filteredLineIndex];
  }

  /**
   * @param filteredLineIndex The filtered line index.
   * @return The original line index.
   */
  public int getLineIndex(int filteredLineIndex) {
    if (this.filteredLineOffsets == null) {
      return filteredLineIndex;
    }
    if (filteredLineIndex == this.filteredLineOffsets.length) { // Allow one beyond end
      return filteredLineIndex + 1 + this.filteredLineOffsets[filteredLineIndex - 1];
    }
    return filteredLineIndex + this.filteredLineOffsets[filteredLineIndex];
  }
}
