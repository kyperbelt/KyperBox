package com.kyperbox.ai;

/**
 * this is the same as {@link com.kyperbox.ai.BehaviorTree.SequenceNode
 * SequenceNode} but its children are randomized on each init
 * 
 * @author john
 *
 */
public class RandomSequenceNode extends SequenceNode {

	@Override
	public void init() {
		super.init();
		getNodes().shuffle();
	}
}