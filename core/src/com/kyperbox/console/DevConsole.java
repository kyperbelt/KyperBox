package com.kyperbox.console;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kyperbox.GameState;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.objects.GameLayer;
import com.kyperbox.umisc.StringUtils;

public class DevConsole implements IDevConsole{

	public static float APPEAR_DURATION = .3f;

	private static final String EMPTY = "";

	private static final String NO_COMMAND_FOUND = "[YELLOW]%1$s[] is not a recognized command.";
	private static final String WRONG_PARAMETER_COUNT = "[SKY]%2$d[] is not enough parameters for [YELLOW]%1$s[] command. It should be [SKY]%3$d[].";
	private static final String FAILED_COMMAND = "[YELLOW]%1$s[] command failed to execute.";
	private static final String BLANK_PARAM = "One or more of your parameters was blank. Try to only use a single space as separator.";
	private static final String STATE_NOT_FOUND = "No current state was found.";
	
	//formats
	private static final String ERROR_FORMAT = "[RED]%s[RED]";

	private KyperBoxGame game;

	//--actors
	private BitmapFont font;
	private Stage stage;
	private Table console_table;
	private Table label_table;
	private TextField text_field;
	private ScrollPane scroll;
	private Array<Label> labels;
	private LabelStyle label_style;
	private TextFieldStyle textfield_style;

	private float fontscale = 1f;

	//--commands
	private Array<ConsoleCommand> console_commands;

	//--refs
	private String fontref;
	private String backgroundref;

	//--check_vars
	private boolean open = false;
	private int open_key;

	private Pool<Label> label_pool = new Pool<Label>() {
		protected Label newObject() {
			return new Label(EMPTY, label_style);
		};
	};

	/**
	 * create a dev console
	 * 
	 * @param font
	 * @param background
	 * @param key
	 */
	public DevConsole(String font, String background, int open_key) {
		this.fontref = font;
		this.backgroundref = background;
		this.open_key = open_key;
		console_commands = new Array<ConsoleCommand>();
		labels = new Array<Label>();
	}

	public void create(KyperBoxGame game) {
		this.game = game;
		stage = new Stage(new ScreenViewport());
		stage.addListener(new InputListener() {
			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				if (keycode == open_key) {
					return true;
				}

				return super.keyDown(event, keycode);
			}

			@Override
			public boolean keyUp(InputEvent event, int keycode) {
				if (open_key == keycode) {

					if (isOpen()) {
						close();
					} else {
						open();
					}
					return true;
				}
				return super.keyUp(event, keycode);
			}
		});
		game.loadFont(fontref);
		game.getAssetManager().load(backgroundref, Texture.class);
		game.getAssetManager().finishLoading();
		font = game.getFont(fontref);

		NinePatchDrawable background = new NinePatchDrawable(
				new NinePatch(game.getAssetManager().get(backgroundref, Texture.class), 4, 4, 4, 4));

		label_style = new LabelStyle(font, Color.WHITE);

		console_table = new Table();
		console_table.setBackground(background);
		console_table.setHeight(game.getView().getWorldHeight() * .6f);
		console_table.setPosition(0, 0);
		console_table.setWidth(game.getView().getWorldWidth());
		console_table.setTouchable(Touchable.enabled);

		label_table = new Table();
		label_table.align(Align.bottomLeft);

		scroll = new ScrollPane(label_table);
		scroll.setScrollingDisabled(true, false);

		textfield_style = new TextFieldStyle();
		textfield_style.background = background;
		textfield_style.font = font;
		textfield_style.fontColor = Color.LIGHT_GRAY;
		textfield_style.focusedFontColor = Color.WHITE;

		text_field = new TextField("", textfield_style);
		text_field.addListener(new InputListener() {
			@Override
			public boolean keyUp(InputEvent event, int keycode) {
				if (keycode == Keys.ENTER) {
					String command = text_field.getText();
					if (command.isEmpty())
						return false;
					executeCommand(command);
					text_field.setText(EMPTY);
					return true;
				}
				return super.keyUp(event, keycode);
			}
		});

		console_table.align(Align.bottom);

		console_table.add(scroll).grow().row();
		console_table.add(text_field).growX();

		stage.addActor(console_table);

		//--add 'help' 'exit' 'lalpha' 'tscale'and 'clear' commands
		final String help_info = "type in \"help nameofcommand\" to get help on a specific command";
		final String help_notfound = "[YELLOW]%s[] - ";
		final String help = "help";
		final String lcommands = " Commands:";
		final String help_format = "[YELLOW]%1$s[] - %2$s This command takes in at least %3$d parameters.";
		final String help_format2 = "[YELLOW]%s[] - %s.";
		final String help_format3 = "    %s";
		addCommand(new ConsoleCommand(help, 0, help_info, new CommandRunnable() {
			@Override
			public boolean executeCommand(DevConsole console, String... args) {
				if (args.length == 0) {
					log(StringUtils.format(help_format2, help, help_info));
					log(lcommands);
					for (int i = 0; i < console_commands.size; i++) {
						ConsoleCommand cc = console_commands.get(i);
						if (cc.getCommand().equals(help))
							continue;
						log(StringUtils.format(help_format3, cc.getCommand()));
					}
				} else {
					ConsoleCommand cc = getCommand(args[0]);
					if (cc != null) {
						log(StringUtils.format(help_format, cc.getCommand(), cc.getInfo(), cc.getParamCount()));
					} else {
						log(StringUtils.format(help_notfound, help) + StringUtils.format(NO_COMMAND_FOUND, args[0]));
					}
				}
				return true;
			}
		}));

		addCommand(new ConsoleCommand("exit", 0,
				"disposes([GREEN]game.dispose()[] is called) all the game before exiting completely.",
				new CommandRunnable() {

					@Override
					public boolean executeCommand(DevConsole console, String... args) {
						Gdx.app.exit();
						return true;
					}
				}));

		addCommand(new ConsoleCommand("clear", 0, "clears the command console.", new CommandRunnable() {
			@Override
			public boolean executeCommand(DevConsole console, String... args) {
				clearConsole();
				return true;
			}
		}));

		final String uilayer = "uiground";
		final String playlayer = "playground";
		final String forelayer = "foreground";
		final String backlayer = "background";
		final String reset = "reset";
		final String lalpha_format1 = "[ORANGE]%s[] is not a valid layer.";
		final String lalpha_format2 = "layer [MAROON]%s[]'s alpha was set to [SKY]%s[]";
		final String reset_string = "all layers were reset to alpha [SKY]1[].";
		addCommand(new ConsoleCommand("lalpha", 1,
				"changes the specified layer's alpha to the given value ([YELLOW]lalpha[]  [GREEN]layername value[]). Resets on state change or when [YELLOW]lalpha[]  [GREEN]reset[] is called. ",
				new CommandRunnable() {
					@Override
					public boolean executeCommand(DevConsole console, String... args) {

						String layer = args[0].trim();
						float value = 0f;
						Array<GameState> states = DevConsole.this.game.getCurrentStates();

						if (states.size == 0) {
							error(STATE_NOT_FOUND);
							return false;
						}

						GameState current_state = states.peek();
						

						if(layer.equals(reset)) {
							current_state.getUiLayer().getColor().a = 1f;
							current_state.getForegroundLayer().getColor().a = 1f;
							current_state.getPlaygroundLayer().getColor().a = 1f;
							current_state.getBackgroundLayer().getColor().a = 1f;
							log(reset_string);
							return true;
						}
						
						
						try {
							value = MathUtils.clamp(Float.parseFloat(args[1].trim()), 0f, 1f);
						} catch (Exception e) {
							error(e.getMessage());
							return false;
						}

						

						GameLayer gamelayer = null;
						if (layer.equals(uilayer)) {
							gamelayer = current_state.getUiLayer();
						} else if (layer.equals(forelayer)) {
							gamelayer = current_state.getForegroundLayer();
						} else if (layer.equals(playlayer)) {
							gamelayer = current_state.getPlaygroundLayer();
						} else if (layer.equals(backlayer)) {
							gamelayer = current_state.getBackgroundLayer();
						}
						if (gamelayer == null) {
							error(StringUtils.format(lalpha_format1, layer));
							return false;
						}

						gamelayer.getColor().a = value;
						log(StringUtils.format(lalpha_format2, layer,value));

						return true;
					}
				}));

		final String scale_string = "Console text scale was set to [SKY]%s[]. ([ORANGE] Min=[SKY]%s[]  Max=[SKY]%s[][])";
		addCommand(new ConsoleCommand("tscale", 1,
				"set the console's text scale to the given value. Takes in a [GREEN]float[] parameter. This clears the console.",
				new CommandRunnable() {
					@Override
					public boolean executeCommand(DevConsole console, String... args) {

						float min = .3f;
						float max = 3f;
						float value = min;
						try {
							value = MathUtils.clamp(Float.parseFloat(args[0].trim()), min,max);
						} catch (Exception e) {
							error(e.getMessage());
							return false;
						}

						fontscale = value;
						
						clearConsole();
						
						log(StringUtils.format(scale_string, value,min,max));
						
						scroll.layout();
						scroll.setScrollPercentY(100f);

						return true;
					}
				}));
		
		final String dr_format1 = "Debug render has been set to [GREEN]%s[]";
		addCommand(new ConsoleCommand("debug", 0,
				"Enable or Disable debug render. Toggles",
				new CommandRunnable() {
					@Override
					public boolean executeCommand(DevConsole console, String... args) {
						boolean value = !getGame().getDebugEnabled();
						console.log(StringUtils.format(dr_format1,value));
						getGame().debugEnabled(value);
						return true;
					}
				}));

		KyperBoxGame.log("CONSOLE:", "Initialized.");
		console_table.setVisible(false);

	}

	/**
	 * write a message to the console
	 * 
	 * @param message
	 */
	public void log(String message) {
		Label l = label_pool.obtain();
		l.setColor(Color.WHITE);
		l.setFontScale(fontscale);
		l.getStyle().font.getData().markupEnabled = true;
		l.setText(message);
		l.setWrap(true);
		label_table.add(l).growX().row();

		scroll.layout();
		scroll.setScrollPercentY(100f);
	}

	/**
	 * write a an error to the console - will be made red
	 * 
	 * @param message
	 */
	public void error(String message) {
		Label l = label_pool.obtain();
		l.setColor(Color.WHITE);
		l.setFontScale(fontscale);
		l.getStyle().font.getData().markupEnabled = true;
		l.setText(StringUtils.format(ERROR_FORMAT, message));
		l.setWrap(true);
		label_table.add(l).growX().row();

		scroll.layout();
		scroll.setScrollPercentY(100f);
	}

	public KyperBoxGame getGame() {
		return game;
	}

	public void addToMultiplexer(InputMultiplexer input) {
		input.addProcessor(stage);
	}

	public void updateSize(int width, int height) {
		stage.getViewport().update(width, height, true);
		console_table.setWidth(stage.getViewport().getWorldWidth());
		console_table.setPosition(0, 0);
		stage.getViewport().apply();
	}

	public void consoleDraw() {
		stage.getViewport().apply();
		stage.draw();
	}

	public void consoleUpdate(float delta) {
		stage.act();
	}

	public void open() {
		open = true;
		stage.setKeyboardFocus(text_field);
		text_field.setText(EMPTY);
		console_table.clearActions();
		console_table.setVisible(true);
		console_table.getColor().a = 0f;
		console_table.addAction(Actions.sequence(Actions.fadeIn(DevConsole.APPEAR_DURATION)));
		game.getInput().resetAllInputs();
	}

	public void close() {
		open = false;
		stage.unfocusAll();
		console_table.clearActions();
		console_table.getColor().a = 1f;
		console_table.addAction(Actions.sequence(Actions.fadeOut(DevConsole.APPEAR_DURATION), Actions.visible(false)));

	}

	public void clearConsole() {
		scroll.setScrollPercentY(0f);
		label_table.clearChildren();
		label_pool.freeAll(labels);
		labels.clear();
	}

	public void addCommand(ConsoleCommand command) {
		console_commands.add(command);
	}

	public void removeCommand(String command) {
		if (command.equals("help") || command.equals("clear"))
			return;
		console_commands.removeValue(getCommand(command), false);
	}

	public void executeCommand(String command) {
		if (command.isEmpty() || command.trim().isEmpty())
			return;
		String[] commandstruct = command.trim().split(" ");
		ConsoleCommand cc = getCommand(commandstruct[0]);
		if (cc == null) {
			error(StringUtils.format(NO_COMMAND_FOUND, commandstruct[0]));
			return;
		}
		int num_of_empty = 0;
		for (int i = 1; i < commandstruct.length; i++) {
			if (commandstruct[i].trim().isEmpty())
				num_of_empty++;
		}
		if (num_of_empty > 0) {
			error(BLANK_PARAM);
		}

		int param_count = cc.getParamCount();
		if (commandstruct.length - 1 - num_of_empty < param_count) {
			error(StringUtils.format(WRONG_PARAMETER_COUNT, commandstruct[0], commandstruct.length - 1, param_count));
			return;
		}

		String[] params = new String[commandstruct.length - 1];
		for (int i = 0; i < params.length; i++) {
			params[i] = commandstruct[i + 1];
		}
		if (!cc.execute(this, params)) {
			error(StringUtils.format(FAILED_COMMAND, cc.getCommand()));
		}
	}

	public ConsoleCommand getCommand(String command) {
		for (int i = 0; i < console_commands.size; i++) {
			if (console_commands.get(i).getCommand().equals(command))
				return console_commands.get(i);
		}
		return null;
	}

	public boolean isOpen() {
		return open;
	}

	public void dispose() {
		stage.dispose();
		font.dispose();
	}

}
