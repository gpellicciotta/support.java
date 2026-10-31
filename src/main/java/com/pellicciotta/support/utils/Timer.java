// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import java.util.function.Consumer;

/**
 * A time measurement facility.
 */
public class Timer implements AutoCloseable {
  // <editor-fold desc="Factory">

  /**
   *  Time the execution time of the provided activityToTime.
   *
   *  @param activityToTime The action to time. Must not be <code>null</code>.
   *
   *  @return The time it took the provided activityToTime to run to completion, expressed in milliseconds.
   */
  public static long timeIt(Runnable activityToTime) {
    assert activityToTime != null;
    Timer t = new Timer();
    try {
      t.start();
      activityToTime.run();
    }
    finally {
      t.stop();
    }
    return t.getElapsedMillis();
  }

  /**
   *  Create and immediately start a timer that, when {@link #close}d, will stop and also invoke the specified function.
   *  <p>
   *    This method is a convenience that enables using the fact that a {@link Timer} is {@link AutoCloseable}.
   *    It can be used for example to print the time spent in a particular block of code:
   *    <pre>
   *      // Run, time and print the elapsed time of an arbitrary block of code:
   *      try (Timer step = Timer.timeItAndThen(t -&gt; System.out.println(TimeUtils.formatTimespan(t.getElapsedTimeInMillis()) + " step execution time"))) {
   *        // Arbitrary code
   *      }
   *    </pre>
   *
   *  @param additionalCloseAction The additional action to execute after {@link #close}ing the timer object.
   *                               May be <code>null</code> in which case no additional action is invoked.
   *
   *  @return The just created and {@link #start}ed {@link Timer} object.
   */
  public static Timer timeItAndThen(Consumer<Timer> additionalCloseAction) {
    if (additionalCloseAction == null) {
      Timer t = new Timer();
      t.start();
      return t;
    }
    else {
      final Timer t = new Timer() {
        @Override
        public void close() {
          super.close();
          additionalCloseAction.accept(this);
        }
      };
      t.start();
      return t;
    }
  }

  // </editor-fold>
  // <editor-fold desc="Construction">

  /**
   * Construct a timer.
   */
  public Timer() {
    this.reset();
  }

  private long startTime;
  private long stopTime;

  // </editor-fold>
  // <editor-fold desc="Operations & State">

  /**
   * @return Is this timer currently started (and hence either running or suspended).
   */
  public boolean isStarted() {
    return (this.startTime != -1L);
  }

  /**
   * @return Is this timer currently suspended.
   */
  public boolean isSuspended() {
    return this.suspended;
  }

  /**
   * @return Has this timer been stopped already.
   */
  public boolean isStopped() {
    return ((this.stopTime != -1L) && !this.suspended);
  }

  // [Timing methods]

  /**
   * Reset the timer to its initial state. {@link #getElapsedMillis()} will return zero in this state.
   */
  public void reset() {
    this.startTime = -1L;
    this.stopTime = -1L;
    this.suspended = false;
  }

  /**
   * (Re)Start the time measurement.
   */
  public void start() {
    this.stopTime = -1L;
    this.startTime = System.currentTimeMillis();
    this.suspended = false;
  }

  /**
   * Stop the time measurement.
   */
  public void stop() {
    this.stopTime = System.currentTimeMillis();
  }


  /**
   *  Same as {@link #stop()}.
   */
  @Override
  public void close() {
    this.stop();
  }

  protected boolean suspended;

  /**
   * Suspend the time measurement.
   */
  public void suspend() {
    this.stopTime = System.currentTimeMillis();
    this.suspended = true;
  }

  /**
   * Resume the timer when it was suspended. Do nothing otherwise.
   */
  public void resume() {
    if (this.suspended) {
      this.startTime += (System.currentTimeMillis() - this.stopTime);
      this.stopTime = -1L;
      this.suspended = false;
    }
  }

  /**
   * Get the elapsed time when the timer is suspended or stopped or the currently elapsed time when the timer is still running.
   *
   * @return The elapsed time, expressed in milliseconds.
   */
  public long getElapsedMillis() {
    if (this.stopTime == -1L) {
      if (this.startTime == -1L) {
        return 0L;
      }
      else {
        return (System.currentTimeMillis() - this.startTime);
      }
    }
    else {
      return (this.stopTime - this.startTime);
    }
  }

  // </editor-fold>
  // <editor-fold desc="Human-readable representation">

  /**
   * @return The currently elapsed time, formatted according to {@link DateTimeUtils#formatTimespan(long)}.
   */
  @Override
  public String toString() {
    return DateTimeUtils.formatTimespan(this.getElapsedMillis());
  }

  // </editor-fold>
}
