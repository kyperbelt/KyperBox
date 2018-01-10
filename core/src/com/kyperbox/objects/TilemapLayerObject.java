package com.kyperbox.objects;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.kyperbox.controllers.TileCollisionController;
import com.kyperbox.util.TileLayerRenderer;

public class TilemapLayerObject extends GameObject {

	private static float SX = 0;
	private static float SY = 0;
	private static float EX = 0;
	private static float EY = 0;
	private static float CX = 0;
	private static float CY = 0;

	private Pool<MapTile> tile_pool;
	
	private TiledMapTileLayer tiles;
	private TileLayerRenderer render;
	private Array<Array<MapTile>> cell_arrays;

	@Override
	public void init(MapProperties properties) {
		super.init(properties);
		tile_pool = new Pool<TilemapLayerObject.MapTile>() {
			@Override
			protected MapTile newObject() {
				return new MapTile();
			}
		};
		tiles = (TiledMapTileLayer) getState().getMapData().getLayers().get(properties.get("tile_layer", String.class));
		setSize(tiles.getWidth() * tiles.getTileWidth(), tiles.getHeight() * tiles.getTileHeight());
		render = new TileLayerRenderer(this);
		cell_arrays = new Array<Array<MapTile>>();

	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		render.renderTileLayer(tiles, batch);
		super.draw(batch, parentAlpha);
	}

	@Override
	public void act(float delta) {
		super.act(delta);
		for (int i = 0; i < cell_arrays.size; i++) {
			Array<MapTile> tile_array = cell_arrays.get(i);
			for (int j = 0; j < tile_array.size; j++) {
				tile_pool.free(tile_array.get(j));
			}
			tile_array.clear();
		}
		getTCAPool().freeAll(cell_arrays);
		cell_arrays.clear();
	}

	/**
	 * get all cell collisions for a side
	 * 
	 * @param xx1
	 * @param yy1
	 * @param xx2
	 * @param yy2
	 * @return
	 */
	public Array<MapTile> getCollisionsForSide(float xx1, float yy1, float xx2, float yy2) {
		Array<MapTile> array = getCellArray();

		SX = Math.min(xx1 - getX(), xx2 - getX());
		SY = Math.min(yy1 - getY(), yy2 - getY());
		EX = SX == xx1 - getX() ? xx2 - getX() : xx1 - getX();
		EY = SY == yy1 - getY() ? yy2 - getY() : yy1 - getY();
		SX = MathUtils.floor((SX+1f) / tiles.getTileWidth());
		SY = MathUtils.floor((SY+1f) / tiles.getTileHeight());
		EX = MathUtils.floor((EX-1f) / tiles.getTileWidth());
		EY = MathUtils.floor((EY-1f) / tiles.getTileHeight());

		if ((SX >= 0 && SX <= tiles.getWidth() && SY >= 0 && SY <= tiles.getHeight()) ||
				(EX >= 0 && EX <= tiles.getWidth() && EY >= 0 && EY <= tiles.getHeight())) {//check if its atleast partially inside the layer bounds

			if (SX != EX && SY != EY)
				; // diagonal line
			else if (SX != EX) { // horizontal line
				CX = EX - SX;
				for (int i = 0; i < CX+1; i++) {
					Cell c = getCellAt(SX+i,SY);
					if(c!=null) {
						MapTile cc = tile_pool.obtain();
						cc.init((SX+i)*tiles.getTileWidth()+getX(), (SY*tiles.getTileHeight())+getY(), tiles.getTileWidth(), tiles.getTileHeight(), c);
						array.add(cc);
					}
				}
			} else if (SY != EY) {// vertical line
				CY = EY - SY;
				for (int i = 0; i < CY+1; i++) {
					Cell c = getCellAt(SX, SY+i);
					if(c!=null) {
						MapTile cc = tile_pool.obtain();
						cc.init((SX)*tiles.getTileWidth()+getX(),(SY+i)*tiles.getTileHeight()+getY(), tiles.getTileWidth(), tiles.getTileHeight(), c);
						array.add(cc);
					}
						
				}
			}else if(SY == EY && SX == EX) { //single tile
				Cell c = getCellAt(SX, SY);
				if(c!=null) {
					MapTile cc = tile_pool.obtain();
					cc.init((SX)*tiles.getTileWidth()+getX(),(SY)*tiles.getTileHeight()+getY(), tiles.getTileWidth(), tiles.getTileHeight(), c);
					array.add(cc);
				}
				
			}

		}

		return array;
	}

	public Cell getCellAt(float x, float y) {
		return tiles.getCell((int)x,(int)y);
	}

	private Array<MapTile> getCellArray() {
		Array<MapTile> array = getTCAPool().obtain();
		cell_arrays.add(array);
		return array;
	}
	
	public static class MapTile implements Poolable{
		private Cell cell;
		private float x;
		private float y;
		private float width;
		private float height;
		
		public void init(float x,float y,float tilewidth,float tileheight,Cell cell) {
			this.cell = cell;
			this.x = x;
			this.y = y;
			this.width = tilewidth;
			this.height = tileheight;
		}
		
		public float getRight() {
			return x+width;
		}
		

		public float getLeft() {
			return x;
		}
		
		public float getTop() {
			return y+height;
		}
		
		public float getBot() {
			return y;
		}
		
		public Cell getCell() {
			return cell;
		}
		
		public int getId() {
			return cell.getTile().getId();
		}
		
		public int getType() {
			return getProperty("type", TileCollisionController.VOID, int.class);
		}
		
		public <t> t getProperty(String name,t def,Class<t> type) {
			return cell.getTile().getProperties().get(name, def,type);
		}
		 
		@Override
		public void reset() {
			x = 0;
			y = 0;
			width = 0;
			height = 0;
			cell = null;
		}
		
		public float getX() {
			return x;
		}
		
		public float getY() {
			return y;
		}
		
		public float getWidth() {
			return width;
		}
		
		public float getHeight() {
			return height;
		}
	}

	private static Pool<Array<MapTile>> getTCAPool() {
		if (tile_col_arrays == null)
			tile_col_arrays = new Pool<Array<MapTile>>() {
				@Override
				protected Array<MapTile> newObject() {
					return new Array<MapTile>();
				}
			};
		return tile_col_arrays;
	}

	private static Pool<Array<MapTile>> tile_col_arrays;

}
