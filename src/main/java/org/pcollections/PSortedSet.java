/*
 * Copyright (c) 2022 Ran Ari-Gur. All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

package org.pcollections;

import java.util.Collection;
import java.util.Comparator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;

/**
 * An immutable, persistent set of distinct elements, with elements arranged in sorted
 * order (according to some {@link java.util.Comparator}), and with various methods to obtain
 * specific elements or ranges of elements based on this ordering (such as the least element greater
 * than some value, or the set of elements between two values).
 *
 * <p>(Note: this is different from {@link OrderedPSet}, which keeps elements in the order that they
 * were added to the set.)
 *
 * <p>Null values are disallowed unless the subclass specifically supports them;
 * attempts to add a null value will result in {@link NullPointerException}.
 *
 * <p>Every PSortedSet is a {@link java.util.Set} and more specifically a {@link PSet}, but as with
 * any sorted set, a PSortedSet will only obey the general contract of those interfaces if its
 * comparator is consistent with equals. (See {@link java.util.SortedSet} for more information.)
 *
 * <p>Every PSortedSet is a {@link java.util.SortedSet} and more specifically a {@link
 * java.util.NavigableSet}, but the implementations of PSortedSet provided by this library
 * (pcollections) depart from the specification of those interfaces in a few ways:
 *
 * <ul>
 *   <li>headSet(...), subSet(...), and tailSet(...) are specified by SortedSet and NavigableSet to
 *       return sets with a "restricted range", and to throw IllegalArgumentException if this
 *       instance already has a restricted range and the relevant argument is outside that range.
 *       (This ensures that set.headSet(10).headSet(15) doesn't contain elements that
 *       set.headSet(10) does not, and that set.headSet(10).headSet(15).add(12) is invalid because
 *       12 can't be added to set.headSet(10).) This library's implementations do not throw
 *       IllegalArgumentException, but rather, they ensure that an argument outside the applicable
 *       range simply has no effect; so, set.headSet(10).headSet(15) is equivalent to
 *       set.headSet(10), because set.headSet(10) already contains no elements ≥ 15. (This is also
 *       the behavior of Guava's ImmutableSortedSet. The JDK's
 *       Collections.unmodifiableSortedSet(...) and Collections.unmodifiableNavigableSet(...) are
 *       agnostic on this point, because they just delegate to the underlying set.) Other
 *       implementations are encouraged to consider doing the same, and to document their behavior
 *       in this respect. Additionally, any implementations that <em>do</em> use the "restricted
 *       range" concept are encouraged to document the behavior of their minus, minusAll, plus, and
 *       plusAll methods when a value is outside the restricted range.
 *   <li>comparator() is specified by SortedSet to return "null if this set uses the natural
 *       ordering of its elements". This library's implementations never return null from that
 *       method; instead, when the set uses the natural ordering, the method returns a Comparator
 *       instance that implements the natural ordering. (This is because this library avoids null in
 *       general. This is also the behavior of Guava's ImmutableSortedSet, which also rejects null
 *       elements.) Other implementations of PSortedSet are encouraged to consider doing the same,
 *       and to document their behavior in this case (whether or not it's to return null).
 *   <li>pollFirst() and pollLast() are specified by NavigableSet to mutate this set, and are not
 *       specified to be optional operations. That's obviously not an option for a PSet, so
 *       PSortedSet provides default implementations of these methods that simply throw
 *       UnsupportedOperationException, which should be the right implementation for any
 *       implementation of this interface. (This is also the behavior of the JDK's
 *       Collections.unmodifiableNavigableSet(...) and Guava's ImmutableSortedSet.)
 * </ul>
 *
 * @param <E> the type of elements maintained by this set
 * @author Ran Ari-Gur
 * @since 3.2.0
 * @see java.util.SortedSet
 * @see java.util.NavigableSet
 * @see java.util.Collections#unmodifiableSortedSet(java.util.SortedSet)
 * @see java.util.Collections#unmodifiableNavigableSet(java.util.NavigableSet)
 * @see org.pcollections.PSet
 * @see org.pcollections.PSortedMap
 * @see org.pcollections.TreePSet
 */
public interface PSortedSet<E> extends PSet<E>, NavigableSet<E> {
  /**
   * @return The comparator used to order the elements in this set. May be null if this set uses the
   *     natural ordering of its elements, though in that case the implementations provided by this
   *     library (pcollections) return a Comparator instance that implements the natural ordering.
   */
  @Override
  public Comparator<? super E> comparator();

  @Override
  public PSortedSet<E> descendingSet();

  @Override
  public PSortedSet<E> headSet(E toElement);

  @Override
  public PSortedSet<E> headSet(E toElement, boolean inclusive);

  /**
   * @param e
   * @return This set, except with e removed (if e is an element of this set).
   * @throws ClassCastException if the type of e is incompatible with this set (optional)
   * @throws IllegalArgumentException if e is incompatible with this set (optional)
   */
  @Override
  public PSortedSet<E> minus(Object e);

  /**
   * @param list
   * @return This set, except with the elements of list removed (if they are elements of this set).
   * @throws NullPointerException if list is null or contains null
   * @throws ClassCastException if list contains an element whose type is incompatible with this set
   *     (optional)
   * @throws IllegalArgumentException if list contains an element that is incompatible with this set
   *     (optional)
   */
  @Override
  public PSortedSet<E> minusAll(Collection<?> list);

  /**
   * @return This set, except with its first (least) element removed.
   * @throws NoSuchElementException if this set is empty
   */
  public PSortedSet<E> minusFirst();

  /**
   * @return This set, except with its last (greatest) element removed.
   * @throws NoSuchElementException if this set is empty
   */
  public PSortedSet<E> minusLast();

  /**
   * @param e
   * @return This set, except with e added (unless e is already an element of this set).
   * @throws NullPointerException if e is null
   * @throws IllegalArgumentException if e is incompatible with this set (optional)
   */
  @Override
  public PSortedSet<E> plus(E e);

  /**
   * @param list
   * @return This set, except with the elements of list added (unless they are already elements of
   *     this set).
   * @throws NullPointerException if list is null or contains null
   * @throws IllegalArgumentException if list contains an element that is incompatible with this set
   *     (optional)
   */
  @Override
  public PSortedSet<E> plusAll(Collection<? extends E> list);

  @Override
  public PSortedSet<E> subSet(E fromElement, E toElement);

  @Override
  public PSortedSet<E> subSet(
      E fromElement, boolean fromInclusive, E toElement, boolean toInclusive);

  @Override
  public PSortedSet<E> tailSet(E fromElement);

  @Override
  public PSortedSet<E> tailSet(E fromElement, boolean inclusive);

  /**
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  default E pollFirst() {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  default E pollLast() {
    throw new UnsupportedOperationException();
  }
}
