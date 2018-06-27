package com.kyperbox.controllers;

import com.badlogic.gdx.math.MathUtils;
import com.kyperbox.objects.GameObject;
import com.kyperbox.objects.GameObjectController;

public class PoiController extends GameObjectController{

	private String name;
	private float weight;
	private boolean active;
	private float elapsed;
	private float duration;
	private int activation_count;
	private int max_activations;
	private boolean in_frame;

	/**
	 * create a Point of Interest controller with
	 * @param name - name of poi
	 * @param weight - weight of poi
	 * @param duration - duration of poi: or how long the camera pays attention to it while in frame. -1 forever
	 * @param max_activations - the max number of activations this poi will have. -1 unlimited
	 */
	public PoiController(String name,float weight,float duration,int max_activations) {
		this.name = name;
		setWeight(weight);
		this.active = false;
		this.elapsed = 0f;
		this.duration = duration;
		this.activation_count = 0;
		this.max_activations = max_activations;
		this.in_frame = false;
		
	}
	
	public PoiController(String name,float weight) {
		this(name,weight,-1,-1);
	}
	
	public PoiController(String name) {
		this(name, 1f);
	}

	/**
	 * in frame activation has happened. 
	 * @return
	 */
	public boolean isInFrame() {
		return in_frame;
	}
	
	public void setInFrame(boolean in_frame) {
		this.in_frame = in_frame;
	}
	
	
	public float getWeight() {
		return weight;
	}
	
	public void setWeight(float weight) {
		this.weight = MathUtils.clamp(weight, 0f, 1f);
	}
	
	public String getPoiName() {
		return name;
	}
	
	public void setPoiName(String name) {
		this.name = name;
	}
	
	public boolean canActivate() {
		return (getMaxActivations() == -1)||(getMaxActivations()>0 && getActiveCount() < getMaxActivations());
	}
	
	public int getActiveCount() {
		return activation_count;
	}
	
	public void incrementActiveCount() {
		activation_count++;
	}
	
	public int getMaxActivations() {
		return max_activations;
	}
	
	public void setMaxActivations(int max_activations) {
		this.max_activations = max_activations;
	}

	public void setActive(boolean active) {
		if(this.active!=active)
			elapsed = 0;
		this.active = active;
		
	}
	
	public boolean isActive() {
		return active;
	}
	
	public float getDuration() {
		return duration;
	}
	
	public void setDuration(float duration) {
		this.duration = duration;
	}
	
	public void resetActivations() {
		activation_count = 0;
	}
	
	public boolean shouldDeactivate() {
		return (duration!=-1)&&(duration > 0 && elapsed >= duration);
	}
	
	
	@Override
	public void init(GameObject object) {
		resetActivations();
		active = false;
		elapsed = 0;
		
	}

	@Override
	public void update(GameObject object, float delta) {
		if(active) {
			if(duration!=-1 && duration > 0) {
				elapsed+=delta;
			}
		}
	}

	@Override
	public void remove(GameObject object) {
		
	}

}
