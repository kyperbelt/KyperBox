package com.kyperbox.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.ztests.CollisionTest;
import com.kyperbox.ztests.MyGame;
import com.kyperbox.ztests.ObjectParentTest;

public class DesktopLauncher {
	public static void main (String[] arg) {
		//AutoPacking.size = 4096;
		AutoPacking.pack("game", "image", "game");
		
		//game
		KyperBoxGame game = new CollisionTest();
		
		//config
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = (int) game.getView().getWorldWidth();
		config.height = (int) game.getView().getWorldHeight();

		new LwjglApplication(game, config);
	}
}
