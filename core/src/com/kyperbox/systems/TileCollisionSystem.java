package com.kyperbox.systems;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kyperbox.controllers.TileCollisionController;
import com.kyperbox.objects.GameObject;
import com.kyperbox.objects.GameObject.GameObjectChangeType;
import com.kyperbox.objects.TilemapLayerObject;
import com.kyperbox.objects.TilemapLayerObject.MapTile;

/**
 * tile collision system to filter collisions
 * @author john
 *
 */
public class TileCollisionSystem extends LayerSystem{
	
	//bt
	private float bullet_threshold; //unit threshold to flag objects as bullets - more intensive collision checks -1 for none
	//lcp
	private float line_crop_percent;
	
	private Array<GameObject> tilemap_objects;
	
	private String tile_layer_name;
	
	private TilemapLayerObject tile_layer;
	
	/**
	 * create a tile collision system with the tile layer name
	 * @param layer_name
	 */
	public  TileCollisionSystem(String layer_name) {
		this.tile_layer_name = layer_name;
		tilemap_objects = new Array<GameObject>();
	}
	
	/**
	 * what speed do we mark flag an object as a bullet
	 * @param pps pixels per second
	 */
	public void setBulletThreshold(float pps) {
		this.bullet_threshold = pps;
	}
	
	/**
	 * the percentage to offset the lines used for checking. 
	 * example (top line is 100 in length and percentage is .05f: line will be cropped by 5% from both sides) 
	 * @param percentage
	 */
	public void setLineCrop(float percentage) {
		this.line_crop_percent = percentage;
	}
	
	/**
	 * set the name of the TilemapLayerObject to use for collision
	 * @param tile_layer_name
	 */
	public void setTileLayerName(String tile_layer_name) {
		this.tile_layer_name = tile_layer_name;
	}
	
	@Override
	public void init(MapProperties properties) {
		tile_layer = (TilemapLayerObject) getLayer().getGameObject(tile_layer_name);
		bullet_threshold = 1000f; //anything moving faster than 1000p/s will be marked as bullet
		line_crop_percent = .05f;
		bullet_threshold = properties.get("bt",bullet_threshold,float.class);
		line_crop_percent = properties.get("lcp",line_crop_percent,float.class);
		
	}
	@Override
	public void gameObjectAdded(GameObject object, GameObject parent) {
		if(object.getController(TileCollisionController.class) == null)
			return;
		tilemap_objects.add(object);
	}
	
	@Override
	public void gameObjectChanged(GameObject object, int type, float value) {
			if(type == GameObjectChangeType.MANAGER) {
				if(value == -1) { //removed 
					if(object.getController(TileCollisionController.class) == null)
						tilemap_objects.removeValue(object, true);
				}else if(value == 1) { //added
					if(object.getController(TileCollisionController.class)!=null&&!tilemap_objects.contains(object, true)) {
						tilemap_objects.add(object);
					}
				}
			}
		
	}
	@Override
	public void gameObjectRemoved(GameObject object, GameObject parent) {
		tilemap_objects.removeValue(object, true);
	}
	@Override
	public void update(float delta) {
		if(tile_layer == null)
			tile_layer = (TilemapLayerObject) getLayer().getGameObject(tile_layer_name);
		if(tile_layer!=null) {
			for (int i = 0; i < tilemap_objects.size; i++) {
				GameObject object = tilemap_objects.get(i);
				TileCollisionController tcc = object.getController(TileCollisionController.class);
				tcc.clear();
				//TODO: clear collision controllers previous collision data here?
				//TODO: do solid collision checks and also send collision details to collision controller
				
				Vector2 pvel = object.getVelocity();
				float minx = 0;
				float miny = 0;
				float maxx = 0;
				float maxy = 0;
				Rectangle object_bounds = object.getCollisionBounds();
				Array<MapTile> tiles = null;
				float mint_off = ((object_bounds.getWidth()))*line_crop_percent;
				float check_off = 1;
				
				
				if(pvel.x <= 0) { //moving left
					minx = object_bounds.getX()+pvel.x*delta-check_off;
					miny = object_bounds.getY()+mint_off; //offset
					
					maxx = minx;
					maxy = object_bounds.getY()+object_bounds.getHeight()-mint_off;
					
					tiles = tile_layer.getCollisionsForSide(minx, miny, maxx, maxy);
					if(addTiles(tiles, tcc)) {
						object.setPosition(tiles.first().getRight()-(object_bounds.getX()-object.getX()), object.getY());
						object.setVelocity(0, pvel.y);
						pvel = object.getVelocity();
					}
					
					tiles = null;
				}
				if(pvel.x >= 0) { //moving right
					minx = object_bounds.getX()+object_bounds.getWidth()+pvel.x*delta+check_off;
					miny = object_bounds.getY()+mint_off;
					
					maxx = minx;
					maxy = object_bounds.getY()+object_bounds.getHeight()-mint_off;
					
					tiles = tile_layer.getCollisionsForSide(minx, miny, maxx, maxy);
					if(addTiles(tiles, tcc)) {
						object.setPosition(tiles.first().getLeft()-object_bounds.getWidth()-(object_bounds.getX()-object.getX()), object.getY()); 
						object.setVelocity(0, pvel.y);
						pvel = object.getVelocity();
					}
					
					tiles = null;
				}
				
				if(pvel.y<=0) { //moving down
					minx = object_bounds.getX()+mint_off;
					miny = object_bounds.getY()+pvel.y*delta-check_off; //offset by 1 pixel
					
					maxx = object_bounds.getX()+object_bounds.getWidth()-mint_off;
					maxy = miny;
					
					tiles = tile_layer.getCollisionsForSide(minx, miny, maxx, maxy);
					if(addTiles(tiles, tcc)) {
						object.setPosition(object.getX(), tiles.first().getTop()+(object_bounds.getY()-object.getY()));
						object.setVelocity(pvel.x, 0);
						pvel = object.getVelocity();
					}
					
					tiles = null;
				}
				if(pvel.y >= 0) {//moving up
					minx = object_bounds.getX()+mint_off;
					miny = object_bounds.getY()+object_bounds.getHeight()+(pvel.y*delta)+check_off;
					
					maxx = object_bounds.getX()+object_bounds.getWidth()-mint_off;
					maxy = miny;
					
					tiles = tile_layer.getCollisionsForSide(minx, miny, maxx, maxy);
					if(addTiles(tiles, tcc)) {
						object.setPosition(object.getX(), tiles.first().getBot()-object_bounds.getHeight()+(object_bounds.getY()-object.getY())); 
						object.setVelocity(pvel.x, 0);
						pvel = object.getVelocity();
					}
					
					tiles = null;
				}
				

			}
		}
	}
	
	/**
	 * returns true if there is a solid collision
	 * @param tiles
	 * @param tcc
	 * @return
	 */
	private boolean addTiles(Array<MapTile> tiles,TileCollisionController tcc) {
		boolean solid_collision = false;
		for (int j = 0; j < tiles.size; j++) {
			MapTile mt = tiles.get(j);
			int type = mt.getType();
			if(tcc.solidAgainst(type)||(type == TileCollisionController.VOID && tcc.collidesWithVoid())) {
				solid_collision = true;
			}
			tcc.addCollision(type,mt);
		}
		return solid_collision;
		
	}
	@Override
	public void onRemove() {
		
	}

}
