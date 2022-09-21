/*
 * Copyright (c) 2008 Harold Cooper. All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

package org.pcollections;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Like {@link PMap} but preserves insertion order. Persistent equivalent of {@link LinkedHashMap}.
 *
 * <p>Note that, like {@link LinkedHashMap}, insertion order is not affected if a key is re-inserted
 * into the map.
 *
 * <p>(Note: this is different from {@link PSortedMap}, which keeps elements in the order specified
 * by {@link java.lang.Comparable#compareTo(Object)} or {@link java.util.Comparator#compare(Object,
 * Object)}.)
 *
 * @param <K>
 * @param <V>
 */
public class OrderedPMap<K, V> extends AbstractUnmodifiableMap<K, V>
    implements PMap<K, V>, Serializable {

  private static final long serialVersionUID = 1L;

  private static final OrderedPMap<Object, Object> EMPTY =
      new OrderedPMap<Object, Object>(Empty.map(), Empty.sortedMap());

  @SuppressWarnings("unchecked")
  public static <K, V> OrderedPMap<K, V> empty() {
    return (OrderedPMap<K, V>) EMPTY;
  }

  @SuppressWarnings("unchecked")
  public static <K, V> OrderedPMap<K, V> from(final Map<? extends K, ? extends V> map) {
    if (map instanceof OrderedPMap) return (OrderedPMap<K, V>) map;
    return OrderedPMap.<K, V>empty().plusAll(map);
  }

  public static <K, V> OrderedPMap<K, V> singleton(final K k, final V v) {
    return OrderedPMap.<K, V>empty().plus(k, v);
  }

  private final PMap<K, Long> ids;
  private final PSortedMap<Long, Entry<K, V>> entries;

  private OrderedPMap(final PMap<K, Long> ids, final PSortedMap<Long, Entry<K, V>> entries) {
    this.ids = ids;
    this.entries = entries;
  }

  @Override
  public V get(final Object k) {
    final Long id = ids.get(k);
    if (id == null) return null;
    return entries.get(id).getValue();
  }

  @Override
  public OrderedPMap<K, V> plus(final K k, final V v) {
    Long id = ids.get(k);
    final PMap<K, Long> newIds;
    if (id == null) {
      id = entries.isEmpty() ? Long.MIN_VALUE : (entries.lastKey() + 1);
      newIds = ids.plus(k, id);
    } else newIds = ids;
    return new OrderedPMap<>(newIds, entries.plus(id, new SimpleImmutableEntry<>(k, v)));
  }

  @Override
  public OrderedPMap<K, V> plusAll(final Map<? extends K, ? extends V> map) {
    OrderedPMap<K, V> m = this;
    for (final Entry<? extends K, ? extends V> e : map.entrySet()) {
      m = m.plus(e.getKey(), e.getValue());
    }
    return m;
  }

  @Override
  public OrderedPMap<K, V> minus(final Object k) {
    final Long id = ids.get(k);
    if (id == null) return this;
    return new OrderedPMap<K, V>(ids.minus(k), entries.minus(id));
  }

  @Override
  public OrderedPMap<K, V> minusAll(final Collection<?> keys) {
    OrderedPMap<K, V> m = this;
    for (final Object k : keys) {
      m = m.minus(k);
    }
    return m;
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return new AbstractUnmodifiableSet<Entry<K, V>>() {
      @Override
      public boolean contains(final Object o) {
        if (!(o instanceof Entry)) return false;
        final Entry e = (Entry) o;
        return Objects.equals(get(e.getKey()), e.getValue());
      }

      @Override
      public Iterator<Entry<K, V>> iterator() {
        return entries.values().iterator();
      }

      @Override
      public int size() {
        return ids.size();
      }
    };
  }

  @Override
  public int size() {
    return ids.size();
  }
}
