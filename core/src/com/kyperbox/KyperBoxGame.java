package com.kyperbox;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader.BitmapFontParameter;
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader;
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader.ParticleEffectParameter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kyperbox.input.GameInput;
import com.kyperbox.managers.Priority.PriorityComparator;
import com.kyperbox.managers.StateManager;
import com.kyperbox.util.KyperMapLoader;
import com.kyperbox.util.SaveUtils;
import com.kyperbox.util.UserData;

public abstract class KyperBoxGame extends ApplicationAdapter {
	
	public static final String IMAGE_FOLDER = "image";
	public static final String MUSIC_FOLDER = "music";
	public static final String SFX_FOLDER = "sound";
	public static final String TMX_FOLDER = "maps";
	public static final String PARTICLE_FOLDER = "particles";
	public static final String GAME_ATLAS = "game.atlas";
	public static final String TAG = "KyperBox->";
	public static final String FILE_SEPARATOR = "/";
	
	private static final String GAME_DATA_NAME = "GAME_GLOBALS";
	
	protected Stage game_stage;
	private AssetManager assets;
	private Viewport view;
	private SoundManager sound;
	private ObjectMap<String,GameState> game_states;
	private Array<GameState> current_gamestates;
	private Array<String> packages;
	private Preferences game_prefs;
	
	private static PriorityComparator prio_compare;
	
	private UserData global_data;
	
	private GameInput input;
	
	private String game_name;
	private String prefs_name;
	
	public KyperBoxGame(String prefs,String game_name,Viewport view) {
		this.view = view;
		this.prefs_name = prefs;
		if(game_name == null)
			this.game_name = this.getClass().getSimpleName();
		else
			this.game_name = game_name;
		if(prefs_name == null) 
			prefs_name = this.game_name+"_data";
	 //WARNING    ===========
	}//DO NOT USE ===========
	
	public KyperBoxGame(String game_name,Viewport view) {
		this(null,game_name,view);
	}
	
	public KyperBoxGame(Viewport view) {
		this(null,view);
	}
	
	public KyperBoxGame() {
		this(new FillViewport(Resolutions._720.WIDTH(), Resolutions._720.HEIGHT()));
	}
	
	public String getGameName() {
		return game_name;
	}
	
	public static PriorityComparator getPriorityComperator() {
		if(prio_compare == null)
			prio_compare = new PriorityComparator();
		return prio_compare;
	}
	
	@Override
	public void create () {
		game_prefs = Gdx.app.getPreferences(prefs_name);
		
		game_stage = new Stage(view);
		game_states = new ObjectMap<String,GameState>();
		game_stage.setDebugAll(false);
		
		current_gamestates = new Array<GameState>();
		
		assets = new AssetManager();
		assets.setLoader(TiledMap.class	, new KyperMapLoader(assets.getFileHandleResolver()));
		assets.setLoader(ParticleEffect.class, new ParticleEffectLoader(assets.getFileHandleResolver()));
		sound = new SoundManager(this);

		

		packages = new Array<String>();
		packages.add("com.kyperbox.objects");
		
		global_data = new UserData(GAME_DATA_NAME);
		input = new GameInput();
		
		Gdx.input.setInputProcessor(game_stage);
		
		initiate();
	}
	
	public Preferences getGamePreferences() {
		return game_prefs;
	}
	
	public boolean getDebugRender() {
		return game_stage.isDebugAll();
	}
	
	public void debugRender(boolean enable) {
		game_stage.setDebugAll(enable);
	}
	
	public Stage getGameState() {
		return game_stage;
	}
	
	public Viewport getView() {
		return view;
	}
	
	public UserData getGlobals() {
		return global_data;
	}
	
	public void saveGlobals() {
		SaveUtils.saveToPrefs(game_prefs, global_data);
	}
	
	public void loadGlobals() {
		SaveUtils.loadFromPrefs(game_prefs, global_data);
	}
	
	public GameInput getInput() {
		return input;
	}
	
	public void registerObjectPackage(String object_package) {
		packages.add(object_package);
	}
	
	protected Array<String> getObjectPackages(){
		return packages;
	}
	
	public void registerGameState(String tmx,StateManager manager) {
		game_states.put(tmx,new GameState(tmx,manager));
		game_states.get(tmx).setGame(this);
	}
	
	public void registerGameState(String tmx) {
		game_states.put(tmx, new GameState(tmx));
		game_states.get(tmx).setGame(this);
	}
	
	public void setGameState(final String tmx) {
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				for(GameState gs: current_gamestates)
					gs.remove();
				GameState state = game_states.get(tmx);
				state.init();
				for(GameState gs: current_gamestates)
					gs.dispose();
				current_gamestates.clear();
				current_gamestates.add(state);
				game_stage.addActor(state);
			}
		});
	}
	
	public void pushGameState(String tmx) {
		if(current_gamestates.contains(game_states.get(tmx), true))
			return;
		if(current_gamestates.peek()!=null)
			current_gamestates.peek().disableLayers(true);
		GameState state = game_states.get(tmx);
		state.init();
		current_gamestates.add(state);
		game_stage.addActor(state);
	}
	
	public GameState popGameState() {
		final GameState popped_state = current_gamestates.pop();
		if(current_gamestates.peek()!=null)
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
	
	public SoundManager getSoundManager() {
		return sound;
	}
	
	public AssetManager getAssetManager() {
		return assets;
	}	

	@Override
	public void render () {
		clear();
		input.update();
		AnimatedTiledMapTile.updateAnimationBaseTime();
		float delta = Gdx.graphics.getDeltaTime();
		if(current_gamestates.size > 0)
			current_gamestates.peek().act(delta);
		game_stage.draw();
		
		
	}
	
	private static void clear() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}
	
	@Override
	public void dispose () {
		game_stage.dispose();
		assets.dispose();
		
	}
	
	//========================================
	//ASSET METHODS
	//========================================
	
	
	public ParticleEffect getParticleEffect(String name) {
		return assets.get(PARTICLE_FOLDER+FILE_SEPARATOR+name,ParticleEffect.class);
	}
	
	public Sound getSound(String name) {
		return assets.get(SFX_FOLDER+FILE_SEPARATOR+name,Sound.class);
	}
	
	public Music getMusic(String name) {
		return assets.get(MUSIC_FOLDER+FILE_SEPARATOR+name,Music.class);
	}
	
	public TextureAtlas getAtlas(String name) {
		return assets.get(IMAGE_FOLDER+FILE_SEPARATOR+name,TextureAtlas.class);
	}
	
	public TiledMap getTiledMap(String name) {
		return assets.get(TMX_FOLDER+FILE_SEPARATOR+name,TiledMap.class);
	}
	
	public BitmapFont getFont(String name) {
		return assets.get(name,BitmapFont.class);
	}
	
	public void loadParticleEffect(String name,String atlas) {
		ParticleEffectParameter param = new ParticleEffectParameter();
		param.atlasFile = atlas;
		assets.load(PARTICLE_FOLDER+FILE_SEPARATOR+name, ParticleEffect.class, param);
	}
	
	public void loadFont(String name,String atlas) {
		BitmapFontParameter bfp = new BitmapFontParameter();
		bfp.atlasName = atlas;
		assets.load(name,BitmapFont.class,bfp);
	}
	
	public void loadFont(String name) {
		assets.load(name,BitmapFont.class);
	}
	
	public void loadSound(String name) {
		assets.load(SFX_FOLDER+FILE_SEPARATOR+name,Sound.class);
	}
	
	public void loadMusic(String name) {
		assets.load(MUSIC_FOLDER+FILE_SEPARATOR+name,Music.class);;
	}
	
	public void loadAtlas(String name) {
		assets.load(IMAGE_FOLDER+FILE_SEPARATOR+name,TextureAtlas.class);
	}
	
	public void loadTiledMap(String name) {
		assets.load(TMX_FOLDER+FILE_SEPARATOR+name,TiledMap.class);
	}
	
	public static void error(String tag,String message) {
		Gdx.app.error(TAG+tag, message);
	}
	
	public static void log(String tag,String message) {
		Gdx.app.log(TAG+tag, message);
	}
	
	@Override
	public void resize(int width, int height) {
		game_stage.getViewport().update(width, height);
	}

	
	public abstract void initiate();
}
