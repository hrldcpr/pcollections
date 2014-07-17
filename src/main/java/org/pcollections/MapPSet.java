package org.pcollections;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;


/**
 * 
 * A map-backed persistent set.
 * <p>
 * If the backing map is thread-safe, then this implementation is thread-safe
 * (assuming Java's AbstractSet is thread-safe), although its iterators
 * may not be.
 * 
 * @author harold
 *
 * @param <E>
 */
public final class MapPSet<E> extends AbstractSet<E> implements PSet<E>, Serializable {
	private static final MapPSet<Object> EMPTY = from(HashPMap.empty());
	private static final long serialVersionUID = -1344047864698590249L;

//// STATIC FACTORY METHODS ////
	/**
	 * @return an empty set
	 */
	@SuppressWarnings("unchecked")
	public static <E> MapPSet<E> empty() {
		return (MapPSet<E>) EMPTY;
	}

	/**
	 * @return empty().plus(e)
	 */
	public static <E> MapPSet<E> singleton(E e) {
		return MapPSet.<E>empty().plus(e);
	}

	/**
	 * @return empty().plus(list)
	 */
	public static <E> MapPSet<E> from(Collection<? extends E> list) {
		return MapPSet.<E>empty().plusAll(list);
	}

	/**
	 * @param <E>
	 * @param map
	 * @return a PSet with the elements of map.keySet(), backed by map
	 */
	@SuppressWarnings("unchecked")
	public static <E> MapPSet<E> from(final PMap<E,?> map) {
		return new MapPSet<E>((PMap<E,Object>)map); }
	
	/**
	 * @param <E>
	 * @param map
	 * @param e
	 * @return from(map).plus(e)
	 */
	public static <E> MapPSet<E> from(final PMap<E,?> map, E e) {
		return from(map).plus(e); }
	
	/**
	 * @param <E>
	 * @param map
	 * @param list
	 * @return from(map).plusAll(list)
	 */
	public static <E> MapPSet<E> from(final PMap<E,?> map, final Collection<? extends E> list) {
		return from(map).plusAll(list); }
	

//// PRIVATE CONSTRUCTORS ////
	private final PMap<E,Object> map;
	// not instantiable (or subclassable):
	private MapPSet(final PMap<E,Object> map) {
		this.map = map; }


//// REQUIRED METHODS FROM AbstractSet ////
	@Override
	public Iterator<E> iterator() {
		return map.keySet().iterator(); }
	@Override
	public int size() {
		return map.size(); }
	

//// OVERRIDDEN METHODS OF AbstractSet ////
	@Override
	public boolean contains(final Object e) {
		return map.containsKey(e); }

	
//// IMPLEMENTED METHODS OF PSet ////
	private static enum In {IN}
	
	public MapPSet<E> plus(final E e) {
		if(contains(e)) return this;
		return new MapPSet<E>(map.plus(e, In.IN));
	}
	
	public MapPSet<E> minus(final Object e) {
		if(!contains(e)) return this;
		return new MapPSet<E>(map.minus(e));
	}

	public MapPSet<E> plusAll(final Collection<? extends E> list) {
		PMap<E,Object> map = this.map;
		for(E e : list)
			map = map.plus(e, In.IN);
		return from(map);
	}
	
	public MapPSet<E> minusAll(final Collection<?> list) {
		PMap<E,Object> map = this.map.minusAll(list);
		return from(map);
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
