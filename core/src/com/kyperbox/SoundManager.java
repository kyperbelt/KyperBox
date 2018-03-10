package com.kyperbox;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.IntFloatMap;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;

public class SoundManager {

	public static final int MASTER = 0;
	public static final int SFX = 1;
	public static final int MUSIC = 2;
	public static final int VOICE = 3;

	private KyperBoxGame game;
	private IntFloatMap tags;
	private ObjectMap<Integer, Music> musics;
	private ObjectMap<String, Sound> sounds;
	private Json json ;

	public SoundManager(KyperBoxGame game) {
		json = new Json();
		this.game = game;
		tags = new IntFloatMap();
		createTag(MASTER);
		createTag(SFX);
		createTag(MUSIC);
		createTag(VOICE);
		musics = new ObjectMap<Integer, Music>();
		sounds = new ObjectMap<String, Sound>();
	}
	
	public String getTagData() {
		return json.toJson(tags, IntFloatMap.class);
	}
	
	public void loadTagData(String data) {
		tags = json.fromJson(IntFloatMap.class, data);
	}

	/**
	 * play a sound with the given tags volume if tag does not exists 1f default
	 * 
	 * @param tag
	 * @param sound
	 * 
	 * @return - the sound id
	 */
	public long playSound(int tag, String sound) {
		Sound s;
		if (!sounds.containsKey(sound)) {
			s = game.getSound(sound);
			sounds.put(sound, s);
		} else
			s = sounds.get(sound);

		return s.play(tags.get(tag, 1f) * tags.get(MASTER, 1f));
	}
	
	public float getTagVolume(int tag) {
		return tags.get(tag, 0f);
	}

	/**
	 * stops all instances of the given sound
	 * 
	 * @param sound
	 */
	public void stopSound(String sound) {
		if (!sounds.containsKey(sound)) {
			return;
		}

		sounds.get(sound).stop();
	}

	/**
	 * stop the instance of the given sound
	 * 
	 * @param sound
	 * @param id
	 */
	public void stopSound(String sound, long id) {
		if (!sounds.containsKey(sound))
			return;
		sounds.get(sound).stop(id);
	}

	/**
	 * play the music with the given tags volume. default 1f return the music object
	 * so you may loop it
	 * 
	 * @param tag
	 * @param music
	 * @return
	 */
	public Music playMusic(int tag, String music, boolean looping) {
		if (musics.containsKey(tag) && musics.get(tag) != null) {
			musics.get(tag).stop();
		}
		Music m = game.getMusic(music);
		m.setVolume(tags.get(tag, 1f) * tags.get(MASTER, 1f));
		musics.put(tag, m);
		m.setLooping(looping);
		m.play();
		return m;
	}

	public Music playMusic(int tag, String music) {
		return playMusic(tag, music, false);
	}

	/**
	 * get the music currently playing in the tag
	 * 
	 * @param tag
	 * @return null if no music currently playing on tag
	 */
	public Music getMusicByTag(int tag) {
		if (!musics.containsKey(tag))
			return null;
		return musics.get(tag);
	}

	/**
	 * stop the music at the given tag
	 * 
	 * @param tag
	 */
	public void stopMusic(int tag) {
		Music m = getMusicByTag(tag);
		if (m != null)
			m.stop();
	}
	
	public Music getMusic(String music) {
		Music m = game.getMusic(music);
		if (m == null)
			return null;
		return m;
	}

	/**
	 * resume the music playing on the given tag
	 * 
	 * @param tag
	 */
	public void resumeMusic(int tag) {
		Music m = getMusicByTag(tag);
		if (m != null)
			m.play();
	}

	/**
	 * pause the music playing at the given tag
	 * 
	 * @param tag
	 */
	public void pauseMusic(int tag) {
		Music m = getMusicByTag(tag);
		if (m != null)
			m.pause();
	}

	/**
	 * change the volume associated with a tag
	 * 
	 * @param tag
	 * @param value
	 */
	public void changeVolume(int tag, float value) {
		if (tags.containsKey(tag)) {
			tags.put(tag, MathUtils.clamp(value, 0f, 1f));
			Music m = musics.get(tag);
			if (m != null)
				m.setVolume(tags.get(MASTER, 1f) * tags.get(tag, 1f));
		} else
			KyperBoxGame.error("SoundManager", "tag [" + tag + "] not found");
	}

	/**
	 * create a tag.
	 * 
	 * @param tag
	 */
	public void createTag(int tag) {
		if (tags.containsKey(tag)) {
			KyperBoxGame.error("SoundManager", "tag [" + tag + "] already exists");
			return;
		}
		tags.put(tag, 1f);
	}

	/**
	 * stops all music and sounds in this soundmanager
	 */
	public void stopAll() {
		stopSounds();
		stopMusic();
	}

	/**
	 * stops all sounds playing but keeps them in soundmanager
	 */
	public void stopSounds() {
		while (sounds.values().hasNext()) {
			Sound s = sounds.values().next();
			s.stop();
		}
	}

	/**
	 * stops all music currently playing but keeps it in soundmanager
	 */
	public void stopMusic() {
		while (musics.values().hasNext()) {
			Music m = musics.values().next();
			m.stop();
		}
	}

	/**
	 * stops and clears all sounds
	 * <p>
	 * Note: this does not unload the sounds from memory
	 */
	public void clearSounds() {
		stopSounds();
		sounds.clear();
	}

	/**
	 * stops and clears all music
	 * <p>
	 * Note: this does not unload the music from memory
	 */
	public void clearMusic() {
		stopMusic();
		musics.clear();
	}

	/**
	 * stops and clears all sounds and music on this soundmanager
	 */
	public void clearAll() {
		clearSounds();
		clearMusic();
	}

	/**
	 * stops and removes all instances of that sound
	 * 
	 * @param sound
	 */
	public void removeSound(String sound) {
		if(!sounds.containsKey(sound)) {
			return;
		}
		sounds.remove(sound).stop();
	}

	/**
	 * stops and removes the music if it is currently playing under any tag
	 * 
	 * @param music
	 */
	public void removeMusic(String music) {
		Music m = game.getMusic(music);
		if (m == null)
			return;
		for (Entry<Integer, Music> entry : musics) {
			if (entry.value == m) {
				musics.remove(entry.key);
				entry.value.stop();
				break;
			}

		}
	}

}
