/*
 * Copyright (c) 2008 Harold Cooper. All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

package org.pcollections.tests;

import static org.pcollections.tests.util.NullCheckAssertions.assertMapAllowsNullKeys;
import static org.pcollections.tests.util.NullCheckAssertions.assertMapAllowsNullValues;
import static org.pcollections.tests.util.NullCheckAssertions.assertMapForbidsNullCollections;
import static org.pcollections.tests.util.UnmodifiableAssertions.assertMapMutatorsThrow;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import junit.framework.TestCase;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

public class HashPMapTest extends TestCase {

  /** Compares the behavior of java.util.HashMap to the behavior of HashTreePMap. */
  public void testRandomlyAgainstJavaMap() {
    PMap<Integer, Integer> pmap = HashTreePMap.empty();
    Map<Integer, Integer> map = new HashMap<Integer, Integer>();
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

      UtilityTest.assertEqualsAndHash(pmap, HashTreePMap.from(pmap));
      UtilityTest.assertEqualsAndHash(HashTreePMap.empty(), pmap.minusAll(pmap.keySet()));
      UtilityTest.assertEqualsAndHash(pmap, pmap.plusAll(pmap));
    }
  }

  public void testEmpty() {
    PMap<?, ?> empty = HashTreePMap.empty();
    UtilityTest.assertEqualsAndHash(new HashMap<Object, Object>(), empty);
    assertEquals(0, empty.size());
    assertTrue(empty.isEmpty());
    for (@SuppressWarnings("unused") Object e : empty.entrySet()) fail();
  }

  public void testIterator() {
    UtilityTest.iteratorExceptions(HashTreePMap.empty().entrySet().iterator());
    UtilityTest.iteratorExceptions(HashTreePMap.singleton(10, "test").entrySet().iterator());
  }

  public void testSingleton() {
    UtilityTest.assertEqualsAndHash(
        HashTreePMap.empty().plus(10, "test"), HashTreePMap.singleton(10, "test"));
  }

  public void testUnmodifiable() {
    assertMapMutatorsThrow(HashTreePMap.empty(), "key1", "value1");
    assertMapMutatorsThrow(HashTreePMap.singleton("key1", "value1"), "key2", "value2");
  }

  public void testChecksForNull() {
    assertMapForbidsNullCollections(HashTreePMap.empty(), "key1", "value1");
    assertMapAllowsNullKeys(HashTreePMap.empty(), "key1", "value1");
    assertMapAllowsNullValues(HashTreePMap.empty(), "key1", "value1");

    assertMapForbidsNullCollections(HashTreePMap.singleton("key1", "value1"), "key2", "value2");
    assertMapAllowsNullKeys(HashTreePMap.singleton("key1", "value1"), "key2", "value2");
    assertMapAllowsNullValues(HashTreePMap.singleton("key1", "value1"), "key2", "value2");
  }
}
