package com.kyperbox.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.kyperbox.ztests.MyGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		//AutoPacking.size = 4096;
		AutoPacking.pack("game", "image", "game");
		
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = MyGame.WIDTH;
		config.height = MyGame.HEIGHT;

		new LwjglApplication(new MyGame(), config);
	}
}
