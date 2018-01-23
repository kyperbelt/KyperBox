package com.kyperbox.ztests;

import com.kyperbox.KyperBoxGame;
import com.kyperbox.yarn.Lexer;

public class YarnTests extends KyperBoxGame {

	String test_text = "->hello my name is jon \n\r" 
						+ "this is a new line \n\r" 
						+ "if(1+1)";

	@Override
	public void initiate() {
		Lexer lexer = new Lexer();
		System.out.println(lexer.tokenise("poop", test_text).toString());

	}

}
