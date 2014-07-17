package org.pcollections;

import java.io.Serializable;
import java.util.*;

/**
 * A persistent deque.
 *
 * @author mtklein
 * @author Yu Kobayashi
 */
public final class AmortizedPDeque<E> extends AbstractQueue<E> implements PDeque<E>, Serializable {
	private static final AmortizedPDeque<Object> EMPTY = new AmortizedPDeque<Object>();
	private static final long serialVersionUID = 5550057968536499274L;

	@SuppressWarnings("unchecked")
	public static <E> AmortizedPDeque<E> empty() {
		return (AmortizedPDeque<E>) EMPTY;
	}

	public static <E> AmortizedPDeque<E> singleton(E e) {
		return AmortizedPDeque.<E>empty().plus(e);
	}

	public static <E> AmortizedPDeque<E> from(Collection<? extends E> collection) {
		return AmortizedPDeque.<E>empty().plusAll(collection);
	}

	private final ConsPStack<E> front;
	private final ConsPStack<E> back;

	private AmortizedPDeque() {
		front = ConsPStack.empty();
		back = ConsPStack.empty();
	}

	private AmortizedPDeque(AmortizedPDeque<E> queue, E e, boolean isInsertFront) {
		// Guarantee that there is always at least 1 element in front or back, which makes peek worst-case O(1).
		switch (queue.size()) {
			case 0:
				this.front = queue.front.plus(e);
				this.back = queue.back;
				break;

			case 1:
				// Must be front.size() == 1 && back.size() == 1 after this.
				if (queue.front.size() == 1) {
					if (isInsertFront) {
						this.front = queue.back.plus(e);
						this.back = queue.front;
					} else {
						this.front = queue.front;
						this.back = queue.back.plus(e);
					}
				} else {
					if (isInsertFront) {
						this.front = queue.front.plus(e);
						this.back = queue.back;
					} else {
						this.front = queue.back;
						this.back = queue.front.plus(e);
					}
				}
				break;

			default:
				if (isInsertFront) {
					this.front = queue.front.plus(e);
					this.back = queue.back;
				} else {
					this.front = queue.front;
					this.back = queue.back.plus(e);
				}
				break;
		}
	}

	private AmortizedPDeque(ConsPStack<E> front, ConsPStack<E> back) {
		this.front = front;
		this.back = back;
	}

	/**
	 * Complexity: Worst-case O(n) for from the head to the last
	 */
	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {
			private AmortizedPDeque<E> queue = AmortizedPDeque.this;

			public boolean hasNext() {
				return queue.size() > 0;
			}

			public E next() {
				E e = queue.getFirst(); // Might throws NoSuchElementException
				queue = queue.tail();
				return e;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Complexity: Worst-case O(n) for from the last to the head
	 */
	public Iterator<E> descendingIterator() {
		return new Iterator<E>() {
			private AmortizedPDeque<E> queue = AmortizedPDeque.this;

			public boolean hasNext() {
				return queue.size() > 0;
			}

			public E next() {
				E e = queue.getLast(); // Might throws NoSuchElementException
				queue = queue.init();
				return e;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Complexity: Worst-case O(n)
	 */
	@Override
	public int hashCode() {
		int hashCode = 0;
		for (E e : this) {
			hashCode += (e == null ? 0 : e.hashCode());
		}
		return hashCode;
	}

	/**
	 * Complexity: Worst-case O(n)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object that) {
		if (!(that instanceof PDeque))
			return false;
		PDeque queue = (PDeque) that;

		if (size() != queue.size())
			return false;

		if (size() == 0) {
			return queue.size() == 0;
		} else {
			Object obj1 = peek();
			Object obj2 = queue.peek();
			return (obj1 == null ? obj2 == null : obj1.equals(obj2)) && tail().equals(queue.tail());
		}
	}

	/**
	 * Complexity: Worst-case O(1)
	 */
	@Override
	public int size() {
		return front.size() + back.size();
	}

	/**
	 * Complexity: Worst-case O(1)
	 */
	public E peekFirst() {
		switch (size()) {
			case 0:
				return null;
			case 1:
				return front.size() > 0 ? front.get(0) : back.get(0);
			default:
				return front.get(0);
		}
	}

	/**
	 * Complexity: Worst-case O(1)
	 */
	public E getFirst() {
		if (size() == 0)
			throw new NoSuchElementException();
		return peekFirst();
	}

	/**
	 * Complexity: Worst-case O(1)
	 */
	public E peek() {
		return peekFirst();
	}

	/**
	 * Complexity: Worst-case O(1)
	 */
	public E element() {
		return getFirst();
	}

	/**
	 * Complexity: Worst-case O(1)
	 */
	public E head() {
		return getFirst();
	}

	/**
	 * Complexity: Worst-case O(1)
	 */
	public E first() {
		return getFirst();
	}

	/**
	 * Complexity: Worst-case O(1)
	 */
	public E peekLast() {
		switch (size()) {
			case 0:
				return null;
			case 1:
				return front.size() > 0 ? front.get(0) : back.get(0);
			default:
				return back.get(0);
		}
	}

	/**
	 * Complexity: Worst-case O(1)
	 */
	public E getLast() {
		if (size() == 0)
			throw new NoSuchElementException();
		return peekLast();
	}

	/**
	 * Complexity: Worst-case O(1)
	 */
	public E last() {
		return getLast();
	}

	/**
	 * Complexity:<br>
	 * <table>
	 * <thead><tr><th>&nbsp;</th><th>Average-case</th><th>Amortized</th><th>Worst-case</th></tr></thead>
	 * <tbody>
	 * <tr><td>Used as Deque</td><td>O(1)</td><td>O(n)</td><td>O(n)</td></tr>
	 * <tr><td>Used as Queue</td><td>O(1)</td><td>O(1)</td><td>O(n)</td></tr>
	 * <tr><td>Used as Stack</td><td>O(1)</td><td>O(1)</td><td>O(1)</td></tr>
	 * </tbody>
	 * </table>
	 */
	public AmortizedPDeque<E> minus() {
		return tail();
	}

	/**
	 * Complexity:<br>
	 * <table>
	 * <thead><tr><th>&nbsp;</th><th>Average-case</th><th>Amortized</th><th>Worst-case</th></tr></thead>
	 * <tbody>
	 * <tr><td>Used as Deque</td><td>O(1)</td><td>O(n)</td><td>O(n)</td></tr>
	 * <tr><td>Used as Queue</td><td>O(1)</td><td>O(1)</td><td>O(n)</td></tr>
	 * <tr><td>Used as Stack</td><td>O(1)</td><td>O(1)</td><td>O(1)</td></tr>
	 * </tbody>
	 * </table>
	 */
	public AmortizedPDeque<E> tail() {
		switch (size()) {
			case 0:
				throw new NoSuchElementException();
			case 1:
				return empty();
		}

		switch (front.size()) {
			case 0:
				// Should never happen
				throw new RuntimeException("Deque is broken. front.size() = 0, back.size = " + back.size());

			case 1:
				// If there's one element on front, dump back onto front,
				// but now we've already removed the head.
				return new AmortizedPDeque<E>(
						ConsPStack.<E>empty().plusAll(back.subList(1)), // reverse order
						ConsPStack.singleton(back.get(0)));

			default:
				// If there's more than one on front, we pop one off.
				return new AmortizedPDeque<E>(front.subList(1), back);
		}
	}

	/**
	 * Complexity:<br>
	 * <table>
	 * <thead><tr><th>&nbsp;</th><th>Average-case</th><th>Amortized</th><th>Worst-case</th></tr></thead>
	 * <tbody>
	 * <tr><td>Used as Deque</td><td>O(1)</td><td>O(n)</td><td>O(n)</td></tr>
	 * <tr><td>Used as Queue</td><td>O(1)</td><td>O(1)</td><td>O(n)</td></tr>
	 * <tr><td>Used as Stack</td><td>O(1)</td><td>O(1)</td><td>O(1)</td></tr>
	 * </tbody>
	 * </table>
	 */
	public AmortizedPDeque<E> init() {
		switch (size()) {
			case 0:
				throw new NoSuchElementException();
			case 1:
				return empty();
		}

		switch (back.size()) {
			case 0:
				// Should never happen
				throw new RuntimeException("Deque is broken. front.size() = " + front.size() + ", back.size = 0");

			case 1:
				return new AmortizedPDeque<E>(
						ConsPStack.singleton(front.get(0)),
						ConsPStack.<E>empty().plusAll(front.subList(1))); // reverse order

			default:
				return new AmortizedPDeque<E>(front, back.subList(1));

		}
	}

	/**
	 * Complexity: Worst-case O(1)
	 */
	public AmortizedPDeque<E> plusFirst(E element) {
		return new AmortizedPDeque<E>(this, element, true);
	}

	/**
	 * Complexity: Worst-case O(collection.size())
	 */
	public AmortizedPDeque<E> plusFirstAll(Collection<? extends E> collection) {
		AmortizedPDeque<E> result = this;
		for (E e : collection) {
			result = result.plusFirst(e);
		}
		return result;
	}

	/**
	 * Complexity: Worst-case O(1)
	 */
	public AmortizedPDeque<E> plusLast(E element) {
		return new AmortizedPDeque<E>(this, element, false);
	}

	/**
	 * Complexity: Worst-case O(collection.size())
	 */
	public AmortizedPDeque<E> plusLastAll(Collection<? extends E> collection) {
		AmortizedPDeque<E> result = this;
		for (E e : collection) {
			result = result.plusLast(e);
		}
		return result;
	}

	/**
	 * Complexity: Worst-case O(1)
	 */
	public AmortizedPDeque<E> plus(E element) {
		return plusLast(element);
	}

	/**
	 * Complexity: Worst-case O(collection.size())
	 */
	public AmortizedPDeque<E> plusAll(Collection<? extends E> collection) {
		return plusLastAll(collection);
	}

	// These 2 methods not guaranteed to be fast.

	/**
	 * Complexity: Worst-case O(n)
	 */
	public AmortizedPDeque<E> minus(Object element) {
		ArrayList<E> list = new ArrayList<E>(this);
		list.remove(element);
		return from(list);
	}

	/**
	 * Complexity: Worst-case O(n + collection.size())
	 */
	public AmortizedPDeque<E> minusAll(Collection<?> collection) {
		ArrayList<E> list = new ArrayList<E>(this);
		list.removeAll(collection);
		return from(list);
	}

	@Deprecated
	public boolean offer(E o) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public E poll() {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public void addFirst(E e) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public void addLast(E e) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public boolean offerFirst(E e) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public boolean offerLast(E e) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public E removeFirst() {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public E removeLast() {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public E pollFirst() {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public E pollLast() {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public boolean removeFirstOccurrence(Object o) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public boolean removeLastOccurrence(Object o) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public void push(E e) {
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public E pop() {
		throw new UnsupportedOperationException();
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
}
