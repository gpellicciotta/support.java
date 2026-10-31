// Copyright (c) Giovanni Pellicciotta
package com.pellicciotta.support.cache;

import com.pellicciotta.support.utils.Contract;

/**
 * Abstract base class for Cache implementation that provides additional statistics gathering methods.
 */
public abstract class AbstractCache<K, V> implements Cache<K, V> {
  // <editor-fold desc="Construction">

  /**
   *  Create a new cache with the given capacity.
   *
   *  @param capacity The number of items this cache will maximally hold.
   */
  public AbstractCache(int capacity) {
    Contract.isPositive("capacity", capacity);
    this.capacity = capacity;
    this.hits = 0;
    this.misses = 0;
  }

  // </editor-fold>
  // <editor-fold desc="Statistics">

  private long hits;

  /**
   * @return The number of times the cache succeeded in getting the requested information since the last resetStatistics calls.
   */
  public long getHitCount() {
    return this.hits;
  }

  private long misses;

  /**
   * @return The number of times the cache failed in getting the requested information since the last resetStatistics calls.
   */
  public long getMissCount() {
    return this.misses;
  }

  /**
   * Re-initialize the miss count and hit count stats to zero.
   */
  public void resetStatistics() {
    this.hits = 0;
    this.misses = 0;
  }

  /**
   * Record a hit. Should be called by classes extending this AbstractCache class.
   */
  protected void recordHit() {
    this.hits += 1;
  }

  /**
   * Record a miss. Should be called by classes extending this AbstractCache class.
   */
  protected void recordMiss() {
    this.misses += 1;
  }

  // </editor-fold>
  // <editor-fold desc="Cache Interface Implementation">

  private int capacity;

  /**
   * @return The max. number of cache entities. Will be Integer.MAX_INT if there is no size constraint. The cache will discard
   *         entities when this number of entities is already cached and a new one needs to be added. Different cache
   *         implementations differ in how they choose which cached entity to remove.
   */
  @Override
  public int getCapacity() {
    return this.capacity;
  }

  @Override
  public abstract int getSize();

  /**
   * @return True if this cache contains no cached entities.
   */
  @Override
  public boolean isEmpty() {
    return this.getSize() == 0;
  }

  @Override
  public abstract void clear();

  // </editor-fold>
  // <editor-fold desc="Debugging Support">

  @Override
  public String toString() {
    return String.format("%s Cache (capacity:%d, actual:%d, hits:%d, misses:%d)",
        this.getClass().getSimpleName(), this.capacity, this.getSize(),
        this.getHitCount(), this.getMissCount());
  }

  // </editor-fold>
}
