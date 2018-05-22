package com.kyperbox.umisc;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * KyperSprite is a simple wrapper class for the Gdx Sprite Class to add the ability to hold a sprite name
 * for backwards compatability with older versions of kyperbox.
 * @author john
 *
 */
public class KyperSprite extends Sprite{
	
	private String name;
	
	public KyperSprite(TextureRegion region) {
		super(region);
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

}
