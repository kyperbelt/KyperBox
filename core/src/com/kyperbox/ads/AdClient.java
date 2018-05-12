package com.kyperbox.ads;

public interface AdClient {

	public static enum AdFeature {
		BANNER, INTERSTITIAL, REWARD
	}

	/** check whether an ad feature is supported */
	public boolean isSupported(AdFeature feature);

	/**
	 * show the requested feature ad, if it is not supported then it will be ignored
	 */
	public void showAdFeature(AdFeature feature);

	/**
	 * hide the requested feature ad, if it is not supported then it will be ignored
	 * [note that not all ads can be hidden]
	 */
	public void hideAdFeature(AdFeature feature);

	/** check to see if the current ad feature is visible */
	public boolean isAdFeatureVisible(AdFeature feature);

	/**
	 * set the listener for the feature - each feature may only have one listener
	 * but each listener is not limited to one feature.
	 */
	public void setAdFeatureListener(AdFeature feature, AdFeatureListener listener);

}
