package com.kyperbox.systems;

import com.badlogic.gdx.maps.MapProperties;
import com.kyperbox.objects.GameLayer;
import com.kyperbox.objects.GameObject;
import com.kyperbox.objects.GameObject.GameObjectChangeType;
import com.kyperbox.umisc.Event;
import com.kyperbox.umisc.EventListener;


public abstract class LayerSystem extends AbstractSystem{
	
	
	private EventListener<GameObject> gameObjectAdded;
	private EventListener<GameObject> gameObjectRemoved;
	private EventListener<GameObject> gameObjectControllerChanged;
	
	public LayerSystem() {
		gameObjectAdded = new GameObjectAddedListener();
		gameObjectRemoved = new GameObjectRemovedListener();
		gameObjectControllerChanged = new GameObjectControllerChangedListener();
	}
	
	@Override
	public void addedToLayer(GameLayer layer) {
		layer.gameObjectAdded.add(gameObjectAdded);
		layer.gameObjectRemoved.add(gameObjectRemoved);
		layer.gameObjectControllerChanged.add(gameObjectControllerChanged);
		init(layer.getLayerProperties());
	}
	
	@Override
	public void removedFromLayer(GameLayer layer) {
		layer.gameObjectAdded.remove(gameObjectAdded);
		layer.gameObjectRemoved.remove(gameObjectRemoved);
		layer.gameObjectControllerChanged.remove(gameObjectControllerChanged);
		onRemove();
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
	public abstract void gameObjectChanged(GameObject object,int type,float value);
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
	
	
	public class GameObjectAddedListener implements EventListener<GameObject>{

		@Override
		public void process(Event<GameObject> event, GameObject object) {
			gameObjectAdded(object, null);
		}
		
	}
	
	public class GameObjectRemovedListener implements EventListener<GameObject>{

		@Override
		public void process(Event<GameObject> event, GameObject object) {
			gameObjectRemoved(object, null);
		}
		
	}
	
	public class GameObjectControllerChangedListener implements EventListener<GameObject>{

		@Override
		public void process(Event<GameObject> event, GameObject object) {
			gameObjectChanged(object, GameObjectChangeType.CONTROLLER, 1);
		}
		
	}
	
	
//	public void refresh() {
//		GameLayer layer = getLayer();
//		for(Actor actor:layer.getChildren()) {
//			GameObject base_object = (GameObject) actor;
//			gameObjectAdded(base_object, null);
//		}
//	}
	
}
