package org.pcollections.tests.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class UnmodifiableAssertions {

	public static <V> void assertSetMutatorsThrow(Set<V> set, V sampleValue) {
		assertThrows(UnsupportedOperationException.class, ()->set.add(sampleValue));
		assertThrows(UnsupportedOperationException.class, ()->set.addAll(asList(sampleValue, sampleValue)));
		assertThrows(UnsupportedOperationException.class, set::clear);
		assertThrows(UnsupportedOperationException.class, ()->set.remove(sampleValue));
		assertThrows(UnsupportedOperationException.class, ()->set.removeAll(asList(sampleValue, sampleValue)));
		assertThrows(UnsupportedOperationException.class, ()->set.removeIf(__->true));
		assertThrows(UnsupportedOperationException.class, ()->set.retainAll(asList(sampleValue, sampleValue)));
	}

	public static <K,V> void assertMapMutatorsThrow(Map<K,V> map, K sampleKey, V sampleValue) {
		assertThrows(UnsupportedOperationException.class, map::clear);
		assertThrows(UnsupportedOperationException.class, ()->map.compute(sampleKey, (k,v)->fail()));
		assertThrows(UnsupportedOperationException.class, ()->map.computeIfAbsent(sampleKey, (k)->fail()));
		assertThrows(UnsupportedOperationException.class, ()->map.computeIfPresent(sampleKey, (k,v)->fail()));
		assertThrows(UnsupportedOperationException.class, ()->map.merge(sampleKey, sampleValue, (k,v)->fail()));
		assertThrows(UnsupportedOperationException.class, ()->map.put(sampleKey, sampleValue));
		assertThrows(UnsupportedOperationException.class, ()->map.putAll(map));
		assertThrows(UnsupportedOperationException.class, ()->map.putIfAbsent(sampleKey, sampleValue));
		assertThrows(UnsupportedOperationException.class, ()->map.remove(sampleKey));
		assertThrows(UnsupportedOperationException.class, ()->map.replace(sampleKey, sampleValue));
		assertThrows(UnsupportedOperationException.class, ()->map.replaceAll((k,v)->fail()));
	}

}
