package com.kyperbox.ztests;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.kyperbox.GameState;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.controllers.ParticleController;
import com.kyperbox.controllers.ParticleController.ParticleSettings;
import com.kyperbox.input.InputDefaults;
import com.kyperbox.input.KeyboardMapping;
import com.kyperbox.managers.StateManager;
import com.kyperbox.managers.StateManagerAdapter;
import com.kyperbox.objects.BasicGameObject;
import com.kyperbox.objects.GameLayer.LayerCamera;
import com.kyperbox.systems.ParticleSystem;

public class ParticleTests extends KyperBoxGame{

	@Override
	public void initiate() {
		debugEnabled(true);
		
		getInput().addInputMapping(InputDefaults.EXIT, new KeyboardMapping(Keys.Z));
		getInput().addInputMapping(InputDefaults.ENTER, new KeyboardMapping(Keys.X));
		getInput().addInputMapping(InputDefaults.ACTION_BUTTON, new KeyboardMapping(Keys.SPACE));
		getInput().addInputMapping(InputDefaults.JUMP_BUTTON, new KeyboardMapping(Keys.W));
		
		registerGameState("particles.tmx", new StateManager() {
			
			private LayerCamera cam;
			private BasicGameObject torch;
			private Vector2 mouse_pos = new Vector2();
			private ParticleController torch_fire;
			
			@Override
			public void update(GameState state,float delta) {
				
				if(getInput().inputJustPressed(InputDefaults.ENTER)) {
					cam.setZoom(Math.max(.1f, cam.getZoom()-.1f));
				}
				
				if(getInput().inputJustPressed(InputDefaults.EXIT)) {
					cam.setZoom(Math.min(10f, cam.getZoom()+.1f));
				}
				
				if(getInput().inputJustPressed(InputDefaults.ACTION_BUTTON) ) {

				}
				
				if(getInput().inputJustPressed(InputDefaults.JUMP_BUTTON)) {
					transitionTo("tile_collision.tmx",1f,0);
				}
				
				float mx = getInput().getX();
				float my = getInput().getY();
				mouse_pos.set(mx, my);
				cam.unproject(mouse_pos);
				torch.setPosition(mouse_pos.x-(torch.getWidth()*.5f), mouse_pos.y-(torch.getHeight()*.25f));
			}
			
			@Override
			public void init(GameState state) {
				torch = (BasicGameObject) state.getPlaygroundLayer().getGameObject("torch");
				
				state.setTimeScale(2f);
				
				torch_fire = new ParticleController();
				ParticleSettings ps = new ParticleSettings();
				ps.scale = 1.2f;
				ps.xoff = torch.getWidth()*.5f;
				ps.yoff = torch.getHeight()*.7f;
				torch_fire.addParticleEffect(state.getEffect("fire"),ps);
				
				torch.addController(torch_fire);
				
				
				cam = state.getPlaygroundLayer().getCamera();
				cam.setCentered();
				cam.setPosition(200, 200);
				
				float mx = getInput().getX();
				float my = getInput().getY();
				mouse_pos.set(mx, my);
				cam.unproject(mouse_pos);
				torch.setPosition(mouse_pos.x-(torch.getWidth()*.5f), mouse_pos.y-(torch.getHeight()*.25f));
			}
			
			@Override
			public void dispose(GameState state) {
				
				torch = null;
				torch_fire = null;
				
				state.unloadFonts();
				state.unloadParticles();
				
			}
			
			@Override
			public void addLayerSystems(GameState state) {
				state.getPlaygroundLayer().addLayerSystem(new ParticleSystem());
			}
		});
		
		registerGameState("tile_collision.tmx",new StateManagerAdapter() {
			@Override
			public void update(GameState state,float delta) {
				if(getInput().inputJustPressed(InputDefaults.JUMP_BUTTON)) {
					transitionTo("particles.tmx",.8f,1);
				}
			}
		});
		
		//debugRender(true);
		setGameState("particles.tmx");
		
	}

}
