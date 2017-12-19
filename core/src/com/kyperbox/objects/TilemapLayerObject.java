package com.kyperbox.objects;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.kyperbox.util.TileLayerRenderer;

public class TilemapLayerObject extends GameObject{
	
	private TiledMapTileLayer tiles;
	private TileLayerRenderer render;
	
	
	@Override
	public void init(MapProperties properties) {
		super.init(properties);
		
		tiles = (TiledMapTileLayer) getState().getMapData().getLayers().get(properties.get("tile_layer", String.class));
		setSize(tiles.getWidth()*tiles.getTileWidth(), tiles.getHeight()*tiles.getTileHeight());
		render = new TileLayerRenderer(this);
			
	}
	
	@Override
	public void draw(Batch batch, float parentAlpha) {
		render.renderTileLayer(tiles, batch);
		super.draw(batch, parentAlpha);
	}
	
	public Cell getCellAt(int x,int y) {
		return tiles.getCell(x, y);
	}

}
