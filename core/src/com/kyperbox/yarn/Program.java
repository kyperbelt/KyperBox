package com.kyperbox.yarn;

import com.badlogic.gdx.utils.Array;
import com.kyperbox.yarn.Lexer.Token;
import com.kyperbox.yarn.Lexer.TokenType;

public class Program {
	
	protected static class ParseException extends RuntimeException{
		private static final long serialVersionUID = -6422941521497633431L;
		
		protected int line_number = 0;
		
		public ParseException(String message) {
			super(message);
		}
		
		protected static ParseException make(Token found_token,TokenType...expected_types) {
			int line_number = found_token.line_number + 1;

			Array<String> expected_type_names = new Array<String>();
			for (TokenType type : expected_types) {
				expected_type_names.add(type.name());
			}
			String possible_values = String.join(",", expected_type_names);
			String message = String.format("Line %1$s:%2$s: Expected %3$s, but found %4$s",
					   line_number,
					   found_token.column_number,
					   possible_values,
					   found_token.type.name()
					   );
			ParseException e = new ParseException(message);
			e.line_number = line_number;
			return e;
		}
		
		protected static ParseException make(Token most_recent_token,String message) {
			int line_number = most_recent_token.line_number+1;
			String m = String.format("Line %1$s:%2$s: %3$s",
					 line_number,
					most_recent_token.column_number,
					 message);
			ParseException e = new ParseException(m);
			e.line_number = line_number;
			return e;
		}
	}

}
