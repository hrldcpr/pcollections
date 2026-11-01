/*
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

package org.pcollections.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.pcollections.AmortizedPQueue;
import org.pcollections.PQueue;

class AmortizedPQueueTest {
  @Test
  void elementReturnsSingletonNull() {
    PQueue<String> queue = AmortizedPQueue.<String>empty().plus(null);
    assertNull(queue.element());
    assertNull(queue.peek());
    assertEquals(1, queue.size());
    assertNull(queue.element());
    assertTrue(queue.minus().isEmpty());
  }

  @Test
  void elementReturnsNullBeforeOtherValues() {
    PQueue<String> queue = AmortizedPQueue.<String>empty().plus(null).plus("next");
    assertNull(queue.element());
    assertEquals("next", queue.minus().element());
    assertEquals(Arrays.asList(null, "next"), Arrays.asList(queue.toArray()));
    assertNull(queue.element());
  }

  @Test
  void elementReturnsNullTransferredFromBack() {
    PQueue<String> queue = AmortizedPQueue.<String>empty().plus("first").plus(null).plus("last");
    assertEquals("first", queue.element());
    PQueue<String> tail = queue.minus();
    assertNull(tail.element());
    assertNull(tail.peek());
    assertEquals(2, tail.size());
    assertEquals("last", tail.minus().element());
    assertEquals(Arrays.asList(null, "last"), Arrays.asList(tail.toArray()));
    assertEquals(Arrays.asList("first", null, "last"), Arrays.asList(queue.toArray()));
    assertEquals("first", queue.element());
  }

  @Test
  void elementThrowsWhenEmpty() {
    PQueue<String> queue = AmortizedPQueue.empty();
    assertThrows(NoSuchElementException.class, queue::element);
    assertNull(queue.peek());
    PQueue<String> drained = queue.plus(null).minus();
    assertThrows(NoSuchElementException.class, drained::element);
    assertNull(drained.peek());
  }

  @Test
  void elementReturnsNonNullHead() {
    PQueue<String> queue = AmortizedPQueue.<String>empty().plus("value");
    assertEquals("value", queue.element());
    assertEquals("value", queue.peek());
    assertEquals(1, queue.size());
  }
}
