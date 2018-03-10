package com.kyperbox.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.kyperbox.KyperBoxGame;

public class GameInput extends InputMultiplexer {

	public static final float NOT_USED = 0f;

	private ObjectMap<String, Float> inputs_pressed;
	private ObjectMap<String, Boolean> inputs_last_pressed;
	private ObjectMap<String, Array<InputMapping>> mappings;
	private Array<String> input_check;
	
	private boolean touch_down = false;
	private boolean justTouched = false;
	
	private InputAdapter ia;

	public GameInput() {
		inputs_pressed = new ObjectMap<String, Float>();
		inputs_last_pressed = new ObjectMap<String, Boolean>();
		mappings = new ObjectMap<String, Array<InputMapping>>();
		input_check = new Array<String>();

		InputDefaults.addDefaults(this);
		
		
		ia = new InputAdapter() {
			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				justTouched = touch_down == false?true:false;
				touch_down = true;
				return false;
			}
			
			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button) {
				justTouched = false;
				touch_down = false;
				return false;
			}
		};
		addProcessor(ia);
		
		
	}

	public void addInputMapping(String input, InputMapping mapping) {
		if (!mappings.containsKey(input)) {
			KyperBoxGame.error("GameInput", "input [" + input + "] has not been registered. mapping not added!");
			return;
		}
		Array<InputMapping> maps = mappings.get(input);
		for (InputMapping map : maps)
			if (map.sameAs(mapping)) {
				KyperBoxGame.log("GameInput", "duplicate input map not added to [" + input + "].");
				return;
			}
		mapping.setGameInput(this);
		mappings.get(input).add(mapping);
	}

	public void registerInput(String input) {
		if (inputs_pressed.containsKey(input)) {
			KyperBoxGame.error("GameInput", "failed to register duplicate input [" + input + "]");
			return;
		}
		inputs_pressed.put(input, NOT_USED);
		inputs_last_pressed.put(input, false);
		mappings.put(input, new Array<InputMapping>());
		input_check.add(input);
	}

	public Array<String> getAllInputs() {
		return input_check;
	}

	/**
	 * removes all inputs and all their mappings
	 */
	public void removeAllInputs() {
		for (String input : input_check) {
			inputs_pressed.remove(input);
			inputs_last_pressed.remove(input);
			Array<InputMapping> im = mappings.remove(input);
			for (int i = 0; i < im.size; i++) {
				im.get(i).removed();
			}
		}
		input_check.clear();
	}

	/**
	 * removes an input and all its mappings
	 * 
	 * @param input
	 */
	public void removeInput(String input) {
		inputs_pressed.remove(input);
		inputs_last_pressed.remove(input);
		input_check.removeValue(input, false);
		Array<InputMapping> im = mappings.remove(input);
		for (int i = 0; i < im.size; i++) {
			im.get(i).removed();
		}
	}

	public float inputValue(String input) {
		return inputs_pressed.get(input);
	}

	public boolean inputPressed(String input) {
		return inputs_pressed.get(input) != NOT_USED;
	}

	public boolean inputJustPressed(String input) {
		return inputs_last_pressed.get(input) && !inputPressed(input);
	}

	public float getX() {
		return Gdx.input.getX();
	}

	public float getY() {
		return Gdx.input.getY();
	}

	/**
	 * return true if the user is clicking on the screen with the left mouse button
	 * or touching the screen on android - if you would like control of button
	 * please use a mousebutton mapping instead of this method
	 * 
	 * @return
	 */
	public boolean touchDown() {
		return touch_down;
	}

	/**
	 * return true if the user has just clicked on the screen with the left mouse
	 * button or touching the screen on android - if you would like control of
	 * button please use a MouseButtonMapping instead of this method
	 * 
	 * @return
	 */
	public boolean justTouched() {
		boolean jt = justTouched;
		justTouched = false;
		return jt;
	}

	public void update() {
		for (String input : input_check) {
			float value = NOT_USED;
			for (InputMapping mapping : mappings.get(input)) {
				value = mapping.inputValue();
				if (value != NOT_USED) {
					break;
				}
			}
			boolean last_pressed = inputs_last_pressed.get(input);
			if (value != NOT_USED && inputs_pressed.get(input) == NOT_USED && !last_pressed) {
				inputs_last_pressed.put(input, !last_pressed);
			} else if (value != NOT_USED) {
				inputs_pressed.put(input, value);
			} else {
				inputs_last_pressed.put(input, false);
				inputs_pressed.put(input, NOT_USED);
			}
		}
	}

}