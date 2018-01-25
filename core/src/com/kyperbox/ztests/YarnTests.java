package com.kyperbox.ztests;

import com.kyperbox.KyperBoxGame;
import com.kyperbox.yarn.Lexer;
import com.kyperbox.yarn.Library;
import com.kyperbox.yarn.Parser;
import com.kyperbox.yarn.Parser.Node;

public class YarnTests extends KyperBoxGame {
	
	enum Type{
		Fishy,
		Testerman
	}

	String test_text = "title: start\r\n" + 
			"---\r\n" + 
			"A: Hey, I'm a character in a script!\r\n" + 
			"B: And I am too! You are talking to me!\r\n" + 
			"-> What's going on\r\n" + 
			"    A: Why this is a demo of the script system!\r\n" + 
			"    B: And you're in it!\r\n" + 
			"-> Um ok\r\n" + 
			"===";

	@Override
	public void initiate() {
		Lexer lexer = new Lexer();
		System.out.println(lexer.tokenise(test_text).toString());
		Library lib = new Library();
		Parser parser = new Parser(lexer.tokenise(test_text), lib);
		Node node = parser.parse();
		System.out.println(node.printTree(0));
	}

}
