package com.kyperbox.ztests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kyperbox.GameState;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.managers.StateManager;
import com.kyperbox.yarn.Dialogue;
import com.kyperbox.yarn.Dialogue.LineResult;
import com.kyperbox.yarn.Dialogue.NodeCompleteResult;
import com.kyperbox.yarn.Dialogue.OptionResult;
import com.kyperbox.yarn.Dialogue.YarnLogger;

public class YarnTests extends KyperBoxGame {
	
	Dialogue dialogue;
	String file_path = "example.json";
	OptionResult current_options= null;
	LineResult current_line;
	Label dialogue_label;
	TextButton next,optionA,optionB;
	OptionResult r;
	boolean running = true;
	
	@Override
	public void initiate() {
		dialogue = new Dialogue(getGlobals());
		
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
				if(running)
					dialogue.update();
				
				if(dialogue.hasNext()) {
					if(dialogue.checkNext() instanceof LineResult && dialogue.checkNext()!=current_line) {
						current_line = dialogue.checkNext(LineResult.class);
						dialogue_label.setText(current_line.line.getText());
						
						//check to see if we have an option coming up
						if(dialogue.optionsAvailable()) {
							//remove line from stack to get options
							dialogue.getNext();
							current_options = dialogue.getNext(OptionResult.class);
							
							//populate option text
							Array<String> ops = current_options.options.getOptions();
							optionA.setText(ops.first());
							if(ops.size > 1)
								optionB.setText(ops.get(1));
							
							//show options
							optionA.setVisible(true);
							optionB.setVisible(true);
							
							
						}else {
							next.setVisible(true);
						}
						
					}else if(dialogue.checkNext() instanceof NodeCompleteResult) {

						//node is complete and there is more pop it off check if its the last node
						if(dialogue.getNext(NodeCompleteResult.class).next_node == null) {
							System.out.println("complete");
							running = false;
						}
					}
				}
				
			}
			
			@Override
			public void init(GameState state) {
				
				dialogue_label = (Label) state.getUiLayer().getActor("diag");
				next = (TextButton) state.getUiLayer().getActor("next");
				next.addListener(new ClickListener() {
					@Override
					public void clicked(InputEvent event, float x, float y) {
						//NEXT button!
						//pop the line and set the next to false
						if(dialogue.checkNext() == current_line)
							dialogue.getNext();
						next.setVisible(false);
					}
				});
				
				optionA = (TextButton) state.getUiLayer().getActor("option1");
				optionA.addListener(new ClickListener() {@Override
				public void clicked(InputEvent event, float x, float y) {
					current_options.chooser.choose(0);
					optionA.setVisible(false);
					optionB.setVisible(false);
				}});
				optionB = (TextButton) state.getUiLayer().getActor("option2");
				optionB.addListener(new ClickListener() {@Override
				public void clicked(InputEvent event, float x, float y) {
					current_options.chooser.choose(1);
					optionA.setVisible(false);
					optionB.setVisible(false);
				}});
				
				
				//disable all buttons
				
				optionA.setVisible(false);
				optionB.setVisible(false);
				
				next.setVisible(false);
				
				//start dialogue
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
