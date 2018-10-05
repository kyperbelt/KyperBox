package com.kyperbox.umisc;

import com.badlogic.gdx.utils.ObjectMap;
import com.kyperbox.objects.BasicGameObject;
import com.kyperbox.objects.GameObject;
import com.kyperbox.objects.PlatformerObject;
import com.kyperbox.objects.ScrollingBackground;
import com.kyperbox.objects.ShaderObject;
import com.kyperbox.objects.TilemapLayerObject;

public class BaseGameObjectFactory implements IGameObjectFactory{

	private ObjectMap<String,IGameObjectGetter> object_getters;
	
	public BaseGameObjectFactory() {
		object_getters = new ObjectMap<String, IGameObjectGetter>();
		registerGameObject("BasicGameObject", new IGameObjectGetter() {
			@Override
			public GameObject getGameObject() {
				return new BasicGameObject();
			}
		});
		registerGameObject("PlatformerObject", new IGameObjectGetter() {
			@Override
			public GameObject getGameObject() {
				return new PlatformerObject();
			}
		});
		registerGameObject("ScrollingBackground", new IGameObjectGetter() {
			@Override
			public GameObject getGameObject() {
				return new ScrollingBackground();
			}
		});
		registerGameObject("ShaderObject", new IGameObjectGetter() {
			@Override
			public GameObject getGameObject() {
				return new ShaderObject();
			}
		});
		registerGameObject("TilemapLayerObject", new IGameObjectGetter() {
			@Override
			public GameObject getGameObject() {
				return new TilemapLayerObject();
			}
		});
	}
	
	public GameObject getGameObject(String name) {
		if(name != null && object_getters.containsKey(name)) {
			return object_getters.get(name).getGameObject();
		}
		return null;
	}

	@Override
	public void registerGameObject(String objectname, IGameObjectGetter getter) {
		object_getters.put(objectname, getter);
	}

}
