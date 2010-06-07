package org.pcollections.tests;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.pcollections.ConsPStack;
import org.pcollections.PStack;



import junit.framework.TestCase;

public class ConsPStackTest extends TestCase {
	
	/**
	 * Compares the behavior of java.util.LinkedList to the behavior of ConsPStack.
	 */
	public void testRandomlyAgainstJavaList() {
		PStack<Integer> pstack = ConsPStack.empty();
		List<Integer> list = new LinkedList<Integer>();
		Random r = new Random();
		for(int i=0;i<1000;i++) {
			if(pstack.size()==0 || r.nextBoolean()) { // add
				if(r.nextBoolean()) { // append
					Integer v = r.nextInt();
		
					assertEquals(list.contains(v), pstack.contains(v));
					
					list.add(0,v);
					pstack = pstack.plus(v);
				}
				else { // insert
					int k = r.nextInt(pstack.size()+1);
					Integer v = r.nextInt();
	
					assertEquals(list.contains(v), pstack.contains(v));
					if(k<pstack.size())
						assertEquals(list.get(k), pstack.get(k));
	
					list.add(k, v);
					pstack = pstack.plus(k, v);
				}
			}
			else if(r.nextBoolean()) { // replace
				int k = r.nextInt(pstack.size());
				Integer v = r.nextInt();
				list.set(k, v);
				pstack = pstack.with(k, v);
			}
			else { // remove a random element
				int j = r.nextInt(pstack.size()), k=0;
				for(Integer e : pstack) {

					assertTrue(list.contains(e));
					assertTrue(pstack.contains(e));
					assertEquals(e, pstack.get(k));
					assertEquals(list.get(k), pstack.get(k));
					UtilityTest.assertEqualsAndHash( pstack, pstack.minus(k).plus(k, pstack.get(k)) );
					UtilityTest.assertEqualsAndHash( pstack, pstack.plus(k, 10).minus(k) );

					if(k==j) {
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
			assertFalse( pstack.contains(v) );
			pstack = pstack.minus(s);

			assertEquals(list.size(), pstack.size());
			UtilityTest.assertEqualsAndHash(list, pstack);
			
			UtilityTest.assertEqualsAndHash(pstack, ConsPStack.from(pstack));
			UtilityTest.assertEqualsAndHash(ConsPStack.empty(), pstack.minusAll(pstack));
			UtilityTest.assertEqualsAndHash(pstack,
					ConsPStack.empty().plusAll(UtilityTest.reverse(pstack)));
			UtilityTest.assertEqualsAndHash(pstack,
					ConsPStack.singleton(10).plusAll(1,UtilityTest.reverse(pstack)).minus(0));
		}
	}

	public void testSubListIntInt() {
		// TODO
	}

	public void testMinusObject() {
		// TODO
	}

}
