/*
 * Copyright (c) 2008 Harold Cooper. All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

package org.pcollections;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

public class OrderedPSet<E> extends AbstractUnmodifiableSet<E>
    implements POrderedSet<E>, Serializable {

  private static final long serialVersionUID = 1L;

  private static final OrderedPSet<Object> EMPTY =
      new OrderedPSet<Object>(Empty.set(), Empty.vector());

  @SuppressWarnings("unchecked")
  public static <E> OrderedPSet<E> empty() {
    return (OrderedPSet<E>) EMPTY;
  }

  @SuppressWarnings("unchecked")
  public static <E> OrderedPSet<E> from(final Collection<? extends E> list) {
    if (list instanceof OrderedPSet) return (OrderedPSet<E>) list;
    return OrderedPSet.<E>empty().plusAll(list);
  }

  public static <E> OrderedPSet<E> singleton(final E e) {
    return OrderedPSet.<E>empty().plus(e);
  }

  private final PSet<E> contents;
  private final PVector<E> order;

  private OrderedPSet(final PSet<E> c, final PVector<E> o) {
    contents = c;
    order = o;
  }

  @Override
  public OrderedPSet<E> plus(final E e) {
    if (contents.contains(e)) return this;
    return new OrderedPSet<E>(contents.plus(e), order.plus(e));
  }

  @Override
  public OrderedPSet<E> plusAll(final Collection<? extends E> list) {
    OrderedPSet<E> s = this;
    for (final E e : list) {
      s = s.plus(e);
    }
    return s;
  }

  @Override
  public OrderedPSet<E> minus(final Object e) {
    if (!contents.contains(e)) return this;
    return new OrderedPSet<E>(contents.minus(e), order.minus(e));
  }

  @Override
  public OrderedPSet<E> minusAll(final Collection<?> list) {
    OrderedPSet<E> s = this;
    for (final Object e : list) {
      s = s.minus(e);
    }
    return s;
  }

  @Override
  public Iterator<E> iterator() {
    return order.iterator();
  }

  @Override
  public int size() {
    return contents.size();
  }

  @Override
  public E get(final int index) {
    return order.get(index);
  }

  @Override
  public int indexOf(final Object o) {
    if (!contents.contains(o)) return -1;
    return order.indexOf(o);
  }
}
