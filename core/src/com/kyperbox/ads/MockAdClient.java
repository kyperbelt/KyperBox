package com.kyperbox.ads;

import com.badlogic.gdx.Gdx;
import com.kyperbox.KyperBoxGame;

public class MockAdClient implements AdClient{

	private static final String SHOW_ADFEATURE = "showAdFeature ";
	private static final String HIDE_ADFEATURE = "hideAdFeature ";
	
	
	@Override
	public boolean isSupported(AdFeature feature) {
		return false;
	}

	@Override
	public void showAdFeature(AdFeature feature) {
		Gdx.app.log(KyperBoxGame.NOT_SUPPORTED, SHOW_ADFEATURE+feature.name());
	}

	@Override
	public void hideAdFeature(AdFeature feature) {
		Gdx.app.log(KyperBoxGame.NOT_SUPPORTED, HIDE_ADFEATURE+feature.name());
	}

	@Override
	public boolean isAdFeatureVisible(AdFeature feature) {
		return false;
	}

	@Override
	public void setAdFeatureListener(AdFeature feature, AdFeatureListener listener) {
		
	}

}
