/*
 * Copyright (c) 2008 Harold Cooper. All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

package org.pcollections.tests;

import static java.util.stream.Collectors.toList;
import static org.pcollections.tests.util.CollectionHelpers.assertSetSemantics;
import static org.pcollections.tests.util.NullCheckAssertions.assertSetChecksForNull;
import static org.pcollections.tests.util.UnmodifiableAssertions.assertSetMutatorsThrow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import junit.framework.TestCase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.pcollections.Empty;
import org.pcollections.OrderedPSet;
import org.pcollections.POrderedSet;
import org.pcollections.PSet;
import org.pcollections.TreePSet;

public class OrderedPSetTest extends TestCase {

  public void testPlus() {
    POrderedSet<Integer> s = Empty.orderedSet();
    s = s.plus(3).plus(2).plus(1).plus(1).plus(2).plus(3).plus(4);

    int vals[] = new int[] {3, 2, 1, 4};
    assertEquals(vals.length, s.size());

    Iterator<Integer> it = s.iterator();
    for (int i = 0; i < vals.length; i++) {
      assertEquals(vals[i], it.next().intValue());
    }
  }

  public void testPlusMinus() {
    POrderedSet<Integer> s = Empty.orderedSet();
    s = s.plus(3).plus(2).plus(1).minus(1).plus(2).plus(3).minus(17).plus(5).plus(1).plus(4);

    int vals[] = new int[] {3, 2, 5, 1, 4};
    assertEquals(vals.length, s.size());

    Iterator<Integer> it = s.iterator();
    for (int i = 0; i < vals.length; i++) {
      assertEquals(vals[i], it.next().intValue());
    }
  }

  public void testBehavesLikePSet() {
    PSet<Integer> s = Empty.set();
    POrderedSet<Integer> os = Empty.orderedSet();

    Random r = new Random(123);
    for (int i = 0; i < 100000; i++) {
      int v = r.nextInt(1000);
      if (r.nextFloat() < 0.8) {
        s = s.plus(v);
        os = os.plus(v);
      } else {
        s = s.minus(v);
        os = os.minus(v);
      }
    }

    assertEquals(s, os);
  }

  public void testIterator() {
    UtilityTest.iteratorExceptions(Empty.orderedSet().iterator());
    UtilityTest.iteratorExceptions(OrderedPSet.singleton(10).iterator());
  }

  public void testUnmodifiable() {
    assertSetMutatorsThrow(OrderedPSet.singleton("value1"), "value2");
  }

  public void testChecksForNull() {
    assertSetChecksForNull(OrderedPSet.singleton("value1"), "value2");
  }

  @ParameterizedTest
  @MethodSource("org.pcollections.tests.util.CollectionHelpers#collectionElementPairCases")
  public void treePSet_hasSetSemantics(List<String> left, List<String> right) {
    assertSetSemantics(OrderedPSet.from(left), right);
  }

  @ParameterizedTest
  @MethodSource("org.pcollections.tests.util.CollectionHelpers#collectionElementPairCases")
  public void intersect_correctOrder(List<String> left, List<String> right) {
    List<String> expected = left.stream()
        .distinct()
        .filter(right::contains)
        .collect(toList());
    List<String> actual = new ArrayList<>(OrderedPSet.from(left).intersect(right));
    assertEquals(expected, actual);
  }
}
