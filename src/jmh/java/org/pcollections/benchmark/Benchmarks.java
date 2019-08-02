/*
 * Copyright (c) 2008 Harold Cooper. All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

package org.pcollections.benchmark;

import static org.pcollections.benchmark.Benchmarks.BaseListType.*;
import static org.pcollections.benchmark.Benchmarks.CollectionType.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.annotations.Benchmark;
import org.pcollections.*;

@State(Scope.Thread)
@Fork(2)
@SuppressWarnings("unchecked")
public class Benchmarks {

  public enum BaseListType {
    LINEAR,
    RANDOM;
  }

  public enum CollectionType {
    LINKED_LIST,
    ARRAY_LIST,
    CONS_P_STACK,
    TREE_P_VECTOR,
    HASH_SET,
    HASH_TREE_P_SET,
    HASH_MAP,
    HASH_TREE_P_BAG,
    INT_TREE_P_MAP;
  }

  @State(Scope.Benchmark)
  public static class LinearAndRandom {
    @Param public BaseListType testAgainst;
  }

  @State(Scope.Benchmark)
  public static class GeneralCollections {
    @Param public BaseListType testAgainst;

    @Param({
      "LINKED_LIST",
      "ARRAY_LIST",
      "CONS_P_STACK",
      "TREE_P_VECTOR",
      "HASH_SET",
      "HASH_TREE_P_SET",
      "HASH_MAP",
      "HASH_TREE_P_BAG",
      "INT_TREE_P_MAP"
    })
    public CollectionType testWith;
  }

  private Object[][] baseList;
  private Collection[][] collections;
  private Map[][] maps;

  /** setup benchmark data structures */
  @Setup
  public void init() {
    baseList = new Object[BaseListType.values().length][];
    collections = new Collection[BaseListType.values().length][CollectionType.values().length];
    maps = new Map[BaseListType.values().length][CollectionType.values().length];

    // make an array of linear indices
    int n = 1000;
    Object[] list = new Object[n];
    for (int i = 0; i < n; i++) list[i] = i;
    baseList[LINEAR.ordinal()] = list;

    collections[LINEAR.ordinal()][LINKED_LIST.ordinal()] = collectionAdd(new LinkedList(), list);
    collections[LINEAR.ordinal()][ARRAY_LIST.ordinal()] = collectionAdd(new ArrayList(), list);
    collections[LINEAR.ordinal()][CONS_P_STACK.ordinal()] =
        pCollectionPlus(ConsPStack.empty(), list);
    collections[LINEAR.ordinal()][TREE_P_VECTOR.ordinal()] =
        pCollectionPlus(TreePVector.empty(), list);
    collections[LINEAR.ordinal()][HASH_SET.ordinal()] = collectionAdd(new HashSet(), list);
    collections[LINEAR.ordinal()][HASH_TREE_P_SET.ordinal()] =
        pCollectionPlus(HashTreePSet.empty(), list);
    collections[LINEAR.ordinal()][HASH_TREE_P_BAG.ordinal()] =
        pCollectionPlus(HashTreePBag.empty(), list);

    maps[LINEAR.ordinal()][HASH_MAP.ordinal()] = mapPut(new HashMap(), list);
    maps[LINEAR.ordinal()][INT_TREE_P_MAP.ordinal()] = pMapPlus(IntTreePMap.empty(), list);

    // make an array of random indices
    Random r = new Random();
    for (int i = 0; i < n; i++) list[i] = r.nextInt();
    baseList[RANDOM.ordinal()] = list;

    collections[RANDOM.ordinal()][LINKED_LIST.ordinal()] = collectionAdd(new LinkedList(), list);
    collections[RANDOM.ordinal()][ARRAY_LIST.ordinal()] = collectionAdd(new ArrayList(), list);
    collections[RANDOM.ordinal()][CONS_P_STACK.ordinal()] =
        pCollectionPlus(ConsPStack.empty(), list);
    collections[RANDOM.ordinal()][TREE_P_VECTOR.ordinal()] =
        pCollectionPlus(TreePVector.empty(), list);
    collections[RANDOM.ordinal()][HASH_SET.ordinal()] = collectionAdd(new HashSet(), list);
    collections[RANDOM.ordinal()][HASH_TREE_P_SET.ordinal()] =
        pCollectionPlus(HashTreePSet.empty(), list);
    collections[RANDOM.ordinal()][HASH_TREE_P_BAG.ordinal()] =
        pCollectionPlus(HashTreePBag.empty(), list);

    maps[RANDOM.ordinal()][HASH_MAP.ordinal()] = mapPut(new HashMap(), list);
    maps[RANDOM.ordinal()][INT_TREE_P_MAP.ordinal()] = pMapPlus(IntTreePMap.empty(), list);
  }

  @Benchmark
  public void linked_list_add(LinearAndRandom lr) {
    List list = new LinkedList();
    collectionAdd(list, baseList[lr.testAgainst.ordinal()]);
  }

  @Benchmark
  public void array_list_add(LinearAndRandom lr) {
    List list = new ArrayList();
    collectionAdd(list, baseList[lr.testAgainst.ordinal()]);
  }

  @Benchmark
  public void consp_plus(LinearAndRandom lr) {
    ConsPStack list = ConsPStack.empty();
    pCollectionPlus(list, baseList[lr.testAgainst.ordinal()]);
  }

  @Benchmark
  public void treePVector_plus(LinearAndRandom lr) {
    TreePVector vector = TreePVector.empty();
    pCollectionPlus(vector, baseList[lr.testAgainst.ordinal()]);
  }

  @Benchmark
  public void hashSet_plus(LinearAndRandom lr) {
    HashSet set = new HashSet();
    collectionAdd(set, baseList[lr.testAgainst.ordinal()]);
  }

  @Benchmark
  public void hashTreePMap_plus(LinearAndRandom lr) {
    PMap map = HashTreePMap.empty();
    pMapPlus(map, baseList[lr.testAgainst.ordinal()]);
  }

  @Benchmark
  public void intTreePMap_plus(LinearAndRandom lr) {
    PMap map = IntTreePMap.empty();
    pMapPlus(map, baseList[lr.testAgainst.ordinal()]);
  }

  // ---- contains / not contains ---- //
  @State(Scope.Benchmark)
  public static class CollectionsWithContains {
    @Param public BaseListType testAgainst;

    @Param({
      "LINKED_LIST",
      "ARRAY_LIST",
      "CONS_P_STACK",
      "TREE_P_VECTOR",
      "HASH_SET",
      "HASH_TREE_P_SET",
      "HASH_TREE_P_BAG"
    })
    public CollectionType testWith;
  }

  @Benchmark
  public void contains(CollectionsWithContains lrl) {
    collectionContains(
        collections[lrl.testAgainst.ordinal()][lrl.testWith.ordinal()],
        baseList[lrl.testAgainst.ordinal()]);
  }

  @Benchmark
  public void _notContains(CollectionsWithContains lrl) {
    collectionNotContains(
        collections[lrl.testAgainst.ordinal()][lrl.testWith.ordinal()],
        baseList[lrl.testAgainst.ordinal()].length);
  }

  // ---- get ---- //

  @State(Scope.Benchmark)
  public static class CollectionsWithGet {
    @Param public BaseListType testAgainst;

    @Param({"LINKED_LIST", "ARRAY_LIST", "CONS_P_STACK", "TREE_P_VECTOR"})
    public CollectionType testWith;
  }

  @Benchmark
  public void get(CollectionsWithGet lrl) {
    listGet((List) collections[lrl.testAgainst.ordinal()][lrl.testWith.ordinal()]);
  }

  // ---- iterator ---- //

  @State(Scope.Benchmark)
  public static class CollectionsWithIterator {
    @Param public BaseListType testAgainst;

    @Param({
      "LINKED_LIST",
      "ARRAY_LIST",
      "CONS_P_STACK",
      "TREE_P_VECTOR",
      "HASH_SET",
      "HASH_TREE_P_SET",
      "HASH_TREE_P_BAG",
      "INT_TREE_P_MAP"
    })
    public CollectionType testWith;
  }

  @Benchmark
  public void iterator(CollectionsWithIterator lrl) {
    collectionIterator(collections[lrl.testAgainst.ordinal()][lrl.testWith.ordinal()]);
  }

  // --- hash map construction ---

  @Benchmark
  public void put(LinearAndRandom lr) {
    mapPut(new HashMap(), baseList[lr.testAgainst.ordinal()]);
  }

  // ---- contains key ----

  @State(Scope.Benchmark)
  public static class CollectionsWithContainsKey {
    @Param public BaseListType testAgainst;

    @Param({"HASH_MAP", "HASH_TREE_P_BAG", "INT_TREE_P_MAP"})
    public CollectionType testWith;
  }

  @Benchmark
  public void mapContainsKey(CollectionsWithContainsKey lrl) {
    mapContainsKey(
        maps[lrl.testAgainst.ordinal()][lrl.testWith.ordinal()],
        baseList[lrl.testAgainst.ordinal()]);
  }

  @Benchmark
  public void mapNotContainsKey(CollectionsWithContainsKey lrl) {
    mapNotContainsKey(
        maps[lrl.testAgainst.ordinal()][lrl.testWith.ordinal()],
        baseList[lrl.testAgainst.ordinal()].length);
  }

  // --- helper methods ---

  private static PCollection pCollectionPlus(PCollection c, final Object[] list) {
    for (Object e : list) c = c.plus(e);
    return c;
  }

  private static Collection collectionAdd(final Collection c, final Object[] list) {
    for (Object e : list) c.add(e);
    return c;
  }

  private static PMap pMapPlus(PMap m, final Object[] list) {
    for (Object e : list) m = m.plus(e, e);
    return m;
  }

  private static Map mapPut(final Map m, final Object[] list) {
    for (Object e : list) m.put(e, e);
    return m;
  }

  private static Collection collectionContains(final Collection c, final Object[] list) {
    for (Object e : list) c.contains(e);
    return c;
  }

  private static Collection collectionNotContains(final Collection c, final int n) {
    for (int i = 0; i < n; i++) c.contains("not an element of c");
    return c;
  }

  private static Map mapContainsKey(final Map m, final Object[] list) {
    for (Object e : list) m.containsKey(e);
    return m;
  }

  private static Map mapNotContainsKey(final Map m, final int n) {
    for (int i = 0; i < n; i++) m.containsKey("not a key of m");
    return m;
  }

  private static List listGet(final List c) {
    for (int i = 0; i < c.size(); i++) c.get(i);
    return c;
  }

  private static Collection collectionIterator(final Collection c) {
    for (@SuppressWarnings("unused") Object e : c) ;
    return c;
  }
}
