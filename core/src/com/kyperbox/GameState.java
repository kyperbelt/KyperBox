package com.kyperbox;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Scaling;
import com.kyperbox.managers.StateManager;
import com.kyperbox.objects.GameLayer;
import com.kyperbox.objects.GameObject;
import com.kyperbox.util.KyperProgressBar;
import com.kyperbox.util.UserData;

public class GameState {
	
	private KyperBoxGame game;
	private String tmx;
	private StateManager manager;
	private GameLayer background;
	private GameLayer playground;
	private GameLayer foreground;
	private GameLayer uiground;
	private UserData user_data;
	
	private ObjectMap<String,BitmapFont> fonts;
	
	public GameState(String tmx) {
		this(tmx,new StateManager() {
			
			@Override
			public void update(float delta) {
				
			}
			
			@Override
			public void init(GameState game) {
				
			}
			
			@Override
			public void dispose(GameState game) {
				
			}

			@Override
			public void preInit(GameState game) {
				
			}
		});
	}
	
	public GameState(String tmx,StateManager manager) {
		this.tmx = tmx;
		setManager(manager);

		
	}

	public void init() {
		//load game state
		game.loadTiledMap(tmx);
		game.getAssetManager().finishLoading();
		TiledMap data = game.getTiledMap(tmx);
		user_data = new UserData(tmx+"(data)");
		
		
		
		background = new GameLayer(this);
		background.setName("background_");
		playground = new GameLayer(this);
		playground.setName("playground_");
		foreground = new GameLayer(this);
		foreground.setName("foreground_");
		uiground = new GameLayer(this);
		uiground.setName("uiground_");
		
		String atlas_name = data.getProperties().get("atlas", String.class);
		atlas_name = atlas_name.substring(atlas_name.lastIndexOf("/"), atlas_name.length());
		atlas_name = atlas_name.replace("/", "");
		
		user_data.setString("map_atlas", atlas_name);
		
		fonts = new ObjectMap<String, BitmapFont>();
		
		//init manager
		if(manager!=null) {
			manager.preInit(this);
		}

		//do preload
		loadFonts(data, data.getProperties().get("atlas",String.class));
		//load UI
		
		loadUi(data.getLayers().get("uiground"), game.getAtlas(atlas_name));
		
		//load foreground
		loadLayer(foreground,data.getLayers().get("foreground"));		
		//load Playground
		loadLayer(playground,data.getLayers().get("playground"));
		//load background
		loadLayer(background,data.getLayers().get("background"));

	
		
		game.addGameLayer(background);
		game.addGameLayer(playground);
		game.addGameLayer(foreground);
		game.addGameLayer(uiground);
		
		//init manager
		if(manager!=null) {
			manager.init(this);
		}
		log("initiated");
	}
	
	public UserData getData() {
		return user_data;
	}
	
	protected void setGame(KyperBoxGame game) {
		this.game = game;
	}
	
	public KyperBoxGame getGame() {
		return game;
	}
	
	public GameLayer getUiLayer() {
		return uiground;
	}
	
	public GameLayer getBackgroundLayer() {
		return background;
	}
	
	public GameLayer getForegroundLayer() {
		return foreground;
	}
	
	public GameLayer getPlaygroundLayer() {
		return playground;
	}
	
	public void update(float delta) {
		uiground.act(delta);
		foreground.act(delta);
		playground.act(delta);
		background.act(delta);
		
		if(manager!=null) {
			manager.update(delta);
		}
	}
	
	public StateManager getManager() {
		return manager;
	}
	
	private void setManager(StateManager manager) {
		this.manager = manager;
		manager.provideGameState(this);
	}
	
	/**
	 * called when the state is removed
	 * removes and clears all layers and 
	 * disposes of the manager
	 */
	public void remove() {
		background.remove();
		playground.remove();
		foreground.remove();
		uiground.remove();
		background.clear();
		playground.clear();
		foreground.clear();
		uiground.clear();
		if(manager!=null) {
			manager.dispose(this);
		}
		
		log("removed");
	}
	
	public GameInput getInput() {
		return game.getInput();
	}
	
	public void log(String message) {
		KyperBoxGame.log(tmx, message);
	}
	
	public void error(String message) {
		KyperBoxGame.error(tmx, message);
	}
	
	public void disableLayers(boolean disable) {
		if(disable) {
			uiground.setTouchable(Touchable.disabled);
			foreground.setTouchable(Touchable.disabled);
			playground.setTouchable(Touchable.disabled);
			background.setTouchable(Touchable.disabled);
		}
		else {
			uiground.setTouchable(Touchable.enabled);
			foreground.setTouchable(Touchable.enabled);
			playground.setTouchable(Touchable.enabled);
			background.setTouchable(Touchable.enabled);
		}
	}
	/*
	 * PRELOAD ----------------------------
	 */
	private void loadFonts(TiledMap data,String atlasname) {
		MapObjects objects = data.getLayers().get("preload").getObjects();
		for(MapObject o: objects) {
			String name = o.getName();
			BitmapFont font = null;
			MapProperties properties = o.getProperties();
			String type = properties.get("type",String.class);
			String fontfile = properties.get("font_file",String.class);
			if(fontfile!=null&&type!=null&&type.equals("Font")) {
				boolean markup = properties.get("markup",new Boolean(false),Boolean.class);
				game.loadFont(fontfile, atlasname);
				game.getAssetManager().finishLoading();
				font = game.getFont(fontfile);
				fonts.put(name, font);
				font.getData().markupEnabled = markup;
			}
		}
	}
	
	/*
	 * LOAD UI ---------------------------------------
	 */
	private Actor getUiActor(Array<String>added,MapObject object,MapProperties properties,MapObjects objects,TextureAtlas atlas) {
		Actor a = null;
		
		String name = object.getName();
		float x = properties.get("x", Float.class);
		float y = properties.get("y",Float.class);

		float w = properties.get("width",Float.class);
		float h = properties.get("height",Float.class);
		float r = properties.get("rotation",new Float(0),Float.class);
		String type = (String) properties.get("type");
		if(type == null || added.contains(name, false))
			return null;
		
		if(type.equals( 	"ImageButton"	 )) { //TODO: implement hover image
			ImageButtonStyle style = new ImageButtonStyle();
			String up = properties.get("upImage", "", String.class);
			String down = properties.get("downImage",String.class);
			float pressedXOff = properties.get("pressedXOff",new Float(0),Float.class);
			float pressedYOff = properties.get("pressedYOff",new Float(0),Float.class);
			
			if(!up.isEmpty()) {
				style.imageUp = new TextureRegionDrawable(atlas.findRegion(up));
			}
			if(!down.isEmpty()) {
				style.imageDown = new TextureRegionDrawable(atlas.findRegion(down));
			}
			style.pressedOffsetX = pressedXOff;
			style.pressedOffsetY = pressedYOff;
			
			a = new ImageButton(style);
			ImageButton b = (ImageButton)a;
			b.setTransform(true);
			b.getImage().setScaling(Scaling.stretch);
			b.getImageCell().grow();
			b.setRotation(-r);
		}else if(type.equals(	"Table"	)) {
			a = new Table();
			Table t = (Table) a;
			String bg_name = properties.get("background","", String.class);
			String children_raw = properties.get("children", String.class);
			if(children_raw!=null && !children_raw.isEmpty()) {
				String[] children = children_raw.split(",");
				for (int i = 0; i < children.length; i++) {
					MapObject child_object = objects.get(children[i]);
					if(child_object !=null) {
						Actor child_actor = null;
						if((child_actor = uiground.getActor(children[i]))!=null) {
							child_actor.remove();
							child_actor.setPosition(child_actor.getX()-x, child_actor.getY()-y);
							//child_actor.setRotation(0);
							log("child actor["+child_actor.getName()+"] added to ["+name+"]");
							t.addActor(child_actor);
						}else {
							MapProperties child_properties = child_object.getProperties();
							child_actor = getUiActor(added, child_object, child_properties, objects, atlas);
							if(child_actor!=null) {
								child_actor.setPosition(child_actor.getX()-x, child_actor.getY()-y);
								//child_actor.setRotation(0);
								t.addActor(child_actor);
							}
						}
						
					}
				}
			}
			
			boolean bg_ninepatch = properties.get("ninepatch",Boolean.class);
			if(!bg_name.isEmpty()) {
				TextureRegion bg_texture = atlas.findRegion(bg_name);
				if(bg_ninepatch) {
					int size = (int) (bg_texture.getRegionWidth()*.33f);
					t.setBackground(new NinePatchDrawable(new NinePatch(bg_texture,size,size,size,size)));
				}else {
					t.setBackground(new TextureRegionDrawable(bg_texture));
				}
			}
			//t.setTransform(true);
			//t.setOrigin(Align.center);
			t.setRotation(-r);
			
		
		}else if(type.equals(	"Image"	)) {
			String texture_name = properties.get("image","",String.class);
			TextureRegion texture = atlas.findRegion(texture_name);
			if(texture!=null) {
				a = new Image(texture);
			}
			
		}else if(type.equals(	"Label"	)) {
			LabelStyle ls = new LabelStyle();
			String font_name = properties.get("font","",String.class);
			String text = properties.get("text","",String.class);
			Color color = properties.get("color", Color.class);
			boolean wrap = properties.get("wrap",new Boolean(false),Boolean.class);
			if(font_name!=null&&!font_name.isEmpty()) {
				BitmapFont font = fonts.get(font_name);
				ls.font = font;
				ls.fontColor = color;
				a = new Label(text, ls);
				Label l = (Label)a;
				l.setAlignment(Align.topLeft);
				//l.setLayoutEnabled(false);
				l.setOrigin(Align.center);
				l.setWrap(wrap);
				
			}
			
		}else if(type.equals(	"TextButton"	)) {
			TextButtonStyle style = new TextButtonStyle();
			String font_name = properties.get("font",String.class);
			String text = properties.get("text","",String.class);
			String button_up_name = properties.get("bg_up",String.class);
			String button_down_name = properties.get("bg_down",String.class);
			String button_hover_name = properties.get("bg_hover",String.class);
			Color font_down_color = properties.get("font_down_color",Color.WHITE,Color.class);
			Color font_up_color = properties.get("font_up_color",Color.WHITE,Color.class);
			Color font_hover_color = properties.get("font_hover_color",Color.WHITE,Color.class);
			float xOff = properties.get("pressedXOff",new Float(0),Float.class);
			float yOff = properties.get("pressedYOff",new Float(0),Float.class);
			
			if(button_down_name== null)
				button_down_name  = button_up_name;
			if(button_hover_name == null)
				button_hover_name = button_up_name;
			
			if(font_name!=null) {
				BitmapFont font = fonts.get(font_name);
				if(font != null) {
					style.font = font;
					style.fontColor = font_up_color;
					style.downFontColor = font_down_color;
					style.overFontColor = font_hover_color;
					style.pressedOffsetX = xOff;
					style.pressedOffsetY = yOff;
					boolean ninepatch = properties.get("ninepatch",Boolean.class);
					//UP_BG
					if(atlas.findRegion(button_up_name)!=null) {
						TextureRegion up_texture = atlas.findRegion(button_up_name);
						if(ninepatch) {
							int size = (int) (up_texture.getRegionWidth()*.33f);
							style.up = new NinePatchDrawable(new NinePatch(up_texture,size,size,size,size));
						}else {
							style.up = new TextureRegionDrawable(up_texture);
						}
					}
					//DOWN BG
					if(atlas.findRegion(button_down_name)!=null) {
						TextureRegion down_texture = atlas.findRegion(button_down_name);
						if(ninepatch) {
							int size = (int) (down_texture.getRegionWidth()*.33f);
							style.down = new NinePatchDrawable(new NinePatch(down_texture,size,size,size,size));
						}else {
							style.down = new TextureRegionDrawable(down_texture);
						}
					}
					//HOVER
					if(atlas.findRegion(button_hover_name)!=null) {
						TextureRegion hover_texture = atlas.findRegion(button_hover_name);
						if(ninepatch) {
							int size = (int) (hover_texture.getRegionWidth()*.33f);
							style.over = new NinePatchDrawable(new NinePatch(hover_texture,size,size,size,size));
						}else {
							style.over = new TextureRegionDrawable(hover_texture);
						}
					}
					
					a = new TextButton(text, style);
					TextButton b = (TextButton)a;
					float font_scale_x = properties.get("font_scale_x",new Float(1),Float.class);
					float font_scale_y = properties.get("font_scale_y",new Float(1),Float.class);
					b.getLabel().setFontScale(font_scale_x, font_scale_y);
					b.getLabelCell().grow().align(Align.top).pad(0);
				}
			}
		}else if(type.equals(	"ProgressBar"	)) {
			ProgressBarStyle style = new ProgressBarStyle();
			boolean vertical = properties.get("vertical",new Boolean(false),Boolean.class);
			Pixmap default_knob_pixmap = new Pixmap(0, 0, Format.RGBA8888);
			default_knob_pixmap.setColor(Color.WHITE);
			TextureRegion knob = new TextureRegion(new Texture(default_knob_pixmap));
			default_knob_pixmap.dispose();
			
			boolean flipped = properties.get("flipped",new Boolean(false),Boolean.class);
			
			String background_name =!flipped? properties.get("background","",String.class):properties.get("knob_before","",String.class);
			String knob_name = properties.get("knob","",String.class);
			String knob_after = properties.get("knob_after","",String.class);
			String knob_before = !flipped?properties.get("knob_before","",String.class):properties.get("background","",String.class);
			
			
			float min = properties.get("min",new Float(0),Float.class);
			float max = properties.get("max",new Float(1),Float.class);
			float step_size = properties.get("step_size",new Float(0.01),Float.class);
			
			TextureRegion bg_texture = atlas.findRegion(background_name);
			TextureRegion knob_texture = atlas.findRegion(knob_name);
			TextureRegion knob_after_texture = atlas.findRegion(knob_after);
			TextureRegion knob_before_texture = atlas.findRegion(knob_before);
			
			int size =1;
			
			if(bg_texture!=null) {
				 size = (int) (bg_texture.getRegionWidth()*.33f);
				 style.background = new NinePatchDrawable(new NinePatch(bg_texture,size,size,size,size));
			}
			if(knob_texture!=null) {
				 size = (int) (knob_texture.getRegionWidth()*.33f);
				 style.knob = new NinePatchDrawable(new NinePatch(knob_texture,size,size,size,size));
			}
			if(knob_after_texture!=null) {
				 size = (int) (knob_after_texture.getRegionWidth()*.33f);
				 style.knobAfter = new NinePatchDrawable(new NinePatch(knob_after_texture,size,size,size,size));
			}
			if(knob_before_texture!=null) {
				 size = (int) (knob_before_texture.getRegionWidth()*.33f);
				 style.knobBefore = new NinePatchDrawable(new NinePatch(knob_before_texture,size,size,size,size));
			}
			if(knob_texture!=null) {
//				 size = (int) (knob_texture.getRegionWidth()*.33f);
				 style.knob = new TextureRegionDrawable(knob_texture);
			}else {
				style.knob = new TextureRegionDrawable(knob);
			}
			
			
			a = new KyperProgressBar(min, max, step_size, vertical, style);
	
			KyperProgressBar p = (KyperProgressBar)a;
			p.setFlipped(flipped);
			p.setValue(p.getMaxValue()*.75f);
			
			
		}
		
		if(a!=null) {
			a.setName(name);
			a.setPosition(x, y);
			a.setSize(w, h);
			added.add(name);
		}
		
		return a;
	}
	
	private void loadUi(MapLayer layer,TextureAtlas atlas) {
		MapObjects objects = layer.getObjects();
		Array<String> added = new Array<String>();
		for(MapObject o : objects) {
			MapProperties properties = o.getProperties();
			Actor ui_actor = getUiActor(added, o, properties, objects, atlas);
			if(ui_actor!=null)
				uiground.addActor(ui_actor);
			
		}
	}
	
	/*
	 * LOAD LAYER OBJECTS ------------------------------------
	 */
	
	private void loadLayer(GameLayer game_layer,MapLayer map_layer) {
	//	MapProperties layer_properties = layer.getProperties();
		MapObjects objects = map_layer.getObjects();
		for(MapObject object: objects) {
			MapProperties object_properties = object.getProperties();
			GameObject game_object = null;
			String sprite = object_properties.get("sprite",GameObject.NO_SPRITE,String.class);
			String type = object_properties.get("type",String.class);
			String name = object.getName();
			float x = object_properties.get("x",Float.class);
			float y = object_properties.get("y",Float.class);
			float w = object_properties.get("width",Float.class);
			float h = object_properties.get("height",Float.class);
			float r = object_properties.get("rotation",new Float(0),Float.class);
			
			for(String package_name:game.getObjectPackages()) {
				try {
					game_object = (GameObject) Class.forName(package_name+"."+type).newInstance();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				if(game_object!=null) {
					break;
				}
				
			}
			if(game_object!=null) {
				game_object.setName(name);
				game_object.setPosition(x, y);
				game_object.setSize(w, h);
				game_object.setRotation(-r);
				game_object.setSprite(sprite);
				game_layer.addGameObject(game_object, object_properties);
			}else {
				log("unable to create object ["+name+"] of type ["+type+"].");
			}
		}
	}
}
