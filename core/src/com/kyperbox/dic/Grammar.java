package com.kyperbox.dic;

import com.badlogic.gdx.utils.Array;

public class Grammar {
	public static final int INVALID = -1; //value was not valid TODO: remove
	public static final int EMPTY = 0;

	private String grammar;
	private Array<String> words;

	public Grammar(String grammar) {
		this.grammar = grammar;
		this.words = null;
	}

	public void addWord(String word) {
		if (!hasWords())
			words = new Array<String>();
		words.add(word);
	}

	public Array<String> words() {
		return words;
	}

	public void removeWord(String word) {
		if (!hasWords())
			return;
		words.removeValue(word, false);
		if (words.size == 0)
			words = null;
	}

	public int getWordIndex(String word) {
		if (!hasWords())
			return INVALID;
		for (int i = 0; i < words.size; i++) {
			if (word.equalsIgnoreCase(words.get(i)))
				return i;
		}
		return INVALID;
	}

	public boolean containsWord(String word) {
		if (!hasWords())
			return false;
		return words.contains(word, false);
	}

	public String getWord(int word) {
		if (hasWords()) {
			return words.get(word);
		}
		return "" + INVALID;
	}

	public int wordCount() {
		if (hasWords())
			return words.size;
		return EMPTY;
	}

	public boolean hasWords() {
		return words != null;
	}

	public String getGrammar() {
		return grammar;
	}

	public void clear() {
		words.clear();
		words = null;
	}

}
