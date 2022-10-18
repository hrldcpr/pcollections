/*
 * Copyright (c) 2008 Harold Cooper. All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

package org.pcollections.tests;

import static org.pcollections.tests.util.UnmodifiableAssertions.assertMapMutatorsThrow;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import junit.framework.TestCase;
import org.pcollections.OrderedPMap;
import org.pcollections.PMap;

public class OrderedPMapTest extends TestCase {

  /** Compares the behavior of java.util.LinkedHashMap to the behavior of OrderedPMap. */
  public void testRandomlyAgainstJavaMap() {
    PMap<Integer, Integer> pmap = OrderedPMap.empty();
    Map<Integer, Integer> map = new LinkedHashMap<Integer, Integer>();
    Random r = new Random(123);
    for (int i = 0; i < 10000; i++) {
      if (pmap.size() == 0 || r.nextBoolean()) { // add
        Integer k = r.nextInt(10) > 0 ? r.nextInt() : null;
        Integer v = r.nextInt(10) > 0 ? r.nextInt() : null;

        assertEquals(map.containsKey(k), pmap.containsKey(k));
        assertEquals(map.get(k), pmap.get(k));

        map.put(k, v);
        pmap = pmap.plus(k, v);
      } else { // remove a random key
        int j = r.nextInt(pmap.size());
        for (Entry<Integer, Integer> e : pmap.entrySet()) {
          Integer k = e.getKey();

          assertTrue(map.containsKey(k));
          assertTrue(pmap.containsKey(k));
          assertEquals(map.get(k), pmap.get(k));
          assertTrue(map.entrySet().contains(e));
          assertTrue(pmap.entrySet().contains(e));
          UtilityTest.assertEqualsAndHash(pmap, pmap.plus(k, e.getValue()));

          if (j-- == 0) {
            map.remove(k);
            pmap = pmap.minus(k);
            assertFalse(pmap.entrySet().contains(e));
          }
        }
      }

      // also try to remove a _totally_ random key:
      Integer k = r.nextInt(10) > 0 ? r.nextInt() : null;
      assertEquals(map.containsKey(k), pmap.containsKey(k));
      assertEquals(map.get(k), pmap.get(k));
      map.remove(k);
      pmap = pmap.minus(k);

      // and try out a non-Integer:
      String s = Integer.toString(r.nextInt());
      assertFalse(pmap.containsKey(s));
      assertEquals(null, pmap.get(s));
      assertFalse(pmap.entrySet().contains(s));
      pmap = pmap.minus(s);

      assertEquals(map.size(), pmap.size());
      UtilityTest.assertEqualsAndHash(map, pmap);
      UtilityTest.assertEqualsAndHash(map.entrySet(), pmap.entrySet());

      UtilityTest.assertEqualsAndHash(pmap, OrderedPMap.from(pmap));
      UtilityTest.assertEqualsAndHash(OrderedPMap.empty(), pmap.minusAll(pmap.keySet()));
      UtilityTest.assertEqualsAndHash(pmap, pmap.plusAll(pmap));
    }

    final Iterator<Entry<Integer, Integer>> it = map.entrySet().iterator();
    final Iterator<Entry<Integer, Integer>> pit = pmap.entrySet().iterator();
    while (it.hasNext() && pit.hasNext()) {
      assertEquals(it.next(), pit.next());
    }
    assertEquals(it.hasNext(), pit.hasNext());
  }

  public void testEmpty() {
    PMap<?, ?> empty = OrderedPMap.empty();
    UtilityTest.assertEqualsAndHash(new LinkedHashMap<Object, Object>(), empty);
    assertEquals(0, empty.size());
    assertTrue(empty.isEmpty());
    for (@SuppressWarnings("unused") Object e : empty.entrySet()) fail();
  }

  public void testIterator() {
    UtilityTest.iteratorExceptions(OrderedPMap.empty().entrySet().iterator());
    UtilityTest.iteratorExceptions(OrderedPMap.singleton(10, "test").entrySet().iterator());
  }

  public void testSingleton() {
    UtilityTest.assertEqualsAndHash(
        OrderedPMap.empty().plus(10, "test"), OrderedPMap.singleton(10, "test"));
  }

  public void testUnmodifiable() {
    assertMapMutatorsThrow(OrderedPMap.singleton("key1", "value1"), "key2", "value2");
  }
}
