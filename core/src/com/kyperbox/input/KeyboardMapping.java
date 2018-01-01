package com.kyperbox.input;

import com.badlogic.gdx.Gdx;

public class KeyboardMapping implements InputMapping{
	
	private int key;
	
	public KeyboardMapping(int key) {
		this.key = key;
	}
	@Override
	public float inputValue() {
		if(Gdx.input.isKeyPressed(key))
			return 1f;
		return GameInput.NOT_USED;
	}
	@Override
	public boolean sameAs(InputMapping m) {
		if(m instanceof KeyboardMapping) {
			KeyboardMapping km = (KeyboardMapping) m;
			if(km.key == key)
				return true;
		
		}
		return false;
	}
	
}