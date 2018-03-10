package com.kyperbox.managers;

import com.kyperbox.GameState;
import com.kyperbox.umisc.UserData;

public abstract class StateManager {
	
	private GameState state;
	
	public void provideGameState(GameState state) {
		this.state = state;
	}

	public GameState getState() {
		return state;
	}
	
	public UserData getStateData() {return state.getData();}
	
	/**
	 * called before all layer data is instantiated
	 * perfect time to add layer_managers
	 */
	public abstract void addLayerSystems(GameState state);
	
	/**
	 * called when this state is set
	 */
	public abstract void init(GameState state);
	
	/**
	 * called every update
	 */
	public abstract void update(GameState state, float delta);
	
	/**
	 * called when the 
	 * @param game
	 */
	public abstract void dispose(GameState state);
}
