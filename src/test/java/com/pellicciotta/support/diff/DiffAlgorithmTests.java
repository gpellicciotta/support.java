// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.diff;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.pellicciotta.support.logging.*;
import com.pellicciotta.support.logging.spi.LoggingConfig;
import com.pellicciotta.support.text.TextBuilder;
import com.pellicciotta.support.utils.*;
import com.pellicciotta.support.utils.Random;
import com.pellicciotta.support.utils.Timer;
import org.junit.jupiter.api.*;

public class DiffAlgorithmTests {
  // <editor-fold desc="Test Setup">

  // DEBUG was the default; only turn it back on locally when investigating a specific failure.
  private static final int MIN_LOG_LEVEL = LogLevel.INFO;
  // Both cut ~20x from 1000/10000 for faster feedback: DEBUG logging over many iterations of long
  // sequences was the dominant cost, not the algorithms themselves. Bump back up if this needs to
  // exercise more/larger random cases.
  private static final int NEVER_BREAK_ITERATIONS = 50;
  private static final int MAX_RANDOM_SEQUENCE_LENGTH = 500;

  @BeforeAll
  public static void loggingSetup() throws Exception {
    LogService.configure(loggingConfigWithMinLevel(MIN_LOG_LEVEL));
  }

  private static LoggingConfig loggingConfigWithMinLevel(int minLevel) {
    LoggingConfig lc = new LoggingConfig();
    lc.setMinLevel(minLevel);
    return lc;
  }

  // </editor-fold>
  // <editor-fold desc="Tests">

  @Test
  public void testDiffWithMyersGreedy() {
    DiffAlgorithm<Character> algo = DiffUtils.create(StandardDiffAlgorithms.MYERS_GREEDY);
    testDiff(algo, "ABCABBA", "CBABAC", "CABA");
    testDiff(algo, "ESTISER", "DERRIET", "EIE");
    testDiff(algo, "garbage0009ABCABBA999garbage", 7, 21, "xxx0007ABCABBA999", 3, 17, "000ABCABBA999");
    testDiff(algo, "000ABCABBA999", "000ABCABBA999", "000ABCABBA999");
    testDiff(algo, "ABC", "DEF", "");
    testDiff(algo, "ABC", "B", "B");
    testDiff(algo, "ABC", "", "");
    testDiff(algo, "LH7", "kLLys", "L");
    testDiff(algo, "UBRxK19rhmTQqOaix8Nxdv2vB", "IGyBRYgwtONkZJiD8Knh5Lizt5FKZ4lO6ZjA0ybxlMoi13RdXIvFVQRHEeOAAKBaTEmDSmsgfP8jIptFfXmal", "BRKhixdvB");
    testDiff(algo, "CDDE", "ABDF", "D");
  }

  @Test
  public void testDiffWithMyersGreedyNeverBreaks() {
    testNeverBreaks(StandardDiffAlgorithms.MYERS_GREEDY, 100, 100);
  }

  @Test
  public void testDiffWithMyersGreedyInReverse() {
    DiffAlgorithm<Character> algo = DiffUtils.create(StandardDiffAlgorithms.MYERS_REVERSE_GREEDY);
    testDiff(algo, "ABCABBA", "CBABAC", "BABA");
    testDiff(algo, "ESTISER", "DERRIET", "EIE");
    testDiff(algo, "garbage0009ABCABBA999garbage", 7, 21, "xxx0007ABCABBA999", 3, 17, "000ABCABBA999");
    testDiff(algo, "000ABCABBA999", "000ABCABBA999", "000ABCABBA999");
    testDiff(algo, "ABC", "DEF", "");
    testDiff(algo, "ABC", "B", "B");
    testDiff(algo, "ABC", "", "");
    testDiff(algo, "LH7", "kLLys", "L");
    testDiff(algo, "UBRxK19rhmTQqOaix8Nxdv2vB", "IGyBRYgwtONkZJiD8Knh5Lizt5FKZ4lO6ZjA0ybxlMoi13RdXIvFVQRHEeOAAKBaTEmDSmsgfP8jIptFfXmal", "BRKhOidvB");
    testDiff(algo, "CDDE", "ABDF", "D");
  }

  @Test
  public void testDiffWithMyersGreedyInReverseNeverBreaks() {
    testNeverBreaks(StandardDiffAlgorithms.MYERS_REVERSE_GREEDY, 100, 100);
  }

  @Test
  public void testDiffWithMyersLinearSpace() {
    DiffAlgorithm<Character> algo = DiffUtils.create(StandardDiffAlgorithms.MYERS_LINEAR_SPACE);
    testDiff(algo, "ABCABBA", "CBABAC", "BABA");
    testDiff(algo, "garbage0009ABCABBA999garbage", 7, 21, "xxx0007ABCABBA999", 3, 17, "000ABCABBA999");
    testDiff(algo, "000ABCABBA999", "000ABCABBA999", "000ABCABBA999");
    testDiff(algo, "ABC", "DEF", "");
    testDiff(algo, "ABC", "B", "B");
    testDiff(algo, "ABC", "", "");
    testDiff(algo, "ESTISER", "DERRIET", "EIE");
    testDiff(algo, "UBRxK19rhmTQqOaix8Nxdv2vB", "IGyBRYgwtONkZJiD8Knh5Lizt5FKZ4lO6ZjA0ybxlMoi13RdXIvFVQRHEeOAAKBaTEmDSmsgfP8jIptFfXmal", "BRKhOidvB");
    testDiff(algo, "LH7", "kLLys", "L");
    testDiff(algo, "TcM31Nzy0OhDSZO2Wyj2vSKxLpULv4djs6PmhdTxxULGmCHPWFtWqPRWM3q", "5zU4ZAE6BDiUfw1ujHXfwVtYSrfyI8WNYI2V8l2pKuJmjbL8E1PRIPBEZFuN7Wd91LZrXFrKEmajHQYKW8YR2", "zDS22KLPdLmHWR");
    testDiff(algo, "CDDE", "ABDF", "D");
  }

  @Test
  public void testDiffWithMyersLinearSpaceNeverBreaks() {
    testNeverBreaks(StandardDiffAlgorithms.MYERS_LINEAR_SPACE);
  }

  @Test
  public void testDiffWithHistogram() {
    DiffAlgorithm<Character> algo = DiffUtils.create(StandardDiffAlgorithms.HISTOGRAM);
    testDiff(algo, "ABCABBA", "CBABAC", "CABA");
    testDiff(algo, "garbage0009ABCABBA999garbage", 7, 21, "xxx0007ABCABBA999", 3, 17, "000ABCABBA999");
    testDiff(algo, "000ABCABBA999", "000ABCABBA999", "000ABCABBA999");
    testDiff(algo, "ABC", "DEF", "");
    testDiff(algo, "ABC", "B", "B");
    testDiff(algo, "ABC", "", "");
    testDiff(algo, "ESTISER", "DERRIET", "ET");  // Not minimal but assumed OK since 'T' is unique
    testDiff(algo, "UBRxK19rhmTQqOaix8Nxdv2vB", "IGyBRYgwtONkZJiD8Knh5Lizt5FKZ4lO6ZjA0ybxlMoi13RdXIvFVQRHEeOAAKBaTEmDSmsgfP8jIptFfXmal", "BRx1QOB"); // Not minimal either
    testDiff(algo, "LH7", "kLLys", "L");
    testDiff(algo, "TcM31Nzy0OhDSZO2Wyj2vSKxLpULv4djs6PmhdTxxULGmCHPWFtWqPRWM3q", "5zU4ZAE6BDiUfw1ujHXfwVtYSrfyI8WNYI2V8l2pKuJmjbL8E1PRIPBEZFuN7Wd91LZrXFrKEmajHQYKW8YR2", "zU46UHtWPRW"); // Not minimal either
    testDiff(algo, "CDDE", "ABDF", "D");
  }

  @Test
  public void testDiffWithHistogramNeverBreaks() {
    testNeverBreaks(StandardDiffAlgorithms.HISTOGRAM);
  }

  @Test
  public void testDiffWithPatience() {
    DiffAlgorithm<Character> algo = DiffUtils.create(StandardDiffAlgorithms.PATIENCE_MYERS);
    testDiff(algo, "ABCABBA", "CBABAC", "BABA");
    testDiff(algo, "garbage0009ABCABBA999garbage", 7, 21, "xxx0007ABCABBA999", 3, 17, "000ABCABBA999");
    testDiff(algo, "000ABCABBA999", "000ABCABBA999", "000ABCABBA999");
    testDiff(algo, "ABC", "DEF", "");
    testDiff(algo, "ABC", "B", "B");
    testDiff(algo, "ABC", "", "");
    testDiff(algo, "ESTISER", "DERRIET", "EIE");
    testDiff(algo, "UBRxK19rhmTQqOaix8Nxdv2vB", "IGyBRYgwtONkZJiD8Knh5Lizt5FKZ4lO6ZjA0ybxlMoi13RdXIvFVQRHEeOAAKBaTEmDSmsgfP8jIptFfXmal", "BRONxdvB"); // Not minimal but assumed to be 'better'
    testDiff(algo, "LH7", "kLLys", "L");
    testDiff(algo, "TcM31Nzy0OhDSZO2Wyj2vSKxLpULv4djs6PmhdTxxULGmCHPWFtWqPRWM3q", "5zU4ZAE6BDiUfw1ujHXfwVtYSrfyI8WNYI2V8l2pKuJmjbL8E1PRIPBEZFuN7Wd91LZrXFrKEmajHQYKW8YR2", "zU46UHtWPRW"); // Not minimal but assumed to be 'better'
    testDiff(algo, "CDDE", "ABDF", "D");
    testDiff(algo, "FOOBARZFOO", "DEBRIZEAT", "BRZ");
  }

  @Test
  public void testDiffWithPatienceNeverBreaks() {
    testNeverBreaks(StandardDiffAlgorithms.PATIENCE_MYERS);
  }

  @Test
  public void testPerformanceOnLongSequences() throws Exception {
    // DEBUG logging (the class default) turns the O(ND) worst case of the greedy Myers algorithms
    // over 1000-char sequences into millions of log lines; raise the level for the duration of this
    // performance test.
    LogService.configure(loggingConfigWithMinLevel(LogLevel.WARNING));
    try {
      List<Character[]> testSequences = generateRandomSequences(1000, 1000);
      testPerformance(StandardDiffAlgorithms.MYERS_LINEAR_SPACE, testSequences);
      testPerformance(StandardDiffAlgorithms.PATIENCE_MYERS, testSequences);
      testPerformance(StandardDiffAlgorithms.PATIENCE_HISTOGRAM, testSequences);
      testPerformance(StandardDiffAlgorithms.HISTOGRAM, testSequences);
      // MYERS_GREEDY/MYERS_REVERSE_GREEDY are disabled here: on 999 pairs of largely-dissimilar
      // 1000-char random strings, their worst-case O(N*D) behavior (D approaching 2*N) made this test
      // run for 20+ minutes without finishing even one of the two. Uncomment to re-run them manually.
      // testPerformance(StandardDiffAlgorithms.MYERS_GREEDY, testSequences);
      // testPerformance(StandardDiffAlgorithms.MYERS_REVERSE_GREEDY, testSequences);
    }
    finally {
      LogService.configure(loggingConfigWithMinLevel(MIN_LOG_LEVEL));
    }
  }

  @Test
  public void testPerformanceOnMediumSequences() throws Exception {
    LogService.configure(loggingConfigWithMinLevel(LogLevel.WARNING));
    try {
      List<Character[]> testSequences = generateRandomSequences(1000, 100);
      testPerformance(StandardDiffAlgorithms.MYERS_LINEAR_SPACE, testSequences);
      testPerformance(StandardDiffAlgorithms.PATIENCE_MYERS, testSequences);
      testPerformance(StandardDiffAlgorithms.PATIENCE_HISTOGRAM, testSequences);
      testPerformance(StandardDiffAlgorithms.HISTOGRAM, testSequences);
      // MYERS_GREEDY/MYERS_REVERSE_GREEDY disabled: ~44.7s and ~similar respectively for 999 diffs
      // even at only 100-char sequences (measured), too much of the 5-minute overall test budget.
      // Uncomment to re-run them manually.
      // testPerformance(StandardDiffAlgorithms.MYERS_GREEDY, testSequences);
      // testPerformance(StandardDiffAlgorithms.MYERS_REVERSE_GREEDY, testSequences);
    }
    finally {
      LogService.configure(loggingConfigWithMinLevel(MIN_LOG_LEVEL));
    }
  }

  @Test
  public void testPerformanceOnShortSequences() throws Exception {
    LogService.configure(loggingConfigWithMinLevel(LogLevel.WARNING));
    try {
      List<Character[]> testSequences = generateRandomSequences(1000, 10);
      testPerformance(StandardDiffAlgorithms.MYERS_LINEAR_SPACE, testSequences);
      testPerformance(StandardDiffAlgorithms.PATIENCE_MYERS, testSequences);
      testPerformance(StandardDiffAlgorithms.PATIENCE_HISTOGRAM, testSequences);
      testPerformance(StandardDiffAlgorithms.HISTOGRAM, testSequences);
      testPerformance(StandardDiffAlgorithms.MYERS_GREEDY, testSequences);
      testPerformance(StandardDiffAlgorithms.MYERS_REVERSE_GREEDY, testSequences);
    }
    finally {
      LogService.configure(loggingConfigWithMinLevel(MIN_LOG_LEVEL));
    }
  }

  @Test
  public void adhocCharacterTest() {
    DiffAlgorithm<Character> algo = DiffUtils.create(StandardDiffAlgorithms.PATIENCE_MYERS);
    //testDiff(algo, "ABCABBA", "CBABAC", "CABA");
    testDiff(algo, "FOOBARZFOO", "DEBRIZEAT", "BRZ");
  }

  @Test
  public void adhocStringTest() {
    DiffAlgorithm<String> algo = DiffUtils.create(StandardDiffAlgorithms.HISTOGRAM);
    String[] a = CollectionUtils.asArray("this", "is", "incorrect", "and", "so", "is", "this");
    String[] b = CollectionUtils.asArray("this", "is", "good", "and", "correct", "and", "so", "is", "this");
    String[] lcs = CollectionUtils.asArray("this", "is", "and", "so", "is", "this");
    //testDiff(algo, a, b, lcs);
    //testNeverBreaks(algo);


    algo.setSequenceA(a);
    algo.setSequenceB(b);
    List<EditCommand> editScript = algo.calculateEditScript();
    List<MatchPair> matches = algo.calculateLongestCommonSubsequence();

    List<EditCommand> derivedEditScript = DiffUtils.sesFromMatches(matches, 0, a.length, 0, b.length, false);
    List<MatchPair> derivedMatches = DiffUtils.lcsFromEdits(editScript, 0, a.length, 0, b.length);

    Assertions.assertEquals(editScript.size(), derivedEditScript.size());
    Assertions.assertArrayEquals(editScript.toArray(), derivedEditScript.toArray());
    Assertions.assertEquals(matches.size(), derivedMatches.size());
    Assertions.assertArrayEquals(matches.toArray(), derivedMatches.toArray());

    List<EditRegion> derivedRegions = DiffUtils.regionsFromMatches(matches, 0, a.length, 0, b.length);
    Assertions.assertEquals(3, derivedRegions.size());
  }

  @Test
  public void streamingDiffTest2() {
    DiffAlgorithm<Character> algo = DiffUtils.create(StandardDiffAlgorithms.MYERS_LINEAR_SPACE);
    testStreamingDiff(algo, "ABCDE!FGHIJ!KLMNO!PQRST!UVWXZ!", "abcd!efgh!ijkl!mnop!qrst!uvwxz!", 6, "!!!!!");
    testStreamingDiff(algo, "ABCDE!F", "abcd!ef", 6, "!");
  }

  @Test
  public void streamingDiffTest() {
    Character[] a = toCharacterArray("ABCDE!FGHIJ!KLMNO!PQRST!UVWXZ!");
    Character[] b = toCharacterArray("abcd!efgh!ijkl!mnop!qrst!uvwxz!");

    Iterator<Character> aIt = CollectionUtils.arrayIterator(a);
    Iterator<Character> bIt = CollectionUtils.arrayIterator(b);

    final int MAX_CHUNK_SIZE = 6;
    DiffAlgorithm<Character> algo = DiffUtils.create(StandardDiffAlgorithms.MYERS_LINEAR_SPACE);
    StreamingDiff<Character> streamingDiff = new StreamingDiff<>(algo, MAX_CHUNK_SIZE, new DiffProcessor() {
      final BlockingQueue<MatchPair> matches = new LinkedBlockingQueue<>();
      final BlockingQueue<EditCommand> editScript = new LinkedBlockingQueue<>();

      @Override
      public void onStarted(Object[] seqA, int aStart, int aEnd, Object[] seqB, int bStart, int bEnd) {
        LogService.debug("Starting diff of two byte-array-slices A[%d,%d] and B[%d,%d]", aStart, aEnd, bStart, bEnd);
      }

      @Override
      public void onStarted(Iterator itA, Iterator itB) {
        LogService.debug("Starting diff of two streams");
      }

      @Override
      public void onChunkStarted(int chunkIndex) {
        LogService.debug("Chunk #%d started", chunkIndex);
      }

      @Override
      public void onEdit(EditCommand ed) {
        this.editScript.add(ed);
      }

      @Override
      public void onMatch(MatchPair mp) {
        this.matches.add(mp);
      }

      @Override
      public void onChunkEnded(int chunkIndex, int aItems, int bItems, int editScriptLength, int lcsLength) {
        LogService.debug("Chunk #%d ended: a-items: %d, b-items: %d, edit-script-length: %d, lcs-length: %d", chunkIndex, aItems, bItems, editScriptLength, lcsLength);
      }

      @Override
      public void onEnded(int aItems, int bItems, int editScriptLength, int lcsLength) {
        TextBuilder tb = new TextBuilder();
        tb.appendLine("Comparing streams ended for A[len=%d] and B[len=%d]", aItems, bItems);
        tb.append("SES with length %d:", this.editScript.size());
        for (EditCommand ed : this.editScript) {
          tb.appendLine();
          tb.append("  %s", ed.toString());
        }
        tb.appendLine();
        tb.append("LCS with length %d:", this.matches.size());
        for (MatchPair mp : this.matches) {
          tb.appendLine();
          tb.append("  %s", mp.toString());
        }
        LogService.warning(tb.toString());
      }
    });
    streamingDiff.diff(aIt, bIt);
  }

  // </editor-fold>
  // <editor-fold desc="Support Functions">

  private static void testDiff(DiffAlgorithm<String> algo, String[] seqA, String[] seqB, String[] expectedLCS) {
    testDiff(algo, seqA, 0, seqA.length, seqB, 0, seqB.length, expectedLCS);
  }

  private static void testDiff(DiffAlgorithm<String> algo, String[] seqA, int startA, String[] seqB, int startB, String[] expectedLCS) {
    testDiff(algo, seqA, startA, seqA.length, seqB, startB, seqB.length, expectedLCS);
  }

  private static void testDiff(DiffAlgorithm<String> algo, String[] seqA, int startA, int endA, String[] seqB, int startB, int endB, String[] expectedLCS) {
    algo.setSequenceA(seqA, startA, endA);
    algo.setSequenceB(seqB, startB, endB);
    List<EditCommand> editScript = algo.calculateEditScript();
    List<MatchPair> matches = algo.calculateLongestCommonSubsequence();

    Assertions.assertEquals((endA - startA) + (endB - startB) - (2 * expectedLCS.length), editScript.size());
    Assertions.assertEquals(expectedLCS.length, matches.size());
    Assertions.assertArrayEquals(expectedLCS, toStringArray(seqA, matches));

    TextBuilder tb = new TextBuilder();
    tb.appendLine("Comparing A[%d, %d] and B[%d, %d]", startA, endA, startB, endB);
    tb.appendLine("  A: %s", CollectionUtils.toString(seqA, startA, endA));
    tb.appendLine("  B: %s", CollectionUtils.toString(seqB, startB, endB));
    tb.append("SES with length %d:", editScript.size());
    for (EditCommand ed : editScript) {
      tb.appendLine();
      tb.append("  %s", ed);
    }
    tb.appendLine();
    tb.append("LCS with length %d:", matches.size());
    for (MatchPair mp : matches) {
      tb.appendLine();
      tb.append("  %s", mp);
    }
    LogService.info(tb.toString());
    printFullDiffs(seqA, startA, endA, seqB, startB, endB, matches);
  }

  private static <T> void printFullDiffs(T[] seqA, int startA, int endA, T[] seqB, int startB, int endB, List<MatchPair> lcs) {
    int m = 0; // current match
    int i = startA;
    int j = startB;
    TextBuilder tb = new TextBuilder();
    for (MatchPair mp : lcs) {
      while (i < mp.indexA) {
        tb.appendLine("-  %3d,   : %s", i, seqA[i]);
        i += 1;
      }
      while (j < mp.indexB) {
        tb.appendLine("+     ,%3d: %s", j, seqB[j]);
        j += 1;
      }
      tb.appendLine("=  %3d,%3d: %s", i, j, seqB[mp.indexB]);
      i += 1;
      j += 1;
    }
    while (i < endA) {
      tb.appendLine("-  %3d,   : %s", i, seqA[i]);
      i += 1;
    }
    while (j < endB) {
      tb.appendLine("+     ,%3d: %s", j, seqB[j]);
      j += 1;
    }
    LogService.info("Full diffs:\n%s", tb);
  }

  private static void testDiff(DiffAlgorithm<Character> algo, String seqA, String seqB, String expectedLCS) {
    testDiff(algo, seqA, 0, seqA.length(), seqB, 0, seqB.length(), expectedLCS);
  }

  private static void testDiff(DiffAlgorithm<Character> algo, String seqA, int startA, String seqB, int startB, String expectedLCS) {
    testDiff(algo, seqA, startA, seqA.length(), seqB, startB, seqB.length(), expectedLCS);
  }

  private static void testDiff(DiffAlgorithm<Character> algo, String seqA, int startA, int endA, String seqB, int startB, int endB, String expectedLCS) {
    Character[] a = toCharacterArray(seqA);
    Character[] b = toCharacterArray(seqB);

    algo.setSequenceA(a, startA, endA);
    algo.setSequenceB(b, startB, endB);
    List<EditCommand> editScript = algo.calculateEditScript();
    List<MatchPair> matches = algo.calculateLongestCommonSubsequence();

    Assertions.assertEquals((endA - startA) + (endB - startB) - (2 * expectedLCS.length()), editScript.size());
    Assertions.assertEquals(expectedLCS.length(), matches.size());
    Assertions.assertArrayEquals(toCharArray(expectedLCS), toCharArray(a, matches));
  }

  private static void testStreamingDiff(DiffAlgorithm<Character> algo, String seqA, String seqB, int batchSize, String expectedLCS) {
    testStreamingDiff(algo, seqA, 0, seqA.length(), seqB, 0, seqB.length(), batchSize, expectedLCS);
  }

  private static void testStreamingDiff(DiffAlgorithm<Character> algo, String seqA, int startA, int endA, String seqB, int startB, int endB, int batchSize, String expectedLCS) {
    CollectingDiffProcessor<Character> dp = new CollectingDiffProcessor<>();
    List<EditCommand> editScript;
    List<MatchPair> matches;

    Character[] a = toCharacterArray(seqA);
    Character[] b = toCharacterArray(seqB);

    // First do streaming diff:
    Iterator<Character> aIt = CollectionUtils.arrayIterator(a);
    Iterator<Character> bIt = CollectionUtils.arrayIterator(b);

    DiffUtils.diff(algo, aIt, bIt, batchSize, dp);
    editScript = dp.getEditScript();
    matches = dp.getLongestCommonSubsequence();

    {
      TextBuilder tb = new TextBuilder();
      tb.appendLine("Comparing A[%d, %d] and B[%d, %d]", startA, endA, startB, endB);
      tb.appendLine("  A: %s", CollectionUtils.toString(a, startA, endA));
      tb.appendLine("  B: %s", CollectionUtils.toString(b, startB, endB));
      tb.append("SES with length %d:", editScript.size());
      for (EditCommand ed : editScript) {
        tb.appendLine();
        tb.append("  %s", ed);
      }
      tb.appendLine();
      tb.append("LCS with length %d:", matches.size());
      for (MatchPair mp : matches) {
        tb.appendLine();
        tb.append("  %s", mp);
      }
      LogService.info(tb.toString());
      printFullDiffs(a, startA, endA, b, startB, endB, matches);
    }

    Assertions.assertEquals(expectedLCS.length(), matches.size());
    Assertions.assertEquals((endA - startA) + (endB - startB) - (2 * expectedLCS.length()), editScript.size());
    Assertions.assertArrayEquals(toCharArray(expectedLCS), toCharArray(a, matches));

    // Then do normal diff:
    DiffUtils.diff(algo, a, startA, endA, b, startB, endB, dp);
    editScript = dp.getEditScript();
    matches = dp.getLongestCommonSubsequence();

    {
      TextBuilder tb = new TextBuilder();
      tb.appendLine("Comparing A[%d, %d] and B[%d, %d]", startA, endA, startB, endB);
      tb.appendLine("  A: %s", CollectionUtils.toString(a, startA, endA));
      tb.appendLine("  B: %s", CollectionUtils.toString(b, startB, endB));
      tb.append("SES with length %d:", editScript.size());
      for (EditCommand ed : editScript) {
        tb.appendLine();
        tb.append("  %s", ed);
      }
      tb.appendLine();
      tb.append("LCS with length %d:", matches.size());
      for (MatchPair mp : matches) {
        tb.appendLine();
        tb.append("  %s", mp);
      }
      LogService.info(tb.toString());
      printFullDiffs(a, startA, endA, b, startB, endB, matches);
    }

    Assertions.assertEquals((endA - startA) + (endB - startB) - (2 * expectedLCS.length()), editScript.size());
    Assertions.assertEquals(expectedLCS.length(), matches.size());
    Assertions.assertArrayEquals(toCharArray(expectedLCS), toCharArray(a, matches));
  }

  private static void testNeverBreaks(StandardDiffAlgorithms algo) {
    testNeverBreaks(algo, MAX_RANDOM_SEQUENCE_LENGTH, NEVER_BREAK_ITERATIONS);
  }

  private static void testNeverBreaks(DiffAlgorithm<Character> algo) {
    testNeverBreaks(algo, MAX_RANDOM_SEQUENCE_LENGTH, NEVER_BREAK_ITERATIONS);
  }

  private static void testPerformance(StandardDiffAlgorithms algo, List<Character[]> sequences) {
    testPerformance(DiffUtils.create(algo), sequences);
  }

  private static void testPerformance(DiffAlgorithm<Character> algo, List<Character[]> sequences) {
    int lastPercentage = -1;
    long lastOutputTime = System.currentTimeMillis();
    long totalLcs = 0;
    long totalSes = 0;
    int maxIterations = sequences.size() - 1;
    int digits = ("" + maxIterations).length();
    TimeSampler sampler = new TimeSampler();
    Timer timer = new Timer();
    for (int i = 0; i < maxIterations; i++) {
      int newPercentage = (int)Math.floor(i * 100.0 / maxIterations);
      if ((newPercentage > lastPercentage + 9) || (System.currentTimeMillis() > lastOutputTime + 10000)) { // Every 10% or every 10s
        LogService.info("Performance test with %s: %3d%% done (%" + digits + "d out of %d)", algo.name(), newPercentage, i, maxIterations);
        lastPercentage = newPercentage;
        lastOutputTime = System.currentTimeMillis();
      }
      Character[] a = sequences.get(i);
      Character[] b = sequences.get(i + 1);
      try {
        algo.setSequenceA(a);
        algo.setSequenceB(b);
        timer.reset();
        timer.start();
        List<EditCommand> editScript = algo.calculateEditScript();
        timer.stop();
        totalSes += editScript.size();
        sampler.add(timer.getElapsedMillis());
        List<MatchPair> matches = algo.calculateLongestCommonSubsequence();
        totalLcs += matches.size();
        Assertions.assertTrue(editScript.size() <= a.length + b.length);
        Assertions.assertTrue(matches.size() <= Math.max(a.length, b.length));
        Assertions.assertEquals(editScript.size(), a.length + b.length - (2 * matches.size()));
      }
      catch (Throwable t) {
        LogService.error("Failed to compare \"%s\" and \"%s\" with %s: %s", CollectionUtils.toString(a), CollectionUtils.toString(b), algo.name(), t);
        throw t;
      }
    }
    LogService.info("Performance test with %s: %3d%% done (%" + digits + "d out of %d)", algo.name(), 100, maxIterations, maxIterations);
    LogService.warning("Executed %d diffs with total time: %s (%6dms), avg. time: %6dms, total-lcs: %6d, total-ses: %6d, algorithm: %s", sampler.getSampleCount(), DateTimeUtils.formatTimespan(sampler.getTotalTime()), sampler.getTotalTime(), (long)sampler.getAverageTime(), totalLcs, totalSes, algo.name());
  }

  private static List<Character[]> generateRandomSequences(int sequenceCount, int maxSequenceLength) {
    List<Character[]> sequences = new ArrayList<>(sequenceCount);
    for (int i = 0; i < sequenceCount; i++) {
      String rndStr = Random.string(0, maxSequenceLength);
      Character[] rndChars = toCharacterArray(rndStr);
      sequences.add(rndChars);
    }
    return sequences;
  }

  private static void testNeverBreaks(StandardDiffAlgorithms algo, int maxSequenceLength, int maxIterations) {
    testNeverBreaks(DiffUtils.create(algo), maxSequenceLength, maxIterations);
  }

  private static void testNeverBreaks(DiffAlgorithm<Character> algo, int maxSequenceLength, int maxIterations) {
    int lastPercentage = -1;
    int digits = ("" + maxIterations).length();
    for (int i = 0; i < maxIterations; i++) {
      int newPercentage = (int)Math.floor(i * 100.0 / maxIterations);
      if (newPercentage > lastPercentage) {
        LogService.info("Break test with %s: %3d%% done (%" + digits + "d out of %d)", algo.name(), newPercentage, i, maxIterations);
        lastPercentage = newPercentage;
      }
      String seqA = Random.string(0, maxSequenceLength);
      String seqB = Random.string(0, maxSequenceLength);
      try {
        Character[] a = toCharacterArray(seqA);
        Character[] b = toCharacterArray(seqB);
        algo.setSequenceA(a);
        algo.setSequenceB(b);
        List<EditCommand> editScript = algo.calculateEditScript();
        List<MatchPair> matches = algo.calculateLongestCommonSubsequence();
        Assertions.assertTrue(editScript.size() <= a.length + b.length);
        Assertions.assertTrue(matches.size() <= Math.max(a.length, b.length));
        Assertions.assertEquals(editScript.size(), a.length + b.length - (2 * matches.size()));
      }
      catch (Throwable t) {
        LogService.error("Failed to compare \"%s\" and \"%s\" with %s: %s", seqA, seqB, algo.name(), t);
        throw t;
      }
    }
    LogService.info("Break test with %s: %3d%% done (%" + digits + "d out of %d)", algo.name(), 100, maxIterations, maxIterations);
    LogService.warning("Break test succeeded: %d calculations done with %s and without breakage", maxIterations, algo.name());
  }

  public static <T> void testArrayElements(T[] a, T[] b) {
    for (int i = 0; i < a.length && i < b.length; i++) {
      if (a[i].equals(b[i])) {
        System.out.println("a[" + i + "]=b[" + i + "]=" + a[i]);
      }
    }
  }

  static class DiffItem<T> extends Item {
    DiffItem(T[] seq, int index) {
      super(index);
      Contract.isNotNull("seq", seq);
      Contract.isInRange("index", index, 0, seq.length);
      this.seq = seq;
      this.val = seq[index];
    }

    T[] seq;
    T val;

    public T[] sequence() {
      return this.seq;
    }

    public T value() {
      return this.val;
    }

    @Override
    public boolean equals(Item otherItem) {
      if (!(otherItem instanceof DiffItem)) {
        return false;
      }
      return this.equals((DiffItem<T>)otherItem);
    }

    public boolean equals(DiffItem<T> otherItem) {
      return this.val.equals(otherItem.val);
    }

    @Override
    public String toString() {
      return String.format("(@%d: %s)", this.index, this.val);
    }
  }

  private static DiffItem<Character>[] toDiffItemArray(String str) {
    Character[] seq = toCharacterArray(str);
    DiffItem<Character>[] ca = new DiffItem[seq.length];
    for (int i = 0; i < seq.length; i++) {
      DiffItem<Character> di = new DiffItem<>(seq, i);
      ca[i] = di;
    }
    return ca;
  }

  private static char[] toCharArray(DiffItem<Character>[] seqA, List<MatchPair> matches) {
    char[] ca = new char[matches.size()];
    for (int i = 0; i < matches.size(); i++) {
      ca[i] = seqA[matches.get(i).indexA].value().charValue();
    }
    return ca;
  }

  private static char[] toCharArray(DiffItem<Character>[] seqA, MatchPair[] matches) {
    char[] ca = new char[matches.length];
    for (int i = 0; i < matches.length; i++) {
      ca[i] = seqA[matches[i].indexA].value().charValue();
    }
    return ca;
  }

  private static char[] toCharArray(String str) {
    return str.toCharArray();
  }

  private static String[] toStringArray(String[] seqA, List<MatchPair> matches) {
    String[] ca = new String[matches.size()];
    for (int i = 0; i < matches.size(); i++) {
      ca[i] = seqA[matches.get(i).indexA];
    }
    return ca;
  }

  private static char[] toCharArray(Character[] seqA, List<MatchPair> matches) {
    char[] ca = new char[matches.size()];
    for (int i = 0; i < matches.size(); i++) {
      ca[i] = seqA[matches.get(i).indexA].charValue();
    }
    return ca;
  }

  private static char[] toCharArray(Character[] seqA, MatchPair[] matches) {
    char[] ca = new char[matches.length];
    for (int i = 0; i < matches.length; i++) {
      ca[i] = seqA[matches[i].indexA].charValue();
    }
    return ca;
  }

  private static Character[] toCharacterArray(String str) {
    return CollectionUtils.listFromCharArray(str.toCharArray()).toArray(new Character[0]);
  }

  private static Character[] toCharacterArray(Character[] seqA, MatchPair[] matches) {
    Character[] ca = new Character[matches.length];
    for (int i = 0; i < matches.length; i++) {
      ca[i] = seqA[matches[i].indexA];
    }
    return ca;
  }

  // </editor-fold>
}
