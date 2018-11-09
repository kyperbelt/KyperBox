package com.kyperbox.ai;

/**
 * this is the same as {@link com.kyperbox.ai.BehaviorTree.SelectorNode
 * SelectorNode} but its children are randomized on each init
 * 
 * @author john
 *
 */
public class RandomSelectorNode extends SelectorNode {
	@Override
	public void init() {
		super.init();
		getNodes().shuffle();
	}
}