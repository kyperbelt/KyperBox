package com.kyperbox.umisc;

import com.badlogic.gdx.controllers.Controller;
import com.kyperbox.KyperBoxGame;

public class KyperControllerMapper {
	
	private int button_a;
	private int button_b;
	private int button_x;
	private int button_y;
	private int dpad;
	private int left_1;
	private int right_1;
	private int select;
	private int start;
	private int left_3;
	private int right_3;
	private int left2_trigger;
	private int right2_trigger;
	private int analog_left_x;
	private int analog_left_y;
	private int analog_right_x;
	private int analog_right_y;
	private int extra1;
	private int extra2;
	private int extra3;
	private int extra4;
	private boolean axis_trigger=true;
	private boolean triggers_same_axis=false;//means that -1
	
	
	//GETTERS
	
	/**
	 * do triggers use axis
	 * @return
	 */
	public boolean isTriggersAxis() {
		return axis_trigger;
	}
	
	/**
	 * do both trigger buttons use the same axis
	 * @return
	 */
	public boolean isTriggersOnSameAxis() {
		return triggers_same_axis;
	}
	
	public int getDpad() {
		return dpad;
	}
	
	public int getButtonA() {
		return button_a;
	}
	
	public int getButtonB() {
		return button_b;
	}
	
	public int getButtonX() {
		return button_x;
	}
	
	public int getButtonY() {
		return button_y;
	}
	
	public int getLeft1() {
		return left_1;
	}
	
	public int getRight1() {
		return right_1;
	}
	
	public int getSelect() {
		return select;
	}
	
	public int getStart() {
		return start;
	}
	
	public int getLeft3() {
		return left_3;
	}
	
	public int getRight3() {
		return right_3;
	}
	
	public int getLeftTrigger() {
		return left2_trigger;
	}
	
	public int getRightTrigger() {
		return right2_trigger;
	}
	
	public int getAnalogLeftX() {
		return analog_left_x;
	}
	
	public int getAnalogLeftY() {
		return analog_left_y;
	}
	
	public int getAnalogRightX() {
		return analog_right_x;
	}
	
	public int getAnalogRightY() {
		return analog_right_y;
	}
	
	public int getExtra1() {
		return extra1;
	}
	
	public int getExtra2() {
		return extra2;
	}
	
	public int getExtra3() {
		return extra3;
	}
	
	public int getExtra4() {
		return extra4;
	}
	
	public static KyperControllerMapper getMapsForController(Controller c) {
		if(XBoxOneWindows.isXboxOneWindowsController(c))
			return XBoxOneWindows.getMappings();
		if(XBoxBluetoothGamepad.isXboxBluetoothGamepad(c))
			return XBoxBluetoothGamepad.getMappings();
		if(BluetoothWirelessController.isBluetoothWirelessController(c))
			return BluetoothWirelessController.getMappings();
		else return null;
	}
	
	
	
	public static class XBoxOneWindows{
		public static final int BUTTON_A = 0;
		public static final int BUTTON_B = 1;
		public static final int BUTTON_X = 2;
		public static final int BUTTON_Y = 3;
		public static final int DPAD = 0;
		public static final int L1 = 4;
		public static final int R1 = 5;
		public static final int SELECT = 6;
		public static final int START = 7;
		public static final int L3 = 8;
		public static final int R3 = 9;
		public static final int L2_TRIGGER = 4; //0 to 1
		public static final int R2_TRIGGER = 4; //0 to -1;
		public static final int ANALOG_LEFT_X = 1; //
		public static final int ANALOG_LEFT_Y = 0;
		public static final int ANALOG_RIGHT_X = 3;
		public static final int ANALOG_RIGHT_Y = 2;
		
		public static boolean isXboxOneWindowsController(Controller controller) {
			return controller.getName().toLowerCase().contains("xbox") &&
					controller.getName().toLowerCase().contains("one") &&
					controller.getName().toLowerCase().contains("windows");
		}
		
		public static KyperControllerMapper getMappings() {
			if(KyperBoxGame.DEBUG_LOGGING) {
				KyperBoxGame.log("ControllerMappings", "Xbox One Controller Mappings");
			}
			KyperControllerMapper mappings = new KyperControllerMapper();
			mappings.button_a = BUTTON_A;
			mappings.button_b = BUTTON_B;
			mappings.button_x = BUTTON_X;
			mappings.button_y = BUTTON_Y;
			mappings.left_1 = L1;
			mappings.right_1 = R1;
			mappings.select = SELECT;
			mappings.start = START;
			mappings.left_3 = L3;
			mappings.right_3 = R3;
			mappings.left2_trigger = L2_TRIGGER;
			mappings.right2_trigger = R2_TRIGGER;
			mappings.analog_left_x = ANALOG_LEFT_X;
			mappings.analog_left_y = ANALOG_LEFT_Y;
			mappings.analog_right_x = ANALOG_RIGHT_X;
			mappings.analog_right_y = ANALOG_RIGHT_Y;
			mappings.dpad = DPAD;
			mappings.axis_trigger = true;
			mappings.triggers_same_axis = true;
			return mappings;
		}
		
	}
	
	public static class XBoxBluetoothGamepad{
		public static final int BUTTON_A = 0;
		public static final int BUTTON_B = 1;
		public static final int BUTTON_X = 2;
		public static final int BUTTON_Y = 3;
		public static final int DPAD = 0;
		public static final int L1 = 4;
		public static final int R1 = 5;
		public static final int SELECT = 6;
		public static final int START = 7;
		public static final int L3 = 8;
		public static final int R3 = 9;
		public static final int L2_TRIGGER = 4; //-1 to 1
		public static final int R2_TRIGGER = 5; //-1 to -1;
		public static final int ANALOG_LEFT_X = 1; //
		public static final int ANALOG_LEFT_Y = 0;
		public static final int ANALOG_RIGHT_X = 3;
		public static final int ANALOG_RIGHT_Y = 2;
		public static final int EXTRA_1 = 10;
		
		public static boolean isXboxBluetoothGamepad(Controller controller) {
			return controller.getName().toLowerCase().contains("xbox") &&
					controller.getName().toLowerCase().contains("bluetooth") &&
					controller.getName().toLowerCase().contains("gamepad");
		}
		
		public static KyperControllerMapper getMappings() {
			if(KyperBoxGame.DEBUG_LOGGING) {
				KyperBoxGame.log("ControllerMappings", "Xbox Bluetooth Gamepad Mappings");
			}
			KyperControllerMapper mappings = new KyperControllerMapper();
			mappings.button_a = BUTTON_A;
			mappings.button_b = BUTTON_B;
			mappings.button_x = BUTTON_X;
			mappings.button_y = BUTTON_Y;
			mappings.left_1 = L1;
			mappings.right_1 = R1;
			mappings.select = SELECT;
			mappings.start = START;
			mappings.left_3 = L3;
			mappings.right_3 = R3;
			mappings.left2_trigger = L2_TRIGGER;
			mappings.right2_trigger = R2_TRIGGER;
			mappings.analog_left_x = ANALOG_LEFT_X;
			mappings.analog_left_y = ANALOG_LEFT_Y;
			mappings.analog_right_x = ANALOG_RIGHT_X;
			mappings.analog_right_y = ANALOG_RIGHT_Y;
			mappings.dpad = DPAD;
			mappings.extra1 = EXTRA_1;
			mappings.axis_trigger = true;
			
			return mappings;
		}
		
	}
	
	public static class BluetoothWirelessController{
		public static final int BUTTON_A = 1;
		public static final int BUTTON_B = 0;
		public static final int BUTTON_X = 4;
		public static final int BUTTON_Y = 3;
		public static final int DPAD = 0;
		public static final int L1 = 6;
		public static final int R1 = 7;
		public static final int SELECT = 12;
		public static final int START = 11;
		public static final int L3 = 13;
		public static final int R3 = 14;
		public static final int L2_TRIGGER = 8; 
		public static final int R2_TRIGGER = 9; 
		public static final int ANALOG_LEFT_X = 2; //
		public static final int ANALOG_LEFT_Y = 3;
		public static final int ANALOG_RIGHT_X = 0;
		public static final int ANALOG_RIGHT_Y = 1;
		public static final int EXTRA1 = 2;
		
		public static boolean isBluetoothWirelessController(Controller controller) {
			return controller.getName().toLowerCase().contains("bluetooth") &&
					controller.getName().toLowerCase().contains("wireless") &&
					controller.getName().toLowerCase().contains("controller");
		}
		
		public static KyperControllerMapper getMappings() {
			if(KyperBoxGame.DEBUG_LOGGING) {
				KyperBoxGame.log("ControllerMappings", "Bluetooth Wireless Controller Mappings");
			}
			KyperControllerMapper mappings = new KyperControllerMapper();
			mappings.button_a = BUTTON_A;
			mappings.button_b = BUTTON_B;
			mappings.button_x = BUTTON_X;
			mappings.button_y = BUTTON_Y;
			mappings.left_1 = L1;
			mappings.right_1 = R1;
			mappings.select = SELECT;
			mappings.start = START;
			mappings.left_3 = L3;
			mappings.right_3 = R3;
			mappings.left2_trigger = L2_TRIGGER;
			mappings.right2_trigger = R2_TRIGGER;
			mappings.analog_left_x = ANALOG_LEFT_X;
			mappings.analog_left_y = ANALOG_LEFT_Y;
			mappings.analog_right_x = ANALOG_RIGHT_X;
			mappings.analog_right_y = ANALOG_RIGHT_Y;
			mappings.dpad = DPAD;
			mappings.axis_trigger = false;
			mappings.extra1 = EXTRA1;
			return mappings;
		}
	}

}
