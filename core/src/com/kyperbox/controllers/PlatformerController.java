package com.kyperbox.controllers;

import com.badlogic.gdx.utils.Array;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.controllers.CollisionController.CollisionData;
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
	private CollisionController collision_control;
	
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
		on_ground = false;
		state = PlatformState.STAND;
		setPriority(Priority.HIGH);
		setJumpSpeed(object.getProperties().get("min_jump_speed", 200f,Float.class),object.getProperties().get("max_jump_speed", 600f,Float.class));
		setGravity(object.getProperties().get("gravity", -12f,Float.class));
		setWalkSpeed(object.getProperties().get("walk_speed",300f,Float.class));
		collision_control = object.getController(CollisionController.class);
		if(collision_control!=null)
			collision_control.getCollisions().clear();
		
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
		on_ground = false;
		if(collision_control==null) {
			collision_control = object.getController(CollisionController.class);
		}
		
		switch(state) {
		case STAND:
			GameObject collision = collision_control.collisionWithOffset(object,0,-1);
			
			if((collision == null)&& object.getCollisionBounds().y > 0) {
					state = PlatformState.FALL;
			}else if(collision!=null){
				PlatformerController cp = collision.getController(PlatformerController.class);
				if(cp!=null && cp.on_ground)
					on_ground = true;
				else
					state = PlatformState.FALL;
			}else {
				on_ground = true;
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
			if(collision_control!=null) {
				
				Array<CollisionData> col_data = collision_control.getCollisions();
				for (int i = 0; i < col_data.size; i++) {
					CollisionData cd = col_data.get(i);
					PlatformerController tpc = cd.getTarget().getController(PlatformerController.class);
					if(tpc == null)
						continue;
					float cd_y = cd.getOverlapBox().y;
					float cd_h = cd.getOverlapBox().height;
					if(tpc.on_ground && cd_y+cd_h > object.getCollisionBounds().y) {
						object.setPosition(object.getX(), cd_y+cd_h);
						stopY();
						on_ground = true;
						state = PlatformState.STAND;
					}
				}
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
