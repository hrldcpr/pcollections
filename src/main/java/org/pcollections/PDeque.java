package org.pcollections;

import java.util.Collection;
import java.util.Deque;

/**
 * A persistent deque.
 * 
 * @author mtklein
 * @author Yu Kobayashi
 */
public interface PDeque<E> extends PQueue<E>, Deque<E> {
	/**
	 * @return the first element of this deque
	 */
	E peek();

	/**
	 * @return the first element of this deque
	 */
	E peekFirst();

	/**
	 * @return the last element of this deque
	 */
	E peekLast();

	/**
	 * @return the first element of this deque
	 * @throws java.util.NoSuchElementException if this deque is empty
	 */
	E element();

	/**
	 * @return the first element of this deque
	 * @throws java.util.NoSuchElementException if this deque is empty
	 */
	E getFirst();

	/**
	 * @return the last element of this deque
	 * @throws java.util.NoSuchElementException if this deque is empty
	 */
	E getLast();

	/**
	 * @return the first element of this deque
	 * @throws java.util.NoSuchElementException if this deque is empty
	 */
	E first();

	/**
	 * @return the first element of this deque
	 * @throws java.util.NoSuchElementException if this deque is empty
	 */
	E head();

	/**
	 * @return the last element of this deque
	 * @throws java.util.NoSuchElementException if this deque is empty
	 */
	E last();

	/**
	 * @return a deque without its first element
	 * @throws java.util.NoSuchElementException if this deque is empty
	 */
	PDeque<E> minus();

	/**
	 * @return a deque without its first element
	 * @throws java.util.NoSuchElementException if this deque is empty
	 */
	PDeque<E> tail();

	/**
	 * @return a deque without its last element
	 * @throws java.util.NoSuchElementException if this deque is empty
	 */
	PDeque<E> init();

	/**
	 * @param element an element to append
	 * @return a deque which contains the element and all of the elements of this
	 */
	PDeque<E> plus(E element);

	/**
	 * @param element an element to append
	 * @return a deque which contains the element and all of the elements of this
	 */
	PDeque<E> plusFirst(E element);

	/**
	 * @param element an element to append
	 * @return a deque which contains the element and all of the elements of this
	 */
	PDeque<E> plusLast(E element);

	/**
	 * @param collection elements to append
	 * @return a deque which contains all of the elements of collection and this
	 */
	PDeque<E> plusAll(Collection<? extends E> collection);

	/**
	 * @param collection elements to append
	 * @return a deque which contains all of the elements of collection and this
	 */
	PDeque<E> plusFirstAll(Collection<? extends E> collection);

	/**
	 * @param collection elements to append
	 * @return a deque which contains all of the elements of collection and this
	 */
	PDeque<E> plusLastAll(Collection<? extends E> collection);

	/**
	 * @param element an element to remove
	 * @return this with a single instance of the element removed, if the element is in this deque
	 */
	PDeque<E> minus(Object element);

	/**
	 * @param collection elements to remove
	 * @return this with all elements of the collection completely removed
	 */
	PDeque<E> minusAll(Collection<?> collection);

	/**
	 * Always throws {@link UnsupportedOperationException}.
	 */
	@Deprecated
	boolean offer(E e);

	/**
	 * Always throws {@link UnsupportedOperationException}.
	 */
	@Deprecated
	E poll();

	/**
	 * Always throws {@link UnsupportedOperationException}.
	 */
	@Deprecated
	E remove();

	/**
	 * Always throws {@link UnsupportedOperationException}.
	 */
	@Deprecated
	void addFirst(E e);

	/**
	 * Always throws {@link UnsupportedOperationException}.
	 */
	@Deprecated
	void addLast(E e);

	/**
	 * Always throws {@link UnsupportedOperationException}.
	 */
	@Deprecated
	boolean offerFirst(E e);

	/**
	 * Always throws {@link UnsupportedOperationException}.
	 */
	@Deprecated
	boolean offerLast(E e);

	/**
	 * Always throws {@link UnsupportedOperationException}.
	 */
	@Deprecated
	E removeFirst();

	/**
	 * Always throws {@link UnsupportedOperationException}.
	 */
	@Deprecated
	E removeLast();

	/**
	 * Always throws {@link UnsupportedOperationException}.
	 */
	@Deprecated
	E pollFirst();

	/**
	 * Always throws {@link UnsupportedOperationException}.
	 */
	@Deprecated
	E pollLast();

	/**
	 * Always throws {@link UnsupportedOperationException}.
	 */
	@Deprecated
	boolean removeFirstOccurrence(Object o);

	/**
	 * Always throws {@link UnsupportedOperationException}.
	 */
	@Deprecated
	boolean removeLastOccurrence(Object o);

	/**
	 * Always throws {@link UnsupportedOperationException}.
	 */
	@Deprecated
	void push(E e);

	/**
	 * Always throws {@link UnsupportedOperationException}.
	 */
	@Deprecated
	E pop();
}
