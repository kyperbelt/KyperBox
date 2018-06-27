package com.kyperbox.controllers;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Pool;
import com.kyperbox.objects.GameObject;
import com.kyperbox.objects.GameObjectController;
import com.kyperbox.objects.TilemapLayerObject;
import com.kyperbox.objects.TilemapLayerObject.MapTile;

public class TileCollisionController extends GameObjectController{
	
	public static final int VOID = -1;
	public static final int WALL = 0;
	public static final int HAZARD = 1;
	
	/**
	 * filters - these should be cleared externally (pref by a layer system)
	 */
	private IntArray solid_filter; //what types are considered solid
	private IntArray current_collisions; //the current collisions types for this object
	private Array<MapTile> collision_map_tiles; //the current collision MapTiles
	private Pool<MapTile> tile_pool;
	private boolean collide_with_void;
	
	public TileCollisionController() {
		collide_with_void = true;
	}
	
	public void collideWithVoid(boolean collide) {
		this.collide_with_void = collide;
	}
	
	/**
	 * checks to see if this treats void tiles as solid--
	 * void tiles are not empty tiles, simply tiles that do not 
	 * have a type
	 * @return
	 */
	public boolean collidesWithVoid() {
		return collide_with_void;
	}
	
	@Override
	public void init(GameObject object) {
		solid_filter = new IntArray();
		solid_filter.add(WALL);
		current_collisions = new IntArray();
		collision_map_tiles = new Array<TilemapLayerObject.MapTile>();
		tile_pool = new Pool<TilemapLayerObject.MapTile>() {
			@Override
			protected MapTile newObject() {
				return new MapTile();
			}
		};
	}
	
	public void addCollision(int type,MapTile tile) {
		MapTile mt = tile_pool.obtain();
		mt.init(tile.getX(), tile.getY(), tile.getWidth(), tile.getHeight(), tile.getCell());
		current_collisions.add(type);
		collision_map_tiles.add(mt);
	}
	
	public MapTile getCollisionTile(int index){
		return collision_map_tiles.get(index);
	}
	
	public IntArray getCurrentCollisionTypes() {
		return current_collisions;
	}
	
	public Array<MapTile> getCurrentCollisionTiles(){
		return collision_map_tiles;
	}
	
	/**
	 * get the solid filter for this controller
	 * @return
	 */
	public IntArray getSolidFilter() {
		return solid_filter;
	}
	
	/**
	 * add solid type to the solid filter. Causing collisions with this type
	 * of tile to be treated as solids. 
	 * @param solid
	 */
	public void addSolidType(int solid) {
		if(solid_filter.contains(solid))
			return;
		solid_filter.add(solid);
	}
	
	/**
	 * see {@link #addSolidType(int)}
	 * @param solid_types
	 */
	public void addSolidTypes(int...solid_types) {
		for (int i = 0; i < solid_types.length; i++) {
			addSolidType(solid_types[i]);
		}
	}
	
	public void removeSolidType(int solid) {
		solid_filter.removeValue(solid);
	}
	
	public void removeSolidTypes(int...solids) {
		for (int i = 0; i < solids.length; i++) {
			removeSolidType(solids[i]);
		}
	}
	
	/**
	 * clears the solid filter -- will result in no solid collision handling
	 */
	public void clearSolids() {
		solid_filter.clear();
	}
	
	/**
	 * check to see if the type should be considered solid
	 * @param type
	 * @return
	 */
	public boolean solidAgainst(int type) {
		return solid_filter.contains(type);
	}

	@Override
	public void update(GameObject object, float delta) {
		 //no update
	}
	
	public void clear(){
		current_collisions.clear();
		for (int i = 0; i < collision_map_tiles.size; i++) {
			tile_pool.free(collision_map_tiles.get(i));
		}
		collision_map_tiles.clear();
	}

	@Override
	public void remove(GameObject object) {
		clear();
	}
	
	

}
