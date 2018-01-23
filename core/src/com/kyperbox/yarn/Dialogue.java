package com.kyperbox.yarn;

import com.badlogic.gdx.utils.Array;

public class Dialogue {

	private VariableStorage continuity;
	
	/**
	 * indicates something the client should do
	 */
	public abstract class RunnerResult{}
	
	/**
	 * the client should run a line of dialogue
	 */
	public class LineResult extends RunnerResult{
		public Line line;
		public LineResult(String text) {
			line = new Line(text);
		}
	}
	
	/**
	 * client should run and parse command
	 */
	public class CommandResult extends RunnerResult{
		public Command command;
		public CommandResult(String text) {
			command = new Command(text);
		}
	}
	
	/**
	 * Client should show a list of options and call
	 * the chooser choose before asking for the next line. 
	 */
	public class OptionResult extends RunnerResult{
		public Options options;
		public OptionChooser chooser;
		public OptionResult(Array<String> options,OptionChooser chooser) {
			this.chooser = chooser;
			this.options = new Options(options);
		}
	}
	
	/**
	 * end of node reached
	 */
	public class NodeCompleteResult extends RunnerResult{
		public String next_node;
	}
	
	public YarnLogger debug_logger;
	public YarnLogger error_logger;
	
	//node we start from
	public static final String DEFAULT_START = "Start";
	
	
	
//	======================================================================================
	/**
	 * something went wrong
	 *
	 */
	public class YarnRuntimeException extends RuntimeException {
		private static final long serialVersionUID = -5732778106783039900L;

		public YarnRuntimeException(String message) {
			super(message);
		}

		public YarnRuntimeException(Throwable t) {
			super(t);
		}

		public YarnRuntimeException(String message, Throwable t) {
			super(message, t);
		}

	}

	/**
	 * option chooser lets client tell dialogue the response selected by the user
	 */
	public interface OptionChooser {
		public void choose(int selected_option_index);
	}

	/**
	 * logger to let the client send output to the console logging/error logging
	 */
	public interface YarnLogger {
		public void log(String message);

		public void error(String message);
	}

	/**
	 * ]
	 * 
	 * information that the client should handle
	 */
	public class Line {
		private String text;

		public Line(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}
	}

	public class Options {
		private Array<String> options;

		public Options(Array<String> options) {
			this.options = options;
		}

		public Array<String> getOptions() {
			return options;
		}

		public void setOptions(Array<String> options) {
			this.options = options;
		}
	}

	public class Command {
		private String command;

		public Command(String command) {
			this.command = command;
		}

		public String getCommand() {
			return command;
		}

		public void setCommand(String command) {
			this.command = command;
		}
	}

	/**
	 * variable storage TODO: try to use {@link com.kyperbox.util.UserData UserData}
	 */
	public interface VariableStorage {
		public void setValue(String name, Object value);

		public <t> t getValue(String name, Class<t> type);

		public void clear();
	}

	/**
	 * a line localized into the current locale that is used in lines, options and
	 * shortcut options. Anything that is user-facing.
	 */
	public class LocalisedLine {
		private String code;
		private String text;
		private String comment;

		public LocalisedLine(String code, String text, String comment) {
			this.code = code;
			this.text = text;
			this.comment = comment;
		}

		public String getCode() {
			return code;
		}

		public String getText() {
			return text;
		}

		public String getComment() {
			return comment;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public void setText(String text) {
			this.text = text;
		}

		public void setComment(String comment) {
			this.comment = comment;
		}
	}
	
}
