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

	String test_text = "This is test text\r\n" + 
			"-> this is an option << (1+1) >> \r\n" + 
			"\r\n" + 
			"//these are comments\r\n" + 
			"\r\n" + 
			"[[second_node |test_node]]";

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
