package com.kyperbox.controllers;

import com.kyperbox.objects.GameObject;

public class ParticleController extends GameObjectController{
	
	private String effect;
	private boolean post_draw;
	private boolean complete;
	private float x_scale_factor;
	private float y_scale_factor;
	private float motion_scale_factor;
	private float relative_x;
	private float relative_y;
	private float duration;
	private float elapsed;
	private float removal_rate;
	private boolean removed;
	
	/**
	 * particle effect controller.. Add this to object that you want to display particle
	 * effects.
	 * 
	 * @param effect
	 * @param post_draw
	 */
	public  ParticleController(String effect,boolean post_draw,float duration) {
		this.effect = effect;
		this.post_draw = post_draw;
		this.x_scale_factor = 1f;
		this.y_scale_factor = 1f;
		this.motion_scale_factor = 1f;
		this.relative_x = 0f;
		this.relative_y = 0f;
		this.duration = duration;
		this.removal_rate = 2f;
	}
	
	
	
	public ParticleController(String effect,boolean post_draw) {
		this(effect,post_draw,-1);
	}
	
	public ParticleController(String effect) {
		this(effect, false);
	}
	
	public void setRemovalRate(float removal_rate) {
		this.removal_rate = removal_rate;
	}
	
	public void setDuration(float duration) {
		this.duration = duration;
	}
	
	public void setRelativeX(float x) {
		this.relative_x = x;
	}
	
	public void setRelativeY(float y) {
		this.relative_y = y;
	}
	
	public void setRelativePos(float x,float y) {
		this.setRelativeX(x);
		this.setRelativeY(y);
	}
	
	public float getRelativeX() {
		return relative_x;
	}
	
	public float getRelativeY() {
		return relative_y;
	}
	
	/**
	 * if set to true this particle effect is drawn after the layer
	 * if false then it is drawn before
	 * @param post_draw
	 */
	public void setPostDraw(boolean post_draw) {
		this.post_draw = post_draw;
	}
	
	public void setEffect(String effect) {
		this.effect = effect;
	}
	
	public void setComplete(boolean complete) {
		this.complete = complete;
		elapsed = 0f;
	}
	
	public float getDuration() {
		return duration;
	}
	
	public float getRemovalRate() {
		return removal_rate;
	}
	
	@Override
	public void reset() {
		elapsed = 0;
		removed = false;
		complete = false;
		super.reset();
	}
	
	public boolean isRemoved() {
		return removed;
	}
	
	public boolean isComplete() {
		return complete;
	}
	
	public String getEffectName() {
		return effect;
	}
	
	public boolean isPostDraw() {
		return post_draw;
	}
	
	public void setXScaleFactor(float x_scale_factor) {
		this.x_scale_factor = x_scale_factor;
	}
	
	public void setYScaleFactor(float y_scale_factor) {
		this.y_scale_factor = y_scale_factor;
	}
	
	public void setMotionScaleFactor(float motion_scale_factor) {
		this.motion_scale_factor = motion_scale_factor;
	}
	
	public float getXScaleFactor() {
		return x_scale_factor;
	}
	
	public float getYScaleFactor() {
		return y_scale_factor;
	}
	
	public float getMotionScaleFactor() {
		return motion_scale_factor;
	}

	@Override
	public void init(GameObject object) {
		//setRelativePos(object.getOriginX(), object.getOriginY());
	}

	@Override
	public void update(GameObject object, float delta) {
		if(duration > 0&&!complete) {
			elapsed+=delta;
			if(elapsed >= duration) {
				complete = true;
				elapsed = 0f;
			}
		}
		if(isComplete()) {
			elapsed+=delta;
			if(elapsed>removal_rate) {
				object.removeController(this);
				removed = true;
				elapsed = 0f;
			}
			
			
		}
	}

	@Override
	public void remove(GameObject object) {
		
	}

}
