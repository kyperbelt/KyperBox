package com.kyperbox.input;

import com.badlogic.gdx.Gdx;

public class MouseButtonMapping extends InputMapping{
	private int button;
	public MouseButtonMapping(int button) {
		this.button = button;
	}
	
	@Override
	public float inputValue() {
		if(Gdx.input.isButtonPressed(button))
			return 1f;
		return GameInput.NOT_USED;
	}

	@Override
	public boolean sameAs(InputMapping m) {
		return m instanceof MouseButtonMapping && ((MouseButtonMapping)m).button == button;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof InputMapping) {
			return sameAs((InputMapping) obj);
		}
		return false;
	}
	
}