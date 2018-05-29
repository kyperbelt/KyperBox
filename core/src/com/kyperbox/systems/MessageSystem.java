package com.kyperbox.systems;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.kyperbox.controllers.MessageController;
import com.kyperbox.objects.GameObject;
import com.kyperbox.objects.GameObject.GameObjectChangeType;

/**
 * system to send messages between messageControllers within the same layer -
 * you may register other MessageSystems to this one in order to communicate
 * between layers
 * 
 */
public class MessageSystem extends LayerSystem {

	/**
	 * the message queue
	 */
	private Array<GameMessage> queued_messages;
	/** the interval at which to send out queued messages **/
	private float message_interval;
	/** the amount of elapsed time currently passed since last message interval **/
	private float message_elapsed;
	/** the other systems linked to this one **/
	private Array<MessageSystem> linked_systems;
	/** the game objects that have message controllers and their controllers **/
	private ArrayMap<GameObject, MessageController> message_controllers;

	/** the interval at which messages in the queue are served **/
	public float getMessageInterval() {
		return message_interval;
	}

	/**
	 * set the interval at which messages in the queue are served
	 * <p>
	 * default is every two seconds
	 **/
	public void setMessageInterval(float message_interval) {
		this.message_interval = message_interval;
	}

	/**
	 * link this message system to a system on a different layer
	 * <p>
	 * this will allow you to send messages between layers with relative ease
	 **/
	public void linkTo(MessageSystem messagesystem) {
		if (!linked_systems.contains(messagesystem, true)) {
			linked_systems.add(messagesystem);
			messagesystem.linkTo(messagesystem);
		}
	}

	/** get all the other message systems linked to this one **/
	protected Array<MessageSystem> getLinkedSystems() {
		return linked_systems;
	}

	/**
	 * adds a message the queue to send at a later time
	 * 
	 * @param header
	 *            - header of the message - used to filter
	 * @param params
	 *            - parameters of this message
	 */
	public void addMessageToQueue(String header, Object... params) {
		GameMessage g = new GameMessage(header, params);
		queued_messages.add(g);
	}

	/**
	 * adds a message to the queue to send at a later time
	 * 
	 * @param header
	 *            - the header of this message
	 */
	public void addMessageToQueue(String header) {
		addMessageToQueue(header);
	}

	/**
	 * instantly sends a message to all message controllers
	 * 
	 * @param header
	 */
	public void instantMessage(String header, Object... params) {
		instantMessage(header, null, null, params);
	}

	/**
	 * instantly sends a message to all message controllers of objects with the
	 * given object name
	 * 
	 * @param header
	 * @param object_name
	 *            - name of objects to message
	 * @param params
	 */
	public void instantMessage(String header, String object_name, Object... params) {
		instantMessage(header, object_name, null, params);
	}

	/**
	 * instantly sends a message to the specified objects controller
	 * 
	 * @param header
	 * @param object
	 *            - the object to send message to
	 * @param params
	 */
	public void instantMessage(String header, GameObject object, Object... params) {
		instantMessage(header, null, object, params);
	}
	
	protected void instantMessage(String header,String object_name,GameObject object,Object...params) {
		GameMessage m = new GameMessage(header, object_name, object, params);
		sendMessage(m);
	}

	public MessageSystem() {
		queued_messages = new Array<MessageSystem.GameMessage>();
		message_controllers = new ArrayMap<GameObject, MessageController>();
		linked_systems = new Array<MessageSystem>();
		message_elapsed = 0;
		message_interval = 2f; //default every two seconds
	}

	@Override
	public void init(MapProperties properties) {
		message_elapsed = 0;
	}

	@Override
	public void gameObjectAdded(GameObject object, GameObject parent) {
		MessageController mc = object.getController(MessageController.class);
		if (mc != null) {
			message_controllers.put(object, mc);
		}
	}

	@Override
	public void gameObjectChanged(GameObject object, int type, float value) {
		if (type == GameObjectChangeType.CONTROLLER) {
			MessageController mc = object.getController(MessageController.class);
			if (mc != null && !message_controllers.containsKey(object)) {
				message_controllers.put(object, mc);
			} else if (mc == null && message_controllers.containsKey(object)) {
				message_controllers.removeKey(object);
			}
		}
	}

	@Override
	public void gameObjectRemoved(GameObject object, GameObject parent) {
		message_controllers.removeKey(object);
	}

	@Override
	public void update(float delta) {

		message_elapsed += delta;
		if (message_elapsed > message_interval) {
			//send out mesasges
			while (queued_messages.size > 0) {
				GameMessage m = queued_messages.removeIndex(0);
				sendMessage(m);
			}

			//reset message_elapsed
			message_elapsed = 0;
		}
	}

	/**
	 * message sent out from this sytem to all message controllers in this area and
	 * in other systems
	 */
	protected void sendMessage(GameMessage m) {
		if (!recieveMessage(m)) {
			for (int i = 0; i < linked_systems.size; i++) {
				if(linked_systems.get(i).recieveMessage(m))
					break;
			}
		}
	}

	/**
	 * message recieved from other message systems return true if it was handled
	 * (only relevant to object specific messages)
	 **/
	protected boolean recieveMessage(GameMessage m) {
		if (m.hasObject()) {
			//message to specific object
			for (int i = 0; i < message_controllers.size; i++) {
				GameObject o = message_controllers.getKeyAt(i);
				if (o == m.getGameObject()) {
					MessageController mc = message_controllers.getValueAt(i);
					mc.recieveMessage(m.getHeader(), m.getParameters());
					return true;
				}
			}
		} else if (m.hasObjectName()) {
			//message to objects of specific name
			for (int i = 0; i < message_controllers.size; i++) {
				GameObject o = message_controllers.getKeyAt(i);
				if (o.getName().equals(m.getObjectName())) {
					MessageController mc = message_controllers.getValueAt(i);
					mc.recieveMessage(m.getHeader(), m.getParameters());
				}
			}
		} else {
			for (int i = 0; i < message_controllers.size; i++) {
				GameObject o = message_controllers.getKeyAt(i);
				if (o.getName().equals(m.getObjectName())) {
					MessageController mc = message_controllers.getValueAt(i);
					mc.recieveMessage(m.getHeader(), m.getParameters());
				}
			}
		}

		return false;
	}

	@Override
	public void onRemove() {
		queued_messages.clear();
		message_elapsed = 0;
	}

	/**
	 * game message object that contains a header and some generic parameter object
	 * 
	 * @author john
	 *
	 */
	public static class GameMessage {

		/** message header **/
		private String header;

		private Object[] params;

		private String object_name;

		private GameObject object;

		private GameMessage(String header, String object_name, GameObject object, Object... params) {
			this.header = header;
			this.params = params;
			this.object_name = object_name;
			this.object = object;
		}

		private GameMessage(String header, Object... params) {
			this(header, null, null, params);
		}

		private GameMessage(String header, GameObject object, Object... params) {
			this(header, null, object, params);
		}

		private GameMessage(String header, String object_name, Object... params) {
			this(header, object_name, null, params);
		}

		private String getHeader() {
			return header;
		}

		private Object[] getParameters() {
			return params;
		}

		private String getObjectName() {
			return object_name;
		}

		private GameObject getGameObject() {
			return object;
		}

		private boolean hasObject() {
			return object != null;
		}

		private boolean hasObjectName() {
			return object_name != null && !object_name.isEmpty();
		}

		private boolean hasParams() {
			return params != null && params.length > 0;
		}

	}

}
