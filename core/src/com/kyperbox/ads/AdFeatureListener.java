package com.kyperbox.ads;

import com.kyperbox.ads.AdClient.AdFeature;

public interface AdFeatureListener {
	
	/**there was an error*/
	public void onError(AdFeature feature,String message);
	
	/**feature is ready to display*/
	public void onReady(AdFeature feature);
	
	/** feature is being shown*/
	public void onShow(AdFeature feature);
	
	/** feature is now hidden*/
	public void onHide(AdFeature feature);
	

}
