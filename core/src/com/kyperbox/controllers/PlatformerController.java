package com.kyperbox.controllers;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.math.MathUtils;
import com.kyperbox.managers.Priority;
import com.kyperbox.objects.GameObject;

public class PlatformerController extends GameObjectController{
	
	private GameObject daddy;
	private PlatformState state;
	private float walk_speed;
	private float jump_speed;
	private float min_jump_speed;
	private boolean on_ground;
	private boolean was_on_ground;
	private float gravity;
	private boolean direction_left;
	private boolean walking;
	private boolean jumping;
	
	/**
	 * simple platformer movement and controls. 
	 */
	public PlatformerController() {
	}
	
	@Override
	public void init(GameObject object) {
		this.daddy = object;
		jumping = false;
		walking = false;
		state = PlatformState.STAND;
		setPriority(Priority.HIGH);
		setJumpSpeed(object.getProperties().get("min_jump_speed", 200f,Float.class),object.getProperties().get("max_jump_speed", 600f,Float.class));
		setGravity(object.getProperties().get("gravity", -12f,Float.class));
		setWalkSpeed(object.getProperties().get("walk_speed",300f,Float.class));
		
	}
	
	public void setWalkSpeed(float walk_speed) {
		this.walk_speed = walk_speed;
	}
	
	public void setJumpSpeed(float min,float max) {
		min_jump_speed = min;
		jump_speed = max;
	}
	
	public void setGravity(float gravity) {
		this.gravity = gravity;
	}
	

	@Override
	public void update(GameObject object, float delta) {
		
		switch(state) {
		case STAND:
			if(object.getY() > 0) {
				state = PlatformState.FALL;
			}
			break;
		case JUMP: 
			if(jumping) {
				object.setVelocity(object.getVelocity().x, object.getVelocity().y+gravity);
			}else {
				object.setVelocity(object.getVelocity().x, Math.min(object.getVelocity().y+gravity,min_jump_speed));
			}
			
			if(object.getVelocity().y < 0) 
				state =PlatformState.FALL;
			break;
		case FALL: 
			object.setVelocity(object.getVelocity().x, object.getVelocity().y+gravity);
			if(object.getY() < 0) {
				on_ground = true;
				object.setPosition(object.getX(), 0);
				state = PlatformState.STAND;
				stopY();
				
			}
			break;
		default:
			break;
		}
		jumping = false;
		walking = false;
	}
	
	public boolean facingLeft() {
		return direction_left ;
	}
	
	public boolean walking() {
		return walking;
	}
	
	public boolean onGround() {
		return on_ground;
	}
	
	public PlatformState getState() {
		return state;
	}

	@Override
	public void remove(GameObject object) {
		
	}
	
	public void jump() {
		jumping = true;
		if(on_ground) {
			state = PlatformState.JUMP;
			on_ground = false;
			daddy.setVelocity(daddy.getVelocity().x, jump_speed);
		}
	}
	
	public void moveLeft() {
		direction_left = true;
		daddy.setVelocity(-walk_speed, daddy.getVelocity().y);
		walking = true;
	}
	
	public void moveRight() {
		direction_left = false;
		walking = true;
		daddy.setVelocity(walk_speed, daddy.getVelocity().y);
	}
	
	public void stop() {
		stopX();
		stopY();
	}
	
	public void stopX() {
		daddy.setVelocity(0, daddy.getVelocity().y);
	}
	
	public void stopY() {
		daddy.setVelocity(daddy.getVelocity().x, 0);
	}
	
	public static enum PlatformState {
		WALK,
		JUMP,
		STAND,
		FALL
	}
	

}
