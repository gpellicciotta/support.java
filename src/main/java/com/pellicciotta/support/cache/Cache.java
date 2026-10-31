// Copyright (c) Giovanni Pellicciotta
package com.pellicciotta.support.cache;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Common interface for caches, which are semi-persistent maps. This means that values can get removed implicitly due to the cache
 * capacity being reached.
 *
 * This interface is designed largely the follow the Map interface, but omitting certain operations and adding the
 * <code>get(K, Callable&lt;? extends V&gt;)</code> and <code>getCapacity()</code> methods.
 */
public interface Cache<K, V> {
  // <editor-fold desc="Information Providing Operations"/>

  /**
   * @return The actual number of cached entities (i.e. key value mappings)
   */
  int getSize();

  /**
   * @return True if this cache contains no cached entities.
   */
  boolean isEmpty();

  /**
   * @return The max. number of cache entities. Will be Integer.MAX_INT if there is no size constraint. The cache will discard
   *         entities when this number of entities is already cached and a new one needs to be added. Different cache
   *         implementations differ in how they choose which cached entity to remove.
   */
  int getCapacity();

  /**
   * Check whether a non-null value is currently associated with the provided key.
   *
   * @param key The key whose presence in this cache is to be tested
   *
   * @return True if this cache contains an entry for the specified key
   */
  boolean containsKey(K key);

  /**
   * Returns the cached value for the specified key or null if this cache contains no mapping for the key.
   *
   * @param key The key whose associated value is to be returned
   *
   * @return The cached value to which the specified key is mapped, or null if this map contains no mapping for the key
   */
  V get(K key);

  /**
   * @return Returns a view of the keys contained in this cache.
   */
  Set<K> keySet();

  // </editor-fold>
  // <editor-fold desc="Modification/Caching Operations">

  /**
   * Returns the cached value to which the specified key is mapped,
   * or puts the value produced by the valueProvider inside the cache and returns that.
   *
   * It is possible that the cache evicts another cached entity due to this operation.
   *
   * @param key The key whose associated value is to be returned
   * @param valueProvider The value producer to be used for producing a new value when no value is associated with the provided key.
   *
   * @return The value to which the specified key is now mapped.
   *
   * @throws Exception When producing a value fails.
   */
  V get(K key, Callable<? extends V> valueProvider) throws Exception;

  /**
   * Associates the specified value with the specified key in this cache. If the cache previously contained a mapping for the key,
   * the old value is replaced by the specified value.
   *
   * It is possible that the cache evicts another cached entity due to this operation.
   *
   * @param key The key with which the specified cached value is to be associated
   * @param value The value to be cached and associated with the specified key
   *
   * @return The previous cached value associated with key, or <code>null</code> if there was no previous cached entity associated with the key.
   */
  V put(K key, V value);

  /**
   * Removes any cached value associated with the key if it is present.
   *
   * @param key The key whose cached value is to be removed from the map
   *
   * @return The previously cached value associated with key, or <code>null</code> if there was no mapping for <code>key</code>.
   */
  V remove(K key);

  // </editor-fold>
  // <editor-fold desc="Bulk Operations">

  /**
   * Copies all of the mappings from the specified map to this cache. The effect of this call is equivalent to that of calling
   * <code>put(k, v)</code> for each mapping from key k to value v in the specified map.
   *
   * @param m Mappings to be stored in this map
   */
  void putAll(Map<? extends K, ? extends V> m);

  /**
   * Clears the cache. The cache will be empty after this call returns.
   */
  void clear();

  // </editor-fold>
}
