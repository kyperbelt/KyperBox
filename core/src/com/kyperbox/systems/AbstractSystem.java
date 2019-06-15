package com.kyperbox.systems;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.kyperbox.managers.Priority;
import com.kyperbox.objects.GameLayer;

public abstract class AbstractSystem implements Priority{

	private int priority;
	private boolean active;
	private GameLayer layer;

	public AbstractSystem() {
		this(0);
	}

	public AbstractSystem(int priority) {
		this.priority = priority;
		this.active = true;
	}

	public int getPriority() {
		return priority;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

	public void internalAddToLayer(GameLayer layer) {
		this.layer = layer;
		addedToLayer(layer);

	}

	public void internalRemoveFromLayer(GameLayer layer) {
		this.layer = null;
		removedFromLayer(layer);
	}

	public GameLayer getLayer() {
		return layer;
	}

	public void drawDebug(ShapeRenderer shapes) {
		/*TODO: override*/
	}

	public void preDraw(Batch batch, float parentAlpha) {
		/* TODO: override */}

	public void postDraw(Batch batch, float parentAlpha) {
		/* TODO:override */}

	public abstract void update(float delta);

	public abstract void addedToLayer(GameLayer layer);

	public abstract void removedFromLayer(GameLayer layer);

}
