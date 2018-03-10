package com.kyperbox.controllers;

import com.badlogic.gdx.ai.fma.FormationMember;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.kyperbox.objects.GameObject;
import com.kyperbox.umisc.KyperBoxLocation;

public class AiController extends GameObjectController implements Steerable<Vector2>, FormationMember<Vector2> {

	private static final SteeringAcceleration<Vector2> produced_steering = new SteeringAcceleration<Vector2>(
			new Vector2());

	private GameObject object;

	//values

	private Vector2 position;
	private float angular_velocity;
	private float bounding_radius;
	private boolean tagged;

	private float max_linear_speed;
	private float max_linear_accel;
	private float max_angular_speed;
	private float max_angular_accel;

	boolean independent_facing;

	private SteeringBehavior<Vector2> sb;

	public AiController(GameObject object) {
		this(false,object);
	}

	public AiController(boolean independent_facing,GameObject object) {
		this.tagged = false;
		this.object = object;
		this.independent_facing = independent_facing;
		this.position = new Vector2();
		this.bounding_radius = 0;

	}

	public SteeringBehavior<Vector2> getSteeringBehaviour() {
		return sb;
	}

	public void setSteeringBehaviour(SteeringBehavior<Vector2> sb) {
		this.sb = sb;
	}

	@Override
	public Vector2 getPosition() {
		if (object != null)
			position.set(object.getX(), object.getY());
		return position;
	}

	@Override
	public float getOrientation() {
		return object.getRotation() * MathUtils.degRad;
	}

	@Override
	public void setOrientation(float orientation) {
		object.setRotation(orientation * MathUtils.radDeg);
	}

	@Override
	public float vectorToAngle(Vector2 vector) {
		return MathUtils.atan2(-vector.x, vector.y);
	}

	@Override
	public Vector2 angleToVector(Vector2 outVector, float angle) {
		outVector.x = -MathUtils.sin(angle);
		outVector.y = MathUtils.cos(angle);
		return outVector;
	}

	@Override
	public Location<Vector2> newLocation() {
		return new KyperBoxLocation();
	}

	@Override
	public float getZeroLinearSpeedThreshold() {
		return 0.001f;
	}

	@Override
	public void setZeroLinearSpeedThreshold(float value) {
		throw new UnsupportedOperationException("culprit:setZeroLinearSpeedThreshold(float value)");
	}

	@Override
	public float getMaxLinearSpeed() {
		return max_linear_speed;
	}

	@Override
	public void setMaxLinearSpeed(float maxLinearSpeed) {
		this.max_linear_speed = maxLinearSpeed;
	}

	@Override
	public float getMaxLinearAcceleration() {
		return max_linear_accel;
	}

	@Override
	public void setMaxLinearAcceleration(float maxLinearAcceleration) {
		this.max_linear_accel = maxLinearAcceleration;
	}

	@Override
	public float getMaxAngularSpeed() {
		return max_angular_speed;
	}

	@Override
	public void setMaxAngularSpeed(float maxAngularSpeed) {
		this.max_angular_speed = maxAngularSpeed;
	}

	@Override
	public float getMaxAngularAcceleration() {
		return max_angular_accel;
	}

	@Override
	public void setMaxAngularAcceleration(float maxAngularAcceleration) {
		this.max_angular_accel = maxAngularAcceleration;
	}

	@Override
	public Vector2 getLinearVelocity() {
		return object.getVelocity();
	}

	@Override
	public float getAngularVelocity() {
		return angular_velocity;
	}

	@Override
	public float getBoundingRadius() {
		return bounding_radius;
	}

	@Override
	public boolean isTagged() {
		return tagged;
	}

	@Override
	public void setTagged(boolean tagged) {
		this.tagged = tagged;
	}

	@Override
	public void init(GameObject object) {
		this.object = object;
		this.bounding_radius = (object.getWidth() + object.getHeight()) / 4f;
		this.target = new KyperBoxLocation();
	}

	@Override
	public void update(GameObject object, float delta) {
		this.object = object;

		if (sb != null) {
			//calculate steering 
			sb.calculateSteering(produced_steering);

			//add other controls here ----
			//-----
			//-----

			//apply steering accel
			applySteering(produced_steering, delta);
		}

	}

	private void applySteering(SteeringAcceleration<Vector2> steering, float delta) {
		//getPosition().mulAdd(object.getVelocity(), delta);
		object.getVelocity().mulAdd(steering.linear, delta).limit(getMaxLinearSpeed());

		if (independent_facing) {
			object.setRotation(object.getRotation() + (angular_velocity * delta) * MathUtils.radDeg);
			angular_velocity += steering.angular * delta;
		} else {
			if (!getLinearVelocity().isZero(getZeroLinearSpeedThreshold())) {
				float new_rotation = vectorToAngle(object.getVelocity());
				angular_velocity = (new_rotation - object.getRotation() * MathUtils.degRad) * delta;
				object.setRotation(new_rotation * MathUtils.radDeg);
			}
		}
	}

	@Override
	public void remove(GameObject object) {
		object = null;
	}

	public KyperBoxLocation target;

	@Override
	public Location<Vector2> getTargetLocation() {
		return target;
	}

}
