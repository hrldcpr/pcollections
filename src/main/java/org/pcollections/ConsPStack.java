package org.pcollections;

import java.io.Serializable;
import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;



/**
 * 
 * A simple persistent stack of non-null values.
 * <p>
 * This implementation is thread-safe (assuming Java's AbstractSequentialList is thread-safe),
 * although its iterators may not be.
 * 
 * @author harold
 *
 * @param <E>
 */
public final class ConsPStack<E> extends AbstractSequentialList<E> implements PStack<E>, Serializable {
	private static final long serialVersionUID = -7306791346408620911L;
	
//// STATIC FACTORY METHODS ////
	private static final ConsPStack<Object> EMPTY = new ConsPStack<Object>();
	
	/**
	 * @param <E>
	 * @return an empty stack
	 */
	@SuppressWarnings("unchecked")
	public static <E> ConsPStack<E> empty() {
		return (ConsPStack<E>)EMPTY; }
	
	/**
	 * @param <E>
	 * @param e
	 * @return empty().plus(e)
	 */
	public static <E> ConsPStack<E> singleton(final E e) {
		return ConsPStack.<E>empty().plus(e); }
	
	/**
	 * @param <E>
	 * @param list
	 * @return a stack consisting of the elements of list in the order of list.iterator()
	 */
	@SuppressWarnings("unchecked")
	public static <E> ConsPStack<E> from(final Collection<? extends E> list) {
		if(list instanceof ConsPStack)
			return (ConsPStack<E>)list; //(actually we only know it's ConsPStack<? extends E>)
									// but that's good enough for an immutable
									// (i.e. we can't mess someone else up by adding the wrong type to it)
		return from(list.iterator());
	}
	
	private static <E> ConsPStack<E> from(final Iterator<? extends E> i) {
		if(!i.hasNext()) return empty();
		E e = i.next();
		return from(i).plus(e);
	}

	
//// PRIVATE CONSTRUCTORS ////
	private final E first; private final ConsPStack<E> rest;
	private final int size;
	// not externally instantiable (or subclassable):
	private ConsPStack() { // EMPTY constructor
		if(EMPTY!=null)
			throw new RuntimeException("empty constructor should only be used once");
		size = 0; first=null; rest=null;
	}
	private ConsPStack(final E first, final ConsPStack<E> rest) {
		this.first = first; this.rest = rest;
		
		size = 1 + rest.size;
	}
	
	
//// REQUIRED METHODS FROM AbstractSequentialList ////
	@Override
	public int size() {
		return size; }
	
	public E get(int i) {
		if (i < 0 || i >= size)
			throw new IndexOutOfBoundsException();

		return subList(i).first;
	}

	/**
	 * Complexity:<br>
	 * <ul>
	 * <li>Head only - O(n)</li>
	 * <li>Head to last - O(n)</li>
	 * <li>Last only - O(n)</li>
	 * <li>Last to head - O(n)</li>
	 * </ul>
	 */
	@Override
	@SuppressWarnings("unchecked")
	public ListIterator<E> listIterator(final int index) {
		return new ArrayListIterator<E>((E[]) toArray(), index);
	}

	/**
	 * Complexity:<br>
	 * <ul>
	 * <li>Head only - O(1)</li>
	 * <li>Head to last - O(n)</li>
	 * </ul>
	 */
	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {
			int idx = 0;
			ConsPStack<E> next = ConsPStack.this;

			public boolean hasNext() {
				return idx < size;
			}

			public E next() {
				if (idx >= size)
					throw new NoSuchElementException();
				idx++;

				E e = next.first;
				next = next.rest;
				return e;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}


//// OVERRIDDEN METHODS FROM AbstractSequentialList ////
	@Override
	public ConsPStack<E> subList(final int start, final int end) {
		if(start<0 || end>size || start>end)
			throw new IndexOutOfBoundsException();
		if(end==size) // want a substack
			return subList(start); // this is faster
		if(start==end) // want nothing
			return empty();
		if(start==0) // want the current element
			return new ConsPStack<E>(first, rest.subList(0, end-1));
		// otherwise, don't want the current element:
		return rest.subList(start-1, end-1);
	}
	
	
//// IMPLEMENTED METHODS OF PStack ////
	public ConsPStack<E> plus(final E e) {
		return new ConsPStack<E>(e, this);
	}
	
	public ConsPStack<E> plusAll(final Collection<? extends E> list) {
		ConsPStack<E> result = this;
		for(E e : list)
			result = result.plus(e);
		return result;
	}

	public ConsPStack<E> plus(final int i, final E e) {
		if(i<0 || i>size)
			throw new IndexOutOfBoundsException();
		if(i==0) // insert at beginning
			return plus(e);
		return new ConsPStack<E>(first, rest.plus(i-1, e));
	}

	public ConsPStack<E> plusAll(final int i, final Collection<? extends E> list) {
		// TODO inefficient if list.isEmpty()
		if(i<0 || i>size)
			throw new IndexOutOfBoundsException();
		if(i==0)
			return plusAll(list);
		return new ConsPStack<E>(first, rest.plusAll(i-1, list));
	}
	
	public ConsPStack<E> minus(final Object e) {
		if(size==0)
			return this;
		if(objectEquals(first, e)) // found it
			return rest; // don't recurse (only remove one)
		// otherwise keep looking:
		ConsPStack<E> newRest = rest.minus(e);
		if(newRest==rest) return this;
		return new ConsPStack<E>(first, newRest);
	}

	public ConsPStack<E> minus(final int i) {
		return minus(get(i));
	}

	public ConsPStack<E> minusAll(final Collection<?> list) {
		if(size==0)
			return this;
		if(list.contains(first)) // get rid of current element
			return rest.minusAll(list); // recursively delete all
		// either way keep looking:
		ConsPStack<E> newRest = rest.minusAll(list);
		if(newRest==rest) return this;
		return new ConsPStack<E>(first, newRest);
	}
	
	public ConsPStack<E> with(final int i, final E e) {
		if(i<0 || i>=size)
			throw new IndexOutOfBoundsException();
		if(i==0) {
			if(objectEquals(first, e)) return this;
			return new ConsPStack<E>(e, rest);
		}
		ConsPStack<E> newRest = rest.with(i-1, e);
		if(newRest==rest) return this;
		return new ConsPStack<E>(first, newRest);
	}

	public ConsPStack<E> subList(final int start) {
		if(start<0 || start>size)
			throw new IndexOutOfBoundsException();
		if(start==0)
			return this;
		return rest.subList(start-1);
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
