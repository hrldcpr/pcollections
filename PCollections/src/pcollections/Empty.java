package pcollections;

/* Mike Klein, 2/27/2009 */

/* Empty remembers which classes implement the interface you want,
 * so you don't have to.
 */

public class Empty {
	public static <E> PStack<E> stack() {
		return ConsPStack.empty();
	}
	
	public static <E> PVector<E> vector() {
		return TreePVector.empty();
	}
	
	public static <E> PSet<E> set() {
		return HashTreePSet.empty();
	}
	
	public static <E> PBag<E> bag() {
		return HashTreePBag.empty();
	}
	
	public static <K,V> PMap<K,V> map() {
		return HashTreePMap.empty();
	}
}
