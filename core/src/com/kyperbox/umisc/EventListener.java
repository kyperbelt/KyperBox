package com.kyperbox.umisc;

public interface EventListener<T> {
	
	public void process(Event<T> event,T object);

}
