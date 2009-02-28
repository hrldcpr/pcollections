/**
 * 
 */
package pcollections;

import java.util.Collection;
import java.util.Queue;

/**
 * @author mtklein
 *
 * A persistent Queue.
 */
public interface PQueue<E> extends PCollection<E>, Queue<E> {
	/* Guaranteed to stay as a PQueue, i.e. guaranteed-fast methods */
	public E         peek();
	public PQueue<E> minus();
	public PQueue<E> plus(E e);
	public PQueue<E> plusAll(Collection<? extends E> list);
	
	
	/* May switch to other PCollection, i.e. may-be-slow methods */
	public PCollection<E> minus(Object e);
	public PCollection<E> minusAll(Collection<?> list);
}
