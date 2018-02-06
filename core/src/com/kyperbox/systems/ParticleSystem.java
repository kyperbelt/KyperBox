package com.kyperbox.systems;

import java.util.Comparator;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.kyperbox.controllers.ParticleController;
import com.kyperbox.objects.GameLayer.LayerCamera;
import com.kyperbox.objects.GameObject;
import com.kyperbox.objects.GameObject.GameObjectChangeType;

public class ParticleSystem extends LayerSystem {

	private ArrayMap<GameObject, PooledEffect> effects;
	private Array<PooledEffect> pre_draws;
	private Array<PooledEffect> post_draws;
	private Vector2 pos_check;
	private LayerCamera cam;
	private float last_zoom;
	private ParticleComparator pcomp;

	@Override
	public void init(MapProperties properties) {
		effects = new ArrayMap<GameObject, ParticleEffectPool.PooledEffect>();
		pre_draws = new Array<ParticleEffectPool.PooledEffect>();
		post_draws = new Array<ParticleEffectPool.PooledEffect>();
		pos_check = new Vector2();
		cam = getLayer().getCamera();
		last_zoom = cam.getZoom();
		pcomp = new ParticleComparator();
	}

	@Override
	public void gameObjectAdded(GameObject object, GameObject parent) {
		ParticleController pc = object.getController(ParticleController.class);
		if (pc != null && !effects.containsKey(object)) {
			PooledEffect pe = getLayer().getState().getEffect(pc.getEffectName());
			pe.reset();
			effects.put(object, pe);

			pe.setEmittersCleanUpBlendFunction(false);
			pos_check.set((getLayer().getX() / cam.getZoom() + object.getAbsoluteX() + pc.getRelativeX()) * cam.getZoom(),
					(getLayer().getY() / cam.getZoom() + object.getAbsoluteY() + pc.getRelativeY()) * cam.getZoom());
			pe.scaleEffect(pc.getXScaleFactor() * cam.getZoom(), pc.getYScaleFactor() * cam.getZoom(),
					pc.getMotionScaleFactor() * cam.getZoom());

			if (pc.isPostDraw()) {
				post_draws.add(pe);
			} else {
				pre_draws.add(pe);
			}
		}
	}

	@Override
	public void gameObjectChanged(GameObject object, int type, float value) {

		if (type != GameObjectChangeType.MANAGER)
			return;

		ParticleController pc = object.getController(ParticleController.class);
		if (pc != null && !effects.containsKey(object)) {
			PooledEffect pe = getLayer().getState().getEffect(pc.getEffectName());
			pe.reset();
			effects.put(object, pe);

			pe.setEmittersCleanUpBlendFunction(false);
			pos_check.set(
					(getLayer().getX() / cam.getZoom() + object.getAbsoluteX() + pc.getRelativeX()) * cam.getZoom(),
					(getLayer().getY() / cam.getZoom() + object.getAbsoluteY() + pc.getRelativeY()) * cam.getZoom());
			pe.setPosition(pos_check.x, pos_check.y);
			pe.scaleEffect(pc.getXScaleFactor() * cam.getZoom(), pc.getYScaleFactor() * cam.getZoom(),
					pc.getMotionScaleFactor() * cam.getZoom());

			if (pc.isPostDraw()) {
				post_draws.add(pe);
			} else {
				pre_draws.add(pe);
			}
		} else if (effects.containsKey(object)) {
			PooledEffect pe = effects.get(object);
			effects.removeKey(object);
			removeEffect(pe);
			pe.free();
		}
	}

	@Override
	public void gameObjectRemoved(GameObject object, GameObject parent) {
		if (effects.containsKey(object)) {
			PooledEffect pe = effects.get(object);
			effects.removeKey(object);
			removeEffect(pe);
			pe.free();
		}
	}
	
	public void removeEffect(PooledEffect pe) {
		while(pre_draws.removeValue(pe, true));
		while(post_draws.removeValue(pe, true));
	}

	@Override
	public void preDraw(Batch batch, float parentAlpha) {
//		boolean last_additive = pre_draws.size > 0?containsAdditive(pre_draws.first()):false;
		
		for (int i = 0; i < pre_draws.size; i++) {
			PooledEffect pe = pre_draws.get(i);
			
//			boolean contains_additive = containsAdditive(pe);
//			if(last_additive&&!contains_additive) {
//				batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
//			}
//			last_additive = contains_additive;
			
			pe.draw(batch);

		}
		
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	}

	@Override
	public void postDraw(Batch batch, float parentAlpha) {
//		boolean last_additive = post_draws.size > 0?containsAdditive(post_draws.first()):false;
		for (int i = 0; i < post_draws.size; i++) {
			PooledEffect pe = post_draws.get(i);
//			boolean contains_additive = containsAdditive(pe);
//			if(last_additive&&!contains_additive) {
//				batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
//			}
//			last_additive = contains_additive;
			
			pe.draw(batch);
		}
		
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
	}

	@Override
	public void update(float delta) {
		for (int i = 0; i < effects.size; i++) {
			GameObject o = effects.getKeyAt(i);
			ParticleController pc = o.getController(ParticleController.class);
			PooledEffect pe = effects.getValueAt(i);
			if (pc.isComplete())
				pe.allowCompletion();
			pos_check.set((getLayer().getX() / cam.getZoom() + o.getAbsoluteX() + pc.getRelativeX()) * cam.getZoom(),
					(getLayer().getY() / cam.getZoom() + o.getAbsoluteY() + pc.getRelativeY()) * cam.getZoom());
			// cam.unproject(pos_check);
			pe.update(delta);
			if (cam.getZoom() != last_zoom) {
				float zoom_dif = last_zoom - cam.getZoom();
				pe.scaleEffect(1f - zoom_dif, 1f - zoom_dif, 1f - zoom_dif);
				last_zoom = cam.getZoom();
			}
			pre_draws.sort(pcomp);
			post_draws.sort(pcomp);
			pe.setPosition(pos_check.x, pos_check.y);
		}
	}

	@Override
	public void onRemove() {

	}

	private static boolean containsAdditive(PooledEffect effect) {
		boolean additive = false;
		Array<ParticleEmitter> emmiters = effect.getEmitters();
		for (int i = 0; i < emmiters.size; i++) {
			if (emmiters.get(i).isAdditive()) {
				additive = true;
				break;
			}
		}
		return additive;
	}

	public static class ParticleComparator implements Comparator<PooledEffect> {

		@Override
		public int compare(PooledEffect o1, PooledEffect o2) {
			boolean o1_add = false;
			boolean o2_add = false;
			o1_add = containsAdditive(o1);
			o2_add = containsAdditive(o2);
			if (o1_add && !o2_add)
				return 11;
			else if (o2_add && !o1_add)
				return -1;
			return 0;
		}

	}

}
