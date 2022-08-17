/*
 * Copyright (c) 2022 Ran Ari-Gur. All rights reserved.
 * Licensed under the MIT License.
 * See LICENSE file in the project root for full license information.
 */

package org.pcollections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A persistent (immutable, purely functional) balanced-binary-tree implementation, with such
 * functionality as is needed to support implementations of {@link PSortedMap} and {@link
 * PSortedSet}, namely {@link TreePMap} and {@link TreePSet}. Each instance of this class is both a
 * (sub)tree (with a host of methods for examining and manipulating that tree) and the node at the
 * root of that (sub)tree (implementing {@link Map.Entry&lt;K, V&gt;} and providing methods {@link
 * #getKey()} and {@link #getValue()} to retrieve the mapping at the node). Method Javadoc refers to
 * 'this node' or 'this tree' as appropriate.
 *
 * <p>All operations are guaranteed to complete within O(log n) time. A complete iteration pass over
 * entryIterator(boolean) completes in O(n) time. A few operations -- namely getKey, getValue,
 * isEmpty, orNullIfEmpty, and size -- complete in O(1) time.
 *
 * @param <K> the key type; serves as the key type for {@code TreePMap}, and as the element type for
 *     {@code TreePSet}. This class provides various methods that maintain the ordering and
 *     distinctness of keys based on a client-provided instance of {@code Comparator<? super K>}.
 * @param <V> the value type; serves as the value type for {@code TreePMap}. (Is ignored by {@code
 *     TreePSet}.)
 * @author Ran Ari-Gur
 * @since 3.2.0
 */
/*
 * Implementation note: this class currently implements the same balance condition as an AVL tree
 * (namely, that the two children of a non-leaf node have heights that differ by at most one). For
 * simplicity, the height of a node is stored explicitly in a private field (called 'height'); this
 * allows all rebalancing to be handled by a single static method (called 'join') that effectively
 * serves as the constructor for non-empty nodes. This is not the approach taken by Clojure and
 * Scala -- they both use red-black trees -- but over the course of implementation, I gradually
 * ended up at this approach; I found it to be much simpler and easier-to-verify overall, because
 * it made for much simpler implementations of 'minus' and 'range*', and because it enabled
 * the constructor to explicitly verify that the balance condition is always maintained.
 */
final class KVTree<K, V> implements Map.Entry<K, V>, Serializable {
  private static final long serialVersionUID = 1L;

  enum SearchType {
    LT,
    LE,
    EQ,
    GE,
    GT
  }

  /** The empty tree / leaf node. Access via {@link #empty()}. */
  private static final KVTree<?, ?> EMPTY = new KVTree<Void, Void>();

  /**
   * The height of this tree: 0 if this tree is empty, otherwise 1 + max(left.height, right.height).
   */
  private final int height;

  /**
   * @return The number of mappings in this tree.
   */
  private final int size;

  private final KVTree<K, V> left;
  private final K key;
  private final V value;
  private final KVTree<K, V> right;

  /** Constructor for the empty tree / leaf node {@link #EMPTY}. */
  private KVTree() {
    this.height = 0;
    this.size = 0;
    this.left = null;
    this.key = null;
    this.value = null;
    this.right = null;
  }

  /**
   * Constructor for a non-empty/non-leaf node. Only intended to be called via {@link #join()},
   * which takes the same parameters but also handles rebalancing if needed. (This constructor just
   * throws an exception if 'left' and 'right' have mismatched heights.)
   */
  private KVTree(final KVTree<K, V> left, final K key, final V value, final KVTree<K, V> right) {
    if (left.height + 1 < right.height || left.height > right.height + 1) {
      // should never happen -- this is a private method -- barring a bug, could only happen if
      // height reaches Integer.MAX_VALUE, which shouldn't be possible:
      throw new IllegalArgumentException();
    }
    this.height = 1 + Math.max(left.height, right.height);

    // Strictly speaking the below calculation misbehaves when size >= 2 ** 31 (in which case this
    // wraps around to negative numbers, whereas both java.util.Collection.size() and
    // java.util.Map.size() are documented to return Integer.MAX_VALUE); but a tree of that size
    // would require over 100 GB of memory without even considering the size of the keys and values,
    // so we probably don't need to support it. (Notably, the usual JDK collections don't support
    // it, and don't even try to detect it to raise a graceful exception.)
    this.size = left.size + 1 + right.size;

    this.left = left;
    this.key = key;
    this.value = value;
    this.right = right;
  }

  /**
   * Creates a tree consisting of all the mappings of 'left', in order, followed by a mapping from
   * 'key' to 'value', followed by all the mappings of 'right', in order. Handles any necessary
   * rebalancing if 'left' and 'right' have mismatched heights. (The intention is that this method
   * be the only code that calls {@link #KVTree(KVTree, K, V, KVTree)} directly; all other methods
   * should delegate to this one.)
   *
   * <p>Requires time proportional to the difference in heights between 'left' and 'right', which is
   * in O(log(max(left.size, right.size))) = O(log(left.size + right.size)).
   *
   * <p>The height of the returned tree is guaranteed to be max(left.height, right.height) plus
   * either zero or one. (This is important in ensuring the time guarantees of this method and of
   * methods that call it.)
   *
   * @return A tree containing the specified mappings in the specified order.
   */
  private static <K, V> KVTree<K, V> join(
      final KVTree<K, V> left, final K key, final V value, final KVTree<K, V> right) {
    final int leftHeight = left.height;
    final int rightHeight = right.height;

    if (leftHeight <= rightHeight + 1 && leftHeight + 1 >= rightHeight) {
      return new KVTree<>(left, key, value, right);
    } else if (leftHeight < rightHeight) {
      if (right.height == left.height + 2 && right.left.height > right.right.height) {
        // If we're here, then there's a height H such that:
        //   left.height = H
        //   right.height = H + 2
        //     right.left.height = H + 1
        //       right.left.left.height = either H - 1 or H
        //       right.left.right.height = either H - 1 or H
        //     right.right.height = H
        // So we just need to right-rotate 'right', then combine:
        return new KVTree<>(
            new KVTree<>(left, key, value, right.left.left),
            right.left.key,
            right.left.value,
            new KVTree<>(right.left.right, right.key, right.value, right.right));
      } else {
        return join(join(left, key, value, right.left), right.key, right.value, right.right);
      }
    } else {
      if (left.height == right.height + 2 && left.right.height > left.left.height) {
        // If we're here, then there's a height H such that:
        //   left.height = H + 2
        //     left.left.height = H
        //     left.right.height = H + 1
        //       left.right.left.height = either H - 1 or H
        //       left.right.right.height = either H - 1 or H
        //   right.height = H
        // So we just need to left-rotate 'left', then combine:
        return new KVTree<>(
            new KVTree<>(left.left, left.key, left.value, left.right.left),
            left.right.key,
            left.right.value,
            new KVTree<>(left.right.right, key, value, right));
      } else {
        return join(left.left, left.key, left.value, join(left.right, key, value, right));
      }
    }
  }

  @SuppressWarnings("unchecked")
  static <K, V> KVTree<K, V> empty() {
    return (KVTree<K, V>) EMPTY;
  }

  static <K, V> KVTree<K, V> fromEntryIterator(
      final Iterator<? extends Map.Entry<? extends K, ? extends V>> iterator) {
    return fromIterator(iterator, IteratorType.ENTRY, Integer.MAX_VALUE);
  }

  static <K, V> KVTree<K, V> fromKeyIterator(final Iterator<? extends K> iterator) {
    return fromIterator(iterator, IteratorType.KEY, Integer.MAX_VALUE);
  }

  /**
   * Whether an iterator returns entries or just keys. (This is a bit of a hack to let {@link
   * #fromEntryIterator(Iterator)} and {@link #fromKeyIterator(Iterator)} share code. A cleaner
   * alternative would be to wrap the key iterator in an entry iterator; but that would create lots
   * of unnecessary Map.Entry instances.)
   */
  private enum IteratorType {
    ENTRY,
    KEY
  }

  private static <K, V> KVTree<K, V> fromIterator(
      final Iterator<?> iterator, final IteratorType iteratorType, final int maxHeight) {
    KVTree<K, V> curr = KVTree.empty();
    while (true) {
      if (curr.height >= maxHeight) {
        assert curr.height == maxHeight;
        return curr;
      }

      if (!iterator.hasNext()) {
        return curr;
      }

      final KVTree<K, V> left = curr;

      final Object datum = iterator.next();
      @SuppressWarnings("unchecked")
      final K key =
          (K) (iteratorType == IteratorType.KEY ? datum : ((Map.Entry<?, ?>) datum).getKey());
      @SuppressWarnings("unchecked")
      final V value =
          (V) (iteratorType == IteratorType.KEY ? null : ((Map.Entry<?, ?>) datum).getValue());

      final KVTree<K, V> right = fromIterator(iterator, iteratorType, left.height);

      curr = KVTree.join(left, key, value, right);

      if (right.height < left.height) {
        return curr;
      }
    }
  }

  /**
   * Creates an iterator over the mappings in this tree.
   *
   * @param isLeftToRight - True if to iterate from left to right; false for right to left.
   * @return An iterator over the mappings in this tree in the specified direction.
   */
  Iterator<Map.Entry<K, V>> entryIterator(final boolean isLeftToRight) {
    return new EntryIterator<>(this, isLeftToRight);
  }

  /** Implements equals(...) as specified by Map.Entry. */
  @Override
  public boolean equals(final Object o) {
    final Map.Entry<?, ?> that = o instanceof Map.Entry<?, ?> ? (Map.Entry<?, ?>) o : null;
    return that != null
        && Objects.equals(this.key, that.getKey())
        && Objects.equals(this.value, that.getValue());
  }

  /**
   * @return This node's key, or null if this node is the root of the empty tree.
   */
  @Override
  public K getKey() {
    return this.key;
  }

  /**
   * @return The leftmost non-empty node in this tree.
   * @throws NoSuchElementException if this tree is empty.
   */
  KVTree<K, V> getLeftmost() {
    checkNotEmpty();
    KVTree<K, V> currNode = this;
    while (!currNode.left.isEmpty()) {
      currNode = currNode.left;
    }
    return currNode;
  }

  /**
   * @return The rightmost non-empty node in this tree.
   * @throws NoSuchElementException if this tree is empty.
   */
  KVTree<K, V> getRightmost() {
    checkNotEmpty();
    KVTree<K, V> currNode = this;
    while (!currNode.right.isEmpty()) {
      currNode = currNode.right;
    }
    return currNode;
  }

  /**
   * @return This node's value (which may be null), or null if this node is the root of the empty
   *     tree.
   */
  @Override
  public V getValue() {
    return this.value;
  }

  /** implements hashCode() as specified by Map.Entry */
  @Override
  public int hashCode() {
    return Objects.hashCode(this.key) ^ Objects.hashCode(this.value);
  }

  /**
   * @return Whether this tree contains any mappings (i.e., whether its size is 0).
   */
  boolean isEmpty() {
    return this == EMPTY;
  }

  KVTree<K, V> minus(final K key, final Comparator<? super K> comparator) {
    if (this.isEmpty()) {
      return this;
    }

    final int cmp = comparator.compare(key, this.key);
    if (cmp < 0) {
      return this.replaceLeft(this.left.minus(key, comparator));
    } else if (cmp == 0) {
      return concat(this.left, this.right);
    } else {
      return this.replaceRight(this.right.minus(key, comparator));
    }
  }

  /**
   * @return A tree with the same mappings as this one, minus the leftmost.
   * @throws NoSuchElementException if this tree is empty.
   */
  KVTree<K, V> minusLeftmost() {
    checkNotEmpty();
    if (this.left.isEmpty()) {
      return this.right;
    }
    return KVTree.join(this.left.minusLeftmost(), this.key, this.value, this.right);
  }

  /**
   * @return A tree with the same mappings as this one, minus the rightmost.
   * @throws NoSuchElementException if this tree is empty.
   */
  KVTree<K, V> minusRightmost() {
    checkNotEmpty();
    if (this.right.isEmpty()) {
      return this.left;
    }
    return KVTree.join(this.left, this.key, this.value, this.right.minusRightmost());
  }

  /**
   * @return This node, or null if this node is the root of the empty tree.
   */
  Map.Entry<K, V> orNullIfEmpty() {
    return this == EMPTY ? null : this;
  }

  KVTree<K, V> plus(final K key, final V value, final Comparator<? super K> comparator) {
    if (this.isEmpty()) {
      return KVTree.join(KVTree.empty(), key, value, KVTree.empty());
    }

    final int cmp = comparator.compare(key, this.key);
    if (cmp < 0) {
      return this.replaceLeft(this.left.plus(key, value, comparator));
    } else if (cmp == 0) {
      // Note: we use the *old* key, but the *new* value. For TreePSet<K>, this ensures that we
      // don't modify the set if we already have an equivalent element. For TreePMap<K, V>, this may
      // be a bit weird (especially when using an ordering that's not consistent with equals), but
      // java.util.TreeMap.put(...) has the corresponding behavior, so we're in good company:
      return this.replaceValue(value);
    } else {
      return this.replaceRight(this.right.plus(key, value, comparator));
    }
  }

  KVTree<K, V> range(
      final K leftBound,
      final boolean isLeftBoundInclusive,
      final K rightBound,
      final boolean isRightBoundInclusive,
      final Comparator<? super K> comparator) {
    if (this.isEmpty()) {
      return this;
    }

    final int toCmp = comparator.compare(rightBound, this.key);
    final int fromCmp = toCmp < 0 ? -1 : comparator.compare(leftBound, this.key);

    if (toCmp < 0) {
      // if we're here, then the entire range is to the left of root
      return this.left.range(
          leftBound, isLeftBoundInclusive, rightBound, isRightBoundInclusive, comparator);
    } else if (toCmp == 0) {
      // if we're here, then the range ends at root
      if (fromCmp == 0) {
        // if we're here, then the range also *starts* at root
        if (isLeftBoundInclusive && isRightBoundInclusive) {
          return KVTree.join(KVTree.empty(), this.key, this.value, KVTree.empty());
        } else {
          return KVTree.empty();
        }
      } else {
        final KVTree<K, V> left =
            this.left.rangeToRight(leftBound, isLeftBoundInclusive, comparator);
        if (isRightBoundInclusive) {
          return KVTree.join(left, this.key, this.value, KVTree.empty());
        } else {
          return left;
        }
      }
    } else if (fromCmp < 0) {
      // if we're here, then the range extends on either side of root
      return KVTree.join(
          this.left.rangeToRight(leftBound, isLeftBoundInclusive, comparator),
          this.key,
          this.value,
          this.right.rangeToLeft(rightBound, isRightBoundInclusive, comparator));
    } else if (fromCmp == 0) {
      // if we're here, then the range starts at root
      final KVTree<K, V> right =
          this.right.rangeToLeft(rightBound, isRightBoundInclusive, comparator);
      if (isLeftBoundInclusive) {
        return KVTree.join(KVTree.empty(), this.key, this.value, right);
      } else {
        return right;
      }
    } else {
      // if we're here, then the entire range is to the right of root
      return this.right.range(
          leftBound, isLeftBoundInclusive, rightBound, isRightBoundInclusive, comparator);
    }
  }

  KVTree<K, V> rangeToLeft(
      final K rightBound,
      final boolean isRightBoundInclusive,
      final Comparator<? super K> comparator) {
    if (this.isEmpty()) {
      return this;
    }

    final int cmp = comparator.compare(rightBound, this.key);
    if (cmp < 0) {
      return this.left.rangeToLeft(rightBound, isRightBoundInclusive, comparator);
    } else if (cmp == 0) {
      if (isRightBoundInclusive) {
        return KVTree.join(this.left, this.key, this.value, KVTree.empty());
      } else {
        return this.left;
      }
    } else {
      return this.replaceRight(
          this.right.rangeToLeft(rightBound, isRightBoundInclusive, comparator));
    }
  }

  KVTree<K, V> rangeToRight(
      final K leftBound,
      final boolean isleftBoundInclusive,
      final Comparator<? super K> comparator) {
    if (this.isEmpty()) {
      return this;
    }

    final int cmp = comparator.compare(leftBound, this.key);
    if (cmp < 0) {
      return this.replaceLeft(this.left.rangeToRight(leftBound, isleftBoundInclusive, comparator));
    } else if (cmp == 0) {
      if (isleftBoundInclusive) {
        return KVTree.join(KVTree.empty(), this.key, this.value, this.right);
      } else {
        return this.right;
      }
    } else {
      return this.right.rangeToRight(leftBound, isleftBoundInclusive, comparator);
    }
  }

  KVTree<K, V> search(
      final K key, final Comparator<? super K> comparator, final SearchType searchType) {
    KVTree<K, V> currNode = this;
    KVTree<K, V> candidate = KVTree.empty();
    while (true) {
      if (currNode.isEmpty()) {
        return candidate;
      }

      final int cmp = comparator.compare(key, currNode.key);
      if (cmp < 0) {
        if (searchType == SearchType.GE || searchType == SearchType.GT) {
          candidate = currNode;
        }
        currNode = currNode.left;
      } else if (cmp == 0) {
        if (searchType == SearchType.LT) {
          if (currNode.left.isEmpty()) {
            return candidate;
          } else {
            return currNode.left.getRightmost();
          }
        } else if (searchType == SearchType.LE
            || searchType == SearchType.EQ
            || searchType == SearchType.GE) {
          return currNode;
        } else {
          if (currNode.right.isEmpty()) {
            return candidate;
          } else {
            return currNode.right.getLeftmost();
          }
        }
      } else {
        if (searchType == SearchType.LT || searchType == SearchType.LE) {
          candidate = currNode;
        }
        currNode = currNode.right;
      }
    }
  }

  /**
   * @throws UnsupportedOperationException always
   * @deprecated Unsupported operation.
   */
  @Deprecated
  @Override
  public V setValue(final V value) {
    throw new UnsupportedOperationException();
  }

  /**
   * @return The number of mappings in this tree.
   */
  int size() {
    return this.size;
  }

  /**
   * implements toString() in a form expected for an implementation of Map.Entry, namely "KEY=VALUE"
   * (with no information about the presence or absence of child nodes).
   */
  @Override
  public String toString() {
    return this.key + "=" + this.value;
  }

  private void checkNotEmpty() {
    if (this.isEmpty()) {
      throw new NoSuchElementException();
    }
  }

  private KVTree<K, V> replaceLeft(final KVTree<K, V> newLeft) {
    return this.left == newLeft ? this : KVTree.join(newLeft, this.key, this.value, this.right);
  }

  private KVTree<K, V> replaceRight(final KVTree<K, V> newRight) {
    return this.right == newRight ? this : KVTree.join(this.left, this.key, this.value, newRight);
  }

  private KVTree<K, V> replaceValue(final V newValue) {
    return this.value == newValue ? this : KVTree.join(this.left, this.key, newValue, this.right);
  }

  private static <K, V> KVTree<K, V> concat(final KVTree<K, V> left, final KVTree<K, V> right) {
    if (right.isEmpty()) {
      return left;
    }
    final KVTree<K, V> mid = right.getLeftmost();
    final KVTree<K, V> restOfRight = right.minusLeftmost();
    return KVTree.join(left, mid.key, mid.value, restOfRight);
  }

  /** An iterator over the mappings of a KVTree. */
  private static class EntryIterator<K, V> implements Iterator<Map.Entry<K, V>> {
    /** whether we're iterating from left to right (vs. right to left) */
    private final boolean isLeftToRight;

    /** node that we need to enter */
    private KVTree<K, V> nextSubtree; // for laziness

    /** nodes that we've recursed into, but not yet returned */
    private final ArrayList<KVTree<K, V>> stack = new ArrayList<>();

    EntryIterator(final KVTree<K, V> tree, final boolean isLeftToRight) {
      this.isLeftToRight = isLeftToRight;
      this.nextSubtree = tree;
    }

    @Override
    public boolean hasNext() {
      return this.nextSubtree.size > 0 || this.stack.size() > 0;
    }

    @Override
    public Map.Entry<K, V> next() {
      while (this.nextSubtree.size > 0) {
        this.stack.add(this.nextSubtree);
        this.nextSubtree = firstChild(this.nextSubtree);
      }
      if (this.stack.isEmpty()) {
        throw new NoSuchElementException();
      }
      final KVTree<K, V> result = this.stack.remove(this.stack.size() - 1);
      this.nextSubtree = secondChild(result);
      return result;
    }

    private KVTree<K, V> firstChild(final KVTree<K, V> node) {
      return this.isLeftToRight ? node.left : node.right;
    }

    private KVTree<K, V> secondChild(final KVTree<K, V> node) {
      return this.isLeftToRight ? node.right : node.left;
    }
  }
}
