package com.kyperbox.input;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector3;
import com.kyperbox.umisc.KyperControllerMaps;

/**
 * a wrapper for Gdx Controller
 * @author john
 *
 */
public class KyperController implements ICWrapper,ControllerListener{
	
	private Controller controller;
	
	//buttons
	private boolean a_justPressed;
	private boolean b_justPressed;
	private boolean x_justPressed;
	private boolean y_justPressed;
	
	//select-start
	private boolean start_justPressed;
	private boolean select_justPressed;
	
	//triggers
	private boolean left_oneJustPressed;
	private boolean right_oneJustPressed;
	private boolean left_threeJustPressed;
	private boolean right_threeJustPressed;

	//directions
	private boolean left_justPressed;
	private boolean right_justPressed;
	private boolean up_justPressed;
	private boolean down_justPressed;
	
	private KyperControllerMaps maps;
	
	public KyperController(Controller controller) {
		this.controller = controller;
		this.controller.addListener(this);
		this.maps = KyperControllerMaps.getMapsForController(this.controller);
	}

	@Override
	public Controller getController() {
		return controller;
	}

	@Override
	public boolean isConnected() {
		return Controllers.getControllers().contains(controller, true);
	}

	@Override
	public float getRightTrigger() {
		return Math.abs(controller.getAxis(maps.getRightTrigger()));
	}

	@Override
	public float getLeftTrigger() {
		return Math.abs(controller.getAxis(maps.getLeftTrigger()));
	}

	@Override
	public float getRightX() {
		return 0;
	}

	@Override
	public float getRightY() {
		return 0;
	}

	@Override
	public float getLeftX() {
		return 0;
	}

	@Override
	public float getLeftY() {
		return 0;
	}

	@Override
	public boolean buttonAPressed() {
		return false;
	}

	@Override
	public boolean buttonBPressed() {
		return false;
	}

	@Override
	public boolean buttonXPressed() {
		return false;
	}

	@Override
	public boolean buttonYPressed() {
		return false;
	}
	
	public boolean leftPressed() {
		return false;
	}
	
	public boolean rightPressed() {
		return false;
	}
	
	public boolean upPressed() {
		return false;
	}
	
	public boolean downPressed() {
		return false;
	}

	@Override
	public boolean triggerLeftOnePressed() {
		return false;
	}

	@Override
	public boolean triggerRightOnePressed() {
		return false;
	}

	@Override
	public boolean triggerLeftThreePressed() {
		return false;
	}

	@Override
	public boolean triggerRightThreePressed() {
		return false;
	}

	@Override
	public boolean selectPressed() {
		return false;
	}

	@Override
	public boolean startPressed() {
		return false;
	}

	@Override
	public boolean buttonAJustPressed() {
		return false;
	}

	@Override
	public boolean buttonBJustPressed() {
		return false;
	}

	@Override
	public boolean buttonXJustPressed() {
		return false;
	}

	@Override
	public boolean buttonYJustPressed() {
		return false;
	}
	
	public boolean leftJustPressed() {
		return false;
	}
	
	public boolean rightJustPressed() {
		return false;
	}
	
	public boolean upJustPressed() {
		return false;
	}
	
	public boolean downJustPressed() {
		return false;
	}

	@Override
	public boolean triggerLeftOneJustPressed() {
		return false;
	}

	@Override
	public boolean triggerRightOneJustPressed() {
		return false;
	}

	@Override
	public boolean triggerLeftThreeJustPressed() {
		return false;
	}

	@Override
	public boolean triggerRightThreeJustPressed() {
		return false;
	}

	@Override
	public boolean selectJustPressed() {
		return false;
	}

	@Override
	public boolean startJustPressed() {
		return false;
	}

	
	//LISTENER
	
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
	
}
