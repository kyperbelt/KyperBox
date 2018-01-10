package com.kyperbox.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.ztests.CollisionTest;
import com.kyperbox.ztests.ParallaxMappingTest;
import com.kyperbox.ztests.TileCollisionTest;

public class DesktopLauncher {
	public static boolean DEPLOYMENT = true;
	public static void main (String[] arg) {
		
		if(!DEPLOYMENT) {
		//AutoPacking.size = 4096;
			AutoPacking.pack("game", "image", "game");
		}
		//game
		KyperBoxGame game = new TileCollisionTest();
		
		//config
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = (int) game.getView().getWorldWidth();
		config.height = (int) game.getView().getWorldHeight();
		new LwjglApplication(game, config);
	}
}
