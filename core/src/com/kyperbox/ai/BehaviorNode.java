package com.kyperbox.ai;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.TimeUtils;
import com.kyperbox.umisc.UserData;

/**
 * base tree node class
 * 
 * @author john
 *
 */
public abstract class BehaviorNode {

	protected BehaviorTree tree;
	protected BehaviorNode parent;
	private NodeState state;
	protected float total_runtime;
	protected float start_time;

	public BehaviorNode() {
		state = NodeState.Running;
	}

	protected NodeState setState(NodeState state) {
		this.state = state;
		return state;
	}

	public NodeState getState() {
		return state;
	}

	public BehaviorTree getTree() {
		return tree == null ? getParent().getTree() : tree;
	}

	public BehaviorNode getParent() {
		return parent;
	}

	public float getTotalRuntime() {
		return total_runtime;
	}

	public UserData getContext() {
		return getTree().getContext();
	}

	public void debugRender(ShapeRenderer render) {

	}

	/**
	 * this method is used to initiate the node - should house any code used to
	 * reset the node to a 'fresh' state
	 */
	public void init() {
		setState(NodeState.Running);
		getTree().setCurrent(this);
		total_runtime = 0;
		start_time = TimeUtils.nanoTime();
	}
	
	public NodeState internalUpdate(float delta) {
		NodeState state = this.update(delta);
		total_runtime = (float) ((TimeUtils.nanoTime() - start_time) / 1000000000.0);
		setState(state);
		return state;
	}

	/**
	 * Bread and butter method of all Behavior nodes. If the update should continue
	 * then {@link NodeState.Running} should be returned. If there is something
	 * preventing the execution of this method then {@link NodeState.Failure} Should
	 * be returned. Otherwise if The execution finished successfuly then
	 * {@link NodeState.Success} Should be returned.
	 * 
	 * @param delta
	 * @return
	 */
	public NodeState update(float delta) {
		
		return NodeState.Running;
	}
}