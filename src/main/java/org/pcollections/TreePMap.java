/*
 * Copyright (c) 2022 Ran Ari-Gur. All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

package org.pcollections;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * An implementation of {@link PSortedMap} based on a self-balancing binary search tree.
 *
 * <p>Instances of this class are obtained via any of various static factory methods and static
 * collector methods. These methods come in pairs, with one version that accepts an explicit
 * comparator to use and one version that uses the natural ordering of the elements.
 *
 * <p>All operations are guaranteed to complete within O(log n) time, except for plusAll and
 * minusAll, whose time cost is equivalent to the corresponding sequence of calls to plus or minus.
 * A complete iteration pass over entrySet() completes in O(n) time. A few operations -- namely
 * comparator, descendingKeySet, descendingMap, entrySet, isEmpty, keySet, navigableKeySet, and size
 * -- complete in O(1) time.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @author Ran Ari-Gur
 * @since 3.2.0
 */
public final class TreePMap<K, V> extends AbstractUnmodifiableMap<K, V>
    implements PSortedMap<K, V>, Serializable {
  private static final long serialVersionUID = 1L;

  private final KVTree<K, V> tree;
  private final Comparator<? super K> ltrComparator;
  private final boolean isLeftToRight;

  private TreePMap(
      final KVTree<K, V> tree,
      final Comparator<? super K> ltrComparator,
      final boolean isLeftToRight) {

    this.tree = requireNonNull(tree, "tree is null");
    this.ltrComparator = requireNonNull(ltrComparator, "comparator is null");
    this.isLeftToRight = isLeftToRight;
  }

  /**
   * Returns an empty TreePMap using the natural ordering.
   *
   * @param <K> the type of keys to be maintained by the map
   * @param <V> the type of mapped values
   * @return an empty TreePMap using the natural ordering
   */
  public static <K extends Comparable<? super K>, V> TreePMap<K, V> empty() {
    return empty(Comparator.naturalOrder());
  }

  /**
   * Returns an empty TreePMap using the specified comparator.
   *
   * @param <K> the type of keys to be maintained by the map
   * @param <V> the type of mapped values
   * @param comparator the comparator according to which keys should be ordered
   * @return an empty TreePMap using the specified comparator
   * @throws NullPointerException if comparator is null
   */
  public static <K, V> TreePMap<K, V> empty(final Comparator<? super K> comparator) {
    return new TreePMap<>(KVTree.empty(), comparator, true);
  }

  /**
   * Returns a TreePMap with the specified mappings, using the natural ordering of the keys.
   *
   * @param <K> the type of keys to be maintained by the map
   * @param <V> the type of mapped values
   * @param map the mappings to include
   * @return a TreePMap containing the specified mappings and using the natural ordering of the keys
   * @throws NullPointerException if the map is null or contains a null key
   */
  public static <K extends Comparable<? super K>, V> TreePMap<K, V> from(
      final Map<? extends K, ? extends V> map) {
    return TreePMap.from(Comparator.naturalOrder(), map);
  }

  /**
   * Returns a TreePMap with the specified comparator and mappings.
   *
   * @param <K> the type of keys to be maintained by the map
   * @param <V> the type of mapped values
   * @param comparator the comparator to use
   * @param map the mappings to include
   * @return a TreePMap with the specified comparator and mappings
   * @throws NullPointerException if the comparator or map is null or the map contains a null key
   */
  public static <K, V> TreePMap<K, V> from(
      final Comparator<? super K> comparator, final Map<? extends K, ? extends V> map) {
    return TreePMap.<K, V>empty(comparator).plusAll(map);
  }

  /**
   * Returns a TreePMap with the same mappings and ordering as the specified map. This is
   * essentially equivalent to {@code TreePMap.from(map.comparator(), map)}, except that it
   * gracefully handles a null comparator, and is much more efficient.
   *
   * @param <K> the type of keys to be maintained by the map
   * @param <V> the type of mapped values
   * @param map the map whose mappings and ordering to use
   * @return a TreePMap with the same mappings and ordering as the specified set
   * @throws NullPointerException if the specified map is null or contains a null key
   */
  public static <K, V> TreePMap<K, V> fromSortedMap(final SortedMap<K, ? extends V> map) {
    requireNonNull(map, "map is null");

    if (map instanceof TreePMap<?, ?>) {
      return sneakilyDowncast(map);
    }

    final KVTree<K, V> tree = KVTree.fromEntryIterator(map.entrySet().iterator());

    // check for null keys
    {
      final Iterator<? extends Map.Entry<K, V>> treeIterator = tree.entryIterator(true);
      while (treeIterator.hasNext()) {
        final Map.Entry<K, V> entry = treeIterator.next();
        requireNonNull(entry.getKey(), "map contains null key");
      }
    }

    final Comparator<? super K> thatComparator = map.comparator();
    final Comparator<? super K> comparator =
        thatComparator == null ? sneakilyDowncast(Comparator.naturalOrder()) : thatComparator;

    return new TreePMap<K, V>(tree, comparator, true);
  }

  /**
   * Returns a TreePMap with a single mapping, using the natural ordering of its keys.
   *
   * @param <K> the type of keys to be maintained by the map
   * @param <V> the type of mapped values
   * @param key the key
   * @param value the value
   * @return a TreePMap containing the specified mapping and using the natural ordering
   * @throws NullPointerException if the specified key or value is null
   */
  public static <K extends Comparable<? super K>, V> TreePMap<K, V> singleton(
      final K key, final V value) {
    return TreePMap.singleton(Comparator.naturalOrder(), key, value);
  }

  /**
   * Returns a TreePMap with a single element, using the specified comparator.
   *
   * @param <K> the type of keys to be maintained by the map
   * @param <V> the type of mapped values
   * @param comparator the comparator according to which keys should be ordered
   * @param key the key
   * @param value the value
   * @return a TreePMap containing the specified mapping and using the specified comparator
   * @throws NullPointerException if the specified comparator or key is null
   */
  public static <K, V> TreePMap<K, V> singleton(
      final Comparator<? super K> comparator, final K key, final V value) {
    return TreePMap.<K, V>empty(comparator).plus(key, value);
  }

  /**
   * Returns a collector that gathers a stream into a TreePMap with mappings computed from the
   * elements of the stream (using the specified keyMapper and valueMapper), in the order determined
   * by the natural ordering of the keys. In the event of duplicate keys, the collector will throw
   * IllegalStateException.
   *
   * @param <K> the type of keys to be maintained by the map
   * @param <V> the type of mapped values
   * @param keyMapper a function to compute the key from a stream element
   * @param valueMapper a function to compute the value from a stream element
   * @return a collector that gathers the elements of the stream into a TreePMap
   * @throws NullPointerException if either keyMapper or valueMapper is null
   */
  public static <T, K extends Comparable<? super K>, V> Collector<T, ?, TreePMap<K, V>> toTreePMap(
      final Function<? super T, ? extends K> keyMapper,
      final Function<? super T, ? extends V> valueMapper) {
    return toTreePMap(Comparator.naturalOrder(), keyMapper, valueMapper);
  }

  /**
   * Returns a collector that gathers a stream into a TreePMap with mappings computed from the
   * elements of the stream (using the specified keyMapper and valueMapper), in the order determined
   * by the specified comparator. In the event of duplicate keys, the collector will throw
   * IllegalStateException.
   *
   * @param <K> the type of keys to be maintained by the map
   * @param <V> the type of mapped values
   * @param comparator the comparator according to which keys should be ordered
   * @param keyMapper a function to compute the key from a stream element
   * @param valueMapper a function to compute the value from a stream element
   * @return a collector that gathers the elements of the stream into a TreePMap
   * @throws NullPointerException if any of this method's arguments are null
   */
  public static <T, K, V> Collector<T, ?, TreePMap<K, V>> toTreePMap(
      final Comparator<? super K> comparator,
      final Function<? super T, ? extends K> keyMapper,
      final Function<? super T, ? extends V> valueMapper) {
    final BinaryOperator<V> mergeFunction =
        (oldValue, newValue) -> {
          throw new IllegalStateException("duplicate key");
        };

    return toTreePMap(comparator, keyMapper, valueMapper, mergeFunction);
  }

  /**
   * Returns a collector that gathers a stream into a TreePMap with mappings computed from the
   * elements of the stream (using the specified keyMapper and valueMapper, with duplicates
   * reconciled via the specified mergeFunction), in the order determined by the natural ordering of
   * the keys.
   *
   * @param <K> the type of keys to be maintained by the map
   * @param <V> the type of mapped values
   * @param keyMapper a function to compute the key from a stream element
   * @param valueMapper a function to compute the value from a stream element
   * @param mergeFunction a function to merge duplicate values
   * @return a collector that gathers the elements of the stream into a TreePMap
   * @throws NullPointerException if any of this method's arguments are null
   */
  public static <T, K extends Comparable<? super K>, V> Collector<T, ?, TreePMap<K, V>> toTreePMap(
      final Function<? super T, ? extends K> keyMapper,
      final Function<? super T, ? extends V> valueMapper,
      final BinaryOperator<V> mergeFunction) {
    return toTreePMap(Comparator.naturalOrder(), keyMapper, valueMapper, mergeFunction);
  }

  /**
   * Returns a collector that gathers a stream into a TreePMap with mappings computed from the
   * elements of the stream (using the specified keyMapper and valueMapper, with duplicates
   * reconciled via the specified mergeFunction), in the order determined by the specified
   * comparator.
   *
   * @param <K> the type of keys to be maintained by the map
   * @param <V> the type of mapped values
   * @param comparator the comparator according to which keys should be ordered
   * @param keyMapper a function to compute the key from a stream element
   * @param valueMapper a function to compute the value from a stream element
   * @param mergeFunction a function to merge duplicate values
   * @return a collector that gathers the elements of the stream into a TreePMap
   * @throws NullPointerException if any of this method's arguments are null
   */
  public static <T, K, V> Collector<T, ?, TreePMap<K, V>> toTreePMap(
      final Comparator<? super K> comparator,
      final Function<? super T, ? extends K> keyMapper,
      final Function<? super T, ? extends V> valueMapper,
      final BinaryOperator<V> mergeFunction) {
    requireNonNull(comparator, "comparator is null");
    requireNonNull(keyMapper, "keyMapper is null");
    requireNonNull(valueMapper, "valueMapper is null");
    requireNonNull(mergeFunction, "mergeFunction is null");

    final Supplier<TreeMap<K, V>> treeMapSupplier = () -> new TreeMap<K, V>(comparator);

    final BiConsumer<TreeMap<K, V>, T> accumulator =
        (treeMap, element) -> {
          final K key = requireNonNull(keyMapper.apply(element), "key is null");
          final V value = valueMapper.apply(element);

          if (treeMap.containsKey(key)) {
            treeMap.put(key, mergeFunction.apply(treeMap.get(key), value));
          } else {
            treeMap.put(key, value);
          }
        };

    final BinaryOperator<TreeMap<K, V>> combiner =
        (treeMap1, treeMap2) -> {
          for (final Map.Entry<K, V> entry : treeMap2.entrySet()) {
            final K key = entry.getKey();
            final V value = entry.getValue();

            if (treeMap1.containsKey(key)) {
              treeMap1.put(key, mergeFunction.apply(treeMap1.get(key), value));
            } else {
              treeMap1.put(key, value);
            }
          }
          return treeMap1;
        };

    final Function<TreeMap<K, V>, TreePMap<K, V>> finisher = TreePMap::fromSortedMap;

    return Collector.<T, TreeMap<K, V>, TreePMap<K, V>>of(
        treeMapSupplier, accumulator, combiner, finisher);
  }

  @Override
  public Map.Entry<K, V> ceilingEntry(final K key) {
    return this.search(key, KVTree.SearchType.GE, KVTree.SearchType.LE).orNullIfEmpty();
  }

  @Override
  public K ceilingKey(final K key) {
    return this.search(key, KVTree.SearchType.GE, KVTree.SearchType.LE).getKey();
  }

  @Override
  public Comparator<? super K> comparator() {
    return this.isLeftToRight ? this.ltrComparator : this.ltrComparator.reversed();
  }

  @Override
  public boolean containsKey(final Object key) {
    return this.search(sneakilyDowncast(key), KVTree.SearchType.EQ, KVTree.SearchType.EQ)
        != KVTree.empty();
  }

  @Override
  public PSortedSet<K> descendingKeySet() {
    return new TreePSet<>(this.tree, this.ltrComparator, !this.isLeftToRight);
  }

  @Override
  public TreePMap<K, V> descendingMap() {
    return new TreePMap<>(this.tree, this.ltrComparator, !this.isLeftToRight);
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return new AbstractUnmodifiableSet<Map.Entry<K, V>>() {
      @Override
      public boolean contains(final Object o) {
        if (!(o instanceof Entry)) return false;
        final Entry e = (Entry) o;
        final Object k = e.getKey();
        if (!containsKey(k)) return false;
        return Objects.equals(get(k), e.getValue());
      }

      @Override
      public Iterator<Entry<K, V>> iterator() {
        return TreePMap.this.tree.entryIterator(TreePMap.this.isLeftToRight);
      }

      @Override
      public int size() {
        return TreePMap.this.tree.size();
      }
    };
  }

  @Override
  public Map.Entry<K, V> firstEntry() {
    return (this.isLeftToRight ? this.tree.getLeftmost() : this.tree.getRightmost())
        .orNullIfEmpty();
  }

  @Override
  public K firstKey() {
    return (this.isLeftToRight ? this.tree.getLeftmost() : this.tree.getRightmost()).getKey();
  }

  @Override
  public Map.Entry<K, V> floorEntry(final K key) {
    return this.search(key, KVTree.SearchType.LE, KVTree.SearchType.GE).orNullIfEmpty();
  }

  @Override
  public K floorKey(final K key) {
    return this.search(key, KVTree.SearchType.LE, KVTree.SearchType.GE).getKey();
  }

  @Override
  public V get(final Object key) {
    return this.search(sneakilyDowncast(key), KVTree.SearchType.EQ, KVTree.SearchType.EQ)
        .getValue();
  }

  @Override
  public TreePMap<K, V> headMap(final K toKey) {
    return this.headMap(toKey, false);
  }

  @Override
  public TreePMap<K, V> headMap(final K toKey, final boolean inclusive) {
    requireNonNull(toKey, "toKey is null");

    if (this.isLeftToRight) {
      return this.withTree(this.tree.rangeToLeft(toKey, inclusive, this.ltrComparator));
    } else {
      return this.withTree(this.tree.rangeToRight(toKey, inclusive, this.ltrComparator));
    }
  }

  @Override
  public Map.Entry<K, V> higherEntry(final K key) {
    return this.search(key, KVTree.SearchType.GT, KVTree.SearchType.LT).orNullIfEmpty();
  }

  @Override
  public K higherKey(final K key) {
    return this.search(key, KVTree.SearchType.GT, KVTree.SearchType.LT).getKey();
  }

  @Override
  public boolean isEmpty() {
    return this.tree.isEmpty();
  }

  @Override
  public TreePSet<K> keySet() {
    return new TreePSet<K>(this.tree, this.ltrComparator, this.isLeftToRight);
  }

  @Override
  public Map.Entry<K, V> lastEntry() {
    return (this.isLeftToRight ? this.tree.getRightmost() : this.tree.getLeftmost())
        .orNullIfEmpty();
  }

  @Override
  public K lastKey() {
    return (this.isLeftToRight ? this.tree.getRightmost() : this.tree.getLeftmost()).getKey();
  }

  @Override
  public Map.Entry<K, V> lowerEntry(final K key) {
    return this.search(key, KVTree.SearchType.LT, KVTree.SearchType.GT).orNullIfEmpty();
  }

  @Override
  public K lowerKey(final K key) {
    return this.search(key, KVTree.SearchType.LT, KVTree.SearchType.GT).getKey();
  }

  @Override
  public TreePMap<K, V> minus(final Object key) {
    return this.withTree(
        this.tree.minus(sneakilyDowncast(requireNonNull(key, "key is null")), this.ltrComparator));
  }

  @Override
  public TreePMap<K, V> minusAll(final Collection<?> keys) {
    KVTree<K, V> updatedTree = this.tree;
    for (final Object key : requireNonNull(keys, "keys is null")) {
      updatedTree =
          updatedTree.minus(
              sneakilyDowncast(requireNonNull(key, "keys contains null element")),
              this.ltrComparator);
    }
    return this.withTree(updatedTree);
  }

  @Override
  public TreePMap<K, V> minusFirstEntry() {
    return this.withTree(
        this.isLeftToRight ? this.tree.minusLeftmost() : this.tree.minusRightmost());
  }

  @Override
  public TreePMap<K, V> minusLastEntry() {
    return this.withTree(
        this.isLeftToRight ? this.tree.minusRightmost() : this.tree.minusLeftmost());
  }

  @Override
  public TreePSet<K> navigableKeySet() {
    return this.keySet();
  }

  @Override
  public TreePMap<K, V> plus(final K key, final V value) {
    return this.withTree(this.tree.plus(requireNonNull(key), value, this.ltrComparator));
  }

  @Override
  public TreePMap<K, V> plusAll(final Map<? extends K, ? extends V> map) {
    requireNonNull(map, "map is null");

    KVTree<K, V> updatedTree = this.tree;

    for (final Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
      updatedTree =
          updatedTree.plus(
              requireNonNull(entry.getKey(), "map contains null key"),
              entry.getValue(),
              this.ltrComparator);
    }

    return this.withTree(updatedTree);
  }

  @Override
  public int size() {
    return this.tree.size();
  }

  @Override
  public TreePMap<K, V> subMap(final K fromKey, final K toKey) {
    return this.subMap(fromKey, true, toKey, false);
  }

  @Override
  public TreePMap<K, V> subMap(
      final K fromKey, final boolean fromInclusive, final K toKey, final boolean toInclusive) {
    requireNonNull(fromKey, "fromKey is null");
    requireNonNull(toKey, "toKey is null");

    if (this.comparator().compare(fromKey, toKey) > 0) {
      throw new IllegalArgumentException("fromKey > toKey");
    }

    if (this.isLeftToRight) {
      return this.withTree(
          this.tree.range(fromKey, fromInclusive, toKey, toInclusive, this.ltrComparator));
    } else {
      return this.withTree(
          this.tree.range(toKey, toInclusive, fromKey, fromInclusive, this.ltrComparator));
    }
  }

  @Override
  public TreePMap<K, V> tailMap(final K fromKey) {
    return this.tailMap(fromKey, true);
  }

  @Override
  public TreePMap<K, V> tailMap(final K fromKey, final boolean inclusive) {
    requireNonNull(fromKey, "fromKey is null");

    if (this.isLeftToRight) {
      return this.withTree(this.tree.rangeToRight(fromKey, inclusive, this.ltrComparator));
    } else {
      return this.withTree(this.tree.rangeToLeft(fromKey, inclusive, this.ltrComparator));
    }
  }

  private KVTree<K, V> search(
      final K key,
      final KVTree.SearchType searchTypeIfLeftToRight,
      final KVTree.SearchType searchTypeIfRightToLeft) {

    return this.tree.search(
        requireNonNull(key, "key is null"),
        this.ltrComparator,
        this.isLeftToRight ? searchTypeIfLeftToRight : searchTypeIfRightToLeft);
  }

  private TreePMap<K, V> withTree(final KVTree<K, V> updatedTree) {
    return updatedTree == this.tree
        ? this
        : new TreePMap<K, V>(updatedTree, this.ltrComparator, this.isLeftToRight);
  }

  // we put this in its own method, to limit the scope of the @SuppressWarnings:
  @SuppressWarnings("unchecked")
  private static <T> T sneakilyDowncast(final Object o) {
    return (T) o;
  }
}
