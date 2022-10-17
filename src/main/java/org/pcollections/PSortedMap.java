/*
 * Copyright (c) 2022 Ran Ari-Gur. All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

package org.pcollections;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NoSuchElementException;

/**
 * An immutable, persistent map from keys of type K to values of type V, with keys
 * arranged in sorted order (according to some {@link java.util.Comparator}), and with various
 * methods to obtain specific mappings or ranges of mappings based on this ordering (such as the
 * least key greater than some instance of type K, or the sub-map with keys between two instances of
 * type K).
 *
 * <p>Null values are supported. Null keys are disallowed unless the subclass specifically supports
 * them; attempts to add a null key will result in {@link NullPointerException}.
 *
 * <p>(Note: this is different from {@link OrderedPMap}, which keeps entries in the order that they
 * were added to the map.)
 *
 * <p>Every PSortedMap is a {@link java.util.Map} and more specifically a {@link PMap}, but as with
 * any sorted map, a PSortedMap will only obey the general contract of those interfaces if its
 * comparator is consistent with equals. (See {@link java.util.SortedMap} for more information.)
 *
 * <p>Every PSortedMap is a {@link java.util.SortedMap} and more specifically a {@link
 * java.util.NavigableMap}, but the implementations of PSortedMap provided by this library
 * (pcollections) depart from the specification of those interfaces in a few ways:
 *
 * <ul>
 *   <li>headMap(...), subMap(...), and tailMap(...) are specified by SortedMap and NavigableMap to
 *       return maps with a "restricted key range", and to throw IllegalArgumentException if this
 *       instance already has a restricted key range and the relevant argument is outside that
 *       range. (This ensures that map.headMap(10).headMap(15) doesn't contain elements that
 *       map.headMap(10) does not, and that map.headMap(10).headMap(15).put(12, "x") is invalid
 *       because a mapping with the key 12 can't be put in map.headMap(10).) This library's
 *       implementations do not throw IllegalArgumentException, but rather, they ensure that an
 *       argument outside the applicable range simply has no effect; so, map.headMap(10).headMap(15)
 *       is equivalent to map.headMap(10), because map.headMap(10) already contains no elements ≥
 *       15. (This is also the behavior of Guava's ImmutableSortedMap. The JDK's
 *       Collections.unmodifiableSortedMap(...) and Collections.unmodifiableNavigableMap(...) are
 *       agnostic on this point, because they just delegate to the underlying map.) Other
 *       implementations are encouraged to consider doing the same, and to document their behavior
 *       in this respect. Additionally, any implementations that <em>do</em> use the "restricted key
 *       range" concept are encouraged to document the behavior of their minus, minusAll, plus, and
 *       plusAll methods when a key is outside the restricted key range.
 *   <li>comparator() is specified by SortedMap to return "null if this map uses the natural
 *       ordering of its elements". This library's implementations never return null from that
 *       method; instead, when the map uses the natural ordering, the method returns a Comparator
 *       instance that implements the natural ordering. (This is also the behavior of Guava's ImmutableSortedMap, which also rejects
 *       null keys and values.) Other implementations of PSortedMap are encouraged to consider doing the same,
 *       and to document their behavior in this case (whether or not it's to return null).
 *   <li>pollFirstEntry() and pollLastEntry() are specified by NavigableMap to mutate this map, and
 *       are not specified to be optional operations. That's obviously not an option for a PMap, so
 *       PSortedMap provides default implementations of these methods that simply throw
 *       UnsupportedOperationException, which should be the right implementation for any
 *       implementation of this interface. (This is also the behavior of the JDK's
 *       Collections.unmodifiableNavigableMap(...) and Guava's ImmutableSortedMap.)
 * </ul>
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @author Ran Ari-Gur
 * @since 3.2.0
 * @see java.util.SortedMap
 * @see java.util.NavigableMap
 * @see java.util.Collections#unmodifiableSortedMap(java.util.SortedMap)
 * @see java.util.Collections#unmodifiableNavigableMap(java.util.NavigableMap)
 * @see org.pcollections.PMap
 * @see org.pcollections.PSortedSet
 * @see org.pcollections.TreePMap
 */
public interface PSortedMap<K, V> extends PMap<K, V>, NavigableMap<K, V> {
  // methods inherited from PMap, overridden to return specifically PSortedMap:

  @Override
  public PSortedMap<K, V> plus(K key, V value);

  @Override
  public PSortedMap<K, V> plusAll(Map<? extends K, ? extends V> map);

  @Override
  public PSortedMap<K, V> minus(Object key);

  @Override
  public PSortedMap<K, V> minusAll(Collection<?> keys);

  // methods inherited from NavigableMap, overridden to return specifically PSortedMap or
  // PSortedSet:

  @Override
  public PSortedSet<K> descendingKeySet();

  @Override
  public PSortedMap<K, V> descendingMap();

  @Override
  public PSortedMap<K, V> headMap(K toKey);

  @Override
  public PSortedMap<K, V> headMap(K toKey, boolean inclusive);

  @Override
  public PSortedSet<K> keySet();

  @Override
  public PSortedSet<K> navigableKeySet();

  @Override
  public PSortedMap<K, V> subMap(K fromKey, K toKey);

  @Override
  public PSortedMap<K, V> subMap(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive);

  @Override
  public PSortedMap<K, V> tailMap(K fromKey);

  @Override
  public PSortedMap<K, V> tailMap(K fromKey, boolean inclusive);

  // other methods:

  /**
   * @return The comparator used to order the keys in this map. (Never null.)
   */
  @Override
  public Comparator<? super K> comparator();

  /**
   * @return This map, minus its first mapping (the mapping with the least/lowest key).
   * @throws NoSuchElementException - if this map is empty
   */
  public PSortedMap<K, V> minusFirstEntry();

  /**
   * @return This map, minus its last mapping (the mapping with the greatest/highest key).
   * @throws NoSuchElementException - if this map is empty
   */
  public PSortedMap<K, V> minusLastEntry();

  /**
   * @throws UnsupportedOperationException - always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  default Map.Entry<K, V> pollFirstEntry() {
    throw new UnsupportedOperationException("This map instance is unmodifiable");
  }

  /**
   * @throws UnsupportedOperationException - always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  default Map.Entry<K, V> pollLastEntry() {
    throw new UnsupportedOperationException("This map instance is unmodifiable");
  }
}
