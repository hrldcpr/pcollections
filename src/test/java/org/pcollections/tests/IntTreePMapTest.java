package org.pcollections.tests;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.pcollections.IntTreePMap;
import org.pcollections.PMap;




import junit.framework.TestCase;

public class IntTreePMapTest extends TestCase {

	/**
	 * Compares the behavior of java.util.HashMap to the behavior of IntTreePMap.
	 */
	public void testRandomlyAgainstJavaMap() {
		PMap<Integer,Integer> pmap = IntTreePMap.empty();
		Map<Integer,Integer> map = new HashMap<Integer,Integer>();
		Random r = new Random();
		for(int i=0;i<10000;i++) {
			if(pmap.size()==0 || r.nextBoolean()) { // add
				int k = r.nextInt(), v = r.nextInt();

				assertEquals(map.containsKey(k), pmap.containsKey(k));
				assertEquals(map.get(k), pmap.get(k));

				map.put(k, v);
				pmap = pmap.plus(k, v);
			} else { // remove a random key
				int j = r.nextInt(pmap.size());
				for(Entry<Integer,Integer> e : pmap.entrySet()) {
					int k = e.getKey();

					assertTrue(map.containsKey(k));
					assertTrue(pmap.containsKey(k));
					assertEquals(map.get(k), pmap.get(k));
					assertTrue(map.entrySet().contains(e));
					assertTrue(pmap.entrySet().contains(e));
					UtilityTest.assertEqualsAndHash( pmap, pmap.plus(k,e.getValue()) );

					if(j--==0) {
						map.remove(k);
						pmap = pmap.minus(k);
						assertFalse(pmap.entrySet().contains(e));
					}
				}
			}
			
			// also try to remove a _totally_ random key:
			int k = r.nextInt();
			assertEquals(map.containsKey(k), pmap.containsKey(k));
			assertEquals(map.get(k), pmap.get(k));
			map.remove(k);
			pmap = pmap.minus(k);
			
			// and try out a non-Integer:
			String s = Integer.toString(k);
			assertFalse( pmap.containsKey(s) );
			assertEquals(null, pmap.get(s));
			assertFalse( pmap.entrySet().contains(s) );
			pmap = pmap.minus(s);

			assertEquals(map.size(), pmap.size());
			UtilityTest.assertEqualsAndHash(map, pmap);
			UtilityTest.assertEqualsAndHash(map.entrySet(), pmap.entrySet());

			UtilityTest.assertEqualsAndHash(pmap, IntTreePMap.from(pmap));
			UtilityTest.assertEqualsAndHash(IntTreePMap.empty(), pmap.minusAll(pmap.keySet()));
			UtilityTest.assertEqualsAndHash(pmap, pmap.plusAll(pmap));
		}
	}

	public void testEmpty() {
		PMap<?,?> empty = IntTreePMap.empty();
		UtilityTest.assertEqualsAndHash(new HashMap<Object,Object>(), empty);
		assertEquals(0, empty.size());
		assertTrue(empty.isEmpty());
		for(@SuppressWarnings("unused") Object e : empty.entrySet())
			fail();
	}

	public void testSingleton() {
		UtilityTest.assertEqualsAndHash(IntTreePMap.empty().plus(10, "test"),
				IntTreePMap.singleton(10, "test"));
	}
	
	public void testWithKeysChanged() {
		// TODO
	}

}
