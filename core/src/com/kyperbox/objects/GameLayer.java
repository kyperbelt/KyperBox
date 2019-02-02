package com.kyperbox.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kyperbox.GameState;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.systems.LayerSystem;
import com.kyperbox.umisc.KyperSprite;

public class GameLayer extends Group {

	private Array<LayerSystem> systems = new Array<LayerSystem>();
	private GameState state;
	private LayerCamera cam;
	private MapProperties layer_properties;
	private float time_scale;
	private ShaderProgram shader;
	private Matrix4 _camDebugTransform;

	public GameLayer(GameState state) {
		this.state = state;
		cam = new LayerCamera(this);
		cam.setPosition(0, 0);
		time_scale = 1f;
		_camDebugTransform = new Matrix4();
	}

	public void setLayerShader(ShaderProgram shader) {
		this.shader = shader;
		if (shader != null && !shader.isCompiled()) {
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
	 * 
	 * @param name
	 * @return
	 */
	public Sprite getGameSprite(String name) {
		return state.getGameSprite(name);
	}

	/**
	 * get a sprite from the given atlas
	 * 
	 * @param name
	 * @param atlas
	 * @return
	 */
	public Sprite getGameSprite(String name, String atlas) {
		return state.getGameSprite(name, atlas);
	}

	/**
	 * get an animation from the animation bank
	 * 
	 * @param animation_name
	 * @return
	 */
	public Animation<KyperSprite> getAnimation(String animation_name) {
		return state.getAnimation(animation_name);
	}

	public Actor getActor(String name) {
		for (Actor c : getChildren()) {
			if (c.getName() != null && c.getName().equals(name))
				return c;
		}
		for (Actor c : getChildren()) {
			if (c instanceof Group) {
				Group g = (Group) c;
				for (Actor b : g.getChildren()) {
					if (b.getName() != null && b.getName().equals(name))
						return b;
				}
			}
		}
		return null;
	}

	/**
	 * get a game object by name
	 * 
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
		for (int i = 0; i < systems.size; i++) {
			LayerSystem system = systems.get(i);
			if (system.isActive())
				system.update(delta * time_scale);
		}
		super.act(delta * time_scale);
		;

	}

	@Override
	protected Matrix4 computeTransform() {
		Matrix4 view = cam.view();
		return view;
	}

	@Override
	protected void drawDebugChildren(ShapeRenderer shapes) {
		_camDebugTransform.set(cam.projection());
		Vector2 halfscreen = cam.getHalfScreen();
		_camDebugTransform.translate(-halfscreen.x*cam.getZoom(), -halfscreen.y*cam.getZoom(), 0);
		_camDebugTransform.scl(cam.getZoom());
			shapes.setProjectionMatrix(_camDebugTransform);
		super.drawDebugChildren(shapes);
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		cam.update();

		ShaderProgram current_shader = batch.getShader();

		if (shader != null && shader != current_shader) {
			batch.setShader(shader);
		}

		for (int i = 0; i < systems.size; i++) {
			LayerSystem system = systems.get(i);
			if (system.isActive())
				system.preDraw(batch, getColor().a * parentAlpha);
		}
		super.draw(batch, parentAlpha);

		for (int i = 0; i < systems.size; i++) {
			LayerSystem system = systems.get(i);
			if (system.isActive())
				system.postDraw(batch, getColor().a * parentAlpha);
		}

		if (current_shader != batch.getShader()) {
			batch.setShader(current_shader);
		}

	}

	/**
	 * called when a game object has changed
	 * 
	 * @param object
	 *            -the object that changed
	 * @param type
	 *            -the type of change
	 * @param value
	 *            -the value of the change
	 */
	public void gameObjectChanged(GameObject object, int type, float value) {
		for (LayerSystem system : systems) {
			system.gameObjectChanged(object, type, value);
		}
	}

	/**
	 * used to add GameObjects to the layer and notify managers
	 * 
	 * @param object
	 * @param properties
	 */
	public void addGameObject(GameObject object, MapProperties properties) {
		object.setGameLayer(this);
		addActor(object);
		gameObjectAdded(object, null);
		object.init(properties);

	}

	/**
	 * used to notify managers that an object was added to the layer or one of its
	 * children
	 * 
	 * @param object
	 * @param parent
	 */
	public void gameObjectAdded(GameObject object, GameObject parent) {
		for (int i = 0; i < systems.size; i++) {
			LayerSystem system = systems.get(i);
			if (system.isActive())
				system.gameObjectAdded(object, parent);
		}
	}

	@Override
	public void addActor(Actor actor) {
		super.addActor(actor);
	}

	@Override
	public boolean removeActor(Actor actor) {
		boolean r = super.removeActor(actor);
		return r;
	}

	public void GameObjectRemoved(GameObject object, GameObject parent) {
		for (int i = 0; i < systems.size; i++) {
			LayerSystem system = systems.get(i);
			if (system.isActive())
				system.gameObjectRemoved(object, parent);
		}
	}

	@Override
	public void drawDebug(ShapeRenderer shapes) {

		if (getDebug()) {
			shapes.setColor(Color.RED);
			Rectangle cfb = cam.getCamFollowBounds();
			shapes.rect(cfb.x, cfb.y, cfb.width, cfb.height);
		}

		super.drawDebug(shapes);
	}

	/**
	 * get all the layer managers
	 * 
	 * @return
	 */
	public Array<LayerSystem> getSystems() {
		return systems;
	}

	/**
	 * add a layer manager
	 */
	public void addLayerSystem(LayerSystem system) {
		for (LayerSystem m : systems)
			if (m.getClass().getName().equals(system.getClass().getName())) {
				getState().error("manager [" + m.getClass().getName() + "] already exists in layer " + getName() + ".");
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
	public <t extends LayerSystem> t getSystem(Class<t> type) {

		for (LayerSystem system : systems) {
			if (system.getClass().getName().equals(type.getName())
					|| system.getClass().getSuperclass().getName().equals(type.getName())) {
				// System.out.println("sysclass_name="+system.getClass().getSuperclass().getName());
				// System.out.println("type_passed_name="+type.getName());
				return (t) system;
			}
		}
		return null;
	}

	public static class LayerCamera {

		public static final int UP = 0;
		public static final int RIGHT = 1;
		public static final int DOWN = 2;
		public static final int LEFT = 3;

		private GameLayer layer;
		private Viewport view;
		private OrthographicCamera cam;
		private Rectangle viewBounds;
		private Rectangle followBounds;
		private Vector2 offset;
		private Vector2 position;
		private Matrix4 _viewScaled;
		private Vector3 _util;
		
		private Vector2 halfscreen;
		private LayerCamera(GameLayer layer) {
			this.layer = layer;
			view = layer.getState().getGame().getView();
			cam = new OrthographicCamera(view.getWorldWidth(), view.getWorldHeight());
			viewBounds = new Rectangle();
			position = new Vector2();
			followBounds = new Rectangle(5, 5, 5, 5);
			offset = new Vector2(0, 0);
			_util = new Vector3();
			_viewScaled = new Matrix4();
			
			halfscreen = new Vector2();
			
			//setCentered();
		}
		
		public Vector2 getHalfScreen() {
			halfscreen.x = cam.viewportWidth * .5f;
			halfscreen.y = cam.viewportHeight * .5f;
			return halfscreen;
		}

		public Rectangle getViewBounds() {
			viewBounds.set((cam.position.x) / cam.zoom, (cam.position.y) / cam.zoom, cam.viewportWidth / cam.zoom,
					cam.viewportHeight / cam.zoom);

			return viewBounds;
		}

		public Matrix4 combined() {
			return cam.combined;
		}
		
		public Matrix4 projection() {
			return cam.projection;
		}

		public Matrix4 view() {
			return _viewScaled.set(cam.view).scl(cam.zoom);
		}

		public void setCamFollowBounds(float x, float y, float width, float height) {

			followBounds.set(x, y, width, height);
		}

		public Rectangle getCamFollowBounds() {
			return followBounds;
		}

		public Vector2 project(Vector2 coords) {
			_util.set(coords.x, coords.y, cam.position.z);
			cam.project(_util);
			coords.set(_util.x + offset.x, _util.y + offset.y);
			return coords;
		}

		public Vector2 unproject(Vector2 coords) {
			_util.set(coords.x, coords.y, cam.position.z);
			cam.unproject(_util);
			coords.set(_util.x + offset.x, _util.y + offset.y);
			return coords;
		}

		public void setPosition(float x, float y) {
			cam.position.set(x* cam.zoom-offset.x  , y* cam.zoom -offset.y , cam.position.z);
		}

		public void setCentered() {
			offset.x = cam.viewportWidth * .5f;
			offset.y = cam.viewportHeight * .5f;
		}

		public Vector2 getPosition() {
			position.set(cam.position.x, cam.position.y );
			return position;
		}

		public float getXOffset() {
			return offset.x;
		}

		public float getYOffset() {
			return offset.y;
		}

		public void setOffset(float x_offset, float y_offset) {
			offset.set(x_offset, y_offset);
		}

		public void setZoom(float zoom) {
			cam.zoom = zoom;
		}

		public float getZoom() {
			return cam.zoom;
		}

		public void translate(float x, float y) {
			setPosition(getPosition().x + x, getPosition().y + y);
		}

		public void update() {

			// Vector2 pos = getPosition();
			// //process screenshake
			// if(isShaking()) {
			// shake_elapsed+=delta;
			//
			// shake_current_stregth = shake_max_strength * ((shake_duration -
			// shake_elapsed)/shake_duration);
			//
			// if(shake_elapsed >= shake_duration) {
			// shake_duration = 0;
			// shake_max_strength = 0;
			// shake_current_stregth = 0;
			// setPosition(goto_pos.x, goto_pos.y);
			// }else {
			// setPosition(goto_pos.x+(MathUtils.random.nextFloat()-.5f)*2*shake_current_stregth,
			// goto_pos.y+(MathUtils.random.nextFloat() -.5f)*2*shake_current_stregth);
			// }
			// }else if(moving_to) { //process moveTo
			// moving_elapsed+=delta;
			// if(moving_elapsed>=moving_duration) {
			// moving_to = false;
			// moving_elapsed = 0;
			// moving_duration = 0;
			// }
			// }

			// else if(follow!=null) {
			//
			// goto_pos.set(MathUtils.floor(follow.getX()+follow.getWidth()*.5f),MathUtils.floor(follow.getY()+follow.getHeight()*.5f));
			// goto_pos = project(goto_pos);
			// float hw = follow.getWidth()*.5f;
			// float hh = follow.getHeight()*.5f;
			// if(!getCamFollowBounds().contains(goto_pos)) {
			// if(goto_pos.x < cam_follow_bounds.x) {
			// setPosition((follow.getX()+hw-(follow_bounds_pos.x))*getZoom(),
			// getPosition().y);
			// layer.gameObjectChanged(follow, GameObjectChangeType.FOLLOW_BOUNDS_REACHED,
			// LEFT);
			// }
			//
			// if(goto_pos.x > cam_follow_bounds.x+cam_follow_bounds.width) {
			// setPosition((follow.getX()+hw-(follow_bounds_pos.x+follow_bounds_size.x))*getZoom(),
			// getPosition().y);
			// layer.gameObjectChanged(follow, GameObjectChangeType.FOLLOW_BOUNDS_REACHED,
			// RIGHT);
			// }
			//
			// if(goto_pos.y < cam_follow_bounds.y) {
			// setPosition(getPosition().x,(follow.getY()+hh-(follow_bounds_pos.y))*getZoom());
			// layer.gameObjectChanged(follow, GameObjectChangeType.FOLLOW_BOUNDS_REACHED,
			// DOWN);
			// }
			//
			// if(goto_pos.y > cam_follow_bounds.y+cam_follow_bounds.height) {
			// setPosition(getPosition().x,(follow.getY()+hh-(follow_bounds_pos.y+follow_bounds_size.y))*getZoom());
			// layer.gameObjectChanged(follow, GameObjectChangeType.FOLLOW_BOUNDS_REACHED,
			// UP);
			// }
			//
			// }
			// }
			// xscale
			cam.update();

		}

		public float getYS() {
			throw new UnsupportedOperationException();
		}

		public float getXS() {
			throw new UnsupportedOperationException();
		}

	}

	@Override
	protected void drawDebugBounds(ShapeRenderer shapes) {
		super.drawDebugBounds(shapes);
		if (getDebug())
			for (LayerSystem system : systems) {
				system.drawDebug(shapes);
			}
	}

	// EXPERIMENTS;

	// public static class LayerCamera{
	//
	// private Viewport view;
	// private OrthographicCamera cam;
	// private GameLayer layer;
	//
	// public LayerCamera(GameLayer layer) {
	// this.layer = layer;
	// view = new StretchViewport(worldWidth, worldHeight)
	// }
	//
	// public void setPosition(float x,float y) {
	//
	// }
	//
	// public Matrix4 getProjectionMatrix() {
	// return cam.getP
	// }
	//
	// public void update() {
	//
	// }
	//
	// }

}
