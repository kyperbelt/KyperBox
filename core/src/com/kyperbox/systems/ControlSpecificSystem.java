package com.kyperbox.systems;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.utils.Array;
import com.kyperbox.objects.GameObject;
import com.kyperbox.objects.GameObject.GameObjectChangeType;
import com.kyperbox.objects.GameObjectController;

public abstract class ControlSpecificSystem extends LayerSystem {

	Array<GameObject> objects;
	Class<? extends GameObjectController> c;

	public ControlSpecificSystem(Class<? extends GameObjectController> c) {
		this.c = c;
		objects = new Array<GameObject>();
	}

	@Override
	public void gameObjectAdded(GameObject object, GameObject parent) {
		Object component = object.getController(c);
		if (component != null) {
			objects.add(object);
			added(object);
		}
	}

	@Override
	public void gameObjectChanged(GameObject object, int type, float value) {
		if (type == GameObjectChangeType.CONTROLLER) {
			Object component = object.getController(c);
			if (component == null) {
				if (objects.removeValue(object, true))
					removed(object);
			} else if (!objects.contains(object, true)) {
				objects.add(object);
				added(object);
			}
		}
	}

	@Override
	public void gameObjectRemoved(GameObject object, GameObject parent) {

		if (objects.removeValue(object, true))
			removed(object);
	}

	@Override
	public void init(MapProperties properties) {
		objects.clear();
	}

	@Override
	public void onRemove() {
		objects.clear();
	}

	@Override
	public void update(float delta) {
		update(objects, delta);
	}

	
	@Override
	public void drawDebug(ShapeRenderer shapes) {
		super.drawDebug(shapes);
		
		for (int i = 0; i < objects.size; i++) {
			GameObject o = objects.get(i);
			debugGameObject(o,shapes);
		}
		
	}
	
	public void debugGameObject(GameObject object,ShapeRenderer shapes) {
		
	}
	
	public abstract void update(Array<GameObject> objects, float delta);

	public abstract void added(GameObject object);

	public abstract void removed(GameObject object);

}
