package com.kyperbox.ai;

/**
 * this node will always return Success when child is finished running - no
 * matter result of the child
 * 
 * @author john
 *
 */
public class SuccessNode extends SupplementNode {

	@Override
	public void init() {
		super.init();
		getChild().init();
	}

	@Override
	public NodeState update(float delta) {
		super.update(delta);
		NodeState result = getChild().internalUpdate(delta);
		if (result != NodeState.Running)
			return NodeState.Success;
		return result;
	}

}