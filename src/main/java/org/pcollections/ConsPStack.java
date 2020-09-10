/*
 * Copyright (c) 2008 Harold Cooper. All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

package org.pcollections;

import java.io.Serializable;
import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * A simple persistent stack of non-null values.
 *
 * <p>This implementation is thread-safe (assuming Java's AbstractSequentialList is thread-safe),
 * although its iterators may not be.
 *
 * @author harold
 * @param <E>
 */
public final class ConsPStack<E> extends AbstractSequentialList<E>
    implements PStack<E>, Serializable {

  private static final long serialVersionUID = 1L;

  //// STATIC FACTORY METHODS ////
  private static final ConsPStack<Object> EMPTY = new ConsPStack<Object>();

  /**
   * @param <E>
   * @return an empty stack
   */
  @SuppressWarnings("unchecked")
  public static <E> ConsPStack<E> empty() {
    return (ConsPStack<E>) EMPTY;
  }

  /**
   * @param <E>
   * @param e
   * @return empty().plus(e)
   */
  public static <E> ConsPStack<E> singleton(final E e) {
    return ConsPStack.<E>empty().plus(e);
  }

  /**
   * @param <E>
   * @param list
   * @return a stack consisting of the elements of list in the order of list.iterator()
   */
  @SuppressWarnings("unchecked")
  public static <E> ConsPStack<E> from(final Collection<? extends E> list) {
    if (list instanceof ConsPStack)
      return (ConsPStack<E>) list; // (actually we only know it's ConsPStack<? extends E>)
    // but that's good enough for an immutable
    // (i.e. we can't mess someone else up by adding the wrong type to it)
    return ConsPStack.<E>from(list.iterator());
  }

  private static <E> ConsPStack<E> from(final Iterator<? extends E> i) {
    if (!i.hasNext()) return empty();
    E e = i.next();
    return ConsPStack.<E>from(i).plus(e);
  }

  //// PRIVATE CONSTRUCTORS ////
  private final E first;
  private final ConsPStack<E> rest;
  private final int size;
  // not externally instantiable (or subclassable):
  private ConsPStack() { // EMPTY constructor
    if (EMPTY != null) throw new RuntimeException("empty constructor should only be used once");
    size = 0;
    first = null;
    rest = null;
  }

  private ConsPStack(final E first, final ConsPStack<E> rest) {
    this.first = first;
    this.rest = rest;

    size = 1 + rest.size;
  }

  //// REQUIRED METHODS FROM AbstractSequentialList ////
  @Override
  public int size() {
    return size;
  }

  @Override
  public ListIterator<E> listIterator(final int index) {
    if (index < 0 || index > size) throw new IndexOutOfBoundsException();

    return new ListIterator<E>() {
      int i = index;
      ConsPStack<E> next = subList(index);

      public boolean hasNext() {
        return next.size > 0;
      }

      public boolean hasPrevious() {
        return i > 0;
      }

      public int nextIndex() {
        return i;
      }

      public int previousIndex() {
        return i - 1;
      }

      public E next() {
        E e = next.first;
        if (e == null && !hasNext()) {
          throw new NoSuchElementException();
        }
        next = next.rest;
        i++;
        return e;
      }

      public E previous() {
        System.err.println("ConsPStack.listIterator().previous() is inefficient, don't use it!");
        if (!hasPrevious()) {
          throw new NoSuchElementException();
        }
        next = subList(--i); // go from beginning...
        return next.first;
      }

      public void add(final E o) {
        throw new UnsupportedOperationException();
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }

      public void set(final E o) {
        throw new UnsupportedOperationException();
      }
    };
  }

  //// OVERRIDDEN METHODS FROM AbstractSequentialList ////
  @Override
  public ConsPStack<E> subList(final int start, final int end) {
    if (start < 0 || end > size || start > end) throw new IndexOutOfBoundsException();
    if (start == end) return empty();
    if (start > 0) return subList(start).subList(0, end - start); // remove from beginning
    if (end == size) return this;

    // remove from end (by popping off until end, and then pushing back on)
    ConsPStack<E> reversed = empty();
    for (final E e : this) {
      if (reversed.size == end) break;
      reversed = reversed.plus(e);
    }
    return this.<E>empty().plusAll(reversed); // plusAll reverses again
  }

  //// IMPLEMENTED METHODS OF PStack ////
  public ConsPStack<E> plus(final E e) {
    return new ConsPStack<E>(e, this);
  }

  public ConsPStack<E> plusAll(final Collection<? extends E> list) {
    ConsPStack<E> result = this;
    for (E e : list) result = result.plus(e);
    return result;
  }

  public ConsPStack<E> plus(final int i, final E e) {
    return plusAll(i, singleton(e));
  }

  public ConsPStack<E> plusAll(final int i, final Collection<? extends E> list) {
    if (i < 0 || i > size) throw new IndexOutOfBoundsException();
    if (list.isEmpty()) return this;

    ConsPStack<E> reversed = empty();
    ConsPStack<E> suffix = this;
    while (reversed.size < i) {
      reversed = reversed.plus(suffix.first);
      suffix = suffix.rest;
    }
    return suffix.plusAll(list).plusAll(reversed); // plusAll reverses again
  }

  public ConsPStack<E> minus(final Object e) {
    final int i = indexOf(e);
    if (i == -1) return this;
    return minus(i);
  }

  public ConsPStack<E> minus(final int i) {
    if (i < 0 || i >= size) throw new IndexOutOfBoundsException("Index: " + i + "; size: " + size);

    ConsPStack<E> reversed = empty();
    ConsPStack<E> suffix = this;
    while (reversed.size <= i) {
      reversed = reversed.plus(suffix.first);
      suffix = suffix.rest;
    }
    return suffix.plusAll(reversed.rest); // plusAll reverses again
  }

  public ConsPStack<E> minusAll(final Collection<?> list) {
    // TODO wrap list in a Set to speed up contains()?
    if (list.isEmpty()) return this;

    ConsPStack<E> kept = empty();
    ConsPStack<E> searching = this;
    ConsPStack<E> reversed = kept;
    ConsPStack<E> suffix = searching;
    while (searching.size > 0) {
      final E e = searching.first;
      searching = searching.rest;
      if (list.contains(e)) {
        // checkpoint only when we actually remove something, so we can reuse existing suffix stack
        reversed = kept;
        suffix = searching;
      } else kept = kept.plus(e);
    }
    return suffix.plusAll(reversed); // plusAll reverses again
  }

  public ConsPStack<E> with(final int i, final E e) {
    if (i < 0 || i >= size) throw new IndexOutOfBoundsException();

    ConsPStack<E> reversed = empty();
    ConsPStack<E> suffix = this;
    while (reversed.size <= i) {
      reversed = reversed.plus(suffix.first);
      suffix = suffix.rest;
    }
    return suffix.plus(e).plusAll(reversed.rest); // plusAll reverses again
  }

  public ConsPStack<E> subList(int start) {
    if (start < 0 || start > size) throw new IndexOutOfBoundsException();

    ConsPStack<E> s = this;
    while (start > 0) {
      s = s.rest;
      start--;
    }
    return s;
  }
}
