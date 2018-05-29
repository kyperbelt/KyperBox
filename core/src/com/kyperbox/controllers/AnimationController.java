package com.kyperbox.controllers;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.kyperbox.objects.GameObject;
import com.kyperbox.umisc.KyperSprite;

public class AnimationController extends GameObjectController{
	
	private Animation<KyperSprite> current_animation;
	private String animation_name;
	private float animation_elapsed;
	private PlayMode mode;
	private float play_speed = 1f;;
	private GameObject daddy; //The gameobject this controller belongs to
							  //or as i like to call it, its daddy haha
	
	@Override
	public void init(GameObject object) {
		daddy = object;
		if(animation_name!=null ) {
			loadAnimation();
		}
	}
	
	private void loadAnimation() {
		animation_elapsed = 0f;
		current_animation = daddy.getState().getAnimation(animation_name);
		current_animation.setPlayMode(mode);
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
		animation_name = animation;
		mode = playmode;
		if(daddy!=null) {
			loadAnimation();
		}
	}
	
	/**
	 * get how much time this animation has been running for
	 * @return - time elapsed in seconds.
	 */
	public float getAnimationElapsed() {
		return animation_elapsed;
	}
	
	public Animation<KyperSprite> getCurrentAnimation(){
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
			KyperSprite rawsprite = current_animation.getKeyFrame(animation_elapsed);
			object.setRawSprite(rawsprite);
		}
	}

	@Override
	public void remove(GameObject object) {
		daddy = null;
	}

}
