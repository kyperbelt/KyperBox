package com.kyperbox.util;

import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;

/**
 * Utilities for saving and loading Data
 *
 */
public class SaveUtils {
	
	/**
	 * load string from given filehandle
	 * @param handle
	 * @return
	 */
	public static String loadFromFile(FileHandle handle) {
		String data = null;
		data = handle.readString();
		return data;
	}
	
	/**
	 * load string data from prefs
	 * @param prefs
	 * @param key
	 * @return
	 */
	public static String loadFromPrefs(Preferences prefs,String key) {
		String data = null;
		data = prefs.getString(key);
		return data;
	}
	
	/**
	 * save data to file
	 * @param data
	 * @param handle
	 * @return false if handle is directory
	 */
	public static boolean saveToFile(String data,FileHandle handle) {
		if(handle.isDirectory())
			return false;
		handle.writeString(data, false);
		return true;
	}
	
	/**
	 * save to prefs
	 * @param prefs
	 * @param name
	 * @param data
	 * @return
	 */
	public static boolean saveToPrefs(Preferences prefs,String name,String data) {
		prefs.putString(name, data);
		prefs.flush();
		return true;
	}
	
	/**
	 * save user data object to prefs
	 * @param prefs
	 * @param data
	 * @return
	 */
	public static boolean saveToPrefs(Preferences prefs,UserData data) {
		saveToPrefs(prefs, data.getName(),data.toJson());
		return true;
	}
	
	public static UserData loadFromPrefs(Preferences prefs,UserData data) {
		data.loadFromJson(prefs.getString(data.getName()));
		return data;
	}

}
