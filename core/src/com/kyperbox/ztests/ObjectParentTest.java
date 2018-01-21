package com.kyperbox.ztests;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kyperbox.GameState;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.controllers.CollisionController;
import com.kyperbox.controllers.PlatformerController;
import com.kyperbox.input.KeyboardMapping;
import com.kyperbox.managers.StateManager;
import com.kyperbox.objects.BasicGameObject;
import com.kyperbox.objects.GameObject;
import com.kyperbox.systems.QuadTree;

public class ObjectParentTest extends KyperBoxGame{

	public ObjectParentTest() {
		super(new FitViewport(1280, 720));
	}

	@Override
	public void initiate() {
		
		registerGameState("parenting_test.tmx", new StateManager() {
			
			GameObject rpgman = null;
			GameObject rpg_chest = null;
			GameObject obstacle = null;
			PlatformerController rpg_man_controller;
			
			String move_left = "move_left";
			String move_right = "move_right";
			String move_down = "move_down";
			String jump = "jump_button";
			
			
			@Override
			public void update(float delta) {
				if(getInput().inputPressed(move_left)) {
					rpg_man_controller.moveLeft();
				}
				if(getInput().inputPressed(move_right)) {
					rpg_man_controller.moveRight();
				}
				if(getInput().inputPressed(move_left)==getInput().inputPressed(move_right))
					rpg_man_controller.stopX();
				
				if(getInput().inputPressed(jump))
					rpg_man_controller.jump();
				
				if(getInput().inputJustPressed(move_down)) {
					debugRender(!getDebugRender());
				}
			}
			
			@Override
			public void addLayerSystems(GameState state) {
				state.getPlaygroundLayer().addLayerSystem(new QuadTree(getView().getWorldWidth(), getView().getWorldHeight()));
				getInput().addInputMapping(move_left, new KeyboardMapping(Keys.LEFT));
				getInput().addInputMapping(move_right, new KeyboardMapping(Keys.RIGHT));
				getInput().addInputMapping(move_down, new KeyboardMapping(Keys.S));
				getInput().addInputMapping(jump, new KeyboardMapping(Keys.SPACE));
			}
			
			@Override
			public void init(GameState state) {
				rpgman = state.getPlaygroundLayer().getGameObject("rpg_man");
				rpgman.addController(new CollisionController());
				rpg_man_controller = new PlatformerController();
				rpg_chest = new BasicGameObject();
				rpg_chest.setName("rpg_chest");
				rpg_chest.addController(new CollisionController());
				rpg_chest.setSize(32, 32);
				rpg_chest.setPosition(20, 20);
				rpg_chest.setSprite("rpg_chest_down");
				rpg_chest.setOrigin(Align.center);
				rpg_chest.addAction(Actions.repeat(-1, Actions.rotateBy(1f)));
				rpg_chest.addController(new PlatformerController());
				//rpgman.addChild(rpg_chest);
				rpgman.addController(rpg_man_controller);
				
				obstacle = state.getPlaygroundLayer().getGameObject("obstacle");
				obstacle.addChild(rpg_chest);
				obstacle.addController(new CollisionController());
			}
			
			@Override
			public void dispose(GameState state) {
				
			}
		});
		
		setGameState("parenting_test.tmx");
		
		
	}

}
