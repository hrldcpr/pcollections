/*
 * Copyright (c) 2008, 2022 Harold Cooper and Ran Ari-Gur. All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

package org.pcollections;

import java.util.Comparator;

/* Mike Klein, 2/27/2009 */

/* Empty remembers which classes implement the interface you want,
 * so you don't have to.
 */

/**
 * A static utility class for getting empty PCollections backed by the 'default' implementations.
 *
 * @author mtklein
 */
public final class Empty {
  // non-instantiable:
  private Empty() {}

  public static <E> PStack<E> stack() {
    return ConsPStack.empty();
  }

  public static <E> PQueue<E> queue() {
    return AmortizedPQueue.empty();
  }

  public static <E> PVector<E> vector() {
    return TreePVector.empty();
  }

  public static <E> PSet<E> set() {
    return HashTreePSet.empty();
  }

  public static <E> POrderedSet<E> orderedSet() {
    return OrderedPSet.empty();
  }

  public static <E> PBag<E> bag() {
    return HashTreePBag.empty();
  }

  public static <K, V> PMap<K, V> map() {
    return HashTreePMap.empty();
  }

  public static <E extends Comparable<? super E>> PSortedSet<E> sortedSet() {
    return TreePSet.empty();
  }

  public static <E> PSortedSet<E> sortedSet(final Comparator<? super E> comparator) {
    return TreePSet.empty(comparator);
  }

  public static <K extends Comparable<? super K>, V> PSortedMap<K, V> sortedMap() {
    return TreePMap.empty();
  }

  public static <K, V> PSortedMap<K, V> sortedMap(final Comparator<? super K> comparator) {
    return TreePMap.empty(comparator);
  }
}
