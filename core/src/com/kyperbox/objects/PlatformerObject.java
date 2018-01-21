package com.kyperbox.objects;

import com.badlogic.gdx.maps.MapProperties;
import com.kyperbox.controllers.AnimationController;
import com.kyperbox.controllers.PlatformerController;

public class PlatformerObject extends GameObject {

	AnimationController anim_controller;
	PlatformerController platform_controller;

	@Override
	public void init(MapProperties properties) {
		super.init(properties);
		anim_controller = new AnimationController();
		platform_controller = new PlatformerController();
		addController(anim_controller);
		addController(platform_controller);
		anim_controller.setPlaySpeed(3f);
		
		// create animation
		createAnimations();
		anim_controller.setAnimation("player_walking");
		
		
	}

	private void createAnimations() {
		if (getState().getAnimation("player_walking") == null) {
			getState().storeAnimation("player_walking",getState().createGameAnimation("player_walking", 4, .25f));
		}
		if (getState().getAnimation("player_jumping") == null) {
			getState().storeAnimation("player_jumping",getState().createGameAnimation("player_jumping", 4, .25f));
		}
		if (getState().getAnimation("player_climbing") == null) {
			getState().storeAnimation("player_climbing",getState().createGameAnimation("player_climbing", 4, .25f));
		}
		if (getState().getAnimation("player_falling") == null) {
			getState().storeAnimation("player_falling",getState().createGameAnimation("player_falling", 2, .25f));
		}
	}

}
