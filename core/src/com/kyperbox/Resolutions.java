package com.kyperbox;

public class Resolutions {
	
	public static final Resolution _1080 = new Resolution(1920, 1080);
	public static final Resolution _720 = new Resolution(1280,720);
	public static final Resolution _900 = new Resolution(1440, 900);
	public static final Resolution _380 = new Resolution(640, 380);
	
	public static class Resolution{
		private int width;
		private int height;
		public Resolution(int width,int height) {
			this.width = width;
			this.height = height;
		}
		
		public int WIDTH() {
			return width;
		}
		
		public int HEIGHT() {
			return height;
		}
	}

}
