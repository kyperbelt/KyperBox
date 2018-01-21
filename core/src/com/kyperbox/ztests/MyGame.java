package com.kyperbox.ztests;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kyperbox.GameState;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.controllers.PlatformerController;
import com.kyperbox.input.KeyboardMapping;
import com.kyperbox.managers.StateManager;
import com.kyperbox.objects.GameLayer;
import com.kyperbox.objects.PlatformerObject;

public class MyGame extends KyperBoxGame{

	public static final int WIDTH = 1280;
	public static final int HEIGHT = 720;
	public MyGame() {
		super(new FitViewport(WIDTH, HEIGHT));
	}

	@Override
	public void initiate() {
		
		StateManager test_manager = new StateManager() {
			
			GameLayer playground;
			//Vector2 mouse_coords;
			//GameObject cloud1;
			//ScrollingBackground background;
			PlatformerObject player;
			
			
			@Override
			public void addLayerSystems(GameState game) {
				getInput().addInputMapping("move_up", new KeyboardMapping(Keys.W));
				getInput().addInputMapping("move_right", new KeyboardMapping(Keys.D));
				getInput().addInputMapping("move_down", new KeyboardMapping(Keys.S));
				getInput().addInputMapping("move_left", new KeyboardMapping(Keys.A));
				getInput().addInputMapping("jump_button", new KeyboardMapping(Keys.SPACE));
				
				playground = game.getPlaygroundLayer();
				
				//mouse_coords = new Vector2();
				
				
				
			}
			
			@Override
			public void update(GameState state,float delta) {
				if(player!=null) {
					PlatformerController pc = player.getController(PlatformerController.class);
					if(getInput().inputPressed("move_right")) {
						player.getController(PlatformerController.class).moveRight();
					}else if(getInput().inputPressed("move_left")) {
						player.getController(PlatformerController.class).moveLeft();
					}else {
						if(pc.onGround()){
							player.getController(PlatformerController.class).stopX();
						}
						
					}
					
					if(getInput().inputPressed("move_up")) {
					}else if(getInput().inputPressed("move_down")) {
					}else {
					}
					
					if(getInput().inputPressed("jump_button")) {
						player.getController(PlatformerController.class).jump();
					}
					
					player.setFlip(pc.facingLeft(), player.getFlipY());
				}
				
				
				
				//mouse unprojection test
//				mouse_coords.set(Gdx.input.getX(),Gdx.input.getY());
//				mouse_coords = playground.getCamera().unproject(mouse_coords);
//				log("My test:", " mouse_coords:"+mouse_coords.toString());
				
				//object projection test
//				if(cloud1 !=null) {
//					mouse_coords.set(cloud1.getX(),cloud1.getY());
//					mouse_coords = playground.getCamera().project(mouse_coords);
//					log("My test:", " cloud1_coords:"+mouse_coords.toString());
//				}
				
			}
			
			@Override
			public void init(GameState game) {
				log("MyGame","initiated manager");
				Actor a = game.getUiLayer().getActor("playbutton");
				
				if(a!=null) {
					ImageButton b = (ImageButton) a;
					b.addListener(new ClickListener() {
						@Override
						public void clicked(InputEvent event, float x, float y) {
							setGameState("game.tmx");
						}
					});
				}
				
				//background = (ScrollingBackground) game.getBackgroundLayer().getGameObject("cloud_background");
				player = (PlatformerObject)game.getPlaygroundLayer().getGameObject("player_1");
				playground.getCamera().setCentered();
				playground.getCamera().setCamFollowBounds(-100, -316, 200, 300);
				
				
			}
			
			@Override
			public void dispose(GameState game) {
				
			}
		};
		
		log("MyGame", "initiated");
		registerGameState("test.tmx", test_manager);
		
		registerGameState("game.tmx", test_manager);
		
		setGameState("test.tmx");
	}

}
