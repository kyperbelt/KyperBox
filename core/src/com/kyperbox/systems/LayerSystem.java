package com.kyperbox.systems;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.kyperbox.managers.Priority;
import com.kyperbox.objects.GameLayer;
import com.kyperbox.objects.GameObject;
import com.kyperbox.objects.GameObject.GameObjectChangeType;

public abstract class LayerSystem implements Priority {
	
	private boolean active;
	private GameLayer layer;
	private int priority;
	
	public LayerSystem() {
		active = true;
		priority = LOW;
	}
	
	/**
	 * called when the gamelayer and tmx is fully loaded
	 * @param properties
	 */
	public abstract void init(MapProperties properties);
	/**
	 *A new gameObject has been added to the layer
	 * @param object
	 */
	public abstract void gameObjectAdded(GameObject object,GameObject parent);
	/**
	 * an object has been altered drastically
	 * right now that means that its managers have been changed
	 * @param object
	 */
	public abstract void gameObjectChanged(GameObject object,GameObjectChangeType type,int value);
	/**
	 * a game object has been removed from this 
	 */
	public abstract void gameObjectRemoved(GameObject object,GameObject parent);
	/**
	 * on layer update
	 * @param delta
	 */
	public abstract void update(float delta);
	/**
	 * when the layer is removed
	 */
	public abstract void onRemove();
	
	/**
	 * set the activity of this manager
	 * @param active
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
	
	/**
	 * if this manager is not active it will not update
	 * Game Object changes will still happen
	 * @return
	 */
	public boolean isActive() {
		return active;
	}
	
	public void setLayer(GameLayer layer) {
		this.layer = layer;
	}
	
	public GameLayer getLayer() {
		return layer;
	}
	
	@Override
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	@Override
	public int getPriority() {
		return priority;
	}
	
	public void drawDebug(ShapeRenderer shapes) {
		
	}
	
	public void refresh() {
		GameLayer layer = getLayer();
		for(Actor actor:layer.getChildren()) {
			GameObject base_object = (GameObject) actor;
			gameObjectAdded(base_object, null);
		}
	}
	
}
