package com.kyperbox.ai;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

/**
 * composite node base - contains multiple node children initiation and updating
 * is left to implementation
 * 
 * @author john
 *
 */
public abstract class CompositeNode extends BehaviorNode {

	private Array<BehaviorNode> nodes;

	/**
	 * add a node to the composite node and return this for chain adding
	 * 
	 * @param node
	 * @return
	 */
	public CompositeNode add(BehaviorNode node) {
		getNodes().add(node);
		node.parent = this;
		return this;
	}

	public Array<BehaviorNode> getNodes() {
		if (nodes == null) {
			nodes = new Array<BehaviorNode>();
		}
		return nodes;
	}

	@Override
	public void debugRender(ShapeRenderer render) {

		for (int i = 0; i < nodes.size; i++) {
			nodes.get(i).debugRender(render);
		}
	}

}
