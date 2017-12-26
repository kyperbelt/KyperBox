package com.kyperbox.controllers;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.kyperbox.managers.Priority;
import com.kyperbox.objects.GameObject;
import com.kyperbox.systems.QuadTree;

/**
 * a collision controller that uses a QuadTree to check for collisions using int
 * filtering.
 * 
 * @author john
 *
 */
public class CollisionController extends GameObjectController {

	private IntArray filter;
	private int group;
	private boolean sensor;
	private QuadTree tree;
	private Array<CollisionData> collisions;
	private Rectangle check;

	public CollisionController(int group, IntArray filter) {
		this.group = group;
		this.filter = filter;
		this.sensor = false;
		check = new Rectangle();
		this.collisions = new Array<CollisionController.CollisionData>();
	}

	public CollisionController(int group, int... filter) {
		this(group, new IntArray(filter));
	}

	public CollisionController(int group) {
		this(group, new IntArray());
	}

	public boolean isSensor() {
		return sensor;
	}

	public void setSensor(boolean sensor) {
		this.sensor = sensor;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	public int getGroup() {
		return group;
	}

	public IntArray getFilter() {
		return filter;
	}

	public Array<CollisionData> getCollisions() {
		return collisions;
	}

	@Override
	public void init(GameObject object) {
		tree = object.getGameLayer().getSystem(QuadTree.class);
		setPriority(Priority.HIGH);
		object.getState().log("COLCon init:"+object.getName());
		collisions.clear();

	}

	@Override
	public void update(GameObject object, float delta) {
		if (tree == null) {
			tree = object.getGameLayer().getSystem(QuadTree.class);
		}

		if (tree != null) {
			CollisionData.getPool().freeAll(collisions);
			collisions.clear();
			Array<GameObject> possible_collisions = tree.checkPossibleCollisionsForRect(object.getCollisionBounds().x, object.getCollisionBounds().y, object.getCollisionBounds().width, object.getCollisionBounds().height);
			for (int i = 0; i < possible_collisions.size; i++) {
				GameObject target = possible_collisions.get(i);
				CollisionController target_controller = target.getController(CollisionController.class);
				if (target == object || target_controller == null)
					continue;
				if (Intersector.overlaps(object.getCollisionBounds(), target.getCollisionBounds())) {
					CollisionData data = CollisionData.getPool().obtain();
					Rectangle self_bounds = object.getCollisionBounds();
					Rectangle target_bounds = target.getCollisionBounds();
					
					//create a quad for the collision
					float x = self_bounds.x > target_bounds.x ? self_bounds.x : target_bounds.x; 						//X
					float y = self_bounds.y > target_bounds.y ? self_bounds.y : target_bounds.y; 						//Y
					float w = self_bounds.x > target_bounds.x ? target_bounds.x + target_bounds.width - self_bounds.x	//WIDTH
							: self_bounds.x + self_bounds.width - target_bounds.x;									
					float h = self_bounds.y > target_bounds.y ? target_bounds.y + target_bounds.height - self_bounds.y	//HEIGHT
							: self_bounds.y + self_bounds.height - target_bounds.y;

					data.init(object, target, this, x, y, w, h);
					collisions.add(data);
				}
			}
		} else
			object.getState().log("tree is null.");

	}
	
	/**
	 * get all possible collisions with object and offsets
	 * TODO:put this method in the quadtree 
	 * @param object
	 * @param xoff
	 * @param yoff
	 * @return
	 */
	public GameObject collisionWithOffset(GameObject object,float xoff, float yoff) {
		if (tree != null) {
			check.set(object.getCollisionBounds().x+xoff, object.getCollisionBounds().y+yoff, object.getCollisionBounds().width, object.getCollisionBounds().height);
			Array<GameObject> possible_collisions = tree.checkPossibleCollisionsForRect(check.x, check.y, check.width, check.height);
			for (int i = 0; i < possible_collisions.size; i++) {
				GameObject target = possible_collisions.get(i);
				if(target!=object&&target.getCollisionBounds().overlaps(check)) {
					return target;
				}
			}
		}
		return null;
	}
	
	public QuadTree getTree() {
		return tree;
	}

	@Override
	public void remove(GameObject object) {

	}

	@Override
	public void reset() {
		filter.clear();
		group = -1;
		collisions.clear();
		super.reset();
	}

	public static class CollisionData implements Poolable {
		private static Pool<CollisionData> col_pool;

		public static Pool<CollisionData> getPool() {
			if (col_pool == null)
				col_pool = new Pool<CollisionController.CollisionData>() {
					@Override
					protected CollisionData newObject() {
						return new CollisionData();
					}
				};
			return col_pool;
		}

		private GameObject self;
		private CollisionController controller;
		private GameObject target;
		private Rectangle overlap_box;

		public CollisionData() {
			controller = null;
			self = null;
			target = null;
			overlap_box = new Rectangle();
		}

		public void init(GameObject self, GameObject target, CollisionController controller, float x, float y, float w,
				float h) {
			this.controller = controller;
			this.self = self;
			this.target = target;
			this.overlap_box.set(x, y, w, h);
		}

		public GameObject getSelf() {
			return self;
		}

		public GameObject getTarget() {
			return target;
		}

		/**
		 * the box created by the collision overlap
		 * 
		 * @return
		 */
		public Rectangle getOverlapBox() {
			return overlap_box;
		}

		public CollisionController getCollisionController() {
			return controller;
		}

		@Override
		public void reset() {
			controller = null;
			self = null;
			target = null;
			overlap_box.set(0, 0, 1, 1);
		}

	}

}
