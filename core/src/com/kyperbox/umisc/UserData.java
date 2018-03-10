package com.kyperbox.umisc;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.kyperbox.KyperBoxGame;

/**
 * A data table that stores string pairs for later retrieval. global instance in
 * {@link com.kyperbox.KyperBoxGame KyperBoxGame} and each
 * {@link com.kyperbox.GameState GameState} has its own instance.
 * 
 *
 */
public class UserData {

	private static final String NAME = "$USERDATA_NAME";
	private static final String NULL_STRING = "NULL_STRING";

	private ObjectMap<String, Object> variables;
	private static Json MYFRIEND;

	private String name;

	public UserData(String name) {
		this.name = name;
		variables = new ObjectMap<String, Object>();
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void put(String name, Object object) {
		if (variables.containsKey(name) && KyperBoxGame.DEBUG_LOGGING) {
			KyperBoxGame.error(name, "[" + name + "] already exists in " + getName() + ". it was overriden.");
		}
		variables.put(name, object);
	}

	public boolean contains(String name) {
		return variables.containsKey(name);
	}

	public String getString(String name) {
		if (variables.containsKey(name))
			return (String) variables.get(name);
		else
			return NULL_STRING;
	}

	public float getFloat(String name) {
		if (variables.containsKey(name))
			return (Float) (variables.get(name));
		else
			return -1f;
	}

	public int getInt(String name) {
		if (variables.containsKey(name))
			return (Integer) (variables.get(name));
		else
			return -1;
	}

	public boolean getBoolean(String name) {
		if (variables.containsKey(name))
			return (Boolean) (variables.get(name));
		else
			return false;
	}
	
	public Object get(String name) {
		if (variables.containsKey(name))
			return (Boolean) (variables.get(name));
		return null;
	}
	
	public <t> t get(String name,Class<t> clazz){
		if (variables.containsKey(name))
			return clazz.cast(variables.get(name));
		return null;
	}
	
	public <t> t get(String name,t dvalue,Class<t> clazz) {
		t ret = get(name,clazz);
		if(ret == null)
			return dvalue;
		else
			return ret;
	} 

	public void setFloat(String name, float value) {
		variables.put(name, value);
	}

	public void setInt(String name, int value) {
		variables.put(name, value);
	}

	public void setBoolean(String name, boolean value) {
		variables.put(name, value);
	}

	public void setString(String name, String value) {
		variables.put(name, value);
	}
	
	public Object remove(String name) {
		return variables.remove(name);
	}
	
	public void clear() {
		variables.clear();
	}

	@SuppressWarnings("unchecked")
	public boolean loadFromJson(String json) {
		if (MYFRIEND == null)
			MYFRIEND = new Json();
		ObjectMap<String, Object> vv = MYFRIEND.fromJson(ObjectMap.class, json);
		if (vv != null)
			setName((String) vv.remove(NAME));
		variables = vv != null ? vv : variables;
		return vv != null;

	}

	public String toJson() {
		variables.put(NAME, getName());
		if (MYFRIEND == null)
			MYFRIEND = new Json();
		return MYFRIEND.toJson(variables);
	}
}
