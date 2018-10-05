package com.kyperbox.ztests;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.umisc.StringUtils;

public class ControllerTest extends KyperBoxGame{

	public static final String S1 = "%s: yslider code=%s bool=%s";
	public static final String S2 = "%s: xslider code=%s bool=%s";
	public static final String S3 = "%s: pov code=%s Dir=%s";
	public static final String S4 = "%s: button-up code=%s";
	public static final String S5 = "%s: button-down code=%s";
	public static final String S6 = "%s: axis code=%s value=%s";
	
	ControllerListener listener = new ControllerListener() {
		
		@Override
		public boolean ySliderMoved(Controller controller, int sliderCode, boolean value) {
			System.out.println(StringUtils.format(S1, controller.getName(),sliderCode,value));
			return false;
		}
		
		@Override
		public boolean xSliderMoved(Controller controller, int sliderCode, boolean value) {
			System.out.println(StringUtils.format(S2, controller.getName(),sliderCode,value));
			return false;
		}
		
		@Override
		public boolean povMoved(Controller controller, int povCode, PovDirection value) {
			System.out.println(StringUtils.format(S3, controller.getName(),povCode,value));
			return false;
		}
		
		@Override
		public void disconnected(Controller controller) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void connected(Controller controller) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public boolean buttonUp(Controller controller, int buttonCode) {
			System.out.println(StringUtils.format(S4, controller.getName(),buttonCode));
			return false;
		}
		
		@Override
		public boolean buttonDown(Controller controller, int buttonCode) {
			System.out.println(StringUtils.format(S5, controller.getName(),buttonCode));
			return false;
		}
		
		@Override
		public boolean axisMoved(Controller controller, int axisCode, float value) {
			System.out.println(StringUtils.format(S6, controller.getName(),axisCode,value));
			return false;
		}
		
		@Override
		public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
			// TODO Auto-generated method stub
			return false;
		}
	};
	
	@Override
	public void initiate() {
		Array<Controller> controllers = Controllers.getControllers();
		Controller first = controllers.size!=0?controllers.get(0):null;
		if(first!=null) {
			Controllers.addListener(listener);
			System.out.println(StringUtils.format("%s is connected", first.getName()));
		}
	}

	
	
	
	
}
