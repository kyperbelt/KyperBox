package com.kyperbox.input;

public class ControllerMapping extends InputMapping{
	
	int type;
	ICWrapper controller;
	public ControllerMapping(ICWrapper controller,int ControllerMaps) {
		this.controller = controller;
		this.type = ControllerMaps;
	}

	@Override
	public float inputValue() {
		switch(type) {
		case ControllerMaps.BUTTON_A:
			if(controller.buttonAPressed())
				return 1f;
			break;
		case ControllerMaps.BUTTON_B:
			if(controller.buttonBPressed())
				return 1f;
			break;
		case ControllerMaps.BUTTON_X:
			if(controller.buttonXPressed())
				return 1f;
			break;
		case ControllerMaps.BUTTON_Y:
			if(controller.buttonYPressed())
				return 1f;
			break;
		case ControllerMaps.DPAD_LEFT:
			if(controller.leftPressed())
				return 1f;
			break;
		case ControllerMaps.DPAD_RIGHT:
			if(controller.rightPressed())
				return 1f;
			break;
		case ControllerMaps.DPAD_UP:
			if(controller.upPressed())
				return 1f;
			break;
		case ControllerMaps.DPAD_DOWN:
			if(controller.downPressed())
				return 1f;
			break;
		case ControllerMaps.LAXIS_LEFT:
			if(controller.getLeftX() < 0)
				return Math.abs(Math.abs(controller.getLeftX()));
			break;
		case ControllerMaps.LAXIS_RIGHT:
			if(controller.getLeftX() > 0)
				return Math.abs(controller.getLeftX());
			break;
		case ControllerMaps.LAXIS_UP:
			if(controller.getLeftY() < 0)
				return Math.abs(controller.getLeftY());
			break;
		case ControllerMaps.LAXIS_DOWN:
			if(controller.getLeftY() > 0) 
				return Math.abs(controller.getLeftY());
			break;
		case ControllerMaps.RAXIS_LEFT:
			if(controller.getRightX() < 0) 
				return Math.abs(controller.getRightX());
			break;
		case ControllerMaps.RAXIS_RIGHT:
			if(controller.getRightX() > 0)
				return Math.abs(controller.getRightX());
			break;
		case ControllerMaps.RAXIS_UP:
			if(controller.getRightY() > 0)
				return Math.abs(controller.getRightY());
			break;
		case ControllerMaps.RAXIS_DOWN:
			if(controller.getRightY() < 0)
				return Math.abs(controller.getRightY());
			break;
		case ControllerMaps.R1:
			if(controller.R1Pressed())
				return 1f;
			break;
		case ControllerMaps.L1:
			if(controller.L1Pressed())
				return 1f;
			break;
		case ControllerMaps.RTRIGGER:
			return controller.getRightTrigger();
		case ControllerMaps.LTRIGGER:
			return controller.getLeftTrigger();
		case ControllerMaps.R3:
			if(controller.R3Pressed())
				return 1f;
			break;
		case ControllerMaps.L3:
			if(controller.L3Pressed())
				return 1f;
			break;
		case ControllerMaps.START:
			if(controller.startPressed())
				return 1f;
			break;
		case ControllerMaps.SELECT:
			if(controller.selectPressed())
				return 1f;
			break;
		}
		return 0;
	}

	@Override
	public boolean sameAs(InputMapping m) {
		if(m instanceof ControllerMapping) {
			ControllerMapping other = (ControllerMapping) m;
			if(this.type == other.type && this.controller == other.controller) {
				return true;
			}
		}
		return false;
	}

}
