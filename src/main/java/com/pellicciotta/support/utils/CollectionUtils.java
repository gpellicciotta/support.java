// Copyright (c) Giovanni Pellicciotta

package com.pellicciotta.support.utils;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A set of collections related utility methods.
 */
public final class CollectionUtils {
  /** Prevent instantiation */
  private CollectionUtils() { }

  // <editor-fold desc="Conversion Methods">

  /**
   *  Check whether the array contains the specified element.
   *
   *  @param array The array to search through.
   *  @param element The element to check.
   *  @return True if the array contains the element.
   */
  public static boolean contains(long[] array, long element) {
    for (int i = 0; i < array.length; i++) {
      if (array[i] == element) {
        return true;
      }
    }
    return false;
  }

  /**
   *  Check whether the array contains the specified element.
   *
   *  @param array The array to search through.
   *  @param element The element to check.
   *  @return True if the array contains the element.
   */
  public static boolean contains(int[] array, int element) {
    for (int i = 0; i < array.length; i++) {
      if (array[i] == element) {
        return true;
      }
    }
    return false;
  }

  /**
   *  Check whether the array contains the specified element.
   *
   *  @param array The array to search through.
   *  @param element The element to check.
   *  @return True if the array contains the element.
   */
  public static boolean contains(char[] array, char element) {
    for (int i = 0; i < array.length; i++) {
      if (array[i] == element) {
        return true;
      }
    }
    return false;
  }

  /**
   *  Check whether the array contains the specified element.
   *
   *  @param array The array to search through.
   *  @param element The element to check.
   *  @return True if the array contains the element.
   */
  public static boolean contains(double[] array, double element) {
    for (int i = 0; i < array.length; i++) {
      if (array[i] == element) {
        return true;
      }
    }
    return false;
  }

  /**
   *  Check whether the string array contains the specified element, while ignoring case.
   *
   *  @param array The array to search through.
   *  @param element The element to check.
   *  @return True if the array contains the element.
   */
  public static boolean containsIgnoreCase(String[] array, String element) {
    for (int i = 0; i < array.length; i++) {
      if (array[i].equalsIgnoreCase(element)) {
        return true;
      }
    }
    return false;
  }

  /**
   *  Check whether the array contains the specified element.
   *
   *  @param array The array to search through.
   *  @param element The element to check.
   *  @return True if the array contains the element.
   */
  public static <T> boolean contains(T[] array, T element) {
    for (int i = 0; i < array.length; i++) {
      if (array[i].equals(element)) {
        return true;
      }
    }
    return false;
  }

  /**
   *  Check whether the collection contains the specified element.
   *
   *  @param collection The collection to search through.
   *  @param element The element to check.
   *  @return True if the collection contains the element.
   */
  public static <T> boolean contains(Collection<T> collection, T element) {
    return collection.contains(element);
  }

  /**
   *  Get the value associated with a certain key, or - if no such value exists yet,
   *  first add a default value and then return that.
   *
   *  @param data The data to get/add data from/to.
   *  @param key The key to search for.
   *  @param defaultVal The default value to get added if no value is yet present for the given key.
   *
   *  @return The value associated with key, or defaultVal if there was previously no value associated with the given key.
   */
  public static <K, V> V getOrAddDefault(Map<K, V> data, K key, V defaultVal) {
    Contract.isNotNull("data", data);
    Contract.isNotNull("key", key);
    data.putIfAbsent(key, defaultVal);
    return data.get(key);
  }

  /**
   *  Return an {@link Iterable} wrapping a concrete {@link Enumeration}, so
   *  it can be used in foreach loops.
   *
   *  @param enumeration The enumeration to wrap.
   *
   *  @return The wrapping {@link Iterable}.
   */
  public static <T> Iterable<T> asIterable(Enumeration<T> enumeration) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return asIterator(enumeration);
      }
    };
  }

  /**
   *  Return an {@link Iterator} wrapping a concrete {@link Enumeration}.
   *
   *  @param enumeration The enumeration to wrap.
   *
   *  @return The wrapping {@link Iterator}.
   */
  public static <T> Iterator<T> asIterator(Enumeration<T> enumeration) {
    return new Iterator<T>() {
      @Override
      public boolean hasNext() {
        return enumeration.hasMoreElements();
      }

      @Override
      public T next() {
        return enumeration.nextElement();
      }
    };
  }

  /**
   *  Return an {@link Iterable} wrapping a concrete {@link Iterator}, so
   *  it can be used in foreach loops.
   *
   *  @param it The iterator to wrap.
   *
   *  @return The wrapping {@link Iterable}.
   */
  public static <T> Iterable<T> asIterable(Iterator<T> it) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return it;
      }
    };
  }

  /**
   *  Return a slice of an original array.
   *
   *  @param <T> The element type.
   *  @param elements Zero or more elements to take a slice of and return that as a new list. Must not be <code>null</code>.
   *  @param startIndex Index of first element to copy into the returned slice.
   *
   *  @return A new array, possibly empty, that represents a slice of the original elements array, starting at {@code startIndex}.
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] sliceOf(T[] elements, int startIndex) {
    Contract.isNotNull("elements", elements);
    return sliceOf(elements, startIndex, elements.length);
  }

  /**
   *  Return a slice of an original array.
   *
   *  @param <T> The element type.
   *  @param elements Zero or more elements to take a slice of and return that as a new list. Must not be <code>null</code>.
   *  @param startIndex Index of first element to copy into the returned slice.
   *  @param endIndex Index that is one beyond the last element in {@code elements} (i.e. the element at this index, if any, will not be part of the slice)
   *                  that should be included in the slice.
   *
   *  @return A new array, possibly empty, that represents a slice of the original elements array, starting at {@code startIndex}.
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] sliceOf(T[] elements, int startIndex, int endIndex) {
    Contract.isNotNull("elements", elements);
    Contract.isInRange("startIndex", startIndex, 0, elements.length);
    Contract.isInRange("endIndex", endIndex, startIndex, elements.length);
    T[] arr = (T[])Array.newInstance(elements.getClass().getComponentType(), endIndex - startIndex);
    System.arraycopy(elements, startIndex, arr, 0, arr.length);
    return arr;
  }

  /**
   *  Return an array of all provided elements.
   *
   *  @param elementType The element type.
   *  @param elementsList A list of the provided element type elements. Must not be null.
   *
   *  @return A new array, possibly empty, of the provided elements.
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] asArray(Class<T> elementType, List<T> elementsList) {
    Contract.isNotNull(elementType);
    Contract.isNotNull(elementsList);
    T[] arr = (T[]) Array.newInstance(elementType, elementsList.size());
    return elementsList.toArray(arr);
  }

  /**
   *  Return an array of all provided elements.
   *
   *  @param elementType The element type.
   *  @param elementsIterator An iterator of the provided element type elements. Must not be null.
   *
   *  @return A new array, possibly empty, of the provided elements.
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] asArray(Class<T> elementType, Iterator<T> elementsIterator) {
    Contract.isNotNull(elementType);
    Contract.isNotNull(elementsIterator);
    List<T> els = new ArrayList<>();
    elementsIterator.forEachRemaining(els::add);
    T[] arr = (T[]) Array.newInstance(elementType, els.size());
    return els.toArray(arr);
  }

  /**
   *  Return an array of all provided elements.
   *
   *  @param <T> The element type.
   *  @param elements Zero or more elements to place in a new list. Must not be null.
   *
   *  @return A new array, possibly empty, of the provided elements.
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] asArray(T... elements) {
    Contract.isNotNull(elements);
    T[] arr = (T[]) Array.newInstance(elements.getClass().getComponentType(), elements.length);
    System.arraycopy(elements, 0, arr, 0, elements.length);
    return arr;
  }

  /**
   *  Return an array of all provided elements. If an element itself is an array, it's elements
   *
   *  @param <T> The element type.
   *  @param element A first element to place in the result array.
   *  @param extraElements Zero or more elements to placed also in the result array. Must not be null.
   *
   *  @return A new array, possibly empty, of the provided elements.
   */
  @SuppressWarnings("unchecked")
  public static <T> T[] asArray(T element, T[] extraElements) {
    Contract.isNotNull(extraElements);
    T[] arr = (T[]) Array.newInstance(element.getClass(), 1 + extraElements.length);
    arr[0] = element;
    System.arraycopy(extraElements, 0, arr, 1, extraElements.length);
    return arr;
  }

  /**
   *  Return an array of all provided elements.
   *
   *  @param elements Zero or more elements to place in a new list. Must not be null.
   *                  All <code>null</code>-elements will be omitted from the result.
   *
   *  @return A new array, possibly empty, of the provided elements.
   */
  public static byte[] asByteArray(List<Byte> elements) {
    Contract.isNotNull(elements);
    Byte[] objArray = elements.stream().filter(l -> l != null).collect(Collectors.toList()).toArray(new Byte[0]);
    byte[] byteArray = new byte[objArray.length];
    for (int i = 0; i < byteArray.length; i++) {
      byteArray[i] = objArray[i].byteValue();
    }
    return byteArray;
  }

  /**
   *  Return an array of all provided elements.
   *
   *  @param elements Zero or more elements to place in a new list. Must not be null.
   *
   *  @return A new array, possibly empty, of the provided elements.
   */
  public static byte[] asByteArray(byte... elements) {
    Contract.isNotNull(elements);
    byte[] arr = new byte[elements.length];
    System.arraycopy(elements, 0, arr, 0, elements.length);
    return arr;
  }

  /**
   *  Return an array of all provided elements.
   *
   *  @param elements Zero or more elements to place in a new list. Must not be null.
   *                  All <code>null</code>-elements will be omitted from the result.
   *
   *  @return A new array, possibly empty, of the provided elements.
   */
  public static char[] asCharArray(List<Character> elements) {
    Contract.isNotNull(elements);
    Character[] objArray = elements.stream().filter(l -> l != null).collect(Collectors.toList()).toArray(new Character[0]);
    char[] charArray = new char[objArray.length];
    for (int i = 0; i < charArray.length; i++) {
      charArray[i] = objArray[i].charValue();
    }
    return charArray;
  }

  /**
   *  Return an array of all provided elements.
   *
   *  @param elements Zero or more elements to place in a new list. Must not be null.
   *
   *  @return A new array, possibly empty, of the provided elements.
   */
  public static char[] asCharArray(char...elements) {
    Contract.isNotNull(elements);
    char[] arr = new char[elements.length];
    System.arraycopy(elements, 0, arr, 0, elements.length);
    return arr;
  }

  /**
   *  Return an array of all provided elements.
   *
   *  @param elements Zero or more elements to place in a new list. Must not be null.
   *                  All <code>null</code>-elements will be omitted from the result.
   *
   *  @return A new array, possibly empty, of the provided elements.
   */
  public static int[] asIntArray(List<Integer> elements) {
    Contract.isNotNull(elements);
    return elements.stream().filter(l -> l != null).mapToInt(l -> l).toArray();
  }

  /**
   *  Return an array of all provided elements.
   *
   *  @param elements Zero or more elements to place in a new list. Must not be null.
   *
   *  @return A new array, possibly empty, of the provided elements.
   */
  public static int[] asIntArray(int... elements) {
    Contract.isNotNull(elements);
    int[] arr = new int[elements.length];
    System.arraycopy(elements, 0, arr, 0, elements.length);
    return arr;
  }

  /**
   *  Return an array of all provided elements.
   *
   *  @param elements Zero or more elements to place in a new list. Must not be null.
   *                  All <code>null</code>-elements will be omitted from the result.
   *
   *  @return A new array, possibly empty, of the provided elements.
   */
  public static long[] asLongArray(List<Long> elements) {
    Contract.isNotNull(elements);
    return elements.stream().filter(l -> l != null).mapToLong(l -> l).toArray();
  }

  /**
   *  Return an array of all provided elements.
   *
   *  @param elements Zero or more elements to place in a new list. Must not be null.
   *
   *  @return A new array, possibly empty, of the provided elements.
   */
  public static long[] asLongArray(long... elements) {
    Contract.isNotNull(elements);
    long[] arr = new long[elements.length];
    System.arraycopy(elements, 0, arr, 0, elements.length);
    return arr;
  }

  /**
   *  Return an array of all provided elements.
   *
   *  @param elements Zero or more elements to place in a new list. Must not be null.
   *                  All <code>null</code>-elements will be omitted from the result.
   *
   *  @return A new array, possibly empty, of the provided elements.
   */
  public static float[] asFloatArray(List<Float> elements) {
    Contract.isNotNull(elements);
    Float[] objArray = elements.stream().filter(l -> l != null).collect(Collectors.toList()).toArray(new Float[0]);
    float[] floatArray = new float[objArray.length];
    for (int i = 0; i < floatArray.length; i++) {
      floatArray[i] = objArray[i].floatValue();
    }
    return floatArray;
  }

  /**
   *  Return an array of all provided elements.
   *
   *  @param elements Zero or more elements to place in a new list. Must not be null.
   *
   *  @return A new array, possibly empty, of the provided elements.
   */
  public static float[] asFloatArray(float... elements) {
    Contract.isNotNull(elements);
    float[] arr = new float[elements.length];
    System.arraycopy(elements, 0, arr, 0, elements.length);
    return arr;
  }

  /**
   *  Return an array of all provided elements.
   *
   *  @param elements Zero or more elements to place in a new list. Must not be null.
   *                  All <code>null</code>-elements will be omitted from the result.
   *
   *  @return A new array, possibly empty, of the provided elements.
   */
  public static double[] asDoubleArray(List<Double> elements) {
    Contract.isNotNull(elements);
    return elements.stream().filter(l -> l != null).mapToDouble(l -> l).toArray();
  }

  /**
   *  Return an array of all provided elements.
   *
   *  @param elements Zero or more elements to place in a new list. Must not be null.
   *
   *  @return A new array, possibly empty, of the provided elements.
   */
  public static double[] asDoubleArray(double... elements) {
    Contract.isNotNull(elements);
    double[] arr = new double[elements.length];
    System.arraycopy(elements, 0, arr, 0, elements.length);
    return arr;
  }

  /**
   *  Create a list of all provided elements, similar to {@link Arrays#asList}.
   *
   *  @param <T> The element type.
   *  @param enumeration The enumeration whose elements to put in a list.
   *  @param sortOrder The way to sort the elements.
   *
   *  @return The created list, containing all provided elements, in sorted order
   */
  public static <T> List<T> asSortedList(Enumeration<T> enumeration, Comparator<? super T> sortOrder) {
    Contract.isNotNull(enumeration);
    List<T> coll = new ArrayList<>();
    while (enumeration.hasMoreElements()) {
      coll.add(enumeration.nextElement());
    }
    coll.sort(sortOrder);
    return coll;
  }

  /**
   *  Create a list of all provided elements, similar to {@link Arrays#asList}.
   *
   *  @param <T> The element type.
   *  @param enumeration The enumeration whose elements to put in a list.
   *
   *  @return The created list, containing all provided elements.
   */
  public static <T> List<T> asList(Enumeration<T> enumeration) {
    Contract.isNotNull(enumeration);
    List<T> coll = new ArrayList<>();
    while (enumeration.hasMoreElements()) {
      coll.add(enumeration.nextElement());
    }
    return coll;
  }

  /**
   *  Create a list of all provided elements, similar to {@link Arrays#asList}.
   *
   *  @param <T> The element type.
   *  @param elementsIt Zero or more elements to place in a new list. Must not be null.
   *  @param sortOrder The way to sort the elements.
   *
   *  @return The created list, containing all provided elements, in sorted order
   */
  public static <T> List<T> asSortedList(Iterator<T> elementsIt, Comparator<? super T> sortOrder) {
    Contract.isNotNull(elementsIt);
    List<T> coll = new ArrayList<>();
    while (elementsIt.hasNext()) {
      coll.add(elementsIt.next());
    }
    coll.sort(sortOrder);
    return coll;
  }

  /**
   *  Create a list of all provided elements, similar to {@link Arrays#asList}.
   *
   *  @param <T> The element type.
   *  @param elementsIt Zero or more elements to place in a new list. Must not be null.
   *
   *  @return The created list, containing all provided elements.
   */
  public static <T> List<T> asList(Iterator<T> elementsIt) {
    Contract.isNotNull(elementsIt);
    List<T> coll = new ArrayList<>();
    while (elementsIt.hasNext()) {
      coll.add(elementsIt.next());
    }
    return coll;
  }

  /**
   *  Create a list of all provided elements, similar to {@link Arrays#asList}.
   *
   *  @param <T> The element type.
   *  @param elements Zero or more elements to place in a new list. Must not be null.
   *  @param sortOrder The way to sort the elements.
   *
   *  @return The created list, containing all provided elements, in sorted order
   */
  public static <T> List<T> asSortedList(T[] elements, Comparator<? super T> sortOrder) {
    Contract.isNotNull(elements);
    List<T> coll = new ArrayList<>();
    for (int i = 0; i < elements.length; i++) {
      coll.add(elements[i]);
    }
    coll.sort(sortOrder);
    return coll;
  }

  /**
   *  Create a list of all provided elements, similar to {@link Arrays#asList}.
   *
   *  @param <T> The element type.
   *  @param elements Zero or more elements to place in a new list. Must not be null.
   *
   *  @return The created list, containing all provided elements.
   */
  @SafeVarargs
  public static <T> List<T> asList(T... elements) {
    Contract.isNotNull(elements);
    List<T> coll = new ArrayList<>();
    for (T el : elements) {
      coll.add(el);
    }
    return coll;
  }

  /**
   *  Create a set of all provided elements, similar to {@link Arrays#asList}.
   *
   *  @param enumeration The enumeration whose elements to put in a list.
   *
   *  @return The created set, containing all provided elements.
   */
  public static <T> Set<T> asSet(Enumeration<T> enumeration) {
    Contract.isNotNull(enumeration);
    Set<T> coll = new HashSet<>();
    while (enumeration.hasMoreElements()) {
      coll.add(enumeration.nextElement());
    }
    return coll;
  }

  /**
   *  Create a set of all provided elements, similar to {@link Arrays#asList}.
   *
   *  @param <T> The element type.
   *  @param elements Zero or more elements to place in a new list. Must not be null.
   *
   *  @return The created set, containing all provided elements.
   */
  @SafeVarargs
  public static <T> Set<T> asSet(T... elements) {
    Contract.isNotNull(elements);
    Set<T> coll = new HashSet<>();
    for (T el : elements) {
      coll.add(el);
    }
    return coll;
  }

  /**
   *  Create a map of all provided elements, similar to {@link Arrays#asList}.
   *
   *  @param keyValuePairs An even number of key/value pairs.
   *
   *  @return The created map, containing all provided elements.
   *
   *  @throws IllegalArgumentException If keyValuePairs is not even.
   */
  @SafeVarargs
  public static <T> Map<T, T> asMap(T... keyValuePairs) {
    if ((keyValuePairs.length % 2) != 0) {
      throw new IllegalArgumentException("An even number of arguments is needed but " + keyValuePairs.length + " were given");
    }
    Map<T, T> map = new HashMap<>();
    for (int i = 0; (i + 1) < keyValuePairs.length; i += 2) {
      T key = keyValuePairs[i];
      T val = keyValuePairs[i + 1];
      map.put(key, val);
    }
    return map;
  }

  /**
   *  Create an a new array that contains all elements of the source array, but in reverse order.
   *
   *  @param array The array to create a reversed copy of.
   *
   *  @return An new array that contains all elements in reverse order. This is using a shallow-copy.
   */
  public static <T> T[] reverse(T[] array) {
    Contract.isNotNull(array);
    T[] destArray = array.clone();
    for (int i = 0, mid = array.length / 2, last = array.length - 1; i < mid; i++) {
      T tmp = destArray[i];
      destArray[i] = destArray[last - i];
      destArray[last - i] = tmp;
    }
    return destArray;
  }

  /**
   *  Create an a new array that contains both parts.
   *
   *  @param firstPart An existing array to be extended with a single element.
   *  @param lastPart Extra element to be added.
   *
   *  @return An new array that contains both parts in succession.
   */
  public static byte[] join(byte[] firstPart, byte lastPart) {
    Contract.isNotNull(firstPart);
    byte[] joined = new byte[firstPart.length + 1];
    System.arraycopy(firstPart, 0, joined, 0, firstPart.length);
    joined[joined.length - 1] = lastPart;
    return joined;
  }

  /**
   *  Create an a new array that contains both parts.
   *
   *  @param parts The individual arrays to be joined. No part must be null.
   *
   *  @return An new array that contains both parts in succession.
   */
  public static byte[] join(byte[]... parts) {
    Contract.isNotNull(parts);
    int totalLength = 0;
    for (int i = 0; i < parts.length; i++) {
      Contract.isNotNull(parts[i]);
      totalLength += parts[i].length;
    }
    byte[] joined = new byte[totalLength];
    int copyIdx = 0;
    for (byte[] part : parts) {
      System.arraycopy(part, 0, joined, copyIdx, part.length);
      copyIdx += part.length;
    }
    return joined;
  }

  /**
   *  Create an a new array that contains both parts.
   *
   *  @param firstPart An existing array to be extended with a single element.
   *  @param lastPart Extra element to be added.
   *
   *  @return An new array that contains both parts in succession.
   */
  public static char[] join(char[] firstPart, char lastPart) {
    Contract.isNotNull(firstPart);
    char[] joined = new char[firstPart.length + 1];
    System.arraycopy(firstPart, 0, joined, 0, firstPart.length);
    joined[joined.length - 1] = lastPart;
    return joined;
  }

  /**
   *  Create an a new array that contains both parts.
   *
   *  @param parts The individual arrays to be joined. No part must be null.
   *
   *  @return An new array that contains both parts in succession.
   */
  public static char[] join(char[]... parts) {
    Contract.isNotNull(parts);
    int totalLength = 0;
    for (int i = 0; i < parts.length; i++) {
      Contract.isNotNull(parts[i]);
      totalLength += parts[i].length;
    }
    char[] joined = new char[totalLength];
    int copyIdx = 0;
    for (char[] part : parts) {
      System.arraycopy(part, 0, joined, copyIdx, part.length);
      copyIdx += part.length;
    }
    return joined;
  }

  /**
   *  Create an a new array that contains both parts.
   *
   *  @param firstPart An existing array to be extended with a single element.
   *  @param lastPart Extra element to be added.
   *
   *  @return An new array that contains both parts in succession.
   */
  public static int[] join(int[] firstPart, int lastPart) {
    Contract.isNotNull(firstPart);
    int[] joined = new int[firstPart.length + 1];
    System.arraycopy(firstPart, 0, joined, 0, firstPart.length);
    joined[joined.length - 1] = lastPart;
    return joined;
  }

  /**
   *  Create an a new array that contains both parts.
   *
   *  @param parts The individual arrays to be joined. No part must be null.
   *
   *  @return An new array that contains both parts in succession.
   */
  public static int[] join(int[]... parts) {
    Contract.isNotNull(parts);
    int totalLength = 0;
    for (int i = 0; i < parts.length; i++) {
      Contract.isNotNull(parts[i]);
      totalLength += parts[i].length;
    }
    int[] joined = new int[totalLength];
    int copyIdx = 0;
    for (int[] part : parts) {
      System.arraycopy(part, 0, joined, copyIdx, part.length);
      copyIdx += part.length;
    }
    return joined;
  }

  /**
   *  Create an a new array that contains both parts.
   *
   *  @param firstPart An existing array to be extended with a single element.
   *  @param lastPart Extra element to be added.
   *
   *  @return An new array that contains both parts in succession.
   */
  public static long[] join(long[] firstPart, long lastPart) {
    Contract.isNotNull(firstPart);
    long[] joined = new long[firstPart.length + 1];
    System.arraycopy(firstPart, 0, joined, 0, firstPart.length);
    joined[joined.length - 1] = lastPart;
    return joined;
  }

  /**
   *  Create an a new array that contains both parts.
   *
   *  @param parts The individual arrays to be joined. No part must be null.
   *
   *  @return An new array that contains both parts in succession.
   */
  public static long[] join(long[]... parts) {
    Contract.isNotNull(parts);
    int totalLength = 0;
    for (int i = 0; i < parts.length; i++) {
      Contract.isNotNull(parts[i]);
      totalLength += parts[i].length;
    }
    long[] joined = new long[totalLength];
    int copyIdx = 0;
    for (long[] part : parts) {
      System.arraycopy(part, 0, joined, copyIdx, part.length);
      copyIdx += part.length;
    }
    return joined;
  }

  /**
   *  Create an a new array that contains both parts.
   *
   *  @param firstPart An existing array to be extended with a single element.
   *  @param lastPart Extra element to be added.
   *
   *  @return An new array that contains both parts in succession.
   */
  public static double[] join(double[] firstPart, double lastPart) {
    Contract.isNotNull(firstPart);
    double[] joined = new double[firstPart.length + 1];
    System.arraycopy(firstPart, 0, joined, 0, firstPart.length);
    joined[joined.length - 1] = lastPart;
    return joined;
  }

  /**
   *  Create an a new array that contains both parts.
   *
   *  @param parts The individual arrays to be joined. No part must be null.
   *
   *  @return An new array that contains both parts in succession.
   */
  public static double[] join(double[]... parts) {
    Contract.isNotNull(parts);
    int totalLength = 0;
    for (int i = 0; i < parts.length; i++) {
      Contract.isNotNull(parts[i]);
      totalLength += parts[i].length;
    }
    double[] joined = new double[totalLength];
    int copyIdx = 0;
    for (double[] part : parts) {
      System.arraycopy(part, 0, joined, copyIdx, part.length);
      copyIdx += part.length;
    }
    return joined;
  }

  /**
   *  Create an a new array that contains both parts.
   *
   *  @param firstPart An existing array to be extended with a single element.
   *  @param lastPart Extra element to be added.
   *
   *  @return An new array that contains both parts in succession.
   */
  public static <T> T[] join(T[] firstPart, T lastPart) {
    Contract.isNotNull(firstPart);
    @SuppressWarnings("unchecked")
    T[] joined = (T[])Array.newInstance(firstPart.getClass().getComponentType(), firstPart.length + 1);
    System.arraycopy(firstPart, 0, joined, 0, firstPart.length);
    joined[joined.length - 1] = lastPart;
    return joined;
  }

  /**
   *  Create an a new array that contains both parts.
   *
   *  @param parts The individual arrays to be joined. No part must be null.
   *
   *  @param <T> Type of elements in the source/target arrays.
   *
   *  @return An new array that contains both parts in succession.
   */
  @SafeVarargs
  public static <T> T[] join(T[]... parts) {
    Contract.isNotNull(parts);
    int totalLength = 0;
    for (int i = 0; i < parts.length; i++) {
      Contract.isNotNull(parts[i]);
      totalLength += parts[i].length;
    }
    @SuppressWarnings("unchecked")
    T[] joined = (T[])Array.newInstance(parts.getClass().getComponentType().getComponentType(), totalLength);
    int copyIdx = 0;
    for (T[] part : parts) {
      System.arraycopy(part, 0, joined, copyIdx, part.length);
      copyIdx += part.length;
    }
    return joined;
  }

  /**
   *  Create an Iterable that represents a slice of an underlying array.
   *
   *  @param source The source array whose elements will be iterated over.
   *  @param startIndex The index of the first element to return.
   *
   *  @param <T> Type of elements in the source array.
   *
   *  @return An {@link Iterable} representing the desired array slice.
   */
  public static <T> Iterable<T> slice(T[] source, int startIndex) {
    final T[] sourceArray = source;
    final int sourceStartIndex = startIndex;
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return new ArraySliceIterator<>(sourceArray, sourceStartIndex);
      }
    };
  }

  /**
   *  Create an Iterable that represents a slice of an underlying array.
   *
   *  @param source The source array whose elements will be iterated over.
   *  @param startIndex The index of the first element to return.
   *  @param count The number of elements to return, so the last element returned will be at index startOffset + count - 1.
   *
   *  @param <T> Type of elements in the source array.
   *
   *  @return An {@link Iterable} representing the desired array slice.
   */
  public static <T> Iterable<T> slice(T[] source, int startIndex, int count) {
    final T[] sourceArray = source;
    final int sourceStartIndex = startIndex;
    final int sourceCount = count;
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return new ArraySliceIterator<>(sourceArray, sourceStartIndex, sourceCount);
      }
    };
  }

  /**
   *  Create an iterator that iterates over an underlying array.
   *
   *  @param source The source array whose elements will be iterated over.
   *
   *  @param <T> Type of elements in the source array.
   *
   *  @return An {@link Iterator} over the desired array.
   */
  public static <T> Iterator<T> arrayIterator(T[] source) {
    return new ArraySliceIterator<>(source, 0);
  }

  /**
   *  Create an iterator that iterates over a slice of an underlying array.
   *
   *  @param source The source array whose elements will be iterated over.
   *  @param startIndex The index of the first element to return.
   *
   *  @param <T> Type of elements in the source array.
   *
   *  @return An {@link Iterator} over the desired array slice.
   */
  public static <T> Iterator<T> sliceIterator(T[] source, int startIndex) {
    return new ArraySliceIterator<>(source, startIndex);
  }

  /**
   *  Create an iterator that iterates over a slice of an underlying array.
   *
   *  @param source The source array whose elements will be iterated over.
   *  @param startIndex The index of the first element to return.
   *  @param count The number of elements to return, so the last element returned will be at index startOffset + count - 1.
   *
   *  @param <T> Type of elements in the source array.
   *
   *  @return An {@link Iterator} over the desired array slice.
   */
  public static <T> Iterator<T> sliceIterator(T[] source, int startIndex, int count) {
    return new ArraySliceIterator<>(source, startIndex, count);
  }

  /**
   *  Create a Properties object with the same keys and values as the provided map.
   *
   *  @param map Map whose entries to use for initializing a Properties object.
   *  @return A newly created Properties object.
   */
  public static Properties mapToProperties(Map<String, String> map) {
    Contract.isNotNull(map);
    Properties props = new Properties();
    for (String key : map.keySet()) {
      props.setProperty(key, map.get(key));
    }
    return props;
  }

  /**
   *  Create a Map&lt;String, String&gt; object with the same keys and values as the provided properties.
   *
   *  @param props Properties object whose entries to use for initializing a Map&lt;String, String&gt; object.
   *  @return A newly created Map&lt;String, String&gt; object.
   *
   *  @throws ClassCastException If any property in props has a non-String key or value.
   */
  public static Map<String, String> propertiesToMap(Properties props) {
    Contract.isNotNull(props);
    HashMap<String, String> map = new HashMap<>(props.size());
    for (String key : props.stringPropertyNames()) {
      map.put(key, props.getProperty(key));
    }
    return map;
  }

  /**
   * Convert from Iterator to List.
   *
   * @param values Array of char values
   * @return List of Character values.
   */
  public static <T> List<T> listFromIterator(Iterator<T> values) {
    List<T> valList = new ArrayList<>();
    values.forEachRemaining(valList::add);
    return valList;
  }

  /**
   * Convert from List&lt;Byte&gt; to byte[].
   *
   * @param values List of Byte values
   * @return Array of byte values.
   */
  public static byte[] arrayFromByteList(List<Byte> values) {
    byte[] byteArray = new byte[values.size()];
    for (int i = 0; i < byteArray.length; i++) {
      byteArray[i] = values.get(i);
    }
    return byteArray;
  }

  /**
   * Convert from array to List.
   *
   * @param values Array of byte values
   * @return List of Byte values.
   */
  public static List<Byte> listFromByteArray(byte[] values) {
    List<Byte> byteList = new ArrayList<>(values.length);
    for (byte v : values) {
      byteList.add(Byte.valueOf(v));
    }
    return byteList;
  }

  /**
   * Convert from List&lt;Character&gt; to char[].
   *
   * @param values List of Character values
   * @return Array of char values.
   */
  public static char[] arrayFromCharacterList(List<Character> values) {
    char[] charArray = new char[values.size()];
    for (int i = 0; i < charArray.length; i++) {
      charArray[i] = values.get(i);
    }
    return charArray;
  }

  /**
   * Convert from array to List.
   *
   * @param values Array of char values
   * @return List of Character values.
   */
  public static List<Character> listFromCharArray(char[] values) {
    List<Character> charList = new ArrayList<>(values.length);
    for (char v : values) {
      charList.add(Character.valueOf(v));
    }
    return charList;
  }

  /**
   * Convert from List&lt;Double&gt; to double[].
   *
   * @param values List of Double values
   * @return Array of double values.
   */
  public static double[] arrayFromDoubleList(List<Double> values) {
    double[] doubleArray = new double[values.size()];
    for (int i = 0; i < doubleArray.length; i++) {
      doubleArray[i] = values.get(i);
    }
    return doubleArray;
  }

  /**
   * Convert from array to List.
   *
   * @param values Array of long values
   * @return List of Double values.
   */
  public static List<Double> listFromDoubleArray(double[] values) {
    List<Double> doubleList = new ArrayList<>(values.length);
    for (double v : values) {
      doubleList.add(v);
    }
    return doubleList;
  }

  /**
   * Convert from List&lt;Long&gt; to long[].
   *
   * @param values List of Long values
   * @return Array of long values.
   */
  public static long[] arrayFromLongList(List<Long> values) {
    long[] longArray = new long[values.size()];
    for (int i = 0; i < longArray.length; i++) {
      longArray[i] = values.get(i);
    }
    return longArray;
  }

  /**
   * Convert from array to List.
   *
   * @param values Array of long values
   * @return List of Long values.
   */
  public static List<Long> listFromLongArray(long[] values) {
    List<Long> longList = new ArrayList<>(values.length);
    for (long v : values) {
      longList.add(v);
    }
    return longList;
  }

  /**
   * Convert from List&lt;Integer&gt; to int[].
   *
   * @param values List of Integer values
   * @return Array of int values.
   */
  public static int[] arrayFromIntegerList(List<Integer> values) {
    int[] intArray = new int[values.size()];
    for (int i = 0; i < intArray.length; i++) {
      intArray[i] = values.get(i);
    }
    return intArray;
  }

  /**
   * Convert from array to List.
   *
   * @param values Array of int values
   * @return List of Integer values.
   */
  public static List<Integer> listFromIntArray(int[] values) {
    List<Integer> intList = new ArrayList<>(values.length);
    for (int v : values) {
      intList.add(Integer.valueOf(v));
    }
    return intList;
  }

  /**
   * Convert a string representation of an integer set to a sorted array of integers. E.g. 1, 13, -5 =&gt; [-5, 1, 13] E.g. 1-10, -5 -
   * -2, 99 =&gt; [-5, -4, -6, -2, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 99]
   *
   * @param value A string representation of a set of integers which can be formatted as a comma-separate list of integers or
   *          integer-ranges. A sorted integer range consists of two integers separated by a dash. Whitespace can be used freely in
   *          between all tokens.
   *
   * @return An array of unique integer values indicated by value. Note that overlapping or double-specified integers will only be
   *         present once.
   *
   * @throws IllegalArgumentException If the value cannot be complete interpreted as a set of integers either because it contains any
   *              other chars than [0-9-,] or because ranges are not sorted from smallest to biggest number or because numbers are
   *              not properly separated by commas or dashes.
   */
  public static int[] integersFromString(String value) {
    Contract.isNotNull(value);
    SortedSet<Integer> intVals = new TreeSet<>();
    // Split on comma first (e.g. 1,3, 4-5, 7 -> [1] [3] [4-5] [7])
    for (String valSpec : value.trim().split("\\s*,\\s*")) {
      if (valSpec.indexOf('-', 1) > 0) { // We have a range
        int splitIdx = valSpec.indexOf('-', 1);
        String fromInt = valSpec.substring(0, splitIdx).trim();
        String toInt = valSpec.substring(splitIdx + 1).trim();
        try {
          int from = Integer.parseInt(fromInt);
          int to = Integer.parseInt(toInt);
          if (from > to) {
            throw new IllegalArgumentException("The range '" + valSpec + "' is invalid");
          }
          for (int i = from; i <= to; i++) {
            intVals.add(i);
          }
        }
        catch (NumberFormatException e) {
          throw new IllegalArgumentException("The range '" + valSpec + "' contains an invalid number");
        }
      }
      else {
        try {
          intVals.add(Integer.parseInt(valSpec));
        }
        catch (NumberFormatException e) {
          throw new IllegalArgumentException("The value '" + valSpec + "' is not a valid number");
        }
      }
    }
    if (intVals.isEmpty()) {
      throw new IllegalArgumentException("No integers found");
    }
    int[] intArray = new int[intVals.size()];
    int i = 0;
    for (int intVal : intVals) {
      intArray[i++] = intVal;
    }
    return intArray;
  }

  /**
   * Get a string representation for the specified boolean array.
   *
   * @param array Array to be presented in String form.
   *
   * @return String representation of array. Has following format: [ &lt;item-1&gt;, &lt;item-2&gt;, ..., &lt;item-n&gt; ].
   *
   * @throws NullPointerException When collection is null.
   */
  public static String toString(boolean[] array) {
    return toString(array, "[ ", ", ", " ]");
  }

  /**
   * Get a string representation for the specified array.
   *
   * @param array Array to be presented in String form.
   * @param prefix Prefix of result string
   * @param elemSep Separator used between elements
   * @param suffix Suffix of result string
   *
   * @return String representation of array. Has following format:
   *         &lt;prefix&gt;&lt;item-1&gt;&lt;elem-sep&gt;&lt;item-2&gt;&lt;elem-sep&gt;...&lt;elem-sep&gt;&lt;item-n&gt;&lt;suffix&gt;.
   *
   * @throws NullPointerException When collection is null.
   */
  public static String toString(boolean[] array, String prefix, String elemSep, String suffix) {
    Contract.isNotNull(array);
    StringBuilder str = new StringBuilder(prefix);
    for (int i = 0; i < array.length; i++) {
      str.append(array[i]);
      if (i < (array.length - 1)) {
        str.append(elemSep);
      }
    }
    str.append(suffix);
    return str.toString();
  }

  /**
   * Get a string representation for the specified byte array.
   *
   * @param array Array to be presented in String form.
   *
   * @return String representation of array. Has following format: [ &lt;item-1&gt;, &lt;item-2&gt;, ..., &lt;item-n&gt; ].
   *
   * @throws NullPointerException When collection is null.
   */
  public static String toString(byte[] array) {
    return toString(array, "[ ", ", ", " ]");
  }

  /**
   * Get a string representation for the specified array.
   *
   * @param array Array to be presented in String form.
   * @param prefix Prefix of result string
   * @param elemSep Separator used between elements
   * @param suffix Suffix of result string
   *
   * @return String representation of array. Has following format:
   *         &lt;prefix&gt;&lt;item-1&gt;&lt;elem-sep&gt;&lt;item-2&gt;&lt;elem-sep&gt;...&lt;elem-sep&gt;&lt;item-n&gt;&lt;suffix&gt;.
   *
   * @throws NullPointerException When collection is null.
   */
  public static String toString(byte[] array, String prefix, String elemSep, String suffix) {
    Contract.isNotNull(array);
    StringBuilder str = new StringBuilder(prefix);
    for (int i = 0; i < array.length; i++) {
      str.append(array[i]);
      if (i < (array.length - 1)) {
        str.append(elemSep);
      }
    }
    str.append(suffix);
    return str.toString();
  }

  /**
   * Get a string representation for the specified char array.
   *
   * @param array Array to be presented in String form.
   *
   * @return String representation of array. Has following format: [ &lt;item-1&gt;, &lt;item-2&gt;, ..., &lt;item-n&gt; ].
   *
   * @throws NullPointerException When collection is null.
   */
  public static String toString(char[] array) {
    return toString(array, "[ ", ", ", " ]");
  }

  /**
   * Get a string representation for the specified array.
   *
   * @param array Array to be presented in String form.
   * @param prefix Prefix of result string
   * @param elemSep Separator used between elements
   * @param suffix Suffix of result string
   *
   * @return String representation of array. Has following format:
   *         &lt;prefix&gt;&lt;item-1&gt;&lt;elem-sep&gt;&lt;item-2&gt;&lt;elem-sep&gt;...&lt;elem-sep&gt;&lt;item-n&gt;&lt;suffix&gt;.
   *
   * @throws NullPointerException When collection is null.
   */
  public static String toString(char[] array, String prefix, String elemSep, String suffix) {
    Contract.isNotNull(array);
    StringBuilder str = new StringBuilder(prefix);
    for (int i = 0; i < array.length; i++) {
      str.append(array[i]);
      if (i < (array.length - 1)) {
        str.append(elemSep);
      }
    }
    str.append(suffix);
    return str.toString();
  }

  /**
   * Get a string representation for the specified double array.
   *
   * @param array Array to be presented in String form.
   *
   * @return String representation of array. Has following format: [ &lt;item-1&gt;, &lt;item-2&gt;, ..., &lt;item-n&gt; ].
   *
   * @throws NullPointerException When collection is null.
   */
  public static String toString(float[] array) {
    return toString(array, "[ ", ", ", " ]");
  }

  /**
   * Get a string representation for the specified array.
   *
   * @param array Array to be presented in String form.
   * @param prefix Prefix of result string
   * @param elemSep Separator used between elements
   * @param suffix Suffix of result string
   *
   * @return String representation of array. Has following format:
   *         &lt;prefix&gt;&lt;item-1&gt;&lt;elem-sep&gt;&lt;item-2&gt;&lt;elem-sep&gt;...&lt;elem-sep&gt;&lt;item-n&gt;&lt;suffix&gt;.
   *
   * @throws NullPointerException When collection is null.
   */
  public static String toString(float[] array, String prefix, String elemSep, String suffix) {
    Contract.isNotNull(array);
    StringBuilder str = new StringBuilder(prefix);
    for (int i = 0; i < array.length; i++) {
      str.append(array[i]);
      if (i < (array.length - 1)) {
        str.append(elemSep);
      }
    }
    str.append(suffix);
    return str.toString();
  }

  /**
   * Get a string representation for the specified double array.
   *
   * @param array Array to be presented in String form.
   *
   * @return String representation of array. Has following format: [ &lt;item-1&gt;, &lt;item-2&gt;, ..., &lt;item-n&gt; ].
   *
   * @throws NullPointerException When collection is null.
   */
  public static String toString(double[] array) {
    return toString(array, "[ ", ", ", " ]");
  }

  /**
   * Get a string representation for the specified array.
   *
   * @param array Array to be presented in String form.
   * @param prefix Prefix of result string
   * @param elemSep Separator used between elements
   * @param suffix Suffix of result string
   *
   * @return String representation of array. Has following format:
   *         &lt;prefix&gt;&lt;item-1&gt;&lt;elem-sep&gt;&lt;item-2&gt;&lt;elem-sep&gt;...&lt;elem-sep&gt;&lt;item-n&gt;&lt;suffix&gt;.
   *
   * @throws NullPointerException When collection is null.
   */
  public static String toString(double[] array, String prefix, String elemSep, String suffix) {
    Contract.isNotNull(array);
    StringBuilder str = new StringBuilder(prefix);
    for (int i = 0; i < array.length; i++) {
      str.append(array[i]);
      if (i < (array.length - 1)) {
        str.append(elemSep);
      }
    }
    str.append(suffix);
    return str.toString();
  }

  /**
   * Get a string representation for the specified long array.
   *
   * @param array Array to be presented in String form.
   *
   * @return String representation of array. Has following format: [ &lt;item-1&gt;, &lt;item-2&gt;, ..., &lt;item-n&gt; ].
   *
   * @throws NullPointerException When collection is null.
   */
  public static String toString(long[] array) {
    return toString(array, "[ ", ", ", " ]");
  }

  /**
   * Get a string representation for the specified array.
   *
   * @param array Array to be presented in String form.
   * @param prefix Prefix of result string
   * @param elemSep Separator used between elements
   * @param suffix Suffix of result string
   *
   * @return String representation of array. Has following format:
   *         &lt;prefix&gt;&lt;item-1&gt;&lt;elem-sep&gt;&lt;item-2&gt;&lt;elem-sep&gt;...&lt;elem-sep&gt;&lt;item-n&gt;&lt;suffix&gt;.
   *
   * @throws NullPointerException When collection is null.
   */
  public static String toString(long[] array, String prefix, String elemSep, String suffix) {
    Contract.isNotNull(array);
    StringBuilder str = new StringBuilder(prefix);
    for (int i = 0; i < array.length; i++) {
      str.append(array[i]);
      if (i < (array.length - 1)) {
        str.append(elemSep);
      }
    }
    str.append(suffix);
    return str.toString();
  }

  /**
   * Get a string representation for the specified int array.
   *
   * @param array Array to be presented in String form.
   *
   * @return String representation of array. Has following format: [ &lt;item-1&gt;, &lt;item-2&gt;, ..., &lt;item-n&gt; ].
   *
   * @throws NullPointerException When collection is null.
   */
  public static String toString(int[] array) {
    return toString(array, "[ ", ", ", " ]");
  }

  /**
   * Get a string representation for the specified array.
   *
   * @param array Array to be presented in String form.
   * @param prefix Prefix of result string
   * @param elemSep Separator used between elements
   * @param suffix Suffix of result string
   *
   * @return String representation of array. Has following format:
   *         &lt;prefix&gt;&lt;item-1&gt;&lt;elem-sep&gt;&lt;item-2&gt;&lt;elem-sep&gt;...&lt;elem-sep&gt;&lt;item-n&gt;&lt;suffix&gt;.
   *
   * @throws NullPointerException When collection is null.
   */
  public static String toString(int[] array, String prefix, String elemSep, String suffix) {
    Contract.isNotNull(array);
    StringBuilder str = new StringBuilder(prefix);
    for (int i = 0; i < array.length; i++) {
      str.append(array[i]);
      if (i < (array.length - 1)) {
        str.append(elemSep);
      }
    }
    str.append(suffix);
    return str.toString();
  }

  /**
   * Get a string representation for the specified array.
   *
   * @param <T> The type of the array elements.
   * @param array Array to be presented in String form.
   *
   * @return String representation of array. Has following format: [ &lt;item-1&gt;, &lt;item-2&gt;, ..., &lt;item-n&gt; ].
   *
   * @throws NullPointerException When collection is null.
   */
  public static <T> String toString(T[] array) {
    return toString(array, "[ ", ", ", " ]");
  }

  /**
   * Get a string representation for the specified array.
   *
   * @param <T> The type of the array elements.
   * @param array Array to be presented in String form.
   * @param prefix Prefix of result string
   * @param elemSep Separator used between elements
   * @param suffix Suffix of result string
   *
   * @return String representation of array. Has following format:
   *         &lt;prefix&gt;&lt;item-1&gt;&lt;elem-sep&gt;&lt;item-2&gt;&lt;elem-sep&gt;...&lt;elem-sep&gt;&lt;item-n&gt;&lt;suffix&gt;.
   *
   * @throws NullPointerException When collection is null.
   */
  public static <T> String toString(T[] array, String prefix, String elemSep, String suffix) {
    Contract.isNotNull(array);
    StringBuilder str = new StringBuilder(prefix);
    for (int i = 0; i < array.length; i++) {
      T element = array[i];
      if (element instanceof Map) {
        str.append(toString((Map<?, ?>) element, prefix, elemSep, suffix, " => "));
      }
      else if (element instanceof Set) {
        str.append(toString((Set<?>) element, prefix, elemSep, suffix));
      }
      else if (element instanceof Collection) {
        str.append(toString((Collection<?>) element, prefix, elemSep, suffix));
      }
      else {
        str.append(element);
      }
      if (i < (array.length - 1)) {
        str.append(elemSep);
      }
    }
    str.append(suffix);
    return str.toString();
  }

  /**
   * Get a string representation for the specified array slice.
   *
   * @param <T> The type of the array elements.
   * @param array Array to be presented in String form.
   * @param startIndex The index of the first element to represent. Inclusive lower bound.
   * @param endIndex The index just after the last element to represent. Exclusive upper bound.
   *
   * @return String representation of an array slice. Has following format: [ &lt;item-1&gt;, &lt;item-2&gt;, ..., &lt;item-n&gt; ].
   *
   * @throws NullPointerException When collection is null.
   */
  public static <T> String toString(T[] array, int startIndex, int endIndex) {
    return toString(array, startIndex, endIndex, "[ ", ", ", " ]");
  }

  /**
   * Get a string representation for the specified array slice.
   *
   * @param <T> The type of the array elements.
   * @param array Array to be presented in String form.
   * @param startIndex The index of the first element to represent. Inclusive lower bound.
   * @param endIndex The index just after the last element to represent. Exclusive upper bound.
   * @param prefix Prefix of result string
   * @param elemSep Separator used between elements
   * @param suffix Suffix of result string
   *
   * @return String representation of an array slice. Has following format:
   *         &lt;prefix&gt;&lt;item-1&gt;&lt;elem-sep&gt;&lt;item-2&gt;&lt;elem-sep&gt;...&lt;elem-sep&gt;&lt;item-n&gt;&lt;suffix&gt;.
   *
   * @throws NullPointerException When collection is null.
   */
  public static <T> String toString(T[] array, int startIndex, int endIndex, String prefix, String elemSep, String suffix) {
    Contract.isNotNull("array", array);
    Contract.isNotNegative("startIndex", startIndex);
    Contract.isInRange("endIndex", endIndex, startIndex, array.length);
    StringBuilder str = new StringBuilder(prefix);
    for (int i = startIndex; i < endIndex; i++) {
      T element = array[i];
      if (element instanceof Map) {
        str.append(toString((Map<?, ?>) element, prefix, elemSep, suffix, " => "));
      }
      else if (element instanceof Set) {
        str.append(toString((Set<?>) element, prefix, elemSep, suffix));
      }
      else if (element instanceof Collection) {
        str.append(toString((Collection<?>) element, prefix, elemSep, suffix));
      }
      else {
        str.append(element);
      }
      if (i < (array.length - 1)) {
        str.append(elemSep);
      }
    }
    str.append(suffix);
    return str.toString();
  }

  /**
   * Get a string representation for the specified collection.
   *
   * @param collection Collection to be presented in String form.
   *
   * @return String representation of collection. Has following format: ( &lt;item-1&gt;, &lt;item-2&gt;, ..., &lt;item-n&gt; ).
   *
   * @throws NullPointerException When collection is null.
   */
  public static String toString(Collection<?> collection) {
    return toString(collection, "( ", ", ", " )");
  }

  /**
   * Get a string representation for the specified collection.
   *
   * @param collection Collection to be presented in String form.
   * @param prefix Prefix of result string
   * @param elemSep Separator used between elements
   * @param suffix Suffix of result string
   *
   * @return String representation of collection. Has following format:
   *         &lt;prefix&gt;&lt;item-1&gt;&lt;elem-sep&gt;&lt;item-2&gt;&lt;elem-sep&gt;...&lt;elem-sep&gt;&lt;item-n&gt;&lt;suffix&gt;.
   *
   * @throws NullPointerException When collection is null.
   */
  @SuppressWarnings("rawtypes")
  public static String toString(Collection collection, String prefix, String elemSep, String suffix) {
    Contract.isNotNull(collection);
    StringBuilder str = new StringBuilder(prefix);
    for (Iterator it = collection.iterator(); it.hasNext();) {
      Object element = it.next();
      if (element instanceof Map) {
        str.append(toString((Map) element, prefix, elemSep, suffix, " => "));
      }
      else if (element instanceof Set) {
        str.append(toString((Set<?>) element, prefix, elemSep, suffix));
      }
      else if (element instanceof Collection) {
        str.append(toString((Collection) element, prefix, elemSep, suffix));
      }
      else {
        str.append(element);
      }
      if (it.hasNext()) {
        str.append(elemSep);
      }
    }
    str.append(suffix);
    return str.toString();
  }

  /**
   * Get a string representation for the specified set.
   *
   * @param set Set to be presented in String form.
   *
   * @return String representation of set. Has following format: { &lt;item-1&gt;, &lt;item-2&gt;, ..., &lt;item-n&gt; }.
   *
   * @throws NullPointerException When set is null.
   */
  public static String toString(Set<?> set) {
    return toString(set, "{ ", ", ", " }");
  }

  /**
   * Get a string representation for the specified set.
   *
   * @param set Set to be presented in String form.
   * @param prefix Prefix of result string
   * @param elemSep Separator used between elements
   * @param suffix Suffix of result string
   *
   * @return String representation of set. Has following format: &lt;prefix&gt;&lt;item-1&gt;&lt;elem-sep&gt;&lt;item-2&gt;&lt;elem-sep&gt;
   *         ...&lt;elem-sep&gt;&lt;item-n&gt;&lt;suffix&gt;.
   *
   * @throws NullPointerException When set is null.
   */
  public static String toString(Set<?> set, String prefix, String elemSep, String suffix) {
    Contract.isNotNull(set);
    StringBuilder str = new StringBuilder(prefix);
    for (Iterator<?> it = set.iterator(); it.hasNext();) {
      Object element = it.next();
      if (element instanceof Map) {
        str.append(toString((Map<?, ?>) element, prefix, elemSep, suffix, " => "));
      }
      else if (element instanceof Set) {
        str.append(toString((Set<?>) element, prefix, elemSep, suffix));
      }
      else if (element instanceof Collection) {
        str.append(toString((Collection<?>) element, prefix, elemSep, suffix));
      }
      else {
        str.append(element);
      }
      if (it.hasNext()) {
        str.append(elemSep);
      }
    }
    str.append(suffix);
    return str.toString();
  }

  /**
   * Get a string representation for the specified map.
   *
   * @param map Map to be presented in String form.
   *
   * @return String representation of map. Has following format: [ &lt;key1&gt; =&gt; &lt;value-1&gt;, &lt;key-2&gt; =&gt; &lt;value-2&gt;, ..., &lt;key-n&gt; =&gt;
   *         &lt;value-n&gt; ].
   *
   * @throws NullPointerException When map is null.
   */
  public static String toString(Map<?, ?> map) {
    return toString(map, "[ ", ", ", " ]", " => ");
  }

  /**
   * Get a string representation for the specified map.
   *
   * @param map Map to be presented in String form.
   * @param prefix Prefix of result string
   * @param elemSep Separator used between elements
   * @param suffix Suffix of result string
   * @param keyValSep Separator between keys and values
   *
   * @return String representation of map. Has following format:
   *         &lt;prefix&gt;&lt;key1&gt;&lt;key-val-sep&gt;&lt;value-1&gt;&lt;sep&gt;&lt;key-2&gt;&lt;key-val-sep&gt;&lt;value-2&gt;&lt;sep&gt;
   *         ...&lt;sep&gt;&lt;key-n&gt;&lt;key-val-sep&gt;&lt;value-n&gt;&lt;suffix&gt;.
   *
   * @throws NullPointerException When map is null.
   */
  public static String toString(Map<?, ?> map, String prefix, String elemSep, String suffix, String keyValSep) {
    Contract.isNotNull(map);
    StringBuilder str = new StringBuilder(prefix);
    for (Iterator<?> it = map.keySet().iterator(); it.hasNext();) {
      Object key = it.next();
      str.append(key + keyValSep);
      Object element = map.get(key);
      if (element instanceof Map) {
        str.append(toString((Map<?, ?>) element, prefix, elemSep, suffix, keyValSep));
      }
      else if (element instanceof Set) {
        str.append(toString((Set<?>) element, prefix, elemSep, suffix));
      }
      else if (element instanceof Collection) {
        str.append(toString((Collection<?>) element, prefix, elemSep, suffix));
      }
      else {
        str.append(element);
      }
      if (it.hasNext()) {
        str.append(elemSep);
      }
    }
    str.append(suffix);
    return str.toString();
  }

  // </editor-fold>
}
