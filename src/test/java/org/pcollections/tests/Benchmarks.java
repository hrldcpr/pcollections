package org.pcollections.tests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.pcollections.ConsPStack;
import org.pcollections.HashTreePBag;
import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.IntTreePMap;
import org.pcollections.PBag;
import org.pcollections.PCollection;
import org.pcollections.PMap;
import org.pcollections.PSet;
import org.pcollections.PStack;
import org.pcollections.PVector;
import org.pcollections.TreePVector;




@SuppressWarnings("unchecked")
public class Benchmarks {
	
	private static final String METHOD = "%65s = ",
		TIME = "%5d ms";

	/**
	 * 
	 * Runs benchmarks() on a list of linear keys and a list of random keys.
	 * 
	 * @param args
	 */
	public static void main(final String[] args) {
		// make an array of linear indices
		int n = 10000;
		Object[] list = new Object[n];
		for(int i=0; i<n; i++) list[i] = i;
		System.out.println("***Benchmarks for "+n+" linear keys:***");
		benchmarks(list);
		
		System.out.println();

		Random r = new Random();
		for(int i=0; i<n; i++) list[i] = r.nextInt();
		System.out.println("***Benchmarks for "+n+" random keys:***");
		benchmarks(list);
	}
	
	/**
	 * 
	 * Prints to stdout the times of various operations on LinkedLists,
	 * ConsPStacks, ArrayLists, TreePVectors, HashSets, HashTreePSets,
	 * HashTreePBags, HashMaps, HashTreePMaps, and IntTreePMaps.
	 * 
	 * @param list An array of keys
	 */
	public static void benchmarks(final Object[] list) {
		List linkedList = new LinkedList();
		PStack consPStack = ConsPStack.empty();

		List arrayList = new ArrayList();
		PVector treePVector = TreePVector.empty();

		Set hashSet = new HashSet();
		PSet hashTreePSet = HashTreePSet.empty();
		PBag hashTreePBag = HashTreePBag.empty();
		
		Map hashMap = new HashMap();
		PMap hashTreePMap = HashTreePMap.empty();
		PMap intTreePMap = IntTreePMap.empty();

		System.out.println("Sequential Lists:");
		collectionAdd(linkedList, list);
		consPStack = (PStack)pCollectionPlus(consPStack, list);
		collectionContains(linkedList, list);
		collectionContains(consPStack, list);
		collectionNotContains(linkedList, list.length);
		collectionNotContains(consPStack, list.length);
		listGet(linkedList);
		listGet(consPStack);
		collectionIterator(linkedList);
		collectionIterator(consPStack);

		System.out.println("Random-access Lists:");
		collectionAdd(arrayList, list);
		treePVector = (PVector)pCollectionPlus(treePVector, list);
		collectionContains(arrayList, list);
		collectionContains(treePVector, list);
		collectionNotContains(arrayList, list.length);
		collectionNotContains(treePVector, list.length);
		listGet(arrayList);
		listGet(treePVector);
		collectionIterator(arrayList);
		collectionIterator(treePVector);

		System.out.println("Sets and Bags:");
		collectionAdd(hashSet, list);
		hashTreePSet = (PSet)pCollectionPlus(hashTreePSet, list);
		hashTreePBag = (PBag)pCollectionPlus(hashTreePBag, list);
		collectionContains(hashSet, list);
		collectionContains(hashTreePSet, list);
		collectionContains(hashTreePBag, list);
		collectionNotContains(hashSet, list.length);
		collectionNotContains(hashTreePSet, list.length);
		collectionNotContains(hashTreePBag, list.length);
		collectionIterator(hashSet);
		collectionIterator(hashTreePSet);
		collectionIterator(hashTreePBag);

		System.out.println("Maps:");
		mapPut(hashMap, list);
		hashTreePMap = pMapPlus(hashTreePMap, list);
		intTreePMap = pMapPlus(intTreePMap, list);
		mapContainsKey(hashMap, list);
		mapContainsKey(hashTreePMap, list);
		mapContainsKey(intTreePMap, list);
		mapNotContainsKey(hashMap, list.length);
		mapNotContainsKey(hashTreePMap, list.length);
		mapNotContainsKey(intTreePMap, list.length);
	}
	
	private static PCollection pCollectionPlus(PCollection c, final Object[] list) {
		System.out.format(METHOD, c.getClass().getName()+".plus()");
		long t0 = System.currentTimeMillis();
		for(Object e : list) 
			c = c.plus(e);
		long t1 = System.currentTimeMillis();
		System.out.format(TIME, t1-t0);
		System.out.println();
		return c;
	}
	
	private static void collectionAdd(final Collection c, final Object[] list) {
		System.out.format(METHOD, c.getClass().getName()+".add()");
		long t0 = System.currentTimeMillis();
		for(Object e : list) 
			c.add(e);
		long t1 = System.currentTimeMillis();
		System.out.format(TIME, t1-t0);
		System.out.println();
	}
	
	private static PMap pMapPlus(PMap m, final Object[] list) {
		System.out.format(METHOD, m.getClass().getName()+".plus()");
		long t0 = System.currentTimeMillis();
		for(Object e : list) 
			m = m.plus(e,e);
		long t1 = System.currentTimeMillis();
		System.out.format(TIME, t1-t0);
		System.out.println();
		return m;
	}
	
	private static void mapPut(final Map m, final Object[] list) {
		System.out.format(METHOD, m.getClass().getName()+".put()");
		long t0 = System.currentTimeMillis();
		for(Object e : list) 
			m.put(e,e);
		long t1 = System.currentTimeMillis();
		System.out.format(TIME, t1-t0);
		System.out.println();
	}
	
	private static void collectionContains(final Collection c, final Object[] list) {
		System.out.format(METHOD, c.getClass().getName()+".contains()==true");
		long t0 = System.currentTimeMillis();
		for(Object e : list)
			c.contains(e);
		long t1 = System.currentTimeMillis();
		System.out.format(TIME, t1-t0);
		System.out.println();
	}
	
	private static void collectionNotContains(final Collection c, final int n) {
		System.out.format(METHOD, c.getClass().getName()+".contains()==false");
		long t0 = System.currentTimeMillis();
		for(int i=0; i<n; i++)
			c.contains("not an element of c");
		long t1 = System.currentTimeMillis();
		System.out.format(TIME, t1-t0);
		System.out.println();
	}
	
	private static void mapContainsKey(final Map m, final Object[] list) {
		System.out.format(METHOD, m.getClass().getName()+".containsKey()==true");
		long t0 = System.currentTimeMillis();
		for(Object e : list)
			m.containsKey(e);
		long t1 = System.currentTimeMillis();
		System.out.format(TIME, t1-t0);
		System.out.println();
	}
	
	private static void mapNotContainsKey(final Map m, final int n) {
		System.out.format(METHOD, m.getClass().getName()+".containsKey()==false");
		long t0 = System.currentTimeMillis();
		for(int i=0; i<n; i++)
			m.containsKey("not a key of m");
		long t1 = System.currentTimeMillis();
		System.out.format(TIME, t1-t0);
		System.out.println();
	}
	
	private static void listGet(final List c) {
		System.out.format(METHOD, c.getClass().getName()+".get()");
		long t0 = System.currentTimeMillis();
		for(int i=0;i<c.size();i++)
			c.get(i);
		long t1 = System.currentTimeMillis();
		System.out.format(TIME, t1-t0);
		System.out.println();
	}
	
	private static void collectionIterator(final Collection c) {
		System.out.format(METHOD, c.getClass().getName()+".iterator()");
		long t0 = System.currentTimeMillis();
		for(@SuppressWarnings("unused") Object e : c);
		long t1 = System.currentTimeMillis();
		System.out.format(TIME, t1-t0);
		System.out.println();
	}

}
