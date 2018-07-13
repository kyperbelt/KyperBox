package com.kyperbox.ai;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.kyperbox.umisc.UserData;

/**
 * a behaviour tree to control the flow of artificial intelligence
 * 
 * @author john
 *
 */
public class BehaviorTree {
	
	private static NodeFactory node_factory = new NodeFactory();

	private BehaviorNode root; //base node in the btree
	private BehaviorNode current; //currently running node
	private UserData context; //context for this tree
	private NodeState last_result; // the last result - defaults to failure
	private boolean finished;

	public BehaviorTree() {
	}

	public void start(UserData context, BehaviorNode root) {
		finished = false;
		this.context = context;
		this.root = root;
		root.tree = this; //set this as the tree
		root.parent = null;
		this.root.init();
		current = root;
	}

	public void setRoot(BehaviorNode root) {
		this.root = root;
	}

	public UserData getContext() {
		return context;
	}

	public void setContext(UserData context) {
		this.context = context;
	}

	protected void setCurrent(BehaviorNode current) {
		this.current = current;
	}

	/**
	 * the topmost currently running node
	 * 
	 * @return
	 */
	public BehaviorNode getCurrent() {
		return current;
	}

	/**
	 * the root node of the tree
	 * 
	 * @return
	 */
	public BehaviorNode getRoot() {
		return root;
	}

	/**
	 * update the btree and check if its finished
	 * 
	 * @param delta
	 * @return - returns true if it is finished executing
	 */
	public boolean update(float delta) {
		if (!finished) {
			if(root == null)
				throw new NullPointerException("the root node of this behavior tree is null, Try and using its start method to set it.");
			NodeState result = root.update(delta);
			if (result != NodeState.Running) {
				finished = true;
				last_result = result;
			}
		}
		return finished;
	}

	/**
	 * get the last result produced by the root node
	 * 
	 * @return
	 */
	public NodeState lastResult() {
		return last_result == null ? NodeState.Failure : last_result;
	}
	
	
	//static start
	
	public static void registerNode(String name,NodeGetter getter) {
		node_factory.registerNode(name, getter);
	}
	
	@SuppressWarnings("unused")
	private static BehaviorNode getNode(String name) {
		return node_factory.getNode(name);
	}
	
	public static BehaviorNode generateRoot(String data) {
		throw new UnsupportedOperationException("Not Yet Implemented");
	}
	
	
	//static end --

	/**
	 * one of the states a nodes update function can return. If failure or success
	 * is returned then update will no longer be called
	 * 
	 * @author john
	 *
	 */
	public static enum NodeState {
		Failure, //node failed
		Success, //node finished running successfully
		Running //node is currently running
	}

	/**
	 * base tree node class
	 * 
	 * @author john
	 *
	 */
	public static abstract class BehaviorNode {

		protected BehaviorTree tree;
		protected BehaviorNode parent;
		
		public BehaviorNode() {
			
		}

		public BehaviorTree getTree() {
			return tree == null ? getParent().getTree() : tree;
		}

		public BehaviorNode getParent() {
			return parent;
		}

		/**
		 * this method is used to initiate the node - should house any code used to
		 * reset the node to a 'fresh' state
		 */
		public abstract void init();

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
		public abstract NodeState update(float delta);
	}

	//START COMPOSITE NODES 
	//-- These nodes are a composite of several different nodes

	/**
	 * composite node base - contains multiple node children initiation and updating
	 * is left to implementation
	 * 
	 * @author john
	 *
	 */
	public static abstract class CompositeNode extends BehaviorNode {

		private Array<BehaviorNode> nodes;

		/**
		 * add a node to the composite node and return this for chain adding
		 * 
		 * @param node
		 * @return
		 */
		public CompositeNode add(BehaviorNode node) {
			getNodes().add(node);
			node.parent = this;
			return this;
		}

		public Array<BehaviorNode> getNodes() {
			if (nodes == null) {
				nodes = new Array<BehaviorTree.BehaviorNode>();
			}
			return nodes;
		}
	}

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
	public static class SequenceNode extends CompositeNode {

		private boolean finished = false; //used to check if all nodes finished successfully
		private int current; //currently running index;
		private int last; //last index

		@Override
		public void init() {
			finished = false;
			current = 0;
			last = -1;

		}

		@Override
		public NodeState update(float delta) {
			Array<BehaviorNode> nodes = getNodes();

			//if already finished just return success
			if (finished)
				return NodeState.Success;

			if (nodes.size == 0) { //no nodes to fail so default to success
				finished = true;
			} else {

				if (current < nodes.size) { //current index is possible 
					BehaviorNode cnode = nodes.get(current);
					if (current != last) { //if this is the first time running this node initiate it
						cnode.init();
						last = current;
					}
					NodeState result = cnode.update(delta); //get the result of the current node
					if (result == NodeState.Failure) //if its a failure then return it as such - this whole sequence fails
						return result;
					else if (result == NodeState.Success) { //node ran successfully so go to the next one
						current++;
					}
				} else {
					finished = true;
				}

			}
			return NodeState.Running;
		}

	}

	/**
	 * this node returns on its first success - and only returns failure if all
	 * children fail
	 * 
	 * @author john
	 *
	 */
	public static class SelectorNode extends CompositeNode {

		private boolean finished = false; //used to check if all nodes finished successfully
		private int current; //currently running index;
		private int last; //last index

		@Override
		public void init() {
			finished = false;
			current = 0;
			last = -1;

		}

		@Override
		public NodeState update(float delta) {
			Array<BehaviorNode> nodes = getNodes();

			//if already finished just return failure
			if (finished)
				return NodeState.Failure;

			if (nodes.size == 0) { //no nodes to succeed so default to failure
				finished = true;
			} else {

				if (current < nodes.size) { //current index is possible 
					BehaviorNode cnode = nodes.get(current);
					if (current != last) { //if this is the first time running this node initiate it
						cnode.init();
						last = current;
					}
					NodeState result = cnode.update(delta); //get the result of the current node
					if (result == NodeState.Success) //if its a success then return it as such - this whole sequence succeeds
						return result;
					else if (result == NodeState.Failure) { //node ran failure so go to the next one
						current++;
					}
				} else {
					finished = true;
				}

			}
			return NodeState.Running;
		}

	}

	/**
	 * this is the same as {@link com.kyperbox.ai.BehaviorTree.SequenceNode
	 * SequenceNode} but its children are randomized on each init
	 * 
	 * @author john
	 *
	 */
	public static class RandomSequenceNode extends SequenceNode {

		@Override
		public void init() {
			super.init();
			getNodes().shuffle();
		}
	}

	/**
	 * this is the same as {@link com.kyperbox.ai.BehaviorTree.SelectorNode
	 * SelectorNode} but its children are randomized on each init
	 * 
	 * @author john
	 *
	 */
	public static class RandomSelectorNode extends SelectorNode {
		@Override
		public void init() {
			super.init();
			getNodes().shuffle();
		}
	}

	//END COMPOSITE NODES

	//START SUPPLEMENTARY NODES
	//-- These nodes are used to supplement a single child node in some way/shape or form

	/**
	 * base supplementary node that can parent one child
	 * {@link com.kyperbox.ai.BehaviorTree.BehaviorNode BehaviourNode}
	 * 
	 * @author john
	 *
	 */
	public static abstract class SupplementNode extends BehaviorNode {

		private BehaviorNode child;

		public void setChild(BehaviorNode child) {
			this.child = child;
			child.parent = this;
		}

		public BehaviorNode getChild() {
			return child;
		}

	}

	/**
	 * this node will invert the result of its child node on failure/success while
	 * child is not running
	 * 
	 * @author john
	 *
	 */
	public static class InvertNode extends SupplementNode {

		@Override
		public void init() {
			getChild().init();
		}

		@Override
		public NodeState update(float delta) {
			NodeState result = getChild().update(delta);
			if (result == NodeState.Success)
				return NodeState.Failure;
			if (result == NodeState.Failure)
				return NodeState.Success;
			return result;
		}

	}

	/**
	 * this node will always return Success when child is finished running - no
	 * matter result of the child
	 * 
	 * @author john
	 *
	 */
	public static class SuccessNode extends SupplementNode {

		@Override
		public void init() {
			getChild().init();
		}

		@Override
		public NodeState update(float delta) {
			NodeState result = getChild().update(delta);
			if (result != NodeState.Running)
				return NodeState.Success;
			return result;
		}

	}

	/**
	 * this node will continuously repeat executing its child node - this is usually
	 * used at the top most level of the tree to continuosly
	 * 
	 * @author john
	 *
	 */
	public static class RepeatNode extends SupplementNode {

		private boolean finished = false;

		@Override
		public void init() {
			getChild().init();
			finished = false;
		}

		@Override
		public NodeState update(float delta) {
			if (finished) {
				finished = false;
				getChild().init();
			}
			NodeState result = getChild().update(delta);
			if (result != NodeState.Running)
				finished = true;

			return NodeState.Running;
		}

	}

	/**
	 * this node will repeat its child node until it fails.
	 * 
	 * @author john
	 *
	 */
	public static class RepeatUntilFailNode extends SupplementNode {

		private boolean finished = false;

		@Override
		public void init() {
			getChild().init();
			finished = false;
		}

		@Override
		public NodeState update(float delta) {
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

	//END SUPPLEMENTARY NODEs

	/**
	 * 
	 * an object used for the generation of behavior trees using a simpler syntax
	 * 
	 * @author john
	 *
	 */
	public static interface NodeGetter {
		/**
		 * return a node
		 * 
		 * @return
		 */
		public BehaviorNode getNode();
	}

	/**
	 * this will be used to create nodes
	 * 
	 * @author john
	 *
	 */
	private static class NodeFactory {
		ObjectMap<String, NodeGetter> getters;
		private NodeFactory() {
			getters = new ObjectMap<String, BehaviorTree.NodeGetter>();
		}
		
		/**
		 * register a node and its getter
		 * @param nodename
		 * @param getter
		 */
		public void registerNode(String nodename, NodeGetter getter) {
			getters.put(nodename, getter);
		}
		
		public BehaviorNode getNode(String node) {
			if(getters.containsKey(node)) {
				return getters.get(node).getNode();
			}else {
				return null;
			}
		}
	}

}
