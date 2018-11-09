package com.kyperbox.ai;

/**
 * one of the states a nodes update function can return. If failure or success
 * is returned then update will no longer be called
 * 
 * @author john
 *
 */
public enum NodeState {

	Failure, // node failed
	Success, // node finished running successfully
	Running // node is currently running
}
