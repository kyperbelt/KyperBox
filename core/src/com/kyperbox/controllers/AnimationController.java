package com.kyperbox.controllers;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.MathUtils;
import com.kyperbox.objects.GameObject;

public class AnimationController extends GameObjectController{
	
	private Animation<String> current_animation;
	private float animation_elapsed;
	private float play_speed;
	private GameObject daddy;
	
	@Override
	public void init(GameObject object) {
		daddy = object;
		current_animation =null;
		play_speed = 1f;
		daddy.getState().log("init animation controller");
	}
	
	public void setPlaySpeed(float play_speed) {
		this.play_speed = Math.max(play_speed, 0f);
	}
	
	public void setAnimation(String animation) {
		setAnimation(animation,PlayMode.LOOP);
	}
	
	public void setAnimation(String animation,PlayMode playmode) {
		animation_elapsed = 0f;
		current_animation = daddy.getGame().getAnimation(animation);
		current_animation.setPlayMode(playmode);
	}
	
	public float getAnimationElapsed() {
		return animation_elapsed;
	}
	
	public Animation<String> getCurrentAniamtion(){
		return current_animation;
	}
	
	public boolean isAnimationFinished() {
		if(current_animation == null) {
			daddy.getState().error("No animation found, finished not possible.");
			return false;
		}
		return current_animation.getAnimationDuration() <= animation_elapsed;
	}

	@Override
	public void update(GameObject object, float delta) {
		animation_elapsed+=delta*play_speed;
		if(current_animation!=null) {
			object.setSprite(current_animation.getKeyFrame(animation_elapsed));
		}
		
	}

	@Override
	public void remove(GameObject object) {
		
	}

}
