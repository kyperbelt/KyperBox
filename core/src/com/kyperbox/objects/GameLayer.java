package com.kyperbox.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kyperbox.GameState;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.systems.LayerSystem;
import com.kyperbox.umisc.KyperSprite;

public class GameLayer extends Group{
	
	private Array<LayerSystem> systems;
	private GameState state;
	private LayerCamera cam;
	private MapProperties layer_properties;
	private float time_scale;
	private ShaderProgram shader;
	
	public GameLayer(GameState state) {
		systems = new Array<LayerSystem>();
		this.state = state;
		cam = new LayerCamera(this);
		cam.setPosition(0, 0);
		time_scale = 1f;
	}
	
	public void setLayerShader(ShaderProgram shader) {
		this.shader = shader;
		if(!shader.isCompiled()) {
			getState().error(shader.getLog());
		}
	}
	
	public ShaderProgram getLayerShader() {
		return shader;
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
		return state.getGameSprite(name);
	}
	
	/**
	 * get a sprite from the given atlas
	 * @param name
	 * @param atlas
	 * @return
	 */
	public Sprite getGameSprite(String name,String atlas) {
		return state.getGameSprite(name, atlas);
	}
	
	/**
	 * get an animation from the animation bank
	 * @param animation_name
	 * @return
	 */
	public Animation<KyperSprite> getAnimation(String animation_name){
		return state.getAnimation(animation_name);
	}
	
	public Actor getActor(String name) {
		for(Actor c: getChildren()) {
			if(c.getName().equals(name))
				return c;
		}
		for(Actor c: getChildren()) {
			if(c instanceof Group) {
				Group g = (Group) c;
				for(Actor b:g.getChildren()) {
					if(b.getName().equals(name))
						return b;
				}
			}
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
	
	public float getTimeScale() {
		return time_scale;
	}
	
	public void setTimeScale(float time_scale) {
		this.time_scale = Math.max(0, time_scale);
	}
	
	@Override
	public void act(float delta) {
		systems.sort(KyperBoxGame.getPriorityComperator());
		for(int i = 0;i < systems.size;i++) {
			LayerSystem system = systems.get(i);
			if(system.isActive())
				system.update(delta*time_scale);
		}
		super.act(delta*time_scale);;
		
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		
		ShaderProgram current_shader = batch.getShader();
		
		

		if(shader!=null && shader!= current_shader) {
			batch.setShader(shader);
		}
		
		for(int i = 0;i < systems.size;i++) {
			LayerSystem system = systems.get(i);
			if(system.isActive())
				system.preDraw(batch, getColor().a * parentAlpha);
		}
		super.draw(batch, parentAlpha);
		
		
		for(int i = 0;i < systems.size;i++) {
			LayerSystem system = systems.get(i);
			if(system.isActive())
				system.postDraw(batch, getColor().a * parentAlpha);
		}
		
		if(current_shader!=batch.getShader()) {
			batch.setShader(current_shader);
		}
		
		cam.update();
	}
	
	/**
	 * called when a game object has changed
	 * @param object -the object that changed
	 * @param type -the type of change
	 * @param value -the value of the change
	 */
	public void gameObjectChanged(GameObject object,int type,float value) {
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
		gameObjectAdded(object, null);
		object.init(properties);
		
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
			if(m.getClass().getName().equals(system.getClass().getName())) {
				getState().error("manager ["+m.getClass().getName()+"] already exists in layer "+getName()+".");
			}
		system.setLayer(this);
		systems.add(system);
		systems.sort(KyperBoxGame.getPriorityComperator());
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
	
	@SuppressWarnings("unchecked")
	public <t extends LayerSystem>t getSystem(Class<t> type) {
		
		for(LayerSystem system:systems) {
			if(system.getClass().getName().equals(type.getName())||system.getClass().getSuperclass().getName().equals(type.getName())) {
//				System.out.println("sysclass_name="+system.getClass().getSuperclass().getName());
//				System.out.println("type_passed_name="+type.getName());
				return (t) system;
			}
		}
		return null;
	}

	public static class LayerCamera{
		
		public static final int UP = 0;
		public static final int RIGHT = 1;
		public static final int DOWN = 2;
		public static final int LEFT = 3;
		
		private GameLayer layer;
		private Viewport view;
		
		private Vector2 position;
		private Vector2 return_pos;
		private Vector2 virtual_size;
		private Vector2 offset;
		private Vector2 follow_bounds_pos;
		private Vector2 follow_bounds_size;
		private Rectangle cam_follow_bounds;
		private Rectangle cam_bounds;
		
		
		private LayerCamera(GameLayer layer) {
			this.layer = layer;
			position = new Vector2();
			return_pos = new Vector2();
			offset = new Vector2();
			cam_bounds = new Rectangle();
			view = layer.getState().getGame().getView();
			virtual_size = new Vector2(view.getWorldWidth(),view.getWorldHeight());
			follow_bounds_size = new Vector2(32, 32);
			follow_bounds_pos  = new Vector2(follow_bounds_size.cpy().scl(-.5f));
			cam_follow_bounds = new Rectangle(follow_bounds_pos.x, follow_bounds_pos.y, follow_bounds_size.x, follow_bounds_size.y);
		}
		
		public Rectangle getViewBounds() {
			cam_bounds.set((getPosition().x+getXOffset()/getZoom()), (getPosition().y+getYOffset()/getZoom()), view.getWorldWidth()/getZoom(), view.getWorldHeight()/getZoom());
			return cam_bounds;
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
			Viewport view = layer.getState().getGame().getView();
			coords.set(-position.x +coords.x, coords.y - position.y).scl(getZoom());
			view.project(coords);
			
			return coords;
		}
		
		public Vector2 unproject(Vector2 coords) {
			Viewport view = layer.getState().getGame().getView();
			view.unproject(coords);
			coords.set(-position.x +coords.x, coords.y - position.y).scl(1f/getZoom());
			return coords;
		}
		
		public void setPosition(float x,float y) {
			position.set(-(x*getZoom()+offset.x), -(y*getZoom()+offset.y));
			//layer.setPosition(position.x, position.y);
		}
		
		public void setCentered() {
			offset.set(-virtual_size.x*.5f,-virtual_size.y*.5f);
			setPosition(position.x, position.y);
		}
		
		public Vector2 getPosition() {
			return_pos.set((-(position.x)-offset.x)/getZoom(), (-(position.y)-offset.y)/getZoom());
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
		
		
		public void setZoom(float zoom) {
			return_pos = getPosition();
			layer.setScale(zoom);
			setPosition(return_pos.x, return_pos.y);
		}
		
		public float getZoom() {
			return (layer.getScaleX()+layer.getScaleY())*.5f;
		}
		
		public void translate(float x,float y) {
			Vector2 p = getPosition();
			setPosition(p.x+x, p.y+y);
		}
		
		public void update() {
			
//		    Vector2 pos = getPosition();
//			//process screenshake
//			if(isShaking()) {
//				shake_elapsed+=delta;
//				
//				shake_current_stregth = shake_max_strength * ((shake_duration - shake_elapsed)/shake_duration);
//				
//				if(shake_elapsed >= shake_duration) {
//					shake_duration = 0;
//					shake_max_strength = 0;
//					shake_current_stregth = 0;
//					setPosition(goto_pos.x, goto_pos.y);
//				}else {
//					setPosition(goto_pos.x+(MathUtils.random.nextFloat()-.5f)*2*shake_current_stregth, 
//							goto_pos.y+(MathUtils.random.nextFloat() -.5f)*2*shake_current_stregth);
//				}
//			}else if(moving_to) { //process moveTo
//				moving_elapsed+=delta;
//				if(moving_elapsed>=moving_duration) {
//					moving_to = false;
//					moving_elapsed = 0;
//					moving_duration = 0;
//				}
//			}
			
//			else if(follow!=null) {
//				
//				goto_pos.set(MathUtils.floor(follow.getX()+follow.getWidth()*.5f),MathUtils.floor(follow.getY()+follow.getHeight()*.5f));
//				goto_pos = project(goto_pos);
//				float hw = follow.getWidth()*.5f;
//				float hh = follow.getHeight()*.5f;
//				if(!getCamFollowBounds().contains(goto_pos)) {
//					if(goto_pos.x < cam_follow_bounds.x) {
//						setPosition((follow.getX()+hw-(follow_bounds_pos.x))*getZoom(), 
//								getPosition().y);
//						layer.gameObjectChanged(follow, GameObjectChangeType.FOLLOW_BOUNDS_REACHED, LEFT);
//					}
//						
//					if(goto_pos.x > cam_follow_bounds.x+cam_follow_bounds.width) {
//						setPosition((follow.getX()+hw-(follow_bounds_pos.x+follow_bounds_size.x))*getZoom(), 
//								getPosition().y);
//						layer.gameObjectChanged(follow, GameObjectChangeType.FOLLOW_BOUNDS_REACHED, RIGHT);
//					}
//						
//					if(goto_pos.y < cam_follow_bounds.y) {
//						setPosition(getPosition().x,(follow.getY()+hh-(follow_bounds_pos.y))*getZoom());
//						layer.gameObjectChanged(follow, GameObjectChangeType.FOLLOW_BOUNDS_REACHED, DOWN);
//					}
//						
//					if(goto_pos.y > cam_follow_bounds.y+cam_follow_bounds.height) {
//						setPosition(getPosition().x,(follow.getY()+hh-(follow_bounds_pos.y+follow_bounds_size.y))*getZoom());
//						layer.gameObjectChanged(follow, GameObjectChangeType.FOLLOW_BOUNDS_REACHED, UP);
//					}
//					
//				}
//			}
			//xscale 
			xs =  view.getWorldWidth() / Gdx.graphics.getWidth();
			//yscale
			ys = view.getWorldHeight() / Gdx.graphics.getHeight();
			
			//xpos rounded
			float xpr = MathUtils.round(position.x / xs) * xs;
			float ypr = MathUtils.round(position.y / ys) * ys;
//			//set the layer position
			layer.setPosition(xpr, ypr);
			layer.setTransform(true);
		
		}
		
		float xs = 1;
		float ys = 1;
		
		public float getYS() {
			return ys;
		}
		
		public float getXS() {
			return xs;
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
	
	
	//EXPERIMENTS;
	
	
//	public static class LayerCamera{
//		
//		private Viewport view;
//		private OrthographicCamera cam;
//		private GameLayer layer;
//		
//		public LayerCamera(GameLayer layer) {
//			this.layer = layer;
//			view = new StretchViewport(worldWidth, worldHeight)
//		}
//		
//		public void setPosition(float x,float y) {
//		
//		}
//		
//		public Matrix4 getProjectionMatrix() {
//			return cam.getP
//		}
//		
//		public void update() {
//			
//		}
//		
//	}
	
}
