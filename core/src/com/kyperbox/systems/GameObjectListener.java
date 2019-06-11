package com.kyperbox.systems;

import com.kyperbox.objects.GameObject;

public interface GameObjectListener {
	
	public void objectAdded(GameObject object);
	public void objectRemoved(GameObject object);

}
