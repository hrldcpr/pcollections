/*
 * Copyright (c) 2008 Harold Cooper. All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

package org.pcollections.tests;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import junit.framework.TestCase;
import org.pcollections.*;

public class UtilityTest extends TestCase {

  public void testStack() {
    List<String> list = Arrays.asList("one", "two", "", "three");

    PStack<String> stack = ConsPStack.empty();

    for (String s : list) {
      stack = stack.plus(s);
      assertEquals("first element should be most recently added", s, stack.get(0));
      assertTrue("stack.plus(x).contains(x) should be true!", stack.contains(s));
    }

    PStack<String> stack2 = ConsPStack.from(list);

    int i = 0;
    for (String s : stack2) assertEquals("iterator() disagrees with asStack()", s, list.get(i++));

    assertEqualsAndHash("iteratively created list should be backwards", stack, reverse(stack2));

    PStack<String> stack3 = stack.plusAll(stack); // palindrome stack

    assertEqualsAndHash("plusAll should reverse order", stack3.subList(0, stack.size()), stack2);
    assertEqualsAndHash(stack3.minusAll(stack3), ConsPStack.empty());
    assertEqualsAndHash(stack3.minusAll(stack), ConsPStack.empty());

    for (String s : list) {
      stack = stack.minus(s);
      assertTrue(!stack.contains(s));
    }
  }

  public void testSet() {
    PSet<String> a = HashTreePSet.empty(), b = HashTreePSet.empty();

    assertEqualsAndHash("empty sets not equal", a, b);
    assertTrue("adding element should change empty set", !a.equals(a.plus("one")));
    a = a.plus("one");
    assertEqualsAndHash("adding duplicate should not change set", a, a.plus("one"));
    a = a.plus("two").plus("");
    b = b.plus("two").plus("").plus("one").plus("two");
    assertEqualsAndHash(a, b);
    b = b.plus("three");
    assertTrue(!a.equals(b));

    int n = b.size();
    for (@SuppressWarnings("unused") String s : b) n--;
    assertTrue("iterator() should return size() elements", n == 0);

    assertEqualsAndHash("adding a set to itself should do nothing", b, b.plusAll(b));
    assertEqualsAndHash(
        "adding a set to the empty set should be identity",
        b,
        MapPSet.from(HashTreePMap.<String, Object>empty(), b));

    // bug Mike found:
    assertEqualsAndHash(
        HashTreePSet.from(Arrays.asList(2)), HashTreePSet.empty().plus(1).plus(2).minus(1));
  }

  public void testMap() {
    PMap<Object, String> m = HashTreePMap.<Object, String>empty();

    assertEquals(m.size(), 0);

    m = m.plus(1, "One");

    assertEquals(m.get(1), "One");

    // last three have same hashCode:
    Object[] keys = {1, 2, 0, 3, ConsPStack.empty()};
    String[] values = {"one", "two", "", "three", "emptyList"};

    for (int i = 0; i < keys.length; i++) {
      m = m.plus(keys[i], values[i]);
      m = m.minus(keys[i]);
      m = m.plus(keys[i], values[i]);
      assertEquals(m.size(), i + 1);
      assertEquals(m.get(keys[i]), values[i]);
    }

    for (int i = keys.length - 1; i >= 0; i--) {
      m = m.minus(keys[i]);
      assertEquals(m.size(), i);
    }
  }

  public void testStackExceptions() {
    sequenceExceptions(ConsPStack.<Integer>empty());
  }

  public void testVectorExceptions() {
    sequenceExceptions(TreePVector.<Integer>empty());
  }

  static void sequenceExceptions(PSequence<Integer> pseq) {
    Random r = new Random();
    for (int i = 0; i < 100; i++) {
      int j = r.nextInt(100) - 10, size0 = 0;
      boolean inBounds = false;

      try {
        size0 = pseq.size();
        inBounds = j >= 0 && j <= pseq.size();
        pseq = pseq.plus(j, r.nextInt());
        if (!inBounds) fail();
      } catch (IndexOutOfBoundsException e) {
        if (inBounds) fail();
      }

      try {
        size0 = pseq.size();
        inBounds = j >= 0 && j <= pseq.size();
        pseq = pseq.plusAll(j, TreePVector.singleton(r.nextInt()));
        if (!inBounds) fail();
      } catch (IndexOutOfBoundsException e) {
        if (inBounds) fail();
      }

      try {
        size0 = pseq.size();
        inBounds = j >= 0 && j < pseq.size();
        pseq = pseq.with(j, r.nextInt());
        if (!inBounds) fail();
      } catch (IndexOutOfBoundsException e) {
        if (inBounds) fail();
      }

      try {
        size0 = pseq.size();
        inBounds = j >= 0 && j < pseq.size();
        pseq = pseq.minus(j);
        if (!inBounds) fail();
      } catch (IndexOutOfBoundsException e) {
        if (inBounds) fail();
      }

      pseq = pseq.plus(r.nextInt());
    }
  }

  static void iteratorExceptions(Iterator<?> iterator) {
    while (iterator.hasNext()) {
      iterator.next();
    }
    try {
      iterator.next();
    } catch (NoSuchElementException e) {
      // expected
      return;
    }
    fail("Expected exception to be thrown");
  }

  static void assertEqualsAndHash(String s, Object a, Object b) {
    assertEquals(s, a, b);
    assertEquals(s + " (hashCode)", a.hashCode(), b.hashCode());
  }

  static void assertEqualsAndHash(Object a, Object b) {
    assertEquals(a, b);
    assertEquals("(hashCode)", a.hashCode(), b.hashCode());
  }

  static <E> PStack<E> reverse(final Collection<? extends E> list) {
    PStack<E> rev = ConsPStack.empty();
    for (E e : list) rev = rev.plus(e);
    return rev;
  }
}
