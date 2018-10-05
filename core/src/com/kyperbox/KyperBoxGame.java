package com.kyperbox;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader.BitmapFontParameter;
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader;
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader.ParticleEffectParameter;
import com.badlogic.gdx.assets.loaders.ShaderProgramLoader;
import com.badlogic.gdx.assets.loaders.ShaderProgramLoader.ShaderProgramParameter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kyperbox.ads.AdClient;
import com.kyperbox.ads.MockAdClient;
import com.kyperbox.console.IDevConsole;
import com.kyperbox.console.MockDevConsole;
import com.kyperbox.dic.Localizer;
import com.kyperbox.input.GameInput;
import com.kyperbox.input.ICWrapper;
import com.kyperbox.managers.Priority.PriorityComparator;
import com.kyperbox.managers.StateManager;
import com.kyperbox.managers.TransitionManager;
import com.kyperbox.umisc.BaseGameObjectFactory;
import com.kyperbox.umisc.IGameObjectFactory;
import com.kyperbox.umisc.IGameObjectGetter;
import com.kyperbox.umisc.KyperMapLoader;
import com.kyperbox.umisc.SaveUtils;
import com.kyperbox.umisc.UserData;

public abstract class KyperBoxGame extends ApplicationAdapter {

	public static String TAG = "KyperBox->";
	public static final String NOT_SUPPORTED = "[NOT SUPPORTED]";
	public static final String NULL_STRING = "NULL";
	public static final String COLON = ":";

	public static final String IMAGE_FOLDER = "image";
	public static final String MUSIC_FOLDER = "music";
	public static final String SFX_FOLDER = "sound";
	public static final String TMX_FOLDER = "maps";
	public static final String PARTICLE_FOLDER = "particles";
	public static final String SHADER_FOLDER = "shaders";
	public static final String GAME_ATLAS = "game.atlas";
	public static final String FILE_SEPARATOR = "/";

	public static final String VERTEX_SUFFIX = ".vert";
	public static final String FRAGMENT_SUFFIX = ".frag";

	public static final String SPACE_SEPARATOR = " ";
	public static final String DASH_SEPARATOR = " - ";

	public static final MapProperties NULL_PROPERTIES = new MapProperties();

	public static final Array<ICWrapper> controllers = new Array<ICWrapper>();

	private static ShaderProgram DEFAULT_SHADER;

	public static ShaderProgram getDefaultShader() {
		if (DEFAULT_SHADER == null)
			DEFAULT_SHADER = SpriteBatch.createDefaultShader();
		return DEFAULT_SHADER;
	}

	private static final String GAME_DATA_NAME = "GAME_GLOBALS";

	protected Stage game_stage;
	private AssetManager assets;
	private Viewport view;
	private SoundManager sound;
	private GameState transition_state;
	private ObjectMap<String, GameState> game_states;
	private Array<GameState> current_gamestates;
	private IGameObjectFactory object_factory;
	private Preferences game_prefs;

	public static boolean DEBUG_LOGGING = true; // TURN OFF -- preface all logging with this

	private InputMultiplexer input_multiplexer;

	private static PriorityComparator prio_compare;

	private UserData global_data;

	private GameInput input;

	private String game_name;
	private String prefs_name;

	private Localizer localizer;

	private AdClient ad_client;

	private IDevConsole console;

	public KyperBoxGame(String prefs, String game_name, Viewport view) {
		this.view = view;
		this.prefs_name = prefs;
		if (game_name == null)
			this.game_name = this.getClass().getSimpleName();
		else
			this.game_name = game_name;
		if (prefs_name == null)
			prefs_name = this.game_name + "_data";
		// WARNING ===========
	}// DO NOT USE ===========

	public KyperBoxGame(String game_name, Viewport view) {
		this(null, game_name, view);
	}

	public KyperBoxGame(Viewport view) {
		this(null, view);
	}

	public KyperBoxGame() {
		this(new FillViewport(Resolutions._720.WIDTH(), Resolutions._720.HEIGHT()));
	}

	public String getGameName() {
		return game_name;
	}

	public GameState getTransitionState() {
		return transition_state;
	}

	public static PriorityComparator getPriorityComperator() {
		if (prio_compare == null)
			prio_compare = new PriorityComparator();
		return prio_compare;
	}

	/**
	 * set the developer console - recommended to leave null for deployment
	 * 
	 * @param console
	 */
	public void setDevConsole(IDevConsole console) {
		this.console = console;
	}

	/**
	 * check if the dev console is available
	 * 
	 * @return false if null or is instance of mock console
	 */
	public boolean isDevConsoleAvailable() {
		return console != null || !(console instanceof MockDevConsole);
	}

	/**
	 * return the dev console if there is one. Otherwise return null. If you want to
	 * check against a boolean instead of null then try isDevConsoleAvailable()
	 * 
	 * @return
	 */
	public IDevConsole getDevConsole() {
		return console;
	}

	/**
	 * get the localization localizer object to help wtih localization. This must be
	 * set usign setLocalizer or it will return a useless empty one
	 * 
	 * @return
	 */
	public Localizer getLocalizer() {
		if (localizer == null)
			localizer = new Localizer();
		return localizer;
	}

	public void setLocalizer(Localizer localizer) {
		this.localizer = localizer;
	}

	@Override
	public void create() {
		game_prefs = Gdx.app.getPreferences(prefs_name);

		game_stage = new Stage(view);
		game_states = new ObjectMap<String, GameState>();
		game_stage.setDebugAll(false);
		game_stage.getBatch().setShader(getDefaultShader());

		current_gamestates = new Array<GameState>();
		transition_state = new GameState(null);
		transition_state.setGame(this);

		assets = new AssetManager();
		assets.setLoader(TiledMap.class, new KyperMapLoader(assets.getFileHandleResolver()));
		assets.setLoader(ParticleEffect.class, new ParticleEffectLoader(assets.getFileHandleResolver()));
		assets.setLoader(ShaderProgram.class,
				new ShaderProgramLoader(assets.getFileHandleResolver(), VERTEX_SUFFIX, FRAGMENT_SUFFIX));
		sound = new SoundManager(this);
		ShaderProgram.pedantic = false;

		object_factory = new BaseGameObjectFactory();

		global_data = new UserData(GAME_DATA_NAME);
		input = new GameInput();

		if (ad_client == null)
			ad_client = new MockAdClient();

		if (console == null) {
			console = new MockDevConsole();
		}
		console.create(this);

		input_multiplexer = new InputMultiplexer();
		if (console != null)
			console.addToMultiplexer(input_multiplexer);
		input_multiplexer.addProcessor(game_stage);
		input_multiplexer.addProcessor(input);
		Gdx.input.setInputProcessor(input_multiplexer);

		initiate();
	}

	/**
	 * set the games adclient if one is available. mostly only for mobile- requires
	 * a custom implementation for each platform. Mock version will be used to avoid
	 * errors.
	 * 
	 * @param ad_client
	 */
	public void setAdClient(AdClient ad_client) {
		this.ad_client = ad_client;
	}

	/**
	 * get the adclient available. if none was set will return a mock version to
	 * maintain cross platform compatibility
	 * 
	 * @return ad_client
	 */
	public AdClient getAdClient() {
		return ad_client;
	}

	/**
	 * get the preferences used for this game
	 * 
	 * @return
	 */
	public Preferences getGamePreferences() {
		return game_prefs;
	}

	/**
	 * check if the debug render is on
	 * 
	 * @return
	 */
	public boolean getDebugEnabled() {
		return game_stage.isDebugAll();
	}

	/**
	 * enable or disable debug rendering
	 * 
	 * @param enable
	 */
	public void debugEnabled(boolean enable) {
		game_stage.setDebugAll(enable);
	}

	public Stage getGameStage() {
		return game_stage;
	}

	public Viewport getView() {
		return view;
	}

	/**
	 * get the global data for this game.
	 * 
	 * @return
	 */
	public UserData getGlobals() {
		return global_data;
	}

	/**
	 * save the global data to the preferences
	 */
	public void saveGlobals() {
		SaveUtils.saveToPrefs(game_prefs, global_data);
	}

	/**
	 * try to load global data from the game preferences
	 */
	public void loadGlobals() {
		SaveUtils.loadFromPrefs(game_prefs, global_data);
	}

	public GameInput getInput() {
		return input;
	}

	public void registerGameObject(String objectname, IGameObjectGetter getter) {
		this.object_factory.registerGameObject(objectname, getter);
	}

	protected IGameObjectFactory getObjectFactory() {
		return object_factory;
	}

	/**
	 * register a game state
	 * 
	 * @param name
	 *            - name of the game state - this will be used for logging and
	 *            storing data
	 * @param tmx
	 *            - the tmx associated with this state.
	 * @param manager
	 *            - the manager for this gamestate - this handles all the logic
	 */
	public void registerGameState(String name, String tmx, StateManager manager) {
		game_states.put(name, new GameState(name, tmx, manager));
		game_states.get(name).setGame(this);
	}

	/**
	 * register a gamestate and use its tmx file as its name
	 * 
	 * @param tmx
	 *            - the tmx associated with this state
	 * @param manager
	 *            - the manager for this gamestate - this handles the logic
	 */
	public void registerGameState(String tmx, StateManager manager) {
		registerGameState(tmx, tmx, manager);
	}

	/**
	 * register a gamestate with no name and manager
	 * 
	 * @param tmx
	 *            - the tmx associated with this gamestate
	 */
	public void registerGameState(String tmx) {
		game_states.put(tmx, new GameState(tmx));
		game_states.get(tmx).setGame(this);
	}

	/**
	 * clear all other states currently in the state stack and set the state to the
	 * one given
	 * 
	 * @param state_name
	 *            - the name of the gamestate to set it to
	 */
	public void setGameState(final String state_name) {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				for (GameState gs : current_gamestates)
					gs.remove();
				GameState state = game_states.get(state_name);
				state.init();
				for (GameState gs : current_gamestates)
					gs.dispose();
				current_gamestates.clear();
				current_gamestates.add(state);
				game_stage.addActor(state);
			}
		});
	}

	/**
	 * push a gamestate on to the state stack. Gamestates have state specific flags
	 * to indicate whether it should halt the update/render of the state directly
	 * under it
	 * 
	 * @param state
	 *            - the gamestate to use
	 */
	public void pushGameState(final GameState state) {
		Gdx.app.postRunnable(new Runnable() {

			public void run() {

				if (current_gamestates.contains(state, true))
					return;
				if (current_gamestates.peek() != null)
					current_gamestates.peek().disableLayers(true);
				state.init();
				current_gamestates.add(state);
				game_stage.addActor(state);
			}
		});
	}

	/**
	 * * push a gamestate on to the state stack. Gamestates have state specific
	 * flags to indicate whether it should halt the update/render of the state
	 * directly under it
	 * 
	 * @param state_name
	 *            - the name of the gamestate to use
	 */
	public void pushGameState(String state_name) {
		pushGameState(game_states.get(state_name));
	}

	/**
	 * pop the topmost state on the statestack
	 * 
	 * @return - the popped state in case you would like to refference it
	 */
	public GameState popGameState() {
		final GameState popped_state = current_gamestates.pop();
		if (current_gamestates.size > 0 && current_gamestates.peek() != null)
			current_gamestates.peek().disableLayers(false);
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				popped_state.remove();
				popped_state.dispose();
			}
		});

		return popped_state;
	}

	/**
	 * transition to another state
	 * 
	 * @param state
	 *            - state to transition to
	 * @param duration
	 *            - duration of transition (*2 for in out)
	 * @param type
	 *            - type of TransitionManager.Type
	 */
	public void transitionTo(String state, float duration, int type) {
		if (transition_state.getManager() == null || !(transition_state.getManager() instanceof TransitionManager)) {
			transition_state.setManager(new TransitionManager());
		}
		TransitionManager ts = (TransitionManager) transition_state.getManager();
		ts.reset();
		ts.setNextState(state);
		ts.setTransitionType(type);
		ts.setDuration(duration);

		pushGameState(transition_state);

	}

	/**
	 * get the gamestate by name
	 * 
	 * @param name
	 * @return
	 */
	public GameState getState(String name) {
		if (game_states.containsKey(name))
			return game_states.get(name);
		error(TAG, "GameState-> not registerd [" + name + "].");
		return null;
	}

	public Array<GameState> getCurrentStates() {
		return current_gamestates;
	}

	/**
	 * get the sound manager
	 * 
	 * @return
	 */
	public SoundManager getSoundManager() {
		return sound;
	}

	/**
	 * get the assetmanager
	 * 
	 * @return
	 */
	public AssetManager getAssetManager() {
		return assets;
	}

	@Override
	public void render() {
		clear();
		AnimatedTiledMapTile.updateAnimationBaseTime();

		game_stage.getViewport().apply();
		game_stage.draw();

		if (console != null) {
			console.consoleDraw();
		}

		for (int i = 0; i < controllers.size; i++) {
			if (controllers.get(i).isConnected())
				controllers.get(i).update();
		}
		input.update();

		float delta = Math.min(Gdx.graphics.getDeltaTime(), 1f);
		if (console != null)
			console.consoleUpdate(delta);

		if (current_gamestates.size > 0)
			for (int i = 0; i < current_gamestates.size; i++) {
				GameState cs = current_gamestates.get(i);
				if (i + 1 < current_gamestates.size) {
					if (!current_gamestates.get(i + 1).haltsUpdate())
						cs.act(delta);
				} else
					cs.act(delta);
			}

	}

	private static void clear() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}

	@Override
	public void dispose() {
		game_stage.dispose();
		assets.dispose();
		if (console != null)
			console.dispose();
	}

	// ========================================
	// ASSET METHODS
	// ========================================

	/**
	 * get a shader program from the shaders folder
	 * 
	 * @param name
	 *            - name of the shader
	 * @return
	 */
	public ShaderProgram getShader(String name) {
		return assets.get(SHADER_FOLDER + FILE_SEPARATOR + name, ShaderProgram.class);
	}

	/**
	 * get a particle effect from the particles folder
	 * 
	 * @param name
	 * @return
	 */
	public ParticleEffect getParticleEffect(String name) {
		return assets.get(PARTICLE_FOLDER + FILE_SEPARATOR + name, ParticleEffect.class);
	}

	/**
	 * get a sound from the sounds folder
	 * 
	 * @param name
	 * @return
	 */
	public Sound getSound(String name) {
		return assets.get(SFX_FOLDER + FILE_SEPARATOR + name, Sound.class);
	}

	/**
	 * get a music file from the music folder
	 * 
	 * @param name
	 * @return
	 */
	public Music getMusic(String name) {
		return assets.get(MUSIC_FOLDER + FILE_SEPARATOR + name, Music.class);
	}

	/**
	 * get a texture atlas from the image folder
	 * 
	 * @param name
	 * @return
	 */
	public TextureAtlas getAtlas(String name) {
		return assets.get(IMAGE_FOLDER + FILE_SEPARATOR + name, TextureAtlas.class);
	}

	/**
	 * get a tiledmap from the maps folder
	 * 
	 * @param name
	 * @return
	 */
	public TiledMap getTiledMap(String name) {
		return assets.get(TMX_FOLDER + FILE_SEPARATOR + name, TiledMap.class);
	}

	/**
	 * get a bitmap font
	 * 
	 * @param name
	 * @return
	 */
	public BitmapFont getFont(String name) {
		return assets.get(name, BitmapFont.class);
	}

	public void loadShader(String name) {
		ShaderProgramParameter param = new ShaderProgramParameter();
		param.fragmentFile = SHADER_FOLDER + FILE_SEPARATOR + name + FRAGMENT_SUFFIX;
		param.vertexFile = SHADER_FOLDER + FILE_SEPARATOR + name + VERTEX_SUFFIX;
		assets.load(SHADER_FOLDER + FILE_SEPARATOR + name, ShaderProgram.class, param);
	}

	public void loadParticleEffect(String name, String atlas) {
		ParticleEffectParameter param = new ParticleEffectParameter();
		param.atlasFile = atlas;
		assets.load(PARTICLE_FOLDER + FILE_SEPARATOR + name, ParticleEffect.class, param);
	}

	public void loadFont(String name, String atlas) {
		BitmapFontParameter bfp = new BitmapFontParameter();
		bfp.atlasName = atlas;
		assets.load(name, BitmapFont.class, bfp);
	}

	public void loadFont(String name) {
		assets.load(name, BitmapFont.class);
	}

	public void loadSound(String name) {
		assets.load(SFX_FOLDER + FILE_SEPARATOR + name, Sound.class);
	}

	public void loadMusic(String name) {
		assets.load(MUSIC_FOLDER + FILE_SEPARATOR + name, Music.class);
	}

	public void loadAtlas(String name) {
		assets.load(IMAGE_FOLDER + FILE_SEPARATOR + name, TextureAtlas.class);
	}

	public void loadTiledMap(String name) {
		assets.load(TMX_FOLDER + FILE_SEPARATOR + name, TiledMap.class);
	}

	public static void error(String tag, String message) {
		Gdx.app.error(TAG + tag, message);
	}

	public static void log(String tag, String message) {
		Gdx.app.log(TAG + tag, message);
	}

	@Override
	public void resize(int width, int height) {
		game_stage.getViewport().update(width, height);
		for (int i = 0; i < current_gamestates.size; i++) {
			current_gamestates.get(i).resize(width, height);
		}
		if (console != null)
			console.updateSize(width, height);
	}

	/**
	 * use this to register your gamestates and set the initial gamestate other
	 * setup type things can also be done here.
	 */
	public abstract void initiate();
}
