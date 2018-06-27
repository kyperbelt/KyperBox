package com.kyperbox.controllers;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.managers.Priority;
import com.kyperbox.objects.BasicGameObject;
import com.kyperbox.objects.GameObject;
import com.kyperbox.objects.GameObjectController;
import com.kyperbox.systems.CollisionSystem;

/**
 * a collision controller that uses a QuadTree to check for collisions using int
 * filtering.
 * 
 * @author john
 *
 */
public class CollisionController extends GameObjectController {

	private static GameObject check_object = new BasicGameObject();
	
	private CollisionSystem tree;
	private Array<CollisionData> collisions;
	private Rectangle check;
	private GameObject object;

	/**
	 * a collision controller that takes in a collisiosn system
	 * 
	 * @param collision_system - can be left null to use the first collision system it can find in the parent objects layer
	 */
	public CollisionController(CollisionSystem collision_system) {
		this.tree = collision_system;
		check = new Rectangle();
		this.collisions = new Array<CollisionController.CollisionData>();
	}
	
	/**
	 * get the current collisions for the parent of this controller
	 * @return
	 */
	public Array<CollisionData> getCollisions(){
		return getCollisions(0.0f);
	}

	/**
	 * get the collisions for this object in the future
	 * @param delta - the amount in the future to test for (usually just the frame elapsed time)
	 * @return
	 */
	public Array<CollisionData> getCollisions(float delta) {
		if (tree != null) {
			CollisionData.getPool().freeAll(collisions);
			collisions.clear();
			
			//setup check_obj - used to check for future positions without messing with the original object 
			check_object.setFilter(object.getFilter());
			check_object.setGroup(object.getGroup());
			check_object.setPosition(object.getX()+object.getVelocity().x * delta, object.getY()+object.getVelocity().y*delta);
			check_object.setSize(object.getWidth(), object.getHeight());
			check = object.getBoundsRaw();
			check_object.setCollisionBounds(check.x, check.y, check.width, check.height);
			check_object.init(KyperBoxGame.NULL_PROPERTIES); //object is initiated for checking purposes but doesnt get added to the layer
			
			Array<GameObject> possible_collisions = tree.getPossibleCollisions(check_object);
			for (int i = 0; i < possible_collisions.size; i++) {
				GameObject target = possible_collisions.get(i);
				if(target == object)
					continue;
				if (Intersector.overlapConvexPolygons(check_object.getCollisionPolygon(), target.getCollisionPolygon())) {
					CollisionData data = CollisionData.getPool().obtain();

					Rectangle self_bounds = check_object.getCollisionPolygon().getBoundingRectangle();
					Rectangle target_bounds = target.getCollisionPolygon().getBoundingRectangle();
					
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
		} else {
			if (tree == null) {
				tree = object.getGameLayer().getSystem(CollisionSystem.class);
			}
			if (tree == null && KyperBoxGame.DEBUG_LOGGING)
			object.getState().log("no collison sytem found : is null.");
		}
		return collisions;
	}

	@Override
	public void init(GameObject object) {
		if(object.getParent() == null)
			return;
		setPriority(Priority.HIGH);
		collisions.clear();
		this.object = object;

	}

	@Override
	public void update(GameObject object, float delta) {
		if (tree == null) {
			tree = object.getGameLayer().getSystem(CollisionSystem.class);
		}

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
			Array<GameObject> possible_collisions = tree.getPossibleCollisions(object);
			for (int i = 0; i < possible_collisions.size; i++) {
				GameObject target = possible_collisions.get(i);
				if(target!=object&&target.getCollisionBounds().overlaps(check)) {
					return target;
				}
			}
		}
		return null;
	}
	
	public CollisionSystem getCollisionSystem() {
		return tree;
	}

	@Override
	public void remove(GameObject object) {

	}

	@Override
	public void reset() {
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
