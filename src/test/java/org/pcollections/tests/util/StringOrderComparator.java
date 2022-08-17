/*
 * Copyright (c) 2022 Ran Ari-Gur. All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

package org.pcollections.tests.util;

import java.io.Serializable;
import java.util.Comparator;

/**
 * A comparator that compares objects by their string values; for example, it reports that 1 is less
 * than 10 and 10 is less than 2, because in fact "1" is less than "10" and "10" is less than "2".
 * This is not consistent with equals over all objects, but is consistent with equals over
 * restricted domains, such as instances of java.lang.Integer, or instances of java.lang.Double.
 * (Intended for use by TreePSetTest and TreePMapTest in verifying correct handling when a custom
 * comparator is used.)
 *
 * @author Ran Ari-Gur
 */
public enum StringOrderComparator implements Comparator<Object>, Serializable {
  INSTANCE;

  @Override
  public int compare(final Object a, final Object b) {
    return String.valueOf(a).compareTo(String.valueOf(b));
  }
}
