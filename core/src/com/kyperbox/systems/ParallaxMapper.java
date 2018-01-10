package com.kyperbox.systems;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kyperbox.objects.GameLayer;
import com.kyperbox.objects.GameLayer.LayerCamera;
import com.kyperbox.objects.GameObject;
import com.kyperbox.objects.ScrollingBackground;

public class ParallaxMapper extends LayerSystem {

	private GameLayer cam_layer;
	private Array<ParallaxData> parallax_mappings;

	// translate vars
	private float last_x;
	private float last_y;
	private float diff_x;
	private float diff_y;

	public ParallaxMapper() {
		this(null);
	}

	public ParallaxMapper(GameLayer cam_layer) {
		this.cam_layer = cam_layer;
		parallax_mappings = new Array<ParallaxMapper.ParallaxData>();
		last_x = 0;
		last_y = 0;
		diff_x = 0;
		diff_y = 0;
	}

	public void setCamLayer(GameLayer cam_layer) {
		this.cam_layer = cam_layer;
	}

	public GameLayer getCamLayer() {
		return cam_layer;
	}

	public void addMapping(String object_name, float x_scale,float y_scale, boolean exact) {
		if (containsMapping(object_name) == -1) {
			parallax_mappings.add(new ParallaxData(object_name, exact, x_scale,y_scale));
		}
	}

	public void removeMapping(String object_name) {
		int index = containsMapping(object_name);
		if (index != -1) {
			parallax_mappings.removeIndex(index).backgrounds.clear();
		}
	}

	private int containsMapping(String name) {
		for (int i = 0; i < parallax_mappings.size; i++) {
			if (parallax_mappings.get(i).name.equals(name))
				return i;
		}
		return -1;
	}

	@Override
	public void init(MapProperties properties) {

	}

	@Override
	public void gameObjectAdded(GameObject object, GameObject parent) {
		if (object instanceof ScrollingBackground) {
			for (int i = 0; i < parallax_mappings.size; i++) {
				ParallaxData pd = parallax_mappings.get(i);
				if (pd.exact) {
					if (pd.name.equals(object.getName())) {
						pd.backgrounds.add((ScrollingBackground)object);
					}
				} else {
					if (object.getName().contains(pd.name)) {
						pd.backgrounds.add((ScrollingBackground)object);
					}
				}
			}
		}
	}

	@Override
	public void gameObjectChanged(GameObject object, int type, float value) {
	}

	@Override
	public void gameObjectRemoved(GameObject object, GameObject parent) {
		if (object instanceof ScrollingBackground) {
			for (int i = 0; i < parallax_mappings.size; i++) {
				ParallaxData pd = parallax_mappings.get(i);
				if(pd.backgrounds.removeValue((ScrollingBackground) object, true))
					break;
			}
		}
	}

	@Override
	public void update(float delta) {
		
		GameLayer cam_layer = this.cam_layer;
		if(cam_layer == null)
			cam_layer = getLayer();
		
		LayerCamera cam = cam_layer.getCamera();
		Vector2 pos = cam.getPosition();
		if(pos.x!=last_x || pos.y != last_y) {
			//cam pos changed
			diff_x = pos.x - last_x;
			diff_y = pos.y - last_y;
			
			last_x = pos.x;
			last_y = pos.y;
			
			for (int i = 0; i < parallax_mappings.size; i++) {
				ParallaxData pd = parallax_mappings.get(i);
				for (int j = 0; j < pd.backgrounds.size; j++) {
					pd.backgrounds.get(j).translateScrollPos(-diff_x*pd.x_scale, -diff_y*pd.y_scale);
				}
			}
		}
	}

	@Override
	public void onRemove() {
		//TODO: free all references to resources
	}

	public static class ParallaxData {

		public boolean exact; // if false uses contains
		public float x_scale;
		public float y_scale;
		public String name;
		public Array<ScrollingBackground> backgrounds;

		public ParallaxData(String name, boolean exact, float x_scale,float y_scale) {
			this.exact = exact;
			this.name = name;
			this.x_scale = x_scale;
			this.y_scale = y_scale;
			backgrounds = new Array<ScrollingBackground>();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ParallaxData))
				return false;
			ParallaxData d = (ParallaxData) obj;
			if (d.name.equals(name))
				return true;
			else
				return false;
		}

	}

}
