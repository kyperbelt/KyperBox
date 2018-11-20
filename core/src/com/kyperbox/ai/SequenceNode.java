package com.kyperbox.ai;

import com.badlogic.gdx.utils.Array;

/**
 * a sequence of nodes that get executed in the order they were added and is not
 * done running until it either finishes the running the last child node or it
 * encounters its first failure
 * <p>
 * stops and returns failure on the first failure encountered
 * 
 * @author john
 *
 */
public class SequenceNode extends CompositeNode {

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

		// if already finished just return success
		if (finished)
			return setState(NodeState.Success);

		if (nodes.size == 0) { // no nodes to fail so default to success
			finished = true;
		} else {

			if (current < nodes.size) { // current index is possible
				BehaviorNode cnode = nodes.get(current);
				if (current != last) { // if this is the first time running this node initiate it
					cnode.init();
					last = current;
				}
				NodeState result = cnode.internalUpdate(delta); // get the result of the current node
				if (result == NodeState.Failure) { // if its a failure then return it as such - this whole sequence
													// fails
					return setState(result);
				}else if (result == NodeState.Success) { // node ran successfully so go to the next one
					current++;
				}
			} else {
				finished = true;
			}

		}
		return setState(NodeState.Running);
	}

}
