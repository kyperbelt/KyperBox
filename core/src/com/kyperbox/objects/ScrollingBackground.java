package com.kyperbox.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.MathUtils;

public class ScrollingBackground extends GameObject {

	private TextureRegion background;

	private int size;
	private int xoff;
	private int yoff;

	private float x_check;
	private float y_check;

	private int x;
	private int y;

	private float scroll_x;
	private float scroll_y;

	private float pos_x;
	private float pos_y;

	private float virtual_width;
	private float virtual_height;

	private boolean repeat_vertical;
	private boolean repeat_horizontal;

	private boolean absolute; // if absolute then do not repeat vertical do not repeat horizontal and do not
								// lock to size.

	@Override
	public void init(MapProperties properties) {
		super.init(properties);
		String image = properties.get("image", String.class);
		absolute = properties.get("absolute", false, boolean.class);
		scroll_x = properties.get("speed_horizontal", 0f, Float.class);
		scroll_y = properties.get("speed_vertical", 0f, Float.class);
		repeat_vertical = properties.get("repeat_vertical", false, Boolean.class);
		repeat_horizontal = properties.get("repeat_horizontal", false, Boolean.class);
		if (image == null || image.isEmpty())
			getState().error("background [" + getName() + "] image not set.");
		background = getGame().getAtlas(getState().getData().getString("map_atlas")).findRegion(image);
		size = 1;
		xoff = 0;
		yoff = 0;
		x_check = 0f;
		y_check = 0f;
		x = 0;
		y = 0;
		virtual_width = getGame().getView().getWorldWidth();
		virtual_height = getGame().getView().getWorldHeight();
		if (!absolute) {
			if (repeat_vertical) {
				yoff -= 1;
				size *= 3;
			}
			if (repeat_horizontal) {
				xoff -= 1;
				size *= 3;
			}
		}

		pos_x = 0;
		pos_y = 0;

	}
	
	/**
	 * if true then this background will not repeat or stop when it reaches its edges. 
	 * Absolution cannot be changed once set in Tiled
	 * @return
	 */
	public boolean isAbsolute() {
		return absolute;
	}

	@Override
	public void act(float delta) {
		pos_x += scroll_x * delta;
		pos_y += scroll_y * delta;

		if (!absolute) {
			if (repeat_horizontal) {
				if (pos_x > getWidth()) {
					pos_x = 0;
				} else if (pos_x < 0) {
					pos_x = getWidth();
				}
			} else {

				if (pos_x + getX() > 0) {
					pos_x = -getX();
				} else if (pos_x + getX() + getWidth() < virtual_width) {
					pos_x = virtual_width - (getX() + getWidth());
				}
			}

			if (repeat_vertical) {
				if (pos_y > getHeight()) {
					pos_y = 0;
				} else if (pos_y < 0) {
					pos_y = getHeight();
				}
			} else {
				if (pos_y + getY() > 0) {
					pos_y = -getY();
				} else if (pos_y + getY() + getHeight() < virtual_height) {
					pos_y = virtual_height - (getY() + getHeight());
				}
			}
		}
		super.act(delta);
	}

	public void setScrollPos(float x, float y) {
		this.pos_x = x;
		this.pos_y = y;
	}
	
	public void translateScrollPos(float x_trans,float y_trans) {
		setScrollPos(this.pos_x+x_trans, this.pos_y+y_trans);
	}

	public void setScrollX(float scroll_x) {
		this.scroll_x = scroll_x;
	}

	public void setScrollY(float scroll_y) {
		this.scroll_y = scroll_y;
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		if (background != null) {
			// draw background
			Color cc = batch.getColor();
			
			batch.setColor(getColor().r,getColor().g,getColor().b,getColor().a *parentAlpha);
			if (size > 1) {
				for (int i = 0; i < size; i++) {
					y = (i % (size / 3)) + yoff;
					x = (i / (size / 3)) + xoff;
					x_check = pos_x + (getWidth() * x);
					y_check = pos_y + (getHeight() * y);
					batch.draw(background, MathUtils.floor(getX() + x_check),MathUtils.floor( getY() + y_check), getWidth(), getHeight());
				}
			} else {
				batch.draw(background, MathUtils.floor(getX() + pos_x), MathUtils.floor(getY() + pos_y), getWidth(), getHeight());
			}
			batch.setColor(cc);

		}
		super.draw(batch, parentAlpha);
	}
}
