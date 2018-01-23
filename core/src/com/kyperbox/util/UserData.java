package com.kyperbox.util;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.yarn.Dialogue.VariableStorage;

/**
 * A data table that stores string pairs for later retrieval. global instance in
 * {@link com.kyperbox.KyperBoxGame KyperBoxGame} and each
 * {@link com.kyperbox.GameState GameState} has its own instance.
 * 
 *
 */
public class UserData implements VariableStorage{

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
		variables.put(name, object);
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

	@Override
	public void setValue(String name, Object value) {
		put(name, value);
	}

	@Override
	public <t> t getValue(String name, Class<t> type) {
		if (variables.containsKey(name))
			return type.cast(variables.get(name));
		KyperBoxGame.error("UserData", " value not found ["+name+"]");
		return null;
	}

	@Override
	public void clear() {
		variables.clear();
	}
}
