/*
 * Copyright (c) 2022 Ran Ari-Gur. All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

package org.pcollections;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * A subclass of AbstractSet that overrides the various mutator methods to mark them as deprecated
 * and unconditionally throw UnsupportedOperationException.
 *
 * @param <E>  the type of elements maintained by this set
 *
 * @author Ran Ari-Gur
 * @since 3.2.0
 */
public abstract class AbstractUnmodifiableSet<E> extends AbstractSet<E> {
  /**
   * @throws UnsupportedOperationException  always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public boolean add(final E e) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException  always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public boolean addAll(final Collection<? extends E> list) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException  always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException  always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public boolean remove(final Object o) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException  always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public boolean removeAll(final Collection<?> list) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException  always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public boolean removeIf(final Predicate<? super E> filter) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException  always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public boolean retainAll(final Collection<?> list) {
    throw new UnsupportedOperationException();
  }
}
