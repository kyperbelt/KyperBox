package com.kyperbox.controllers;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.kyperbox.objects.GameObject;
import com.kyperbox.objects.GameObjectController;
import com.kyperbox.systems.Box2dPhysicsSystem;

/**
 * this is a box2d controller which holds a body as an object driver
 * or as just a lighting object
 * @author john
 *
 */
public class Box2dController extends GameObjectController {

	Box2dPhysicsSystem world;
	Body body;

	Rectangle bounds;

	private boolean driving;
	private BodyDef bdef;
	private FixtureDef fdef;

	private boolean created = false;

	/**
	 * create a box2d controller that creates a body using the objects bounding
	 * collison rectangle and group/filter
	 * 
	 * @param driving
	 */
	public Box2dController(BodyDef bdef, FixtureDef fdef, boolean driving) {
		this.driving = driving;
		this.bdef = bdef;
		this.fdef = fdef;
	}

	public Box2dController(BodyDef bdef, boolean driving) {
		this(bdef,new FixtureDef() {
			{
			density = .5f;
			friction = .5f;
			restitution = .5f;
			isSensor = false;
			}
		},driving);
	}
	
	public Box2dController(final BodyType t,boolean driving) {
		
		this(new BodyDef() {{this.type = t;}},driving);
		
	}
	
	public Box2dController(boolean driving) {
		this(BodyType.KinematicBody,driving);
	}

	@Override
	public void init(GameObject object) {
		getWorld(object);
	}

	@Override
	public void update(GameObject object, float delta) {
		getWorld(object);
		createBody(object);
		
		if(body!=null) {
			if(!driving) {
				body.setTransform(object.getTruePosition().scl(world.pixelsToMeters()), object.getTrueRotation() * MathUtils.degreesToRadians);
			}else {
				object.setPosition(body.getPosition().x * world.metersToPixel(), body.getPosition().y * world.metersToPixel());
				//body.setLinearVelocity(object.getVelocity().scl(world.pixelsToMeters()));
				object.setVelocity(0, 0);
			}
		}
	}

	public void disposeWorld() {
		world = null;
		created = false;
	}

	/**
	 * set the body of this controller - body will not be auto created
	 * 
	 * @param body
	 */
	public void setBody(Body body) {
		if (this.body != null)
			world.destroyBody(this.body);
		this.body = body;
		created = true;
	}

	@Override
	public void remove(GameObject object) {
		disposeWorld();
	}

	private void getWorld(GameObject object) {
		if (world != null)
			return;
		world = object.getGameLayer().getSystem(Box2dPhysicsSystem.class);
	}

	private void createBody(GameObject object) {
		if (world == null || created)
			return;
		float[] verts = new float[object.getCollisionPolygon().getVertices().length];
		for (int i = 0; i < verts.length; i++) {
			verts[i] = object.getCollisionPolygon().getVertices()[i] * world.pixelsToMeters();
		}

		if (body != null) {
			body = world.createBody(bdef);
			body.setTransform(object.getTruePosition().scl(world.pixelsToMeters()), object.getTrueRotation() * MathUtils.degreesToRadians);
		}

		PolygonShape s = new PolygonShape();
		s.set(verts);

		fdef.shape = s;
		fdef.filter.categoryBits = (short) object.getGroup();
		fdef.filter.maskBits = (short) object.getFilter();

		body.createFixture(fdef);

		s.dispose();

		created = true;
	}

}
