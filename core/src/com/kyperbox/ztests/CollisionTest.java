package com.kyperbox.ztests;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.kyperbox.GameState;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.controllers.CollisionController;
import com.kyperbox.controllers.PlatformerController;
import com.kyperbox.input.KeyboardMapping;
import com.kyperbox.input.MouseButtonMapping;
import com.kyperbox.managers.StateManager;
import com.kyperbox.objects.BasicGameObject;
import com.kyperbox.objects.GameLayer;
import com.kyperbox.systems.QuadTree;

public class CollisionTest extends KyperBoxGame{
	
	public static final String CAM_LEFT = "cam_left";
	public static final String CAM_RIGHT = "cam_right";
	public static final String CAM_DOWN = "cam_down";
	public static final String CAM_UP = "cam_up";

	public CollisionTest() {
		super(new FitViewport(1280, 720));
	}

	@Override
	public void initiate() {
		//Gdx.app.setLogLevel(Application.LOG_NONE);
		
		getInput().registerInput(CAM_UP);
		getInput().registerInput(CAM_DOWN);
		getInput().registerInput(CAM_LEFT);
		getInput().registerInput(CAM_RIGHT);
		
		getInput().addInputMapping(CAM_UP, new KeyboardMapping(Keys.UP));
		getInput().addInputMapping(CAM_DOWN, new KeyboardMapping(Keys.DOWN));
		getInput().addInputMapping(CAM_LEFT, new KeyboardMapping(Keys.LEFT));
		getInput().addInputMapping(CAM_RIGHT, new KeyboardMapping(Keys.RIGHT));
		
		registerGameState("collisiontest.tmx", new StateManager() {
			QuadTree quadtree;
			private String sprite_name = "windowbackground";
			private String action_button = "action_button";
			private String move_up = "move_up";
			private String move_down = "move_down";
			private String object_name_start = "object@";
			private Vector2 mouse_pos;
			private MapProperties pooperties;
			private Label stats;
			private GameLayer playground;
			private int counter;
			private float interval = .5f;
			private float elapsed;
			private float cam_speed = 100f;
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
			public void update(GameState state,float delta) {
				elapsed+=delta;
				
				if(getInput().inputPressed(action_button)&&elapsed >= interval) {
					
					elapsed = 0;
					mouse_pos = playground.getCamera().unproject(mouse_pos.set(getInput().getX(), getInput().getY()));
					BasicGameObject test = test_objects.obtain();
					test.setSize(32, 32);
					test.setName(object_name_start+counter);
					test.setSprite(sprite_name);
					counter++;
					test.setPosition(mouse_pos.x, mouse_pos.y);
					
					playground.addGameObject(test, pooperties);
					
					
					if(test.getController(PlatformerController.class)==null)
						test.addController(new PlatformerController());
					else {
						test.getController(PlatformerController.class).reset();
					}
					if(test.getController(CollisionController.class)==null)
						test.addController(new CollisionController());
					else {
						test.getController(CollisionController.class).reset();
					}
					
					
					
					objects.add(test);
					
				}
				
				Vector2 cam_pos = playground.getCamera().getPosition();
				
				if(getInput().inputPressed(CAM_LEFT)) {
					playground.getCamera().setPosition(cam_pos.x-cam_speed*delta,cam_pos.y);
					cam_pos = playground.getCamera().getPosition();
				}
				if(getInput().inputPressed(CAM_RIGHT)) {
					playground.getCamera().setPosition(cam_pos.x+cam_speed*delta, cam_pos.y);
					cam_pos = playground.getCamera().getPosition();
				}
				if(getInput().inputPressed(CAM_UP)) {
					playground.getCamera().setPosition(cam_pos.x, cam_pos.y+cam_speed*delta);
					cam_pos = playground.getCamera().getPosition();
				}
				if(getInput().inputPressed(CAM_DOWN)) {
					playground.getCamera().setPosition(cam_pos.x, cam_pos.y-cam_speed*delta);
				}
				
				
				if(getInput().inputJustPressed(move_up)) {
					debugEnabled(!getDebugEnabled());
					if(Gdx.app.getLogLevel()!=Application.LOG_NONE)
						Gdx.app.setLogLevel(Application.LOG_NONE);
					else
						Gdx.app.setLogLevel(Application.LOG_INFO);
				}
				
				if(getInput().inputJustPressed(move_down)) {

					counter = 0;
					for (int i = 0; i < objects.size; i++) {
						objects.get(i).remove();
					}
					test_objects.freeAll(objects);

					objects.clear();
				}
				
				string.setLength(0);
				string.append(object_head);
				string.append(playground.getChildren().size);
				stats.setText(string);
			}
			
			@Override
			public void addLayerSystems(GameState state) {
				objects = new Array<BasicGameObject>(2000);
				counter = 0;
				mouse_pos = new Vector2();
				pooperties = new MapProperties();
				pooperties.put("sprite", "windowbackground");
				quadtree = new QuadTree(0, 0, getView().getWorldWidth(), getView().getWorldHeight());
				quadtree.setFollowView(true);
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
		debugEnabled(true);
		setGameState("collisiontest.tmx");
		
	}

}
