// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.cache;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * A stub do-nothing cache that will always stay empty.
 */
public final class StubCache<K, V> extends AbstractCache<K, V> {

  /**
   *  Create a new cache with the given capacity.
   *
   *  @param capacity The number of items this cache will maximally hold. This number will be ignored.
   */
  public StubCache(int capacity) {
    super(capacity);
  }

  @Override
  public int getCapacity() {
    return 0;
  }

  @Override
  public int getSize() {
    return 0;
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public boolean containsKey(K key) {
    return false;
  }

  @Override
  public Set<K> keySet() {
    return Collections.<K> emptySet();
  }

  @Override
  public V get(K key) {
    this.recordMiss();
    return null;
  }

  @Override
  public V get(K key, Callable<? extends V> valueProvider) throws Exception {
    this.recordMiss();
    return null;
  }

  @Override
  public V put(K key, V value) {
    return null;
  }

  @Override
  public V remove(K key) {
    return null;
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    ; // Do nothing
  }

  @Override
  public void clear() {
    ; // Do nothing
  }

  @Override
  public String toString() {
    return "Stub " + super.toString();
  }
}
