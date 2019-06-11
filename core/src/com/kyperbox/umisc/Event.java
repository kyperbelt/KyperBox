package com.kyperbox.umisc;

import com.badlogic.gdx.utils.SnapshotArray;

public class Event<T> {

	SnapshotArray<EventListener<T>> listeners;

	public Event() {
		listeners = new SnapshotArray<EventListener<T>>();
	}

	public void add(EventListener<T> listener) {
		listeners.add(listener);
	}

	public void remove(EventListener<T> listener) {
		listeners.removeValue(listener, true);
	}
	
	public void removeAllListeners() {
		listeners.clear();
	}

	@SuppressWarnings("unchecked")
	public void fire(T object) {
		final Object[] list = listeners.begin();
		for (int i = 0; i < listeners.size; i++) {
			EventListener<T> listener = (EventListener<T>)list[i];
			listener.process(this, object);
		}
		listeners.end();
	}

}
