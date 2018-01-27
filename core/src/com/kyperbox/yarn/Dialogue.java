package com.kyperbox.yarn;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.kyperbox.yarn.Analyser.Context;
import com.kyperbox.yarn.Lexer.TokenType;
import com.kyperbox.yarn.Library.ReturningFunc;
import com.kyperbox.yarn.Loader.NodeFormat;
import com.kyperbox.yarn.Program.LineInfo;
import com.kyperbox.yarn.VirtualMachine.CommandHandler;
import com.kyperbox.yarn.VirtualMachine.ExecutionState;
import com.kyperbox.yarn.VirtualMachine.LineHandler;
import com.kyperbox.yarn.VirtualMachine.NodeCompleteHandler;
import com.kyperbox.yarn.VirtualMachine.OptionsHandler;

public class Dialogue {

	protected VariableStorage continuity;

	public YarnLogger debug_logger;
	public YarnLogger error_logger;

	//node we start from
	public static final String DEFAULT_START = "Start";

	//loader contains all the nodes we're going to run
	protected Loader loader;

	//the program is the compiled yarn program
	protected Program program;

	//the library contains all the functions and operators we know about
	public Library library;

	private VirtualMachine vm;

	//collection of nodes that we've seen
	public ObjectMap<String, Integer> visited_node_count = new ObjectMap<String, Integer>();

	public Dialogue(VariableStorage continuity) {
		this.continuity = continuity;
		loader = new Loader(this);
		library = new Library();

		library.importLibrary(new StandardLibrary());

	}

	public void loadString(String text, String file_name, boolean show_tokens, boolean show_tree,
			String only_consider) {
		if (debug_logger == null) {
			throw new YarnRuntimeException("DebugLogger must be set before loading");
		}

		if (error_logger == null)
			throw new YarnRuntimeException("ErrorLogger must be set before loading");

		//try to infer type
		NodeFormat format;
		if (text.startsWith("[")) {
			format = NodeFormat.Json;
		} else if (text.contains("---")) {
			format = NodeFormat.Text;
		} else {
			format = NodeFormat.SingleNodeText;
		}

		program = loader.load(text, library, file_name, program, show_tokens, show_tree, only_consider, format);
	}

	public void loadFile(String file, boolean show_tokens, boolean show_tree, String only_consider) {
		String input = Gdx.files.internal(file).readString();

		loadString(input, file, show_tokens, show_tree, only_consider);
	}

	/**
	 * Start a thread that spits out results waits for results to be consumed
	 */
	private Array<RunnerResult> results;

	public boolean run(String start) {
		if (results == null)
			results = new Array<Dialogue.RunnerResult>();
		results.clear();

		if (debug_logger == null) {
			throw new YarnRuntimeException("debug_logger must be set before running");
		}

		if (error_logger == null) {
			throw new YarnRuntimeException("error_logger must be set before running");
		}

		if (program == null) {
			error_logger.log("Dialogue.run was called but no program was loaded.");
			return false;
		}

		vm = new VirtualMachine(this, program);

		vm.setLineHandler(new LineHandler() {
			@Override
			public void handle(LineResult line) {
				results.insert(0, line);
			}
		});

		vm.setCommandHandler(new CommandHandler() {
			@Override
			public void handle(CommandResult command) {
				//if stop
				if (command.command.getCommand().equals("stop")) {
					vm.stop();
				}
				results.insert(0, command);
			}
		});

		vm.setCompleteHandler(new NodeCompleteHandler() {
			@Override
			public void handle(NodeCompleteResult compelte) {
				if (vm.currentNodeName() != null) {
					int count = 0;
					if (visited_node_count.containsKey(vm.currentNodeName()))
						count = visited_node_count.get(vm.currentNodeName());

					visited_node_count.put(vm.currentNodeName(), count + 1);
				}
				results.insert(0, compelte);
			}
		});

		vm.setOptionsHandler(new OptionsHandler() {
			@Override
			public void handle(OptionResult options) {
				results.insert(0, options);
			}
		});

		if (!vm.setNode(start)) {
			return false;
		}

		return true;
	}

	public boolean run() {
		return run(DEFAULT_START);
	}

	/**
	 * update the virtual machine counter.
	 * 
	 */
	public void update() {
		if (vm.getExecutionState() != ExecutionState.WaitingOnOptionSelection
				&& vm.getExecutionState() != ExecutionState.Stopped)
			vm.runNext();
	}

	//CHECK FUNCS
	public boolean hasNext() {
		return checkNext(0) != null;
	}

	public boolean optionsAvailable() {
		return checkNext(1) != null && checkNext(1) instanceof OptionResult;
	}

	/**
	 * offset from the end of the results stack
	 * 
	 * @param offset
	 * @return
	 */
	public RunnerResult checkNext(int offset) {
		return results.size - Math.abs(offset) <= 0 ? null : results.get(results.size - 1 - Math.abs(offset));
	}

	public RunnerResult checkNext() {
		return checkNext(0);
	}

	public <t> t checkNext(Class<t> type) {
		return type.cast(checkNext(0));
	}

	public RunnerResult getNext() {
		return results.size == 0 ? null : results.pop();
	}

	public <t> t getNext(Class<t> type) {
		return type.cast(getNext());
	}

	public void stop() {
		if (vm != null)
			vm.stop();

	}

	public Array<String> allNodes() {
		return program.nodes.keys().toArray();
	}

	public String currentNode() {
		return vm == null ? null : vm.currentNodeName();
	}

	ObjectMap<String, String> _tx4n;

	public ObjectMap<String, String> getTextForAllNodes() {
		if (_tx4n == null)
			_tx4n = new ObjectMap<String, String>();
		_tx4n.clear();
		for (Entry<String, Program.Node> entry : program.nodes) {
			String text = program.getTextForNode(entry.key);

			if (text == null)
				continue;

			_tx4n.put(entry.key, text);
		}

		return _tx4n;
	}

	/**
	 * get the source code for the node
	 * 
	 * @param node
	 * @return
	 */
	public String getTextForNode(String node) {
		if (program.nodes.size == 0) {
			error_logger.log("no nodes are loaded!");
			return null;
		} else if (program.nodes.containsKey(node)) {
			return program.getTextForNode(node);
		} else {
			error_logger.log("no node named " + node);
			return null;
		}
	}

	public void addStringTable(ObjectMap<String, String> string_table) {
		program.loadStrings(string_table);
	}

	public ObjectMap<String, String> getStringTable() {
		return program.strings;
	}

	protected ObjectMap<String, LineInfo> getStringInfoTable() {
		return program.line_info;
	}

	/**
	 * unload all nodes
	 * 
	 * @param clear_visisted_nodes
	 */
	public void unloadAll(boolean clear_visisted_nodes) {
		if (clear_visisted_nodes)
			visited_node_count.clear();
		program = null;
	}

	protected String getByteCode() {
		return program.dumpCode(library);
	}

	public boolean nodeExists(String node_name) {
		if (program == null) {
			error_logger.log("no nodes compiled");
			return false;
		}
		if (program.nodes.size == 0) {
			error_logger.log("no nodes in program");
			return false;
		}

		return program.nodes.containsKey(node_name);

	}

	public void analyse(Context context) {
		context.addProgramToAnalysis(program);
	}

	public Array<String> getvisitedNodes() {
		return visited_node_count.keys().toArray();
	}

	public void setVisitedNodes(Array<String> visited) {
		visited_node_count.clear();
		for (String string : visited) {
			visited_node_count.put(string, 1);
		}
	}

	/**
	 * unload all nodes clears visited nodes
	 */
	public void unloadAll() {
		unloadAll(true);
	}

	public enum CompiledFormat {
		V1
	}

	/**
	 * A function exposed to yarn that returns the number of times a node has been
	 * run. if no parameters are supplied, returns the number of times the current
	 * node has been run.
	 */
	protected ReturningFunc yarnFunctionNodeVisitCount = new ReturningFunc() {
		@Override
		public Object invoke(Value... params) {

			//determin ethe node were checking 
			String node_name;

			if (params.length == 0) {
				//no marams? check the current node
				node_name = vm.currentNodeName();
			} else if (params.length == 1) {
				//a parameter? check the named node
				node_name = params[0].asString();
				//ensure node existance
				if (!nodeExists(node_name)) {
					String error = String.format(" the node %s does not exist.", node_name);
					error_logger.log(error);
					return 0;
				}
			} else {
				//we go ttoo many parameters
				String error = String.format("incorrect number of parameters visitcount expect 0 or 1, got %s",
						params.length);
				error_logger.log(error);
				return 0;
			}
			int visit_count = 0;
			if (visited_node_count.containsKey(node_name))
				visit_count = visited_node_count.get(node_name);
			return visit_count;
		}
	};

	protected ReturningFunc yarnFunctionIsNodeVisited = new ReturningFunc() {
		@Override
		public Object invoke(Value... params) {
			return (Integer) yarnFunctionNodeVisitCount.invoke(params) > 0;
		}
	};

	//	======================================================================================

	/**
	 * indicates something the client should do
	 */
	public static abstract class RunnerResult {
		//		private boolean consumed = false;
		//		public void consume() {consumed = true;}
		//		private boolean isConsumed() {return consumed;}
	}

	/**
	 * the client should run a line of dialogue
	 */
	public static class LineResult extends RunnerResult {
		public Line line;

		public LineResult(String text) {
			line = new Line(text);
		}
	}

	/**
	 * client should run and parse command
	 */
	public static class CommandResult extends RunnerResult {
		public Command command;

		public CommandResult(String text) {
			command = new Command(text);
		}
	}

	/**
	 * Client should show a list of options and call the chooser choose before
	 * asking for the next line.
	 */
	public static class OptionResult extends RunnerResult {
		public Options options;
		public OptionChooser chooser;

		public OptionResult(Array<String> options, OptionChooser chooser) {
			this.chooser = chooser;
			this.options = new Options(options);
		}
	}

	/**
	 * end of node reached
	 */
	public static class NodeCompleteResult extends RunnerResult {
		public String next_node;

		public NodeCompleteResult(String next_node) {
			this.next_node = next_node;
		}
	}

	/**
	 * something went wrong
	 *
	 */
	public static class YarnRuntimeException extends RuntimeException {
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
	public static interface OptionChooser {
		public void choose(int selected_option_index);
	}

	/**
	 * logger to let the client send output to the console logging/error logging
	 */
	public static interface YarnLogger {
		public void log(String message);
	}

	/**
	 * 
	 * 
	 * information that the client should handle
	 */
	public static class Line {
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

	public static class Options {
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

	public static class Command {
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
	public static interface VariableStorage {
		public void setValue(String name, Value value);

		public Value getValue(String name);

		public void clear();
	}

	public static abstract class BaseVariableStorage implements VariableStorage {

	}

	public static class MemoryVariableStorage extends BaseVariableStorage {

		ObjectMap<String, Value> variables = new ObjectMap<String, Value>();

		@Override
		public void setValue(String name, Value value) {
			variables.put(name, value);
		}

		@Override
		public Value getValue(String name) {
			Value value = Value.NULL;
			if (variables.containsKey(name))
				value = variables.get(name);
			return value;
		}

		@Override
		public void clear() {
			variables.clear();
		}

	}

	/**
	 * a line localized into the current locale that is used in lines, options and
	 * shortcut options. Anything that is user-facing.
	 */
	public static class LocalisedLine {
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

	/**
	 * the standrad built in lib of functions and operators
	 */
	private static class StandardLibrary extends Library {

		public StandardLibrary() {
			//operations

			registerFunction(TokenType.Add.name(), 2, new ReturningFunc() {
				@Override
				public Object invoke(Value... params) {
					return params[0].add(params[1]);
				}
			});

			registerFunction(TokenType.Minus.name(), 2, new ReturningFunc() {
				@Override
				public Object invoke(Value... params) {
					return params[0].sub(params[1]);
				}
			});

			registerFunction(TokenType.UnaryMinus.name(), 1, new ReturningFunc() {
				@Override
				public Object invoke(Value... params) {
					return params[0].negative();
				}
			});

			registerFunction(TokenType.Divide.name(), 2, new ReturningFunc() {
				@Override
				public Object invoke(Value... params) {
					return params[0].div(params[1]);
				}
			});

			registerFunction(TokenType.Multiply.name(), 2, new ReturningFunc() {
				@Override
				public Object invoke(Value... params) {
					return params[0].mul(params[1]);
				}
			});

			registerFunction(TokenType.Modulo.name(), 2, new ReturningFunc() {
				@Override
				public Object invoke(Value... params) {
					return params[0].mod(params[1]);
				}
			});

			registerFunction(TokenType.EqualTo.name(), 2, new ReturningFunc() {
				@Override
				public Object invoke(Value... params) {
					return params[0].equals(params[1]);
				}
			});

			registerFunction(TokenType.NotEqualTo.name(), 2, new ReturningFunc() {
				@Override
				public Object invoke(Value... params) {
					return !params[0].equals(params[1]);
				}
			});

			registerFunction(TokenType.GreaterThan.name(), 2, new ReturningFunc() {
				@Override
				public Object invoke(Value... params) {
					return params[0].greaterThan(params[1]);
				}
			});

			registerFunction(TokenType.GreaterThanOrEqualTo.name(), 2, new ReturningFunc() {
				@Override
				public Object invoke(Value... params) {
					return params[0].greaterThanOrEqual(params[1]);
				}
			});

			registerFunction(TokenType.LessThan.name(), 2, new ReturningFunc() {
				@Override
				public Object invoke(Value... params) {
					return params[0].lessThan(params[1]);
				}
			});

			registerFunction(TokenType.LessThanOrEqualTo.name(), 2, new ReturningFunc() {
				@Override
				public Object invoke(Value... params) {
					return params[0].lessThanOrEqual(params[1]);
				}
			});

			registerFunction(TokenType.And.name(), 2, new ReturningFunc() {
				@Override
				public Object invoke(Value... params) {
					return params[0].asBool() && params[1].asBool();
				}
			});

			registerFunction(TokenType.Or.name(), 2, new ReturningFunc() {
				@Override
				public Object invoke(Value... params) {
					return params[0].asBool() || params[1].asBool();
				}
			});

			registerFunction(TokenType.Xor.name(), 2, new ReturningFunc() {
				@Override
				public Object invoke(Value... params) {
					return params[0].asBool() ^ params[1].asBool();
				}
			});

			registerFunction(TokenType.Not.name(), 1, new ReturningFunc() {
				@Override
				public Object invoke(Value... params) {
					return !params[0].asBool();
				}
			});

			//end operations ===

		}
	}

}
