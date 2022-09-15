package org.pcollections.tests.util;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class UnmodifiableAssertions {

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
