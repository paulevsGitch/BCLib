package ru.bclib.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CollectionsUtil {
	/**
	 * Will return mutable copy of list.
	 * @param list {@link List} to make mutable.
	 * @return {@link ArrayList} or original {@link List} if it is mutable.
	 */
	public static <E> List<E> getMutable(List<E> list) {
		if (list instanceof ArrayList) {
			return list;
		}
		return new ArrayList<>(list);
	}
	
	/**
	 * Will return mutable copy of set.
	 * @param set {@link Set} to make mutable.
	 * @return {@link HashSet} or original {@link Set} if it is mutable.
	 */
	public static <E> Set<E> getMutable(Set<E> set) {
		if (set instanceof HashSet) {
			return set;
		}
		return new HashSet<>(set);
	}
	
	/**
	 * Will return mutable copy of map.
	 * @param map {@link Map} to make mutable.
	 * @return {@link HashMap} or original {@link Map} if it is mutable.
	 */
	public static <K, V> Map<K, V> getMutable(Map<K, V> map) {
		if (map instanceof HashMap) {
			return map;
		}
		return new HashMap<>(map);
	}
	
	/**
	 * Converts list into array.
	 * @param list {@link List} to convert.
	 * @return array of list elements. If list is empty will return empty {@link Object} array.
	 */
	public static <E> E[] toArray(List<E> list) {
		if (list.isEmpty()) {
			return (E[]) new Object[0];
		}
		E[] result = (E[]) Array.newInstance(list.get(0).getClass(), list.size());
		result = list.toArray(result);
		return result;
	};
}
