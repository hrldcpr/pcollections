package pcollections;

import java.util.Collection;
import java.util.List;

/**
 * 
 * An immutable indexed collection.
 * 
 * @author harold
 *
 * @param <E>
 */
public interface PSequence<E> extends PCollection<E>, List<E> {
	
	//@Override
	public PSequence<E> plus(E e);
	
	//@Override
	public PSequence<E> plusAll(Collection<? extends E> list);
	
	/**
	 * @param i
	 * @param e
	 * @return a sequence consisting of the elements of this with e replacing the element at index i.
	 */
	public PSequence<E> with(int i, E e);
	
	/**
	 * @param i
	 * @param e non-null
	 * @return a sequence consisting of the elements of this with e inserted at index i.
	 */
	public PSequence<E> plus(int i, E e);
	
	/**
	 * @param i
	 * @param list
	 * @return a sequence consisting of the elements of this with list inserted at index i.
	 */
	public PSequence<E> plusAll(int i, Collection<? extends E> list);
	
	/**
	 * Returns a sequence consisting of the elements of this without the first occurrence of e.
	 */
	//@Override
	public PSequence<E> minus(Object e);
	
	//@Override
	public PSequence<E> minusAll(Collection<?> list);
	
	/**
	 * @param i
	 * @return a sequence consisting of the elements of this with the element at index i removed.
	 */
	public PSequence<E> minus(int i);

	//@Override
	public PSequence<E> subList(int start, int end);
}
