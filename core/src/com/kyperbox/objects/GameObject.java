package com.kyperbox.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.kyperbox.GameState;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.input.GameInput;
import com.kyperbox.umisc.KyperSprite;
import com.kyperbox.umisc.StringUtils;

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
	private Polygon col_poly;
	private Polygon ret_poly;

	private Array<GameObjectController> controllers;
	private MapProperties properties;
	private Vector2 velocity;
	private Vector2 position;
	private Vector2 collision_center;
	private float depth; // this is the z depth component;
	private float thickness; // this is the pseudo 3 dimensional height
	private float depth_velocity;

	private Color debug_bounds;
	private boolean flip_x;
	private boolean flip_y;
	private boolean apply_velocity = true; // whether or not this object applies its own velocity

	private boolean change_sprite;
	private KyperSprite render;
	private boolean pre_draw_children = false;


	public GameObject() {
		setTransform(false);
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
		change_sprite = true;
		render = null;
		depth = 0;
		thickness = 1;
		position = new Vector2();
		collision_center = new Vector2();
	}

	public float getDepthVelocity() {
		return depth_velocity;
	}

	public void setDepthVelocity(float depth_velocity) {
		this.depth_velocity = depth_velocity;
	}

	public Vector2 getPosition() {
		position.set(getX(), getY());
		return position;
	}

	public Vector2 getTruePosition() {
		position.set(getTrueX(), getTrueY());
		return position;
	}

	/**
	 * get the depth component of the gameobject default is 0 and unless you are
	 * using a 3rd dimension in a top down game it should be ignored because it will
	 * effect rendering
	 * 
	 * @return
	 */
	public float getDepth() {
		return depth;
	}

	/**
	 * get the depth top of this object
	 * 
	 * @return trueY+trueDepth+thickness
	 */
	public float getDepthTop() {
		return getTrueY() + getTrueDepth() + getThickness();
	}

	/**
	 * get the depth bottom of this objecct
	 * 
	 * @return trueY+trueDepth
	 */
	public float getDepthBottom() {
		return getTrueY() + getTrueDepth();
	}

	/**
	 * set the depth of this object - this affects the rendering but not the
	 * rendering order
	 * 
	 * @param depth
	 */
	public void setDepth(float depth) {
		this.depth = depth;
	}

	/**
	 * get whether or not this object applies its own velocity after all controller
	 * updates
	 * 
	 * @return
	 */
	public boolean getApplyVelocity() {
		return apply_velocity;
	}

	/**
	 * set whether this object applies its own velocity after all controller updates
	 * 
	 * @param apply_velocity
	 */
	public void setApplyVelocity(boolean apply_velocity) {
		this.apply_velocity = apply_velocity;
	}

	/**
	 * get the depth of this child all the way up to its parent
	 * 
	 * @return
	 */
	public float getTrueDepth() {
		if (getParent() != null) {
			if (getParent() instanceof GameObject) {
				GameObject p = (GameObject) getParent();
				return p.getTrueDepth() + getDepth();
			}
		}
		return getDepth();
	}

	/**
	 * get the center point of the collision bounds. sometimes considered the true
	 * center of a game object.
	 * 
	 * @return
	 */
	public Vector2 getCollisionCenter() {
		collision_center.set(getBoundsX() + getBoundsRaw().width * .5f, getBoundsY() + getBoundsRaw().height * .5f);
		return collision_center;
	}

	/**
	 * get the 3d thickness/height of this object. default is 0 and should be
	 * ignored unless using the 3rd dimension. DOES NOT EFFECT RENDERING
	 * 
	 * @return
	 */
	public float getThickness() {
		return thickness;
	}

	public void setThickness(float thickness) {
		this.thickness = thickness;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	public void setFilter(int filter) {
		this.filter = filter;
	}

	public boolean isRemoved() {
		return getParent() == null;
	}

	public int getGroup() {
		return group;
	}

	public int getFilter() {
		return filter;
	}

	public void setAlpha(float alpha) {
		setColor(getColor().r, getColor().g, getColor().b, alpha);
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

	public float getTrueX() {
		if (getParent() != null) {
			if (getParent() instanceof GameObject) {
				GameObject p = (GameObject) getParent();

				return p.getTrueX() + getX();

			} else if (getParent() != layer) {
				return getParent().getX() + getX();
			}
		}
		return getX();
	}

	public float getTrueY() {
		if (getParent() != null) {
			if (getParent() instanceof GameObject) {
				GameObject p = (GameObject) getParent();
				return p.getTrueY() + getY();
			} else if (getParent() != layer) {
				return getParent().getY() + getY();
			}
		}
		return getY();
	}

	public float getTrueRotation() {
		if (getParent() != null) {
			if (getParent() instanceof GameObject) {
				GameObject p = (GameObject) getParent();
				return p.getTrueRotation() + getRotation();
			} else if (getParent() != layer) {
				return getParent().getRotation() + getRotation();
			}
		}
		return getRotation();
	}
	
	public String getSprite() {
		return sprite;
	}
	
	public KyperSprite getRenderSprite() {
		return render;
	}

	public void setFlip(boolean flip_x, boolean flip_y) {
		this.flip_x = flip_x;
		this.flip_y = flip_y;
	}

	public void setCollisionBounds(float x, float y, float w, float h) {
		if (bounds == null)
			bounds = new Rectangle();
		bounds.set(x, y, w, h);
		col_poly = null;
	}

	public Rectangle getCollisionBounds() {
		if (getParent() != null) {
			ret_bounds.setCenter(getOriginX(), getOriginY());
			ret_bounds.set(getTrueX() + bounds.x, getTrueY() + bounds.y, bounds.width, bounds.height);
			// System.out.println("object||"+getName()+"|| bounds - "+bounds+" - retbounds
			// -"+ret_bounds+" truepos["+getTrueX()+","+getTrueY()+"]");
			return ret_bounds;
		} else if (getCollisionPolygon() != null) {
			return getCollisionPolygon().getBoundingRectangle();
		}

		return null;
	}

	public Polygon getCollisionPolygon() {
		if (bounds != null && col_poly == null) {
			ret_poly = new Polygon();
			col_poly = new Polygon(new float[] { 0, 0, 0 + bounds.getWidth(), 0, 0 + bounds.getWidth(),
					0 + bounds.getHeight(), 0, 0 + bounds.getHeight() });
			col_poly.setOrigin(bounds.getWidth() * .5f, bounds.getHeight() * .5f);
		}
		if (col_poly == null)
			return null;

		col_poly.setScale(getScaleX(), getScaleY());
		col_poly.setPosition(getTrueX() + bounds.getX(), getTrueY() + bounds.getY());
		col_poly.setRotation(getRotation());
		ret_poly.setVertices(col_poly.getTransformedVertices());
		return col_poly;
	}

	public void setDebugBoundsColor(Color color) {
		this.debug_bounds = color;
	}

	@Override
	protected void drawChildren(Batch batch, float parentAlpha) {
		super.drawChildren(batch, parentAlpha);
	}

	@Override
	public void drawDebug(ShapeRenderer shapes) {
		if (getDebug()) {
			shapes.setColor(ignoresCollision() ? Color.PINK : debug_bounds);
			Color cc = shapes.getColor();
			Polygon p = getCollisionPolygon();
			if (bounds != null && p != null) {
				shapes.polygon(ret_poly.getTransformedVertices());
				Rectangle r = ret_poly.getBoundingRectangle();
				// shapes.setColor(Color.SKY);
				shapes.rect(r.x, r.y, r.width, r.height);
			}
			shapes.setColor(cc);
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
		if (getParent() != null) {
			addActor(child);

			if (layer != null) {
				child.setGameLayer(layer);
				layer.gameObjectAdded(child, this);
				child.init(properties);
			}

		} else if (getParent() == null) {
			addActor(child);
		}

	}

	@Override
	public void addActor(Actor actor) {
		if (!(actor instanceof GameObject)) {
			getState().log("you should not add [" + actor.getName() + "] to [" + getName()
					+ "]. Only GameObjects are recommended children of GameObjects");
		}
		super.addActor(actor);
	}

	@SuppressWarnings("unchecked")
	public <t> t getController(Class<t> type) {
		for (GameObjectController manager : controllers)
			if (manager.getClass().getName().equals(type.getName())
					|| manager.getClass().getSuperclass().getName().equals(type.getName())) {
				// System.out.println("sysclass_name="+system.getClass().getSuperclass().getName());
				// System.out.println("type_passed_name="+type.getName());
				return (t) manager;
			}
		// if (type.isInstance(manager))
		// return type.cast(manager);
		return null;
	}

	public MapProperties getProperties() {
		return properties;
	}

	public void init(MapProperties properties) {
		setOrigin(Align.center);
		this.properties = properties;
		if (bounds == null)
			bounds = new Rectangle(0, 0, getWidth(), getHeight());
		ret_bounds = new Rectangle(bounds);
		if (getChildren().size > 0)
			for (int i = 0; i < getChildren().size; i++) {
				if (!(getChildren().get(i) instanceof GameObject))
					continue;
				GameObject child = (GameObject) getChildren().get(i);
				child.setGameLayer(layer);
				layer.gameObjectAdded(child, this);
				child.init(properties);
			}
		for (int i = 0; i < controllers.size; i++) {
			controllers.get(i).init(this);
		}

	}

	public Array<GameObjectController> getControllers() {
		return controllers;
	}

	/**
	 * get the raw bounds untrasnformed
	 * 
	 * @return
	 */
	public Rectangle getBoundsRaw() {
		if (bounds == null)
			bounds = new Rectangle();
		return bounds;
	}

	/**
	 * get the world y of the bounds
	 * 
	 * @return
	 */
	public float getBoundsY() {
		return getY() + (bounds != null ? bounds.getY() : 0);
	}

	/**
	 * get the world x of the bounds
	 * 
	 * @return
	 */
	public float getBoundsX() {
		return getX() + (bounds != null ? bounds.getX() : 0);
	}

	public void update(float delta) {
		controllers.sort(KyperBoxGame.getPriorityComperator());
		for (int i = 0; i < controllers.size; i++)
			controllers.get(i).update(this, delta);

		if (apply_velocity)
			setPosition(getX() + velocity.x * delta, getY() + velocity.y * delta);
	}

	@Override
	public void act(float delta) {
		update(delta);
		super.act(delta);
	}

	public void onRemove() {
		// for (int i = 0; i < controllers.size; i++) {
		// controllers.get(i).remove(this);
		// }
	}

	protected void setGameLayer(GameLayer layer) {
		this.layer = layer;
	}

	public void setSprite(String sprite) {
		this.sprite = sprite;
		change_sprite = true;
		if (sprite == null || sprite.isEmpty())
			this.sprite = NO_SPRITE;
	}

	public void setRawSprite(KyperSprite sprite) {
		this.sprite = sprite.getName();
		this.render = sprite;

	}

	public void addController(GameObjectController controller) {
		if (getController(controller.getClass()) != null) {
			if (KyperBoxGame.DEBUG_LOGGING)
				layer.getState().error("->" + getName() + " :Cannot add type [" + controller.getClass().getName()
						+ "] more than once.");
			return;
		}

		controllers.add(controller);
		controller.setRemoved(false);
		controllers.sort(KyperBoxGame.getPriorityComperator());

		if (layer != null) {
			controller.init(this);
			layer.gameObjectChanged(this, GameObjectChangeType.CONTROLLER, 1);
		}
	}

	public void removeController(GameObjectController controller) {
		if (controllers.removeValue(controller, true)) {
			controller.remove(this);
			controller.setRemoved(true);
			if (layer != null) {
				layer.gameObjectChanged(this, GameObjectChangeType.CONTROLLER, -1);
			}
		}
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {

		if (pre_draw_children) {
			super.draw(batch, parentAlpha);
		}

		if (change_sprite && !sprite.equals(NO_SPRITE) && layer != null) {
			String atlas = null;
			if (sprite.contains(KyperBoxGame.COLON)) {
				String ss[] = sprite.split(KyperBoxGame.COLON);
				atlas = ss[0].trim();
				sprite = ss[1].trim();
			}
			if (atlas == null)
				render = (KyperSprite) layer.getGameSprite(sprite);
			else
				render = (KyperSprite) layer.getGameSprite(sprite, atlas);
			change_sprite = false;
		} else if (change_sprite && sprite.equals(NO_SPRITE)) {
			render = null;
			change_sprite = false;
		}

		if (render != null) {

			if (getParent().isTransform()) {
				render.setPosition(getX(), getY() + getDepth());
				render.setRotation(getRotation());
			} else {
				render.setPosition(getTrueX(), getTrueY() + getTrueDepth());
				render.setRotation(getTrueRotation());
			}
			render.setAlpha(getColor().a * parentAlpha);
			render.setOrigin(getOriginX(), getOriginY());
			render.setColor(getColor());
			render.setSize(getWidth(), getHeight());
			render.setScale(getScaleX(), getScaleY());
			render.setFlip(flip_x, flip_y);
			render.draw(batch, parentAlpha);
		}

		if (!pre_draw_children)
			super.draw(batch, parentAlpha);
	}
	
	public boolean isPreDrawChildren() {
		return pre_draw_children;
	}
	
	public void setPreDrawChildren(boolean pre_draw_children) {
		this.pre_draw_children = pre_draw_children;
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
		return layer != null ? layer.getState() : null;
	}

	@Override
	public boolean remove() {
		boolean l = false;
		if (getParent() == null)
			return l;
		else if (getParent() == layer) {
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

	// map properties utility --------------------------

	public int[] getIntArray(String name) {
		String[] strings = getStringArray(name);
		if (strings == null)
			return null;
		int[] ints = new int[strings.length];
		for (int i = 0; i < strings.length; i++) {
			if (!isNumeric(strings[i]))
				throw new GdxRuntimeException(
						StringUtils.format("GameObject [%s] could not create Int Array. %s is not a valid number.",
								getName(), strings[i]));
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
	 * 
	 * @param name
	 * @return
	 */
	public String[] getStringArray(String name) {
		if (!getProperties().containsKey(name))
			return null;
		String[] strings = getProperties().get(name, String.class).trim().split(",");
		return strings;
	}

	/**
	 * comma delimited set of floats ignores
	 * 
	 * @param name
	 * @return
	 */
	public float[] getFloatArray(String name) {
		String[] strings = getStringArray(name);
		if (strings == null)
			return null;
		float[] floats = new float[strings.length];
		for (int i = 0; i < strings.length; i++) {
			if (!isNumeric(strings[i]))
				throw new GdxRuntimeException(
						StringUtils.format("GameObject [%s] could not create FloatArray. %s is not a valid float.",
								getName(), strings[i]));
			floats[i] = Float.parseFloat(strings[i]);
		}
		return floats;
	}

	private boolean isNumeric(String s) {
		return s != null && s.matches("[-+]?\\d*\\.?\\d+");
	}

	public GameInput getGameInput() {
		return getGame().getInput();
	}

	/**
	 * use this to pass messages to this object. Must be overridden
	 * 
	 * @param type
	 * @param args
	 * @return
	 */
	public boolean passGameMessage(int type, Object... args) {
		// TODO OVERRIDE ---
		return false;
	}

	public static class GameObjectChangeType {
		public static final int CONTROLLER = 0; // value -1 means removed 1 = added
		public static final int LOCATION = 1;
		public static final int NAME = 2;
		public static final int FOLLOW_BOUNDS_REACHED = 3;
	}
}
