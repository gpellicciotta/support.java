// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

/**
 *  Class providing time sample management functionality.
 */
public class TimeSampler
{
  // <editor-fold desc="Construction">

  /**
   * Construct a new Sampler.
   */
  public TimeSampler() {
    this.reset();
  }

  // </editor-fold>
  // <editor-fold desc="Properties">

  protected long lastSampleTime;

  /**
   * @return Last sampled time, expressed in milliseconds.
   */
  public long getLastSampleTime() {
    return this.lastSampleTime;
  }

  protected long minTime;

  /**
   * @return Minimum sampled time, expressed in miliseconds.
   */
  public long getMinTime() {
    if (this.minTime == Long.MAX_VALUE) {
      return 0L;
    } else {
      return this.minTime;
    }
  }

  protected long maxTime;

  /**
   * @return Maximum sampled time, expressed in miliseconds.
   */
  public long getMaxTime() {
    if (this.maxTime == Long.MIN_VALUE) {
      return 0L;
    } else {
      return this.maxTime;
    }
  }

  protected long totalTime;

  /**
   * @return Total sampled time, that is the sum of all time samples. Expressed in milliseconds.
   */
  public long getTotalTime() {
    return this.totalTime;
  }

  protected long sampleCount;

  /**
   * @return Number of samples gathered since the last @{see: #reset} call.
   */
  public long getSampleCount() {
    return this.sampleCount;
  }

  /**
   * @return Average sampled time, expressed in milliseconds.
   */
  public double getAverageTime() {
    if (this.sampleCount > 0) {
      return (double) this.totalTime / (double) this.sampleCount;
    } else {
      return 0.0d;
    }
  }

  // </editor-fold>
  // <editor-fold desc="Operations">

  /**
   * Reset the sampler, setting all it's properties to their initial values.
   */
  public void reset() {
    this.minTime = Long.MAX_VALUE;
    this.maxTime = Long.MIN_VALUE;
    this.totalTime = 0;
    this.lastSampleTime = 0;
    this.sampleCount = 0;
  }

  /**
   * Add a new sample value.
   *
   * @param time The new value sample. It should represent a timespan expressed in milliseconds.
   *
   * @throws IllegalArgumentException When time &lt; 0.
   */
  public void add(long time) throws IllegalArgumentException {
    Contract.isInRange("time", time, 0L, Long.MAX_VALUE);
    this.lastSampleTime = time;
    this.sampleCount += 1;
    if (time < this.minTime) {
      this.minTime = time;
    }
    if (time > this.maxTime) {
      this.maxTime = time;
    }
    this.totalTime += time;
  }

  // </editor-fold>
  // <editor-fold desc="Text Representations">

  @Override
  public String toString() {
    return "[" + "sample-count: " + this.getSampleCount() + ", max: " + this.getMaxTime() + ", min: " + this.getMinTime() + ", average: " + this.getAverageTime() + ", total: " + this.getTotalTime() + "]";
  }

  // </editor-fold>
}
