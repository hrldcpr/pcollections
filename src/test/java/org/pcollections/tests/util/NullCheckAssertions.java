package org.pcollections.tests.util;

import java.util.Collections;
import org.junit.jupiter.api.function.Executable;
import org.pcollections.PMap;
import org.pcollections.PSet;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NullCheckAssertions {

	public static <V> void assertSetChecksForNull(PSet<V> set, V sampleValue) {
		assertNPE(() -> set.plus(null));
		assertNPE(() -> set.plusAll(asList(sampleValue, null)));
		assertNPE(() -> set.minus(null));
		assertNPE(() -> set.minusAll(asList(sampleValue, null)));
		assertNPE(() -> set.intersect(asList(sampleValue, null)));
		// Could also check for contains and containsAll; is that overly strict?
	}

	public static <K,V> void assertMapChecksForNull(PMap<K,V> map, K sampleKey, V sampleValue) {
//		assertNPE(() -> map.plus(sampleKey, null));
		assertNPE(() -> map.plus(null, sampleValue));
//		assertNPE(() -> map.plusAll(singletonMap(sampleKey, null)));
		assertNPE(() -> map.plusAll(singletonMap(null, sampleValue)));
		assertNPE(() -> map.minus(null));
		assertNPE(() -> map.minusAll(asList(sampleKey, null)));
	}

	static void assertNPE(Executable executable) {
		assertThrows(NullPointerException.class, executable);
	}
}
