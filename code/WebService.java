import java.util.TreeMap;
import java.util.ArrayList;
import java.util.HashMap;
import web.framework.Context;

class WebService {

	static ArrayList<Language> languages = new ArrayList<>();
	static ArrayList<TreeMap<String,Object>> listLanguages = new ArrayList<>();
	
	static void
	refresh() {
		languages.clear();
		listLanguages.clear();
		
		ArrayList<Language> items = Storage.listLanguages();
		for (Language lang : items) {
			languages.add(lang);
		}
		
		for (Language lang : languages) {
			TreeMap<String, Object> item = new TreeMap<>();
			item.put("number", lang.number);
			item.put("name",   lang.name);
			item.put("alias",  lang.alias);
			listLanguages.add(item);
		}
	}
	
	static Object
	listLanguages(Context context) {
		TreeMap<String, Object> result = new TreeMap<>();
		result.put("result", "OK");
		result.put("data", listLanguages);
		return result;
	}
	
	static Object
	getUserStatus(Context context) {
		String signature = context.getParameter("signature");
		
		String status = Storage.getUserStatus(signature);

		TreeMap<String, Object> result = new TreeMap<>();
		result.put("result", "OK");
		result.put("status", status);
		result.put("signature", signature);
		return result;
	}
	
	
	static Object
	translate(Context context) {
		String code = context.getParameter("code");
		String language = context.getParameter("language");

		TreeMap<String, Object> result = new TreeMap<>();
		result.put("result",   "OK");
		result.put("code",     code);
		result.put("language", language);
		result.put("text", "");
		
		HashMap<String, String> list = Translation.map.get(language);
		if (list == null) return result;
		String text = list.get(code);
		if (text == null) text = "";
		result.put("text", text);
		
		System.out.println(result);
		
		return result;
	}
	
}
