package com.kyperbox.dic;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * 
 * @author kyperbelt
 *
 */
public class Dictionary {
	
	//random number generator
	private Random random;
	//map of grammar
	private ObjectMap<String, Grammar> grammar;
	
	public Dictionary(){
		grammar = new ObjectMap<String,Grammar>();
		random = new Random();
	}
	
	/**
	 * Adds a grammar object to the grammar map
	 * @param g
	 * @return false if already exists
	 */
	public boolean addGrammar(Grammar g){
		if(grammar.containsKey(g.getGrammar()))
			return false;
		grammar.put(g.getGrammar(), g);
		return true;
	}
	
	/**
	 * adds a grammar object based on the string
	 * @param g
	 * @return false if grammar already exists
	 */
	public boolean addGrammar(String g){
		if(grammar.containsKey(g))
			return false;
		Grammar ga = new Grammar(g);
		grammar.put(g, ga);
		return true;
	}
	
	/**
	 * Get a grammar object contained in the grammar map
	 * @param g
	 * @return 
	 */
	public Grammar getGrammar(String g){
		if(grammar.containsKey(g))
			return grammar.get(g);
		return null;
	}
	
	/**
	 * Get a random 'word' from the given grammar
	 * @param g
	 * @return
	 */
	private String getRandomGrammarWord(String g){
		if(!grammar.containsKey(g))
			return "[NotFound:"+g+"]";
		Grammar ga = grammar.get(g);
		String word = ga.getWord(random.nextInt(ga.wordCount()));
		if(word.equals(""+Grammar.INVALID))
			word = "[invalid:"+g+"]";
		return word;
	}
	
	
	private static final String CURLYFORWARD = "\\{";
	private static final String CURLYBACKWARD = "\\}";
	private static final String PERIOD = "\\.";
	/**
	 * parse the the input into awesome generated text 100%
	 * random so cool much wow!
	 * @param input
	 * @return
	 */
	public String parse(String input){
		String s = input;
		Array<String> grammars = StringParser.getGrammar(input);
		for (int i = 0; i < grammars.size; i++) {
			String compound = grammars.get(i);
			String rule = null;
			String word = compound;
			
			if(compound.contains(".")){
				String split[] = compound.split(PERIOD);
				word = split[0];
				word = getRandomGrammarWord(word);
				rule = split[1];
				word = StringParser.applyRule(word, rule);
			}else{
				word = getRandomGrammarWord(compound);
			}
			
			if(StringParser.getGrammar(word).size>0){
				word = parse(word);
			}
			 s = s.replaceFirst(CURLYFORWARD+compound+CURLYBACKWARD, word);
		}
		return s;
	}
	
	public static Dictionary loadFromFile(InputStream file){
		Dictionary d = new Dictionary();
		
		JsonReader mapper = new JsonReader();
		JsonValue root = null;
		
		root = mapper.parse(file);
		Iterator<JsonValue> fields = root.iterator();
		
		while(fields.hasNext()){
			JsonValue node = fields.next();
			Grammar g = new Grammar(node.name);
			for (int i = 0; i < node.size; i++) {
				
				String word = node.get(i).asString();
				g.addWord(word);
			}
			d.addGrammar(g);
		}
		
		return d;
	}
	
	public static Dictionary loadFromPreffrence(String pref){
		Preferences p = Gdx.app.getPreferences(pref);
		String data = p.getString("data");
		
		return loadFromString(data); 
	}
	
	public static Dictionary loadFromString(String data) {
		InputStream stream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
		return loadFromFile(stream);
	}
	
	
	public static String writeToString(Dictionary dic){
		String document = "{\n";
		Array<String> grammar = dic.grammar.keys().toArray();
		for (int i = 0; i < grammar.size; i++) {
			String g = grammar.get(i);
			document+="\""+g+"\":[";
			Array<String> words = dic.grammar.get(g).words();
			if(words!=null){
				for (int j = 0; j < words.size; j++) {
					if(j!=0){
						document+=",";
					}
					document+="\""+words.get(j)+"\"";
				}
			}
			document+="],\n";
		}
		document+="\n}";
		
		return document;
	}
	
	public static void writeToPrefs(String pref,Dictionary dic){
		Preferences p = Gdx.app.getPreferences(pref);
		p.putString("data", writeToString(dic));
		p.flush();
	}

}
