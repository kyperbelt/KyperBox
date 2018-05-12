package com.kyperbox.dic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.badlogic.gdx.utils.Array;

public class StringParser {

	public static Array<Rule> RULES = new Array<Rule>();

	//parse rules
	static {
		RULES.add(new Rule("capitalize") { //capitalize the first letter of the word
			@Override
			public String apply(String input) {
				String output = input.substring(0, 1).toUpperCase() + input.substring(1);
				return output;
			}
		});
		RULES.add(new Rule("tolowercase") { //all made to lower case
			@Override
			public String apply(String input) {
				return input.toLowerCase();
			}
		});
		RULES.add(new Rule("uppercase") {
			@Override
			public String apply(String input) {
				return input.toUpperCase();
			}
		});
		RULES.add(new Rule("plural") {
			@Override
			public String apply(String input) {
				String output = "";
				if (input.endsWith("y") && !(input.equalsIgnoreCase("toy") || input.equalsIgnoreCase("boy"))) {
					output = input.replace("y", "ies");
				} else if (input.endsWith("o") || input.endsWith("s")) {
					output = input + "es";
				} else if (input.equalsIgnoreCase("woman")) {
					output = "women";
				} else if (input.equalsIgnoreCase("fox")) {
					output = "foxes";
				} else if (input.equalsIgnoreCase("ox")) {
					output = "oxen";
				} else if (input.equalsIgnoreCase("man")) {
					output = "men";
				} else if (input.equalsIgnoreCase("goose")) {
					output = "geese";
				} else {
					output = input + "s";
				}
				return output;
			}
		});
		RULES.add(new Rule("ing") {
			@Override
			public String apply(String input) {
				String output = "";
				if (input.endsWith("e")) {
					output = input.replace("e", "ing");
				} else {
					output = input + "ing";
				}
				return output;
			}
		});
	}

	private static final String BRACKPATTERN = "\\{(.*?)\\}";
	
	/**
	 * gets all the grammar in the given input example:{grammar.rule}
	 * 
	 * @param input
	 * @return
	 */
	public static Array<String> getGrammar(String input) {
		Array<String> a = new Array<String>();
		Pattern p = Pattern.compile(BRACKPATTERN);
		Matcher m = p.matcher(input);
		while (m.find()) {
			a.add(m.group(1));
		}
		return a;
	}

	/**
	 * applies the rule to the input
	 * 
	 * @param input
	 * @param rule
	 * @return
	 */
	public static String applyRule(String input, String rule) {
		for (int i = 0; i < RULES.size; i++) {
			Rule r = RULES.get(i);
			if (r.getRuleName().equalsIgnoreCase(rule)) {
				return r.apply(input);
			}
		}
		return input;
	}

	public static abstract class Rule {

		private String rule;

		public Rule(String rule) {
			this.rule = rule;
		}

		public String getRuleName() {
			return rule;
		}

		public abstract String apply(String input);
	}

}