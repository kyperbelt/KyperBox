package com.kyperbox.systems;

import java.util.Comparator;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.kyperbox.objects.GameObject;

public class YSortSystem extends LayerSystem{
	
	Array<String> ignore_array;
	
	public Comparator<Actor> ysort = new Comparator<Actor>() {
		@Override
		public int compare(Actor o1, Actor o2) {
			if(ignore_array.contains(o1.getName(), false)||ignore_array.contains(o2.getName(), false))
				return 0;
			return Float.compare(o2.getY(), o1.getY());
		}
	};
	
	public YSortSystem(String...ignore) {
		ignore_array = new Array<String>();
		if(ignore!=null) {
			ignore_array.addAll(ignore);
		}
	}
	
	@Override
	public void init(MapProperties properties) {
		
	}

	@Override
	public void gameObjectAdded(GameObject object, GameObject parent) {
		
	}

	@Override
	public void gameObjectChanged(GameObject object, int type, float value) {
		
	}

	@Override
	public void gameObjectRemoved(GameObject object, GameObject parent) {
		
	}

	@Override
	public void update(float delta) {
		getLayer().getChildren().sort(ysort);
	}

	@Override
	public void onRemove() {
	}

}
