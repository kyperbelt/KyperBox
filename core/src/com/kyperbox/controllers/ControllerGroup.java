package com.kyperbox.controllers;

import com.badlogic.gdx.utils.Bits;
import com.badlogic.gdx.utils.ObjectMap;
import com.kyperbox.objects.GameObject;
import com.kyperbox.objects.GameObjectController;

public class ControllerGroup {
	private static ObjectMap<String, ControllerGroup> groups = new ObjectMap<String, ControllerGroup>();
	private static int ControllerGroupIndex = 0;
	private static final Builder builder = new Builder();
	private static final Bits zeroBits = new Bits();

	private final Bits all;
	private final Bits one;
	private final Bits exclude;
	private final int index;

	/** Private constructor, use static method ControllerGroup.getControllerGroupFor() */
	private ControllerGroup (Bits all, Bits any, Bits exclude) {
		this.all = all;
		this.one = any;
		this.exclude = exclude;
		this.index = ControllerGroupIndex++;
	}

	/** @return This ControllerGroup's unique index */
	public int getIndex () {
		return this.index;
	}

	/** @return Whether the WorldObject matches the ControllerGroup requirements or not */
	public boolean matches (GameObject object) {
		Bits worldObjectGameObjectControllerBits = object.getControllerBits();

		if (!worldObjectGameObjectControllerBits.containsAll(all)) {
			return false;
		}

		if (!one.isEmpty() && !one.intersects(worldObjectGameObjectControllerBits)) {
			return false;
		}

		if (!exclude.isEmpty() && exclude.intersects(worldObjectGameObjectControllerBits)) {
			return false;
		}

		return true;
	}

	/**
	 * @param ControllerTypes world objects will have to contain all of the specified GameObjectControllers.
	 * @return A Builder singleton instance to get a ControllerGroup
	 */
	@SafeVarargs
	public static final Builder all (Class<? extends GameObjectController>... ControllerTypes) {
		return builder.reset().all(ControllerTypes);
	}

	/**
	 * @param ControllerTypes world objects will have to contain at least one of the specified GameObjectControllers.
	 * @return A Builder singleton instance to get a ControllerGroup
	 */
	@SafeVarargs
	public static final Builder one (Class<? extends GameObjectController>... ControllerTypes) {
		return builder.reset().one(ControllerTypes);
	}

	/**
	 * @param ControllerTypes world objects cannot contain any of the specified GameObjectControllers.
	 * @return A Builder singleton instance to get a ControllerGroup
	 */
	@SafeVarargs
	public static final Builder exclude (Class<? extends GameObjectController>... ControllerTypes) {
		return builder.reset().exclude(ControllerTypes);
	}

	public static class Builder {
		private Bits all = zeroBits;
		private Bits one = zeroBits;
		private Bits exclude = zeroBits;

		Builder() {
			
		}
		
		/**
		 * Resets the builder instance
		 * @return A Builder singleton instance to get a ControllerGroup
		 */
		public Builder reset () {
			all = zeroBits;
			one = zeroBits;
			exclude = zeroBits;
			return this;
		}

		/**
		 * @param ControllerTypes world objects will have to contain all of the specified GameObjectControllers.
		 * @return A Builder singleton instance to get a ControllerGroup
		 */
		@SafeVarargs
		public final Builder all (Class<? extends GameObjectController>... ControllerTypes) {
			all = ControllerType.getBitsFor(ControllerTypes);
			return this;
		}

		/**
		 * @param ControllerTypes world objects will have to contain at least one of the specified GameObjectControllers.
		 * @return A Builder singleton instance to get a ControllerGroup
		 */
		@SafeVarargs
		public final Builder one (Class<? extends GameObjectController>... ControllerTypes) {
			one = ControllerType.getBitsFor(ControllerTypes);
			return this;
		}

		/**
		 * @param ControllerTypes world objects cannot contain any of the specified GameObjectControllers.
		 * @return A Builder singleton instance to get a ControllerGroup
		 */
		@SafeVarargs
		public final Builder exclude (Class<? extends GameObjectController>... ControllerTypes) {
			exclude = ControllerType.getBitsFor(ControllerTypes);
			return this;
		}

		/** @return A ControllerGroup for the configured GameObjectController types */
		public ControllerGroup get () {
			String hash = getControllerGroupHash(all, one, exclude);
			ControllerGroup ControllerGroup = groups.get(hash, null);
			if (ControllerGroup == null) {
				ControllerGroup = new ControllerGroup(all, one, exclude);
				groups.put(hash, ControllerGroup);
			}
			return ControllerGroup;
		}
	}

	@Override
	public int hashCode () {
		return index;
	}

	@Override
	public boolean equals (Object obj) {
		return this == obj;
	}

	private static String getControllerGroupHash (Bits all, Bits one, Bits exclude) {
		StringBuilder stringBuilder = new StringBuilder();
		if (!all.isEmpty()) {
			stringBuilder.append("{all:").append(getBitsString(all)).append("}");
		}
		if (!one.isEmpty()) {
			stringBuilder.append("{one:").append(getBitsString(one)).append("}");
		}
		if (!exclude.isEmpty()) {
			stringBuilder.append("{exclude:").append(getBitsString(exclude)).append("}");
		}
		return stringBuilder.toString();
	}

	private static String getBitsString (Bits bits) {
		StringBuilder stringBuilder = new StringBuilder();

		int numBits = bits.length();
		for (int i = 0; i < numBits; ++i) {
			stringBuilder.append(bits.get(i) ? "1" : "0");
		}

		return stringBuilder.toString();
	}
}
