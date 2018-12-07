
/**
 * from : https://github.com/libgdx/ashley/blob/master/ashley/src/com/badlogic/ashley/core/ComponentType.java
 */
package com.kyperbox.controllers;

import com.badlogic.gdx.utils.Bits;
import com.badlogic.gdx.utils.ObjectMap;
import com.kyperbox.objects.GameObjectController;

public class ControllerType {
	private static ObjectMap<Class<? extends GameObjectController>, ControllerType> controllerTypes = new ObjectMap<Class<? extends GameObjectController>, ControllerType>();
	private static int typeIndex = 0;
	
	private final int index;
	
	private ControllerType() {
		index = typeIndex++;
	}
	
	public int getIndex() {
		return index;
	}
	
	public static ControllerType getFor(Class<? extends GameObjectController> controllerType) {
		
		ControllerType type = controllerTypes.get(controllerType);
		
		if(type == null) {
			type = new ControllerType();
			controllerTypes.put(controllerType, type);
		}
		
		return type;
		
	}
	
	public static int getIndexFor(Class<? extends GameObjectController> controllerType) {
		return getFor(controllerType).getIndex();
	}
	
	public static Bits getBitsFor (Class<? extends GameObjectController>... controllerTypes) {
		Bits bits = new Bits();

		int typesLength = controllerTypes.length;
		for (int i = 0; i < typesLength; i++) {
			bits.set(ControllerType.getIndexFor(controllerTypes[i]));
		}

		return bits;
	}
	
	public int hashCode () {
		return index;
	}
	
	@Override
	public boolean equals (Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ControllerType other = (ControllerType)obj;
		return index == other.index;
	}
	
}
