package org.pcollections;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map.Entry;



/**
 * 
 * A persistent vector of non-null elements.
 * <p>
 * This implementation is backed by an IntTreePMap and
 * supports logarithmic-time querying, setting, insertion,
 * and removal.
 * <p>
 * This implementation is thread-safe (assuming Java's AbstractList is thread-safe)
 * although its iterators may not be.
 * 
 * @author harold
 * @author Yu Kobayashi
 *
 * @param <E>
 */
public class TreePVector<E> extends AbstractList<E> implements PVector<E>, Serializable {
//// STATIC FACTORY METHODS ////
	private static final TreePVector<Object> EMPTY = new TreePVector<Object>();
	private static final long serialVersionUID = -6788530879100442978L;
	
	/**
	 * @param <E>
	 * @return an empty vector
	 */
	@SuppressWarnings("unchecked")
	public static <E> TreePVector<E> empty() {
		return (TreePVector<E>)EMPTY; }
	
	/**
	 * @param <E>
	 * @param e
	 * @return empty().plus(e)
	 */
	public static <E> TreePVector<E> singleton(final E e) {
		return TreePVector.<E>empty().plus(e); }
	
	/**
	 * @param <E>
	 * @param list
	 * @return empty().plusAll(list)
	 */
	@SuppressWarnings("unchecked")
	public static <E> TreePVector<E> from(final Collection<? extends E> list) {
		if(list instanceof TreePVector)
			return (TreePVector<E>)list; //(actually we only know it's TreePVector<? extends E>)
									// but that's good enough for an immutable
									// (i.e. we can't mess someone else up by adding the wrong type to it)
		return TreePVector.<E>empty().plusAll(list);
	}

//// PRIVATE CONSTRUCTORS ////
	private final IntTreePMap<E> map;
	
	private TreePVector() {
		this(IntTreePMap.<E>empty());
	}
	
	private TreePVector(final IntTreePMap<E> map) {
		this.map = map; }
	

//// REQUIRED METHODS FROM AbstractList ////
	@Override
	public int size() {
		return map.size(); }
	
	@Override
	public E get(final int index) {
		if(index<0 || index>=size())
			throw new IndexOutOfBoundsException();
		return map.get(index);
	}
	

//// OVERRIDDEN METHODS FROM AbstractList ////
	/**
	 * Complexity:<br>
	 * <ul>
	 * <li>Head only - O(log n)</li>
	 * <li>Head to last - O(n log n)</li>
	 * </ul>
	 */
	@Override
	public Iterator<E> iterator() {
		return map.values().iterator(); }
	
	/**
	 * Complexity:<br>
	 * <ul>
	 * <li>Head only - O(n log n)</li>
	 * <li>Head to last - O(n log n)</li>
	 * <li>Last only - O(n log n)</li>
	 * <li>Last to head - O(n log n)</li>
	 * </ul>
	 */
	@Override
	@SuppressWarnings("unchecked")
	public ListIterator<E> listIterator(int index) {
		return new ArrayListIterator<E>((E[]) toArray(), index);
	}

	@Override
	public TreePVector<E> subList(final int start, final int end) {
		final int size = size();
		if(start<0 || end>size || start>end)
			throw new IndexOutOfBoundsException();
		if(start==end)
			return empty();
		if(start==0) {
			if(end==size) return this;
			// remove from end:
			return this.minus(size-1).subList(start, end);
		}
		// remove from start:
		return this.minus(0).subList(start-1,end-1);
	}
	
	public TreePVector<E> subList(int start) {
		return subList(start, size());
	}

////IMPLEMENTED METHODS OF PVector ////
	public TreePVector<E> plus(final E e) {
		return new TreePVector<E>( map.plus(size(), e) ); }

	public TreePVector<E> plus(final int i, final E e) {
		if(i<0 || i>size())
			throw new IndexOutOfBoundsException();
		return new TreePVector<E>( map.withKeysChangedAbove(i,1).plus(i,e) );
	}

	public TreePVector<E> minus(final Object e) {
		for(Entry<Integer,E> entry : map.entrySet())
			if(objectEquals(entry.getValue(), e))
				return minus(entry.getKey().intValue());
		return this;
	}

	public TreePVector<E> minus(final int i) {
		if(i<0 || i>=size())
			throw new IndexOutOfBoundsException();
		return new TreePVector<E>( map.minus(i).withKeysChangedAbove(i,-1) );
	}

	public TreePVector<E> plusAll(final Collection<? extends E> list) {
		TreePVector<E> result = this;
		for(E e : list)
			result = result.plus(e);
		return result;
	}

	public TreePVector<E> minusAll(final Collection<?> list) {
		TreePVector<E> result = this;
		for(Object e : list)
			result = result.minus(e);
		return result;
	}

	public TreePVector<E> plusAll(int i, final Collection<? extends E> list) {
		if(i<0 || i>size())
			throw new IndexOutOfBoundsException();
		if(list.size()==0)
			return this;
		IntTreePMap<E> map = this.map.withKeysChangedAbove(i, list.size());
		for(E e : list)
			map = map.plus(i++, e);
		return new TreePVector<E>( map );
	}

	public TreePVector<E> with(final int i, final E e) {
		if(i<0 || i>=size())
			throw new IndexOutOfBoundsException();
		IntTreePMap<E> map = this.map.plus(i, e);
		if(map==this.map) return this;
		return new TreePVector<E>( map );
	}
	
	private static boolean objectEquals(Object a, Object b) {
		return a == null ? b == null : a.equals(b);
	}

	@Override
	@Deprecated
	public boolean add(E e) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public void add(int index, E e) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public E remove(int index) {
		throw new UnsupportedOperationException();
	}
}

