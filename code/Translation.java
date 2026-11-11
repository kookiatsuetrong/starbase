/**
 * Translation.java
 * 
 */
import java.util.ArrayList;
import java.util.HashMap;

class Translation {
	
	static String defaultLanguage = "en-us";
	
	static HashMap<String, HashMap<String, String>> map = new HashMap<>();
	
	public static void
	refresh() {
		for (String key : map.keySet()) {
			HashMap<String,String> language = map.get(key);
			language.clear();
		}
		map.clear();
		
		ArrayList<Language> list = Storage.listLanguages();
		for (Language item : list) {
			map.put(item.alias, new HashMap<>() );
		}
		
		map = Storage.refreshTranslation();
		
		// Make English be the default language ("en-us")
		var defaultMap = map.get(Translation.defaultLanguage);
		
		for (String language : map.keySet()) {
			var current = map.get(language);
			
			for (String key : defaultMap.keySet()) {
				if (current.containsKey(key)) {
					/*
					System.out.println("Language " + language + 
										" has " + key);
					*/
				}
				if (current.containsKey(key) == false) {
					String value = defaultMap.get(key);
					current.put(key, value);
				}
			}
		}
		
		System.out.println("Translation is ready");

	}
	
	
	public static HashMap<String, String>
	getTranslation(String alias) {
		HashMap<String, String> language = map.get(alias);
		return language;
	}
	
}
