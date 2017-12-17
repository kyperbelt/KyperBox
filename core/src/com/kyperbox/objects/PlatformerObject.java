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
		addGameObjectController(anim_controller);
		addGameObjectController(platform_controller);
		anim_controller.setPlaySpeed(3f);
		
		// create animation
		createAnimations();
		anim_controller.setAnimation("player_walking");
		
		
	}

	private void createAnimations() {
		if (getGame().getAnimation("player_walking") == null) {
			getGame().storeAnimation("player_walking",getGame().createGameAnimation("player_walking", 4, .25f));
		}
		if (getGame().getAnimation("player_jumping") == null) {
			getGame().storeAnimation("player_jumping",getGame().createGameAnimation("player_jumping", 4, .25f));
		}
		if (getGame().getAnimation("player_climbing") == null) {
			getGame().storeAnimation("player_climbing",getGame().createGameAnimation("player_climbing", 4, .25f));
		}
		if (getGame().getAnimation("player_falling") == null) {
			getGame().storeAnimation("player_falling",getGame().createGameAnimation("player_falling", 2, .25f));
		}
	}

}
