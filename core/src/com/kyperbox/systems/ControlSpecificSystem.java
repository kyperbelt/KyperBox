package com.kyperbox.systems;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.kyperbox.controllers.ControllerGroup;
import com.kyperbox.objects.GameLayer;
import com.kyperbox.objects.GameObject;
import com.kyperbox.objects.GameObjectController;
import com.kyperbox.umisc.ImmutableArray;

public abstract class ControlSpecificSystem extends AbstractSystem {

	ImmutableArray<GameObject> objects;
	Class<? extends GameObjectController> c;
	ControllerGroup group;
	
	GameObjectListener listener = new GameObjectListener() {
		@Override
		public void objectRemoved(GameObject object) {
			removed(object);
		}
		
		@Override
		public void objectAdded(GameObject object) {
			added(object);
		}
	};

	@SuppressWarnings("unchecked")
	public ControlSpecificSystem(Class<? extends GameObjectController> c) {
		this.c = c;
		group = ControllerGroup.all(c).get();
	}
	
	@Override
	public void internalAddToLayer(GameLayer layer) {
		objects = layer.getControllerGroupObjects(group);
		layer.addGameObjectListener(group, 0, listener);
		super.internalAddToLayer(layer);
	}
	
	@Override
	public void internalRemoveFromLayer(GameLayer layer) {
		objects = null;
		layer.removeGameObjectListener(listener);
		super.internalRemoveFromLayer(layer);
	}

	@Override
	public void update(float delta) {
		update(objects, delta);
	}

	
	@Override
	public void drawDebug(ShapeRenderer shapes) {
		super.drawDebug(shapes);
		
		for (int i = 0; i < objects.size(); i++) {
			GameObject o = objects.get(i);
			debugGameObject(o,shapes);
		}
		
	}
	
	public void debugGameObject(GameObject object,ShapeRenderer shapes) {
		
	}
	
	public abstract void update(ImmutableArray<GameObject> objects, float delta);

	public abstract void added(GameObject object);

	public abstract void removed(GameObject object);
	
	

}
