package com.kyperbox.util;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.kyperbox.KyperBoxGame;

public class UserData {
	
	private ObjectMap<String, String> variables;
	private static Json MYFRIEND;
	
	private String name;
	
	public UserData(String name) {
		this.name = name;
		variables = new ObjectMap<String, String>();
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void put(String name,String object) {
		if(variables.containsKey(name)) {
			KyperBoxGame.error(name, "["+name+"] already exists.");
		}else {
			variables.put(name, object);
		}
	}
	
	public String getString(String name) {
		return variables.get(name);
	}
	
	public float getFloat(String name) {
		return Float.parseFloat(variables.get(name));
	}
	
	public int getInt(String name) {
		return Integer.parseInt(variables.get(name));
	}
	
	public boolean getBoolean(String name) {
		return Boolean.parseBoolean(variables.get(name));
	}
	
	public void setFloat(String name,float value) {
		variables.put(name, ""+value);
	}
	
	public void setInt(String name,int value) {
		variables.put(name, ""+value);
	}
	
	public void setBoolean(String name,boolean value) {
		variables.put(name, Boolean.toString(value));
	}
	
	public void setString(String name,String value) {
		variables.put(name, value);
	}
	
	@SuppressWarnings("unchecked")
	public boolean loadFromJson(String json) {
		if(MYFRIEND == null)
			MYFRIEND = new Json();
		ObjectMap<String,String> vv = MYFRIEND.fromJson(ObjectMap.class, json);
		variables = vv !=null? vv:variables;
		return vv != null;
	
	}
	
	public String toJson() {
		if(MYFRIEND == null)
			MYFRIEND = new Json();
		return MYFRIEND.toJson(variables);
	}
}
