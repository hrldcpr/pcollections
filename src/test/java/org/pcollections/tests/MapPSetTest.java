/*
 * Copyright (c) 2008 Harold Cooper. All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

package org.pcollections.tests;

import static org.pcollections.tests.util.CollectionHelpers.assertSetSemantics;
import static org.pcollections.tests.util.NullCheckAssertions.assertSetAllowsNullElements;
import static org.pcollections.tests.util.NullCheckAssertions.assertSetForbidsNullCollections;
import static org.pcollections.tests.util.UnmodifiableAssertions.assertSetMutatorsThrow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import junit.framework.TestCase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.MapPSet;
import org.pcollections.PMap;
import org.pcollections.PSet;

public class MapPSetTest extends TestCase {

  private static <E> MapPSet<E> emptySet() {
    return MapPSet.from(HashTreePMap.<E, Object>empty());
  }

  /** Compares the behavior of java.util.HashSet to the behavior of MapPSet. */
  public void testRandomlyAgainstJavaSet() {
    PSet<Integer> pset = emptySet();
    Set<Integer> set = new HashSet<>();
    Random r = new Random(123);
    for (int i = 0; i < 10000; i++) {
      if (pset.size() == 0 || r.nextBoolean()) { // add
        Integer v = r.nextInt(10) > 0 ? r.nextInt() : null;

        assertEquals(set.contains(v), pset.contains(v));

        set.add(v);
        pset = pset.plus(v);
      } else if (r.nextBoolean()) { // addAll
        List<Integer> toAdd = new ArrayList<>();
        int n = r.nextInt(5);
        for (int j = 0; j < n; j++) {
          toAdd.add(r.nextInt(10) > 0 ? r.nextInt() : null);
        }
        set.addAll(toAdd);
        pset = pset.plusAll(toAdd);
      } else { // remove a random element
        int j = r.nextInt(pset.size());
        for (Integer e : pset) {
          assertTrue(set.contains(e));
          assertTrue(pset.contains(e));
          if (j-- == 0) {
            set.remove(e);
            pset = pset.minus(e);
            break;
          }
        }
      }

      // also try to remove a _totally_ random value:
      Integer v = r.nextInt(10) > 0 ? r.nextInt() : null;
      assertEquals(set.contains(v), pset.contains(v));
      set.remove(v);
      pset = pset.minus(v);

      // and try out a non-Integer:
      String s = Integer.toString(r.nextInt());
      assertFalse(pset.contains(s));
      pset = pset.minus(s);

      assertEquals(set.size(), pset.size());
      UtilityTest.assertEqualsAndHash(set, pset);

      UtilityTest.assertEqualsAndHash(
          pset, MapPSet.<Integer>from(HashTreePMap.empty()).plusAll(pset));
      UtilityTest.assertEqualsAndHash(HashTreePSet.empty(), pset.minusAll(pset));
      UtilityTest.assertEqualsAndHash(pset, pset.plusAll(pset));
    }
  }

  public void testBehavesLikeHashTreePSet() {
    PSet<Integer> hashSet = HashTreePSet.empty();
    PSet<Integer> mapSet = emptySet();

    Random r = new Random(123);
    for (int i = 0; i < 100000; i++) {
      int v = r.nextInt(1000);
      if (r.nextFloat() < 0.8) {
        hashSet = hashSet.plus(v);
        mapSet = mapSet.plus(v);
      } else {
        hashSet = hashSet.minus(v);
        mapSet = mapSet.minus(v);
      }
    }

    assertEquals(hashSet, mapSet);
  }

  public void testFromFactories() {
    PMap<String, Object> map = HashTreePMap.singleton("existing", new Object());

    assertEquals(
        HashTreePSet.from(Arrays.asList("a", "b")),
        MapPSet.from(HashTreePMap.<String, Object>empty(), Arrays.asList("a", "b")));
    assertEquals(
        HashTreePSet.from(Arrays.asList("existing", "x")),
        MapPSet.from(map, "x"));
    assertEquals(HashTreePSet.from(map.keySet()), MapPSet.from(map));
  }

  public void testEmpty() {
    MapPSet<Integer> empty = emptySet();
    assertEquals(0, empty.size());
    assertTrue(empty.isEmpty());
    UtilityTest.assertEqualsAndHash(new HashSet<Integer>(), empty);
  }

  public void testIterator() {
    UtilityTest.iteratorExceptions(emptySet().iterator());
    UtilityTest.iteratorExceptions(emptySet().plus(1).plus(2).iterator());
  }

  public void testUnmodifiable() {
    assertSetMutatorsThrow(emptySet(), "value");
    assertSetMutatorsThrow(emptySet().plus("value1"), "value2");
  }

  public void testChecksForNull() {
    assertSetForbidsNullCollections(emptySet(), "value");
    assertSetAllowsNullElements(emptySet(), "value");
    assertSetForbidsNullCollections(emptySet().plus("value1"), "value2");
    assertSetAllowsNullElements(emptySet().plus("value1"), "value2");
  }

  @ParameterizedTest
  @MethodSource("org.pcollections.tests.util.CollectionHelpers#collectionElementPairCases")
  public void mapPSet_hasSetSemantics(List<String> left, List<String> right) {
    assertSetSemantics(MapPSet.<String>from(HashTreePMap.empty()).plusAll(left), right);
  }
}
