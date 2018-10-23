package com.kyperbox.desktop;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.console.DevConsole;
import com.kyperbox.ztests.ControllerTest;
import com.kyperbox.ztests.LightingTest;
import com.kyperbox.ztests.ParticleTests;
import com.kyperbox.ztests.TileCollisionTest;

public class DesktopLauncher {
	public static boolean DEPLOYMENT = false;
	public static void main (String[] arg) {
		
		if(!DEPLOYMENT) {
		//AutoPacking.size = 4096;
			AutoPacking.pack("game", "image", "game");
		}
		//game
		KyperBoxGame game = new TileCollisionTest();
		
		game.setDevConsole(new DevConsole("console.fnt", "shade1.png",Keys.GRAVE));
		
		//config
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = (int) game.getView().getWorldWidth();
		config.height = (int) game.getView().getWorldHeight();
		config.title = game.getGameName();
		new LwjglApplication(game, config);
	}
}
