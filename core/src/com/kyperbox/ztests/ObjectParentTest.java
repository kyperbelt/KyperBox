package com.kyperbox.ztests;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kyperbox.GameState;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.controllers.CollisionController;
import com.kyperbox.controllers.CollisionController.CollisionData;
import com.kyperbox.input.InputDefaults;
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
		
		debugEnabled(true);
		registerGameState("parenting_test.tmx", new StateManager() {
			
			GameObject rpgman = null;
			GameObject rpg_chest = null;
			GameObject obstacle = null;
			
			String move_left = InputDefaults.MOVE_LEFT;
			String move_right = InputDefaults.MOVE_RIGHT;
			String move_down = InputDefaults.MOVE_DOWN;
			String move_up = InputDefaults.MOVE_UP;
			
			QuadTree quadtree;
			
			
			@Override
			public void update(GameState state,float delta) {
				float move = 100f;
				rpgman.setVelocity(0, 0);
				if(getInput().inputPressed(move_left)) {
					rpgman.setVelocity(-move, rpgman.getVelocity().y);
				}
				if(getInput().inputPressed(move_right)) {
					rpgman.setVelocity(move, rpgman.getVelocity().y);
				}
				
				if(getInput().inputPressed(move_up)) {
					rpgman.setVelocity(rpgman.getVelocity().x,move);
				}
				if(getInput().inputPressed(move_down)) {
					rpgman.setVelocity(rpgman.getVelocity().x, -move);
				}
				
				CollisionController cc = rpgman.getController(CollisionController.class);
				if(cc.getCollisions(delta).size > 0) {
					for (int i = 0; i < cc.getCollisions(delta).size; i++) {
						CollisionData cd = cc.getCollisions(delta).get(i);
						System.out.println("collided with "+cd.getTarget().getName());
					}
				}
				
			}
			
			@Override
			public void addLayerSystems(GameState state) {
				quadtree = new QuadTree(getView().getWorldWidth(), getView().getWorldHeight());
				
				state.getPlaygroundLayer().addSystem(quadtree);
				getInput().addInputMapping(move_left, new KeyboardMapping(Keys.LEFT));
				getInput().addInputMapping(move_right, new KeyboardMapping(Keys.RIGHT));
				getInput().addInputMapping(move_down, new KeyboardMapping(Keys.DOWN));
				getInput().addInputMapping(move_up, new KeyboardMapping(Keys.UP));
			}
			
			@Override
			public void init(GameState state) {
				rpgman = state.getPlaygroundLayer().getGameObject("rpg_man");
				rpgman.addController(new CollisionController(quadtree));
				rpg_chest = new BasicGameObject();
				rpg_chest.setName("rpg_chest");
				rpg_chest.addController(new CollisionController(quadtree));
				rpg_chest.setSize(32, 32);
				rpg_chest.setPosition(20, 20);
				rpg_chest.setSprite("rpg_chest_down");
				rpg_chest.setOrigin(Align.center);
				rpg_chest.addAction(Actions.repeat(-1, Actions.rotateBy(1f)));
				//rpg_chest.addController(new PlatformerController());
				//rpgman.addChild(rpg_chest);
				//rpgman.addController(rpg_man_controller);
				
				obstacle = state.getPlaygroundLayer().getGameObject("obstacle");
				obstacle.addChild(rpg_chest);
				obstacle.addController(new CollisionController(quadtree));
			}
			
			@Override
			public void dispose(GameState state) {
				
			}
		});
		
		setGameState("parenting_test.tmx");
		
		
	}

}
