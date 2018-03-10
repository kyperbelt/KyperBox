package com.kyperbox.umisc;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;

public class ImageButton extends com.badlogic.gdx.scenes.scene2d.ui.ImageButton{

	public ImageButton(ImageButtonStyle style) {
		super(style);
	}
	
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		Color c = batch.getColor();
		super.draw(batch, parentAlpha);
		batch.setColor(c);
	}

}
