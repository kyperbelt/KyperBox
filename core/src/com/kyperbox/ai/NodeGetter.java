package com.kyperbox.ai;

import com.badlogic.gdx.utils.JsonValue;

/**
 * 
 * an object used for the generation of behavior trees using a simpler syntax
 * 
 * @author john
 *
 */
public interface NodeGetter {
	/**
	 * return a node
	 * 
	 * @return
	 */
	public BehaviorNode getNode(JsonValue properties);
}
