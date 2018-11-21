package com.kyperbox.ai;

/**
 * this node will continuously repeat executing its child node - this is usually
 * used at the top most level of the tree to continuosly
 * 
 * @author john
 *
 */
public class RepeatNode extends SupplementNode {

	private boolean finished = false;
	private int amount;
	private int times_ran;

	public RepeatNode(int amount) {
		this.amount = amount;
	}

	public RepeatNode() {
		this(-1);
	}

	@Override
	public void init() {
		super.init();
		if (getChild() != null)
			getChild().init();
		else {
			throw new NullPointerException("Supplement Nodes must have a child!");
		}
		finished = false;
		times_ran = 1;
	}

	@Override
	public NodeState update(float delta) {
		super.update(delta);
		if (finished) {
			finished = false;
			getChild().init();
			times_ran++;
		}

		NodeState result = getChild().internalUpdate(delta);

		if (times_ran >= amount && amount != -1)
			return result;

		if (result != NodeState.Running)
			finished = true;

		return NodeState.Running;
	}

}
