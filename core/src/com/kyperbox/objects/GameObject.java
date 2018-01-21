package com.kyperbox.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.kyperbox.GameState;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.controllers.GameObjectController;

public abstract class GameObject extends Group {

	public static final String NO_SPRITE = "NO_SPRITE_RENDERED";

	private GameLayer layer;
	private String sprite;

	// collision vars -- will only be used if layer supports collision
	// it is layer manager responsibility to use these vars.
	private boolean ignore_collision;
	private int group;
	private int filter;
	private Rectangle bounds;
	private Rectangle ret_bounds;

	private Array<GameObjectController> controllers;
	private MapProperties properties;
	private Vector2 velocity;

	private Color debug_bounds;
	private boolean flip_x;
	private boolean flip_y;

	public GameObject() {
		controllers = new Array<GameObjectController>();
		sprite = NO_SPRITE;
		velocity = new Vector2();
		setOrigin(Align.center);
		debug_bounds = Color.YELLOW;
		flip_x = false;
		flip_y = false;
		ignore_collision = false;
		group = 1;
		filter = -1;
	}
	
	public void setGroup(int group) {
		this.group = group;
	}
	
	public void setFilter(int filter) {
		this.filter = filter;
	}
	
	public int getGroup() {
		return group;
	}
	
	public int getFilter() {
		return filter;
	}

	public boolean ignoresCollision() {
		return ignore_collision;
	}

	public void setIgnoreCollision(boolean ignore_collision) {
		this.ignore_collision = ignore_collision;
	}

	public boolean getFlipX() {
		return flip_x;
	}

	public boolean getFlipY() {
		return flip_y;
	}

	public void setFlip(boolean flip_x, boolean flip_y) {
		this.flip_x = flip_x;
		this.flip_y = flip_y;
	}

	public void setCollisionBounds(float x, float y, float w, float h) {
		bounds.set(x, y, w, h);
	}

	public Rectangle getCollisionBounds() {
		if (getParent() != null && getParent() != layer)
			ret_bounds.set(getX() + getParent().getX() + bounds.x, getY() + getParent().getY() + bounds.y, bounds.width,
					bounds.height);
		else
			ret_bounds.set(getX() + bounds.x, getY() + bounds.y, bounds.width, bounds.height);
		return ret_bounds;
	}

	public void setDebugBoundsColor(Color color) {
		this.debug_bounds = color;
	}

	@Override
	public void drawDebug(ShapeRenderer shapes) {
		if (getDebug()) {
			shapes.setColor(debug_bounds);
			Rectangle b = bounds;
			shapes.rect(b.x + getX(), b.y + getY(), b.width, b.height);
		}
		super.drawDebug(shapes);
	}

	public void setVelocity(float x, float y) {
		velocity.set(x, y);
	}

	public Vector2 getVelocity() {
		return velocity;
	}

	public void addChild(GameObject child) {
		if (getParent() != null && getParent() == layer) {// this is a base object
			addActor(child);
			child.setGameLayer(layer);
			layer.gameObjectAdded(child, this);
			child.init(properties);
		} else if (getParent() == null) {
			addActor(child);
		} else {
			KyperBoxGame.error("GAMEOBJECT [" + getName()+"] ->", "Could not add child to child object.");
		}

	}

	@Override
	public void addActor(Actor actor) {
		if (!(actor instanceof GameObject)) {
			getState().log("unable to add [" + actor.getName() + "] to [" + getName()
					+ "]. Only GameObjects can be children of GameObjects");
			return;
		}
		super.addActor(actor);
	}

	public <t> t getController(Class<t> type) {
		for (GameObjectController manager : controllers)
			if (type.isInstance(manager))
				return type.cast(manager);
		return null;
	}

	public MapProperties getProperties() {
		return properties;
	}

	public void init(MapProperties properties) {
		setOrigin(Align.center);
		this.properties = properties;
		bounds = new Rectangle(0, 0, getWidth(), getHeight());
		ret_bounds = new Rectangle(bounds);
		if (getChildren().size > 0)
			for (int i = 0; i < getChildren().size; i++) {
				GameObject child = (GameObject) getChildren().get(i);
				child.setGameLayer(layer);
				init(properties);
			}
		for (int i = 0; i < controllers.size; i++) {
			controllers.get(i).init(this);
		}

	}

	public Array<GameObjectController> getControllers() {
		return controllers;
	}

	public void update(float delta) {
		for (int i = 0; i < controllers.size; i++)
			controllers.get(i).update(this, delta);
		setPosition(getX() + velocity.x * delta, getY() + velocity.y * delta);
	}
	
	public float getAbsoluteX() {
		if(getParent() != layer && getParent()!=null) {
			return getParent().getX()+getX();
		}
		return getX();
	}
	
	public float getAbsoluteY() {
		if(getParent() != layer&& getParent()!=null) {
			return getParent().getY()+getY();
		}
		return getY();
	}

	@Override
	public void act(float delta) {
		update(delta);
		super.act(delta);
	}

	public void onRemove() {
		for (int i = 0; i < controllers.size; i++) {
			controllers.get(i).remove(this);
		}
	}

	protected void setGameLayer(GameLayer layer) {
		this.layer = layer;
	}

	public void setSprite(String sprite) {
		this.sprite = sprite;
	}

	public void addController(GameObjectController controller) {
		if (getController(controller.getClass()) != null) {
			layer.getState().error("Cannot add type [" + controller.getClass().getName() + "] more than once.");
			return;
		}
		
		controllers.add(controller);
		controllers.sort(KyperBoxGame.getPriorityComperator());
		controller.init(this);
		if(layer!=null) {
			layer.gameObjectChanged(this, GameObjectChangeType.MANAGER, 1);
		}
	}
	
	public void removeController(GameObjectController controller) {
		if(controllers.removeValue(controller, true)) {
			controller.remove(this);
			if(layer!=null) {
				layer.gameObjectChanged(this, GameObjectChangeType.MANAGER, -1);
			}
		}
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		if (sprite != null && !sprite.isEmpty() && !sprite.equals(NO_SPRITE)) {
			Sprite render = layer.getGameSprite(sprite);
			render.setPosition(MathUtils.round(getX()), MathUtils.round(getY()));
			render.setRotation(getRotation());
			render.setAlpha(getColor().a * parentAlpha);
			render.setOrigin(getOriginX(), getOriginY());
			render.setColor(getColor());
			render.setSize(getWidth(), getHeight());
			render.setScale(getScaleX(), getScaleY());
			render.setFlip(flip_x, flip_y);
			render.draw(batch);
		}
		super.draw(batch, parentAlpha);
	}

	@Override
	public void setPosition(float x, float y) {
		if (layer != null && (x != getX() || y != getY()))
			layer.gameObjectChanged(this, GameObjectChangeType.LOCATION, 1);
		super.setPosition(x, y);
	}

	/**
	 * get the game layer this object belongs to
	 * 
	 * @return
	 */
	public GameLayer getGameLayer() {
		return layer;
	}

	public KyperBoxGame getGame() {
		return getState().getGame();
	}

	public GameState getState() {
		return layer.getState();
	}

	@Override
	public boolean remove() {
		boolean l = false;
		if (getParent() == layer) {
			layer.GameObjectRemoved(this, null);
			onRemove();
			l = layer.removeActor(this);
			layer = null;
		} else if (getParent() instanceof GameObject) {
			layer.GameObjectRemoved(this, (GameObject) getParent());
			onRemove();
			layer = null;
			l = true;
		}
		return l;
	}
	
	//map properties utility --------------------------
	
	public int[] getIntArray(String name) {
		String[] strings = getStringArray(name);
		if(strings == null)
			return null;
		int[] ints = new int[strings.length];
		for (int i = 0; i < strings.length; i++) {
			if(!isNumeric(strings[i]))
				throw new GdxRuntimeException(String.format("GameObject [%s] could not create Int Array. %s is not a valid number.",getName(),strings[i]));
			ints[i] = Integer.parseInt(strings[i]);
		}
		return ints;
	}
	
	@Override
	public void setBounds(float x, float y, float width, float height) {
		setCollisionBounds(x, y, width, height);
	}
	
	/**
	 * comma delimited set of strings
	 * @param name
	 * @return
	 */
	public String[] getStringArray(String name) {
		if(!getProperties().containsKey(name))
			return null;
		String[] strings = getProperties().get(name,String.class).trim().split(",");
		return strings;
	}
	
	/**
	 * comma delimited set of floats
	 * ignores
	 * @param name
	 * @return
	 */
	public float[] getFloatArray(String name) {
		String[] strings = getStringArray(name);
		if(strings == null)
			return null;
		float[] floats = new float[strings.length];
		for (int i = 0; i < strings.length; i++) {
			if(!isNumeric(strings[i]))
				throw new GdxRuntimeException(String.format("GameObject [%s] could not create FloatArray. %s is not a valid float.",getName(),strings[i]));
			floats[i] = Float.parseFloat(strings[i]);
		}
		return floats;
	}
	
	private boolean isNumeric(String s) {  
	    return s != null && s.matches("[-+]?\\d*\\.?\\d+");  
	} 
	
	/**
	 * use this to pass messages to this object.
	 * Must be overridden 
	 * @param type
	 * @param args
	 * @return
	 */
	public boolean passGameMessage(int type,Object...args) {
		//TODO OVERRIDE ---
		return false;
	}

	public static class GameObjectChangeType {
		public static final int MANAGER = 0; //value -1 means removed  1 = added
		public static final int LOCATION = 1;
		public static final int NAME = 2;
		public static final int FOLLOW_BOUNDS_REACHED = 3;
	}
}
