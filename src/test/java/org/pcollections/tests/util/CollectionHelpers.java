package org.pcollections.tests.util;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import junit.framework.TestCase;
import org.junit.jupiter.params.provider.Arguments;
import org.pcollections.PSet;

public class CollectionHelpers {

  public static Stream<Arguments> collectionElementCases() {
    return elementLists().map(Arguments::of);
  }

  private static Stream<List<?>> elementLists() {
    return Stream.of(
        emptyList(),
        singletonList("item"),
        asList("first", "second", "third"),
        asList("spam", "spam", "bacon"),
        asList("spam", "bacon", "spam"),
        asList("bacon", "spam", "spam"),
        asList("bacon", "spam", "spam", "eggs"));
  }

  public static Stream<Arguments> collectionElementPairCases() {
    return elementLists().flatMap(left -> elementLists().map(right -> Arguments.of(left, right)));
  }

  public static <E> void assertSetSemantics(PSet<E> left, Collection<E> right) {
    {
      Set<E> expected = new HashSet<>(left);
      expected.addAll(right);
      TestCase.assertEquals(
          String.format("plusAll should match addAll for %s and %s", left, right),
          expected,
          left.plusAll(right));
    }
    {
      Set<E> expected = new HashSet<>(left);
      expected.removeAll(right);
      TestCase.assertEquals(
          String.format("minusAll should match removeAll for %s and %s", left, right),
          expected,
          left.minusAll(right));
    }
    {
      Set<E> expected = new HashSet<>(left);
      expected.retainAll(right);
      TestCase.assertEquals(
          String.format("intersect should match retainAll for %s and %s", left, right),
          expected,
          left.intersect(right));
    }
  }
}
