package com.kyperbox.umisc;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.kyperbox.objects.GameLayer.LayerCamera;
import com.kyperbox.objects.GameObject;

public class BakedEffects {

	// TODO: ADD POOLING

//	/**
//	 * Effect that flashes the player the given color
//	 * 
//	 * @return
//	 */
//	public static Action flash(Color color, float duration) {
//		return sequence();
//	}

	// DOES NOT POOL // needs pooling
	/**
	 * create an action that shakes the game_object
	 * This effect is not compatible with non GameObject actors.
	 * 
	 * @param duration
	 *            - how long should we shake for
	 * @param strength
	 *            - how hard should we shake
	 * @param place_at_start
	 *            - do we place it back to where it was before the shake
	 * @return
	 */
	public static Action shake(final float duration, final float strength, final boolean steady,
			final boolean place_at_start) {
		Action a = null;
		a = new Action() {
			float start_x = Float.NaN;
			float start_y = Float.NaN;
			float elapsed = 0;
			float str = strength;
			LayerCamera cam;

			@Override
			public boolean act(float delta) {
				GameObject o = (GameObject) getActor();
				if (Float.isNaN(start_x)) {
					start_x = o.getX();
					start_y = o.getY();
				}
				if (cam == null)
					cam = o.getGameLayer().getCamera();
				elapsed += delta;
				if (elapsed < duration) {
					if (steady)
						str = strength / cam.getZoom();// * ((duration - elapsed) / duration);
					else
						str = strength / cam.getZoom() * ((duration - elapsed) / duration);
					float x = (MathUtils.random() - .5f) * str;
					float y = (MathUtils.random() - .5f) * str;
					if (place_at_start)
						o.setPosition(start_x + x, start_y + y);
					else
						o.setPosition(o.getX() + x, o.getY() + y);

				} else {
					if (place_at_start)
						o.setPosition(start_x, start_y);
					return true;
				}

				return false;
			}

			@Override
			public void reset() {
				super.reset();
			}

			@Override
			public void restart() {
				elapsed = 0;
				start_x = Float.NaN;
				start_y = Float.NaN;
				cam = null;
			}
		};
		return a;
	}

	/**
	 * create an action that shakes the actor and returns it to where it begun at
	 * the end
	 * 
	 * @param duration
	 * @param strength
	 * @param steady
	 * @return
	 */
	public static Action shake(float duration, float strength, boolean steady) {
		return shake(duration, strength, steady, true);
	}

	/**
	 * creates an action that steadily shakes the actor and returns it to where it
	 * begun at the end
	 * 
	 * @param duration
	 * @param strength
	 * @return
	 */
	public static Action shake(float duration, float strength) {
		return shake(duration, strength, true, true);
	}

	private static Vector2 ov = new Vector2();
	private static Vector2 cv = new Vector2();
	
	/**
	 * create a spiral effect around the center actor.. if no actor is provided the
	 * original position of the object that has this effect applied will be used.
	 * The actor cannot move while it is spinning but if it is parented it should
	 * move fine.
	 * 
	 * @param duration
	 *            - how long the spiral
	 * @param rotations
	 *            - how many times should you rotate.. cannot be 0  or no movement
	 * @param strength
	 *            - how far away from center should the spiral get to.
	 * @param spiral_center
	 *            - the center of the spiral
	 * @param state - how should strength diminish (-1 = out->in 0 = nodiminish 1 = in->out) 
	 * @param revert_state
	 *            - revert back to original pos: use this when not repeating if
	 *            desired
	 * @return
	 */
	public static Action spiral(final float duration,final int rotations, final float strength,
			final Actor spiral_center, final int state,final boolean revert_state,final boolean keep_angle) {
		Action a = null;
		a = new Action() {

			float start_x = Float.NaN;
			float start_y = Float.NaN;
			float start_angle = Float.NaN;
			float rotation = 360*rotations;
			float current_rotation = 360*duration;
			
			Actor center = spiral_center;
			float elapsed = 0;

			@Override
			public boolean act(float delta) {
				Actor object = getActor();

				if (Float.isNaN(start_x)) {
					start_x = object.getX();
					start_y = object.getY();
					start_angle = object.getRotation();
				}

				elapsed += delta;

				if (elapsed < duration) {
					
					float t = (elapsed/duration);
					current_rotation = ((start_angle+rotation)*t);
					
					float cx = center == null?start_x:center.getX();
					float cy = center == null?start_y:center.getY();
					
					float ox = cx+(state == 0?strength:state == -1?strength*(1f-t):strength*t);
					float oy = cy+(state == 0?strength:state == -1?strength*(1f-t):strength*t);
					
					float rad_angle = current_rotation * MathUtils.degRad;
					float x = MathUtils.cos(rad_angle) * (ox - cx)
							- MathUtils.sin(rad_angle) * (oy - cy) + cx;
					float y = MathUtils.sin(rad_angle) * (ox - cx)
							+ MathUtils.cos(rad_angle) * (oy - cy) + cy;
					object.setPosition(x, y);
					ov.set(object.getX(),object.getY());
					cv.set(cx, cy);
					if(!keep_angle)
					object.setRotation((rad_angle*MathUtils.radDeg)-180);
				} else {
					if(revert_state) {
						object.setPosition(start_x, start_y);
						object.setRotation(start_angle);
					}
					return true;
				}

				return false;
			}

			@Override
			public void restart() {
				if(revert_state) {
					start_x = Float.NaN;
					start_y = Float.NaN;
					start_angle = Float.NaN;
				}
				
				elapsed = 0;
				super.restart();
			}
		};
		

		return a;
	}
	
	public static Action spiral(float duration,int rotations,float strength,Actor spiral_center,int state) {
		return spiral(duration, rotations, strength, spiral_center,state, false,false);
	}
	
	public static Action spiral(float duration,int rotations,float strength,int state) {
		return spiral(duration, rotations, strength, null,state);
	}
	
	public static Action spiral(float duration,int rotations,float strength) {
		return spiral(duration, rotations, strength, 0);
	}
	
	/**
	 * creates a spiral effect action with a default rotations of 2
	 * @param duration
	 * @param strength
	 * @return
	 */
	public static Action spiral(float duration,float strength) {
		return spiral(duration, 2, strength);
	}
	
	public static Action spiral(float duration,float strength,Actor center) {
		return spiral(duration, 2, strength, center, 0);
	}
	
	public static Action spiral(float duration,float strength,Actor center,boolean keep_angle) {
		return spiral(duration, 2, strength, center, 0, false, keep_angle);
	}
	
	public static Action spiral(float duration,float strength,boolean keep_angle) {
		return spiral(duration, strength,null,keep_angle);
	}

	public static Action pulse(final Actor actor,final float duration,final float scale_pulse,final float angle_pulse) {
		Action a = null;
		float original_scale_x = actor.getScaleX();
		float original_scale_y = actor.getScaleY();
		float original_angle = actor.getRotation();
		float pulse = scale_pulse*.5f;
		float angle = angle_pulse*.5f;
		float set_dur = duration*.25f;
		a = sequence(
					parallel(
							 scaleTo(original_scale_x+(original_scale_x*pulse), original_scale_y+(original_scale_y*pulse), set_dur),
							 rotateTo(original_angle+angle, set_dur)
							),
					parallel(
							scaleTo(original_scale_x, original_scale_y, set_dur),
							 rotateTo(original_angle, set_dur)
							),
					parallel(
							 scaleTo(original_scale_x+(original_scale_x*pulse), original_scale_y+(original_scale_y*pulse), set_dur),
							 rotateTo(original_angle-angle, set_dur)
							),
					parallel(
							scaleTo(original_scale_x, original_scale_y, set_dur),
							 rotateTo(original_angle, set_dur)
							));
		

		return a;
	}

//	/**
//	 * NOT USABLE
//	 * @param duration
//	 * @return
//	 */
//	public Action flicker(float duration) {
//		Action a = null;
//
//		return a;
//	}
	
	
}
