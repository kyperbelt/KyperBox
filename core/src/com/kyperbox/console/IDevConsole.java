package com.kyperbox.console;

import com.badlogic.gdx.InputMultiplexer;
import com.kyperbox.KyperBoxGame;

public interface IDevConsole {
	
	public void create(KyperBoxGame game);
	
	public void log(String message);
	
	public void error(String message);
	
	public KyperBoxGame getGame();
	
	public void addToMultiplexer(InputMultiplexer input);
	
	public void updateSize(int width, int height);
	
	public void consoleDraw();
	
	public void consoleUpdate(float delta);
	
	public void open();
	
	public void close();
	
	public void clearConsole();
	
	public void addCommand(ConsoleCommand command);
	
	public void removeCommand(String command);
	
	public void executeCommand(String command);
	
	public ConsoleCommand getCommand(String command);
	
	public boolean isOpen();
	
	public void dispose();
	
	
}
