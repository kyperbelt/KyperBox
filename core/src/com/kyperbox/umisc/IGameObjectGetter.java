package com.kyperbox.umisc;

import com.kyperbox.objects.GameObject;

public interface IGameObjectGetter {

	/**
	 * get an instance of a gameobject or its subclasses
	 * @return
	 */
	public GameObject getGameObject();
}
