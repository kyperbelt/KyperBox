package com.kyperbox.ztests;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.kyperbox.GameState;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.controllers.ParticleController;
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
			public void update(float delta) {
				
				if(getInput().inputJustPressed(InputDefaults.ENTER)) {
					cam.setZoom(Math.max(.1f, cam.getZoom()-.1f));
				}
				
				if(getInput().inputJustPressed(InputDefaults.EXIT)) {
					cam.setZoom(Math.min(10f, cam.getZoom()+.1f));
				}
				
				if(getInput().inputJustPressed(InputDefaults.ACTION_BUTTON) && torch_fire.isRemoved()) {
					torch_fire.reset();
					torch.addController(torch_fire);
				}
				
				if(getInput().inputJustPressed(InputDefaults.JUMP_BUTTON)) {
					setGameState("tile_collision.tmx");
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
				
				
				torch_fire = new ParticleController("fire");
				
				torch_fire.setRelativePos(torch.getOriginX(), torch.getHeight()*.75f);
				torch_fire.setDuration(10f);
				torch.addController(torch_fire);
				
				ParticleController torch_smoke = new ParticleController("smoke");
				torch_smoke.setRelativePos(torch_fire.getRelativeX(), torch_fire.getRelativeY());
				BasicGameObject smoke = new BasicGameObject();
				smoke.addController(torch_smoke);
				torch.addChild(smoke);
				
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
			public void update(float delta) {
				if(getInput().inputJustPressed(InputDefaults.JUMP_BUTTON)) {
					setGameState("particles.tmx");
				}
			}
		});
		
		//debugRender(true);
		setGameState("particles.tmx");
		
	}

}
