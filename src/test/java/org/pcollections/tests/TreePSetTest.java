/*
 * Copyright (c) 2022 Ran Ari-Gur. All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

package org.pcollections.tests;

import static org.pcollections.tests.util.CollectionHelpers.assertSetSemantics;
import static org.pcollections.tests.util.CollectionHelpers.collectionElementCases;
import static org.pcollections.tests.util.UnmodifiableAssertions.assertSetMutatorsThrow;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pcollections.TreePSet;
import org.pcollections.tests.util.CollectionHelpers;
import org.pcollections.tests.util.CompareInconsistentWithEquals;
import org.pcollections.tests.util.StringOrderComparator;

public class TreePSetTest extends TestCase {
  /**
   * An empty TreePSet&lt;Integer&gt; using the natural ordering; useful in cases where type
   * inference isn't clever enough to deduce the '&lt;Integer&gt;' on its own.
   */
  private static final TreePSet<Integer> EMPTY = TreePSet.empty();

  private static final Random RANDOM = new Random(123);

  private static final Comparator<Object> STRING_ORDER_COMPARATOR = StringOrderComparator.INSTANCE;

  public void testComparator() {
    // If don't specify a comparator, should get Comparator.naturalOrder():
    assertEquivalentComparator(treeSetOf().comparator(), EMPTY.comparator());

    // . . . and, descendingSet():
    assertEquivalentComparator(
        treeSetOf().descendingSet().comparator(), EMPTY.descendingSet().comparator());

    // If do specify a comparator, should get it:
    assertSame(STRING_ORDER_COMPARATOR, TreePSet.empty(STRING_ORDER_COMPARATOR).comparator());

    // . . . and, descendingSet():
    assertEquivalentComparator(
        STRING_ORDER_COMPARATOR.reversed(),
        TreePSet.empty(STRING_ORDER_COMPARATOR).descendingSet().comparator());
  }

  public void testDescendingIterator() {
    assertSameSequence(treeSetOf()::descendingIterator, EMPTY::descendingIterator);
    assertSameSequence(
        treeSetOf().descendingSet()::descendingIterator, EMPTY.descendingSet()::descendingIterator);

    assertSameSequence(
        treeSetOf(1, 2, 3, 4, 5)::descendingIterator,
        TreePSet.of(1, 2, 3, 4, 5)::descendingIterator);
    assertSameSequence(
        treeSetOf(1, 2, 3, 4, 5).descendingSet()::descendingIterator,
        TreePSet.of(1, 2, 3, 4, 5).descendingSet()::descendingIterator);

    final TreePSet<Integer> set = TreePSet.of(1, 2, 3);
    assertNotSame(set.descendingIterator(), set.descendingIterator());

    final Iterator<Integer> iterator = set.descendingIterator();
    while (iterator.hasNext()) {
      iterator.next();
      assertThrows(UnsupportedOperationException.class, () -> iterator.remove());
    }
    assertThrows(NoSuchElementException.class, () -> iterator.next());
  }

  public void testDescendingSet() {
    assertEquivalentState(treeSetOf().descendingSet(), EMPTY.descendingSet());
    assertEquivalentState(
        treeSetOf().descendingSet().descendingSet(), EMPTY.descendingSet().descendingSet());

    assertEquivalentState(
        treeSetOf(1, 2, 3, 4).descendingSet(), TreePSet.of(1, 2, 3, 4).descendingSet());
    assertEquivalentState(
        treeSetOf(1, 2, 3, 4).descendingSet().descendingSet(),
        TreePSet.of(1, 2, 3, 4).descendingSet().descendingSet());

    assertEquivalentState(
        TreePSet.of(1, 2, 3, 4), TreePSet.of(1, 2, 3, 4).descendingSet().descendingSet());

    // Note: descendingSet() differs from all the other producer methods, from a testing standpoint,
    // in that it sets a flag in the returned TreePSet that almost all other TreePSet methods have
    // to take into account in order to behave correctly. So all of the other test-methods here also
    // test different aspects of the set returned by descendingSet(), in order to test whatever
    // method they're trying to test.
  }

  /**
   * Test that TreePSet.equals(...) returns true for each of various sets that have the same size
   * and elements, and that it returns false for each of various objects that are not such sets.
   */
  public void testEquals() {
    final List<Subcase> subcases =
        Arrays.asList(
            new Subcase(treeSetOf(), EMPTY),
            new Subcase(treeSetOf().descendingSet(), EMPTY.descendingSet()),
            new Subcase(treeSetOf(1, 2, 3, 4, 5), TreePSet.of(1, 2, 3, 4, 5)),
            new Subcase(
                treeSetOf(1, 2, 3, 4, 5).descendingSet(),
                TreePSet.of(1, 2, 3, 4, 5).descendingSet()));

    for (final Subcase subcase : subcases) {
      final NavigableSet<Integer> expected = subcase.expected;
      final TreePSet<Integer> actual = subcase.actual;

      // must return false for anything other than a set:
      assertFalse(actual.equals(null));
      assertFalse(actual.equals(new Object()));
      assertFalse(actual.equals((Object) "foo"));
      assertFalse(actual.equals((Object) new ArrayList<>(actual)));

      // must return true for a set with the same elements:
      assertTrue(actual.equals(actual));
      assertTrue(actual.equals(expected));
      assertTrue(actual.equals((Object) new LinkedHashSet<>(expected)));
      // . . . even if the iteration order is different:
      assertTrue(actual.equals((Object) new HashSet<>(expected)));
      assertTrue(actual.equals(actual.descendingSet()));
      assertTrue(actual.equals(expected.descendingSet()));

      // must return false for a set that's too small:
      if (!actual.isEmpty()) {
        final TreeSet<Integer> tooSmall = new TreeSet<>(expected);
        tooSmall.pollFirst();
        assertFalse(actual.equals((Object) tooSmall));
      }

      // must return false for a set that's too big:
      {
        final HashSet<Integer> tooBig = new HashSet<>(expected);
        tooBig.add(findSomeNonElement(actual));
        assertFalse(actual.equals((Object) tooBig));
      }

      // must return false for a set that has a wrong element:
      if (!actual.isEmpty()) {
        final TreeSet<Integer> wrong = new TreeSet<>(expected);
        final Integer wrongElement = findSomeNonElement(wrong);
        wrong.pollFirst();
        wrong.add(wrongElement);
        assertFalse(actual.equals((Object) wrong));
      }
    }
  }

  public void testEmpty() {
    // the overload that takes no arguments (uses natural ordering):

    assertEquivalentState(new TreeSet<>(), TreePSet.empty());

    // the overload that takes a comparator:

    assertEquivalentState(
        new TreeSet<>(STRING_ORDER_COMPARATOR), TreePSet.empty(STRING_ORDER_COMPARATOR));

    assertThrows(NullPointerException.class, () -> TreePSet.empty(null));
  }

  public void testFrom() {
    // the overload that doesn't take an explicit comparator (uses natural ordering):

    {
      final List<Integer> collection = new ArrayList<>();
      for (int i = 0; i < 20; ++i) {
        collection.add(RANDOM.nextInt());
      }

      assertEquivalentState(new TreeSet<>(collection), TreePSet.from(collection));
    }

    assertThrows(NullPointerException.class, () -> TreePSet.from(null));

    assertThrows(NullPointerException.class, () -> TreePSet.from(Arrays.asList(3, null)));

    // the overload that does take an explicit comparator:

    {
      final List<Integer> collection = new ArrayList<>();
      for (int i = 0; i < 20; ++i) {
        collection.add(RANDOM.nextInt());
      }

      final TreeSet<Integer> expected = new TreeSet<>(STRING_ORDER_COMPARATOR);
      expected.addAll(collection);

      assertEquivalentState(expected, TreePSet.from(STRING_ORDER_COMPARATOR, collection));
    }

    assertThrows(NullPointerException.class, () -> TreePSet.from(null, new ArrayList<>()));

    assertThrows(NullPointerException.class, () -> TreePSet.from(STRING_ORDER_COMPARATOR, null));

    assertThrows(
        NullPointerException.class,
        () -> TreePSet.from(STRING_ORDER_COMPARATOR, Arrays.asList(3, null)));
  }

  public void testFromSortedSet() {
    // if SortedSet has null comparator, use natural-ordering comparator:
    {
      final TreeSet<Integer> expected = new TreeSet<>();
      for (int i = 0; i < 20; ++i) {
        expected.add(RANDOM.nextInt());
      }

      assertEquivalentState(expected, TreePSet.fromSortedSet(expected));
    }

    // if SortedSet has explicit comparator, use it:
    {
      final TreeSet<Integer> expected = new TreeSet<>(STRING_ORDER_COMPARATOR);
      for (int i = 0; i < 20; ++i) {
        expected.add(RANDOM.nextInt());
      }

      assertEquivalentState(expected, TreePSet.fromSortedSet(expected));
    }

    // if SortedSet is actually already a TreePSet, just use it as-is:
    {
      TreePSet<Integer> expected = TreePSet.empty(STRING_ORDER_COMPARATOR);
      for (int i = 0; i < 20; ++i) {
        expected = expected.plus(RANDOM.nextInt());
      }

      assertSame(expected, TreePSet.fromSortedSet(expected));
    }

    // if SortedSet is actually null, complain:
    assertThrows(NullPointerException.class, () -> TreePSet.fromSortedSet(null));

    // if SortedSet contains null, complain:
    {
      final TreeSet<Integer> containsNull = new TreeSet<>(STRING_ORDER_COMPARATOR);
      containsNull.add(RANDOM.nextInt());
      containsNull.add(null);

      assertThrows(NullPointerException.class, () -> TreePSet.fromSortedSet(containsNull));
    }
  }

  public void testHashCode() {
    assertEquals(0, EMPTY.hashCode());
    assertEquals(0, EMPTY.descendingSet().hashCode());

    assertEquals(10, TreePSet.of(1, 2, 3, 4).hashCode());
    assertEquals(10, TreePSet.of(1, 2, 3, 4).descendingSet().hashCode());
  }

  /**
   * Verifies that, when comparison is not consistent with equals, that TreePSet honors the former
   * rather than the latter.
   */
  public void testInconsistentWithEquals() {
    final TreeSet<CompareInconsistentWithEquals> expected = new TreeSet<>();

    TreePSet<CompareInconsistentWithEquals> actual = TreePSet.empty();

    for (int i = 0; i < 20; ++i) {
      final int randomEq1 = RANDOM.nextInt();
      final int randomEq2 = RANDOM.nextInt();
      final int randomComp1 = RANDOM.nextInt();
      final int randomComp2 = RANDOM.nextInt();

      final List<CompareInconsistentWithEquals> toAdd =
          Arrays.asList(
              new CompareInconsistentWithEquals(randomEq1, randomComp1),
              new CompareInconsistentWithEquals(randomEq1, randomComp2),
              new CompareInconsistentWithEquals(randomEq2, randomComp1),
              new CompareInconsistentWithEquals(randomEq2, randomComp2));

      expected.addAll(toAdd);
      actual = actual.plusAll(toAdd);
    }

    assertEquals(expected.size(), actual.size());

    final Iterator<CompareInconsistentWithEquals> actualIterator = actual.iterator();
    for (final CompareInconsistentWithEquals expectedElement : expected) {
      assertSame(expectedElement, actualIterator.next());
    }
  }

  public void testIsEmpty() {
    assertTrue(EMPTY.isEmpty());
    assertTrue(EMPTY.descendingSet().isEmpty());

    assertFalse(TreePSet.of(1, 2, 3, 4).isEmpty());
    assertFalse(TreePSet.of(1, 2, 3, 4).descendingSet().isEmpty());
  }

  public void testIterator() {
    assertEquivalentState(treeSetOf(), EMPTY);
    assertEquivalentState(treeSetOf().descendingSet(), EMPTY.descendingSet());

    assertEquivalentState(treeSetOf(1, 2, 3, 4, 5), TreePSet.of(1, 2, 3, 4, 5));
    assertEquivalentState(
        treeSetOf(1, 2, 3, 4, 5).descendingSet(), TreePSet.of(1, 2, 3, 4, 5).descendingSet());

    final TreePSet<Integer> set = TreePSet.of(1, 2, 3);
    assertNotSame(set.iterator(), set.iterator());

    final Iterator<Integer> iterator = set.iterator();
    while (iterator.hasNext()) {
      iterator.next();
      assertThrows(UnsupportedOperationException.class, () -> iterator.remove());
    }
    assertThrows(NoSuchElementException.class, () -> iterator.next());
  }

  /**
   * This test ensures that we cover all the different cases in KVTree.join (meaning, all the
   * different relative heights of left.left, left.right, right.left, and right.right that have to
   * be handled in different ways), because some of them have low probability of occurring randomly.
   */
  public void testKVTreeJoin() {
    /** a perfect tree of height 3 */
    final TreePSet<Integer> oneToSeven = TreePSet.of(1, 2, 3, 4, 5, 6, 7);
    /** a perfect tree of height 4 */
    final TreePSet<Integer> oneToFifteen =
        TreePSet.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);

    // case where left.height < right.height - 2:
    assertEquivalentState(treeSetOf(8, 9, 10, 11, 12, 13, 14, 15), oneToFifteen.tailSet(8, true));

    // case where left.height == right.height - 2 and right.left.height <= right.right.height:
    assertEquivalentState(treeSetOf(4, 6, 7), oneToSeven.minus(5).tailSet(4, true));

    // case where left.height == right.height - 2 and right.left.height > right.right.height:
    assertEquivalentState(treeSetOf(4, 5, 6), oneToSeven.minus(7).tailSet(4, true));

    // skip case where |left.height - right.height| <= 1 -- this happens naturally all the time
    // (including in this method), no special coverage needed

    // case where left.height == right.height + 2 and left.left.height < left.right.height:
    assertEquivalentState(treeSetOf(2, 3, 4), oneToSeven.minus(1).headSet(4, true));

    // case where left.height == right.height + 2 and left.left.height >= left.right.height:
    assertEquivalentState(treeSetOf(1, 2, 4), oneToSeven.minus(3).headSet(4, true));

    // case where left.height > right.height + 2:
    assertEquivalentState(treeSetOf(1, 2, 3, 4, 5, 6, 7, 8), oneToFifteen.headSet(8, true));
  }

  /** Perform a few simple validations on a largish random set (just as a sanity-check). */
  public void testLargishRandomSet() {
    final TreeSet<Integer> expected = new TreeSet<>();
    TreePSet<Integer> actual = TreePSet.empty();

    for (int i = 0; i < 20_000; ++i) {
      final Integer intToAdd = RANDOM.nextInt();
      expected.add(intToAdd);
      actual = actual.plus(intToAdd);
    }

    assertEquivalentState(expected, actual);
    assertEquivalentState(expected.descendingSet(), actual.descendingSet());

    assertTrue(actual.containsAll(expected));

    {
      final int a = RANDOM.nextInt();
      final int b = RANDOM.nextInt();

      final int from = Math.min(a, b);
      final int to = Math.max(a, b);

      assertEquivalentState(expected.subSet(from, to), actual.subSet(from, to));
      assertEquivalentState(
          expected.descendingSet().subSet(to, from), actual.descendingSet().subSet(to, from));
    }
  }

  public void testMinusAll() {
    assertEquivalentState(EMPTY, EMPTY.minusAll(Arrays.asList()));

    assertEquivalentState(EMPTY, EMPTY.minusAll(Arrays.asList(1, 2, 3, 4, 5)));

    assertEquivalentState(EMPTY, TreePSet.of(1, 2, 3, 4, 5).minusAll(Arrays.asList(1, 2, 3, 4, 5)));

    assertEquivalentState(
        TreePSet.of(1, 3, 5), TreePSet.of(1, 2, 3, 4, 5).minusAll(Arrays.asList(0, 2, 4, 6)));

    assertEquivalentState(EMPTY.descendingSet(), EMPTY.descendingSet().minusAll(Arrays.asList()));

    assertEquivalentState(
        EMPTY.descendingSet(), EMPTY.descendingSet().minusAll(Arrays.asList(1, 2, 3, 4, 5)));

    assertEquivalentState(
        EMPTY.descendingSet(),
        TreePSet.of(1, 2, 3, 4, 5).descendingSet().minusAll(Arrays.asList(1, 2, 3, 4, 5)));

    assertEquivalentState(
        TreePSet.of(1, 3, 5).descendingSet(),
        TreePSet.of(1, 2, 3, 4, 5).descendingSet().minusAll(Arrays.asList(0, 2, 4, 6)));

    assertThrows(NullPointerException.class, () -> EMPTY.minusAll(null));
    assertThrows(NullPointerException.class, () -> EMPTY.minusAll(Arrays.asList(0, null)));
  }

  public void testMinusFirst() {
    assertThrows(NoSuchElementException.class, () -> EMPTY.minusFirst());

    assertEquivalentState(TreePSet.of(2, 3, 4, 5), TreePSet.of(1, 2, 3, 4, 5).minusFirst());

    assertEquivalentState(
        TreePSet.of(1, 2, 3, 4).descendingSet(),
        TreePSet.of(1, 2, 3, 4, 5).descendingSet().minusFirst());
  }

  public void testMinusLast() {
    assertThrows(NoSuchElementException.class, () -> EMPTY.minusLast());

    assertEquivalentState(TreePSet.of(1, 2, 3, 4), TreePSet.of(1, 2, 3, 4, 5).minusLast());

    assertEquivalentState(
        TreePSet.of(2, 3, 4, 5).descendingSet(),
        TreePSet.of(1, 2, 3, 4, 5).descendingSet().minusLast());
  }

  /**
   * Test that add(...), addAll(...), clear(), pollFirst(), pollLast(), remove(...), removeAll(...),
   * removeIf(...), and retainAll(...) all throw UnsupportedOperationException.
   */
  @SuppressWarnings("deprecation")
  public void testMutators() {
    final TreePSet<Integer> set = TreePSet.of(1, 2, 3, 4);

    assertThrows(UnsupportedOperationException.class, () -> set.add(10));
    assertThrows(UnsupportedOperationException.class, () -> set.addAll(Collections.emptyList()));
    assertThrows(UnsupportedOperationException.class, () -> set.clear());
    assertThrows(UnsupportedOperationException.class, () -> set.pollFirst());
    assertThrows(UnsupportedOperationException.class, () -> set.pollLast());
    assertThrows(UnsupportedOperationException.class, () -> set.remove(10));
    assertThrows(UnsupportedOperationException.class, () -> set.removeAll(Collections.emptyList()));
    assertThrows(UnsupportedOperationException.class, () -> set.removeIf(o -> false));
    assertThrows(UnsupportedOperationException.class, () -> set.retainAll(Collections.emptyList()));
  }

  /** Perform a few simple validations on a set of non-integers (just as a sanity-check). */
  public void testNonIntegerSet() {
    final TreeSet<Double> expected = new TreeSet<>();
    TreePSet<Double> actual = TreePSet.empty();

    for (int i = 0; i < 100; ++i) {
      final Double doubleToAdd = RANDOM.nextDouble();
      expected.add(doubleToAdd);
      actual = actual.plus(doubleToAdd);
    }

    assertSameSequence(expected, actual);
    assertSameSequence(expected.descendingSet(), actual.descendingSet());

    assertTrue(actual.containsAll(expected));

    {
      final double a = RANDOM.nextDouble();
      final double b = RANDOM.nextDouble();

      final double from = Math.min(a, b);
      final double to = Math.max(a, b);

      assertSameSequence(expected.subSet(from, to), actual.subSet(from, to));
      assertSameSequence(
          expected.descendingSet().subSet(to, from), actual.descendingSet().subSet(to, from));
    }
  }

  public void testOf() {
    // the overload that doesn't take an explicit comparator (uses natural ordering):

    assertEquivalentState(treeSetOf(), EMPTY);

    assertEquivalentState(treeSetOf(1), TreePSet.of(1));

    assertEquivalentState(treeSetOf(1, 3, 5, 2, 4), TreePSet.of(1, 3, 5, 2, 4));

    assertEquivalentState(
        treeSetOf(new Integer[] {1, 3, 5, 2, 4}), TreePSet.of(new Integer[] {1, 3, 5, 2, 4}));

    assertThrows(NullPointerException.class, () -> TreePSet.from(null));

    assertThrows(NullPointerException.class, () -> TreePSet.from(Arrays.asList(3, null)));

    // the overload that does take an explicit comparator:

    {
      final List<Integer> collection = new ArrayList<>();
      for (int i = 0; i < 20; ++i) {
        collection.add(RANDOM.nextInt());
      }

      final TreeSet<Integer> expected = new TreeSet<>(STRING_ORDER_COMPARATOR);
      expected.addAll(collection);

      assertEquivalentState(expected, TreePSet.from(STRING_ORDER_COMPARATOR, collection));
    }

    assertThrows(NullPointerException.class, () -> TreePSet.from(null, new ArrayList<>()));

    assertThrows(NullPointerException.class, () -> TreePSet.from(STRING_ORDER_COMPARATOR, null));

    assertThrows(
        NullPointerException.class,
        () -> TreePSet.from(STRING_ORDER_COMPARATOR, Arrays.asList(3, null)));
  }

  public void testPlusAndMinus() {
    final List<Subcase> subcases =
        Arrays.asList(
            new Subcase(treeSetOf(), EMPTY),
            new Subcase(treeSetOf().descendingSet(), EMPTY.descendingSet()),
            new Subcase(treeSetOf(1, 2, 3, 4, 5), TreePSet.of(1, 2, 3, 4, 5)),
            new Subcase(
                treeSetOf(1, 2, 3, 4, 5).descendingSet(),
                TreePSet.of(1, 2, 3, 4, 5).descendingSet()));

    for (final Subcase subcase : subcases) {
      final NavigableSet<Integer> expected = subcase.expected;
      final TreePSet<Integer> actual = subcase.actual;

      final ArrayList<Integer> valuesToTest = new ArrayList<>();
      valuesToTest.add(0);
      for (final int i : expected) {
        valuesToTest.add(i - 1);
        valuesToTest.add(i);
        valuesToTest.add(i + 1);
      }

      for (final Integer value : valuesToTest) {
        final boolean wasPresent = expected.contains(value);

        expected.add(value);
        assertEquivalentState(expected, actual.plus(value));

        expected.remove(value);
        assertEquivalentState(expected, actual.minus(value));

        if (wasPresent) {
          expected.add(value);
        }
      }

      assertThrows(NullPointerException.class, () -> actual.plus(null));
      assertThrows(NullPointerException.class, () -> actual.minus(null));
    }
  }

  public void testPlusAll() {
    assertEquivalentState(EMPTY, EMPTY.plusAll(Arrays.asList()));
    assertEquivalentState(EMPTY.descendingSet(), EMPTY.descendingSet().plusAll(Arrays.asList()));

    assertEquivalentState(TreePSet.of(1, 2, 3, 4, 5), EMPTY.plusAll(Arrays.asList(1, 2, 3, 4, 5)));
    assertEquivalentState(
        TreePSet.of(1, 2, 3, 4, 5).descendingSet(),
        EMPTY.descendingSet().plusAll(Arrays.asList(1, 2, 3, 4, 5)));

    assertEquivalentState(
        TreePSet.of(1, 2, 3, 4, 5), TreePSet.of(1, 2, 3, 4, 5).plusAll(Arrays.asList()));
    assertEquivalentState(
        TreePSet.of(1, 2, 3, 4, 5).descendingSet(),
        TreePSet.of(1, 2, 3, 4, 5).descendingSet().plusAll(Arrays.asList()));

    assertEquivalentState(
        TreePSet.of(1, 2, 3, 4, 5), TreePSet.of(1, 3, 5).plusAll(Arrays.asList(2, 3, 4)));
    assertEquivalentState(
        TreePSet.of(1, 2, 3, 4, 5).descendingSet(),
        TreePSet.of(1, 3, 5).descendingSet().plusAll(Arrays.asList(2, 3, 4)));

    assertThrows(NullPointerException.class, () -> EMPTY.plusAll(null));
    assertThrows(NullPointerException.class, () -> EMPTY.plusAll(Arrays.asList(0, null)));
  }

  /**
   * Test headSet(), subSet(), and tailSet() -- both the overloads with the 'isInclusive'
   * parameter(s) and the overloads without.
   */
  public void testRanges() {
    final List<Subcase> subcases =
        Arrays.asList(
            new Subcase(treeSetOf(), EMPTY),
            new Subcase(treeSetOf().descendingSet(), EMPTY.descendingSet()),
            new Subcase(treeSetOf(1, 2, 3, 4, 5), TreePSet.of(1, 2, 3, 4, 5)),
            new Subcase(
                treeSetOf(1, 2, 3, 4, 5).descendingSet(),
                TreePSet.of(1, 2, 3, 4, 5).descendingSet()));

    for (final Subcase subcase : subcases) {
      final NavigableSet<Integer> expected = subcase.expected;
      final TreePSet<Integer> actual = subcase.actual;

      final ArrayList<Integer> bounds = new ArrayList<>();
      bounds.add(0);
      for (final int i : expected) {
        bounds.add(i - 1);
        bounds.add(i);
        bounds.add(i + 1);
      }

      for (final int to : bounds) {
        assertEquivalentState(expected.headSet(to), actual.headSet(to));
        assertEquivalentState(expected.headSet(to, true), actual.headSet(to, true));
        assertEquivalentState(expected.headSet(to, false), actual.headSet(to, false));

        for (final int from : bounds) {
          if (actual.comparator().compare(from, to) <= 0) {
            assertEquivalentState(expected.subSet(from, to), actual.subSet(from, to));
            assertEquivalentState(
                expected.subSet(from, true, to, true), actual.subSet(from, true, to, true));
            assertEquivalentState(
                expected.subSet(from, true, to, false), actual.subSet(from, true, to, false));
            assertEquivalentState(
                expected.subSet(from, false, to, true), actual.subSet(from, false, to, true));
            assertEquivalentState(
                expected.subSet(from, false, to, false), actual.subSet(from, false, to, false));
          } else {
            assertThrows(IllegalArgumentException.class, () -> actual.subSet(from, to));
            assertThrows(IllegalArgumentException.class, () -> actual.subSet(from, true, to, true));
            assertThrows(
                IllegalArgumentException.class, () -> actual.subSet(from, true, to, false));
            assertThrows(
                IllegalArgumentException.class, () -> actual.subSet(from, false, to, true));
            assertThrows(
                IllegalArgumentException.class, () -> actual.subSet(from, false, to, false));
          }
        }
      }

      for (final int from : bounds) {
        assertEquivalentState(expected.tailSet(from), actual.tailSet(from));
        assertEquivalentState(expected.tailSet(from, true), actual.tailSet(from, true));
        assertEquivalentState(expected.tailSet(from, false), actual.tailSet(from, false));
      }

      assertThrows(NullPointerException.class, () -> actual.headSet(null));
      assertThrows(NullPointerException.class, () -> actual.headSet(null, true));
      assertThrows(NullPointerException.class, () -> actual.subSet(null, 0));
      assertThrows(NullPointerException.class, () -> actual.subSet(null, true, 0, true));
      assertThrows(NullPointerException.class, () -> actual.subSet(0, null));
      assertThrows(NullPointerException.class, () -> actual.subSet(0, true, null, true));
      assertThrows(NullPointerException.class, () -> actual.tailSet(null));
      assertThrows(NullPointerException.class, () -> actual.tailSet(null, true));
    }
  }

  /**
   * Test ceiling(...), contains(...), containsAll(...), first(), floor(...), higher(...), last(),
   * and lower(...), by validating that they give the same result for a TreePSet as for a TreeSet.
   */
  public void testSearchers() {
    final List<Subcase> subcases =
        Arrays.asList(
            new Subcase(treeSetOf(), EMPTY),
            new Subcase(treeSetOf().descendingSet(), EMPTY.descendingSet()),
            new Subcase(treeSetOf(1, 2, 3, 4, 5), TreePSet.of(1, 2, 3, 4, 5)),
            new Subcase(
                treeSetOf(1, 2, 3, 4, 5).descendingSet(),
                TreePSet.of(1, 2, 3, 4, 5).descendingSet()),
            new Subcase(treeSetOf(1, 3, 5, 7, 9), TreePSet.of(1, 3, 5, 7, 9)),
            new Subcase(
                treeSetOf(1, 3, 5, 7, 9).descendingSet(),
                TreePSet.of(1, 3, 5, 7, 9).descendingSet()));

    for (final Subcase subcase : subcases) {
      final NavigableSet<Integer> expected = subcase.expected;
      final TreePSet<Integer> actual = subcase.actual;

      // ensure that we have an element to test even if expected is empty:
      final Iterable<Integer> elementsToTest = expected.isEmpty() ? Arrays.asList(0) : expected;

      for (final int element : elementsToTest) {
        for (final int arg : new int[] {element - 1, element, element + 1}) {
          assertEquals(expected.ceiling(arg), actual.ceiling(arg));
          assertEquals(expected.contains(arg), actual.contains(arg));
          assertEquals(expected.floor(arg), actual.floor(arg));
          assertEquals(expected.higher(arg), actual.higher(arg));
          assertEquals(expected.lower(arg), actual.lower(arg));
        }
      }

      assertTrue(actual.containsAll(expected));
      assertFalse(actual.containsAll(Arrays.asList(findSomeNonElement(expected))));

      if (expected.isEmpty()) {
        assertThrows(NoSuchElementException.class, actual::first);
        assertThrows(NoSuchElementException.class, actual::last);
      } else {
        assertEquals(expected.first(), actual.first());
        assertEquals(expected.last(), actual.last());
      }

      assertThrows(NullPointerException.class, () -> actual.ceiling(null));
      assertThrows(NullPointerException.class, () -> actual.contains(null));
      assertThrows(NullPointerException.class, () -> actual.floor(null));
      assertThrows(NullPointerException.class, () -> actual.higher(null));
      assertThrows(NullPointerException.class, () -> actual.lower(null));
    }
  }

  public void testSerializeAndDeserialize() throws Exception {
    assertEquivalentState(
        TreePSet.of(1, 2, 3, 4), serializeAndDeserialize(TreePSet.of(1, 2, 3, 4)));

    assertEquivalentState(
        TreePSet.of(1, 2, 3, 4).descendingSet(),
        serializeAndDeserialize(TreePSet.of(1, 2, 3, 4).descendingSet()));

    assertEquivalentState(
        TreePSet.of(STRING_ORDER_COMPARATOR, 1, 2, 3, 4),
        serializeAndDeserialize(TreePSet.of(STRING_ORDER_COMPARATOR, 1, 2, 3, 4)));

    assertEquivalentState(
        TreePSet.of(STRING_ORDER_COMPARATOR, 1, 2, 3, 4).descendingSet(),
        serializeAndDeserialize(TreePSet.of(STRING_ORDER_COMPARATOR, 1, 2, 3, 4).descendingSet()));
  }

  public void testSingleton() {
    // the overload that doesn't take an explicit comparator (uses natural ordering):

    assertEquivalentState(new TreeSet<>(Arrays.asList(3)), TreePSet.singleton(3));

    assertThrows(NullPointerException.class, () -> TreePSet.singleton(null));

    // the overload that does take an explicit comparator:

    {
      final TreeSet<Integer> expected = new TreeSet<>(STRING_ORDER_COMPARATOR);
      expected.add(17);

      assertEquivalentState(expected, TreePSet.singleton(STRING_ORDER_COMPARATOR, 17));
    }

    assertThrows(NullPointerException.class, () -> TreePSet.singleton(null, 3));

    assertThrows(
        NullPointerException.class, () -> TreePSet.singleton(STRING_ORDER_COMPARATOR, null));
  }

  public void testSize() {
    assertEquals(0, EMPTY.size());
    assertEquals(0, EMPTY.descendingSet().size());

    assertEquals(4, TreePSet.of(1, 2, 3, 4).size());
    assertEquals(4, TreePSet.of(1, 2, 3, 4).descendingSet().size());
  }

  /**
   * Test toArray() -- both the overload that takes an array parameter, and the one that does not.
   */
  public void testToArray() {
    final List<Subcase> subcases =
        Arrays.asList(
            new Subcase(treeSetOf(), EMPTY),
            new Subcase(treeSetOf().descendingSet(), EMPTY.descendingSet()),
            new Subcase(treeSetOf(1, 2, 3, 4, 5), TreePSet.of(1, 2, 3, 4, 5)),
            new Subcase(
                treeSetOf(1, 2, 3, 4, 5).descendingSet(),
                TreePSet.of(1, 2, 3, 4, 5).descendingSet()));

    for (final Subcase subcase : subcases) {
      final NavigableSet<Integer> expected = subcase.expected;
      final TreePSet<Integer> actual = subcase.actual;

      // no arg:
      assertEquals(Arrays.asList(expected.toArray()), Arrays.asList(actual.toArray()));

      // arg with exactly the right size:
      {
        final Integer[] expectedArr = new Integer[expected.size()];
        expected.toArray(expectedArr);
        final Integer[] actualArr = new Integer[expected.size()];
        assertSame(actualArr, actual.toArray(actualArr));
        assertEquals(Arrays.asList(expectedArr), Arrays.asList(actualArr));
      }

      // arg that's too small:
      if (!expected.isEmpty()) {
        final Integer[] tooSmall = new Integer[expected.size() - 1];
        final Integer[] expectedArr = expected.toArray(tooSmall);
        final Integer[] actualArr = actual.toArray(tooSmall);
        assertEquals(Arrays.asList(expectedArr), Arrays.asList(actualArr));
      }

      // arg that's too big:
      {
        final Integer[] expectedArr = new Integer[expected.size() + 10];
        Arrays.fill(expectedArr, 20);
        expected.toArray(expectedArr);
        final Integer[] actualArr = new Integer[expected.size() + 10];
        Arrays.fill(actualArr, 20);
        assertSame(actualArr, actual.toArray(actualArr));
        assertEquals(Arrays.asList(expectedArr), Arrays.asList(actualArr));
      }

      // arg that's a supertype:
      assertEquals(Number.class, actual.toArray(new Number[0]).getClass().getComponentType());
    }
  }

  public void testToString() {
    // empty set:
    assertEquals("[]", EMPTY.toString());
    assertEquals("[]", EMPTY.descendingSet().toString());

    // singleton set -- no commas:
    assertEquals("[1]", TreePSet.of(1).toString());
    assertEquals("[1]", TreePSet.of(1).descendingSet().toString());

    // set with multiple elements -- separated with comma + space, and order matters:
    assertEquals("[1, 2, 3, 4, 5]", TreePSet.of(1, 2, 3, 4, 5).toString());
    assertEquals("[5, 4, 3, 2, 1]", TreePSet.of(1, 2, 3, 4, 5).descendingSet().toString());
  }

  public void testToTreePSet() {
    // the overload that doesn't take an explicit comparator (uses natural ordering):

    {
      final List<Integer> collection = new ArrayList<>();
      for (int i = 0; i < 20; ++i) {
        collection.add(RANDOM.nextInt());
      }

      final TreeSet<Integer> expected = new TreeSet<>(collection);

      assertEquivalentState(expected, collection.stream().collect(TreePSet.toTreePSet()));
    }

    assertThrows(
        NullPointerException.class,
        () -> Arrays.asList(3, null).stream().collect(TreePSet.toTreePSet()));

    // the overload that does take an explicit comparator:

    {
      final List<Integer> collection = new ArrayList<>();
      for (int i = 0; i < 20; ++i) {
        collection.add(RANDOM.nextInt());
      }

      final TreeSet<Integer> expected = new TreeSet<>(STRING_ORDER_COMPARATOR);
      expected.addAll(collection);

      assertEquivalentState(
          expected, collection.stream().collect(TreePSet.toTreePSet(STRING_ORDER_COMPARATOR)));
    }

    assertThrows(NullPointerException.class, () -> TreePSet.toTreePSet(null));

    assertThrows(
        NullPointerException.class,
        () ->
            Arrays.asList(3, null).stream().collect(TreePSet.toTreePSet(STRING_ORDER_COMPARATOR)));
  }

  /**
   * Holder of a NavigableSet that is known to behave as expected (typically an instance of TreeSet)
   * and a corresponding TreePSet that we want to test against it. Useful for tests that involve a
   * complicated set of verifications on each of multiple such subcases; such a test can build a
   * list of appropriate subcases, then perform the verifications in a loop over that list.
   */
  private static final class Subcase {
    final NavigableSet<Integer> expected;
    final TreePSet<Integer> actual;

    Subcase(final NavigableSet<Integer> expected, final TreePSet<Integer> actual) {
      this.expected = expected;
      this.actual = actual;
    }
  }

  /**
   * Assert that actual has the expected state, in that it has the same elements as expected, in the
   * same order, and reports an equivalent comparator.
   *
   * <p>This method is intended to validate the result of any method that produces a TreePSet.
   *
   * <p>Background: a TreePSet instance has three pieces of state: a tree containing the elements
   * (which obviously has internal structure, but the TreePSet doesn't have to worry about that), a
   * comparator that determines (and must match) the order of elements in the tree, and a boolean
   * that controls whether the set's order is the same as the tree's or the opposite. To validate
   * these three things, it's sufficient to verify that it has the right elements in the right order
   * (which confirms that the tree and the boolean are OK) and that it reports the right comparator
   * (which, given that the boolean is OK, confirms that that the internal comparator is OK). (It's
   * obviously not ideal to write a test based on our understanding of the inner workings of the
   * class we're testing; but given the huge number of different producer method scenarios, we'd
   * have a massive combinatorial explosion of different instances to test if we didn't cut it down
   * in some way along these lines.)
   *
   * <p>Caveat: if actual is empty or has only one element, then it's meaningless to talk about the
   * order that its elements are in. So any method that produces a TreePSet should have a test case
   * for producing a TreePSet with more than one element (unless the method <em>never</em> produces
   * a TreePSet with more than one element, in which case that doesn't matter).
   *
   * @param expected
   * @param actual
   */
  private static void assertEquivalentState(
      final SortedSet<Integer> expected, final TreePSet<Integer> actual) {
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

    // We only work with five orderings in this test -- the natural ordering (1 < 2 < 10), the
    // reverse of that (10 < 2 < 1), a string-based ordering (1 < 10 < 2), the reverse of that
    // (2 < 10 < 1), and an ordering that treats all values as equivalent (1 = 2 = 10) -- and we're
    // not concerned that TreePSet might conjure up a different ordering or an invalid/inconsistent
    // ordering; so all we need to check is that the two comparators sort { 2, 1, 10 } into the
    // same order:

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

  private static Integer findSomeNonElement(final SortedSet<Integer> set) {
    int i = 0;
    do {
      if (!set.contains(i)) {
        return i;
      }
    } while (++i != 0);
    throw new AssertionFailedError("Set contains all integers??");
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

  private static TreeSet<Integer> treeSetOf(final Integer... elements) {
    return new TreeSet<>(Arrays.asList(elements));
  }

  public void testUnmodifiable() {
    assertSetMutatorsThrow(TreePSet.singleton("value1"), "value2");
  }

  @ParameterizedTest
  @MethodSource("org.pcollections.tests.util.CollectionHelpers#collectionElementPairCases")
  public void treePSet_hasSetSemantics(List<String> left, List<String> right) {
    assertSetSemantics(TreePSet.from(left), right);
  }

}
