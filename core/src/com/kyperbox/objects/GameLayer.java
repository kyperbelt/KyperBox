package com.kyperbox.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Bits;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kyperbox.GameState;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.controllers.ControllerGroup;
import com.kyperbox.objects.GameObject.GameObjectChangeType;
import com.kyperbox.systems.AbstractSystem;
import com.kyperbox.systems.GameObjectListener;
import com.kyperbox.umisc.Event;
import com.kyperbox.umisc.EventListener;
import com.kyperbox.umisc.KyperSprite;

public class GameLayer extends Group {
	
	//TODO: make a test to check controller adding works, entity adding listeners work
	
	@SuppressWarnings("unchecked")
	public static final ControllerGroup ALL = ControllerGroup.all().get();

	private EventListener<GameObject> controllerAddedListener;
	private EventListener<GameObject> controllerRemovedListener;

	public final Event<AbstractSystem> AbstractSystemAdded;
	public final Event<AbstractSystem> AbstractSystemRemoved;

	// for backwards compatibility
	public final Event<GameObject> gameObjectControllerChanged;
	public final Event<GameObject> gameObjectAdded;
	public final Event<GameObject> gameObjectRemoved;

	private Array<GameObject> objects;
	private ObjectMap<ControllerGroup, Array<GameObject>> objectsByGroup;

	private SnapshotArray<ObjectListenerData> objectListeners = new SnapshotArray<ObjectListenerData>(true, 16);
	private ObjectMap<ControllerGroup, Bits> objectListenerMasks = new ObjectMap<ControllerGroup, Bits>();
	private BitsPool bitsPool = new BitsPool();

	private boolean updating = false;
	private boolean notifying = false;

	private ObjectOperationPool objectOperationPool = new ObjectOperationPool();
	private Array<GameObjectOperation> objectOperations = new Array<GameObjectOperation>();
	
	private ControllerOperationPool controllerOperationPool = new ControllerOperationPool();
	private Array<ControllerOperation> controllerOperations = new Array<GameLayer.ControllerOperation>();

	private Array<AbstractSystem> systems;
	private ObjectMap<Class<? extends AbstractSystem>, AbstractSystem> systemsByClass;

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

		objects = new Array<GameObject>();
		objectsByGroup = new ObjectMap<ControllerGroup, Array<GameObject>>();

		systems = new Array<AbstractSystem>();
		systemsByClass = new ObjectMap<Class<? extends AbstractSystem>, AbstractSystem>();

		// deprecated events
		gameObjectAdded = new Event<GameObject>();
		gameObjectRemoved = new Event<GameObject>();
		gameObjectControllerChanged = new Event<GameObject>();
		// ----

		controllerAddedListener = new ControllerListener();
		controllerRemovedListener = new ControllerListener();

		AbstractSystemAdded = new Event<AbstractSystem>();
		AbstractSystemRemoved = new Event<AbstractSystem>();
	}

	public void addGameObjectListener(ControllerGroup group, int priority, GameObjectListener listener) {
		getControllerGroupObjects(group);

		int insertionIndex = 0;
		while (insertionIndex < objectListeners.size) {
			if (objectListeners.get(insertionIndex).priority <= priority) {
				insertionIndex++;
			} else {
				break;
			}
		}

		// Shift up bitmasks by one step
		for (Bits mask : objectListenerMasks.values()) {
			for (int k = mask.length(); k > insertionIndex; k--) {
				if (mask.get(k - 1)) {
					mask.set(k);
				} else {
					mask.clear(k);
				}
			}
			mask.clear(insertionIndex);
		}

		objectListenerMasks.get(group).set(insertionIndex);

		ObjectListenerData objectListenerData = new ObjectListenerData();
		objectListenerData.listener = listener;
		objectListenerData.priority = priority;
		objectListeners.insert(insertionIndex, objectListenerData);

	}

	public void removeGameObjectListener(GameObjectListener listener) {
		for (int i = 0; i < objectListeners.size; i++) {
			ObjectListenerData objectListenerData = objectListeners.get(i);
			if (objectListenerData.listener == listener) {
				// Shift down bitmasks by one step
				for (Bits mask : objectListenerMasks.values()) {
					for (int k = i, n = mask.length(); k < n; k++) {
						if (mask.get(k + 1)) {
							mask.set(k);
						} else {
							mask.clear(k);
						}
					}
				}

				objectListeners.removeIndex(i--);
			}
		}
	}

	protected void addController(GameObject object) {
		ControllerOperation co = controllerOperationPool.obtain();
		co.type = ControllerOperation.ADD;
		co.object = object;

		controllerOperations.add(co);
	}

	protected void removeController(GameObject object) {
		ControllerOperation co = controllerOperationPool.obtain();
		co.type = ControllerOperation.REMOVE;
		co.object = object;

		controllerOperations.add(co);
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

	/**
	 * do not use this on loop as it may cause slow down
	 * 
	 * @param name
	 * @return
	 */
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
	 * get a game object by name do not use this on loop as it may cause slowdown
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

		updating = true;

		try {

			for (int i = 0; i < systems.size; i++) {
				AbstractSystem system = systems.get(i);
				if (system.isActive())
					system.update(delta * time_scale);

				// object removal happens here
				while (objectOperations.size > 0 || controllerOperations.size > 0) {
//					for (int j = 0; j < removingObjects.size; j++) {
//						internalRemoveGameObject(removingObjects.get(j));
//					}
//					objects.removeAll(removingObjects, true);
//					removingObjects.clear();

					// object addition/remove happens here
					for (int j = 0; j < objectOperations.size; j++) {
						GameObjectOperation op = objectOperations.get(j);
						if(op.type == GameObjectOperation.ADD) {
							addInternal(op.object, op.properties, false);
						}else if(op.type == GameObjectOperation.REMOVE){
							internalRemoveGameObject(op.object);
							objects.removeValue(op.object, true);
						}
						
					}
					objectOperationPool.freeAll(objectOperations);
					objectOperations.clear();

					// controller operation notifications happen here
					for (int j = 0; j < controllerOperations.size; j++) {
						ControllerOperation cop = controllerOperations.get(j);
						if(cop.type == ControllerOperation.ADD) {
							cop.object.notifyControllerAdded();
						}else 
						if(cop.type == ControllerOperation.REMOVE) {
							cop.object.notifyControllerRemoved();
						}
					}
					controllerOperationPool.freeAll(controllerOperations);
					controllerOperations.clear();
					
				}
			}

		} finally {
			updating = false;
		}

		super.act(delta * time_scale);

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
		_camDebugTransform.translate(-halfscreen.x * cam.getZoom(), -halfscreen.y * cam.getZoom(), 0);
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
			AbstractSystem system = systems.get(i);
			if (system.isActive())
				system.preDraw(batch, getColor().a * parentAlpha);
		}
		super.draw(batch, parentAlpha);

		for (int i = 0; i < systems.size; i++) {
			AbstractSystem system = systems.get(i);
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
	protected void gameObjectChanged(GameObject object, int type, float value) {
		gameObjectControllerChanged.fire(object);
	}

	/**
	 * used to add GameObjects to the layer and notify managers
	 * 
	 * @param object
	 * @param properties
	 */
	public void addGameObject(GameObject object, MapProperties properties) {
		addInternal(object, properties, (updating || notifying));
	}

	private void addInternal(GameObject object, MapProperties properties, boolean delayed) {

		if (delayed) {
			GameObjectOperation op = objectOperationPool.obtain();
			op.object = object;
			op.properties = properties;
			op.type = GameObjectOperation.ADD;
			objectOperations.add(op);
		} else {
			objects.add(object);
			object.setGameLayer(this);
			addActor(object);
			gameObjectAdded(object);
			object.init(properties);
		}
	}

	/**
	 * used to notify managers that an object was added to the layer or one of its
	 * children
	 * 
	 * @param object
	 * @param parent
	 */
	protected void gameObjectAdded(GameObject object) {
		object.controllerAdded.add(controllerAddedListener);
		object.controllerRemoved.add(controllerRemovedListener);
		refreshControllerGroup(object);
		gameObjectAdded.fire(object);
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

	public void removeGameObject(GameObject object) {
		object.shouldRemove = true;
		if(updating || notifying) {
			GameObjectOperation op = objectOperationPool.obtain();
			op.type = GameObjectOperation.REMOVE;
			op.object = object;
			objectOperations.add(op);
		}else {
			internalRemoveGameObject(object);
		}
	}

	@Override
	public void drawDebug(ShapeRenderer shapes) {

		if (getDebug()) {
			shapes.setColor(Color.RED);
			Rectangle cfb = cam.getCamFollowBounds();
			shapes.rect(cfb.x, cfb.y, cfb.width, cfb.height);

			if (getDebug())
				for (AbstractSystem system : systems) {
					system.drawDebug(shapes);
				}
		}

		super.drawDebug(shapes);
	}

	/**
	 * get all the layer managers
	 * 
	 * @return
	 */
	public Array<AbstractSystem> getSystems() {
		return systems;
	}

	public void addSystem(AbstractSystem system) {
		addSystem(system, 0);
	}

	/**
	 * add a layer manager
	 */
	public void addSystem(AbstractSystem system, int priority) {
		AbstractSystem oldSystem = systemsByClass.get(system.getClass());
		if (oldSystem != null) {
			getState()
					.error("manager [" + system.getClass().getName() + "] already exists in layer " + getName() + ".");
			return;
		}

		systems.add(system);
		systems.sort(KyperBoxGame.getPriorityComperator());
		systemsByClass.put(system.getClass(), system);
		AbstractSystemAdded.fire(system);
		system.internalAddToLayer(this);
		
	}

	public MapProperties getLayerProperties() {
		return layer_properties;
	}

	@Override
	public boolean remove() {
		while (systems.size > 0) {
			AbstractSystem system = systems.pop();
			AbstractSystemRemoved.fire(system);
			systemsByClass.remove(system.getClass());
		}
		return super.remove();
	}

	public LayerCamera getCamera() {
		return cam;
	}

	private void internalRemoveGameObject(GameObject object) {
		if (!object.isRemoved())
			object.remove();
		object.shouldRemove = false;
		object.removing = true;
		object.setGameLayer(null);
		refreshControllerGroup(object);
		gameObjectRemoved.fire(object);

		object.controllerAdded.remove(controllerAddedListener);
		object.controllerRemoved.remove(controllerRemovedListener);
		object.removing = false;

	}

	private void refreshControllerGroup(GameObject object) {

		Bits addListenerBits = bitsPool.obtain();
		Bits removeListenerBits = bitsPool.obtain();

		for (ControllerGroup controllerGroup :objectListenerMasks.keys()) {
			final int groupIndex = controllerGroup.getIndex();
			final Bits objectGroupBits = object.getControllerGroupBits();

			boolean belongsToGroup = objectGroupBits.get(groupIndex);
			boolean matches = controllerGroup.matches(object) && !object.removing;

			if (belongsToGroup != matches) {
				final Bits listenersMask = objectListenerMasks.get(controllerGroup);
				final Array<GameObject> groupObjects = objectsByGroup.get(controllerGroup);
				if (matches) {
					System.out.println("object added");
					addListenerBits.or(listenersMask);
					groupObjects.add(object);
					objectGroupBits.set(groupIndex);
				} else {
					System.out.println("object removed");
					removeListenerBits.or(listenersMask);
					groupObjects.removeValue(object, true);
					objectGroupBits.clear(groupIndex);
				}
			}
		}

		notifying = true;
		Object[] items = objectListeners.begin();

		try {
			for (int i = removeListenerBits.nextSetBit(0); i >= 0; i = removeListenerBits.nextSetBit(i + 1)) {
				((ObjectListenerData) items[i]).listener.objectRemoved(object);
			}

			for (int i = addListenerBits.nextSetBit(0); i >= 0; i = addListenerBits.nextSetBit(i + 1)) {
				((ObjectListenerData) items[i]).listener.objectAdded(object);
			}
		} finally {
			addListenerBits.clear();
			removeListenerBits.clear();
			bitsPool.free(addListenerBits);
			bitsPool.free(removeListenerBits);
			objectListeners.end();
			notifying = false;
		}

	}

	public boolean isNotifying() {
		return notifying;
	}

	public Array<GameObject> getControllerGroupObjects(ControllerGroup group) {
		Array<GameObject> objectsInGroup = objectsByGroup.get(group);
		if (objectsInGroup == null) {
			objectsInGroup = new Array<GameObject>();
			objectsByGroup.put(group, objectsInGroup);
			objectListenerMasks.put(group, new Bits());

			for (GameObject object : objects) {
				refreshControllerGroup(object);
			}
		}
		return objectsInGroup;
	}

	@Override
	protected void drawDebugBounds(ShapeRenderer shapes) {
		super.drawDebugBounds(shapes);
	}

	public <t extends AbstractSystem> t getSystem(Class<t> type) {
		return getSystem(type,false);
		// for (AbstractSystem system : systems) {
		// if (system.getClass().getName().equals(type.getName())
		// || system.getClass().getSuperclass().getName().equals(type.getName())) {
		// //
		// System.out.println("sysclass_name="+system.getClass().getSuperclass().getName());
		// // System.out.println("type_passed_name="+type.getName());
		// return (t) system;
		// }
		// }
		// return null;
	}
	
	/**
	 * get the system and add if this is a superclass type that may return a subclass
	 * @param type
	 * @param superclass - whether or not a subclass is a valid return
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <t extends AbstractSystem> t getSystem(Class<t> type,boolean superclass) {
		t system = (t) systemsByClass.get(type);
		if(superclass && system == null) {
			for (AbstractSystem s : systems) {
				if (s.getClass().getSuperclass().getName().equals(type.getName())) {
					system = (t) s;
					break;
				}
			}
		}
		return system;
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

			// setCentered();
		}
		
		public GameLayer getLayer() {
			return layer;
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
			cam.position.set(x * cam.zoom - offset.x, y * cam.zoom - offset.y, cam.position.z);
		}

		public void setCentered() {
			offset.x = cam.viewportWidth * .5f;
			offset.y = cam.viewportHeight * .5f;
		}

		public Vector2 getPosition() {
			position.set(cam.position.x + offset.x, cam.position.y + offset.y);
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

	private class ControllerListener implements EventListener<GameObject> {

		@Override
		public void process(Event<GameObject> event, GameObject object) {
			refreshControllerGroup(object);
			gameObjectChanged(object, GameObjectChangeType.CONTROLLER, 1);

		}

	}

	private static class BitsPool extends Pool<Bits> {
		@Override
		protected Bits newObject() {
			return new Bits();
		}
	}

	private static class ObjectListenerData {
		public GameObjectListener listener;
		public int priority;
	}

	private static class ControllerOperationPool extends Pool<ControllerOperation> {

		@Override
		protected ControllerOperation newObject() {
			return new ControllerOperation();
		}

	}
	
	private static class ObjectOperationPool extends Pool<GameObjectOperation> {

		@Override
		protected GameObjectOperation newObject() {
			return new GameObjectOperation();
		}
		
	}
	
	private static class GameObjectOperation {
		public static final int ADD = 1;
		public static final int REMOVE = -1;
		
		public int type; 
		public GameObject object;
		public MapProperties properties;
	}

	private static class ControllerOperation {
		public static final int ADD = 1;
		public static final int REMOVE = -1;

		public int type;
		public GameObject object;
		//public GameObjectController controller;

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
