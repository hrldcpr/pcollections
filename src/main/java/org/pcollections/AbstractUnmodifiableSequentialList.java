/*
 * Copyright (c) 2022 Harold Cooper. All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

package org.pcollections;

import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * A subclass of AbstractSequentialList that overrides the various mutator methods to mark them as
 * deprecated and unconditionally throw UnsupportedOperationException.
 */
public abstract class AbstractUnmodifiableSequentialList<E> extends AbstractSequentialList<E> {

  /**
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public boolean add(final E e) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public void add(final int index, final E element) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public boolean addAll(final Collection<? extends E> c) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public boolean addAll(final int index, final Collection<? extends E> c) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public E remove(final int index) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public boolean remove(final Object o) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public boolean removeAll(final Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public boolean removeIf(final Predicate<? super E> filter) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public void replaceAll(UnaryOperator<E> operator) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public boolean retainAll(final Collection<?> list) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public E set(final int index, final E element) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public void sort(Comparator<? super E> c) {
    throw new UnsupportedOperationException();
  }
}
