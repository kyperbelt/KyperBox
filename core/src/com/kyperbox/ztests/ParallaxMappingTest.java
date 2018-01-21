package com.kyperbox.ztests;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.kyperbox.GameState;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.input.InputDefaults;
import com.kyperbox.input.KeyboardMapping;
import com.kyperbox.managers.StateManager;
import com.kyperbox.objects.GameLayer;
import com.kyperbox.systems.ParallaxMapper;

public class ParallaxMappingTest extends KyperBoxGame{

	public static final String PARALLAX_TMX = "parallax_test.tmx";
	
	public ParallaxMappingTest() {
		super(new FillViewport(1280,720));
	}

	@Override
	public void initiate() {
		
		registerGameState(PARALLAX_TMX,new StateManager() {
			
			ParallaxMapper parallax;
			GameLayer playground;
			private float cam_speed = 250f;
			
			@Override
			public void update(GameState state,float delta) {
				if(getInput().inputPressed(InputDefaults.MOVE_LEFT)) {
					Vector2 cam_pos = playground.getCamera().getPosition();
					playground.getCamera().setPosition(cam_pos.x-cam_speed*delta, cam_pos.y);
				}
				
				if(getInput().inputPressed(InputDefaults.MOVE_RIGHT)) {
					Vector2 cam_pos = playground.getCamera().getPosition();
					playground.getCamera().setPosition(cam_pos.x+cam_speed*delta, cam_pos.y);
				}
			}
			
			@Override
			public void addLayerSystems(GameState state) {
				parallax = new ParallaxMapper(state.getPlaygroundLayer());
				parallax.addMapping("trees", .9f,0f, true);
				parallax.addMapping("mountains", .6f,0f, true);
				parallax.addMapping("mountain_far", .2f,0f, true);
				parallax.addMapping("parallax_back", 0,0f, true);
				state.getBackgroundLayer().addLayerSystem(parallax);
				
			}
			
			@Override
			public void init(GameState state) {
				playground = state.getPlaygroundLayer();
				getInput().addInputMapping(InputDefaults.MOVE_LEFT, new KeyboardMapping(Keys.LEFT));
				getInput().addInputMapping(InputDefaults.MOVE_RIGHT, new KeyboardMapping(Keys.RIGHT));
				
		
				
			}
			
			@Override
			public void dispose(GameState state) {
				
			}
		});
		
		setGameState(PARALLAX_TMX);
		
	}

}
