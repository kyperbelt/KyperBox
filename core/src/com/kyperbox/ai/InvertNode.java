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
		super.update(delta);
		NodeState result = getChild().update(delta);
		if (result == NodeState.Success)
			return setState(NodeState.Failure);
		if (result == NodeState.Failure)
			return setState(NodeState.Success);
		return setState(result);
	}

}