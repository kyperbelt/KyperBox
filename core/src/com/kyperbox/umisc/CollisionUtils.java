package com.kyperbox.umisc;

import com.badlogic.gdx.math.Rectangle;
import com.kyperbox.objects.BasicGameObject;
import com.kyperbox.objects.GameObject;

public class CollisionUtils {

	private static GameObject ztest;

	/**
	 * checks if the objects have colliding depth+thickess
	 * 
	 * @param o1
	 * @param o2
	 * @return
	 */
	public static boolean depthCollision(GameObject o1, GameObject o2) {
		if (o1.getDepth() + o1.getThickness() < o2.getDepth())
			return false;
		if (o1.getDepth() > o2.getDepth() + o2.getThickness())
			return false;
		return true;

	}

	/**
	 * checks if the objects have colliding collision bounds
	 * 
	 * @param o1
	 * @param o2
	 * @return
	 */
	public static boolean boundsCollision(GameObject o1, GameObject o2) {
		return o1.getCollisionBounds().overlaps(o2.getCollisionBounds());
	}

	/**
	 * checks if the objects collide in 3d space using depth+thickness as the third
	 * dimension and the collision bounds as the x/y
	 * 
	 * @return
	 */
	public static boolean boxCollision(GameObject o1, GameObject o2) {
		return boundsCollision(o1, o2) && depthCollision(o1, o2);
	}

	private static GameObject getZTest() {
		if (ztest == null) {
			ztest = new BasicGameObject();
		}
		return ztest;
	}

	/**
	 * get a game object to test with. There is only ever one instance - so you must
	 * use it before creating another one. Do not keep a reference to this object
	 * or it may cause bugs
	 * 
	 * @param bounds
	 * @param name
	 * @param group
	 * @param filter
	 * @return
	 */
	public static GameObject getTestObject(Rectangle bounds, String name, int group, int filter) {
		GameObject z = getZTest();
		z.setPosition(bounds.x, bounds.y);
		z.setSize(bounds.width, bounds.height);
		z.setCollisionBounds(0, 0, bounds.width, bounds.height);
		z.setName(name);
		z.setGroup(group);
		z.setFilter(filter);
		return z;
	}
}
