/*
 * Copyright (c) 2008 Harold Cooper. All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

package org.pcollections.tests;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import junit.framework.TestCase;
import org.pcollections.HashTreePBag;
import org.pcollections.HashTreePMap;
import org.pcollections.MapPBag;
import org.pcollections.PBag;
import org.pcollections.PMap;

public class MapPBagTest extends TestCase {

  private static <E> MapPBag<E> emptyBag() {
    return MapPBag.empty(HashTreePMap.<E, Integer>empty());
  }

  /** Compares the behavior of java.util.ArrayList to the behavior of MapPBag. */
  public void testRandomlyAgainstJavaList() {
    PBag<Integer> pbag = emptyBag();
    List<Integer> list = new ArrayList<>();
    Random r = new Random(123);
    for (int i = 0; i < 10000; i++) {
      if (pbag.size() == 0 || r.nextBoolean()) { // add one element
        Integer v = r.nextInt(10) > 0 ? r.nextInt(100) : null;

        assertEquals(list.contains(v), pbag.contains(v));

        list.add(v);
        pbag = pbag.plus(v);
      } else if (r.nextBoolean()) { // add a collection
        List<Integer> toAdd = new ArrayList<>();
        int n = r.nextInt(5);
        for (int j = 0; j < n; j++) {
          toAdd.add(r.nextInt(10) > 0 ? r.nextInt(100) : null);
        }
        list.addAll(toAdd);
        pbag = pbag.plusAll(toAdd);
      } else { // remove one occurrence at a random list index
        int j = r.nextInt(list.size());
        Integer toRemove = list.remove(j);
        pbag = pbag.minus(toRemove);
      }

      // also try to remove a _totally_ random value:
      Integer v = r.nextInt(10) > 0 ? r.nextInt(100) : null;
      assertEquals(list.contains(v), pbag.contains(v));
      list.remove(v);
      pbag = pbag.minus(v);

      // and try out a non-Integer:
      String s = Integer.toString(r.nextInt());
      assertFalse(pbag.contains(s));
      pbag = pbag.minus(s);

      assertEquals(list.size(), pbag.size());
      UtilityTest.assertEqualsAndHash(HashTreePBag.from(list), pbag);

      UtilityTest.assertEqualsAndHash(pbag, HashTreePBag.from(pbag));
      UtilityTest.assertEqualsAndHash(HashTreePBag.empty(), pbag.minusAll(pbag));
    }
  }

  public void testMinusAllRemovesEveryOccurrence() {
    PBag<String> bag =
        MapPBag.<String>empty(HashTreePMap.empty())
            .plus("a")
            .plus("b")
            .plus("a")
            .plus("c")
            .plus("a")
            .plus("b")
            .plus("d");
    assertEquals(7, bag.size());

    PBag<String> expected = MapPBag.<String>empty(HashTreePMap.empty()).plus("c").plus("d");
    assertEquals(expected, bag.minusAll(Arrays.asList("a", "b")));
    assertEquals(expected, HashTreePBag.from(Arrays.asList("c", "d")));
  }

  public void testEmpty() {
    PMap<Integer, Integer> map = HashTreePMap.empty();
    MapPBag<Integer> empty = MapPBag.empty(map);
    assertEquals(0, empty.size());
    assertTrue(empty.isEmpty());
    assertEquals(empty, MapPBag.empty(map.plus(1, 2)));
    UtilityTest.assertEqualsAndHash(HashTreePBag.empty(), empty);
  }

  public void testIterator() {
    UtilityTest.iteratorExceptions(emptyBag().iterator());
    UtilityTest.iteratorExceptions(emptyBag().plus(1).plus(2).plus(1).iterator());
  }

  public void testMutators() {
    final MapPBag<Integer> bag = MapPBag.<Integer>empty(HashTreePMap.empty()).plus(1).plus(2);
    final Collection<Integer> collection = bag;

    assertThrows(UnsupportedOperationException.class, () -> collection.add(10));
    assertThrows(
        UnsupportedOperationException.class, () -> collection.addAll(Collections.emptyList()));
    assertThrows(UnsupportedOperationException.class, collection::clear);
    assertThrows(UnsupportedOperationException.class, () -> collection.remove(10));
    assertThrows(
        UnsupportedOperationException.class, () -> collection.removeAll(Collections.emptyList()));
    assertThrows(UnsupportedOperationException.class, () -> collection.removeIf(o -> false));
    assertThrows(
        UnsupportedOperationException.class, () -> collection.retainAll(Collections.emptyList()));

    final Iterator<Integer> iterator = bag.iterator();
    while (iterator.hasNext()) {
      iterator.next();
      assertThrows(UnsupportedOperationException.class, iterator::remove);
    }
  }

  public void testNullElements() {
    PBag<String> bag =
        MapPBag.<String>empty(HashTreePMap.empty()).plus(null).plus("x").plus(null);
    assertEquals(3, bag.size());
    assertTrue(bag.contains(null));
    assertEquals(2, countOccurrences(bag, null));

    PBag<String> minusOne = bag.minus(null);
    assertEquals(2, minusOne.size());
    assertTrue(minusOne.contains(null));

    assertEquals(
        MapPBag.<String>empty(HashTreePMap.empty()).plus("x"),
        bag.minus(null).minus(null).minus(null));
  }

  private static int countOccurrences(PBag<String> bag, String value) {
    int n = 0;
    for (String e : bag) {
      if (value == null ? e == null : value.equals(e)) {
        n++;
      }
    }
    return n;
  }
}
