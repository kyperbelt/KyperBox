package com.kyperbox.controllers;

import com.badlogic.gdx.utils.Pool.Poolable;
import com.kyperbox.managers.Priority;
import com.kyperbox.objects.GameObject;

public abstract class GameObjectController implements Poolable,Priority{
	
	private int priority;
	
	/**
	 * called when the object is initiated
	 * @param object
	 */
	public abstract void init(GameObject object);
	/**
	 * called to update the 
	 * @param object
	 * @param delta
	 */
	public abstract void update(GameObject object,float delta);
	
	/**
	 * called when the manager is removed
	 * all managers are removed when an GameObject is removed
	 * TODO: see if it causes problems to call remove on a controller without having it be removed from the gameobject
	 * @param object
	 */
	public abstract void remove(GameObject object);
	
	
	public int getPriority() {
		return priority;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	@Override
	public void reset() {
		
	}

	
	

}
