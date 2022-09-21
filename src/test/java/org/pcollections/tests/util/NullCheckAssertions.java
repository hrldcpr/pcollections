package org.pcollections.tests.util;

import org.junit.jupiter.api.function.Executable;
import org.pcollections.PMap;
import org.pcollections.PSet;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NullCheckAssertions {

	public static <V> void assertSetForbidsNullCollections(PSet<V> set, V sampleValue) {
		assertNPE(() -> set.plusAll(null));
		assertNPE(() -> set.minusAll(null));
		assertNPE(() -> set.intersect(null));
		assertNPE(() -> set.containsAll(null));
	}

	public static <V> void assertSetAllowsNullElements(PSet<V> set, V sampleValue) {
		assertFalse(set.contains(null), "Given set should not contain a null");

		assertTrue(set.plus(null).contains(null));
		assertTrue(set.plusAll(asList(sampleValue, null)).contains(null));
		assertTrue(set.plusAll(asList(sampleValue, null)).containsAll(asList(sampleValue, null)));
		assertEquals(set, set.plus(null).minus(null));
		assertEquals(set, set.plus(null).minusAll(asList(sampleValue, null)));
		assertEquals(emptySet(), set.intersect(asList(sampleValue, null)));
		assertEquals(singleton(null), set.plus(null).intersect(asList(sampleValue, null)));
	}

	public static <V> void assertSetForbidsNullElements(PSet<V> set, V sampleValue) {
		assertNPE(() -> set.plus(null));
		assertNPE(() -> set.plusAll(asList(sampleValue, null)));
		assertNPE(() -> set.minus(null));
		assertNPE(() -> set.minusAll(asList(sampleValue, null)));
		assertNPE(() -> set.intersect(asList(sampleValue, null)));
		assertNPE(() -> set.contains(null));
		assertNPE(() -> set.containsAll(asList(null, sampleValue)));

		// This one is not reliable because the implementation is permitted,
		// but not required, to short-circuit. That means it can return false,
		// instead of throwing, when it encounters a non-null element that is
		// not in the set.
//		assertNPE(() -> set.containsAll(asList(sampleValue, null)));
	}

	public static <K,V> void assertMapForbidsNullCollections(PMap<K,V> map, K sampleKey, V sampleValue) {
		assertNPE(() -> map.plusAll(null));
		assertNPE(() -> map.minusAll(null));
	}

	public static <K,V> void assertMapForbidsNullKeys(PMap<K,V> map, K sampleKey, V sampleValue) {
		assertNPE(() -> map.plus(null, sampleValue));
		assertNPE(() -> map.plusAll(singletonMap(null, sampleValue)));
		assertNPE(() -> map.minus(null));
		assertNPE(() -> map.minusAll(asList(sampleKey, null)));
		assertNPE(() -> map.containsKey(null));
	}

	public static <K,V> void assertMapAllowsNullKeys(PMap<K,V> map, K sampleKey, V sampleValue) {
		assertFalse(map.containsKey(null), "Given map should not contain a null key");

		assertEquals(sampleValue, map.plus(null, sampleValue).get(null));
		assertEquals(sampleValue, map.plusAll(singletonMap(null, sampleValue)).get(null));
		assertEquals(map, map.plus(null, sampleValue).minus(null));
		assertEquals(map, map.plus(null, sampleValue).minusAll(asList(sampleKey, null)));
	}

	public static <K,V> void assertMapForbidsNullValues(PMap<K,V> map, K sampleKey, V sampleValue) {
		assertNPE(() -> map.plus(sampleKey, null));
		assertNPE(() -> map.plusAll(singletonMap(sampleKey, null)));
		assertNPE(() -> map.containsValue(null));
	}

	public static <K,V> void assertMapAllowsNullValues(PMap<K,V> map, K sampleKey, V sampleValue) {
		assertFalse(map.containsValue(null), "Given map should not contain a null value");

		assertNull(map.plus(sampleKey, null).get(sampleKey));
		assertNull(map.plusAll(singletonMap(sampleKey, null)).get(sampleKey));
		assertTrue(map.plus(sampleKey, null).containsValue(null));
	}

	static void assertNPE(Executable executable) {
		assertThrows(NullPointerException.class, executable);
	}
}
