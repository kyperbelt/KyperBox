package com.kyperbox.ztests;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.kyperbox.GameState;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.console.CommandRunnable;
import com.kyperbox.console.ConsoleCommand;
import com.kyperbox.console.DevConsole;
import com.kyperbox.controllers.LightController;
import com.kyperbox.controllers.LightController.LightType;
import com.kyperbox.input.InputDefaults;
import com.kyperbox.input.KeyboardMapping;
import com.kyperbox.managers.StateManager;
import com.kyperbox.objects.GameObject;
import com.kyperbox.systems.Box2dPhysicsSystem;
import com.kyperbox.systems.GameCameraSystem;
import com.kyperbox.systems.LayerSystem;
import com.kyperbox.umisc.StringUtils;

public class LightingTest extends KyperBoxGame {

	Box2dPhysicsSystem b2dphys;
	GameCameraSystem gcs;
	GameObject player;

	@Override
	public void initiate() {
		

		registerGameState("lightingtest.tmx", new StateManager() {
			
			@Override
			public void update(GameState state, float delta) {
				float speed = 100;
				player.setVelocity(0, 0);
				Vector2 v = player.getVelocity();
				
				if(getInput().inputPressed(InputDefaults.MOVE_LEFT)) {
					player.setVelocity(v.x-speed, v.y);
				}

				if(getInput().inputPressed(InputDefaults.MOVE_RIGHT)) {
					player.setVelocity(v.x+speed, v.y);
				}

				if(getInput().inputPressed(InputDefaults.MOVE_DOWN)) {
					player.setVelocity(v.x, v.y-speed);
				}

				if(getInput().inputPressed(InputDefaults.MOVE_UP)) {
					player.setVelocity(v.x, v.y+speed);
				}
				
			}

			@Override
			public void init(GameState state) {
				LightController lc = new LightController(LightType.ConeLight, Color.WHITE, 100);
				lc.setDirection(90);
				GameObject object = state.getPlaygroundLayer().getGameObject("player");
				object.setPosition(0, 0);
				object.addController(lc);
				player = object;
				gcs.addFocus(player);
			}

			@Override
			public void dispose(GameState state) {

			}

			@Override
			public void addLayerSystems(GameState state) {
				state.getPlaygroundLayer().getCamera().setCentered();
				gcs = new GameCameraSystem(10);
				gcs.setFeathering(true);
				state.getPlaygroundLayer().addSystem(gcs);
				b2dphys = new Box2dPhysicsSystem(100, 0, 0, true);
				state.getPlaygroundLayer().addSystem(b2dphys);
			}
		});

		//register state
		setGameState("lightingtest.tmx");

		//add command	
		getDevConsole().addCommand(new ConsoleCommand("ali", 1,
				"sets the ambient light intensity [SKY]0f[] - [SKY]1f[]", new CommandRunnable() {

					@Override
					public boolean executeCommand(DevConsole console, String... args) {

						float value = 0f;
						try {
							value = MathUtils.clamp(Float.parseFloat(args[0]), 0, 1f);
						} catch (Exception e) {
							console.error(e.getMessage());
							return false;
						}

						b2dphys.setAmbientLight(value);

						return true;
					}
				}));

		final String dr_format1 = "Debug render has been set to [GREEN]%s[]";
		getDevConsole().addCommand(new ConsoleCommand("debugrender", 1,
				"Enable or Disable debug render. Takes in a binary param ([GREEN]true[] or [GREEN]false[] - [sky]1[] or [sky]0[])",
				new CommandRunnable() {
					@Override
					public boolean executeCommand(DevConsole console, String... args) {
						boolean value = false;
						try {
							value = Boolean.parseBoolean(args[0]);
						} catch (Exception e) {
							console.error(e.getMessage());
							return false;
						}
						console.log(StringUtils.format(dr_format1,value));
						debugEnabled(value);
						return true;
					}
				}));
		
		
		//inputs
		
		getInput().addInputMapping(InputDefaults.MOVE_LEFT, new KeyboardMapping(Keys.LEFT));
		getInput().addInputMapping(InputDefaults.MOVE_RIGHT, new KeyboardMapping(Keys.RIGHT));
		getInput().addInputMapping(InputDefaults.MOVE_UP, new KeyboardMapping(Keys.UP));
		getInput().addInputMapping(InputDefaults.MOVE_DOWN, new KeyboardMapping(Keys.DOWN));
	}

}
