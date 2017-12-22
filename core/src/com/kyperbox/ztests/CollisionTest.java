package com.kyperbox.ztests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kyperbox.GameInput.KeyboardMapping;
import com.kyperbox.GameInput.MouseButtonMapping;
import com.kyperbox.GameState;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.controllers.PlatformerController;
import com.kyperbox.managers.StateManager;
import com.kyperbox.objects.BasicGameObject;
import com.kyperbox.objects.GameLayer;
import com.kyperbox.systems.QuadTree;

public class CollisionTest extends KyperBoxGame{

	public CollisionTest() {
		super(new FitViewport(1280, 720));
	}

	@Override
	public void initiate() {
		Gdx.app.setLogLevel(Application.LOG_NONE);
		
		registerGameState("collisiontest.tmx", new StateManager() {
			QuadTree quadtree;
			private Vector2 mouse_pos;
			private MapProperties pooperties;
			private Label stats;
			private GameLayer playground;
			private int counter;
			private float interval = .01f;
			private float elapsed;
			private StringBuilder string = new StringBuilder(100);
			private String object_head = "objects=";
			private Pool<BasicGameObject> test_objects = new Pool<BasicGameObject>() {
				@Override
				protected BasicGameObject newObject() {
					return new BasicGameObject();
				}
			};
			
			private Array<BasicGameObject> objects;
			
			
			@Override
			public void update(float delta) {
				elapsed+=delta;
				
				if(getInput().inputPressed("action_button")&&elapsed >= interval) {
					
					elapsed = 0;
					mouse_pos = playground.getCamera().unproject(mouse_pos.set(getInput().getMouseX(), getInput().getMouseY()));
					BasicGameObject test = test_objects.obtain();
					test.setSize(32, 32);
					test.setName("random_object@"+counter);
					test.setSprite("windowbackground");
					counter++;
					test.setPosition(mouse_pos.x, mouse_pos.y);
					log("positioncheck", mouse_pos.toString());
					playground.addGameObject(test, pooperties);
					test.addGameObjectController(new PlatformerController());
					objects.add(test);
					
				}
				
				if(getInput().inputJustPressed("move_up")) {
					debugRender(!getDebugRender());
					if(Gdx.app.getLogLevel()!=Application.LOG_NONE)
						Gdx.app.setLogLevel(Application.LOG_NONE);
					else
						Gdx.app.setLogLevel(Application.LOG_INFO);
				}
				
				if(getInput().inputJustPressed("move_down")) {
					for (int i = 0; i < objects.size; i++) {
						objects.get(i).remove();
					}
					objects.clear();
					test_objects.freeAll(objects);
				}
				
				string.setLength(0);
				string.append(object_head);
				string.append(playground.getChildren().size);
				stats.setText(string);
			}
			
			@Override
			public void preInit(GameState state) {
				objects = new Array<BasicGameObject>(2000);
				counter = 0;
				mouse_pos = new Vector2();
				pooperties = new MapProperties();
				pooperties.put("sprite", "windowbackground");
				quadtree = new QuadTree(0, 0, getView().getWorldWidth(), getView().getWorldHeight());
				state.getPlaygroundLayer().addLayerSystem(quadtree);
			}
			
			@Override
			public void init(GameState state) {
				getInput().addInputMapping("action_button", new MouseButtonMapping(Buttons.LEFT));
				getInput().addInputMapping("move_up", new KeyboardMapping(Keys.W));
				getInput().addInputMapping("move_down", new KeyboardMapping(Keys.S));
				playground = state.getPlaygroundLayer();
				stats = (Label)state.getUiLayer().getActor("stats");
				stats.setText(object_head);
			}
			
			@Override
			public void dispose(GameState state) {
				
			}
		});
		
		setGameState("collisiontest.tmx");
		
	}

}
