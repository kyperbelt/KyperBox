package com.kyperbox.ai;

/**
 * this node will invert the result of its child node on failure/success while
 * child is not running
 * 
 * @author john
 *
 */
public class InvertNode extends SupplementNode {

	@Override
	public void init() {
		super.init();
		getChild().init();
	}

	@Override
	public NodeState update(float delta) {
		NodeState result = getChild().internalUpdate(delta);
		if (result == NodeState.Success)
			return NodeState.Failure;
		if (result == NodeState.Failure)
			return NodeState.Success;
		return result;
	}

}