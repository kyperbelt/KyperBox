package com.kyperbox.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kyperbox.GameState;
import com.kyperbox.objects.GameObject.GameObjectChangeType;
import com.kyperbox.systems.LayerSystem;

public class GameLayer extends Group{
	
	private Array<LayerSystem> systems;
	private GameState state;
	private LayerCamera cam;
	private MapProperties layer_properties;
	
	public GameLayer(GameState state) {
		systems = new Array<LayerSystem>();
		this.state = state;
		cam = new LayerCamera(this);
		cam.setPosition(0, 0);
	}
	
	public void setLayerProperties(MapProperties properties) {
		this.layer_properties = properties;
	}
	
	/**
	 * get the state this layer belongs to
	 */
	public GameState getState() {
		return state;
	}
	
	/**
	 * get a sprite from the sprite bank
	 * @param name
	 * @return
	 */
	public Sprite getGameSprite(String name) {
		return state.getGame().getGameSprite(name);
	}
	
	/**
	 * get a sprite from the given atlas
	 * @param name
	 * @param atlas
	 * @return
	 */
	public Sprite getGameSprite(String name,String atlas) {
		return state.getGame().getGameSprite(name, atlas);
	}
	
	/**
	 * get an animation from the animation bank
	 * @param animation_name
	 * @return
	 */
	public Animation<String> getAnimation(String animation_name){
		return state.getGame().getAnimation(animation_name);
	}
	
	public Actor getActor(String name) {
		for(Actor c: getChildren()) {
			if(c.getName().equals(name))
				return c;
		}
		return null;
	}
	
	/**
	 * get a game object by name
	 * @param name
	 * @return
	 */
	public GameObject getGameObject(String name) {
		return (GameObject) getActor(name);
	}
	
	
	@Override
	public void act(float delta) {
		for(LayerSystem system:systems) {
			if(system.isActive())
				system.update(delta);
		}
		cam.update(delta);
		super.act(delta);
	}
	
	/**
	 * called when a game object has changed
	 * @param object -the object that changed
	 * @param type -the type of change
	 * @param value -the value of the change
	 */
	public void gameObjectChanged(GameObject object,GameObjectChangeType type,int value) {
		for(LayerSystem system : systems) {
			system.gameObjectChanged(object, type, value);
		}
	}
	
	/**
	 * used to add GameObjects to the layer and notify managers
	 * @param object
	 * @param properties
	 */
	public void addGameObject(GameObject object,MapProperties properties) {
		object.setGameLayer(this);
		addActor(object);
		object.init(properties);
		gameObjectAdded(object, null);
	}
	
	/**
	 * used to notify managers that an object 
	 * was added to the layer or one of its children
	 * @param object
	 * @param parent
	 */
	public void gameObjectAdded(GameObject object,GameObject parent) {
		for(LayerSystem system:systems) {
			if(system.isActive())
				system.gameObjectAdded(object,parent);
		}
	}
	
	@Override
	public void addActor(Actor actor) {
		super.addActor(actor);
	}
	
	
	@Override
	public boolean removeActor(Actor actor) {
		boolean r =  super.removeActor(actor);
		GameObjectRemoved((GameObject) actor, null);
		return r;
	}
	
	public void GameObjectRemoved(GameObject object,GameObject parent) {
		for(LayerSystem system:systems) {
			if(system.isActive())
				system.gameObjectRemoved(object,parent);
		}
	}
	
	@Override
	public void drawDebug(ShapeRenderer shapes) {
		
		if(getDebug()) {
			shapes.setColor(Color.RED);
			Rectangle cfb = cam.getCamFollowBounds();
			shapes.rect(cfb.x, cfb.y, cfb.width, cfb.height);
		}
		
		super.drawDebug(shapes);
	}
	
	/**
	 * get all the layer managers
	 * @return
	 */
	public Array<LayerSystem> getSystems(){return systems;}
	
	/**
	 * add a layer manager
	 */
	public void addLayerSystem(LayerSystem system) {
		for(LayerSystem m: systems)
			if(m.getClass().isInstance(system)) {
				getState().error("manager ["+m.getClass().getName()+"] already exists in layer "+getName()+".");
			}
		system.setLayer(this);
		systems.add(system);
		systems.sort(getState().getGame().getPriorityComperator());
		system.init(getLayerProperties());
	}
	
	public MapProperties getLayerProperties() {
		return layer_properties;
	}
	
	
	@Override
	public boolean remove() {
		while (systems.size > 0) {
			systems.pop().onRemove();
		}
		return super.remove();
	}
	

	public LayerCamera getCamera() {
		return cam;
	}
	
	public static class LayerCamera{
		
		public static final int UP = 0;
		public static final int RIGHT = 1;
		public static final int DOWN = 2;
		public static final int LEFT = 3;
		
		private GameLayer layer;
		
		private Vector2 position;
		private Vector2 return_pos;
		private Vector2 virtual_size;
		private Vector2 offset;
		private Vector2 follow_bounds_pos;
		private Vector2 follow_bounds_size;
		private Rectangle cam_follow_bounds;
		
		//action vars
		private Vector2 goto_pos;
		private float shake_current_stregth;
		private float shake_max_strength;
		private float shake_duration;
		private float shake_elapsed;
		
		private boolean moving_to;
		private float moving_duration;
		private float moving_elapsed;
		private Interpolation interpolation;
		
		private GameObject follow;
		
		private LayerCamera(GameLayer layer) {
			this.layer = layer;
			position = new Vector2();
			return_pos = new Vector2();
			goto_pos = new Vector2();
			offset = new Vector2();
			
			
			Viewport view = layer.getState().getGame().getView();
			virtual_size = new Vector2(view.getWorldWidth(),view.getWorldHeight());
			interpolation = Interpolation.linear;
			shake_current_stregth = 0;
			shake_max_strength = 0;
			shake_duration = 0;
			shake_elapsed = 0;
			
			follow_bounds_size = new Vector2(32, 32);
			follow_bounds_pos  = new Vector2(follow_bounds_size.cpy().scl(-.5f));
			cam_follow_bounds = new Rectangle(follow_bounds_pos.x, follow_bounds_pos.y, follow_bounds_size.x, follow_bounds_size.y);
		}
		
		public void setCamFollowBounds(float x,float y,float width,float height) {
			this.follow_bounds_pos.set(x, y);
			this.follow_bounds_size.set(width,height);
		}
		
		
		public Rectangle getCamFollowBounds() {
			cam_follow_bounds.set(follow_bounds_pos.x-offset.x, follow_bounds_pos.y-offset.y, follow_bounds_size.x, follow_bounds_size.y);
			return cam_follow_bounds;
		}
		
		
		public Vector2 project(Vector2 coords) {
			coords.scl(getZoom());
			coords.set(coords.x + position.x, coords.y + position.y);
			return coords;
		}
		
		public Vector2 unproject(Vector2 coords) {
			Viewport view = layer.getState().getGame().getView();
			view.unproject(coords);
			coords.set(-position.x +coords.x, position.y + coords.y).scl(1f/getZoom());
			return coords;
		}
		
		public void setInterpolation(Interpolation interpolation) {
			this.interpolation = interpolation;
		}
		
		public void setPosition(float x,float y) {
			position.set(-(x+offset.x), -(y+offset.y));
		}
		
		public void setCentered() {
			offset.set(-virtual_size.x*.5f,-virtual_size.y*.5f);
			setPosition(position.x, position.y);
		}
		
		public Vector2 getPosition() {
			return_pos.set(-(position.x)-offset.x, -(position.y)-offset.y);
			return return_pos;
		}
		
		public float getXOffset() {
			return offset.x;
		}
		
		public float getYOffset() {
			return offset.y;
		}
		
		public void setOffset(float x_offset,float y_offset) {
			offset.set(x_offset, y_offset);
		}
		
		public void shake(float strength,float duration) {
			if(!isShaking())
				goto_pos.set(getPosition().x,getPosition().y);
			this.shake_max_strength = strength;
			this.shake_duration = duration;
			this.shake_elapsed = 0;
		
		}
		
		public boolean isShaking() {
			return shake_duration > 0;
		}
		
		public void moveTo(float x,float y) {
			moveTo(x,y,1f);
		}
		
		public void moveTo(float x,float y,float duration) {
			if(moving_to)
				return;
			goto_pos.set(-(x+offset.x), -(y+offset.y));
			moving_to = true;
			moving_duration = duration;
			moving_elapsed = 0;
		}
		
		public boolean isMoving() {
			return moving_to;
		}
		
		public void setZoom(float zoom) {
			layer.setScale(zoom);
		}
		
		public float getZoom() {
			return layer.getScaleX();
		}
		
		/**
		 * set the object to follow
		 * @param follow
		 * @param follow_speed
		 */
		public void follow(GameObject follow) {
			this.follow = follow;
		}
		
		
		public void update(float delta) {
			
			//process screenshake
			if(isShaking()) {
				shake_elapsed+=delta;
				
				shake_current_stregth = shake_max_strength * ((shake_duration - shake_elapsed)/shake_duration);
				
				if(shake_elapsed >= shake_duration) {
					shake_duration = 0;
					shake_max_strength = 0;
					shake_current_stregth = 0;
					setPosition(goto_pos.x, goto_pos.y);
				}else {
					setPosition(goto_pos.x+(MathUtils.random.nextFloat()-.5f)*2*shake_current_stregth, 
							goto_pos.y+(MathUtils.random.nextFloat() -.5f)*2*shake_current_stregth);
				}
			}else if(moving_to) { //process moveTo
				moving_elapsed+=delta;
				position.interpolate(goto_pos, moving_elapsed/moving_duration, interpolation);
				if(moving_elapsed>=moving_duration) {
					moving_to = false;
					moving_elapsed = 0;
					moving_duration = 0;
				}
			}else if(follow!=null) {
				
				goto_pos.set(follow.getX()+follow.getWidth()*.5f,follow.getY()+follow.getHeight()*.5f);
				goto_pos = project(goto_pos);
				float hw = follow.getWidth()*.5f;
				float hh = follow.getHeight()*.5f;
				if(!getCamFollowBounds().contains(goto_pos)) {
					if(goto_pos.x < cam_follow_bounds.x) {
						setPosition((follow.getX()+hw-(follow_bounds_pos.x))*getZoom(), 
								getPosition().y);
						layer.gameObjectChanged(follow, GameObjectChangeType.FOLLOW_BOUNDS_REACHED, LEFT);
					}
						
					if(goto_pos.x > cam_follow_bounds.x+cam_follow_bounds.width) {
						setPosition((follow.getX()+hw-(follow_bounds_pos.x+follow_bounds_size.x))*getZoom(), 
								getPosition().y);
						layer.gameObjectChanged(follow, GameObjectChangeType.FOLLOW_BOUNDS_REACHED, RIGHT);
					}
						
					if(goto_pos.y < cam_follow_bounds.y) {
						setPosition(getPosition().x,(follow.getY()+hh-(follow_bounds_pos.y))*getZoom());
						layer.gameObjectChanged(follow, GameObjectChangeType.FOLLOW_BOUNDS_REACHED, DOWN);
					}
						
					if(goto_pos.y > cam_follow_bounds.y+cam_follow_bounds.height) {
						setPosition(getPosition().x,(follow.getY()+hh-(follow_bounds_pos.y+follow_bounds_size.y))*getZoom());
						layer.gameObjectChanged(follow, GameObjectChangeType.FOLLOW_BOUNDS_REACHED, UP);
					}
					
				}
			}
			
			
			//set the layer position
			layer.setPosition(position.x, position.y);
		
		}
	}
	
	@Override
	protected void drawDebugBounds(ShapeRenderer shapes) {
		super.drawDebugBounds(shapes);
		if(getDebug())
			for(LayerSystem system:systems) {
				system.drawDebug(shapes);
			}
	}
	
}
