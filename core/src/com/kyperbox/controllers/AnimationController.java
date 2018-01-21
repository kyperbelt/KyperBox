package com.kyperbox.controllers;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.kyperbox.objects.GameObject;

public class AnimationController extends GameObjectController{
	
	private Animation<String> current_animation;
	private float animation_elapsed;
	private float play_speed;
	private GameObject daddy; //The gameobject this controller belongs to
							  //or as i like to call it, its daddy haha
	
	@Override
	public void init(GameObject object) {
		daddy = object;
		current_animation =null;
		play_speed = 1f;
		daddy.getState().log("init animation controller");
	}
	
	/**
	 * Play speed gets applied to the update delta value
	 * to increase or decrease the animation playback speed
	 * @param play_speed
	 */
	public void setPlaySpeed(float play_speed) {
		this.play_speed = Math.max(play_speed, 0f);
	}
	
	public void setAnimation(String animation) {
		setAnimation(animation,PlayMode.LOOP);
	}
	
	/**
	 * set the animation currently played by this animation controller. 
	 * Animations are retrieved from the animation store in KyperBoxGame.java
	 * @param animation
	 * @param playmode - the animation playmode
	 */
	public void setAnimation(String animation,PlayMode playmode) {
		animation_elapsed = 0f;
		current_animation = daddy.getState().getAnimation(animation);
		current_animation.setPlayMode(playmode);
	}
	
	/**
	 * get how much time this animation has been running for
	 * @return - time elapsed in seconds.
	 */
	public float getAnimationElapsed() {
		return animation_elapsed;
	}
	
	public Animation<String> getCurrentAniamtion(){
		return current_animation;
	}
	
	/**
	 * check to see if The animation is finished by checking the animation 
	 * total duration against elapsed time
	 * @return - true if elapsed time is greater than duration.
	 */
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
