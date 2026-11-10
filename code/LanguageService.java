import java.util.ArrayList;
import java.util.TreeMap;
import web.framework.Context;

class LanguageService {
	
	@SuppressWarnings("unchecked")
	static Object
	add(Context context) {
		User user = (User)context.getSession("user");
		if (user == null) {
			return Tool.createError("invalid-user");
		}
		
		TreeMap<String,Object> input = (TreeMap<String,Object>)context.read();
		
		ArrayList<String> data = (ArrayList<String>)input.get("data");
		
		boolean result = false;
		
		if (result == false) {
			return Tool.createError("unable-to-update-language");
		}
		
		return Tool.createSuccess("language-has-been-updated");
	}
	
}
