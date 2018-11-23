package com.kyperbox.input;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.umisc.KyperControllerMapper;

/**
 * a wrapper for Gdx Controller
 * 
 * @author john
 *
 */
public class KyperController implements ICWrapper, ControllerListener {

	private Controller controller;

	private KyperControllerMapper maps;
	// just pressed checks
	private boolean up;
	private boolean down;
	private boolean left;
	private boolean right;
	private boolean a;
	private boolean b;
	private boolean x;
	private boolean y;
	private boolean r1;
	private boolean select;
	private boolean start;
	private boolean r2;
	private boolean r3;
	private boolean l1;
	private boolean l2;
	private boolean l3;

	public KyperController(Controller controller,KyperControllerMapper maps) {
		this.controller = controller;
		this.controller.addListener(this);
		this.maps = maps;
		KyperBoxGame.controllers.removeValue(this, true);
		KyperBoxGame.controllers.add(this);
	}
	
	public KyperController(Controller controller) {
		this(controller,KyperControllerMapper.getMapsForController(controller));
	}

	@Override
	public Controller getController() {
		return controller;
	}

	public boolean hasTOriggerAxis() {
		return maps.isTriggersAxis();
	}

	@Override
	public boolean isConnected() {
		return Controllers.getControllers().contains(controller, true);
	}

	@Override
	public float getRightTrigger() {
		return maps.isTriggersAxis()
				? (!maps.isTriggersOnSameAxis() ? MathUtils.clamp(controller.getAxis(maps.getRightTrigger()), 0, 1f)
						: Math.abs(controller.getAxis(maps.getRightTrigger())))
				: (controller.getButton(maps.getRightTrigger()) ? 1 : 0);
	}

	@Override
	public float getLeftTrigger() {
		return maps.isTriggersAxis() 
				? (!maps.isTriggersOnSameAxis() ? MathUtils.clamp(controller.getAxis(maps.getLeftTrigger()),0,1f):
					Math.abs(controller.getAxis(maps.getLeftTrigger())))
				: (controller.getButton(maps.getLeftTrigger()) ? 1 : 0);
	}

	@Override
	public float getRightX() {
		return controller.getAxis(maps.getAnalogRightX());
	}

	@Override
	public float getRightY() {
		return controller.getAxis(maps.getAnalogRightY());
	}

	@Override
	public float getLeftX() {
		return controller.getAxis(maps.getAnalogLeftX());
	}

	@Override
	public float getLeftY() {
		return controller.getAxis(maps.getAnalogLeftY());
	}

	@Override
	public boolean buttonAPressed() {
		return controller.getButton(maps.getButtonA());
	}

	@Override
	public boolean buttonBPressed() {
		return controller.getButton(maps.getButtonB());
	}

	@Override
	public boolean buttonXPressed() {
		return controller.getButton(maps.getButtonX());
	}

	@Override
	public boolean buttonYPressed() {
		return controller.getButton(maps.getButtonY());
	}

	public boolean leftPressed() {
		return controller.getPov(maps.getDpad()) == PovDirection.west;
	}

	public boolean rightPressed() {
		return controller.getPov(maps.getDpad()) == PovDirection.east;
	}

	public boolean upPressed() {
		return controller.getPov(maps.getDpad()) == PovDirection.north;
	}

	public boolean downPressed() {
		return controller.getPov(maps.getDpad()) == PovDirection.south;
	}

	@Override
	public boolean L1Pressed() {
		return controller.getButton(maps.getLeft1());
	}

	@Override
	public boolean R1Pressed() {
		return controller.getButton(maps.getRight1());
	}

	@Override
	public boolean L3Pressed() {
		return controller.getButton(maps.getLeft3());
	}

	@Override
	public boolean R3Pressed() {
		return controller.getButton(maps.getRight3());
	}

	@Override
	public boolean selectPressed() {
		return controller.getButton(maps.getSelect());
	}

	@Override
	public boolean startPressed() {
		return controller.getButton(maps.getStart());
	}

	@Override
	public boolean buttonAJustPressed() {
		if (buttonAPressed() && !a) {
			a = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean buttonBJustPressed() {
		if (buttonBPressed() && !b) {
			b = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean buttonXJustPressed() {
		if (buttonXPressed() && !x) {
			x = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean buttonYJustPressed() {
		if (buttonYPressed() && !y) {
			y = true;
			return true;
		}
		return false;
	}

	public boolean leftJustPressed() {
		if (leftPressed() && !left) {
			left = true;
			return true;
		}
		return false;
	}

	public boolean rightJustPressed() {
		if (rightPressed() && !right) {
			right = true;
			return true;
		}
		return false;
	}

	public boolean upJustPressed() {
		if (upPressed() && !up) {
			up = true;
			return true;
		}
		return false;
	}

	public boolean downJustPressed() {
		if (downPressed() && !down) {
			down = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean L1JustPressed() {
		if (L1Pressed() && !l1) {
			l1 = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean R1JustPressed() {
		if (R1Pressed() && !r1) {
			r1 = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean L3JustPressed() {
		if (L3Pressed() && !l3) {
			l3 = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean R3JustPressed() {
		if (R3Pressed() && !r3) {
			r3 = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean selectJustPressed() {
		if (selectPressed() && !select) {
			select = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean startJustPressed() {
		if (startPressed() && !start) {
			start = true;
			return true;
		}
		return false;
	}

	public void update() {
		start = !startPressed() ? false : start;
		select = !selectPressed() ? false : select;
		a = !buttonAPressed() ? false : a;
		b = !buttonBPressed() ? false : b;
		x = !buttonXPressed() ? false : x;
		y = !buttonYPressed() ? false : y;
		up = !upPressed() ? false : up;
		down = !downPressed() ? false : down;
		left = !leftPressed() ? false : left;
		right = !rightPressed() ? false : right;
		r1 = !R1Pressed() ? false : r1;
		l1 = !L1Pressed() ? false : l1;
		r3 = !R3Pressed() ? false : r3;
		l3 = !L3Pressed() ? false : l3;
	}

	// LISTENER

	@Override
	public void connected(Controller controller) {

	}

	@Override
	public void disconnected(Controller controller) {

	}

	@Override
	public boolean buttonDown(Controller controller, int buttonCode) {
		return false;
	}

	@Override
	public boolean buttonUp(Controller controller, int buttonCode) {
		return false;
	}

	@Override
	public boolean axisMoved(Controller controller, int axisCode, float value) {
		return false;
	}

	@Override
	public boolean povMoved(Controller controller, int povCode, PovDirection value) {
		return false;
	}

	@Override
	public boolean xSliderMoved(Controller controller, int sliderCode, boolean value) {
		return false;
	}

	@Override
	public boolean ySliderMoved(Controller controller, int sliderCode, boolean value) {
		return false;
	}

	@Override
	public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
		return false;
	}

	@Override
	public void remove() {
		KyperBoxGame.controllers.removeValue(this, true);
		
	}

}
