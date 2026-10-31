// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.cache;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Cache that has no formal capacity but that stores its values as {@link SoftReference}s, hereby delegating cache eviction to the GC.
 */
public class MemoryCache<K, V> extends AbstractCache<K, V> {
  // <editor-fold desc="Construction">

  /**
   *  A soft reference that has a hard reference to the key.
   */
  private static class SoftValue<T> extends SoftReference<T> {
      final Object key;

      public SoftValue(T ref, ReferenceQueue<T> q, Object key) {
        super(ref, q);
        this.key = key;
    }
  }

  /**
   * Create a new cache with the given capacity.
   *
   * @param capacity Used solely as an initial cache size hint. The cache can grow beyond if memory and GC allow.
   */
  public MemoryCache(int capacity) {
    super(Integer.MAX_VALUE);
    this.dataStore = new HashMap<>(capacity);
  }

  private HashMap<K, SoftValue<V>> dataStore;
  private ReferenceQueue<V> queue = new ReferenceQueue<>();

  /**
   * Clean up obsolete keys when a value is evicted.
   */
  @SuppressWarnings("unchecked")
  private void processQueue() {
    while (true) {
      Reference<? extends V> o = this.queue.poll();
      if (o == null) {
        return;
      }
      SoftValue<V> k = (SoftValue<V>) o;
      Object key = k.key;
      this.dataStore.remove(key);
    }
  }

  // </editor-fold>
  // <editor-fold desc="Cache Interface Implementation">

  @Override
  public int getSize() {
    this.processQueue();
    return this.dataStore.size();
  }

  @Override
  public boolean isEmpty() {
    this.processQueue();
    return this.dataStore.isEmpty();
  }

  @Override
  public boolean containsKey(K key) {
    this.processQueue();
    return this.dataStore.containsKey(key);
  }

  @Override
  public Set<K> keySet() {
    this.processQueue();
    return this.dataStore.keySet();
  }

  @Override
  public V get(K key) {
    this.processQueue();
    SoftValue<V> valueRef = this.dataStore.get(key);
    V value = null;
    if (valueRef != null) {
      value = valueRef.get();
    }
    if (value != null) {
      this.recordHit();
    }
    else {
      this.recordMiss();
    }
    return value;
  }

  @Override
  public V get(K key, Callable<? extends V> valueProvider) throws Exception {
    this.processQueue();
    V value = this.get(key);
    if (value == null) {
      value = valueProvider.call();
      this.put(key, value);
    }
    return value;
  }

  @Override
  public V put(K key, V value) {
    this.processQueue();
    SoftValue<V> softRefVal = new SoftValue<>(value, this.queue, key);
    SoftValue<V> prevSoftRefVal = this.dataStore.put(key, softRefVal);
    if (prevSoftRefVal != null) {
      return prevSoftRefVal.get();
    }
    return null;
  }

  @Override
  public V remove(K key) {
    this.processQueue();
    SoftValue<V> removedVal = this.dataStore.remove(key);
    if (removedVal != null) {
      return removedVal.get();
    }
    return null;
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
      this.put(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public void clear() {
    this.processQueue();
    this.dataStore.clear();
  }

  // </editor-fold>
  // <editor-fold desc="Debugging Support">

  @Override
  public String toString() {
    return String.format("Memory Cache (capacity:unlimited, actual:%d, hits:%d, misses:%d)", this.getSize(), this.getHitCount(),
        this.getMissCount());
  }

  // </editor-fold>
}
