package com.kyperbox.input;

/**useless?**/
public class TouchParameterBinding extends InputMapping {

	public static final int X = 0;
	public static final int Y = 1;

	private float value;
	int type;

	public TouchParameterBinding(int type) {
		this.type = type;
	}

	@Override
	public float inputValue() {
		return value;
	}

	@Override
	public boolean touchDown(float x, float y, int pointer, int button) {

		switch (type) {
		case X:
			value = x;
			return true;
		case Y:
			value = y;
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {


		switch (type) {
		case X:value = 0;
			return true;
		case Y:value = 0;
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean sameAs(InputMapping m) {
		return m instanceof TouchParameterBinding && ((TouchParameterBinding) m).type == type;
	}

}
