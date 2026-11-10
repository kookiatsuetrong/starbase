import java.util.ArrayList;
import java.util.HashMap;
import web.framework.Context;
import web.framework.Freemarker;
import web.framework.Redirect;

class LanguageHandler {
	
	// GET /manage-language
	static Object
	show(Context context) {
		String language = (String)context.request.getAttribute("language");
		
		// S0: Check current user
		User user = (User)context.getSession("user");
		if (user == null) return new Redirect("/" + language + "/profile");
		
		// M1: Update model
		// HashMap<String, Object> model = new HashMap<>();
		var model = Start.createModel(context);
		model.put("user", user);
		
		return Freemarker.render("/web/language-list.html", model);
	}
}
