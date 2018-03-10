package com.kyperbox.umisc;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;

public class KyperProgressBar extends ProgressBar{

	
	private boolean flipped = false;
	
	public KyperProgressBar(float min, float max, float stepSize, boolean vertical, ProgressBarStyle style) {
		super(min, max, stepSize, vertical, style);
	}
	
	@Override
	public boolean setValue(float value) {
		if(!flipped)
			return super.setValue(value);
		else
			return super.setValue(Math.abs(value-getMaxValue()));
	}
	
	public boolean isFlipped() {
		return flipped;
	}
	
	public void setFlipped(boolean flipped) {
		this.flipped = flipped;
	}
	
	public void setAlpha(float alpha) {
		setColor(getColor().r,getColor().g,getColor().b,alpha);
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		Color c = Color.WHITE;
		
		super.draw(batch, parentAlpha);
		
		batch.setColor(c);
	}
	
	

}
