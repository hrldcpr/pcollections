package org.pcollections;

import java.util.Collection;

/**
 * 
 * An unordered collection allowing duplicate elements.
 * 
 * @author harold
 *
 * @param <E>
 */
public interface PBag<E> extends PCollection<E> {
	//@Override
	public PBag<E> plus(E e);
	//@Override
	public PBag<E> plusAll(Collection<? extends E> list);
	//@Override
	public PBag<E> minus(Object e);
	//@Override
	public PBag<E> minusAll(Collection<?> list);
}
