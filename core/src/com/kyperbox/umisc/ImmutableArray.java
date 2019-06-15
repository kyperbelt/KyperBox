package com.kyperbox.umisc;

import java.util.Iterator;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Array.ArrayIterable;

public class ImmutableArray<T> implements Iterable<T> {
	
	final Array<T> array;
	private ArrayIterable<T> iterable;

	public ImmutableArray (Array<T> array) {
		this.array = array;
	}

	public int size () {
		return array.size;
	}

	public T get (int index) {
		return array.get(index);
	}

	public boolean contains (T value, boolean identity) {
		return array.contains(value, identity);
	}

	public int indexOf (T value, boolean identity) {
		return array.indexOf(value, identity);
	}

	public int lastIndexOf (T value, boolean identity) {
		return array.lastIndexOf(value, identity);
	}

	public T peek () {
		return array.peek();
	}

	public T first () {
		return array.first();
	}

	public T random () {
		return array.random();
	}

	public T[] toArray () {
		return array.toArray();
	}

	public <V> V[] toArray (Class<?> type) {
		return array.toArray(type);
	}
	
	public int hashCode() {
		return array.hashCode();
	}

	public boolean equals (Object object) {
		return array.equals(object);
	}

	public String toString () {
		return array.toString();
	}

	public String toString (String separator) {
		return array.toString(separator);
	}

	@Override
	public Iterator<T> iterator () {
		if (iterable == null) {
			iterable = new ArrayIterable<T>(array, false);
		}

		return iterable.iterator();
	}

}
