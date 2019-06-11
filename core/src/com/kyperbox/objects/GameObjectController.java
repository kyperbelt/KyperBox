package com.kyperbox.objects;

import com.badlogic.gdx.utils.Pool.Poolable;
import com.kyperbox.managers.Priority;

public abstract class GameObjectController implements Poolable,Priority{
	
	private int priority =0;
	private boolean removed = false;
	
	/**
	 * set removed
	 * @param removed
	 */
	protected void setRemoved(boolean removed) {
		this.removed = removed;
	}
	
	/**
	 * check if this object has been removed
	 * @return
	 */
	public boolean isRemoved() {
		return removed;
	}
	
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
