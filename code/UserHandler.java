import java.util.ArrayList;
import java.util.HashMap;
import web.framework.Context;
import web.framework.Freemarker;
import web.framework.Redirect;

class UserHandler {
	
	static boolean valid(Object o) {
		return o != null;
	}
	
	static Object askEmail(Context context) {
		User user = (User)context.getSession("user");
		String language = (String)context.request.getAttribute("language");
		if (valid(user)) return new Redirect("/" + language + "/profile");
		
		var model = Start.createModel(context);
		return Freemarker.render("/web/ask-email.html", model);
	}
	
	static Object resetPassword(Context context) {
		User user = (User)context.getSession("user");
		String language = (String)context.request.getAttribute("language");
		if (valid(user)) return new Redirect("/" + language + "/profile");
		
		var model = Start.createModel(context);
		return Freemarker.render("/web/reset-password.html", model);
	}
	
	static Object askPassword(Context context) {
		User user = (User)context.getSession("user");
		String language = (String)context.request.getAttribute("language");
		if (valid(user)) return new Redirect("/" + language + "/profile");
		
		String email = (String)context.getSession("email");
		if (email == null) {
			return new Redirect("/" + language + "/ask-email");
		}
		
		var model = Start.createModel(context);
		return Freemarker.render("/web/ask-password.html", model);
	}
	
	static Object createAccount(Context context) {
		User user = (User)context.getSession("user");
		String language = (String)context.request.getAttribute("language");
		if (valid(user)) return new Redirect("/" + language + "/profile");
		
		String email = (String)context.getSession("email");
		if (email == null) {
			return new Redirect("/" + language + "/ask-email");
		}
		
		var model = Start.createModel(context);
		return Freemarker.render("/web/create-account.html", model);
	}
	
	static Object showProfile(Context context) {
		String language = (String)context.request.getAttribute("language");
		
		User user = (User)context.getSession("user");
		if (user == null) return new Redirect("/" + language + "/ask-email");
		
		var model = Start.createModel(context);
		return Freemarker.render("/web/user-profile.html", model);
	}
	
	static Object showPassword(Context context) {
		User user = (User)context.getSession("user");
		String language = (String)context.request.getAttribute("language");
		if (user == null) return new Redirect("/" + language + "/ask-email");
		
		var model = Start.createModel(context);
		return Freemarker.render("/web/password.html", model);
	}
	
	static Object showPhotoManagement(Context context) {
		User user = (User)context.getSession("user");
		String language = (String)context.request.getAttribute("language");
		if (user == null) return new Redirect("/" + language + "/ask-email");
		
		var model = Start.createModel(context);
		return Freemarker.render("/web/user-photo.html", model);
	}
	
	static Object showSignature(Context context) {
		User user = (User)context.getSession("user");
		String language = (String)context.request.getAttribute("language");
		if (user == null) return new Redirect("/" + language + "/ask-email");
		
		var model = Start.createModel(context);
		return Freemarker.render("/web/user-signature.html", model);
	}
	
	static Object showRemoveAccount(Context context) {
		User user = (User)context.getSession("user");
		String language = (String)context.request.getAttribute("language");
		if (user == null) return new Redirect("/" + language + "/ask-email");
		
		var model = Start.createModel(context);
		return Freemarker.render("/web/remove-account.html", model);
	}
	
	static Object showSettings(Context context) {
		User user = (User)context.getSession("user");
		String language = (String)context.request.getAttribute("language");
		if (user == null) return new Redirect("/" + language + "/ask-email");
		
		ArrayList<Language> languages = Storage.listLanguages();
		
		var model = Start.createModel(context);
		model.put("languages", languages);
		
		return Freemarker.render("/web/user-settings.html", model);
	}
	
	static Object showStatus(Context context) {
		User user = (User)context.getSession("user");
		String language = (String)context.request.getAttribute("language");
		if (user == null) return new Redirect("/" + language + "/ask-email");
		
		var model = Start.createModel(context);
		return Freemarker.render("/web/user-status.html", model);
	}

}
