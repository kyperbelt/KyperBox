package com.kyperbox.systems;

import java.util.Comparator;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.kyperbox.controllers.ParticleController;
import com.kyperbox.objects.GameObject;
import com.kyperbox.objects.GameObject.GameObjectChangeType;

public class ParticleSystem extends LayerSystem {
	

	private ArrayMap<GameObject, ParticleController> effects;
	private Array<PooledEffect> pre_draws;
	private Array<PooledEffect> post_draws;
	private ParticleComparator pcomp;
	private Color pc;
	private boolean enabled;
	
	public ParticleSystem() {
		enabled = true;
	}

	@Override
	public void init(MapProperties properties) {
		effects = new ArrayMap<GameObject, ParticleController>();
		pre_draws = new Array<ParticleEffectPool.PooledEffect>();
		post_draws = new Array<ParticleEffectPool.PooledEffect>();
		pcomp = new ParticleComparator();
		pc = new Color(1f, 1f, 1f, 1f);
	}

	@Override
	public void gameObjectAdded(GameObject object, GameObject parent) {
		ParticleController pc = object.getController(ParticleController.class);
		if (pc != null && !effects.containsKey(object)) {
			whenObjectAdded(object, pc);
		}
	}

	@Override
	public void gameObjectChanged(GameObject object, int type, float value) {

		if (type != GameObjectChangeType.MANAGER)
			return;

		ParticleController pc = object.getController(ParticleController.class);
		if (pc != null && !effects.containsKey(object)) {
			whenObjectAdded(object, pc);
		} else if (pc == null && effects.containsKey(object)) {
			whenObjectRemoved(object);
		}
	}

	@Override
	public void gameObjectRemoved(GameObject object, GameObject parent) {
		whenObjectRemoved(object);
	}
	
	protected void whenObjectAdded(GameObject object,ParticleController pc) {
		effects.put(object, pc);
	}
	
	protected void whenObjectRemoved(GameObject object) {
		if (effects.containsKey(object)) {
			effects.removeKey(object);
		}
	}
	
	protected void removeEffect(PooledEffect pe) {
		while(pre_draws.removeValue(pe, true));
		while(post_draws.removeValue(pe, true));
	}

	@Override
	public void preDraw(Batch batch, float parentAlpha) {
		if(!enabled)
			return;
//		boolean last_additive = pre_draws.size > 0?containsAdditive(pre_draws.first()):false;
		
		Color cc = batch.getColor();
		batch.setColor(pc.r, pc.g, pc.b, pc.a * parentAlpha);
		for (int i = 0; i < pre_draws.size; i++) {
			PooledEffect pe = pre_draws.get(i);
			
			
//			boolean contains_additive = containsAdditive(pe);
//			if(last_additive&&!contains_additive) {
//				batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
//			}
//			last_additive = contains_additive;
			
			pe.draw(batch);

		}
		
		batch.setColor(cc);
		
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void postDraw(Batch batch, float parentAlpha) {
		if(!enabled)
			return;
		Color cc = batch.getColor();
		batch.setColor(pc.r, pc.g, pc.b, pc.a * parentAlpha);
		
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
		batch.setColor(cc);
		
		batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
	}

	@Override
	public void update(float delta) {
		if(!enabled)
			return;
		pre_draws.clear();
		post_draws.clear();
		
		for (int i = 0; i < effects.size; i++) {
			Array<PooledEffect> posta = effects.getValueAt(i).getPostDraw();
			Array<PooledEffect> prea = effects.getValueAt(i).getPreDraw();
			
			post_draws.addAll(posta);
			pre_draws.addAll(prea);
			
		}
		
		pre_draws.sort(pcomp);
		post_draws.sort(pcomp);
	}

	@Override
	public void onRemove() {
		effects.clear();
		post_draws.clear();
		pre_draws.clear();
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
	
	@Override
	public void drawDebug(ShapeRenderer shapes) {
		if(!enabled)
			return;
		for (int i = 0; i < pre_draws.size; i++) {
			BoundingBox bb = pre_draws.get(i).getBoundingBox();
			
			shapes.rect(bb.getCenterX()-bb.getWidth()/2, bb.getCenterY()-bb.getHeight()/2, bb.getWidth(), bb.getHeight());
		}
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
