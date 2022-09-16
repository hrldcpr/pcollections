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
import java.util.EnumMap;
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
    HASH_TREE_P_BAG,
    HASH_MAP,
    INT_TREE_P_MAP;
  }

  @State(Scope.Benchmark)
  public static class LinearAndRandom {
    @Param public BaseListType testAgainst;
  }

  private Map<BaseListType, Object[]> baseList;
  private Map<BaseListType, Map<CollectionType, Collection>> collections;
  private Map<BaseListType, Map<CollectionType, Map>> maps;

  /** setup benchmark data structures */
  @Setup
  public void init() {
    baseList = new EnumMap<>(BaseListType.class);
    collections = new EnumMap<>(BaseListType.class);
    maps = new EnumMap<>(BaseListType.class);

    int n = 1000;
    Object[] list = new Object[n];
    Random r = new Random();
    for (BaseListType lr : BaseListType.values()) {
      for (int i = 0; i < n; i++) list[i] = lr == LINEAR ? i : r.nextInt();
      baseList.put(lr, list);

      Map<CollectionType, Collection> cs = new EnumMap<>(CollectionType.class);
      cs.put(LINKED_LIST, collectionAdd(new LinkedList(), list));
      cs.put(ARRAY_LIST, collectionAdd(new ArrayList(), list));
      cs.put(CONS_P_STACK, pCollectionPlus(ConsPStack.empty(), list));
      cs.put(TREE_P_VECTOR, pCollectionPlus(TreePVector.empty(), list));
      cs.put(HASH_SET, collectionAdd(new HashSet(), list));
      cs.put(HASH_TREE_P_SET, pCollectionPlus(HashTreePSet.empty(), list));
      cs.put(HASH_TREE_P_BAG, pCollectionPlus(HashTreePBag.empty(), list));
      collections.put(lr, cs);

      Map<CollectionType, Map> ms = new EnumMap<>(CollectionType.class);
      ms.put(HASH_MAP, mapPut(new HashMap(), list));
      ms.put(INT_TREE_P_MAP, pMapPlus(IntTreePMap.empty(), list));
      maps.put(lr, ms);
    }
  }

  @Benchmark
  public void linked_list_add(LinearAndRandom lr) {
    List list = new LinkedList();
    collectionAdd(list, baseList.get(lr.testAgainst));
  }

  @Benchmark
  public void array_list_add(LinearAndRandom lr) {
    List list = new ArrayList();
    collectionAdd(list, baseList.get(lr.testAgainst));
  }

  @Benchmark
  public void consp_plus(LinearAndRandom lr) {
    ConsPStack list = ConsPStack.empty();
    pCollectionPlus(list, baseList.get(lr.testAgainst));
  }

  @Benchmark
  public void treePVector_plus(LinearAndRandom lr) {
    TreePVector vector = TreePVector.empty();
    pCollectionPlus(vector, baseList.get(lr.testAgainst));
  }

  @Benchmark
  public void hashSet_plus(LinearAndRandom lr) {
    HashSet set = new HashSet();
    collectionAdd(set, baseList.get(lr.testAgainst));
  }

  @Benchmark
  public void hashTreePMap_plus(LinearAndRandom lr) {
    PMap map = HashTreePMap.empty();
    pMapPlus(map, baseList.get(lr.testAgainst));
  }

  @Benchmark
  public void intTreePMap_plus(LinearAndRandom lr) {
    PMap map = IntTreePMap.empty();
    pMapPlus(map, baseList.get(lr.testAgainst));
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
        collections.get(lrl.testAgainst).get(lrl.testWith), baseList.get(lrl.testAgainst));
  }

  @Benchmark
  public void notContains(CollectionsWithContains lrl) {
    collectionNotContains(
        collections.get(lrl.testAgainst).get(lrl.testWith), baseList.get(lrl.testAgainst).length);
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
    listGet((List) collections.get(lrl.testAgainst).get(lrl.testWith));
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
      "HASH_TREE_P_BAG"
    })
    public CollectionType testWith;
  }

  @Benchmark
  public void iterator(CollectionsWithIterator lrl) {
    collectionIterator(collections.get(lrl.testAgainst).get(lrl.testWith));
  }

  // --- hash map construction ---

  @Benchmark
  public void put(LinearAndRandom lr) {
    mapPut(new HashMap(), baseList.get(lr.testAgainst));
  }

  // ---- contains key ----

  @State(Scope.Benchmark)
  public static class CollectionsWithContainsKey {
    @Param public BaseListType testAgainst;

    @Param({"HASH_MAP", "INT_TREE_P_MAP"})
    public CollectionType testWith;
  }

  @Benchmark
  public void mapContainsKey(CollectionsWithContainsKey lrl) {
    mapContainsKey(maps.get(lrl.testAgainst).get(lrl.testWith), baseList.get(lrl.testAgainst));
  }

  @Benchmark
  public void mapNotContainsKey(CollectionsWithContainsKey lrl) {
    mapNotContainsKey(
        maps.get(lrl.testAgainst).get(lrl.testWith), baseList.get(lrl.testAgainst).length);
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
    for (@SuppressWarnings("unused") Object e : c)
      ;
    return c;
  }
}
