package ph.phstorage.api;

import java.util.*;

public interface ListSorter<T> {
	T get(int index);
	
	void set(int index, T value);
	
	int size();
	
	Comparator<T> comparator();
	
	default void sort() {
		List<T> list = new ArrayList<>(size());
		for (int i = 0; i < size(); i++) {
			list.set(i, get(i));
		}
		list.sort(comparator());
		for (int i = 0; i < size(); i++) {
			set(i, list.get(i));
		}
	}
}
