package org.pcollections.tests.util;

import org.junit.jupiter.api.function.Executable;
import org.pcollections.PSet;

import static java.util.Arrays.asList;
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

	static void assertNPE(Executable executable) {
		assertThrows(NullPointerException.class, executable);
	}
}
