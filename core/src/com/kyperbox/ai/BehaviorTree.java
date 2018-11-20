package com.kyperbox.ai;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.kyperbox.umisc.UserData;

/**
 * a behaviour tree to control the flow of artificial intelligence
 * 
 * @author john
 *
 */
public class BehaviorTree {
	
	private static NodeFactory node_factory = new NodeFactory();

	private BehaviorNode root; // base node in the btree
	private BehaviorNode current; // currently running node
	private UserData context; // context for this tree
	private NodeState last_result; // the last result - defaults to failure
	private boolean finished;
	private float total_runtime;
	private boolean debug = false;;

	public BehaviorTree() {
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean getDebug() {
		return debug;
	}

	public void start(UserData context, BehaviorNode root) {
		finished = false;
		this.context = context;
		this.root = root;
		root.tree = this; // set this as the tree
		root.parent = null;
		this.root.init();
		current = root;
		total_runtime = 0;
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

	public float getTotalRuntime() {
		return total_runtime;
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

	public void debugRender(ShapeRenderer shapes) {
		if (root == null)
			throw new NullPointerException(
					"the root node of this behavior tree is null, Try and using its start method to set it.");
		root.debugRender(shapes);
	}

	/**
	 * update the btree and check if its finished
	 * 
	 * @param delta
	 * @return - returns true if it is finished executing
	 */
	public boolean update(float delta) {
		if (!finished) {
			if (root == null)
				throw new NullPointerException(
						"the root node of this behavior tree is null, Try and using its start method to set it.");
			NodeState result = root.internalUpdate(delta);
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

	// static starta

	public static void registerNode(String name, NodeGetter getter) {
		node_factory.registerNode(name, getter);
	}

	private static BehaviorNode getNode(String name, JsonValue json) {
		BehaviorNode node = node_factory.getNode(name, json.get("properties"));
		JsonValue children = json.get("children");

		if (children != null && children.child!=null) {
			System.out.println("child of -"+name+" count="+children.size);
			
			for (int i = 0; i < children.child.size; i++) {
				BehaviorNode child = getNode(children.child.get(i).name, children.child.get(i));
				if (child != null) {
					if(node instanceof CompositeNode) {
						((CompositeNode)node).add(child);
					}else if(node instanceof SupplementNode) {
						((SupplementNode)node).setChild(child);
					}
				}
			}
		}
		return node;
	}

	public static BehaviorNode generateRoot(String json_data) {
		JsonValue json = new JsonReader().parse(json_data);
		int last = json.size -1;
		JsonValue root = json.get(last);
		return getNode(root.name, root);
	}

	public static BehaviorNode generateRoot(FileHandle file) {
		return generateRoot(file.readString());
	}

	// static end --





}
