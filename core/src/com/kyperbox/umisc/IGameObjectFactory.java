package com.kyperbox.umisc;

import com.kyperbox.objects.GameObject;

public interface IGameObjectFactory {

	/**
	 * attempt to retrieve a game object of the given name
	 * 
	 */
	public GameObject getGameObject(String name);
	/**
	 * register a gameobject getter to the game object name
	 * @param objectname
	 * @param getter
	 */
	public void registerGameObject(String objectname,IGameObjectGetter getter);
}
