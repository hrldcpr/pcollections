/*
 * Copyright (c) 2022 Ran Ari-Gur. All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

package org.pcollections.tests;

import static org.pcollections.tests.util.NullCheckAssertions.assertMapAllowsNullValues;
import static org.pcollections.tests.util.NullCheckAssertions.assertMapForbidsNullCollections;
import static org.pcollections.tests.util.NullCheckAssertions.assertMapForbidsNullKeys;
import static org.pcollections.tests.util.UnmodifiableAssertions.assertMapMutatorsThrow;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.pcollections.PSortedSet;
import org.pcollections.TreePMap;
import org.pcollections.TreePSet;
import org.pcollections.tests.util.CompareInconsistentWithEquals;
import org.pcollections.tests.util.StringOrderComparator;

public class TreePMapTest extends TestCase {
  /**
   * An empty TreePMap&lt;Integer, String&gt; using the natural ordering; useful in cases where type
   * inference isn't clever enough to deduce the '&lt;Integer, String&gt;' on its own.
   */
  private static final TreePMap<Integer, String> EMPTY = TreePMap.empty();

  private static final Random RANDOM = new Random(123);

  private static final Comparator<Object> STRING_ORDER_COMPARATOR = StringOrderComparator.INSTANCE;

  private static final Function<Object, String> STRINGIFY = String::valueOf;

  public void testComparator() {
    // If don't specify a comparator, should get Comparator.naturalOrder():
    assertEquivalentComparator(treeMapOf().comparator(), EMPTY.comparator());

    // . . . and, descendingMap():
    assertEquivalentComparator(
        treeMapOf().descendingMap().comparator(), EMPTY.descendingMap().comparator());

    // If do specify a comparator, should get it:
    assertSame(STRING_ORDER_COMPARATOR, TreePMap.empty(STRING_ORDER_COMPARATOR).comparator());

    // . . . and, descendingMap():
    assertEquivalentComparator(
        STRING_ORDER_COMPARATOR.reversed(),
        TreePMap.empty(STRING_ORDER_COMPARATOR).descendingMap().comparator());
  }

  public void testDescendingKeySet() {
    // Rather than doing deep assertions into the key-set, we just assert that it's an instance of
    // TreePSet, that it has the right sequence, and that it has the right comparator. Beyond that,
    // we rely on TreePSetTest to test that a TreePSet with these properties will behave correctly.

    assertSame(TreePSet.class, EMPTY.descendingKeySet().getClass());
    assertSame(TreePSet.class, EMPTY.descendingMap().descendingKeySet().getClass());

    assertEquivalentState(treeMapOf().descendingKeySet(), EMPTY.descendingKeySet());
    assertEquivalentState(
        treeMapOf().descendingMap().descendingKeySet(), EMPTY.descendingMap().descendingKeySet());

    assertEquivalentState(
        treeMapOf(STRINGIFY, 1, 2, 3, 4).descendingKeySet(),
        treePMapOf(STRINGIFY, 1, 2, 3, 4).descendingKeySet());
    assertEquivalentState(
        treeMapOf(STRINGIFY, 1, 2, 3, 4).descendingMap().descendingKeySet(),
        treePMapOf(STRINGIFY, 1, 2, 3, 4).descendingMap().descendingKeySet());

    assertEquivalentState(
        treePMapOf(STRINGIFY, 1, 2, 3, 4).keySet(),
        treePMapOf(STRINGIFY, 1, 2, 3, 4).descendingMap().descendingKeySet());
  }

  public void testDescendingMap() {
    assertEquivalentState(treeMapOf().descendingMap(), EMPTY.descendingMap());
    assertEquivalentState(
        treeMapOf().descendingMap().descendingMap(), EMPTY.descendingMap().descendingMap());

    assertEquivalentState(
        treeMapOf(STRINGIFY, 1, 2, 3, 4).descendingMap(),
        treePMapOf(STRINGIFY, 1, 2, 3, 4).descendingMap());
    assertEquivalentState(
        treeMapOf(STRINGIFY, 1, 2, 3, 4).descendingMap().descendingMap(),
        treePMapOf(STRINGIFY, 1, 2, 3, 4).descendingMap().descendingMap());

    assertEquivalentState(
        treePMapOf(STRINGIFY, 1, 2, 3, 4),
        treePMapOf(STRINGIFY, 1, 2, 3, 4).descendingMap().descendingMap());

    // Note: descendingMap() differs from all the other producer methods, from a testing standpoint,
    // in that it sets a flag in the returned TreePMap that almost all other TreePMap methods have
    // to take into account in order to behave correctly. So all of the other test-methods here also
    // test different aspects of the map returned by descendingMap(), in order to test whatever
    // method they're trying to test.
  }

  public void testEmpty() {
    assertEquivalentState(new TreeMap<>(), TreePMap.empty());

    assertEquivalentState(
        new TreeMap<>(STRING_ORDER_COMPARATOR), TreePMap.empty(STRING_ORDER_COMPARATOR));

    assertThrows(NullPointerException.class, () -> TreePMap.empty(null));
  }

  public void testEntrySet() {
    final List<Subcase> subcases =
        Arrays.asList(
            new Subcase(treeMapOf(), EMPTY),
            new Subcase(treeMapOf().descendingMap(), EMPTY.descendingMap()),
            new Subcase(treeMapOf(STRINGIFY, 1, 2, 3, 4, 5), treePMapOf(STRINGIFY, 1, 2, 3, 4, 5)),
            new Subcase(
                treeMapOf(STRINGIFY, 1, 2, 3, 4, 5).descendingMap(),
                treePMapOf(STRINGIFY, 1, 2, 3, 4, 5).descendingMap()));

    for (final Subcase subcase : subcases) {
      final Set<Map.Entry<Integer, String>> expected = subcase.expected.entrySet();
      final Set<Map.Entry<Integer, String>> actual = subcase.actual.entrySet();

      assertSameSequence(expected, actual);

      final List<Map.Entry<Integer, String>> entriesToTest = new ArrayList<>();
      entriesToTest.add(entryOf(0, "0"));
      entriesToTest.add(entryOf(1, null));
      entriesToTest.addAll(expected);
      if (!expected.isEmpty()) {
        entriesToTest.add(entryOf(expected.iterator().next().getKey(), "wrong"));
      }

      for (final Map.Entry<Integer, String> entry : entriesToTest) {
        assertEquals(expected.contains(entry), actual.contains(entry));

        assertEquals(
            expected.containsAll(Arrays.asList(entry)), actual.containsAll(Arrays.asList(entry)));
      }
      assertFalse(actual.contains(null));
      assertTrue(actual.containsAll(expected));
      assertFalse(actual.containsAll(entriesToTest));

      assertTrue(actual.equals(actual));
      assertTrue(actual.equals(expected));
      assertTrue(actual.equals(new HashSet<>(expected)));
      assertTrue(actual.equals(subcase.expected.descendingMap().entrySet()));
      assertFalse(actual.equals(null));
      assertFalse(actual.equals(new Object()));
      assertFalse(actual.equals((Object) subcase.actual));
      assertFalse(actual.equals(subcase.actual.plus(0, "wrong").entrySet()));

      assertEquals(expected.hashCode(), actual.hashCode());

      assertEquals(expected.isEmpty(), actual.isEmpty());

      final Iterator<Map.Entry<Integer, String>> iterator = actual.iterator();
      while (iterator.hasNext()) {
        iterator.next();
        assertThrows(UnsupportedOperationException.class, () -> iterator.remove());
      }
      assertThrows(NoSuchElementException.class, () -> iterator.next());

      assertEquals(expected.size(), actual.size());

      assertEquals(Arrays.asList(expected.toArray()), Arrays.asList(actual.toArray()));

      assertEquals(expected.toString(), actual.toString());

      // assert that all mutators throw UnsupportedOperationException:
      assertThrows(UnsupportedOperationException.class, () -> actual.add(null));
      assertThrows(UnsupportedOperationException.class, () -> actual.addAll(Arrays.asList()));
      assertThrows(UnsupportedOperationException.class, () -> actual.clear());
      assertThrows(UnsupportedOperationException.class, () -> actual.remove(null));
      assertThrows(UnsupportedOperationException.class, () -> actual.removeAll(null));
      assertThrows(UnsupportedOperationException.class, () -> actual.removeIf(null));
      assertThrows(UnsupportedOperationException.class, () -> actual.retainAll(null));
      if (!expected.isEmpty()) {
        assertThrows(
            UnsupportedOperationException.class, () -> actual.iterator().next().setValue("wrong"));
      }
    }
  }

  /**
   * Test that TreePMap.equals(...) returns true for each of various maps that have the same size
   * and mappings, and that it returns false for each of various objects that are not such map.
   */
  public void testEquals() {
    final List<Subcase> subcases =
        Arrays.asList(
            new Subcase(treeMapOf(), EMPTY),
            new Subcase(treeMapOf().descendingMap(), EMPTY.descendingMap()),
            new Subcase(treeMapOf(STRINGIFY, 1, 2, 3, 4, 5), treePMapOf(STRINGIFY, 1, 2, 3, 4, 5)),
            new Subcase(
                treeMapOf(STRINGIFY, 1, 2, 3, 4, 5).descendingMap(),
                treePMapOf(STRINGIFY, 1, 2, 3, 4, 5).descendingMap()));

    for (final Subcase subcase : subcases) {
      final NavigableMap<Integer, String> expected = subcase.expected;
      final TreePMap<Integer, String> actual = subcase.actual;

      // must return false for anything other than a map:
      assertFalse(actual.equals(null));
      assertFalse(actual.equals(new Object()));
      assertFalse(actual.equals((Object) "foo"));
      assertFalse(actual.equals((Object) actual.keySet()));
      assertFalse(actual.equals((Object) actual.entrySet()));

      // must return true for a map with the same mappings:
      assertTrue(actual.equals(actual));
      assertTrue(actual.equals(expected));
      assertTrue(actual.equals((Object) new LinkedHashMap<>(expected)));
      // . . . even if the iteration order is different:
      assertTrue(actual.equals((Object) new HashMap<>(expected)));
      assertTrue(actual.equals(actual.descendingMap()));
      assertTrue(actual.equals(expected.descendingMap()));

      // must return false for a map that's too small:
      if (!actual.isEmpty()) {
        final TreeMap<Integer, String> tooSmall = new TreeMap<>(expected);
        tooSmall.pollFirstEntry();
        assertFalse(actual.equals((Object) tooSmall));
      }

      // must return false for a map that's too big:
      {
        final HashMap<Integer, String> tooBig = new HashMap<>(expected);
        tooBig.put(findSomeNonKey(actual), "extra");
        assertFalse(actual.equals((Object) tooBig));
      }

      // must return false for a map that has a wrong mapping:
      if (!actual.isEmpty()) {
        final TreeMap<Integer, String> wrong = new TreeMap<>(expected);
        wrong.put(wrong.firstKey(), "wrong");
        assertFalse(actual.equals((Object) wrong));
      }
    }
  }

  public void testFrom() {
    // the overload that doesn't take an explicit comparator (uses natural ordering):

    {
      final Map<Integer, String> map = new HashMap<>();
      for (int i = 0; i < 20; ++i) {
        map.put(RANDOM.nextInt(), randomString());
      }

      assertEquivalentState(new TreeMap<>(map), TreePMap.from(map));
    }

    assertThrows(NullPointerException.class, () -> TreePMap.from(null));

    assertThrows(
        NullPointerException.class, () -> TreePMap.from(Collections.singletonMap(null, "foo")));

    assertEquals(
        TreePMap.singleton("foo", null), TreePMap.from(Collections.singletonMap("foo", null)));

    // the overload that does take an explicit comparator:

    {
      final Map<Integer, String> map = new HashMap<>();
      for (int i = 0; i < 20; ++i) {
        map.put(RANDOM.nextInt(), randomString());
      }

      final TreeMap<Integer, String> expected = new TreeMap<>(STRING_ORDER_COMPARATOR);
      expected.putAll(map);

      assertEquivalentState(expected, TreePMap.from(STRING_ORDER_COMPARATOR, map));
    }

    assertThrows(NullPointerException.class, () -> TreePMap.from(null, new HashMap<>()));

    assertThrows(NullPointerException.class, () -> TreePMap.from(STRING_ORDER_COMPARATOR, null));

    assertThrows(
        NullPointerException.class,
        () -> TreePMap.from(STRING_ORDER_COMPARATOR, Collections.singletonMap(null, "foo")));

    assertEquals(
        TreePMap.singleton("foo", null),
        TreePMap.from(STRING_ORDER_COMPARATOR, Collections.singletonMap("foo", null)));
  }

  public void testFromSortedMap() {
    // if SortedMap has null comparator, use natural-ordering comparator:
    {
      final TreeMap<Integer, String> expected = new TreeMap<>();
      for (int i = 0; i < 20; ++i) {
        expected.put(RANDOM.nextInt(), randomString());
      }

      assertEquivalentState(expected, TreePMap.fromSortedMap(expected));
    }

    // if SortedMap has explicit comparator, use it:
    {
      final TreeMap<Integer, String> expected = new TreeMap<>(STRING_ORDER_COMPARATOR);
      for (int i = 0; i < 20; ++i) {
        expected.put(RANDOM.nextInt(), randomString());
      }

      assertEquivalentState(expected, TreePMap.fromSortedMap(expected));
    }

    // if SortedMap is actually already a TreePMap, just use it as-is:
    {
      TreePMap<Integer, String> expected = TreePMap.empty(STRING_ORDER_COMPARATOR);
      for (int i = 0; i < 20; ++i) {
        expected = expected.plus(RANDOM.nextInt(), randomString());
      }

      assertSame(expected, TreePMap.fromSortedMap(expected));
    }

    // if SortedMap is actually null, complain:
    assertThrows(NullPointerException.class, () -> TreePMap.fromSortedMap(null));

    // if SortedMap contains null key, complain:
    {
      final TreeMap<Integer, String> containsNullKey = new TreeMap<>(STRING_ORDER_COMPARATOR);
      containsNullKey.put(RANDOM.nextInt(), randomString());
      containsNullKey.put(null, randomString());

      assertThrows(NullPointerException.class, () -> TreePMap.fromSortedMap(containsNullKey));
    }

    // if SortedMap contains null value, it should work
    {
      final TreeMap<Integer, String> containsNullValue = new TreeMap<>(STRING_ORDER_COMPARATOR);
      containsNullValue.put(RANDOM.nextInt(), randomString());
      containsNullValue.put(RANDOM.nextInt(), null);

      assertEquals(containsNullValue, TreePMap.fromSortedMap(containsNullValue));
    }
  }

  public void testHashCode() {
    assertEquals(treeMapOf().hashCode(), EMPTY.hashCode());
    assertEquals(treeMapOf().descendingMap().hashCode(), EMPTY.descendingMap().hashCode());

    assertEquals(
        treeMapOf(STRINGIFY, 1, 2, 3, 4).hashCode(), treePMapOf(STRINGIFY, 1, 2, 3, 4).hashCode());
    assertEquals(
        treeMapOf(STRINGIFY, 1, 2, 3, 4).descendingMap().hashCode(),
        treePMapOf(STRINGIFY, 1, 2, 3, 4).descendingMap().hashCode());
  }

  /**
   * Verifies that, when comparison is not consistent with equals, that TreePMap honors the former
   * rather than the latter.
   */
  public void testInconsistentWithEquals() {
    final TreeMap<CompareInconsistentWithEquals, String> expected = new TreeMap<>();

    TreePMap<CompareInconsistentWithEquals, String> actual = TreePMap.empty();

    for (int i = 0; i < 20; ++i) {
      final int randomEq1 = RANDOM.nextInt();
      final int randomEq2 = RANDOM.nextInt();
      final int randomComp1 = RANDOM.nextInt();
      final int randomComp2 = RANDOM.nextInt();

      final List<CompareInconsistentWithEquals> keysToPut =
          Arrays.asList(
              new CompareInconsistentWithEquals(randomEq1, randomComp1),
              new CompareInconsistentWithEquals(randomEq1, randomComp2),
              new CompareInconsistentWithEquals(randomEq2, randomComp1),
              new CompareInconsistentWithEquals(randomEq2, randomComp2));

      for (final CompareInconsistentWithEquals key : keysToPut) {
        final String value = key.toString();
        expected.put(key, value);
        actual = actual.plus(key, value);
      }
    }

    assertEquals(expected.size(), actual.size());

    final Iterator<Map.Entry<CompareInconsistentWithEquals, String>> actualIterator =
        actual.entrySet().iterator();
    for (final Map.Entry<CompareInconsistentWithEquals, String> expectedEntry :
        expected.entrySet()) {
      final Map.Entry<CompareInconsistentWithEquals, String> actualEntry = actualIterator.next();
      assertSame(expectedEntry.getKey(), actualEntry.getKey());
      assertEquals(expectedEntry.getValue(), actualEntry.getValue());
    }
  }

  public void testIsEmpty() {
    assertTrue(EMPTY.isEmpty());
    assertTrue(EMPTY.descendingMap().isEmpty());

    assertFalse(treePMapOf(STRINGIFY, 1, 2, 3, 4).isEmpty());
    assertFalse(treePMapOf(STRINGIFY, 1, 2, 3, 4).descendingMap().isEmpty());
  }

  public void testKeySet() {
    // Rather than doing deep assertions into the key-set, we just assert that it's an instance of
    // TreePSet, that it has the right sequence, and that it has the right comparator. Beyond that,
    // we rely on TreePSetTest to test that a TreePSet with these properties will behave correctly.

    assertSame(TreePSet.class, EMPTY.keySet().getClass());
    assertSame(TreePSet.class, EMPTY.descendingMap().keySet().getClass());

    assertEquivalentState(treeMapOf().navigableKeySet(), EMPTY.keySet());
    assertEquivalentState(
        treeMapOf().descendingMap().navigableKeySet(), EMPTY.descendingMap().keySet());

    assertEquivalentState(
        treeMapOf(STRINGIFY, 1, 2, 3, 4).navigableKeySet(),
        treePMapOf(STRINGIFY, 1, 2, 3, 4).keySet());
    assertEquivalentState(
        treeMapOf(STRINGIFY, 1, 2, 3, 4).descendingMap().navigableKeySet(),
        treePMapOf(STRINGIFY, 1, 2, 3, 4).descendingMap().keySet());
  }

  /**
   * This test ensures that we cover all the different cases in KVTree.join (meaning, all the
   * different relative heights of left.left, left.right, right.left, and right.right that have to
   * be handled in different ways), because some of them have low probability of occurring randomly.
   */
  public void testKVTreeJoin() {
    /** a perfect tree of height 3 */
    final TreePMap<Integer, String> oneToSeven = treePMapOf(STRINGIFY, 1, 2, 3, 4, 5, 6, 7);
    /** a perfect tree of height 4 */
    final TreePMap<Integer, String> oneToFifteen =
        treePMapOf(STRINGIFY, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);

    // case where left.height < right.height - 2:
    assertEquivalentState(
        treeMapOf(STRINGIFY, 8, 9, 10, 11, 12, 13, 14, 15), oneToFifteen.tailMap(8, true));

    // case where left.height == right.height - 2 and right.left.height <= right.right.height:
    assertEquivalentState(treeMapOf(STRINGIFY, 4, 6, 7), oneToSeven.minus(5).tailMap(4, true));

    // case where left.height == right.height - 2 and right.left.height > right.right.height:
    assertEquivalentState(treeMapOf(STRINGIFY, 4, 5, 6), oneToSeven.minus(7).tailMap(4, true));

    // skip case where |left.height - right.height| <= 1 -- this happens naturally all the time
    // (including in this method), no special coverage needed

    // case where left.height == right.height + 2 and left.left.height < left.right.height:
    assertEquivalentState(treeMapOf(STRINGIFY, 2, 3, 4), oneToSeven.minus(1).headMap(4, true));

    // case where left.height == right.height + 2 and left.left.height >= left.right.height:
    assertEquivalentState(treeMapOf(STRINGIFY, 1, 2, 4), oneToSeven.minus(3).headMap(4, true));

    // case where left.height > right.height + 2:
    assertEquivalentState(
        treeMapOf(STRINGIFY, 1, 2, 3, 4, 5, 6, 7, 8), oneToFifteen.headMap(8, true));
  }

  /** Perform a few simple validations on a largish random map (just as a sanity-check). */
  public void testLargishRandomMap() {
    final TreeMap<Integer, String> expected = new TreeMap<>();
    TreePMap<Integer, String> actual = TreePMap.empty();

    for (int i = 0; i < 20_000; ++i) {
      final Integer keyToPut = RANDOM.nextInt();
      final String valueToPut = randomString();
      expected.put(keyToPut, valueToPut);
      actual = actual.plus(keyToPut, valueToPut);
    }

    assertEquivalentState(expected, actual);
    assertEquivalentState(expected.descendingMap(), actual.descendingMap());

    assertTrue(actual.entrySet().containsAll(expected.entrySet()));

    {
      final int a = RANDOM.nextInt();
      final int b = RANDOM.nextInt();

      final int from = Math.min(a, b);
      final int to = Math.max(a, b);

      assertEquivalentState(expected.subMap(from, to), actual.subMap(from, to));
      assertEquivalentState(
          expected.descendingMap().subMap(to, from), actual.descendingMap().subMap(to, from));
    }
  }

  public void testMinusAll() {
    assertEquivalentState(EMPTY, EMPTY.minusAll(Arrays.asList()));

    assertEquivalentState(EMPTY, EMPTY.minusAll(Arrays.asList(1, 2, 3, 4, 5)));

    assertEquivalentState(
        EMPTY, treePMapOf(STRINGIFY, 1, 2, 3, 4, 5).minusAll(Arrays.asList(1, 2, 3, 4, 5)));

    assertEquivalentState(
        treePMapOf(STRINGIFY, 1, 3, 5),
        treePMapOf(STRINGIFY, 1, 2, 3, 4, 5).minusAll(Arrays.asList(0, 2, 4, 6)));

    assertEquivalentState(EMPTY.descendingMap(), EMPTY.descendingMap().minusAll(Arrays.asList()));

    assertEquivalentState(
        EMPTY.descendingMap(), EMPTY.descendingMap().minusAll(Arrays.asList(1, 2, 3, 4, 5)));

    assertEquivalentState(
        EMPTY.descendingMap(),
        treePMapOf(STRINGIFY, 1, 2, 3, 4, 5)
            .descendingMap()
            .minusAll(Arrays.asList(1, 2, 3, 4, 5)));

    assertEquivalentState(
        treePMapOf(STRINGIFY, 1, 3, 5).descendingMap(),
        treePMapOf(STRINGIFY, 1, 2, 3, 4, 5).descendingMap().minusAll(Arrays.asList(0, 2, 4, 6)));

    assertThrows(NullPointerException.class, () -> EMPTY.minusAll(null));
    assertThrows(NullPointerException.class, () -> EMPTY.minusAll(Arrays.asList(0, null)));
  }

  public void testMinusFirstEntry() {
    assertThrows(NoSuchElementException.class, () -> EMPTY.minusFirstEntry());

    assertEquivalentState(
        treePMapOf(STRINGIFY, 2, 3, 4, 5), treePMapOf(STRINGIFY, 1, 2, 3, 4, 5).minusFirstEntry());

    assertEquivalentState(
        treePMapOf(STRINGIFY, 1, 2, 3, 4).descendingMap(),
        treePMapOf(STRINGIFY, 1, 2, 3, 4, 5).descendingMap().minusFirstEntry());
  }

  public void testMinusLastEntry() {
    assertThrows(NoSuchElementException.class, () -> EMPTY.minusLastEntry());

    assertEquivalentState(
        treePMapOf(STRINGIFY, 1, 2, 3, 4), treePMapOf(STRINGIFY, 1, 2, 3, 4, 5).minusLastEntry());

    assertEquivalentState(
        treePMapOf(STRINGIFY, 2, 3, 4, 5).descendingMap(),
        treePMapOf(STRINGIFY, 1, 2, 3, 4, 5).descendingMap().minusLastEntry());
  }

  /**
   * Test that clear(), compute(...), computeIfAbsent(...), computeIfPresent(...), merge(...),
   * pollFirstEntry(), pollLastEntry(), put(...), putAll(...), remove(...), replace(...), and
   * replaceAll(...) all throw UnsupportedOperationException.
   */
  @SuppressWarnings("deprecation")
  public void testMutators() {
    final TreePMap<Integer, String> map = treePMapOf(STRINGIFY, 1, 2, 3, 4);

    assertThrows(UnsupportedOperationException.class, () -> map.clear());
    assertThrows(UnsupportedOperationException.class, () -> map.compute(10, (i, s) -> "10"));
    assertThrows(UnsupportedOperationException.class, () -> map.computeIfAbsent(10, i -> "10"));
    assertThrows(UnsupportedOperationException.class, () -> map.computeIfPresent(10, (i, s) -> s));
    assertThrows(UnsupportedOperationException.class, () -> map.merge(10, "10", (s1, s2) -> s2));
    assertThrows(UnsupportedOperationException.class, () -> map.pollFirstEntry());
    assertThrows(UnsupportedOperationException.class, () -> map.pollLastEntry());
    assertThrows(UnsupportedOperationException.class, () -> map.put(10, "10"));
    assertThrows(UnsupportedOperationException.class, () -> map.putAll(Collections.emptyMap()));
    assertThrows(UnsupportedOperationException.class, () -> map.putIfAbsent(10, "10"));
    assertThrows(UnsupportedOperationException.class, () -> map.remove(10));
    assertThrows(UnsupportedOperationException.class, () -> map.replace(10, "10"));
    assertThrows(UnsupportedOperationException.class, () -> map.replaceAll((i, s) -> s));
  }

  public void testNavigableKeySet() {
    // Rather than doing deep assertions into the key-set, we just assert that it's an instance of
    // TreePSet, that it has the right sequence, and that it has the right comparator. Beyond that,
    // we rely on TreePSetTest to test that a TreePSet with these properties will behave correctly.

    assertSame(TreePSet.class, EMPTY.navigableKeySet().getClass());
    assertSame(TreePSet.class, EMPTY.descendingMap().navigableKeySet().getClass());

    assertEquivalentState(treeMapOf().navigableKeySet(), EMPTY.navigableKeySet());
    assertEquivalentState(
        treeMapOf().descendingMap().navigableKeySet(), EMPTY.descendingMap().navigableKeySet());

    assertEquivalentState(
        treeMapOf(STRINGIFY, 1, 2, 3, 4).navigableKeySet(),
        treePMapOf(STRINGIFY, 1, 2, 3, 4).navigableKeySet());
    assertEquivalentState(
        treeMapOf(STRINGIFY, 1, 2, 3, 4).descendingMap().navigableKeySet(),
        treePMapOf(STRINGIFY, 1, 2, 3, 4).descendingMap().navigableKeySet());
  }

  public void testPlusAndMinus() {
    final List<Subcase> subcases =
        Arrays.asList(
            new Subcase(treeMapOf(), EMPTY),
            new Subcase(treeMapOf().descendingMap(), EMPTY.descendingMap()),
            new Subcase(treeMapOf(STRINGIFY, 1, 2, 3, 4, 5), treePMapOf(STRINGIFY, 1, 2, 3, 4, 5)),
            new Subcase(
                treeMapOf(STRINGIFY, 1, 2, 3, 4, 5).descendingMap(),
                treePMapOf(STRINGIFY, 1, 2, 3, 4, 5).descendingMap()));

    for (final Subcase subcase : subcases) {
      final NavigableMap<Integer, String> expected = subcase.expected;
      final TreePMap<Integer, String> actual = subcase.actual;

      final ArrayList<Integer> keysToTest = new ArrayList<>();
      keysToTest.add(0);
      for (final int i : expected.keySet()) {
        keysToTest.add(i - 1);
        keysToTest.add(i);
        keysToTest.add(i + 1);
      }

      for (final Integer key : keysToTest) {
        final String originalValue = expected.get(key);

        expected.put(key, "x");
        assertEquivalentState(expected, actual.plus(key, "x"));

        expected.remove(key);
        assertEquivalentState(expected, actual.minus(key));

        if (originalValue != null) {
          expected.put(key, originalValue);
        }
      }
    }
  }

  public void testPlusAll() {
    assertEquivalentState(EMPTY, EMPTY.plusAll(Collections.emptyMap()));
    assertEquivalentState(
        EMPTY.descendingMap(), EMPTY.descendingMap().plusAll(Collections.emptyMap()));

    assertEquivalentState(
        treePMapOf(STRINGIFY, 1, 2, 3, 4, 5), EMPTY.plusAll(treeMapOf(STRINGIFY, 1, 2, 3, 4, 5)));
    assertEquivalentState(
        treePMapOf(STRINGIFY, 1, 2, 3, 4, 5).descendingMap(),
        EMPTY.descendingMap().plusAll(treeMapOf(STRINGIFY, 1, 2, 3, 4, 5)));

    assertEquivalentState(
        treePMapOf(STRINGIFY, 1, 2, 3, 4, 5),
        treePMapOf(STRINGIFY, 1, 2, 3, 4, 5).plusAll(Collections.emptyMap()));
    assertEquivalentState(
        treePMapOf(STRINGIFY, 1, 2, 3, 4, 5).descendingMap(),
        treePMapOf(STRINGIFY, 1, 2, 3, 4, 5).descendingMap().plusAll(Collections.emptyMap()));

    assertEquivalentState(
        treePMapOf(STRINGIFY, 1, 2, 3, 4, 5),
        treePMapOf(STRINGIFY, 1, 3, 5).plusAll(treeMapOf(STRINGIFY, 2, 3, 4)));
    assertEquivalentState(
        treePMapOf(STRINGIFY, 1, 2, 3, 4, 5).descendingMap(),
        treePMapOf(STRINGIFY, 1, 3, 5).descendingMap().plusAll(treeMapOf(STRINGIFY, 2, 3, 4)));

    final Function<Integer, String> evenOrStringify = (i -> i % 2 == 0 ? "even" : i.toString());

    assertEquivalentState(
        treePMapOf(evenOrStringify, 1, 2, 3, 4, 5),
        treePMapOf(STRINGIFY, 1, 2, 3, 4, 5).plusAll(treeMapOf(evenOrStringify, 2, 3, 4)));
    assertEquivalentState(
        treePMapOf(evenOrStringify, 1, 2, 3, 4, 5).descendingMap(),
        treePMapOf(STRINGIFY, 1, 2, 3, 4, 5)
            .descendingMap()
            .plusAll(treeMapOf(evenOrStringify, 2, 3, 4)));

    assertThrows(NullPointerException.class, () -> EMPTY.plusAll(null));
    assertThrows(
        NullPointerException.class, () -> EMPTY.plusAll(Collections.singletonMap(null, "foo")));
    assertEquals(TreePMap.singleton(3, null), EMPTY.plusAll(Collections.singletonMap(3, null)));
  }

  /**
   * Test headMap(), subMap(), and tailMap() -- both the overloads with the 'isInclusive'
   * parameter(s) and the overloads without.
   */
  public void testRanges() {
    final List<Subcase> subcases =
        Arrays.asList(
            new Subcase(treeMapOf(), EMPTY),
            new Subcase(treeMapOf().descendingMap(), EMPTY.descendingMap()),
            new Subcase(treeMapOf(STRINGIFY, 1, 2, 3, 4, 5), treePMapOf(STRINGIFY, 1, 2, 3, 4, 5)),
            new Subcase(
                treeMapOf(STRINGIFY, 1, 2, 3, 4, 5).descendingMap(),
                treePMapOf(STRINGIFY, 1, 2, 3, 4, 5).descendingMap()));

    for (final Subcase subcase : subcases) {
      final NavigableMap<Integer, String> expected = subcase.expected;
      final TreePMap<Integer, String> actual = subcase.actual;

      final ArrayList<Integer> bounds = new ArrayList<>();
      bounds.add(0);
      for (final int i : expected.keySet()) {
        bounds.add(i - 1);
        bounds.add(i);
        bounds.add(i + 1);
      }

      for (final int to : bounds) {
        assertEquivalentState(expected.headMap(to), actual.headMap(to));
        assertEquivalentState(expected.headMap(to, true), actual.headMap(to, true));
        assertEquivalentState(expected.headMap(to, false), actual.headMap(to, false));

        for (final int from : bounds) {
          if (actual.comparator().compare(from, to) <= 0) {
            assertEquivalentState(expected.subMap(from, to), actual.subMap(from, to));
            assertEquivalentState(
                expected.subMap(from, true, to, true), actual.subMap(from, true, to, true));
            assertEquivalentState(
                expected.subMap(from, true, to, false), actual.subMap(from, true, to, false));
            assertEquivalentState(
                expected.subMap(from, false, to, true), actual.subMap(from, false, to, true));
            assertEquivalentState(
                expected.subMap(from, false, to, false), actual.subMap(from, false, to, false));
          } else {
            assertThrows(IllegalArgumentException.class, () -> actual.subMap(from, to));
            assertThrows(IllegalArgumentException.class, () -> actual.subMap(from, true, to, true));
            assertThrows(
                IllegalArgumentException.class, () -> actual.subMap(from, true, to, false));
            assertThrows(
                IllegalArgumentException.class, () -> actual.subMap(from, false, to, true));
            assertThrows(
                IllegalArgumentException.class, () -> actual.subMap(from, false, to, false));
          }
        }
      }

      for (final int from : bounds) {
        assertEquivalentState(expected.tailMap(from), actual.tailMap(from));
        assertEquivalentState(expected.tailMap(from, true), actual.tailMap(from, true));
        assertEquivalentState(expected.tailMap(from, false), actual.tailMap(from, false));
      }

      assertThrows(NullPointerException.class, () -> actual.headMap(null));
      assertThrows(NullPointerException.class, () -> actual.headMap(null, true));
      assertThrows(NullPointerException.class, () -> actual.subMap(null, 0));
      assertThrows(NullPointerException.class, () -> actual.subMap(null, true, 0, true));
      assertThrows(NullPointerException.class, () -> actual.subMap(0, null));
      assertThrows(NullPointerException.class, () -> actual.subMap(0, true, null, true));
      assertThrows(NullPointerException.class, () -> actual.tailMap(null));
      assertThrows(NullPointerException.class, () -> actual.tailMap(null, true));
    }
  }

  /**
   * Test ceilingEntry(...), ceilingKey(...), containsKey(...), containsValue(), firstEntry(),
   * firstKey(), floorEntry(...), floorKey(...), get(...), getOrDefault(...), higherEntry(...),
   * higherKey(...), lastEntry(), lastKey(), lowerEntry(...), and lowerKey(...), by validating that
   * they give the same result for a TreePMap as for a TreeMap.
   */
  public void testSearchers() {
    final List<Subcase> subcases =
        Arrays.asList(
            new Subcase(treeMapOf(), EMPTY),
            new Subcase(treeMapOf().descendingMap(), EMPTY.descendingMap()),
            new Subcase(treeMapOf(STRINGIFY, 1, 2, 3, 4, 5), treePMapOf(STRINGIFY, 1, 2, 3, 4, 5)),
            new Subcase(
                treeMapOf(STRINGIFY, 1, 2, 3, 4, 5).descendingMap(),
                treePMapOf(STRINGIFY, 1, 2, 3, 4, 5).descendingMap()),
            new Subcase(treeMapOf(STRINGIFY, 1, 3, 5, 7, 9), treePMapOf(STRINGIFY, 1, 3, 5, 7, 9)),
            new Subcase(
                treeMapOf(STRINGIFY, 1, 3, 5, 7, 9).descendingMap(),
                treePMapOf(STRINGIFY, 1, 3, 5, 7, 9).descendingMap()));

    for (final Subcase subcase : subcases) {
      final NavigableMap<Integer, String> expected = subcase.expected;
      final TreePMap<Integer, String> actual = subcase.actual;

      // ensure that we have an key to test even if expected is empty:
      final Iterable<Integer> keysToTest =
          expected.isEmpty() ? Arrays.asList(0) : expected.keySet();

      for (final int key : keysToTest) {
        for (final int arg : new int[] {key - 1, key, key + 1}) {
          assertEquals(expected.ceilingEntry(arg), actual.ceilingEntry(arg));
          assertEquals(expected.ceilingKey(arg), actual.ceilingKey(arg));
          assertEquals(expected.containsKey(arg), actual.containsKey(arg));
          assertEquals(expected.floorEntry(arg), actual.floorEntry(arg));
          assertEquals(expected.floorKey(arg), actual.floorKey(arg));
          assertEquals(expected.get(arg), actual.get(arg));
          assertEquals(expected.getOrDefault(arg, "default"), actual.getOrDefault(arg, "default"));
          assertEquals(expected.higherEntry(arg), actual.higherEntry(arg));
          assertEquals(expected.higherKey(arg), actual.higherKey(arg));
          assertEquals(expected.lowerEntry(arg), actual.lowerEntry(arg));
          assertEquals(expected.lowerKey(arg), actual.lowerKey(arg));
        }
      }

      for (final String value : expected.values()) {
        assertTrue(actual.containsValue(value));
      }
      assertFalse(actual.containsValue("wrong"));

      if (expected.isEmpty()) {
        assertThrows(NoSuchElementException.class, actual::firstEntry);
        assertThrows(NoSuchElementException.class, actual::firstKey);
        assertThrows(NoSuchElementException.class, actual::lastEntry);
        assertThrows(NoSuchElementException.class, actual::lastKey);
      } else {
        assertEquals(expected.firstEntry(), actual.firstEntry());
        assertEquals(expected.firstKey(), actual.firstKey());
        assertEquals(expected.lastEntry(), actual.lastEntry());
        assertEquals(expected.lastKey(), actual.lastKey());
      }
    }
  }

  public void testSerializeAndDeserialize() throws Exception {
    assertEquivalentState(
        treePMapOf(STRINGIFY, 1, 2, 3, 4),
        serializeAndDeserialize(treePMapOf(STRINGIFY, 1, 2, 3, 4)));

    assertEquivalentState(
        treePMapOf(STRINGIFY, 1, 2, 3, 4).descendingMap(),
        serializeAndDeserialize(treePMapOf(STRINGIFY, 1, 2, 3, 4).descendingMap()));

    assertEquivalentState(
        treePMapOf(STRING_ORDER_COMPARATOR, STRINGIFY, 1, 2, 3, 4),
        serializeAndDeserialize(treePMapOf(STRING_ORDER_COMPARATOR, STRINGIFY, 1, 2, 3, 4)));

    assertEquivalentState(
        treePMapOf(STRING_ORDER_COMPARATOR, STRINGIFY, 1, 2, 3, 4).descendingMap(),
        serializeAndDeserialize(
            treePMapOf(STRING_ORDER_COMPARATOR, STRINGIFY, 1, 2, 3, 4).descendingMap()));
  }

  public void testSerializationAndDeserializationOfEmpty() throws Exception {
    assertTrue(serializeAndDeserialize(TreePMap.empty()).isEmpty());
  }

  public void testSingleton() {
    // the overload that doesn't take an explicit comparator (uses natural ordering):

    assertEquivalentState(
        new TreeMap<>(Collections.singletonMap(3, "foo")), TreePMap.singleton(3, "foo"));

    assertThrows(NullPointerException.class, () -> TreePMap.singleton(null, "foo"));
    assertEquals(EMPTY.plus(3, null), TreePMap.singleton(3, null));

    // the overload that does take an explicit comparator:

    {
      final TreeMap<Integer, String> expected = new TreeMap<>(STRING_ORDER_COMPARATOR);
      expected.put(17, "yes");

      assertEquivalentState(expected, TreePMap.singleton(STRING_ORDER_COMPARATOR, 17, "yes"));
    }

    assertThrows(NullPointerException.class, () -> TreePMap.singleton(null, 3, "foo"));

    assertThrows(
        NullPointerException.class, () -> TreePMap.singleton(STRING_ORDER_COMPARATOR, null, "foo"));
    assertEquals(
        TreePMap.<String, String>empty().plus("foo", null),
        TreePMap.singleton(STRING_ORDER_COMPARATOR, "foo", null));
  }

  public void testSize() {
    assertEquals(0, EMPTY.size());
    assertEquals(0, EMPTY.descendingMap().size());

    assertEquals(4, treePMapOf(STRINGIFY, 1, 2, 3, 4).size());
    assertEquals(4, treePMapOf(STRINGIFY, 1, 2, 3, 4).descendingMap().size());
  }

  public void testToString() {
    // empty map:
    assertEquals("{}", EMPTY.toString());
    assertEquals("{}", EMPTY.descendingMap().toString());

    // singleton map -- no commas:
    assertEquals("{1=1}", treePMapOf(STRINGIFY, 1).toString());
    assertEquals("{1=1}", treePMapOf(STRINGIFY, 1).descendingMap().toString());

    // map with multiple mappings -- separated with comma + space, and order matters:
    assertEquals("{1=1, 2=2, 3=3, 4=4, 5=5}", treePMapOf(STRINGIFY, 1, 2, 3, 4, 5).toString());
    assertEquals(
        "{5=5, 4=4, 3=3, 2=2, 1=1}",
        treePMapOf(STRINGIFY, 1, 2, 3, 4, 5).descendingMap().toString());
  }

  public void testToTreePMap() {
    // the overload that doesn't take an explicit comparator (uses natural ordering) or explicit
    // merge function (throws IllegalStateException on duplicate keys):

    {
      final Map<Integer, String> map = new HashMap<>();
      for (int i = 0; i < 20; ++i) {
        map.put(RANDOM.nextInt(), randomString());
      }

      assertEquivalentState(
          new TreeMap<>(map),
          map.entrySet().parallelStream()
              .collect(TreePMap.toTreePMap(Map.Entry::getKey, Map.Entry::getValue)));

      final SortedMap<Integer, String> nullValues = new TreeMap<>(map);
      for (Integer k : nullValues.keySet()) { // Collectors.toMap doesn't support null values
        nullValues.put(k, null);
      }
      final TreePMap<Integer, String> pNullValues =
          map.entrySet().parallelStream()
              .collect(TreePMap.toTreePMap(Map.Entry::getKey, entry -> null));
      assertEquivalentState(nullValues, pNullValues);

      assertThrows(
          NullPointerException.class,
          () ->
              map.entrySet().parallelStream()
                  .collect(TreePMap.toTreePMap(entry -> null, Map.Entry::getValue)));
      assertThrows(
          IllegalStateException.class,
          () ->
              map.entrySet().parallelStream()
                  .collect(TreePMap.toTreePMap(entry -> "duplicate key", Map.Entry::getValue)));
    }

    assertThrows(NullPointerException.class, () -> TreePMap.toTreePMap(null, Function.identity()));
    assertThrows(NullPointerException.class, () -> TreePMap.toTreePMap(Function.identity(), null));

    // the overload that takes an explicit comparator, but not an explicit merge function (throws
    // IllegalStateException on duplicate keys):

    {
      final Map<Integer, String> map = new HashMap<>();
      for (int i = 0; i < 20; ++i) {
        map.put(RANDOM.nextInt(), randomString());
      }

      final TreeMap<Integer, String> expected = new TreeMap<>(STRING_ORDER_COMPARATOR);
      expected.putAll(map);

      assertEquivalentState(
          expected,
          map.entrySet().parallelStream()
              .collect(
                  TreePMap.toTreePMap(
                      STRING_ORDER_COMPARATOR, Map.Entry::getKey, Map.Entry::getValue)));

      final SortedMap<Integer, String> nullValues = new TreeMap<>(STRING_ORDER_COMPARATOR);
      nullValues.putAll(map);
      for (Integer k : nullValues.keySet()) { // Collectors.toMap doesn't support null values
        nullValues.put(k, null);
      }
      final TreePMap<Integer, String> pNullValues =
          map.entrySet().parallelStream()
              .collect(
                  TreePMap.toTreePMap(STRING_ORDER_COMPARATOR, Map.Entry::getKey, entry -> null));
      assertEquivalentState(nullValues, pNullValues);

      assertThrows(
          NullPointerException.class,
          () ->
              map.entrySet().parallelStream()
                  .collect(
                      TreePMap.toTreePMap(
                          STRING_ORDER_COMPARATOR, entry -> null, Map.Entry::getValue)));
      assertThrows(
          IllegalStateException.class,
          () ->
              map.entrySet().parallelStream()
                  .collect(
                      TreePMap.toTreePMap(
                          STRING_ORDER_COMPARATOR, entry -> "duplicate", Map.Entry::getValue)));
    }

    assertThrows(
        NullPointerException.class,
        () -> TreePMap.toTreePMap(null, Function.identity(), STRINGIFY));
    assertThrows(
        NullPointerException.class,
        () -> TreePMap.toTreePMap(STRING_ORDER_COMPARATOR, null, STRINGIFY));
    assertThrows(
        NullPointerException.class,
        () -> TreePMap.toTreePMap(STRING_ORDER_COMPARATOR, Function.identity(), null));

    // the overload that doesn't take an explicit comparator (uses natural ordering) but does take
    // an explicit merge function:

    final BinaryOperator<String> mergeFunction =
        (oldValue, newValue) -> {
          final List<String> mergedValue = new ArrayList<>();
          mergedValue.addAll(Arrays.asList(oldValue.split(":")));
          mergedValue.addAll(Arrays.asList(newValue.split(":")));
          Collections.sort(mergedValue);
          return String.join(":", mergedValue);
        };

    {
      final Map<Integer, String> map = new HashMap<>();
      for (int i = 0; i < 20; ++i) {
        map.put(RANDOM.nextInt(), randomString());
      }

      assertEquivalentState(
          new TreeMap<>(map),
          map.entrySet().parallelStream()
              .collect(TreePMap.toTreePMap(Map.Entry::getKey, Map.Entry::getValue, mergeFunction)));

      // map all entries to duplicate key, to exercise merge function:
      assertEquivalentState(
          new TreeMap<>(
              Collections.singletonMap(
                  3, map.values().parallelStream().sorted().collect(Collectors.joining(":")))),
          map.entrySet().parallelStream()
              .collect(TreePMap.toTreePMap(entry -> 3, Map.Entry::getValue, mergeFunction)));

      assertThrows(
          NullPointerException.class,
          () ->
              map.entrySet().parallelStream()
                  .collect(TreePMap.toTreePMap(entry -> null, Map.Entry::getValue, mergeFunction)));
    }

    assertThrows(
        NullPointerException.class,
        () -> TreePMap.toTreePMap(null, Function.identity(), mergeFunction));
    assertThrows(
        NullPointerException.class,
        () -> TreePMap.toTreePMap(Function.identity(), null, mergeFunction));
    assertThrows(
        NullPointerException.class,
        () ->
            TreePMap.<Map.Entry<Integer, String>, Integer, String>toTreePMap(
                Map.Entry::getKey, Map.Entry::getValue, (BinaryOperator<String>) null));

    // the overload that takes an explicit comparator and an explicit merge function (reusing the
    // 'mergeFunction' from the previous section):

    {
      final Map<Integer, String> map = new HashMap<>();
      for (int i = 0; i < 20; ++i) {
        map.put(RANDOM.nextInt(), randomString());
      }

      final TreeMap<Integer, String> expected = new TreeMap<>(STRING_ORDER_COMPARATOR);

      expected.putAll(map);
      assertEquivalentState(
          expected,
          map.entrySet().parallelStream()
              .collect(
                  TreePMap.toTreePMap(
                      STRING_ORDER_COMPARATOR,
                      Map.Entry::getKey,
                      Map.Entry::getValue,
                      mergeFunction)));

      // map all entries to duplicate key, to exercise merge function:
      expected.clear();
      expected.put(3, map.values().stream().sorted().collect(Collectors.joining(":")));
      assertEquivalentState(
          expected,
          map.entrySet().parallelStream()
              .collect(
                  TreePMap.toTreePMap(
                      STRING_ORDER_COMPARATOR, entry -> 3, Map.Entry::getValue, mergeFunction)));

      assertThrows(
          NullPointerException.class,
          () ->
              map.entrySet().parallelStream()
                  .collect(
                      TreePMap.toTreePMap(
                          STRING_ORDER_COMPARATOR,
                          entry -> null,
                          Map.Entry::getValue,
                          mergeFunction)));
    }

    assertThrows(
        NullPointerException.class,
        () -> TreePMap.toTreePMap(null, Function.identity(), STRINGIFY, mergeFunction));
    assertThrows(
        NullPointerException.class,
        () -> TreePMap.toTreePMap(STRING_ORDER_COMPARATOR, null, STRINGIFY, mergeFunction));
    assertThrows(
        NullPointerException.class,
        () ->
            TreePMap.toTreePMap(STRING_ORDER_COMPARATOR, Function.identity(), null, mergeFunction));
    assertThrows(
        NullPointerException.class,
        () -> TreePMap.toTreePMap(STRING_ORDER_COMPARATOR, Function.identity(), STRINGIFY, null));
  }

  public void testValues() {
    // TreePMap doesn't override AbstractMap.values(), so we just do a small sanity-check:

    assertEquals(Arrays.asList(), new ArrayList<>(EMPTY.values()));
    assertEquals(
        Arrays.asList("1", "2", "10"), new ArrayList<>(treePMapOf(STRINGIFY, 1, 2, 10).values()));
  }

  /**
   * Holder of a NavigableMap that is known to behave as expected (typically an instance of TreeMap)
   * and a corresponding TreePMap that we want to test against it. Useful for tests that involve a
   * complicated set of verifications on each of multiple such subcases; such a test can build a
   * list of appropriate subcases, then perform the verifications in a loop over that list.
   */
  private static final class Subcase {
    final NavigableMap<Integer, String> expected;
    final TreePMap<Integer, String> actual;

    Subcase(final NavigableMap<Integer, String> expected, final TreePMap<Integer, String> actual) {
      this.expected = expected;
      this.actual = actual;
    }
  }

  /**
   * Assert that actual has the expected state, in that it has the same mappings as expected, in the
   * same order, and reports an equivalent comparator.
   *
   * <p>This method is intended to validate the result of any method that produces a TreePMap.
   *
   * <p>Background: a TreePMap instance has three pieces of state: a tree containing the mappings
   * (which obviously has internal structure, but the TreePMap doesn't have to worry about that), a
   * comparator that determines (and must match) the order of elements in the tree, and a boolean
   * that controls whether the map's order is the same as the tree's or the opposite. To validate
   * these three things, it's sufficient to verify that it has the right mappings in the right order
   * (which confirms that the tree and the boolean are OK) and that it reports the right comparator
   * (which, given that the boolean is OK, confirms that that the internal comparator is OK). (It's
   * obviously not ideal to write a test based on our understanding of the inner workings of the
   * class we're testing; but given the huge number of different producer method scenarios, we'd
   * have a massive combinatorial explosion of different instances to test if we didn't cut it down
   * in some way along these lines.)
   *
   * <p>Caveat: if actual is empty or has only one mapping, then it's meaningless to talk about the
   * order that its mappings are in. So any method that produces a TreePMap should have a test case
   * for producing a TreePMap with more than one mapping (unless the method <em>never</em> produces
   * a TreePMap with more than one mapping, in which case that doesn't matter).
   *
   * @param expected
   * @param actual
   */
  private static <V> void assertEquivalentState(
      final SortedMap<Integer, V> expected, final TreePMap<Integer, V> actual) {
    assertSameSequence(expected.entrySet(), actual.entrySet());

    assertEquivalentComparator(expected.comparator(), actual.comparator());
  }

  /** Same as {@link #assertEquivalentState(SortedMap, TreePMap)}, but for sets instead of maps. */
  private static <V> void assertEquivalentState(
      final SortedSet<Integer> expected, final PSortedSet<Integer> actual) {
    assertSameSequence(expected, actual);

    assertEquivalentComparator(expected.comparator(), actual.comparator());
  }

  private static <E> void assertSameSequence(final Iterable<E> expected, final Iterable<E> actual) {
    final ArrayList<E> expectedList = new ArrayList<>();
    for (final E element : expected) {
      expectedList.add(element);
    }
    final ArrayList<E> actualList = new ArrayList<>();
    for (final E element : actual) {
      actualList.add(element);
    }
    assertEquals(expectedList, actualList);
  }

  private static void assertEquivalentComparator(
      final Comparator<? super Integer> expected, final Comparator<? super Integer> actual) {
    assertNotNull(actual);

    // We only work with four orderings in this test -- the natural ordering (1 < 2 < 10), the
    // reverse of that (10 < 2 < 1), a string-based ordering (1 < 10 < 2), and the reverse of that
    // (2 < 10, 1) -- and we're not concerned that TreePMap might conjure up a different ordering
    // or an invalid/inconsistent ordering; so all we need to check is that the two comparators sort
    // { 1, 2, 10 } into the correct order:

    final List<Integer> expectedList = Arrays.asList(2, 1, 10);
    final List<Integer> actualList = Arrays.asList(2, 1, 10);

    Collections.sort(expectedList, expected);
    Collections.sort(actualList, actual);

    assertEquals(expectedList, actualList);
  }

  private static void assertThrows(
      final Class<? extends RuntimeException> exceptionClass, final Runnable runnable) {
    try {
      runnable.run();

      fail("Expected a [" + exceptionClass.getName() + "] to be thrown");
    } catch (final RuntimeException re) {
      if (!exceptionClass.isInstance(re)) {
        final AssertionFailedError failure =
            new AssertionFailedError(
                "Expected a [" + exceptionClass.getName() + "] to be thrown, instead got: " + re);
        failure.initCause(re);
        throw failure;
      }
    }
  }

  private static <K, V> Map.Entry<K, V> entryOf(final K key, final V value) {
    return new AbstractMap.SimpleImmutableEntry<>(key, value);
  }

  private static Integer findSomeNonKey(final SortedMap<Integer, ?> map) {
    int i = 0;
    do {
      if (!map.containsKey(i)) {
        return i;
      }
    } while (++i != 0);
    throw new AssertionFailedError("Map contains mappings for all integers??");
  }

  private static String randomString() {
    return Integer.toString(RANDOM.nextInt());
  }

  private static <T> T serializeAndDeserialize(final T t) throws Exception {
    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    final ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
    objectOutputStream.writeObject(t);
    objectOutputStream.flush();

    final byte[] byteArray = byteArrayOutputStream.toByteArray();

    final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
    final ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

    @SuppressWarnings("unchecked")
    final T result = (T) objectInputStream.readObject();
    assertEquals(-1, objectInputStream.read()); // assert that we read the whole thing

    return result;
  }

  private static TreeMap<Integer, String> treeMapOf() {
    return treeMapOf(STRINGIFY);
  }

  private static TreeMap<Integer, String> treeMapOf(
      final Function<? super Integer, String> keyToValue, final Integer... keys) {
    final TreeMap<Integer, String> treeMap = new TreeMap<>();
    for (final Integer key : keys) {
      treeMap.put(key, keyToValue.apply(key));
    }
    return treeMap;
  }

  private static TreePMap<Integer, String> treePMapOf(
      final Function<? super Integer, String> keyToValue, final Integer... keys) {
    TreePMap<Integer, String> treeMap = TreePMap.empty();
    for (final Integer key : keys) {
      treeMap = treeMap.plus(key, keyToValue.apply(key));
    }
    return treeMap;
  }

  private static TreePMap<Integer, String> treePMapOf(
      final Comparator<? super Integer> comparator,
      final Function<? super Integer, String> keyToValue,
      final Integer... keys) {
    TreePMap<Integer, String> treeMap = TreePMap.empty(comparator);
    for (final Integer key : keys) {
      treeMap = treeMap.plus(key, keyToValue.apply(key));
    }
    return treeMap;
  }

  public void testUnmodifiable() {
    assertMapMutatorsThrow(TreePMap.empty(), "key", "value");
    assertMapMutatorsThrow(TreePMap.singleton("key1", "value1"), "key2", "value2");
  }

  public void testChecksForNull() {
    assertMapForbidsNullCollections(TreePMap.empty(), "key", "value");
    assertMapForbidsNullKeys(TreePMap.empty(), "key", "value");
    assertMapAllowsNullValues(TreePMap.empty(), "key", "value");

    assertMapForbidsNullCollections(TreePMap.singleton("key1", "value1"), "key2", "value2");
    assertMapForbidsNullKeys(TreePMap.singleton("key1", "value1"), "key2", "value2");
    assertMapAllowsNullValues(TreePMap.singleton("key1", "value1"), "key2", "value2");
  }

  public void testNullEntrySet() {
    final Map<String, Integer> m = TreePMap.singleton("x", 1).plus("y", null).plus("z", null);
    for (final Map.Entry<String, Integer> e : m.entrySet()) {
      assertEquals(m.get(e.getKey()), e.getValue());
      assertTrue(m.entrySet().contains(e));
    }
  }
}
