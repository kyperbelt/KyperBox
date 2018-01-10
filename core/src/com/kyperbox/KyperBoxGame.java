package com.kyperbox;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.BitmapFontLoader.BitmapFontParameter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kyperbox.managers.Priority.PriorityComparator;
import com.kyperbox.input.GameInput;
import com.kyperbox.managers.StateManager;
import com.kyperbox.objects.GameLayer;
import com.kyperbox.util.KyperMapLoader;
import com.kyperbox.util.UserData;

public abstract class KyperBoxGame extends ApplicationAdapter {
	
	public static final String IMAGE_FOLDER = "image";
	public static final String MUSIC_FOLDER = "music";
	public static final String SFX_FOLDER = "sound";
	public static final String TMX_FOLDER = "maps";
	public static final String GAME_ATLAS = "game.atlas";
	public static final String TAG = "KyperBox->";
	public static final String FILE_SEPARATOR = "/";
	
	protected Stage game_stage;
	private AssetManager assets;
	private Viewport view;
	private SoundManager sound;
	private ObjectMap<String,GameState> game_states;
	private Array<GameState> current_gamestates;
	private Array<String> packages;
	
	private static PriorityComparator prio_compare;

	private ObjectMap<String,Sprite> sprites;
	private ObjectMap<String,Animation<String>>animations;
	
	private UserData data;
	
	private GameInput input;
	
	public KyperBoxGame(Viewport view) {
		this.view = view; 
	 //WARNING    ===========
	}//DO NOT USE ===========
	
	
	public static PriorityComparator getPriorityComperator() {
		if(prio_compare == null)
			prio_compare = new PriorityComparator();
		return prio_compare;
	}
	
	@Override
	public void create () {
		game_stage = new Stage(view);
		game_states = new ObjectMap<String,GameState>();
		game_stage.setDebugAll(false);
		
		current_gamestates = new Array<GameState>();
		
		assets = new AssetManager();
		assets.setLoader(TiledMap.class	, new KyperMapLoader(assets.getFileHandleResolver()));
		sound = new SoundManager(this);

		sprites = new ObjectMap<String, Sprite>();
		animations = new ObjectMap<String, Animation<String>>();

		packages = new Array<String>();
		packages.add("com.kyperbox.objects");
		
		data = new UserData("GAME_DATA");
		input = new GameInput();
		
		Gdx.input.setInputProcessor(game_stage);
		
		initiate();
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
	
	public UserData getData() {
		return data;
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
				current_gamestates.clear();
				GameState state = game_states.get(tmx);
				state.init();
				current_gamestates.add(state);
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
	}
	
	public GameState popGameState() {
		final GameState popped_state = current_gamestates.pop();
		if(current_gamestates.peek()!=null)
			current_gamestates.peek().disableLayers(false);
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				popped_state.remove();
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
			current_gamestates.peek().update(delta);
		game_stage.draw();
		
		
	}
	
	protected void addGameLayer(GameLayer layer) {
		game_stage.addActor(layer);
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
	

	/**
	 * get a sprite so we dont keep creating new sprites
	 * @param name
	 * @return
	 */
	public Sprite getGameSprite(String name) {
		return getGameSprite(name, GAME_ATLAS);
	}
	
	public Sprite getGameSprite(String name,String atlas) {
		if(!sprites.containsKey(name))
			sprites.put(name, getAtlas(atlas).createSprite(name));
		return sprites.get(name);
	}
	
	/**
	 * store an animation for later use with getAnimation
	 * @param animation_name
	 * @param animation
	 */
	public void storeAnimation(String animation_name,Animation<String> animation) {
		if(animations.containsKey(animation_name))
			log("Animations", "animation ["+animation_name+"] already exists and has been overriden!");
		animations.put(animation_name, animation);
	}

	/**
	 * create a game animation with the base name
	 * and the number of frames. 
	 * This method uses indexes (ex.calling (animation,3) = Animation0,Animation1,Animation2 - )
	 * @param frames
	 * @param frame_duration
	 * @return
	 */
	public Animation<String> createGameAnimation(String name,int frames,float frame_duration){
		String[] f = new String[frames];
		for(int i = 0;i<f.length;i++) {
			f[i]=name+"_"+i;
		}
		Animation<String> animations = new Animation<String>(frame_duration,f);
		return animations;
	}
	
	/**
	 * returns a stored animation to avoid garbage collection. 
	 * Animations may use the same sprites allowing multiple variations of the same
	 * animation
	 * @param name
	 * @return
	 */
	public Animation<String> getAnimation(String name){
		if(animations.containsKey(name)) {
			return animations.get(name);
		}else {
			error("Animations", "not found["+name+"].");
			return null;
		}
	}
	
	
	public abstract void initiate();
}
