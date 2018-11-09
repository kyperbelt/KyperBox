package com.kyperbox.ai;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.kyperbox.KyperBoxGame;
import com.kyperbox.umisc.StringUtils;

/**
 * this will be used to create nodes
 * 
 * @author john
 *
 */
public class NodeFactory {
	ObjectMap<String, NodeGetter> getters;

	public NodeFactory() {
		getters = new ObjectMap<String,NodeGetter>();
	}

	/**
	 * register a node and its getter
	 * 
	 * @param nodename
	 * @param getter
	 */
	public void registerNode(String nodename, NodeGetter getter) {
		getters.put(nodename, getter);
	}

	public BehaviorNode getNode(String node, JsonValue json) {
		if (getters.containsKey(node)) {

			return getters.get(node).getNode(json);
		} else {
			KyperBoxGame.error("BehaviorTree",StringUtils.format("Node[%s] not found in node factory.", node));
			return null;
		}
	}
}