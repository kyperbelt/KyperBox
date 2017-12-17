package com.kyperbox;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntFloatMap;
import com.badlogic.gdx.utils.ObjectMap;

public class SoundManager {
	
	public static final int MASTER_VOLUME = 0;
	
	private KyperBoxGame game;
	private IntFloatMap tags;
	private ObjectMap<Integer,Array<Music>> musics;

	public SoundManager(KyperBoxGame game) {
		this.game = game;
		tags = new IntFloatMap();
		createTag(MASTER_VOLUME);
		musics = new ObjectMap<Integer,Array<Music>>();
	}
	
	/**
	 * play a sound with the given tags volume
	 * if tag does not exists 1f default
	 * @param tag
	 * @param sound
	 */
	public void playSound(int tag,String sound){
		game.getSound(sound).play(tags.get(tag, 1f)*tags.get(MASTER_VOLUME, 1f));
	}
	
	/**
	 * play the music with the given tags volume. default 1f
	 * return the music object so you may loop it
	 * @param tag
	 * @param music
	 * @return
	 */
	public Music playMusic(int tag,String music) {
		Music m = game.getMusic(music);
		m.setVolume(tags.get(tag, 1f)*tags.get(MASTER_VOLUME, 1f));
		musics.get(tag).add(m);
		return m;
	}
	
	public Array<Music> getMusicByTag(int tag){
		return musics.get(tag);
	}
	
	/**
	 * change the volume associated with a tag
	 * @param tag
	 * @param value
	 */
	public void changeVolume(int tag,float value) {
		if(tags.containsKey(tag)) {
			tags.put(tag, MathUtils.clamp(value, 0f, 1f));
			for(Music m : getMusicByTag(tag)) {
				m.setVolume(tags.get(tag, 1f));
			}
		}else
			KyperBoxGame.error("SoundManager","tag ["+tag+"] not found");
	} 
	
	/**
	 * create a tag.
	 * @param tag
	 */
	public void createTag(int tag) {
		if(tags.containsKey(tag)) {
			KyperBoxGame.error("SoundManager","tag ["+tag+"] already exists");
			return;
		}
		tags.put(tag,1f);
	}
	
	public void stopAll() {
		
	}
}
