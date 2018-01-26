package com.kyperbox.ztests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kyperbox.GameState;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.managers.StateManager;
import com.kyperbox.yarn.Dialogue;
import com.kyperbox.yarn.Dialogue.LineResult;
import com.kyperbox.yarn.Dialogue.NodeCompleteResult;
import com.kyperbox.yarn.Dialogue.OptionResult;
import com.kyperbox.yarn.Dialogue.RunnerResult;
import com.kyperbox.yarn.Dialogue.YarnLogger;

public class YarnTests extends KyperBoxGame {
	
	Dialogue dialogue;
	String file_path = "example.json";
	RunnerResult current_result = null;
	Label dialogue_label;
	TextButton next,optionA,optionB;
	OptionResult r;
	private boolean waiting;
	private boolean complete = false;
	
	@Override
	public void initiate() {
		dialogue = new Dialogue(getGlobals());
		waiting = false;
		
		dialogue.debug_logger = new YarnLogger() {
			@Override
			public void log(String message) {
				Gdx.app.log("",String.format("YarnLog: %s", message));
			}
		};
		
		dialogue.error_logger = new YarnLogger() {
			@Override
			public void log(String message) {
				error("YarnError:", message);
			}
		};
		
		dialogue.loadFile(file_path,  false, true, null);
		
		
		
		registerGameState("yarn_test.tmx", new StateManager() {
			
			@Override
			public void update(GameState state, float delta) {
				
				if(!complete)
				dialogue.update();
				
				if(current_result ==  null)
					current_result = dialogue.getNextResult();
				
				if(current_result!=null) {
					if(current_result instanceof LineResult && !waiting) {
						
						LineResult line_result = (LineResult) current_result;
						log("Yarn", line_result.line.getText());
						dialogue_label.setText(line_result.line.getText());
						next.setVisible(true);
						//current_result = dialogue.getNextResult();
						
					}
					
					if(current_result instanceof OptionResult) {
						OptionResult op_result = (OptionResult) current_result;
						next.setVisible(false);
						if(op_result.options.getOptions().size > 0) {
							optionA.setText(op_result.options.getOptions().first());
							optionA.setVisible(true);
						}
						if(op_result.options.getOptions().size > 1) {
							optionB.setText(op_result.options.getOptions().get(1));
							optionB.setVisible(true);
						}
					}
					
					if(current_result instanceof NodeCompleteResult) {
						complete = true;
					}
					
					waiting = true;
				}
			}
			
			@Override
			public void init(GameState state) {
				dialogue_label = (Label) state.getUiLayer().getActor("diag");
				next = (TextButton) state.getUiLayer().getActor("next");
				next.addListener(new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						waiting = false;
						next.setVisible(false);
						current_result = null;
					}
				});
				
				optionA = (TextButton) state.getUiLayer().getActor("option1");
				optionA.addListener(new ClickListener() {@Override
				public void clicked(InputEvent event, float x, float y) {
					OptionResult option= (OptionResult) current_result;
					option.chooser.choose(0);
					optionA.setVisible(false);
					optionB.setVisible(false);
					current_result = null;
					waiting = false;
				}});
				optionB = (TextButton) state.getUiLayer().getActor("option2");
				optionB.addListener(new ClickListener() {@Override
				public void clicked(InputEvent event, float x, float y) {
					OptionResult option= (OptionResult) current_result;
					option.chooser.choose(1);
					optionA.setVisible(false);
					optionB.setVisible(false);
					current_result = null;
					waiting = false;
				}});
				
				optionA.setVisible(false);
				optionB.setVisible(false);
				
				next.setVisible(false);
				dialogue.run();
			}
			
			@Override
			public void dispose(GameState state) {
				
			}
			
			@Override
			public void addLayerSystems(GameState state) {
				
			}
		});
		
		setGameState("yarn_test.tmx");
	}

}
