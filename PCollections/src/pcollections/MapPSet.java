package pcollections;

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
public final class MapPSet<E> extends AbstractSet<E> implements PSet<E> {
//// STATIC FACTORY METHODS ////
	/**
	 * @param <E>
	 * @param map
	 * @return a PSet with the elements of map.keySet(), backed by map
	 */
	@SuppressWarnings("unchecked")
	public static <E> MapPSet<E> from(final PMap<E,?> map) {
		return new MapPSet<E>((PMap<E,Object>)map, map.keySet().hashCode()); }
	
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
	private final int hashCode;
	// not instantiable (or subclassable):
	private MapPSet(final PMap<E,Object> map, final int hashCode) {
		this.map = map; this.hashCode = hashCode; }


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
	@Override
	public int hashCode() {
		return hashCode; }

	
//// IMPLEMENTED METHODS OF PSet ////
	private static enum In {IN}
	
	public MapPSet<E> plus(final E e) {
		if(contains(e)) return this;
		return new MapPSet<E>(map.plus(e, In.IN), hashCode+e.hashCode());
	}
	
	public MapPSet<E> minus(final Object e) {
		if(!contains(e)) return this;
		return new MapPSet<E>(map.minus(e), hashCode-e.hashCode());
	}

	public MapPSet<E> plusAll(final Collection<? extends E> list) {
		PMap<E,Object> map = this.map;
		for(E e : list)
			map = map.plus(e, In.IN);
		return from(map); // (completely recomputes hashCode)
	}
	
	public MapPSet<E> minusAll(final Collection<?> list) {
		PMap<E,Object> map = this.map.minusAll(list);
		return from(map); // (completely recomputes hashCode)
	}
}
