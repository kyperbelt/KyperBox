package com.kyperbox.systems;

import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.Timepiece;
import com.badlogic.gdx.maps.MapProperties;
import com.kyperbox.objects.GameObject;

public class AISystem extends LayerSystem{
	
	
	private boolean paused;

	@Override
	public void init(MapProperties properties) {
		paused = false;
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
	
	public void setPaused(boolean paused) {
		this.paused = paused;
	}
	
	public boolean isPaused() {
		return paused;
	}

	@Override
	public void update(float delta) {
		GdxAI.getTimepiece().update(delta);
	}

	@Override
	public void onRemove() {
		
	}

}
