package com.kyperbox.ztests;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.kyperbox.GameState;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.controllers.PoiController;
import com.kyperbox.controllers.TileCollisionController;
import com.kyperbox.input.InputDefaults;
import com.kyperbox.input.KeyboardMapping;
import com.kyperbox.managers.StateManager;
import com.kyperbox.objects.BasicGameObject;
import com.kyperbox.objects.GameLayer;
import com.kyperbox.objects.GameObject;
import com.kyperbox.systems.GameCameraSystem;
import com.kyperbox.systems.ParallaxMapper;
import com.kyperbox.systems.TileCollisionSystem;
import com.kyperbox.util.BakedEffects;

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
		getInput().addInputMapping(InputDefaults.START, new KeyboardMapping(Keys.W));
		getInput().addInputMapping(InputDefaults.STOP, new KeyboardMapping(Keys.S));
		getInput().addInputMapping(InputDefaults.ACTION_BUTTON, new KeyboardMapping(Keys.SPACE));

		getInput().addInputMapping(InputDefaults.ENTER, new KeyboardMapping(Keys.Z));
		getInput().addInputMapping(InputDefaults.EXIT, new KeyboardMapping(Keys.X));

		// register test game state
		registerGameState(TILE_COL_TEST, new StateManager() {

			ParallaxMapper parallax;
			GameLayer playground;
			BasicGameObject player;
			GameCameraSystem game_cam;
			PoiController poi_test2;
			ShaderProgram default_shader;

			private float speed = 300f;

			@Override
			public void update(GameState state, float delta) {
				if(default_shader== null)
					default_shader = state.getStage().getBatch().getShader();
				player.setVelocity(0, 0); // reset velocity for this checks... this should be done in controllers not
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

				if (getInput().inputJustPressed(InputDefaults.START)) {
					playground.getCamera().setZoom(playground.getCamera().getZoom() * 1.1f);
				}

				if (getInput().inputJustPressed(InputDefaults.STOP)) {
					playground.getCamera().setZoom(playground.getCamera().getZoom() * .9f);
				}

				if (getInput().inputJustPressed(InputDefaults.ACTION_BUTTON)) {
					debugRender(!getDebugRender());
				}

				if (getInput().inputJustPressed(InputDefaults.ENTER)) {
					game_cam.addShake(20f, 1f);
				}

				if (getInput().inputJustPressed(InputDefaults.EXIT)) {
					if (state.getStateShader() == null) {
						state.setStateShader(getShader("grayscale"));
						if(playground.getLayerShader() == null)
							playground.setLayerShader(default_shader);
						else
							playground.setLayerShader(null);
					}else {
						state.setStateShader(null);
					}
				
				}

				// playground.getCamera().setPosition(player.getX(), player.getY());

			}

			@Override
			public void addLayerSystems(GameState state) {
				parallax = new ParallaxMapper(state.getPlaygroundLayer());
				parallax.addMapping("trees", .9f, 0f, true);
				parallax.addMapping("mountains", .6f, 0f, true);
				parallax.addMapping("mountain_far", .2f, 0f, true);
				parallax.addMapping("parallax_back", 0, 0f, true);
				parallax.setIgnoreZoom(false);
				state.getBackgroundLayer().addLayerSystem(parallax);
				state.getPlaygroundLayer().addLayerSystem(new TileCollisionSystem("platformer_tiles"));
				game_cam = new GameCameraSystem(3);
				state.getPlaygroundLayer().addLayerSystem(game_cam);
				debugRender(true);
			}

			@Override
			public void init(GameState state) {
				state.loadStateData();
				log(state.getData().getName(), state.getData().getInt("test") + "");
				state.getData().setInt("test", state.getData().getInt("test") + 110);
				state.saveStateData();
				// layers
				playground = state.getPlaygroundLayer();
				playground.getCamera().setZoom(1f);
				player = (BasicGameObject) playground.getGameObject("player");
				GameObject player2 = playground.getGameObject("player2");
				player2.addAction(Actions.repeat(-1, BakedEffects.shake(1f, 10f, true)));
				game_cam.addFocus(player);
				// game_cam.addFocus(player2);

				// tile collision controller
				TileCollisionController tcc = new TileCollisionController();
				// tcc.collideWithVoid(false);
				player.addController(tcc);
				playground.getGameObject("poi_test").addController(new PoiController("Player"));
				poi_test2 = new PoiController("Player2");
				poi_test2.setDuration(5f);
				playground.getGameObject("poi_test2").addController(poi_test2);
				playground.getGameObject("poi_test2").setOrigin(Align.center);
				playground.getGameObject("poi_test2").addAction(Actions.repeat(-1, Actions.sequence(
						BakedEffects.spiral(6f, 3, 200, playground.getGameObject("poi_test"), -1, false, true),
						BakedEffects.spiral(6f, -3, 200, playground.getGameObject("poi_test"), 1, false, true))));
				playground.getGameObject("poi_test").addAction(
						Actions.repeat(-1, BakedEffects.pulse(playground.getGameObject("poi_test"), 1.5f, .8f, 30)));
				player.setCollisionBounds(player.getWidth() * .2f, 0, player.getWidth() * .6f,
						player.getHeight() * .7f);
				playground.getCamera().setCentered();
				// offset the camera to the left
				// playground.getCamera().setOffset(playground.getCamera().getXOffset()+300,
				// playground.getCamera().getYOffset());

				state.setStateShader(state.getShader("grayscale"));

			}

			@Override
			public void dispose(GameState state) {
				state.unloadFonts();
				state.unloadParticles();
				state.unloadShaders();
			}
		});

		// set the test state as initial state
		setGameState(TILE_COL_TEST);
	}

}
