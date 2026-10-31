// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.tools;

import java.util.*;

import com.pellicciotta.support.app.BasicConfig;
import com.pellicciotta.support.config.*;
import com.pellicciotta.support.diff.StandardDiffAlgorithms;
import com.pellicciotta.support.tools.diff.DiffOutputFormat;

class DiffToolConfig extends BasicConfig
{
  public DiffToolConfig() {
    super();
    List<ConfigElement> configElements = new ArrayList<>();
    // Add existing ones, from base config:
    for (Iterator<? extends ConfigElement> it = this.topLevelConfigElementsIterator(); it.hasNext(); ) {
      ConfigElement ce = it.next();
      configElements.add(ce);
    }
    // Add additional ones:
    SettingsFactory create = new SettingsFactory();
    this.algorithm = create.standaloneEnumSetting(StandardDiffAlgorithms.class, "algorithm", "Diff algorithm to be used", StandardDiffAlgorithms.MYERS_LINEAR_SPACE);
    configElements.add(this.algorithm);
    this.outputFormat = create.standaloneEnumSetting(DiffOutputFormat.class, "output-format", "Diff output format to be produced", DiffOutputFormat.UNIFIED_DIFF);
    configElements.add(this.outputFormat);
    this.metaDataOnly = create.standaloneBooleanSetting("meta-data-only", "Don't compare file contents during a directory compare", false);
    configElements.add(this.metaDataOnly);
    this.stopOnFirstDiff = create.standaloneBooleanSetting("stop-on-first-diff", "Stop comparing after a first difference has been found", false);
    configElements.add(this.stopOnFirstDiff);
    this.contextSize = create.standaloneIntegerSetting("context-size", "The number of context items/lines to show", 3);
    configElements.add(this.contextSize);
    this.maxLineSize = create.standaloneIntegerSetting("max-line-size", "The max. number of characters to output per line", 80);
    configElements.add(this.maxLineSize);
    this.useColors = create.standaloneBooleanSetting("use-colors", "Use colors in output (if possible)", false);
    configElements.add(this.useColors);
    this.initialize(configElements.toArray(new ConfigElement[0]));
  }

  private final Setting<StandardDiffAlgorithms> algorithm;

  /**
   *  @return The configured diff algorithm.
   */
  public StandardDiffAlgorithms getAlgorithm() {
    return this.algorithm.getValueOrDefault();
  }

  /**
   *  Configure a diff algorithm to be used.
   *
   *  @param algo The algorithm to be used.
   */
  public void setAlgorithm(StandardDiffAlgorithms algo) {
    this.algorithm.setValue(algo);
  }

  private final Setting<DiffOutputFormat> outputFormat;

  /**
   *  @return The configured file comparison output format.
   */
  public DiffOutputFormat getOutputFormat() {
    return this.outputFormat.getValueOrDefault();
  }

  /**
   *  Configure an output format to be produced for file comparisons.
   *
   *  @param outputType The type of output to produce.
   */
  public void setOutputFormat(DiffOutputFormat outputType) {
    this.outputFormat.setValue(outputType);
  }

  private final Setting<Boolean> metaDataOnly;

  /**
   *  @return Whether caching is enabled or not.
   */
  public boolean getMetaDataOnly() {
    return this.metaDataOnly.getValueOrDefault();
  }

  /**
   *  Configure whether to cache results.
   *
   *  @param enableCache Enable using a cache for all types of results.
   */
  public void setMetaDataOnly(boolean enableCache) {
    this.metaDataOnly.setValue(enableCache);
  }

  private final Setting<Boolean> stopOnFirstDiff;

  /**
   *  @return Whether diff'ing should stop after a first difference has been found.
   */
  public boolean getStopOnFirstDiff() {
    return this.stopOnFirstDiff.getValueOrDefault();
  }

  /**
   *  Configure whether diff'ing should stop after a first difference has been found
   *
   *  @param stopOnFirst Stop after a first diff has been detected.
   */
  public void setStopOnFirstDiff(boolean stopOnFirst) {
    this.stopOnFirstDiff.setValue(stopOnFirst);
  }

  private final Setting<Integer> contextSize;

  /**
   *  @return How many items/lines of context to use in unified diff output.
   */
  public int getContextSize() {
    return this.contextSize.getValueOrDefault();
  }

  /**
   *  Configure the number of items/lines of context to use in unified diff output.
   *
   *  @param contextSize Number of context items/lines to use.
   */
  public void setContextSize(int contextSize) {
    this.contextSize.setValue(contextSize);
  }

  private final Setting<Integer> maxLineSize;

  /**
   *  @return The max. size of output lines to assume, i.e. the terminal width.
   */
  public int getMaxLineSize() {
    return this.maxLineSize.getValueOrDefault();
  }

  /**
   *  Configure the max. output line size to assume.
   *
   *  @param lineSize Max. number of characters that can be output per line.
   */
  public void setMaxLineSize(int lineSize) {
    this.maxLineSize.setValue(lineSize);
  }

  private final Setting<Boolean> useColors;

  /**
   *  @return Whether output should use colors.
   */
  public boolean getUseColors() {
    return this.useColors.getValueOrDefault();
  }

  /**
   *  Configure whether output should use colors.
   *
   *  @param useColors Use colors in the output.
   */
  public void setUseColors(boolean useColors) {
    this.useColors.setValue(useColors);
  }
}
