package com.kyperbox.input;

public class SimpleGestureMapping extends InputMapping {
	public static final int VERTICAL_PAN = 0;
	public static final int HORIZONTAL_PAN = 1;
	public static final int ZOOM = 2;

	private int type;
	private float value;
	
	public SimpleGestureMapping(int type) {
		value = 0;
		this.type = type;
	}

	@Override
	public float inputValue() {
		float v = value;
		value = 0;
		return v;
	}

	@Override
	public boolean sameAs(InputMapping m) {
		return m instanceof SimpleGestureMapping && type == ((SimpleGestureMapping) m).type;
	}
	

	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY) {
		switch (type) {
		case VERTICAL_PAN:
			value = deltaY; 
			break;
		case HORIZONTAL_PAN:
			value = deltaX;
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	public boolean panStop(float x, float y, int pointer, int button) {
		switch (type) {
		case VERTICAL_PAN:
		case HORIZONTAL_PAN:value = 0;
			break;
		case ZOOM:value = 0;
		break;
		default:
			break;
		}
		return false;
	}
	
	@Override
	public boolean zoom(float initialDistance, float distance) {
		if(type == ZOOM) {
			value = distance/initialDistance;
		}
		return false;
	}

}
