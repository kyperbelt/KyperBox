package com.kyperbox.yarn;

public class Loader {
	public enum NodeFormat{
		Unkown,						//type not known
		
		SingleNodeText, 			//plain text file with single node & no metadata
		
		Json, 						//json file containing multiple nodes with metadata
		
		Text						//text containing multiple nodes with metadata
	}
	
	
}
