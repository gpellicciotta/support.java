// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

/**
 *  Class providing number sample management functionality.
 */
public class NumberSampler {
  // <editor-fold desc="Construction">

  /**
   * Construct a new Sampler.
   */
  public NumberSampler() {
    this.reset();
  }

  // </editor-fold>
  // <editor-fold desc="Properties">

  /** The most recently sampled value. */
  protected double lastSampleValue;

  /**
   * @return Last sampled value.
   */
  public double getLastSampleValue() {
    return this.lastSampleValue;
  }

  /** The smallest sampled value; NaN when no samples were gathered. */
  protected double minSampleValue;

  /**
   * @return Minimum sample value.
   */
  public double getMinSampleValue() {
    if (Double.isNaN(this.minSampleValue)) {
      return 0L;
    }
    else {
      return this.minSampleValue;
    }
  }

  /** The largest sampled value; NaN when no samples were gathered. */
  protected double maxSampleValue;

  /**
   * @return Maximum sampled value.
   */
  public double getMaxSampleValue() {
    if (Double.isNaN(this.maxSampleValue)) {
      return 0L;
    }
    else {
      return this.maxSampleValue;
    }
  }

  /** The running sum of all sampled values. */
  protected long sumOfSampleValues;

  /**
   * @return The sum of all samples.
   */
  public double getSumOfSampleValues() {
    return this.sumOfSampleValues;
  }

  /** The number of samples gathered since the last reset. */
  protected long sampleCount;

  /**
   * @return Number of samples gathered since the last {@link #reset} call.
   */
  public long getSampleCount() {
    return this.sampleCount;
  }

  /**
   * @return Average sampled value.
   */
  public double getAverageSampleValue() {
    if (this.sampleCount > 0) {
      return this.sumOfSampleValues / (double) this.sampleCount;
    }
    else {
      return 0.0d;
    }
  }

  // </editor-fold>
  // <editor-fold desc="Operations">

  /**
   * Reset the sampler, setting all it's properties to their initial values.
   */
  public void reset() {
    this.minSampleValue = Double.NaN;
    this.maxSampleValue = Double.NaN;
    this.sumOfSampleValues = 0;
    this.lastSampleValue = 0;
    this.sampleCount = 0;
  }

  /**
   * Add a new sample value.
   *
   * @param value The new value sample.
   */
  public void add(double value) throws IllegalArgumentException {
    this.lastSampleValue = value;
    this.sampleCount += 1;
    if (Double.isNaN(this.minSampleValue) || (value < this.minSampleValue)) {
      this.minSampleValue = value;
    }
    if (Double.isNaN(this.maxSampleValue) || (value > this.maxSampleValue)) {
      this.maxSampleValue = value;
    }
    this.sumOfSampleValues += value;
  }

  // </editor-fold>
  // <editor-fold desc="Text Representations">

  @Override
  public String toString() {
    return "[" + "sample-count: " + this.getSampleCount() + ", max: " + this.getMaxSampleValue() + ", min: " + this.getMinSampleValue() + ", average: " + this.getAverageSampleValue() + ", total: " + this.getSumOfSampleValues() + "]";
  }

  // </editor-fold>
}
