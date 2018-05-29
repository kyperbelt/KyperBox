package com.kyperbox.controllers;

import com.badlogic.gdx.graphics.Color;
import com.kyperbox.objects.GameObject;
import com.kyperbox.systems.Box2dPhysicsSystem;

import box2dLight.ConeLight;
import box2dLight.Light;
import box2dLight.PointLight;

public class LightController extends GameObjectController{
	
	public static enum LightType{
		PointLight,
		ConeLight,
		
	}
	
	private Color color;
	private int rays;
	private float distance;
	private LightType type;
	private float cone_degrees;
	private float direction = 0;
	
	private Box2dPhysicsSystem world;
	private Light light;
	
	public LightController(LightType type,Color color,int rays, float distance,float cone_degrees) {
		this.color = color;
		this.type = type;
		this.rays = rays;
		this.distance = distance;
		this.cone_degrees = cone_degrees;
	}
	
	public LightController(LightType type,Color color, int rays, float distance) {
		this(type, color, rays, distance, 90/2);
	}
	
	public LightController(LightType type,Color color, int rays) {
		this(type, color, rays, 300);
	}

	@Override
	public void init(GameObject object) {
		
		getWorld(object);
		getLight(object);
	}
	
	public void setDirection(float direction) {
		this.direction = direction;
	}
	
	/**
	 * checks if point is inside the light
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean contains(float x,float y) {
		if(light!=null) {
			return light.contains(x*world.pixelsToMeters(), y*world.pixelsToMeters());
		}
		return false;
	}

	@Override
	public void update(GameObject object, float delta) {
		getWorld(object);
		getLight(object);
		if(world!= null && light!=null) {
			float x = (object.getTrueX()+(object.getWidth()*.5f)) * world.pixelsToMeters();
			float y = (object.getTrueY()+(object.getHeight()*.5f)) * world.pixelsToMeters();
			
			light.setPosition(x, y);
			light.setDistance(distance * world.pixelsToMeters());
			light.setDirection(direction + object.getTrueRotation());
			
			if(type == LightType.ConeLight) {
				ConeLight cl = (ConeLight) light;
				cl.setConeDegree(cone_degrees);
			}
			
		}
	}

	@Override
	public void remove(GameObject object) {
		destroyLight();
		light = null;
	}
	
	public void setConeDegree(float degree) {
		cone_degrees = degree;
	}
	
	public LightType getLightType() {
		return type;
	}
	
	/**
	 * destroys the light
	 */
	public void destroyLight() {
		world.destroyLight(light);
	}
	
	public void setStrength(float distance) {
		this.distance = distance;
	}
	
	public void setActive(boolean active) {
		if(light!=null) {
			light.setActive(active);
		}
	}
	
	private void getLight(GameObject object) {
		if(world == null || light != null) {
			return;
		}
		float x = (object.getTrueX()+object.getWidth()*.5f) * world.pixelsToMeters();
		float y = (object.getTrueY()+object.getHeight()*.5f)  * world.pixelsToMeters();
		switch(type) {
		case PointLight: light = new PointLight(world.getLights(), rays, color, distance * world.pixelsToMeters() , 0, 0);
		break;
		case ConeLight: light = new ConeLight(world.getLights(), rays, color, distance * world.pixelsToMeters(), x, y, object.getTrueRotation() , cone_degrees);
		}
		
//		
//		float[] verts = new float[object.getCollisionPolygon().getVertices().length];
//		for (int i = 0; i < verts.length; i++) {
//			verts[i] = object.getCollisionPolygon().getVertices()[i] * world.pixelsToMeters();
//		}
//
//		PolygonShape s = new PolygonShape();
//		s.set(verts);
		
//		System.out.println("x :"+x+" y:"+y);
//		
//		BodyDef bdef = new BodyDef();
//		bdef.type = BodyType.DynamicBody;
//		bdef.allowSleep  = true;
//		
//		body = world.createBody(bdef);
//		body.setTransform(0, 0, object.getTrueRotation()*MathUtils.degRad);
//		
//		System.out.println(body.getPosition());
		
		light.setStaticLight(true);
		//light.attachToBody(body);
	}
	
	private void getWorld(GameObject object) {
		if(world==null) {
			world = object.getGameLayer().getSystem(Box2dPhysicsSystem.class);
		}
	}

}
