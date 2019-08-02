/*
 * Copyright (c) 2008 Harold Cooper. All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

package org.pcollections;

import java.util.Collection;

/**
 * An unordered collection allowing duplicate elements.
 *
 * @author harold
 * @param <E>
 */
public interface PBag<E> extends PCollection<E> {
  // @Override
  public PBag<E> plus(E e);
  // @Override
  public PBag<E> plusAll(Collection<? extends E> list);
  // @Override
  public PBag<E> minus(Object e);
  // @Override
  public PBag<E> minusAll(Collection<?> list);
}
