/*
 * Copyright (c) 2022 Harold Cooper. All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

package org.pcollections;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * A subclass of AbstractQueue that overrides the various mutator methods to mark them as deprecated
 * and unconditionally throw UnsupportedOperationException.
 */
public abstract class AbstractUnmodifiableQueue<E> extends AbstractQueue<E> {

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
  public boolean addAll(final Collection<? extends E> c) {
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
  public boolean offer(final E e) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public E poll() {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public E remove() {
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
  public boolean retainAll(final Collection<?> list) {
    throw new UnsupportedOperationException();
  }
}
