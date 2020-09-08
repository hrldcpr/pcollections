/*
 * Copyright (c) 2008 Harold Cooper. All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

package org.pcollections.tests;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import junit.framework.TestCase;
import org.pcollections.ConsPStack;
import org.pcollections.Empty;
import org.pcollections.PStack;

public class ConsPStackTest extends TestCase {

  /** Compares the behavior of java.util.LinkedList to the behavior of ConsPStack. */
  public void testRandomlyAgainstJavaList() {
    PStack<Integer> pstack = ConsPStack.empty();
    List<Integer> list = new LinkedList<Integer>();
    Random r = new Random();
    for (int i = 0; i < 1000; i++) {
      if (pstack.size() == 0 || r.nextBoolean()) { // add
        if (r.nextBoolean()) { // append
          Integer v = r.nextInt();

          assertEquals(list.contains(v), pstack.contains(v));

          list.add(0, v);
          pstack = pstack.plus(v);
        } else { // insert
          int k = r.nextInt(pstack.size() + 1);
          Integer v = r.nextInt();

          assertEquals(list.contains(v), pstack.contains(v));
          if (k < pstack.size()) assertEquals(list.get(k), pstack.get(k));

          list.add(k, v);
          pstack = pstack.plus(k, v);
        }
      } else if (r.nextBoolean()) { // replace
        int k = r.nextInt(pstack.size());
        Integer v = r.nextInt();
        list.set(k, v);
        pstack = pstack.with(k, v);
      } else { // remove a random element
        int j = r.nextInt(pstack.size()), k = 0;
        for (Integer e : pstack) {

          assertTrue(list.contains(e));
          assertTrue(pstack.contains(e));
          assertEquals(e, pstack.get(k));
          assertEquals(list.get(k), pstack.get(k));
          UtilityTest.assertEqualsAndHash(pstack, pstack.minus(k).plus(k, pstack.get(k)));
          UtilityTest.assertEqualsAndHash(pstack, pstack.plus(k, 10).minus(k));

          if (k == j) {
            list.remove(k);
            pstack = pstack.minus(k);
            k--; // indices are now smaller
            j = -1; // don't remove again
          }
          k++;
        }
      }

      // also try to remove a _totally_ random value:
      Integer v = r.nextInt();
      assertEquals(list.contains(v), pstack.contains(v));
      list.remove(v);
      pstack = pstack.minus(v);

      // and try out a non-Integer:
      String s = Integer.toString(v);
      assertFalse(pstack.contains(v));
      pstack = pstack.minus(s);

      assertEquals(list.size(), pstack.size());
      UtilityTest.assertEqualsAndHash(list, pstack);

      UtilityTest.assertEqualsAndHash(pstack, ConsPStack.from(pstack));
      UtilityTest.assertEqualsAndHash(ConsPStack.empty(), pstack.minusAll(pstack));
      UtilityTest.assertEqualsAndHash(
          pstack, ConsPStack.empty().plusAll(UtilityTest.reverse(pstack)));
      UtilityTest.assertEqualsAndHash(
          pstack, ConsPStack.singleton(10).plusAll(1, UtilityTest.reverse(pstack)).minus(0));

      int end = r.nextInt(pstack.size() + 1), start = r.nextInt(end + 1);
      UtilityTest.assertEqualsAndHash(pstack.subList(start, end), list.subList(start, end));

      if (!pstack.isEmpty()) {
        final Integer x = pstack.get(r.nextInt(pstack.size()));
        assertEquals(pstack.indexOf(x), list.indexOf(x));
      }
    }
  }

  /** Make sure the right element is removed */
  public void testMinusInt() {
    // First, let's try a list with distinct elements
    PStack<String> pstack = ConsPStack.<String>empty().plus("C").plus("B").plus("A");
    assertEquals(Arrays.asList("A", "B", "C"), pstack);
    assertEquals(Arrays.asList("B", "C"), pstack.minus(0));
    assertEquals(Arrays.asList("A", "B"), pstack.minus(2));

    // Now, let's try duplicates
    pstack = pstack.plus("B");
    assertEquals(Arrays.asList("B", "A", "B", "C"), pstack);
    assertEquals(Arrays.asList("A", "B", "C"), pstack.minus(0));
    assertEquals(Arrays.asList("B", "A", "C"), pstack.minus(2));
  }

  public void testIterator() {
    UtilityTest.iteratorExceptions(Empty.stack().iterator());
    UtilityTest.iteratorExceptions(Empty.stack().plus(1).iterator());
  }

  public void testListIterator() {
    PStack<Integer> s = ConsPStack.empty();
    for (int i = 0; i < 1000; i++) {
      s = s.plus(i);
    }

    int i = 0;
    ListIterator<Integer> it = s.listIterator();
    for (final Integer x : s) {
      final int j = it.nextIndex();
      final Integer y = it.next();
      assertEquals(x, y);
      assertEquals(i, j);
      assertEquals(i, it.previousIndex());
      i++;
    }
  }

  public void testSubListStackOverflowRegression() {
    PStack<Integer> s = ConsPStack.empty();
    for (int i = 0; i < 20000; i++) {
      s = s.plus(i);
    }

    PStack<Integer> head = s.subList(0, 10000);
    assertEquals(head.size(), 10000 - 0);

    PStack<Integer> tail = s.subList(10000, 20000);
    assertEquals(tail.size(), 20000 - 10000);

    PStack<Integer> t = s.subList(9000, 11000);
    assertEquals(t.size(), 11000 - 9000);
  }

  public void testPlusStackOverflowRegression() {
    PStack<Integer> s = ConsPStack.empty();
    for (int i = 0; i < 20000; i++) {
      s = s.plus(i);
    }

    PStack<Integer> t = s.plus(10000, 1234);
    assertEquals(t.size(), s.size() + 1);
  }

  public void testPlusAllStackOverflowRegression() {
    PStack<Integer> s = ConsPStack.empty();
    for (int i = 0; i < 20000; i++) {
      s = s.plus(i);
    }

    PStack<Integer> t = s.plusAll(10000, s);
    assertEquals(t.size(), 2 * s.size());
  }

  public void testMinusStackOverflowRegression() {
    PStack<String> s = ConsPStack.empty();
    for (int i = 0; i < 20000; i++) {
      s = s.plus(Integer.toString(i));
    }

    PStack<String> t = s.minus("10000");
    assertEquals(t.size(), s.size() - 1);
  }

  public void testMinusIndexStackOverflowRegression() {
    PStack<String> s = ConsPStack.empty();
    for (int i = 0; i < 20000; i++) {
      s = s.plus(Integer.toString(i));
    }

    // unlike the other tests, this one wasn't failing at 10,000, no idea why...
    PStack<String> t = s.minus(19000);
    assertEquals(t.size(), s.size() - 1);
  }

  public void testMinusAllStackOverflowRegression() {
    PStack<Integer> s = ConsPStack.empty();
    for (int i = 0; i < 20000; i++) {
      s = s.plus(i);
    }

    PStack<Integer> t = s.minusAll(s);
    assertTrue(t.isEmpty());
  }

  public void testWithStackOverflowRegression() {
    PStack<Integer> s = ConsPStack.empty();
    for (int i = 0; i < 20000; i++) {
      s = s.plus(i);
    }

    PStack<Integer> t = s.with(10000, 1234);
    assertEquals(t.size(), s.size());
  }
}
