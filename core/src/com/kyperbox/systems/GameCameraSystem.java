package com.kyperbox.systems;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kyperbox.controllers.PoiController;
import com.kyperbox.objects.GameLayer.LayerCamera;
import com.kyperbox.objects.GameObject;
import com.kyperbox.objects.GameObject.GameObjectChangeType;

public class GameCameraSystem extends LayerSystem {
	
	//TODO: create a distance equation so that instead of 10% of distance you reach 
	//		Math.max(.01f,1f-( distance/threshold )) * distance_to_move;

	// dynamic vars
	// private boolean dynamic_zoom;

	// movement vars
	private float x_speed;
	private float y_speed;

	// points of focus
	private Array<GameObject> points_of_focus;

	// point of interest
	private Array<GameObject> poi_objects;
	private Array<PoiController> pois;
	private float poi_threshold;

	private float top;
	private float bot;
	private float left;
	private float right;

	private Vector2 focus_midpoint;
	private Vector2 obj_check;
	private Vector2 poi_midpoint;

	private float feather_duration;
	private float feather_elapsed;
	private boolean feathering;

	private float highest_weight;

	// shake vars
	private float shake_strength;
	private float shake_duration;
	private float shake_elapsed;
	private Vector2 prev_pos;
	
	//if axis locked it will not follow in that axis
	private boolean lock_y;
	private boolean lock_x;

	public GameCameraSystem(float speed) {
		setSpeed(speed);
		// dynamic_zoom = false;
		this.poi_objects = new Array<GameObject>();
		this.pois = new Array<PoiController>();
		this.points_of_focus = new Array<GameObject>();
		this.feather_duration = 0f;
		this.feather_elapsed = 0f;
		this.feathering = false;
		this.highest_weight = 0;
		
		lock_x = false;
		lock_y = false;

		shake_strength = 0;
		shake_duration = 0;
		shake_elapsed = 0;
		prev_pos = new Vector2();

		top = 0;
		bot = 0;
		left = 0;
		right = 0;

		focus_midpoint = new Vector2();
		obj_check = new Vector2();
		poi_midpoint = new Vector2();
	}
	
	public void lockX() {
		this.lock_x = true;
	}
	
	public void lockY() {
		this.lock_y = true;
	}
	
	public boolean isXLocked() {
		return lock_x;
	}
	
	public boolean isYLocked() {
		return lock_y;
	} 
	
	public void unlockX() {
		this.lock_x = false;
	}
	
	public void unlockY() {
		this.lock_y = false;
	}

	public void freshShake(float strength, float duration) {
		this.shake_elapsed = 0;
		this.shake_duration = duration;
		this.shake_strength = strength;
		Vector2 pos = getCam().getPosition();
		this.prev_pos.set(pos.x, pos.y);
	}
	
	public void stopShake() {
		this.shake_duration = 0;
		this.shake_duration = 0;
		this.shake_strength = 0;
		this.prev_pos.set(getCam().getPosition());
	}
	
	public void addShake(float strength,float duration) {
		
		this.shake_duration+=duration;
		this.shake_strength+=strength;
		if(Float.isNaN(prev_pos.x)) {
			Vector2 pos = getCam().getPosition();
			this.prev_pos.set(pos.x,pos.y);
		}
	}

	public boolean isFeathered() {
		return feather_duration > 0f;
	}

	/**
	 * set feathering duration to prevent jarring snaps to camera. this will slow
	 * the camera speed for a set duration when making point of focus/interest
	 * transitions.
	 * 
	 * @param feather_duration
	 *            0 = feathering disabled.
	 */
	public void setFeatherDuration(float feather_duration) {
		this.feather_duration = feather_duration;
	}

	/**
	 * add an object of focus. The camera will try to adjust to keep all objects of
	 * focus centered.
	 * 
	 * @param focus
	 */
	public void addFocus(GameObject focus) {
		if (points_of_focus.contains(focus, true))
			return;
		points_of_focus.add(focus);
	}

	public boolean removeFocus(GameObject focus) {
		return points_of_focus.removeValue(focus, true);
	}

	/**
	 * asymptotic speed .01f - 100f -- timescaled
	 * 
	 * @param speed
	 */
	public void setSpeed(float speed) {
		setXSpeed(speed);
		setYSpeed(speed);
	}

	public void setXSpeed(float x_speed) {
		this.x_speed = MathUtils.clamp(x_speed, .01f, 100f);
	}

	public void setYSpeed(float y_speed) {
		this.y_speed = MathUtils.clamp(y_speed, .01f, 100f);
	}

	public float getXSpeed() {
		return x_speed;
	}

	public float getYSpeed() {
		return y_speed;
	}

	public float getPointOfInterestThreshold() {
		return poi_threshold;
	}

	/**
	 * set the distance at which to consider activating points of interests.
	 * 
	 * @param poi_threshold
	 */
	public void setPointOfInterestThreshold(float poi_threshold) {
		this.poi_threshold = poi_threshold;
	}

	@Override
	public void init(MapProperties properties) {
		poi_threshold = getCam().getViewBounds().getWidth() * .5f;
	}

	@Override
	public void gameObjectAdded(GameObject object, GameObject parent) {
		PoiController poic = object.getController(PoiController.class);
		if (poic != null && !poi_objects.contains(object, true)) {
			poi_objects.add(object);
			pois.add(poic);
		}
	}

	@Override
	public void gameObjectChanged(GameObject object, int type, float value) {
		if (type == GameObjectChangeType.MANAGER) {
			if (value > 0) {// manager added
				PoiController poic = object.getController(PoiController.class);
				if (poic != null && !poi_objects.contains(object, true)) {
					poi_objects.add(object);
					pois.add(poic);
				}
			} else { // manager removed
				for (int i = 0; i < poi_objects.size; i++) {
					if (poi_objects.get(i) == object) {
						poi_objects.removeIndex(i);
						pois.removeIndex(i);
						break;
					}
				}
			}
		}

	}

	@Override
	public void gameObjectRemoved(GameObject object, GameObject parent) {
		for (int i = 0; i < poi_objects.size; i++) {
			if (poi_objects.get(i) == object) {
				poi_objects.removeIndex(i);
				pois.removeIndex(i);
				break;
			}
		}
	}

	protected LayerCamera getCam() {
		return getLayer().getCamera();
	}
	
	public boolean isShaking() {
		return shake_duration > 0;
	}

	@Override
	public void update(float delta) {
		Vector2 cam_pos = getCam().getPosition();
		cam_pos.x = MathUtils.floor(cam_pos.x);
		cam_pos.y = MathUtils.floor(cam_pos.y);
		
		Rectangle view = getCam().getViewBounds();

		// adjust bounds to point of focus.
		updateFocus(cam_pos,view);

		// adjust midpoint to include prominent point of interest
		if (updatePois(getCam())) {
			focus_midpoint.lerp(poi_midpoint, Math.max(.1f, highest_weight*.5f));
		}

		//interpolate to cam position
		float newxspeed = /*x_speed*delta;*/Math.min(x_speed*delta,1f);
		float newyspeed = Math.min(y_speed*delta,1f);
		obj_check.set(cam_pos.x + (focus_midpoint.x - cam_pos.x) * newxspeed,
				cam_pos.y + (focus_midpoint.y - cam_pos.y) * newyspeed).scl(getCam().getZoom());
		

		if (!calculateShake(delta)) {
			if (feathering) { //TODO: feathering is not used.. remove or find use
				feather_elapsed += delta;
				getCam().setPosition(cam_pos.x + (((focus_midpoint.x - cam_pos.x) * (x_speed * .5f)) * delta),
						cam_pos.y + ((focus_midpoint.y - cam_pos.y) * (y_speed * .5f)) * delta);
				if (feather_elapsed >= feather_duration) {
					feather_elapsed = 0f;
					feathering = false;
				}
			} else {
				getCam().setPosition(MathUtils.floor(obj_check.x),MathUtils.floor(obj_check.y));
				
			}
		}
		

	}

	private boolean calculateShake(float delta) {
		if (shake_duration > 0) {
			LayerCamera cam = getCam();
			float current_strength = (shake_strength / cam.getZoom())
					* ((shake_duration - shake_elapsed) / shake_duration);
			float x = (MathUtils.random() - .5f) * 2 * current_strength;
			float y = (MathUtils.random() - .5f) * 2 * current_strength;

			cam.setPosition(obj_check.x-x,obj_check.y-y);
			if (shake_elapsed >= shake_duration) {
				shake_duration = 0;
				shake_strength = 0;
				shake_elapsed = 0;
				obj_check.set(prev_pos.x, prev_pos.y);
				prev_pos.set(Float.NaN, Float.NaN);
			}
			
			shake_elapsed += delta;
			return true;
		}
		return false;
	}

	@Override
	public void onRemove() {
		getCam().setPosition(0, 0);
	}

	private void updateFocus(Vector2 cam_pos, Rectangle view) {
		focus_midpoint.set(cam_pos.x, cam_pos.y);

		if (points_of_focus.size > 0) {
			top = Float.NaN;
			bot = Float.NaN;
			left = Float.NaN;
			right = Float.NaN;

			for (int i = 0; i < points_of_focus.size; i++) {
				GameObject focus = points_of_focus.get(i);
				float focus_top = focus.getY()+focus.getHeight();
				float focus_right = focus.getX()+focus.getWidth();
				float focus_left = focus.getX();
				float focus_bot = focus.getY();

				if (Float.isNaN(top) || top < focus_top)
					top = focus_top;
				if (Float.isNaN(bot) || bot > focus_bot)
					bot = focus_bot;
				if (Float.isNaN(right) || right < focus_right)
					right = focus_right;
				if (Float.isNaN(left) || left > focus_left)
					left = focus_left;
			}

			if (top > bot + view.height)
				top = bot + view.height;
			if (right > left + view.width)
				right = left + view.width;

			focus_midpoint.set(isXLocked()?focus_midpoint.x:left + (right - left) * .5f, isYLocked()?focus_midpoint.y:bot + (top - bot) * .5f);
			focus_midpoint.x = MathUtils.floor(focus_midpoint.x);
			focus_midpoint.y = MathUtils.floor(focus_midpoint.y);
		}

	}

	/**
	 * adjust all points of focus and set the midpoint to the most relevant point
	 * 
	 * @param cam
	 * @return false if no points found
	 */
	private boolean updatePois(LayerCamera cam) {
		highest_weight = 0;
		boolean available = false;
		float last_weight = 1;
		poi_midpoint.set(0, 0);

		for (int i = 0; i < pois.size; i++) {
			PoiController poic = pois.get(i);
			GameObject object = poi_objects.get(i);

			obj_check.set(object.getX(), object.getY());

			// check to see if the object is on screen and is near threshold
			if (obj_check.dst(focus_midpoint) <= poi_threshold / getCam().getZoom()) {

				// check to see if it can be activated
				if (!poic.isInFrame() && poic.canActivate()) {
					poic.setActive(true);
					poic.incrementActiveCount();
					poic.setInFrame(true);
				}

				// check to see if should deactivate
				if (poic.shouldDeactivate()) {
					poic.setActive(false);
				}

				if (poic.isActive()) {
					// point of interest is viable-- calculate
					if (!available) {
						// first available poi -- set
						poi_midpoint.set(obj_check.x, obj_check.y);
						available = true;

					} else {
						// calculate midpoint vs last available poi using weights
						poi_midpoint.lerp(obj_check,
								MathUtils.clamp(((poic.getWeight() + last_weight) * .5f - .5f), 0f, 1f));
					}
					if (highest_weight < poic.getWeight())
						highest_weight = poic.getWeight();
					last_weight = poic.getWeight();
				}

			} else {
				// not in view so just deactivate and set not in view
				poic.setActive(false);
				poic.setInFrame(false);
			}

		}

		return available;
	}

	// =========================================================
	// $$$$$$$\ $$$$$$\ $$$$$$\ $$$$$$\ $$$$$$$$\ $$\ $$\ $$$$$$$$\ $$$$$$$$\
	// $$ __$$\ $$ __$$\ \_$$ _| $$ __$$\\__$$ __|$$ | $$ |$$ _____|$$ _____|
	// $$ | $$ |$$ / $$ | $$ | $$ / \__| $$ | $$ | $$ |$$ | $$ |
	// $$$$$$$ |$$ | $$ | $$ | \$$$$$$\ $$ | $$ | $$ |$$$$$\ $$$$$\
	// $$ ____/ $$ | $$ | $$ | \____$$\ $$ | $$ | $$ |$$ __| $$ __|
	// $$ | $$ | $$ | $$ | $$\ $$ | $$ | $$ | $$ |$$ | $$ |
	// $$ | $$$$$$ |$$$$$$\ \$$$$$$ | $$ | \$$$$$$ |$$ | $$ |
	// \__| \______/ \______| \______/ \__| \______/ \__| \__|
	// ==========================================================

	//// TODO: MOVED TO POICONTROLLER === remove;
	// public static class PointOfInterest implements Comparable<PointOfInterest>{
	//
	// public static final String FOLLOW = "follow";
	//
	// private String name;
	// private GameObject object;
	// private float weight;
	//
	// public PointOfInterest(String name,GameObject object,float weight) {
	// this.object = object;
	// setWeight(weight);
	// this.name = name;
	// }
	//
	// public PointOfInterest(String name,GameObject object) {
	// this(name,object,1f);
	// }
	//
	// public PointOfInterest(String name) {
	// this(name, null);
	// }
	//
	// public void init(String name,GameObject object,float weight) {
	// this.object = object;
	// setWeight(weight);
	// this.name = name;
	// }
	//
	// public void setWeight(float weight) {
	// this.weight = MathUtils.clamp(weight, 0f, 1f);
	// }
	//
	// public void setObject(GameObject object) {
	// this.object = object;
	// }
	//
	// public void setName(String name) {
	// this.name = name;
	// }
	//
	// public GameObject getObject() {
	// return object;
	// }
	//
	// public String getName() {
	// return name;
	// }
	//
	// public float getWeight() {
	// return weight;
	// }
	//
	// @Override
	// public int compareTo(PointOfInterest o) {
	// if(o.weight > weight)
	// return -1;
	// if(o.weight < weight)
	// return 1;
	// return 0;
	// }
	//
	// }

}
