package com.kyperbox.managers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.kyperbox.GameState;
import com.kyperbox.objects.GameObject;

public class TransitionManager extends StateManager {

	public static final int FADE = 0;
	public static final int WIPE = 1;

	private Pixmap transition_map;
	private TextureRegion r;
	private TransitionObject trans_object;
	private int transition_type;
	private boolean state_in;
	private float duration;
	private float elapsed;
	private String next_state;
	private Color color;
	private float scale;
	
	public TransitionManager() {
		color = Color.BLACK;
		duration = 1f;
		scale = .25f;
		r = null;
	}

	public void setTransitionType(int type) {
		this.transition_type = type;
	}

	public boolean isIn() {
		return state_in;
	}

	public boolean isOut() {
		return !state_in;
	}

	public String nextState() {
		return next_state;
	}

	public void setNextState(String next_state) {
		this.next_state = next_state;
	}

	public int getTransitionType() {
		return transition_type;
	}
	
	public float getDuration() {
		return duration;
	}
	
	public void setDuration(float duration) {
		this.duration = duration;
	}

	public Pixmap createTransitionPixMap(int type, GameState state) {
		Pixmap map = null;
		switch (type) {
		case FADE:
		case WIPE:
		default:
			map = new Pixmap((int) (state.getGame().getView().getWorldWidth()*scale),
					(int) (state.getGame().getView().getWorldHeight()*scale), Format.RGBA8888);
			map.setColor(Color.BLACK);
			map.fill();
			break;
		}

		return map;
	}

	public TransitionObject getTransitionObject(Color c,boolean in, int type,TextureRegion map) {
		TransitionObject o = new TransitionObject();
		o.setTexture(map);
		float width = map.getRegionWidth()/scale;
		float height = map.getRegionHeight()/scale;
		switch (type) {
		case FADE:
			o.setSize(width, height);
			o.setPosition(0, 0);
			if(in) {
				o.setColor(c.r, c.g, c.b, 1f);
				o.addAction(Actions.fadeOut(duration));
			}else{
				o.setColor(c.r,c.g,c.b,0f);
				o.addAction(Actions.fadeIn(duration));
			}
			
			break;
		case WIPE:
			o.setSize(width, height);
			o.setColor(c);
			if(in) {
				o.setPosition(0, 0);
				o.addAction(Actions.moveBy(o.getWidth(), o.getY(), duration));
			}else{
				o.setPosition(width, 0);
				o.addAction(Actions.moveBy(-o.getWidth(), o.getY(), duration));
			}
			break;
		default:
			break;
		}
		return o;
	}

	public boolean finished() {
		return elapsed >= duration;
	}

	public void reset() {
		state_in = false;
	}

	@Override
	public void addLayerSystems(GameState state) {

	}

	@Override
	public void init(GameState state) {
		elapsed = 0;
		if(r==null) {
			transition_map = createTransitionPixMap(getTransitionType(), state);
			r = new TextureRegion(new Texture(transition_map));
		}
		trans_object = getTransitionObject(getColor(),isIn(), getTransitionType(), r);

		state.getForegroundLayer().addActor(trans_object);


	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public Color getColor() {
		return color;
	}

	@Override
	public void update(GameState state, float delta) {
		elapsed += delta;
		if (finished()) {
			if (isIn()) {
				elapsed = 0;
				state.getGame().popGameState();
				state_in = false;
			} else {
				elapsed = 0;
				state.getGame().setGameState(next_state);
				state.getGame().pushGameState(state);
				state_in = true;
			}
		}
	}

	@Override
	public void dispose(GameState state) {

	}

	public static class TransitionObject extends GameObject {

		TextureRegion map;

		public TransitionObject() {
		}

		public void setTexture(TextureRegion map) {
			this.map = map;
		}

		@Override
		public void draw(Batch batch, float parentAlpha) {
			if (map != null) {
				Color prev = batch.getColor();
				batch.setColor(getColor());
				batch.draw(map, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(),
						getScaleY(), getRotation());
				batch.setColor(prev);
			}
		}

	}

}
