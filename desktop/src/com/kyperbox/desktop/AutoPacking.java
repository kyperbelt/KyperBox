package com.kyperbox.desktop;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;

public class AutoPacking {
	
	public static String input_folder = "../../input_assets";
	public static String output_folder = "./";
	public static int size  = 2048;
	public static String extension = ".atlas";
	public static boolean strip_whitespace = true;
	public static boolean use_index = false;

	
	public static void pack(String input_asset_folder,String output_asset_folder,String output_file_name) {
		Settings settings = new Settings();
		settings.useIndexes = use_index;
		settings.pot = true;
		settings.maxWidth  = size;
		settings.maxHeight = size;
		settings.alias = false;
//		settings.stripWhitespaceX = strip_whitespace;//
//		settings.stripWhitespaceY = strip_whitespace;
		
		settings.atlasExtension = extension;
		
		TexturePacker.process(settings, input_folder+"/"+input_asset_folder,output_folder+output_asset_folder, output_file_name);
	}

}
