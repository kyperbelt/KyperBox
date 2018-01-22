package com.kyperbox.input;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.kyperbox.KyperBoxGame;

public class GameInput{
	
	public static final float NOT_USED = 0f;
	
	private ObjectMap<String, Float> inputs_pressed;
	private ObjectMap<String, Boolean> inputs_last_pressed;
	private ObjectMap<String, Array<InputMapping>> mappings;
	private Array<String> input_check;
	
	public GameInput() {
		inputs_pressed = new ObjectMap<String, Float>();
		inputs_last_pressed = new ObjectMap<String, Boolean>();
		mappings = new ObjectMap<String, Array<InputMapping>>();
		input_check = new Array<String>();
		
		InputDefaults.addDefaults(this);
		
	}
	
	public void addInputMapping(String input,InputMapping mapping) {
		if(!mappings.containsKey(input)) {
			KyperBoxGame.error("GameInput", "input ["+input+"] has not been registered. mapping not added!");
			return;
		}
		Array<InputMapping> maps = mappings.get(input);
		for(InputMapping map:maps) 
			if(map.sameAs(mapping)) {
				KyperBoxGame.log("GameInput", "duplicate input map not added to ["+input+"].");
				return;
			}
		mappings.get(input).add(mapping);
	}
	
	public void registerInput(String input) {
		if(inputs_pressed.containsKey(input)) {
			KyperBoxGame.error("GameInput", "failed to register duplicate input ["+input+"]");
			return;
		}
		inputs_pressed.put(input, NOT_USED);
		inputs_last_pressed.put(input, false);
		mappings.put(input, new Array<InputMapping>());
		input_check.add(input);
	}
	
	public Array<String> getAllInputs(){
		return input_check;
	}
	
	/**
	 * removes all inputs and all their mappings
	 */
	public void removeAllInputs() {
		for(String input:input_check) {
			inputs_pressed.remove(input);
			inputs_last_pressed.remove(input);
			mappings.remove(input);
		}
		input_check.clear();
	}
	
	/**
	 * removes an input and all its mappings
	 * @param input
	 */
	public void removeInput(String input) {
		inputs_pressed.remove(input);
		inputs_last_pressed.remove(input);
		input_check.removeValue(input, false);
		mappings.remove(input);
	}
	
	public float inputValue(String input) {
		return inputs_pressed.get(input);
	}
	
	public boolean inputPressed(String input) {
		return inputs_pressed.get(input)!=NOT_USED;
	}
	
	public boolean inputJustPressed(String input) {
		return inputs_last_pressed.get(input)&&!inputPressed(input);
	}
	
	public float getX() {
		return Gdx.input.getX();
	}
	
	public float getY() {
		return Gdx.input.getY();
	}
	
	public void update() {
		for(String input:input_check) {
			float value = NOT_USED;
			for(InputMapping mapping:mappings.get(input)) {
				value = mapping.inputValue();
				if(value!=NOT_USED) {
					break;
				}
			}
			boolean last_pressed = inputs_last_pressed.get(input);
			if(value!=NOT_USED&&inputs_pressed.get(input)==NOT_USED&&!last_pressed) {
				inputs_last_pressed.put(input, !last_pressed);
			}else if(value!=NOT_USED) {
				inputs_pressed.put(input, value);
			}else {
				inputs_last_pressed.put(input, false);
				inputs_pressed.put(input, NOT_USED);
			}
		}
	}
	
}