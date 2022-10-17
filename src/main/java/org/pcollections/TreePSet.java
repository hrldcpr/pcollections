/*
 * Copyright (c) 2022 Ran Ari-Gur. All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

package org.pcollections;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * An implementation of {@link PSortedSet} based on a self-balancing binary search tree.
 *
 * <p>Null values are not allowed.
 *
 * <p>Instances of this class are obtained via any of various static factory methods and static
 * collector methods. These methods come in pairs, with one version that accepts an explicit
 * comparator to use and one version that uses the natural ordering of the elements.
 *
 * <p>All operations are guaranteed to complete within O(log n) time, except for plusAll and
 * minusAll, whose time cost is equivalent to the corresponding sequence of calls to plus or minus.
 * A complete iteration pass completes in O(n) time. A few operations -- namely comparator,
 * descendingSet, isEmpty, and size -- complete in O(1) time.
 *
 * @param <E> the type of elements maintained by this set
 * @author Ran Ari-Gur
 * @since 3.2.0
 */
public final class TreePSet<E> extends AbstractUnmodifiableSet<E>
    implements PSortedSet<E>, Serializable {
  private static final long serialVersionUID = 1L;

  private final KVTree<E, ?> tree;
  private final Comparator<? super E> ltrComparator;
  private final boolean isLeftToRight;

  /**
   * Non-private only because also used by TreePMap.keySet(). No other code should use it from
   * outside this class.
   */
  TreePSet(
      final KVTree<E, ?> tree,
      final Comparator<? super E> ltrComparator,
      final boolean isLeftToRight) {

    this.tree = requireNonNull(tree, "tree is null");
    this.ltrComparator = requireNonNull(ltrComparator, "comparator is null");
    this.isLeftToRight = isLeftToRight;
  }

  /**
   * Returns an empty TreePSet using the natural ordering.
   *
   * @param <E> the type of elements to be maintained by the set
   * @return an empty TreePSet using the natural ordering
   */
  public static <E extends Comparable<? super E>> TreePSet<E> empty() {
    return TreePSet.empty(Comparator.naturalOrder());
  }

  /**
   * Returns an empty TreePSet using the specified comparator.
   *
   * @param <E> the type of elements to be maintained by the set
   * @param comparator the comparator according to which elements should be ordered
   * @return an empty TreePSet using the specified comparator
   * @throws NullPointerException if comparator is null
   */
  public static <E> TreePSet<E> empty(final Comparator<? super E> comparator) {
    return new TreePSet<E>(KVTree.empty(), comparator, true);
  }

  /**
   * Returns a TreePSet with the specified elements, using their natural ordering.
   *
   * @param <E> the type of elements to be maintained by the set
   * @param list the elements to include
   * @return a TreePSet containing the elements of list and using the natural ordering
   * @throws NullPointerException if list is null or contains null
   */
  public static <E extends Comparable<? super E>> TreePSet<E> from(
      final Collection<? extends E> list) {
    return TreePSet.from(Comparator.naturalOrder(), list);
  }

  /**
   * Returns a TreePSet with the specified comparator and elements.
   *
   * @param <E> the type of elements to be maintained by the set
   * @param comparator the comparator to use
   * @param list the collection of elements to include; may include duplicates, but the returned
   *     TreePSet will not
   * @return a TreePSet with the specified comparator and elements
   * @throws NullPointerException if the comparator is null or the collection is or contains null
   */
  public static <E> TreePSet<E> from(
      final Comparator<? super E> comparator, final Collection<? extends E> list) {
    return TreePSet.<E>empty(comparator).plusAll(list);
  }

  /**
   * Returns a TreePSet with the same elements and ordering as the specified set. This is
   * essentially equivalent to {@code TreePSet.from(set.comparator(), set)}, except that it
   * gracefully handles a null comparator, and is much more efficient.
   *
   * @param <E> the type of elements to be maintained by the set
   * @param set the set whose elements and ordering to use
   * @return a TreePSet with the same elements and ordering as the specified set
   * @throws NullPointerException if the specified set is or contains null
   */
  public static <E> TreePSet<E> fromSortedSet(final SortedSet<E> set) {
    if (requireNonNull(set, "set is null") instanceof TreePSet<?>) {
      return (TreePSet<E>) set;
    }

    final KVTree<E, ?> tree = KVTree.fromKeyIterator(set.iterator());

    // check for null elements:
    {
      final Iterator<? extends Map.Entry<E, ?>> treeIterator = tree.entryIterator(true);
      while (treeIterator.hasNext()) {
        requireNonNull(treeIterator.next().getKey(), "set contains null element");
      }
    }

    final Comparator<? super E> thatComparator = set.comparator();
    final Comparator<? super E> comparator =
        thatComparator == null ? sneakilyDowncast(Comparator.naturalOrder()) : thatComparator;

    return new TreePSet<E>(tree, comparator, true);
  }

  /**
   * Returns a TreePSet with the specified elements, using their natural ordering.
   *
   * @param <E> the type of elements to be maintained by the set
   * @param elements the elements to include; may include duplicates, but the returned TreePSet will
   *     not
   * @return a TreePSet containing the specified elements and using their natural ordering
   * @throws NullPointerException if any of the specified elements is null, or if the varargs
   *     array-ref is itself null
   */
  @SafeVarargs
  public static <E extends Comparable<? super E>> TreePSet<E> of(final E... elements) {
    return of(Comparator.naturalOrder(), elements);
  }

  /**
   * Returns a TreePSet with the specified comparator and elements.
   *
   * @param <E> the type of elements to be maintained by the set
   * @param comparator the comparator to use
   * @param elements the elements to include; may include duplicates, but the returned TreePSet will
   *     not
   * @return a TreePSet containing the specified elements and using the specified comparator
   * @throws NullPointerException if the specified comparator is null, or if any of the specified
   *     elements is null, or if the varargs array-ref is itself null
   */
  @SafeVarargs
  public static <E> TreePSet<E> of(final Comparator<? super E> comparator, final E... elements) {
    return TreePSet.<E>empty(comparator).plusAll(Arrays.asList(elements));
  }

  /**
   * Returns a TreePSet with a single element, using the natural ordering.
   *
   * @param <E> the type of elements to be maintained by the set
   * @param e the element
   * @return a TreePSet containing the specified element and using the natural ordering
   * @throws NullPointerException if the specified element is null
   */
  public static <E extends Comparable<? super E>> TreePSet<E> singleton(final E e) {
    return TreePSet.singleton(Comparator.naturalOrder(), e);
  }

  /**
   * Returns a TreePSet with a single element, using the specified comparator.
   *
   * @param <E> the type of elements to be maintained by the set
   * @param comparator the comparator according to which elements should be ordered
   * @param e the element
   * @return a TreePSet containing the specified element and using the specified comparator
   * @throws NullPointerException if either the comparator or the element is null
   */
  public static <E> TreePSet<E> singleton(final Comparator<? super E> comparator, final E e) {
    return TreePSet.<E>empty(comparator).plus(e);
  }

  /**
   * Returns a collector that gathers a stream into a TreePSet with the elements of that stream,
   * using their natural ordering.
   *
   * @param <E> the type of elements to be maintained by the set
   * @return a collector that gathers the elements of the stream into a TreePSet that uses the
   *     natural ordering
   */
  public static <E extends Comparable<? super E>> Collector<E, ?, TreePSet<E>> toTreePSet() {
    return TreePSet.toTreePSet(Comparator.naturalOrder());
  }

  /**
   * Returns a collector that gathers a stream into a TreePSet with the elements of that stream,
   * using the specified comparator.
   *
   * @param <E> the type of elements to be maintained by the set
   * @param comparator the comparator to use
   * @return a collector that gathers the elements of the stream into a TreePSet that uses the
   *     specified comparator
   * @throws NullPointerException if the comparator is null
   */
  public static <E> Collector<E, ?, TreePSet<E>> toTreePSet(
      final Comparator<? super E> comparator) {
    requireNonNull(comparator, "comparator is null");

    return Collectors.collectingAndThen(
        Collectors.toCollection(() -> new TreeSet<E>(comparator)),
        set -> TreePSet.fromSortedSet(set));
  }

  @Override
  public E ceiling(final E e) {
    return this.search(e, KVTree.SearchType.GE, KVTree.SearchType.LE);
  }

  @Override
  public Comparator<? super E> comparator() {
    return this.isLeftToRight ? this.ltrComparator : this.ltrComparator.reversed();
  }

  @Override
  public boolean contains(final Object e) {
    return this.search(sneakilyDowncast(e), KVTree.SearchType.EQ, KVTree.SearchType.EQ) != null;
  }

  @Override
  public Iterator<E> descendingIterator() {
    return this.descendingSet().iterator();
  }

  @Override
  public TreePSet<E> descendingSet() {
    return new TreePSet<E>(this.tree, this.ltrComparator, !this.isLeftToRight);
  }

  @Override
  public E first() {
    return (this.isLeftToRight ? this.tree.getLeftmost() : this.tree.getRightmost()).getKey();
  }

  @Override
  public E floor(final E e) {
    return this.search(e, KVTree.SearchType.LE, KVTree.SearchType.GE);
  }

  @Override
  public TreePSet<E> headSet(final E toElement) {
    return this.headSet(toElement, false);
  }

  @Override
  public TreePSet<E> headSet(final E toElement, final boolean inclusive) {
    requireNonNull(toElement, "toElement is null");

    return this.withTree(
        this.isLeftToRight
            ? this.tree.rangeToLeft(toElement, inclusive, this.ltrComparator)
            : this.tree.rangeToRight(toElement, inclusive, this.ltrComparator));
  }

  @Override
  public E higher(final E e) {
    return this.search(e, KVTree.SearchType.GT, KVTree.SearchType.LT);
  }

  @Override
  public Iterator<E> iterator() {
    final Iterator<? extends Map.Entry<E, ?>> entryIterator =
        this.tree.entryIterator(this.isLeftToRight);
    return new Iterator<E>() {
      @Override
      public boolean hasNext() {
        return entryIterator.hasNext();
      }

      @Override
      public E next() {
        return entryIterator.next().getKey();
      }
    };
  }

  @Override
  public E last() {
    return (this.isLeftToRight ? this.tree.getRightmost() : this.tree.getLeftmost()).getKey();
  }

  @Override
  public E lower(final E e) {
    return this.search(e, KVTree.SearchType.LT, KVTree.SearchType.GT);
  }

  @Override
  public TreePSet<E> minus(final Object e) {
    return this.withTree(
        this.tree.minus(
            sneakilyDowncast(requireNonNull(e, "element is null")), this.ltrComparator));
  }

  @Override
  public TreePSet<E> minusAll(final Collection<?> list) {

    KVTree<E, ?> tree = this.tree;

    for (final Object e : requireNonNull(list, "list is null")) {
      requireNonNull(e, "list contains null element");

      tree = tree.minus(sneakilyDowncast(e), this.ltrComparator);
    }

    return this.withTree(tree);
  }

  @Override
  public TreePSet<E> minusFirst() {
    return this.withTree(
        this.isLeftToRight ? this.tree.minusLeftmost() : this.tree.minusRightmost());
  }

  @Override
  public TreePSet<E> minusLast() {
    return this.withTree(
        this.isLeftToRight ? this.tree.minusRightmost() : this.tree.minusLeftmost());
  }

  @Override
  public TreePSet<E> plus(final E e) {
    return this.withTree(
        this.tree.plus(requireNonNull(e, "element is null"), null, this.ltrComparator));
  }

  @Override
  public TreePSet<E> plusAll(final Collection<? extends E> list) {
    KVTree<E, ?> tree = this.tree;

    for (final E e : requireNonNull(list, "list is null")) {
      tree = tree.plus(requireNonNull(e, "list contains null element"), null, this.ltrComparator);
    }

    return this.withTree(tree);
  }

  @Override
  public int size() {
    return this.tree.size();
  }

  @Override
  public TreePSet<E> subSet(final E fromElement, final E toElement) {
    return this.subSet(fromElement, true, toElement, false);
  }

  @Override
  public TreePSet<E> subSet(
      final E fromElement,
      final boolean fromInclusive,
      final E toElement,
      final boolean toInclusive) {
    requireNonNull(fromElement, "fromElement is null");
    requireNonNull(fromElement, "toElement is null");

    if (this.comparator().compare(fromElement, toElement) > 0) {
      throw new IllegalArgumentException("fromElement > toElement");
    }

    return this.withTree(
        this.isLeftToRight
            ? this.tree.range(
                fromElement, fromInclusive, toElement, toInclusive, this.ltrComparator)
            : this.tree.range(
                toElement, toInclusive, fromElement, fromInclusive, this.ltrComparator));
  }

  @Override
  public TreePSet<E> tailSet(final E fromElement) {
    return this.tailSet(fromElement, true);
  }

  private E search(
      final E e,
      final KVTree.SearchType searchTypeIfLeftToRight,
      final KVTree.SearchType searchTypeIfRightToLeft) {
    return this.tree
        .search(
            requireNonNull(e, "e is null"),
            this.ltrComparator,
            this.isLeftToRight ? searchTypeIfLeftToRight : searchTypeIfRightToLeft)
        .getKey();
  }

  @Override
  public TreePSet<E> tailSet(final E fromElement, final boolean inclusive) {
    requireNonNull(fromElement, "fromElement is null");

    return this.withTree(
        this.isLeftToRight
            ? this.tree.rangeToRight(fromElement, inclusive, this.ltrComparator)
            : this.tree.rangeToLeft(fromElement, inclusive, this.ltrComparator));
  }

  private TreePSet<E> withTree(final KVTree<E, ?> tree) {
    return tree == this.tree ? this : new TreePSet<E>(tree, this.ltrComparator, this.isLeftToRight);
  }

  // we put this in its own method, to limit the scope of the @SuppressWarnings:
  @SuppressWarnings("unchecked")
  private static <T> T sneakilyDowncast(final Object o) {
    return (T) o;
  }
}
