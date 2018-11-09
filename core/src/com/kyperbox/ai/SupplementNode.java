package com.kyperbox.ai;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * base supplementary node that can parent one child
 * {@link com.kyperbox.ai.BehaviorTree.BehaviorNode BehaviourNode}
 * 
 * @author john
 *
 */
public abstract class SupplementNode extends BehaviorNode {

	private BehaviorNode child;

	public BehaviorNode setChild(BehaviorNode child) {
		this.child = child;
		child.parent = this;
		return this;
	}

	public BehaviorNode getChild() {
		return child;
	}

	@Override
	public void debugRender(ShapeRenderer render) {
		getChild().debugRender(render);
	}

}
