package com.kyperbox.ztests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kyperbox.GameInput.MouseButtonMapping;
import com.kyperbox.GameState;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.managers.QuadTree;
import com.kyperbox.managers.StateManager;
import com.kyperbox.objects.BasicGameObject;
import com.kyperbox.objects.GameLayer;

public class CollisionTest extends KyperBoxGame{

	public CollisionTest() {
		super(new FitViewport(1920, 1080));
	}

	@Override
	public void initiate() {
		registerGameState("collisiontest.tmx", new StateManager() {
			
			QuadTree quadtree;
			private Vector2 mouse_pos;
			private MapProperties pooperties;
			private GameLayer playground;
			private int counter;
			
			@Override
			public void update(float delta) {
				Gdx.graphics.setTitle("Collision Test - objects="+playground.getChildren().size+" fps="+Gdx.graphics.getFramesPerSecond());
				if(getInput().inputPressed("action_button")) {
					mouse_pos = playground.getCamera().unproject(mouse_pos.set(getInput().getMouseX(), getInput().getMouseY()));
					BasicGameObject test = new BasicGameObject();
					test.setSize(32, 32);
					test.setName("random_object@"+counter);
					test.setSprite("windowbackground");
					counter++;
					test.setPosition(mouse_pos.x, mouse_pos.y);
					log("positioncheck", mouse_pos.toString());
					playground.addGameObject(test, pooperties);
					
				}
			}
			
			@Override
			public void preInit(GameState state) {
				debugRender(true);
				counter = 0;
				mouse_pos = new Vector2();
				pooperties = new MapProperties();
				pooperties.put("sprite", "windowbackground");
				quadtree = new QuadTree(0, 0, getView().getWorldWidth(), getView().getWorldHeight());
				state.getPlaygroundLayer().addLayerManager(quadtree);
			}
			
			@Override
			public void init(GameState state) {
				getInput().addInputMapping("action_button", new MouseButtonMapping(Buttons.LEFT));
				playground = state.getPlaygroundLayer();
			}
			
			@Override
			public void dispose(GameState state) {
				
			}
		});
		
		setGameState("collisiontest.tmx");
		
	}

}
