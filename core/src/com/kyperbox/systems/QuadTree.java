package com.kyperbox.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.objects.BasicGameObject;
import com.kyperbox.objects.GameObject;
import com.kyperbox.objects.GameObject.GameObjectChangeType;
import com.kyperbox.umisc.StringUtils;

public class QuadTree extends CollisionSystem {

	//this padding is added for absolutely no reason TODO: remove? it doesnt bother me right now so im going to leave it.
	private static final int PAD = 0;
	// TODO: add bounds follow
	// TODO: test with object controller
	// TODO: add a recursive update check to clean up unused Quads

	private Rectangle bounds;
	private int max_depth; // how deep is the quad tree
	private int max_objects; // how many objects before the quad breaks
	private boolean follow_view; // does this manager follow the layer camera view
	private Array<GameObject> objects;
	private Pool<Quad> quad_pool;
	private Quad root; // the head honcho of this quad tree
	private Array<GameObject> ret_list; // an array used to return results to gameobjects for collision checks
	private GameObject check_object;
	private IntArray remove_objects;
	private boolean culling;

	/**
	 * create a quadtree collision manager with a default max_depth of 4 and a
	 * max_object count of 10.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public QuadTree(float x, float y, float width, float height) {
		check_object = new BasicGameObject();
		check_object.init(null);
		remove_objects = new IntArray();
		culling = true;
		bounds = new Rectangle(x - PAD, y - PAD, width + PAD * 2, height + PAD * 2);
		objects = new Array<GameObject>();
		follow_view = false;
		final QuadTree m = this;
		quad_pool = new Pool<QuadTree.Quad>() {
			@Override
			protected Quad newObject() {
				return new Quad(m);
			}
		};
		max_depth = 4;
		max_objects = 10;
		root = quad_pool.obtain();
		root.init(null, 0, bounds.x, bounds.y, bounds.width, bounds.height);
		ret_list = new Array<GameObject>();
	}

	public void setMaxDepth(int max_depth) {
		this.max_depth = max_depth;
	}

	public Quad getRoot() {
		return root;
	}

	public void setMaxObjects(int max_objects) {
		this.max_objects = max_objects;
	}

	public QuadTree(float width, float height) {
		this(0, 0, width, height);
	}

	@Override
	public void init(MapProperties properties) {
		// TODO: set boundaries here maybe????? using some underlying layer properties.
		// I dont know how to implement this
		// since layers at this moment dont have types so just adding properties seems
		// dirty.
		if (KyperBoxGame.DEBUG_LOGGING)
			getLayer().getState().log("QuadTree: init");
	}

	@Override
	public void gameObjectAdded(GameObject object, GameObject parent) {
		if (KyperBoxGame.DEBUG_LOGGING)
			getLayer().getState().log(StringUtils.format("QuadTree: added object[%s] with parent[%s]",object.getName()==null?KyperBoxGame.NULL_STRING:object.getName(),parent == null ?KyperBoxGame.NULL_STRING:parent.getName()));
		objects.add(object);

	}

	@Override
	public void gameObjectChanged(GameObject object, int type, float value) {
		if (type != GameObjectChangeType.LOCATION)
			return;

		if (!objects.contains(object, true)) {
			Rectangle o = object.getCollisionBounds();
			if (root.bounds.overlaps(o)) {
				objects.add(object);
			}
		}
	}

	@Override
	public void gameObjectRemoved(GameObject object, GameObject parent) {
		//object.getState().log("removed:"+object.getName()+" from quadtree");
		objects.removeValue(object, true);
	}

	@Override
	public void update(float delta) {
		root.clear();
		remove_objects.clear();
		
		Rectangle view_bounds = getLayer().getCamera().getViewBounds();
		
		if (follow_view) {
			
			root.setPosition( bounds.getX() + view_bounds.getX(), bounds.getY() + view_bounds.getY());

		}
		for (int i = 0; i < objects.size; i++) {
			Rectangle rect = objects.get(i).getCollisionBounds();
			if (culling && !root.bounds.overlaps(rect)) {
				remove_objects.add(i);
			} else
				root.place(objects.get(i));

		}
		

		for (int i = 0; i < remove_objects.size; i++) {
			if (remove_objects.get(i) < objects.size)
				objects.removeIndex(remove_objects.get(i));
		}
		

		
	}

	@Override
	public void drawDebug(ShapeRenderer shapes) {
		shapes.setColor(Color.SKY);
		root.debugRender(shapes);

	}

	@Override
	public void onRemove() {
		if (KyperBoxGame.DEBUG_LOGGING)
			getLayer().getState().log("QuadTree: removed");
		root.clear();
		ret_list.clear();
		objects.clear();
		remove_objects.clear();
	}

	public Pool<Quad> getPool() {
		return quad_pool;
	}

	/**
	 * if true then this quadtree bounds follow the camera view of the layer it
	 * belongs to.
	 * 
	 * @param follow_view
	 */
	public void setFollowView(boolean follow_view) {
		this.follow_view = follow_view;
	}

	public boolean isFollowingView() {
		return follow_view;
	}

	/**
	 * if true, objects are culled when they are outside the bounds of the root
	 * {@link Quad} and added when they re-enter.
	 * 
	 * @param culling
	 */
	public void setCulling(boolean culling) {
		this.culling = culling;
	}

	public boolean isCulling() {
		return culling;
	}

	@Override
	public Array<GameObject> getPossibleCollisions(GameObject object) {
		ret_list.clear();
		root.assessPossibleCollisions(ret_list, object);
		return ret_list;
	}

	public static class Quad implements Poolable {

		private Pool<Quad> pool;
		private QuadTree manager;
		private Quad parent;
		private Quad[] quads;
		private Rectangle bounds;
		private int depth;
		private Array<GameObject> quad_objects;

		public Quad(QuadTree manager) {
			this.pool = manager.getPool();
			this.manager = manager;
			quad_objects = new Array<GameObject>();
			bounds = new Rectangle();
			quads = new Quad[4];
		}

		public void init(Quad parent, int depth, float x, float y, float width, float height) {
			this.parent = parent;
			bounds.set(x, y, width, height);
			this.depth = depth;
		}

		public Quad getParent() {
			return parent;
		}

		@Override
		public void reset() {
			quad_objects.clear();
			depth = 0;

		}

		public Rectangle getBounds() {
			return bounds;
		}

		public void divide() {
			float x = this.bounds.getX();
			float y = this.bounds.getY();
			float s_w = this.bounds.getWidth() * .5f; // sub_ width
			float s_h = this.bounds.getHeight() * .5f;// sub_ height

			// init quads
			quads[0] = pool.obtain();
			quads[1] = pool.obtain();
			quads[2] = pool.obtain();
			quads[3] = pool.obtain();

			// set bounds
			quads[2].init(this, depth + 1, x + s_w, y, s_w, s_h);
			quads[3].init(this, depth + 1, x, y, s_w, s_h);
			quads[0].init(this, depth + 1, x, y + s_h, s_w, s_h);
			quads[1].init(this, depth + 1, x + s_w, y + s_h, s_w, s_h);
		}

		/**
		 * used to place an object in the correct quad
		 * 
		 * @param object
		 * @return the Quad this object got placed in
		 */
		public void place(GameObject object) {
			if (quads[0] != null) {
				int index = getQuadIndex(object);
				if (index != -1) {
					quads[index].place(object);
					return;
				}
			}

			quad_objects.add(object);

			if (quad_objects.size > manager.max_objects && depth < manager.max_depth) {
				if (quads[0] == null)
					divide();
				int i = 0;
				while (i < quad_objects.size) {
					int index = getQuadIndex(quad_objects.get(i));
					if (index != -1)
						quads[index].place(quad_objects.removeIndex(i));
					else
						i++;
				}
			}
		}

		public void assessPossibleCollisions(Array<GameObject> possible_collisions, GameObject object) {
			if (quads[0] != null) {
				for (int i = 0; i < quads.length; i++)
					if (quads[i].getBounds().overlaps(object.getCollisionBounds()))
						quads[i].assessPossibleCollisions(possible_collisions, object);
			}
			for (int i = 0; i < quad_objects.size; i++) {
				if (manager.shouldCollide(object, quad_objects.get(i)))
					possible_collisions.add(quad_objects.get(i));
			}
		}

		public void debugRender(ShapeRenderer shapes) {
			Rectangle view = manager.getLayer().getCamera().getViewBounds();
			shapes.rect(this.bounds.x-view.x ,
					this.bounds.y-view.y, this.bounds.width, this.bounds.height);
			if (quads[0] != null) {
				for (int i = 0; i < quads.length; i++) {
					quads[i].debugRender(shapes);
				}
			}
		}

		/**
		 * recursive return of objects contained in this and all its children
		 * 
		 * @return
		 */
		public int getSize() {
			int size = quad_objects.size;
			for (int i = 0; i < quads.length; i++) {
				if (quads[i] == null)
					break;
				size += quads[i].getSize();
			}
			return size;
		}

		public void translate(float x, float y) {
			bounds.setPosition(bounds.x + x, bounds.y + y);
			if (quads[0] != null)
				for (int i = 0; i < quads.length; i++) {
					quads[i].translate(x, y);
				}
		}
		
		public void setPosition(float x,float y) {
			bounds.setPosition(x, y);
		}

		/**
		 * check to see what quad this object belongs to
		 * 
		 * @param object
		 * @return
		 */
		public int getQuadIndex(GameObject object) {
			int quad_index = -1;
			if (quads[0] != null)
				for (int i = 0; i < quads.length; i++) {
					if (quads[i].getBounds().contains(object.getCollisionBounds())) {
						quad_index = i;
						break;
					}
				}
			return quad_index;
		}

		public void clear() {
			quad_objects.clear();
			for (int i = 0; i < quads.length; i++) {
				if (quads[i] != null) {
					quads[i].clear();
					pool.free(quads[i]);
					quads[i] = null;
				}
			}

		}
	}

}
