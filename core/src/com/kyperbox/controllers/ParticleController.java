package com.kyperbox.controllers;

import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.kyperbox.objects.GameLayer.LayerCamera;
import com.kyperbox.objects.GameObject;
import com.kyperbox.objects.GameObjectController;

public class ParticleController extends GameObjectController {
	
	private ArrayMap<PooledEffect, ParticleSettings> particles;
	private Array<PooledEffect> post_effects;
	private Array<PooledEffect> pre_effects;
	private GameObject parent;
	
	public ParticleController() {
		particles = new ArrayMap<ParticleEffectPool.PooledEffect, ParticleController.ParticleSettings>();
		post_effects = new Array<ParticleEffectPool.PooledEffect>();
		pre_effects = new Array<ParticleEffectPool.PooledEffect>();
	}
	
	public void addParticleEffect(PooledEffect pe,ParticleSettings settings) {
		pe.reset(true);
		pe.setEmittersCleanUpBlendFunction(false);
		pe.scaleEffect(settings.scale);
		particles.put(pe,settings);
		if(parent!=null) {
			LayerCamera cam = parent.getGameLayer().getCamera();
			Vector2 campos = cam.getPosition();
			pe.setPosition(parent.getX()+settings.xoff-campos.x-cam.getXOffset(), parent.getY()+settings.yoff-campos.y-cam.getYOffset());
		}
	}
	
	public void removeComplete() {
		int index = 0;
		while (index < particles.size) {
			if(particles.getKeyAt(index).isComplete()) {
				PooledEffect pe = particles.getKeyAt(index);
				particles.removeIndex(index);
				pe.free();
			}else
				index++;
		}
	}
	
	public void clearAll() {
		for (int i = 0; i < particles.size; i++) {
			PooledEffect pe = particles.getKeyAt(i);
			pe.free();
		}
		particles.clear();
		post_effects.clear();
		pre_effects.clear();
	}
	
	@Override
	public void init(GameObject object) {
		parent = object;
	}

	@Override
	public void update(GameObject object, float delta) {
		post_effects.clear();
		pre_effects.clear();
		LayerCamera cam = object.getGameLayer().getCamera();
		Vector2 campos = cam.getPosition();
		for (int i = 0; i < particles.size; i++) {
			ParticleSettings ps = particles.getValueAt(i);
			PooledEffect pe = particles.getKeyAt(i);
			pe.update(delta);
			pe.setPosition(object.getX()+ps.xoff-campos.x+cam.getXOffset(), object.getY()+ps.yoff-campos.y+cam.getYOffset());
			if(ps.post_draw) {
				post_effects.add(pe);
			}else {
				pre_effects.add(pe);
			}
		}
	}

	@Override
	public void remove(GameObject object) {
		parent = null;
		clearAll();
	}
	
	public Array<PooledEffect> getPostDraw(){
		return post_effects;
	}
	
	public Array<PooledEffect> getPreDraw(){
		return pre_effects;
	}
	
	
	public static class ParticleSettings{
		public float xoff;
		public float yoff;
		public float scale;
		public boolean post_draw;
		
		public ParticleSettings(boolean post_draw) {
			this.scale = 1;
			this.xoff = 0;
			this.yoff = 0;
			this.post_draw = post_draw;
		}
		
		public ParticleSettings() {this(false);}
	
	}



}
