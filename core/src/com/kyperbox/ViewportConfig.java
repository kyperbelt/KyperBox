package com.kyperbox;

import com.badlogic.gdx.utils.viewport.*;

public class ViewportConfig {
	public ViewportType type;
	public int width;
	public int height;
	
	public ViewportConfig() {}
	
	public ViewportConfig(Viewport view) {
		ViewportType[] types = ViewportType.values();
		for (int i = 0; i < types.length; i++) {
			if(types[i].isSame(view)) {
				type = types[i];
			}
		}
		this.width = (int) view.getWorldWidth();
		this.height = (int) view.getWorldHeight();
	}
	
	public static enum ViewportType{
		STRETCH(StretchViewport.class),
		FIT(FitViewport.class),
		FILL(FillViewport.class),
		SCREEN(ScreenViewport.class);
		
		public final Class<?> type;
		ViewportType(Class<?> type) {
			this.type = type;
		}
		
		public boolean isSame(Viewport view) {
			return view.getClass() == type;
		}
	}
}
