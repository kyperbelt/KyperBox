package com.kyperbox.systems;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.utils.Array;
import com.kyperbox.objects.GameObject;
import com.kyperbox.objects.GameObject.GameObjectChangeType;

public abstract class ControlSpecificSystem extends LayerSystem {

	Array<GameObject> objects;
	Class<?> c;

	public ControlSpecificSystem(Class<?> c) {
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
				objects.removeValue(object, true);
				removed(object);
			} else if (!objects.contains(object, true)) {
				objects.add(object);
				added(object);
			}
		}
	}

	@Override
	public void gameObjectRemoved(GameObject object, GameObject parent) {
		Object component = object.getController(c);
		if (component == null) {
			objects.removeValue(object, true);
			removed(object);
		}
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
		update(objects,delta);
	}	

	public abstract void update(Array<GameObject> objects, float delta);

	public abstract void added(GameObject object);

	public abstract void removed(GameObject object);

}
