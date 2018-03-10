package com.kyperbox.input;

public class InputDefaults {
	public static final String MOVE_DOWN = 		 	"move_down";
	public static final String MOVE_UP =			"move_up";
	public static final String MOVE_LEFT = 			"move_left";
	public static final String MOVE_RIGHT = 		"move_right";
	
	public static final String JUMP_BUTTON = 		"jump_button";
	public static final String ACTION_BUTTON = 		"action_button";
	
	public static final String EXIT = 				"exit";
	public static final String ENTER = 				"enter";
	
	public static final String STOP = 				"stop";
	public static final String START =				"start";
	
	public static final String ADD =				"add";
	public static final String SUB = 			    "sub";
	
	public static final String UI_UP =				"ui_up";
	public static final String UI_DOWN = 			"ui_down";
	public static final String UI_LEFT = 			"ui_left";
	public static final String UI_RIGHT =			"ui_right";
	
	public static final String X = 					"xX";
	public static final String Y = 					"yY";
	public static final String Z = 					"zZ";
	
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
		
		input.removeInput(ADD);
		input.removeInput(SUB);
		
		input.removeInput(UI_UP);
		input.removeInput(UI_DOWN);
		input.removeInput(UI_LEFT);
		input.removeInput(UI_RIGHT);
		
		input.removeInput(X);
		input.removeInput(Y);
		input.removeInput(Z);
	}
	
	public static void addDefaults(GameInput input) {
		input.registerInput(MOVE_DOWN);
		input.registerInput(MOVE_UP);
		input.registerInput(MOVE_LEFT);
		input.registerInput(MOVE_RIGHT);
		
		input.registerInput(JUMP_BUTTON);
		input.registerInput(ACTION_BUTTON);
		
		input.registerInput(EXIT);
		input.registerInput(ENTER);
		
		input.registerInput(STOP);
		input.registerInput(START);
		
		input.registerInput(ADD);
		input.registerInput(SUB);
		
		input.registerInput(UI_UP);
		input.registerInput(UI_DOWN);
		input.registerInput(UI_LEFT);
		input.registerInput(UI_RIGHT);
		
		input.registerInput(X);
		input.registerInput(Y);
		input.registerInput(Z);
	}

}
