/*
 * Copyright (c) 2022 Ran Ari-Gur. All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

package org.pcollections.tests.util;

/**
 * A class with a natural ordering that is inconsistent with equals; instances have a distinct 'eq'
 * field and 'comp' field, where two instances are equal according to {@link #equals(Object)} if
 * they have the same value of 'eq', but compare equal according to {@link
 * #compareTo(CompareInconsistentWithEquals)} if they have the same value of 'comp'. (Intended for
 * use by TreePSetTest and TreePMapTest in verifying correct handling when an ordering is
 * inconsistent with equals.)
 *
 * @author Ran Ari-Gur
 */
public final class CompareInconsistentWithEquals
    implements Comparable<CompareInconsistentWithEquals> {
  private final int eq;
  private final int comp;

  /**
   * @param eq the value to control whether two instances are equal according to {@link
   *     #equals(Object)}
   * @param comp the value to control the ordering of two instances according to {@link
   *     #compareTo(CompareInconsistentWithEquals)}, and hence whether two instances compare equal
   *     according to their natural ordering
   */
  public CompareInconsistentWithEquals(final int eq, final int comp) {
    this.eq = eq;
    this.comp = comp;
  }

  @Override
  public int compareTo(final CompareInconsistentWithEquals that) {
    return Integer.compare(this.comp, that.comp);
  }

  @Override
  public boolean equals(final Object that) {
    return that instanceof CompareInconsistentWithEquals
        && this.eq == ((CompareInconsistentWithEquals) that).eq;
  }

  @Override
  public String toString() {
    return String.format("CompareInconsistentWithEquals(%s, %s)", eq, comp);
  }
}
