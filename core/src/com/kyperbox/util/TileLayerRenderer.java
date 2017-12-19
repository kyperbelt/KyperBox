package com.kyperbox.util;

import static com.badlogic.gdx.graphics.g2d.Batch.C1;
import static com.badlogic.gdx.graphics.g2d.Batch.C2;
import static com.badlogic.gdx.graphics.g2d.Batch.C3;
import static com.badlogic.gdx.graphics.g2d.Batch.C4;
import static com.badlogic.gdx.graphics.g2d.Batch.U1;
import static com.badlogic.gdx.graphics.g2d.Batch.U2;
import static com.badlogic.gdx.graphics.g2d.Batch.U3;
import static com.badlogic.gdx.graphics.g2d.Batch.U4;
import static com.badlogic.gdx.graphics.g2d.Batch.V1;
import static com.badlogic.gdx.graphics.g2d.Batch.V2;
import static com.badlogic.gdx.graphics.g2d.Batch.V3;
import static com.badlogic.gdx.graphics.g2d.Batch.V4;
import static com.badlogic.gdx.graphics.g2d.Batch.X1;
import static com.badlogic.gdx.graphics.g2d.Batch.X2;
import static com.badlogic.gdx.graphics.g2d.Batch.X3;
import static com.badlogic.gdx.graphics.g2d.Batch.X4;
import static com.badlogic.gdx.graphics.g2d.Batch.Y1;
import static com.badlogic.gdx.graphics.g2d.Batch.Y2;
import static com.badlogic.gdx.graphics.g2d.Batch.Y3;
import static com.badlogic.gdx.graphics.g2d.Batch.Y4;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.kyperbox.objects.GameLayer.LayerCamera;
import com.kyperbox.objects.GameObject;

public class TileLayerRenderer {
	static protected final int NUM_VERTICES = 20;
	private float unit_scale;
	private Rectangle view_bounds;
	private Vector2 pos;
	private float vertices[] = new float[NUM_VERTICES];
	private GameObject object;
	public TileLayerRenderer(GameObject object) {
		this.object = object;
		view_bounds = new Rectangle();
		unit_scale = 1.0f;
		pos = new Vector2(0,0);
	}
	
	public Rectangle getViewBounds() {
		return view_bounds;
	}
	
	public void setViewBounds(float x,float y,float w, float h) {
		view_bounds.set(x, y, w, h);
	}
	
	public float getUnitScale() {
		return unit_scale;
	}
	
	public void setUnitScale(float unit_scale) {
		this.unit_scale = unit_scale;
	}
	
	public void renderTileLayer (TiledMapTileLayer layer,Batch batch) {
		LayerCamera cam = object.getGameLayer().getCamera();
		pos.set(0, 0);
		pos = cam.unproject(pos);
		view_bounds.set(pos.x, pos.y, object.getGame().getView().getWorldWidth(), object.getGame().getView().getWorldHeight());
		unit_scale = (object.getScaleX()+object.getScaleY())/2f;
		final Color batchColor = batch.getColor();
		final float color = Color.toFloatBits(batchColor.r, batchColor.g, batchColor.b, batchColor.a * layer.getOpacity());

		final int layer_width = layer.getWidth();
		final int layer_height = layer.getHeight();

		final float layer_tilewidth = layer.getTileWidth() * unit_scale;
		final float layer_tileheight = layer.getTileHeight() * unit_scale;

		
		final float offset_x = object.getX() * unit_scale;
		final float offset_y = object.getY() * unit_scale;

		final int col1 = Math.max(0, (int)((view_bounds.x - offset_x) / layer_tilewidth));
		final int col2 = Math.min(layer_width,
			(int)((view_bounds.x + view_bounds.width + layer_tilewidth - offset_x) / layer_tilewidth));

		final int row1 = Math.max(0, (int)((view_bounds.y - offset_y) / layer_tileheight));
		final int row2 = Math.min(layer_height,
			(int)((view_bounds.y + view_bounds.height + layer_tileheight - offset_y) / layer_tileheight));

		float y = row2 * layer_tileheight + offset_y;
		float xStart = col1 * layer_tilewidth + offset_x;
		final float[] vertices = this.vertices;


	
		for (int row = row2; row >= row1; row--) {
			
			float x = xStart;
			for (int col = col1; col < col2; col++) {
				
				final TiledMapTileLayer.Cell cell = layer.getCell(col, row);
				if (cell == null) {
					x += layer_tilewidth;
					continue;
				}
				final TiledMapTile tile = cell.getTile();
				if (tile != null) {
					final boolean flipX = cell.getFlipHorizontally();
					final boolean flipY = cell.getFlipVertically();
					final int rotations = cell.getRotation();

					TextureRegion region = tile.getTextureRegion();

					float x1 = x + tile.getOffsetX() * unit_scale;
					float y1 = y + tile.getOffsetY() * unit_scale;
					float x2 = x1 + region.getRegionWidth() * unit_scale;
					float y2 = y1 + region.getRegionHeight() * unit_scale;

					float u1 = region.getU();
					float v1 = region.getV2();
					float u2 = region.getU2();
					float v2 = region.getV();

					vertices[X1] = x1;
					vertices[Y1] = y1;
					vertices[C1] = color;
					vertices[U1] = u1;
					vertices[V1] = v1;

					vertices[X2] = x1;
					vertices[Y2] = y2;
					vertices[C2] = color;
					vertices[U2] = u1;
					vertices[V2] = v2;

					vertices[X3] = x2;
					vertices[Y3] = y2;
					vertices[C3] = color;
					vertices[U3] = u2;
					vertices[V3] = v2;

					vertices[X4] = x2;
					vertices[Y4] = y1;
					vertices[C4] = color;
					vertices[U4] = u2;
					vertices[V4] = v1;

					if (flipX) {
						float temp = vertices[U1];
						vertices[U1] = vertices[U3];
						vertices[U3] = temp;
						temp = vertices[U2];
						vertices[U2] = vertices[U4];
						vertices[U4] = temp;
					}
					if (flipY) {
						float temp = vertices[V1];
						vertices[V1] = vertices[V3];
						vertices[V3] = temp;
						temp = vertices[V2];
						vertices[V2] = vertices[V4];
						vertices[V4] = temp;
					}
					if (rotations != 0) {
						switch (rotations) {
						case Cell.ROTATE_90: {
							float tempV = vertices[V1];
							vertices[V1] = vertices[V2];
							vertices[V2] = vertices[V3];
							vertices[V3] = vertices[V4];
							vertices[V4] = tempV;

							float tempU = vertices[U1];
							vertices[U1] = vertices[U2];
							vertices[U2] = vertices[U3];
							vertices[U3] = vertices[U4];
							vertices[U4] = tempU;
							break;
						}
						case Cell.ROTATE_180: {
							float tempU = vertices[U1];
							vertices[U1] = vertices[U3];
							vertices[U3] = tempU;
							tempU = vertices[U2];
							vertices[U2] = vertices[U4];
							vertices[U4] = tempU;
							float tempV = vertices[V1];
							vertices[V1] = vertices[V3];
							vertices[V3] = tempV;
							tempV = vertices[V2];
							vertices[V2] = vertices[V4];
							vertices[V4] = tempV;
							break;
						}
						case Cell.ROTATE_270: {
							float tempV = vertices[V1];
							vertices[V1] = vertices[V4];
							vertices[V4] = vertices[V3];
							vertices[V3] = vertices[V2];
							vertices[V2] = tempV;

							float tempU = vertices[U1];
							vertices[U1] = vertices[U4];
							vertices[U4] = vertices[U3];
							vertices[U3] = vertices[U2];
							vertices[U2] = tempU;
							break;
						}
						}
					}
					batch.draw(region.getTexture(), vertices, 0, NUM_VERTICES);
					
				}
				x += layer_tilewidth;
			}
			y -= layer_tileheight;
		}
	}

}
