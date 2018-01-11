package com.kyperbox.ztests;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.kyperbox.GameState;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.controllers.TileCollisionController;
import com.kyperbox.input.InputDefaults;
import com.kyperbox.input.KeyboardMapping;
import com.kyperbox.managers.StateManager;
import com.kyperbox.objects.BasicGameObject;
import com.kyperbox.objects.GameLayer;
import com.kyperbox.systems.ParallaxMapper;
import com.kyperbox.systems.TileCollisionSystem;

public class TileCollisionTest extends KyperBoxGame {

	public static final String TILE_COL_TEST = "tile_collision.tmx";

	public TileCollisionTest() {
		super(new FillViewport(1280, 720));
	}

	@Override
	public void initiate() {
		

		// register inputs
		getInput().addInputMapping(InputDefaults.MOVE_LEFT, new KeyboardMapping(Keys.LEFT));
		getInput().addInputMapping(InputDefaults.MOVE_RIGHT, new KeyboardMapping(Keys.RIGHT));
		getInput().addInputMapping(InputDefaults.MOVE_UP, new KeyboardMapping(Keys.UP));
		getInput().addInputMapping(InputDefaults.MOVE_DOWN, new KeyboardMapping(Keys.DOWN));
		
		
		//register test game state
		registerGameState(TILE_COL_TEST, new StateManager() {

			ParallaxMapper parallax;
			GameLayer playground;
			BasicGameObject player;

			private float speed = 300f;

			@Override
			public void update(float delta) {
				player.setVelocity(0,0); // reset velocity for this checks... this should be done in controllers not
										  // here

				if (getInput().inputPressed(InputDefaults.MOVE_LEFT)) {
					player.setVelocity(-speed, player.getVelocity().y);
				}

				if (getInput().inputPressed(InputDefaults.MOVE_RIGHT)) {
					player.setVelocity(speed, player.getVelocity().y);
				}

				if (getInput().inputPressed(InputDefaults.MOVE_DOWN)) {
					player.setVelocity(player.getVelocity().x, -speed);
				}

				if (getInput().inputPressed(InputDefaults.MOVE_UP)) {
					player.setVelocity(player.getVelocity().x, speed);
				}
				
				

			}

			@Override
			public void addLayerSystems(GameState state) {
				parallax = new ParallaxMapper(state.getPlaygroundLayer());
				parallax.addMapping("trees", .9f, 0f, true);
				parallax.addMapping("mountains", .6f, 0f, true);
				parallax.addMapping("mountain_far", .2f, 0f, true);
				parallax.addMapping("parallax_back", 0, 0f, true);
				state.getBackgroundLayer().addLayerSystem(parallax);
				state.getPlaygroundLayer().addLayerSystem(new TileCollisionSystem("platformer_tiles"));
				
				//debugRender(true);
			}

			@Override
			public void init(GameState state) {
				state.loadStateData();
				log(state.getData().getName(), state.getData().getInt("test")+"");
				state.getData().setInt("test", state.getData().getInt("test")+110);
				state.saveStateData();
				// layers
				playground = state.getPlaygroundLayer();
				player = (BasicGameObject) playground.getGameObject("player");
				//tile collision controller
				TileCollisionController tcc = new TileCollisionController();
				//tcc.collideWithVoid(false);
				player.addGameObjectController(tcc);
				player.setCollisionBounds(player.getWidth()*.2f, 0, player.getWidth()*.6f, player.getHeight()*.7f);
				playground.getCamera().setCentered();
				playground.getCamera().follow(player);

			}

			@Override
			public void dispose(GameState state) {

			}
		});
		
		//set the test state as initial state
		setGameState(TILE_COL_TEST);
	}

}
