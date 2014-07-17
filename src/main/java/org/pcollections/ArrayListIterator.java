package org.pcollections;

import java.io.Serializable;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Array based {@link java.util.ListIterator} for immutable collections.
 *
 * @author Yu Kobayashi
 */
public class ArrayListIterator<E> implements ListIterator<E>, Serializable {
    private static final long serialVersionUID = 3478878196035068857L;

    private final E[] ary;
    private int idx;

    ArrayListIterator(E[] ary, int idx) {
        if (idx < 0 || idx > ary.length)
            throw new IndexOutOfBoundsException();

        this.ary = ary;
        this.idx = idx;
    }

    public boolean hasNext() {
        return idx < ary.length;
    }

    public E next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return ary[idx++];
    }

    public boolean hasPrevious() {
        return idx > 0;
    }

    public E previous() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        return ary[--idx];
    }

    public int nextIndex() {
        return idx;
    }

    public int previousIndex() {
        return idx - 1;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public void set(E e) {
        throw new UnsupportedOperationException();
    }

    public void add(E e) {
        throw new UnsupportedOperationException();
    }
}
