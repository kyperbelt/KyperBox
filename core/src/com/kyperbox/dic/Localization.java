package com.kyperbox.dic;

import com.badlogic.gdx.utils.ObjectMap;

public class Localization {
	
	protected String local;
	private ObjectMap<String,String> strings;
	
	public Localization(String local) {
		this.local = local;
		strings = new ObjectMap<String, String>();
	}
	
	public ObjectMap<String,String> getStrings(){
		return strings;
	}
	
	public String getLocalName() {
		return local;
	}
	
	public void add(String key,String string) {
		strings.put(key, string);
	}
	
	public String get(String key) {
		if(strings.containsKey(key)) {
			return strings.get(key);
		}
		return null;
	}
	

}
