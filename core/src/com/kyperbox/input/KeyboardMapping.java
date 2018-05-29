package com.kyperbox.input;

public class KeyboardMapping extends InputMapping{
	
	private int key;
	private boolean pressed;
	
	public KeyboardMapping(int key) {
		this.key = key;
	}
	@Override
	public float inputValue() {
		if(pressed)
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
	
	@Override
	public boolean keyDown(int keycode) {
		if(key == keycode)
			pressed = true;
		return super.keyDown(keycode);
	}
	
	@Override
	public boolean keyUp(int keycode) {
		if(key == keycode)
			pressed = false;
		return super.keyUp(keycode);
	}
	
	@Override
	public void reset() {
		pressed = false;
		super.reset();
	}
	
}