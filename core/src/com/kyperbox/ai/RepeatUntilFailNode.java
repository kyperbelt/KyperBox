package com.kyperbox.ai;

/**
 * this node will repeat its child node until it fails.
 * 
 * @author john
 *
 */
public class RepeatUntilFailNode extends SupplementNode {

	private boolean finished = false;

	@Override
	public void init() {
		super.init();
		getChild().init();
		finished = false;
	}

	@Override
	public NodeState update(float delta) {
		super.update(delta);
		if (finished) {
			finished = false;
			getChild().init();
		}

		NodeState result = getChild().update(delta);
		if (result != NodeState.Running && result != NodeState.Failure)
			finished = true;
		else
			return result;

		return NodeState.Running;
	}

}
