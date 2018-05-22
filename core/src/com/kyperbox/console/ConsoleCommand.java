package com.kyperbox.console;

public class ConsoleCommand {
	
	private String command;
	private String info;
	private CommandRunnable run;
	private int params;
	
	/**
	 * create a console command
	 * @param command - the name of the command and the word used to invoke it
	 * @param params - the number of parameters
	 * @param info - the information for when displaying help
	 * @param command_runnable - the command runnable to execute when the command is called
	 */
	public ConsoleCommand(String command,int params,String info,CommandRunnable command_runnable) {
		this.command = command;
		this.params = params;
		this.info = info;
		this.run = command_runnable;
		if(run == null) {
			throw new IllegalArgumentException("CommandRunnable for command:["+command+"] cannot be null");
		}
	}
	
	
	public String getCommand() {
		return command;
	}
	
	public int getParamCount() {
		return params;
	}
	
	public String getInfo() {
		return info;
	}
	
	public boolean execute(DevConsole console,String...args) {
		return run.executeCommand(console, args);
	}
	
	
}
