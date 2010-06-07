package org.pcollections.tests;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.pcollections.PVector;
import org.pcollections.TreePVector;




import junit.framework.TestCase;

public class TreePVectorTest extends TestCase {

	/**
	 * Compares the behavior of java.util.LinkedList to the behavior of TreePVector.
	 */
	public void testRandomlyAgainstJavaList() {
		PVector<Integer> pvec = TreePVector.empty();
		List<Integer> list = new LinkedList<Integer>();
		Random r = new Random();
		for(int i=0;i<1000;i++) {
			if(pvec.size()==0 || r.nextBoolean()) { // add
				if(r.nextBoolean()) { // append
					Integer v = r.nextInt();
		
					assertEquals(list.contains(v), pvec.contains(v));
					
					list.add(v);
					pvec = pvec.plus(v);
				}
				else { // insert
					int k = r.nextInt(pvec.size()+1);
					Integer v = r.nextInt();
	
					assertEquals(list.contains(v), pvec.contains(v));
					if(k<pvec.size())
						assertEquals(list.get(k), pvec.get(k));
	
					list.add(k, v);
					pvec = pvec.plus(k, v);
				}
			}
			else if(r.nextBoolean()) { // replace
				int k = r.nextInt(pvec.size());
				Integer v = r.nextInt();
				list.set(k, v);
				pvec = pvec.with(k, v);
			}
			else { // remove a random element
				int j = r.nextInt(pvec.size()), k=0;
				for(Integer e : pvec) {

					assertTrue(list.contains(e));
					assertTrue(pvec.contains(e));
					assertEquals(e, pvec.get(k));
					assertEquals(list.get(k), pvec.get(k));
					UtilityTest.assertEqualsAndHash( pvec, pvec.minus(k).plus(k, pvec.get(k)) );
					UtilityTest.assertEqualsAndHash( pvec, pvec.plus(k, 10).minus(k) );

					if(k==j) {
						list.remove(k);
						pvec = pvec.minus(k);
						k--; // indices are now smaller
						j = -1; // don't remove again
					}
					k++;
				}
			}
			
			// also try to remove a _totally_ random value:
			Integer v = r.nextInt();
			assertEquals(list.contains(v), pvec.contains(v));
			list.remove(v);
			pvec = pvec.minus(v);
			
			// and try out a non-Integer:
			String s = Integer.toString(v);
			assertFalse( pvec.contains(v) );
			pvec = pvec.minus(s);

			assertEquals(list.size(), pvec.size());
			UtilityTest.assertEqualsAndHash(list, pvec);
			
			UtilityTest.assertEqualsAndHash(pvec, TreePVector.from(pvec));
			UtilityTest.assertEqualsAndHash(TreePVector.empty(), pvec.minusAll(pvec));
			UtilityTest.assertEqualsAndHash(pvec, TreePVector.empty().plusAll(pvec));
			UtilityTest.assertEqualsAndHash(pvec, TreePVector.singleton(10).plusAll(1,pvec).minus(0));
			
			int end=r.nextInt(pvec.size()+1), start=r.nextInt(end+1);
			UtilityTest.assertEqualsAndHash(pvec.subList(start, end), list.subList(start, end));
		}
	}

	public void testSubListIntInt() {
		// TODO
	}

	public void testMinusObject() {
		// TODO
	}

}
