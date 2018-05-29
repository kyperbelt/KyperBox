package com.kyperbox.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.kyperbox.controllers.Box2dController;
import com.kyperbox.controllers.LightController;
import com.kyperbox.objects.GameLayer;
import com.kyperbox.objects.GameObject;

import box2dLight.Light;
import box2dLight.RayHandler;

public class Box2dPhysicsSystem extends LayerSystem {

	private final float pixels_per_meter;
	private final float meters_per_pixel;

	private World world;
	private RayHandler lights;

	private int v_iterations = 6;
	private int p_iterations = 2;

	private float step = 1f / 60f;
	private float accum = 0;
	
	private Matrix4 combined;
	
	private Array<GameObject> body_cons;
	private Array<GameObject> light_cons;
	
	public Box2dPhysicsSystem(float pixels_per_meter, float x_gravity, float y_gravity, boolean lights) {
		world = new World(new Vector2(x_gravity, y_gravity), true);
		this.pixels_per_meter = pixels_per_meter;
		this.meters_per_pixel = 1f/pixels_per_meter;
		if (lights) {
			//RayHandler.useDiffuseLight(true);
			this.lights = new RayHandler(world);
			light_cons = new Array<GameObject>();
			
		}
		
		body_cons = new Array<GameObject>();
	}
	
	public void setAmbientLight(float ambient) {
		if(lights!=null) {
			lights.setAmbientLight(ambient);
		}
		
	}
	
	public void setBlur(boolean blur) {
		if(lights!=null) {
			lights.setBlur(blur);
		}
	}
	
	public Body createBody(BodyDef bdef) {
		return world.createBody(bdef);
	}
	
	public float getPixelsPerMeter() {
		return pixels_per_meter;
	}
	
	public float getMetersPerPixel() {
		return meters_per_pixel;
	}
	
	public float pixelsToMeters() {
		return getMetersPerPixel();
	}
	
	public float metersToPixel() {
		return getPixelsPerMeter();
	}
	
	/**
	 * safely dispose of a body
	 * @param body
	 */
	public void destroyBody(final Body body) {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				world.destroyBody(body);
			}
		});
	}
	
	public void destroyLight(final Light light) {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				light.dispose();
			}
		});
	}

	@Override
	public void init(MapProperties properties) {

	}

	@Override
	public void gameObjectAdded(GameObject object, GameObject parent) {
		Box2dController b2dc = object.getController(Box2dController.class);
		LightController lc = object.getController(LightController.class);
		
		if(b2dc != null) {
			body_cons.add(object);
		}
		
		if(lc!=null) {
			light_cons.add(object);
		}
	}

	@Override
	public void gameObjectChanged(GameObject object, int type, float value) {
		Box2dController b2dc = object.getController(Box2dController.class);
		LightController lc = object.getController(LightController.class);
		
		if(b2dc == null) {
			body_cons.removeValue(object,true);
		}
		
		if(lc == null) {
			light_cons.removeValue(object,true);
		}
	}

	@Override
	public void gameObjectRemoved(GameObject object, GameObject parent) {
		body_cons.removeValue(object,true);
		light_cons.removeValue(object,true);
	}

	@Override
	public void update(float delta) {
		float frame_time = Math.min(delta, 0.25f);
		accum += frame_time;
		while (accum >= step) {
			world.step(step, v_iterations, p_iterations);
			accum -= step;
		}
	}

	@Override
	public void drawDebug(ShapeRenderer shapes) {

	}

	@SuppressWarnings("deprecation")
	@Override
	public void postDraw(Batch batch, float parentAlpha) {
		batch.end();
		
		GameLayer layer = getLayer();
		if(combined == null) {
			combined = getLayer().getState().getGame().getView().getCamera().combined.cpy().scl(pixels_per_meter);
		}else {
			combined.set(getLayer().getState().getGame().getView().getCamera().combined);
			combined.translate(layer.getX(), layer.getY(), 0);
			combined.scl(pixels_per_meter);
		}
		if (lights != null) {
			lights.setCombinedMatrix(combined);
			lights.updateAndRender();
		}

		batch.begin();
	}

	public boolean hasLights() {
		return lights != null;
	}
	
	public RayHandler getLights() {
		return lights;
	}

	@Override
	public void onRemove() {
		if (lights != null)
			lights.dispose();
		if (world != null)
			world.dispose();
		body_cons.clear();
		light_cons.clear();
		world = null;
		lights = null;
	}

}
