package com.kyperbox.ai;

import com.badlogic.gdx.utils.Array;

/**
 * this node returns on its first success - and only returns failure if all
 * children fail
 * 
 * @author john
 *
 */
public class SelectorNode extends CompositeNode {

	private boolean finished = false; // used to check if all nodes finished successfully
	private int current; // currently running index;
	private int last; // last index

	@Override
	public void init() {
		super.init();
		finished = false;
		current = 0;
		last = -1;

	}

	@Override
	public NodeState update(float delta) {
		super.update(delta);
		Array<BehaviorNode> nodes = getNodes();

		// if already finished just return failure
		if (finished)
			return setState(NodeState.Failure);

		if (nodes.size == 0) { // no nodes to succeed so default to failure
			finished = true;
		} else {

			if (current < nodes.size) { // current index is possible
				BehaviorNode cnode = nodes.get(current);
				if (current != last) { // if this is the first time running this node initiate it
					cnode.init();
					last = current;
				}
				NodeState result = cnode.update(delta); // get the result of the current node
				if (result == NodeState.Success) // if its a success then return it as such - this whole sequence
													// succeeds
					return setState(result);
				else if (result == NodeState.Failure) { // node ran failure so go to the next one
					current++;
				}
			} else {
				finished = true;
			}

		}
		return setState(NodeState.Running);
	}

}
