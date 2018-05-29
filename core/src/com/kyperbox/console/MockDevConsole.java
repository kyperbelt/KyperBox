package com.kyperbox.console;

import com.badlogic.gdx.InputMultiplexer;
import com.kyperbox.KyperBoxGame;

/**
 * fake dev console to avoid having to put nullchecks everywhere
 * @author john
 *
 */
public class MockDevConsole implements IDevConsole{

	private static final String TAG = "MockDEVConsole";
	
	KyperBoxGame game;
	
	@Override
	public void create(KyperBoxGame game) {
		this.game = game;
		log("Initiated");
	}

	@Override
	public void log(String message) {
		KyperBoxGame.log(TAG, message);
	}

	@Override
	public void error(String message) {
		KyperBoxGame.error(TAG, message);
	}

	@Override
	public KyperBoxGame getGame() {
		return game;
	}

	@Override
	public void addToMultiplexer(InputMultiplexer input) {
		
	}

	@Override
	public void updateSize(int width, int height) {
		
	}

	@Override
	public void consoleDraw() {
		
	}

	@Override
	public void consoleUpdate(float delta) {
		
	}

	@Override
	public void open() {
		
	}

	@Override
	public void close() {
		
	}

	@Override
	public void clearConsole() {
		
	}

	@Override
	public void addCommand(ConsoleCommand command) {
		
	}

	@Override
	public void removeCommand(String command) {
		
	}

	@Override
	public void executeCommand(String command) {
		
	}

	@Override
	public ConsoleCommand getCommand(String command) {
		return null;
	}

	@Override
	public boolean isOpen() {
		return false;
	}

	@Override
	public void dispose() {
		
	}

}
