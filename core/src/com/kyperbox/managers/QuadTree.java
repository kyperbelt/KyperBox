package com.kyperbox.managers;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.kyperbox.objects.GameObject;
import com.kyperbox.objects.GameObject.GameObjectChangeType;

public class QuadTree extends LayerManager{
	
	//TODO: add bounds follow
	//TODO: test with object controller
	//TODO: add a recursive update check to clean up unused Quads
	
	
	private Rectangle bounds;
	private int max_depth; 		 //how deep is the quad tree
	private int max_objects; 	 //how many objects before the quad breaks
	private boolean follow_view; //does this manager follow the layer camera view
	private ObjectMap<GameObject,Quad> objects;
	private Pool<Quad> quad_pool;
	private Quad root;     //the head honcho of this quad tree 
	private Array<GameObject> ret_list; // an array used to return results to gameobjects for collision checks
	
	/**
	 * create a quadtree collision manager with a default max_depth of 4 
	 * and a max_object count of 10.
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public QuadTree(float x,float y,float width,float height) {
		bounds = new Rectangle(x, y, width, height);
		objects = new ObjectMap<GameObject,Quad>();
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
		root.init(null, 0, x, y, width, height);
		ret_list = new Array<GameObject>();
	}
	
	public void setMaxDepth(int max_depth) {
		this.max_depth = max_depth;
	}
	
	public void setMaxObjects(int max_objects) {
		this.max_objects = max_objects;
	}
	
	public QuadTree(float width,float height) {
		this(0, 0, width, height);
	}

	@Override
	public void init(MapProperties properties) {
		//TODO: set boundaries here maybe????? using some underlying layer properties. I dont know how to implement this
		//since layers at this moment dont have types so just adding properties seems dirty.
		getLayer().getState().log("QuadTree: init");
	}

	@Override
	public void gameObjectAdded(GameObject object, GameObject parent) {
		root.place(object);
		getLayer().getState().log("QuadTree: object_added="+object.getName());
	}

	@Override
	public void gameObjectChanged(GameObject object, GameObjectChangeType type, int value) {
		if(type != GameObjectChangeType.LOCATION)
			return;
		getLayer().getState().log("QuadTree: location_changed object="+object.getName());
		Quad check = objects.get(object);
		if(check != null) {
			if(!check.getBounds().contains(object.getCollisionBounds())) {
				check.remove(object);
				root.place(object);
			}
		}
	}

	@Override
	public void gameObjectRemoved(GameObject object, GameObject parent) {
		objects.get(object).remove(object);
		getLayer().getState().log("QuadTree: removed object ="+object.getName());
	}

	@Override
	public void update(float delta) {
	}
	
	@Override
	public void drawDebug(ShapeRenderer shapes) {
		root.debugRender(shapes);
	}

	@Override
	public void onRemove() {
		getLayer().getState().log("QuadTree: removed");
	}
	
	public Pool<Quad> getPool(){
		return quad_pool;
	}
	
	private void setObjectQuadPair(GameObject object,Quad quad) {
		objects.put(object, quad);
		
	}
	
	public static class Quad implements Poolable{
		
		private Pool<Quad> pool;
		private QuadTree manager;
		private Quad parent;
		private Quad[] quads;
		private Rectangle bounds;
		private int depth;
		private Array<GameObject> objects;
		
		public Quad(QuadTree manager) {
			this.pool = manager.getPool();
			this.manager = manager;
			objects = new Array<GameObject>();
			bounds = new Rectangle();
			quads = new Quad[4];
		}
		
		public void init(Quad parent,int depth,float x,float y,float width,float height) {
			this.parent = parent;
			bounds.set(x, y, width, height);
			this.depth = depth;
		}
		
		public Quad getParent() {
			return parent;
		}
		
		@Override
		public void reset() {
			objects.clear();
			depth = 0;
			
		}
		
		public Rectangle getBounds() {
			return bounds;
		}
		
		public void divide() {
			float x = bounds.getX();
			float y = bounds.getY();
			float s_w = bounds.getWidth()*.5f; //sub_ width
			float s_h = bounds.getHeight() * .5f;//sub_ height
			
			//init quads
			quads[0] = pool.obtain();
			quads[1] = pool.obtain();
			quads[2] = pool.obtain();
			quads[3] = pool.obtain();
			
			//set bounds
			quads[2].init(this,depth+1, x+s_w, y, s_w, s_h);
			quads[3].init(this,depth+1, x,y, s_w, s_h);
			quads[0].init(this,depth+1, x, y+s_h, s_w, s_h);
			quads[1].init(this,depth+1, x+s_w, y+s_h, s_w, s_h);
		}
		
		/**
		 * used to place an object in the correct quad
		 * @param object
		 * @return the Quad this object got placed in
		 */
		public void place(GameObject object) {
			if(quads[0]!=null) {
				int index = getQuadIndex(object);
				if(index!=-1) {
					quads[index].place(object);
					return;
				}
			}
			
			objects.add(object);
			manager.setObjectQuadPair(object, this);
			
			if(objects.size > manager.max_objects && depth < manager.max_depth) {
				if(quads[0]==null)
					divide();
				int i = 0;
				while(i < objects.size) {
					int index = getQuadIndex(objects.get(i));
					if(index!=-1)
						quads[index].place(objects.removeIndex(i));
					else
						i++;
				}
			}
		}
		
		public void assessPossibleCollisions(Array<GameObject> possible_collisions,GameObject object) {
			int index = getQuadIndex(object);
			if(index != -1 && quads[0]!=null){
				quads[index].assessPossibleCollisions(possible_collisions, object);
			}
			for (int i = 0; i < objects.size; i++) {
				if(objects.get(i)!=object)
					possible_collisions.add(object);
			}
		}
		
		public void debugRender(ShapeRenderer shapes) {
			shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
			if(quads[0]!=null) {
				for (int i = 0; i < quads.length; i++) {
					quads[i].debugRender(shapes);
				}
			}
		}
		
		/**
		 * recursive return of objects contained in this
		 * and all its children
		 * @return
		 */
		public int getSize() {
			int size = objects.size;
			for (int i = 0; i < quads.length; i++) {
				if(quads[i] == null)
					break;
				size+=quads[i].getSize();
			}
			return size;
		}
		
		public void remove(GameObject object) {
			objects.removeValue(object, true);
		}
		
		/**
		 * check to see what quad this object belongs to
		 * @param object
		 * @return
		 */
		public int getQuadIndex(GameObject object) {
			int quad_index = -1;
			for (int i = 0; i < quads.length; i++) {
				if(quads[i].getBounds().contains(object.getCollisionBounds())) {
					quad_index = i;
					break;
				}
			}
			return quad_index;
		}
		
		public void clear() {
			objects.clear();
			for (int i = 0; i < quads.length; i++) {
				if(quads[i]!=null) {
					quads[i].clear();
					pool.free(quads[i]);
					quads[i] = null;
				}
			}
			
		}
	}

}
