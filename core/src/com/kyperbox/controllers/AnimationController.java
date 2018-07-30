package com.kyperbox.controllers;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.kyperbox.objects.GameObject;
import com.kyperbox.objects.GameObjectController;
import com.kyperbox.umisc.KyperSprite;
import com.kyperbox.umisc.StringUtils;

public class AnimationController extends GameObjectController{
	
	private Animation<KyperSprite> current_animation;
	private ObjectMap<String, String> animations;
	private String animation_name;
	private String current;
	private AnimationListener listener;
	private  int playtimes = 0;
	private float animation_elapsed;
	private PlayMode mode;
	private float play_speed = 1f;;
	private GameObject daddy; //The gameobject this controller belongs to
							  //or as i like to call it, its daddy haha
	
	public AnimationController() {
		animations = new ObjectMap<String, String>();
	}
	
	public void setListener(AnimationListener listener) {
		this.listener = listener;
	}
	
	public String getCurrent() {
		return current == null ? animation_name : current;
	}
	
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
	
	public void setPlayMode(PlayMode mode) {
		if(current_animation!=null) {
			current_animation.setPlayMode(mode);
		}else {
			this.mode = mode;
		}
	}
	
	/**
	 * add an animation to this animation controller with a given name
	 * @param name
	 * @param animation
	 */
	public void addAnimation(String name,String animation) {
		this.animations.put(name, animation);
		if(animation_name==null) {
			set(name);
		}
	}
	
	public void set(String name,PlayMode mode) {
		if(animations.containsKey(name)) {
			setAnimation(animations.get(name),mode);
		}else {
			throw new IllegalArgumentException(StringUtils.format("AnimationController from object[%s] could not find the animation[%s].",daddy.getName(),name));
		}
	}
	
	public void set(String name) {
		set(name,PlayMode.LOOP);
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
		playtimes = 0;
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
	
	public String getAnimationName() {
		return animation_name;
	}
	
	/**
	 * check to see if The animation is finished by checking the animation 
	 * total duration against elapsed time
	 * @return - true if elapsed time is greater than duration.
	 */
	public boolean isAnimationFinished() {
		if(current_animation == null) {
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
			if(isAnimationFinished()) {
				playtimes++;
				if(listener!=null)
					listener.finished(playtimes);
			}
		}
	}

	@Override
	public void remove(GameObject object) {
		daddy = null;
	}
	
	public static interface AnimationListener{
		/**
		 * the animation has finished
		 * @param times - how many times it has finished
		 */
		public void finished(int times);
	}

}
