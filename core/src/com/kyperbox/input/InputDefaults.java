package com.kyperbox.input;

public class InputDefaults {
	public static final String MOVE_DOWN = "move_down";
	public static final String MOVE_UP = "move_up";
	public static final String MOVE_LEFT = "move_left";
	public static final String MOVE_RIGHT = "move_right";
	
	public static final String JUMP_BUTTON = "jump_button";
	public static final String ACTION_BUTTON = "action_button";
	
	public static final String EXIT = "exit";
	public static final String ENTER = "enter";
	
	public static final String STOP = "stop";
	public static final String START = "start";
	
	/**
	 * if you for some reason want to delete all default mappings
	 * @param input - your GameInput instance
	 */
	public static void removeDefaults(GameInput input) {
		input.removeInput(MOVE_DOWN);
		input.removeInput(MOVE_UP);
		input.removeInput(MOVE_LEFT);
		input.removeInput(MOVE_RIGHT);
		
		input.removeInput(JUMP_BUTTON);
		input.removeInput(ACTION_BUTTON);
		
		input.removeInput(EXIT);
		input.removeInput(ENTER);
		
		input.removeInput(STOP);
		input.removeInput(START);
	}

}
