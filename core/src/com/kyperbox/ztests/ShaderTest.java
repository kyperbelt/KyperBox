package com.kyperbox.ztests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kyperbox.GameState;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.console.DevConsole;
import com.kyperbox.input.InputDefaults;
import com.kyperbox.input.KeyboardMapping;
import com.kyperbox.managers.StateManagerAdapter;
import com.kyperbox.objects.GameObject;
import com.kyperbox.objects.ShaderObject;

public class ShaderTest extends KyperBoxGame{

	public ShaderTest() {
		super(new FitViewport(1280, 720));
	}
	
	@Override
	public void initiate() {
		
		log("ShaderTest","helloworld");
		final GLProfiler profiler = new GLProfiler(Gdx.graphics);
		profiler.enable();
		
		registerGameState("shadertest.tmx", new StateManagerAdapter() {
			
			ShaderObject object;
			GameObject player;
			GameObject scrollingBackground;
			
			ShaderProgram flashbackshader;
			
			@Override
			public void addLayerSystems(GameState state) {
			
			}

			@Override
			public void init(GameState state) {
				object = (ShaderObject) state.getPlaygroundLayer().getGameObject("object");
				flashbackshader = state.getShader("flashbackShader");
				flashbackshader.begin();
				flashbackshader.setUniformf("u_resolution", getView().getWorldWidth(), getView().getWorldHeight());
				flashbackshader.end();
				state.setStateShader(flashbackshader);
			}
			
			@Override
			public void update(GameState state, float delta) {
				if(getInput().inputJustPressed(InputDefaults.JUMP_BUTTON)) {
					System.out.println("--------------------------");
					System.out.println("draw_calls       = "+profiler.getDrawCalls());
					System.out.println("shader_switching = "+profiler.getShaderSwitches());
					System.out.println("texture binding  = "+profiler.getTextureBindings());
					
					int size = object.getShaders().size;
					if(size > 0) {
						object.clearShaders();
						flashbackshader = state.getShader("flashbackShader");
						flashbackshader.begin();
						flashbackshader.setUniformf("u_resolution", getView().getWorldWidth(), getView().getWorldHeight());
						flashbackshader.end();
						state.setStateShader(flashbackshader);
					}else {
						object.addShader(state.getShader("grayscaleShader"));
						state.setStateShader(getDefaultShader());
						
					}
				}
				profiler.reset();
			}
			
			@Override
			public void dispose(GameState state) {

			}
			
			@Override
			public void resize(int width, int height) {
				System.out.println("resized");
				Viewport view = getView();
				flashbackshader.begin();
				flashbackshader.setUniformf("u_resolution", view.getWorldWidth(), view.getWorldHeight());
				flashbackshader.end();
			}
			
		});
		
		setGameState("shadertest.tmx");
		getInput().addInputMapping(InputDefaults.JUMP_BUTTON, new KeyboardMapping(Keys.SPACE));
		
	}

}
