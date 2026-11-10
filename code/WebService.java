import java.util.TreeMap;
import java.util.ArrayList;
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
	
}
